package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.recognizer.AssetRecognition;
import name.jurgenei.gradle.ooxml.recognizer.ConfidenceModel;
import name.jurgenei.gradle.ooxml.recognizer.TextSnippetRecognizer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TextSnippetRecognizerTest {
    @Test
    void recoversSingletonGraphFromRichAssetText() {
        TextSnippetRecognizer recognizer = new TextSnippetRecognizer(new ConfidenceModel());
        String richText = "VRE Request | Cover | Static Data | Eligible Covers Based on CRR3 LER rules | "
                + "Determine Eligible Guarantees and Security Covers | start | Cover Eligibility Ind | Basel4 Exposure Class SA | "
                + "Eligible SA Guarantor Ind | Outstanding / | Cover Eligiblity Ind | LER Eligiblity | Substitution Indicator | "
                + "Y and Eligible SA | Guarantor Ind | VRE Exposure Class";

        AssetRecognition recognition = recognizer.recognize("235935778", "word/media/image777.emf",
                richText.getBytes(StandardCharsets.UTF_16LE));

        assertTrue(recognition.nodes().size() >= 5, "Recovery should synthesize multiple nodes from rich token stream.");
        assertTrue(recognition.edges().size() >= 4, "Recovery should synthesize flow edges.");
        assertTrue(recognition.groups().size() >= 2, "Recovery should synthesize nested groups for leading hierarchy tokens.");
        assertTrue(recognition.groups().stream().anyMatch(group ->
                group.getMembers().stream().anyMatch(member -> member.getGroup() != null)),
                "Recovery should include nested group references.");
        assertTrue(recognition.annotations().stream().anyMatch(annotation ->
                        "recovery".equals(annotation.getKind()) || "coverage-heuristic".equals(annotation.getKind())),
                "Recovery or coverage diagnostics annotation should be present.");
        assertAssetTextTokenCoverage(recognition);
    }

    private void assertAssetTextTokenCoverage(AssetRecognition recognition) {
        List<String> labels = new ArrayList<>();
        recognition.nodes().stream().map(node -> node.getLabel()).filter(label -> label != null && !label.isBlank()).forEach(labels::add);
        recognition.edges().stream().map(edge -> edge.getLabel()).filter(label -> label != null && !label.isBlank()).forEach(labels::add);
        recognition.groups().stream().map(group -> group.getLabel()).filter(label -> label != null && !label.isBlank()).forEach(labels::add);

        List<String> normalizedLabels = labels.stream().map(this::normalize).toList();
        recognition.annotations().stream()
                .filter(annotation -> "asset-text".equals(annotation.getKind()))
                .map(annotation -> annotation.getText())
                .filter(text -> text != null && !text.isBlank())
                .forEach(text -> {
                    for (String raw : text.split("\\|")) {
                        String token = normalize(raw);
                        if (token.isBlank() || token.length() < 3 || isStopToken(token)) {
                            continue;
                        }
                        assertTrue(isTokenCovered(token, normalizedLabels), "Missing token in labels: " + token);
                    }
                });
    }

    private boolean isTokenCovered(String token, List<String> labels) {
        for (String label : labels) {
            if (label.contains(token) || token.contains(label)) {
                return true;
            }
            if (wordOverlap(token, label) >= 0.67) {
                return true;
            }
        }
        return false;
    }

    private double wordOverlap(String token, String label) {
        String[] tokenWords = token.split("\\s+");
        String[] labelWords = label.split("\\s+");
        int matches = 0;
        for (String tokenWord : tokenWords) {
            for (String labelWord : labelWords) {
                if (labelWord.equals(tokenWord)) {
                    matches++;
                    break;
                }
            }
        }
        return tokenWords.length == 0 ? 0.0 : (double) matches / (double) tokenWords.length;
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isStopToken(String token) {
        return "and".equals(token)
                || "the".equals(token)
                || "for".equals(token)
                || "with".equals(token)
                || "yes".equals(token)
                || "no".equals(token);
    }
}

