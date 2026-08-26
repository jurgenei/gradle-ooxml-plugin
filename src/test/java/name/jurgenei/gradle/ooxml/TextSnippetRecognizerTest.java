package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.recognizer.AssetRecognition;
import name.jurgenei.gradle.ooxml.recognizer.ConfidenceModel;
import name.jurgenei.gradle.ooxml.recognizer.TextSnippetRecognizer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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
        assertTrue(recognition.annotations().stream().anyMatch(annotation -> "recovery".equals(annotation.getKind())),
                "Recovery diagnostics annotation should be present.");
    }
}

