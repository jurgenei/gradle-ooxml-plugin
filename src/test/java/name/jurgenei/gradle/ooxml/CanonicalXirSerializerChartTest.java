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

class CanonicalXirSerializerChartTest {
    @Test
    void serializesChartEvidenceAsCanonicalXir() throws Exception {
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
        new CanonicalXirSerializer().write(document, output);
        String xir = output.toString(StandardCharsets.UTF_8);

        assertTrue(xir.startsWith("(."));
        assertTrue(xir.contains("(chart"));
        assertTrue(xir.contains("source-path \"/xl/charts/chart1.xml\""));
        assertTrue(xir.contains("(title \"Revenue trend\")"));
        assertTrue(xir.contains("(legend \"Region\")"));
        assertTrue(xir.contains("role \"x\""));
        assertTrue(xir.contains("role \"y\""));
        assertTrue(xir.contains("(name \"NL\")"));
    }
}

