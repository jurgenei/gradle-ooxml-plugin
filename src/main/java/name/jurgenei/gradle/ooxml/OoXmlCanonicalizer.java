package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.Body;
import name.jurgenei.gradle.ooxml.canonical.CanonicalList;
import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import name.jurgenei.gradle.ooxml.canonical.Cell;
import name.jurgenei.gradle.ooxml.canonical.Connector;
import name.jurgenei.gradle.ooxml.canonical.Diagram;
import name.jurgenei.gradle.ooxml.canonical.Link;
import name.jurgenei.gradle.ooxml.canonical.ListItem;
import name.jurgenei.gradle.ooxml.canonical.Metadata;
import name.jurgenei.gradle.ooxml.canonical.Paragraph;
import name.jurgenei.gradle.ooxml.canonical.Reference;
import name.jurgenei.gradle.ooxml.canonical.Row;
import name.jurgenei.gradle.ooxml.canonical.Shape;
import name.jurgenei.gradle.ooxml.canonical.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Maps OOXML package structures to canonical model objects.
 *
 * <p>The canonicalizer keeps format-specific parsing details internal and emits a stable
 * canonical representation used by downstream validation and transformation tasks.</p>
 */
final class OoXmlCanonicalizer {
    private static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    /**
     * Canonicalizes a single OOXML package.
     *
     * @param inputFile source OOXML document.
     * @return canonical model.
     * @throws IOException when package access or XML parsing fails.
     */
    CanonicalDocument canonicalize(File inputFile) throws IOException {
        String fileName = inputFile.getName();
        String stem = stem(fileName);
        VersionResolver.ResolvedVersion resolvedVersion = VersionResolver.resolve(stem);
        String documentType = resolveDocumentType(fileName);
        Extraction extraction = extractByFormat(inputFile, documentType, fileName);
        return buildCanonicalModel(fileName, documentType, resolvedVersion, extraction);
    }

    private Extraction extractDocx(File inputFile) throws IOException {
        String sourceDocument = inputFile.getName();
        try (ZipFile zipFile = new ZipFile(inputFile)) {
            ZipEntry entry = zipFile.getEntry("word/document.xml");
            if (entry == null) {
                return new Extraction(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            try (InputStream input = zipFile.getInputStream(entry)) {
                Document document = parseXml(input);
                return new Extraction(
                        extractDocxParagraphs(document, sourceDocument),
                        extractDocxLists(document),
                        extractDocxTables(document),
                        extractDocxLinks(document, readRelationships(zipFile, "word/_rels/document.xml.rels")),
                        extractDocxReferences(document),
                        extractDocxDiagrams(document)
                );
            }
        }
    }

    private Extraction extractPptx(File inputFile) throws IOException {
        String sourceDocument = inputFile.getName();
        List<Paragraph> paragraphs = new ArrayList<>();
        List<CanonicalList> lists = new ArrayList<>();
        List<Table> tables = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        List<Reference> references = new ArrayList<>();
        List<Diagram> diagrams = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(inputFile)) {
            List<ZipEntry> slides = findEntries(zipFile, "ppt/slides/slide", ".xml");
            for (ZipEntry slide : slides) {
                try (InputStream input = zipFile.getInputStream(slide)) {
                    Document document = parseXml(input);
                    String sourcePath = "/" + normalize(slide.getName());
                    Map<String, String> relationships = readRelationships(zipFile, relationshipPartPath(normalize(slide.getName())));
                    paragraphs.addAll(extractSlideParagraphs(document, sourceDocument, sourcePath));
                    lists.addAll(extractSlideLists(document));
                    tables.addAll(extractSlideTables(document));
                    links.addAll(extractSlideLinks(document, relationships));
                    references.addAll(extractSlideReferences(document));
                    diagrams.addAll(extractSlideDiagrams(document));
                }
            }
        }
        return new Extraction(paragraphs, lists, tables, links, references, diagrams);
    }

    private Extraction extractXlsx(File inputFile) throws IOException {
        String sourceDocument = inputFile.getName();
        List<Paragraph> paragraphs = new ArrayList<>();
        List<Table> tables = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        List<Reference> references = new ArrayList<>();
        List<Diagram> diagrams = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(inputFile)) {
            List<String> shared = readSharedStrings(zipFile);
            List<ZipEntry> sheets = findEntries(zipFile, "xl/worksheets/sheet", ".xml");
            for (ZipEntry sheet : sheets) {
                try (InputStream input = zipFile.getInputStream(sheet)) {
                    Document document = parseXml(input);
                    String sheetPath = normalize(sheet.getName());
                    Map<String, String> relationships = readRelationships(zipFile, relationshipPartPath(sheetPath));
                    SheetExtraction sheetExtraction = extractSheet(document, shared, sourceDocument, sheetPath, relationships);
                    paragraphs.addAll(sheetExtraction.paragraphs());
                    if (!sheetExtraction.table().getRows().isEmpty()) {
                        tables.add(sheetExtraction.table());
                    }
                    links.addAll(sheetExtraction.links());
                    references.addAll(sheetExtraction.references());
                }
            }
            diagrams.addAll(extractDrawingDiagrams(zipFile));
        }
        return new Extraction(paragraphs, List.of(), tables, links, references, diagrams);
    }

