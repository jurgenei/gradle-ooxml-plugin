package name.jurgenei.gradle.ooxml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Small runner to canonicalize one OOXML file and report suspicious singleton graphs.
 */
public final class DiagramAuditRunner {
    private DiagramAuditRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: DiagramAuditRunner <input-docx/pptx/xlsx> [output-xml]");
            System.exit(2);
        }

        Path input = Path.of(args[0]);
        Path output = args.length == 2
                ? Path.of(args[1])
                : input.getParent().resolve(input.getFileName().toString().replaceAll("\\.[^.]+$", "") + ".canonical.xml");

        OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();
        CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();
        serializer.write(canonicalizer.canonicalize(input.toFile()), output);

        String xml = Files.readString(output);
        Report report = inspect(xml);
        System.out.println("canonical=" + output.toAbsolutePath());
        System.out.println("graphs=" + report.graphs + ", suspicious-singletons=" + report.suspiciousSingletons);
        for (SuspiciousGraph suspiciousGraph : report.suspiciousGraphs) {
            System.out.println("suspicious source-path=" + suspiciousGraph.sourcePath
                    + " tokens=" + suspiciousGraph.assetTextTokens
                    + " preview=" + suspiciousGraph.preview);
        }
    }

    static Report inspect(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        NodeList graphs = document.getElementsByTagNameNS(CanonicalNamespace.GRAPHML_URI, "graph");
        int suspicious = 0;
        List<SuspiciousGraph> suspiciousGraphs = new ArrayList<>();
        for (int i = 0; i < graphs.getLength(); i++) {
            Element graph = (Element) graphs.item(i);
            int nodeCount = childCount(graph, CanonicalNamespace.GRAPHML_URI, "node");
            AssetTextInfo assetTextInfo = maxAssetTextTokens(graph);
            int tokenCount = assetTextInfo.tokens();
            if (nodeCount <= 1 && tokenCount >= 8 && !isLegendOrEcosystemDiagram(assetTextInfo.preview())) {
                suspicious++;
                suspiciousGraphs.add(new SuspiciousGraph(
                        graph.getAttribute("source-path"),
                        tokenCount,
                        assetTextInfo.preview()
                ));
            }
        }
        return new Report(graphs.getLength(), suspicious, suspiciousGraphs);
    }

    private static int childCount(Element parent, String ns, String local) {
        int count = 0;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element
                    && ns.equals(element.getNamespaceURI())
                    && local.equals(element.getLocalName())) {
                count++;
            }
        }
        return count;
    }

    private static AssetTextInfo maxAssetTextTokens(Element graph) {
        int max = 0;
        String preview = "";
        NodeList children = graph.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element element)) {
                continue;
            }
            if (!CanonicalNamespace.GRAPHML_URI.equals(element.getNamespaceURI()) || !"annotation".equals(element.getLocalName())) {
                continue;
            }
            String kind = element.getAttribute("kind");
            if (!"asset-text".equals(kind)) {
                continue;
            }
            String text = element.getTextContent() == null ? "" : element.getTextContent();
            int tokens = (int) java.util.Arrays.stream(text.split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).count();
            if (tokens > max) {
                max = tokens;
                preview = text.length() > 140 ? text.substring(0, 140) + "..." : text;
            }
        }
        return new AssetTextInfo(max, preview);
    }

    private static boolean isLegendOrEcosystemDiagram(String preview) {
        String normalized = preview == null ? "" : preview.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("legend for the diagrams")
                || normalized.contains("functional component")
                || normalized.contains("outside ing")
                || normalized.contains("other agile")
                || normalized.contains("knowledge centre")
                || normalized.contains("reference data management")
                || normalized.contains("architects");
    }

    record Report(int graphs, int suspiciousSingletons, List<SuspiciousGraph> suspiciousGraphs) {
    }

    record AssetTextInfo(int tokens, String preview) {
    }

    record SuspiciousGraph(String sourcePath, int assetTextTokens, String preview) {
    }
}

