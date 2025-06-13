import java.io.*;
import java.util.*;
import java.util.regex.*;

public class PlagiarismChecker {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "the", "is", "in", "at", "of", "on", "and", "a", "to", "for", "with", "that", "this", "it"
    ));

    public static List<String> readFileSmart(String filePath) throws IOException {
        if (filePath.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(filePath);
        } else {
            return readFile(filePath);
        }
    }

    public static List<String> readFile(String path) throws IOException {
        List<String> words = new ArrayList<>();
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNext()) {
            String word = scanner.next().toLowerCase().replaceAll("[^a-z]", "");
            if (!word.isEmpty() && !STOPWORDS.contains(word)) {
                words.add(word);
            }
        }
        scanner.close();
        return words;
    }

    private static List<String> extractTextFromPdf(String filePath) throws IOException {
        // Placeholder: Replace with real PDF parsing logic using PDFBox or similar
        return readFile(filePath);
    }

    public static List<String> getMatchedNGrams(List<String> words1, List<String> words2, int n) {
        Set<String> ngrams1 = getNGrams(words1, n);
        Set<String> ngrams2 = getNGrams(words2, n);
        List<String> matched = new ArrayList<>();
        for (String gram : ngrams1) {
            if (ngrams2.contains(gram)) matched.add(gram);
        }
        return matched;
    }

    public static Set<String> getNGrams(List<String> words, int n) {
        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words.get(i + j)).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    public static double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    public static double calculateSimilarity(String file1, String file2) {
        try {
            List<String> words1 = readFileSmart(file1);
            List<String> words2 = readFileSmart(file2);
            Set<String> ngrams1 = getNGrams(words1, 3);
            Set<String> ngrams2 = getNGrams(words2, 3);
            double jaccard = jaccardSimilarity(ngrams1, ngrams2);
            return jaccard * 100;
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static List<String> extractMatchedPhrases(String file1, String file2) {
        try {
            List<String> words1 = readFileSmart(file1);
            List<String> words2 = readFileSmart(file2);
            return getMatchedNGrams(words1, words2, 3);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static Map<String, List<String>> getContextLinesWithHighlight(String filePath, List<String> phrases) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String originalLine = lines.get(i);
            String lowerLine = originalLine.toLowerCase();

            for (String phrase : phrases) {
                if (lowerLine.contains(phrase)) {
                    // Safely highlight matched phrase using regex
                    String regex = "(?i)(" + Pattern.quote(phrase) + ")";
                    String highlighted = originalLine.replaceAll(regex, "**$1**");

                    result.computeIfAbsent(phrase, k -> new ArrayList<>())
                            .add("Line " + (i + 1) + ": " + highlighted);
                }
            }
        }

        return result;
    }

    public static void generateReport(String file1, String file2, String reportPath) {
        try {
            double sim = calculateSimilarity(file1, file2);
            List<String> matched = extractMatchedPhrases(file1, file2);
            Map<String, List<String>> c1 = getContextLinesWithHighlight(file1, matched);
            Map<String, List<String>> c2 = getContextLinesWithHighlight(file2, matched);

            try (PrintWriter w = new PrintWriter(new FileWriter(reportPath))) {
                w.println("Plagiarism Detection Report");
                w.println("=========================================");
                w.println("Compared Files:");
                w.println("- File A: " + file1);
                w.println("- File B: " + file2);
                w.printf("Overall Similarity: %.2f%%\n", sim);
                w.println("-----------------------------------------");

                if (matched.isEmpty()) {
                    w.println("No significant plagiarism detected !");
                } else {
                    w.println("Potentially Plagiarised Phrases ⚠ :");
                    int i = 1;
                    for (String phrase : matched) {
                        w.printf("\n%d. Matched Phrase: \"%s\"\n", i++, phrase);

                        List<String> file1Context = c1.getOrDefault(phrase, List.of("(Phrase not found in File A)"));
                        List<String> file2Context = c2.getOrDefault(phrase, List.of("(Phrase not found in File B)"));

                        w.println("Context in File A:");
                        for (String line : file1Context) {
                            w.println("      - " + line);
                        }

                        w.println("Context in File B:");
                        for (String line : file2Context) {
                            w.println("      - " + line);
                        }
                    }

                    w.println("\nNote:");
                    w.println("Phrases are flagged if they appear in both documents after filtering stopwords.");
                    w.println("The highlighted phrases are exact 3-word sequences matched between both files.");
                }
            }

            System.out.println("Improved report saved to: " + reportPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
