import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter path to first file:");
        String path1 = scanner.nextLine();

        System.out.println("Enter path to second file:");
        String path2 = scanner.nextLine();

        String text1 = FileHandler.readFile(path1);
        String text2 = FileHandler.readFile(path2);

        double similarity = PlagiarismChecker.calculateSimilarity(text1, text2);

        String report = String.format("Plagiarism similarity: %.2f%%\n", similarity * 100);
        System.out.print(report);

        ReportWriter.writeReport(report);

        scanner.close();
    }
}