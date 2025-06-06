import java.io.*;
import java.util.*;

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
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    String clean = token.toLowerCase().replaceAll("[^a-z]", "");
                    if (!clean.isEmpty() && !STOPWORDS.contains(clean)) {
                        words.add(clean);
                    }
                }
            }
        }
        return words;
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
}
