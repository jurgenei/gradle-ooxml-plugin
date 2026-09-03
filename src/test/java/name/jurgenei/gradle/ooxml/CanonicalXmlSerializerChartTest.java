package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.Body;
import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import name.jurgenei.gradle.ooxml.canonical.Chart;
import name.jurgenei.gradle.ooxml.canonical.ChartAxis;
import name.jurgenei.gradle.ooxml.canonical.ChartSeries;
import name.jurgenei.gradle.ooxml.canonical.Metadata;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalXmlSerializerChartTest {
    @Test
    void serializesChartEvidenceInCanonicalNamespace() throws Exception {
        Chart chart = new Chart(
                "Revenue trend",
                "Region",
                List.of(
                        new ChartAxis("x", "Quarter", null),
                        new ChartAxis("y", "Revenue", "EUR")
                ),
                List.of(new ChartSeries("NL", List.of("10", "12"))),
                "/xl/charts/chart1.xml",
                "media/chart1.xml"
        );

        Body body = Body.ordered(List.of(chart));
        CanonicalDocument document = new CanonicalDocument(
                new Metadata("chart-sample", "v1", "chart.xlsx", "XLSX"),
                body
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new CanonicalXmlSerializer().write(document, output);
        String xml = output.toString(StandardCharsets.UTF_8);

        assertTrue(xml.contains("<chart "));
        assertTrue(xml.contains("source-path=\"/xl/charts/chart1.xml\""));
        assertTrue(xml.contains("<title>Revenue trend</title>"));
        assertTrue(xml.contains("<legend>Region</legend>"));
        assertTrue(xml.contains("<axis role=\"x\""));
        assertTrue(xml.contains("<axis role=\"y\""));
        assertTrue(xml.contains("<series>"));
        assertTrue(xml.contains("<name>NL</name>"));
        assertTrue(xml.contains("<value>10</value>"));
    }
}