    private List<String> readSharedStrings(ZipFile zipFile) throws IOException {
        ZipEntry sharedStrings = zipFile.getEntry("xl/sharedStrings.xml");
        if (sharedStrings == null) {
            return List.of();
        }
        try (InputStream input = zipFile.getInputStream(sharedStrings)) {
            List<String> texts = extractTextNodes(parseXml(input));
            return texts.isEmpty() ? List.of() : texts;
        }
    }

    private List<String> extractTextNodes(Document doc) {
        NodeList textNodes = doc.getElementsByTagNameNS("*", "t");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < textNodes.getLength(); i++) {
            String text = textNodes.item(i).getTextContent();
            if (text != null) {
                String trimmed = text.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }
        return values;
    }

    private List<Paragraph> asParagraphs(List<String> texts, String sourceDocument, String sourcePathPrefix) {
        List<Paragraph> paragraphs = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            paragraphs.add(new Paragraph(text, sourceDocument, sourcePathPrefix + "/text[" + (i + 1) + "]"));
        }
        return paragraphs;
    }

    private CanonicalDocument buildCanonicalModel(String fileName, String documentType,
                                                  VersionResolver.ResolvedVersion resolvedVersion,
                                                  Extraction extraction) {
        Metadata metadata = new Metadata(
                resolvedVersion.documentId(),
                resolvedVersion.version(),
                fileName,
                documentType
        );
        return new CanonicalDocument(
                metadata,
                new Body(
                        extraction.paragraphs(),
                        extraction.lists(),
                        extraction.tables(),
                        extraction.links(),
                        extraction.references(),
                        extraction.diagrams()
                )
        );
    }

    private Extraction extractByFormat(File inputFile, String documentType, String sourceDocument) throws IOException {
        return switch (documentType) {
            case "DOCX" -> extractDocx(inputFile);
            case "PPTX" -> extractPptx(inputFile);
            case "XLSX" -> extractXlsx(inputFile);
            default -> throw new IOException("Unsupported OOXML type: " + sourceDocument);
        };
    }

    private List<Paragraph> extractDocxParagraphs(Document document, String sourceDocument) {
        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        List<Paragraph> paragraphs = new ArrayList<>();
        for (int i = 0; i < paragraphNodes.getLength(); i++) {
            Element paragraph = (Element) paragraphNodes.item(i);
            String text = extractNestedText(paragraph);
            if (!text.isEmpty()) {
                paragraphs.add(new Paragraph(text, sourceDocument, "/word/document/p[" + (i + 1) + "]"));
            }
        }
        return paragraphs;
    }

    private List<CanonicalList> extractDocxLists(Document document) {
        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        List<ListItem> items = new ArrayList<>();
        for (int i = 0; i < paragraphNodes.getLength(); i++) {
            Element paragraph = (Element) paragraphNodes.item(i);
            if (containsDescendant(paragraph, "numPr")) {
                String text = extractNestedText(paragraph);
                if (!text.isEmpty()) {
                    items.add(new ListItem(text));
                }
            }
        }
        if (items.isEmpty()) {
            return List.of();
        }
        return List.of(new CanonicalList(false, items));
    }

    private List<Table> extractDocxTables(Document document) {
        return extractTablesByLocalNames(document, "tbl", "tr", "tc");
    }

    private List<Paragraph> extractSlideParagraphs(Document document, String sourceDocument, String sourcePathPrefix) {
        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        List<Paragraph> paragraphs = new ArrayList<>();
        for (int i = 0; i < paragraphNodes.getLength(); i++) {
            Element paragraph = (Element) paragraphNodes.item(i);
            String text = extractNestedText(paragraph);
            if (!text.isEmpty()) {
                paragraphs.add(new Paragraph(text, sourceDocument, sourcePathPrefix + "/p[" + (i + 1) + "]"));
            }
        }
        return paragraphs;
    }

    private List<CanonicalList> extractSlideLists(Document document) {
        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        List<ListItem> items = new ArrayList<>();
        boolean ordered = false;
        for (int i = 0; i < paragraphNodes.getLength(); i++) {
            Element paragraph = (Element) paragraphNodes.item(i);
            boolean bullet = containsDescendant(paragraph, "buChar") || containsDescendant(paragraph, "buAutoNum");
            if (!bullet) {
                continue;
            }
            ordered = ordered || containsDescendant(paragraph, "buAutoNum");
            String text = extractNestedText(paragraph);
            if (!text.isEmpty()) {
                items.add(new ListItem(text));
            }
        }
        if (items.isEmpty()) {
            return List.of();
        }
        return List.of(new CanonicalList(ordered, items));
    }

    private List<Table> extractSlideTables(Document document) {
        return extractTablesByLocalNames(document, "tbl", "tr", "tc");
    }

    private List<Link> extractDocxLinks(Document document, Map<String, String> relationships) {
        NodeList hyperlinkNodes = document.getElementsByTagNameNS("*", "hyperlink");
        List<Link> links = new ArrayList<>();
        for (int i = 0; i < hyperlinkNodes.getLength(); i++) {
            Element hyperlink = (Element) hyperlinkNodes.item(i);
            String relationshipId = relationshipId(hyperlink);
            if (relationshipId.isEmpty()) {
                continue;
            }
            String target = relationships.getOrDefault(relationshipId, relationshipId);
            String text = extractNestedText(hyperlink);
            links.add(new Link(target, text));
        }
        return links;
    }

    private List<Reference> extractDocxReferences(Document document) {
        NodeList hyperlinkNodes = document.getElementsByTagNameNS("*", "hyperlink");
        List<Reference> references = new ArrayList<>();
        for (int i = 0; i < hyperlinkNodes.getLength(); i++) {
            Element hyperlink = (Element) hyperlinkNodes.item(i);
            String anchor = hyperlink.getAttribute("w:anchor");
            if (anchor.isEmpty()) {
                anchor = hyperlink.getAttribute("anchor");
            }
            if (!anchor.isEmpty()) {
                references.add(new Reference(anchor, extractNestedText(hyperlink)));
            }
        }

        NodeList bookmarkNodes = document.getElementsByTagNameNS("*", "bookmarkStart");
        for (int i = 0; i < bookmarkNodes.getLength(); i++) {
            Element bookmark = (Element) bookmarkNodes.item(i);
            String name = bookmark.getAttribute("w:name");
            if (name.isEmpty()) {
                name = bookmark.getAttribute("name");
            }
            if (!name.isEmpty()) {
                references.add(new Reference(name, ""));
            }
        }

        return references;
    }

    private List<Diagram> extractDocxDiagrams(Document document) {
        List<Shape> shapes = new ArrayList<>();

        NodeList docPrNodes = document.getElementsByTagNameNS("*", "docPr");
        for (int i = 0; i < docPrNodes.getLength(); i++) {
            Element docPr = (Element) docPrNodes.item(i);
            shapes.add(new Shape(attributeAny(docPr, "id"), attributeAny(docPr, "name")));
        }

        if (shapes.isEmpty()) {
            return List.of();
        }
        return List.of(new Diagram(shapes, List.of()));
    }

    private List<Link> extractSlideLinks(Document document, Map<String, String> relationships) {
        NodeList linkNodes = document.getElementsByTagNameNS("*", "hlinkClick");
        List<Link> links = new ArrayList<>();
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Element linkNode = (Element) linkNodes.item(i);
            String relationshipId = relationshipId(linkNode);
            if (relationshipId.isEmpty()) {
                continue;
            }
            links.add(new Link(relationships.getOrDefault(relationshipId, relationshipId), ""));
        }
        return links;
    }

    private List<Reference> extractSlideReferences(Document document) {
        NodeList linkNodes = document.getElementsByTagNameNS("*", "hlinkClick");
        List<Reference> references = new ArrayList<>();
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Element linkNode = (Element) linkNodes.item(i);
            String action = attributeAny(linkNode, "action");
            if (!action.isEmpty()) {
                references.add(new Reference(action, ""));
            }
        }
        return references;
    }

    private List<Diagram> extractSlideDiagrams(Document document) {
        List<Shape> shapes = new ArrayList<>();
        List<Connector> connectors = new ArrayList<>();

        NodeList shapeNodes = document.getElementsByTagNameNS("*", "sp");
        for (int i = 0; i < shapeNodes.getLength(); i++) {
            Element shapeNode = (Element) shapeNodes.item(i);
            Element cNvPr = firstDescendant(shapeNode, "cNvPr");
            if (cNvPr == null) {
                continue;
            }
            shapes.add(new Shape(attributeAny(cNvPr, "id"), attributeAny(cNvPr, "name")));
        }

        NodeList connectorNodes = document.getElementsByTagNameNS("*", "cxnSp");
        for (int i = 0; i < connectorNodes.getLength(); i++) {
            Element connectorNode = (Element) connectorNodes.item(i);
            Element stCxn = firstDescendant(connectorNode, "stCxn");
            Element endCxn = firstDescendant(connectorNode, "endCxn");
            connectors.add(new Connector(
                    stCxn == null ? "" : attributeAny(stCxn, "id"),
                    endCxn == null ? "" : attributeAny(endCxn, "id")
            ));
        }

        if (shapes.isEmpty() && connectors.isEmpty()) {
            return List.of();
        }
        return List.of(new Diagram(shapes, connectors));
    }

    private SheetExtraction extractSheet(Document document,
                                         List<String> sharedStrings,
                                         String sourceDocument,
                                         String sheetPath,
                                         Map<String, String> relationships) {
        NodeList rowNodes = document.getElementsByTagNameNS("*", "row");
        List<Row> rows = new ArrayList<>();
        Map<String, String> orderedCells = new TreeMap<>();
        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element rowElement = (Element) rowNodes.item(i);
            NodeList cellNodes = rowElement.getElementsByTagNameNS("*", "c");
            List<Cell> cells = new ArrayList<>();
            for (int j = 0; j < cellNodes.getLength(); j++) {
                Element cell = (Element) cellNodes.item(j);
                String ref = cell.getAttribute("r");
                String value = resolveSheetCellValue(cell, sharedStrings);
                if (value.isEmpty()) {
                    continue;
                }
                cells.add(new Cell(value));
                if (!ref.isEmpty()) {
                    orderedCells.put(ref, value);
                }
            }
            if (!cells.isEmpty()) {
                rows.add(new Row(cells));
            }
        }

        List<Paragraph> paragraphs = new ArrayList<>();
        for (Map.Entry<String, String> cellEntry : orderedCells.entrySet()) {
            paragraphs.add(new Paragraph(
                    cellEntry.getKey() + "=" + cellEntry.getValue(),
                    sourceDocument,
                    "/" + sheetPath + "/" + cellEntry.getKey()
            ));
        }

        List<Link> links = new ArrayList<>();
        List<Reference> references = new ArrayList<>();
        NodeList hyperlinkNodes = document.getElementsByTagNameNS("*", "hyperlink");
        for (int i = 0; i < hyperlinkNodes.getLength(); i++) {
            Element hyperlink = (Element) hyperlinkNodes.item(i);
            String ref = attributeAny(hyperlink, "ref");
            String display = attributeAny(hyperlink, "display");
            String relationshipId = relationshipId(hyperlink);
            if (!relationshipId.isEmpty()) {
                links.add(new Link(relationships.getOrDefault(relationshipId, relationshipId), display.isEmpty() ? ref : display));
            }
            String location = attributeAny(hyperlink, "location");
            if (!location.isEmpty()) {
                references.add(new Reference(location, display.isEmpty() ? ref : display));
            }
        }

        return new SheetExtraction(paragraphs, new Table(rows), links, references);
    }

    private List<Diagram> extractDrawingDiagrams(ZipFile zipFile) throws IOException {
        List<ZipEntry> drawings = findEntries(zipFile, "xl/drawings/drawing", ".xml");
        List<Diagram> diagrams = new ArrayList<>();
        for (ZipEntry drawing : drawings) {
            try (InputStream input = zipFile.getInputStream(drawing)) {
                Document document = parseXml(input);
                List<Shape> shapes = new ArrayList<>();
                List<Connector> connectors = new ArrayList<>();

                NodeList cNvPrNodes = document.getElementsByTagNameNS("*", "cNvPr");
                for (int i = 0; i < cNvPrNodes.getLength(); i++) {
                    Element cNvPr = (Element) cNvPrNodes.item(i);
                    shapes.add(new Shape(attributeAny(cNvPr, "id"), attributeAny(cNvPr, "name")));
                }

                NodeList connectorNodes = document.getElementsByTagNameNS("*", "cxnSp");
                for (int i = 0; i < connectorNodes.getLength(); i++) {
                    Element connectorNode = (Element) connectorNodes.item(i);
                    Element stCxn = firstDescendant(connectorNode, "stCxn");
                    Element endCxn = firstDescendant(connectorNode, "endCxn");
                    connectors.add(new Connector(
                            stCxn == null ? "" : attributeAny(stCxn, "id"),
                            endCxn == null ? "" : attributeAny(endCxn, "id")
                    ));
                }

                if (!shapes.isEmpty() || !connectors.isEmpty()) {
                    diagrams.add(new Diagram(shapes, connectors));
                }
            }
        }
        return diagrams;
    }

    private String resolveSheetCellValue(Element cell, List<String> sharedStrings) {
        NodeList values = cell.getElementsByTagNameNS("*", "v");
        if (values.getLength() == 0) {
            return "";
        }
        String value = values.item(0).getTextContent().trim();
        if (value.isEmpty()) {
            return "";
        }
        String type = cell.getAttribute("t");
        if ("s".equals(type)) {
            try {
                int index = Integer.parseInt(value);
                if (index >= 0 && index < sharedStrings.size()) {
                    return sharedStrings.get(index);
                }
            } catch (NumberFormatException ignored) {
                return "";
            }
            return "";
        }
        return value;
    }

    private List<Table> extractTablesByLocalNames(Document document, String tableLocalName, String rowLocalName, String cellLocalName) {
        NodeList tableNodes = document.getElementsByTagNameNS("*", tableLocalName);
        List<Table> tables = new ArrayList<>();
        for (int i = 0; i < tableNodes.getLength(); i++) {
            Element tableElement = (Element) tableNodes.item(i);
            NodeList rowNodes = tableElement.getElementsByTagNameNS("*", rowLocalName);
            List<Row> rows = new ArrayList<>();
            for (int j = 0; j < rowNodes.getLength(); j++) {
                Element rowElement = (Element) rowNodes.item(j);
                NodeList cellNodes = rowElement.getElementsByTagNameNS("*", cellLocalName);
                List<Cell> cells = new ArrayList<>();
                for (int k = 0; k < cellNodes.getLength(); k++) {
                    Element cellElement = (Element) cellNodes.item(k);
                    String text = extractNestedText(cellElement);
                    if (!text.isEmpty()) {
                        cells.add(new Cell(text));
                    }
                }
                if (!cells.isEmpty()) {
                    rows.add(new Row(cells));
                }
            }
            if (!rows.isEmpty()) {
                tables.add(new Table(rows));
            }
        }
        return tables;
    }

    private Map<String, String> readRelationships(ZipFile zipFile, String relPartPath) throws IOException {
        ZipEntry relEntry = zipFile.getEntry(relPartPath);
        if (relEntry == null) {
            return Map.of();
        }

        try (InputStream input = zipFile.getInputStream(relEntry)) {
            Document relationshipsDoc = parseXml(input);
            NodeList relationshipNodes = relationshipsDoc.getElementsByTagNameNS("*", "Relationship");
            Map<String, String> relationships = new HashMap<>();
            for (int i = 0; i < relationshipNodes.getLength(); i++) {
                Element relationship = (Element) relationshipNodes.item(i);
                String id = attributeAny(relationship, "Id");
                String target = attributeAny(relationship, "Target");
                if (!id.isEmpty() && !target.isEmpty()) {
                    relationships.put(id, target);
                }
            }
            return relationships;
        }
    }

    private String relationshipPartPath(String partPath) {
        int slash = partPath.lastIndexOf('/');
        String folder = slash >= 0 ? partPath.substring(0, slash) : "";
        String name = slash >= 0 ? partPath.substring(slash + 1) : partPath;
        return (folder.isEmpty() ? "" : folder + "/") + "_rels/" + name + ".rels";
    }

    private boolean containsDescendant(Element element, String localName) {
        return element.getElementsByTagNameNS("*", localName).getLength() > 0;
    }

    private Element firstDescendant(Element element, String localName) {
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return (Element) nodes.item(0);
    }

    private String extractNestedText(Element element) {
        NodeList texts = element.getElementsByTagNameNS("*", "t");
        if (texts.getLength() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < texts.getLength(); i++) {
            String value = texts.item(i).getTextContent();
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private String relationshipId(Element element) {
        String id = element.getAttributeNS(REL_NS, "id");
        if (!id.isEmpty()) {
            return id;
        }
        return attributeAny(element, "r:id");
    }

    private String attributeAny(Element element, String attributeName) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        if (attributeName.contains(":")) {
            String local = attributeName.substring(attributeName.indexOf(':') + 1);
            if (element.hasAttribute(local)) {
                return element.getAttribute(local);
            }
        }
        return "";
    }

    private List<ZipEntry> findEntries(ZipFile zipFile, String startsWith, String endsWith) {
        List<ZipEntry> entries = new ArrayList<>();
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            String name = normalize(entry.getName());
            if (name.startsWith(startsWith) && name.endsWith(endsWith)) {
                entries.add(entry);
            }
        }
        entries.sort((left, right) -> left.getName().compareToIgnoreCase(right.getName()));
        return entries;
    }

    private String stem(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private String resolveDocumentType(String fileName) {
        return extension(fileName).toUpperCase(Locale.ROOT);
    }

    private Document parseXml(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(input);
        } catch (Exception e) {
            throw new IOException("Failed to parse XML part", e);
        }
    }

    private String normalize(String path) {
        String normalized = path.replace('\\', '/');
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1) : "";
    }

    private record Extraction(List<Paragraph> paragraphs,
                              List<CanonicalList> lists,
                              List<Table> tables,
                              List<Link> links,
                              List<Reference> references,
                              List<Diagram> diagrams) {
    }

    private record SheetExtraction(List<Paragraph> paragraphs,
                                   Table table,
                                   List<Link> links,
                                   List<Reference> references) {
    }
}

