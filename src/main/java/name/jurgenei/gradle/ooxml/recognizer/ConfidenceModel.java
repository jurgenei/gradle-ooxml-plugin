package name.jurgenei.gradle.ooxml.recognizer;

/**
 * Confidence scoring model based on observed evidence ratios.
 */
public final class ConfidenceModel {
    double score(int matchedSignals, int expectedSignals, int sampleSize) {
        int safeExpected = Math.max(1, expectedSignals);
        int safeSample = Math.max(1, sampleSize);

        // Laplace smoothing keeps small samples from receiving extreme probabilities.
        double probability = (matchedSignals + 1.0) / (safeExpected + 2.0);
        double representativeness = Math.min(1.0, safeSample / 8.0);
        double combined = 0.55 * probability + 0.45 * representativeness;
        return roundToTwoDecimals(clamp(0.40, 0.97, combined));
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

