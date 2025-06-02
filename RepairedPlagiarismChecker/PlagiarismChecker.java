import java.io.*;
import java.util.*;

public class PlagiarismChecker {

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
            if (!word.isEmpty()) words.add(word);
        }
        scanner.close();
        return words;
    }

    private static List<String> extractTextFromPdf(String filePath) throws IOException {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\s+");
                for (String token : tokens) {
                    String clean = token.toLowerCase().replaceAll("[^a-z]", "");
                    if (!clean.isEmpty()) words.add(clean);
                }
            }
        }
        return words;
    }

    public static Set<String> getNGrams(List<String> words, int n) {
        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i < words.size() - n + 1; i++) {
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
        return ((double) intersection.size() / union.size()) * 100;
    }


    public static double calculateSimilarity(String file1, String file2) {
        try {
            List<String> words1 = readFileSmart(file1);
            List<String> words2 = readFileSmart(file2);

            Set<String> ngrams1 = new HashSet<>();
            Set<String> ngrams2 = new HashSet<>();
            ngrams1.addAll(getNGrams(words1, 3));
            ngrams1.addAll(getNGrams(words1, 4));
            ngrams2.addAll(getNGrams(words2, 3));
            ngrams2.addAll(getNGrams(words2, 4));

            return jaccardSimilarity(ngrams1, ngrams2);
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
