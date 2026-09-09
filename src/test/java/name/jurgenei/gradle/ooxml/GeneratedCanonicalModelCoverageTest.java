package name.jurgenei.gradle.ooxml;

import jakarta.xml.bind.JAXBElement;
import name.jurgenei.gradle.ooxml.generated.canonical.AnnotationType;
import name.jurgenei.gradle.ooxml.generated.canonical.BodyType;
import name.jurgenei.gradle.ooxml.generated.canonical.CellType;
import name.jurgenei.gradle.ooxml.generated.canonical.ChartAxisType;
import name.jurgenei.gradle.ooxml.generated.canonical.ChartSeriesType;
import name.jurgenei.gradle.ooxml.generated.canonical.ChartType;
import name.jurgenei.gradle.ooxml.generated.canonical.ConnectorType;
import name.jurgenei.gradle.ooxml.generated.canonical.DocumentType;
import name.jurgenei.gradle.ooxml.generated.canonical.EdgeType;
import name.jurgenei.gradle.ooxml.generated.canonical.GraphType;
import name.jurgenei.gradle.ooxml.generated.canonical.GroupMemberType;
import name.jurgenei.gradle.ooxml.generated.canonical.GroupType;
import name.jurgenei.gradle.ooxml.generated.canonical.ItemType;
import name.jurgenei.gradle.ooxml.generated.canonical.LinkType;
import name.jurgenei.gradle.ooxml.generated.canonical.ListType;
import name.jurgenei.gradle.ooxml.generated.canonical.MetadataType;
import name.jurgenei.gradle.ooxml.generated.canonical.NodeType;
import name.jurgenei.gradle.ooxml.generated.canonical.ObjectFactory;
import name.jurgenei.gradle.ooxml.generated.canonical.ParagraphType;
import name.jurgenei.gradle.ooxml.generated.canonical.ReferenceType;
import name.jurgenei.gradle.ooxml.generated.canonical.RowType;
import name.jurgenei.gradle.ooxml.generated.canonical.ShapeType;
import name.jurgenei.gradle.ooxml.generated.canonical.TableType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedCanonicalModelCoverageTest {

    @Test
    void exercisesGeneratedJaxbModelAccessors() throws Exception {
        final List<Object> instances = List.of(
                new AnnotationType(),
                new BodyType(),
                new CellType(),
                new ChartAxisType(),
                new ChartSeriesType(),
                new ChartType(),
                new ConnectorType(),
                new DocumentType(),
                new EdgeType(),
                new GraphType(),
                new GroupMemberType(),
                new GroupType(),
                new ItemType(),
                new LinkType(),
                new ListType(),
                new MetadataType(),
                new NodeType(),
                new ParagraphType(),
                new ReferenceType(),
                new RowType(),
                new ShapeType(),
                new TableType()
        );

        for (Object instance : instances) {
            final Class<?> type = instance.getClass();
            for (Method method : type.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get")) {
                    final Object value = method.invoke(instance);
                    if (value instanceof java.util.List<?> list) {
                        // Force lazy-list initialization branches and live-list semantics.
                        assertNotNull(list);
                    }
                    continue;
                }
                if (method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                    try {
                        final Object value = sampleValue(method.getParameterTypes()[0]);
                        method.invoke(instance, value);
                    } catch (NoSuchMethodException ignored) {
                        // Some generated signatures use types without no-arg constructors.
                    }
                }
            }
        }

        final MetadataType metadata = new MetadataType();
        metadata.setDocumentId("doc-id");
        metadata.setVersion("v1");
        metadata.setSourceFile("sample.docx");
        metadata.setDocumentType("DOCX");

        final ParagraphType para = new ParagraphType();
        para.setSourcePath("/word/document/p[1]");
        para.setLabel("h1");

        final ObjectFactory factory = new ObjectFactory();
        para.getTextOrAny().add(factory.createParagraphTypeText("hello"));

        final CellType cell = new CellType();
        cell.getTextOrAny().add(factory.createCellTypeText("cell"));

        final RowType row = new RowType();
        row.getCell().add(cell);

        final TableType table = new TableType();
        table.getRow().add(row);

        final ListType list = new ListType();
        list.setOrdered(true);
        list.getItem().add(new ItemType());

        final ChartAxisType axisX = new ChartAxisType();
        axisX.setRole("x");
        axisX.setLabel("time");

        final ChartSeriesType series = new ChartSeriesType();
        series.setName("trajectory");
        series.getValue().add("(1,2)");

        final ChartType chart = new ChartType();
        chart.setSourcePath("/xl/charts/chart1.xml");
        chart.setHref("media/chart1.xml");
        chart.setLegend("right");
        chart.getAxis().add(axisX);
        chart.getSeries().add(series);

        final GraphType graph = new GraphType();
        graph.setSourcePath("/word/document/p[2]/drawing[1]");
        graph.setHref("media/image1.emf");
        graph.getNode().add(new NodeType());
        graph.getEdge().add(new EdgeType());
        graph.getShape().add(new ShapeType());
        graph.getConnector().add(new ConnectorType());
        graph.getGroup().add(new GroupType());
        graph.getAnnotation().add(new AnnotationType());

        final BodyType body = new BodyType();
        body.getParaOrListOrTable().add(para);
        body.getParaOrListOrTable().add(list);
        body.getParaOrListOrTable().add(table);
        body.getParaOrListOrTable().add(new LinkType());
        body.getParaOrListOrTable().add(new ReferenceType());
        body.getParaOrListOrTable().add(chart);
        body.getParaOrListOrTable().add(graph);

        final DocumentType document = new DocumentType();
        document.setMetadata(metadata);
        document.setBody(body);

        final JAXBElement<DocumentType> docEl = factory.createDocument(document);
        final JAXBElement<GraphType> graphEl = factory.createGraph(graph);

        assertEquals("document", docEl.getName().getLocalPart());
        assertEquals("graph", graphEl.getName().getLocalPart());
        assertEquals("doc-id", document.getMetadata().getDocumentId());
        assertTrue(((JAXBElement<?>) para.getTextOrAny().get(0)).getValue().toString().contains("hello"));
    }

    private Object sampleValue(final Class<?> type) throws Exception {
        if (type == String.class) {
            return "x";
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.TRUE;
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == double.class || type == Double.class) {
            return 1.0d;
        }
        return type.getDeclaredConstructor().newInstance();
    }
}

