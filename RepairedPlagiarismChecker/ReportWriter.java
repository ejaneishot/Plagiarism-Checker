import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    public static void writeReport(String content) {
        try (FileWriter writer = new FileWriter("plagiarism_report.txt")) {
            writer.write(content);
            System.out.println("Report saved to plagiarism_report.txt");
        } catch (IOException e) {
            System.out.println("Failed to write report.");
        }
    }
}