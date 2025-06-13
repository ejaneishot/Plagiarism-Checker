import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class MainGUI extends JFrame {
    private JTextField fileField1, fileField2;
    private JButton browseButton1, browseButton2, checkButton, saveReportButton;
    private JLabel resultLabel;
    private JTextArea matchArea;
    private String reportContent = "";

    public MainGUI() {
        setTitle("Plagiarism Checker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder("Select Files"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fileField1 = new JTextField(30);
        fileField2 = new JTextField(30);
        browseButton1 = new JButton("Browse...");
        browseButton2 = new JButton("Browse...");
        checkButton = new JButton("Check Plagiarism");
        saveReportButton = new JButton("Save Report");

        gbc.gridx = 0; gbc.gridy = 0;
        filePanel.add(new JLabel("File 1 :"), gbc);
        gbc.gridx = 1;
        filePanel.add(fileField1, gbc);
        gbc.gridx = 2;
        filePanel.add(browseButton1, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filePanel.add(new JLabel("File 2 :"), gbc);
        gbc.gridx = 1;
        filePanel.add(fileField2, gbc);
        gbc.gridx = 2;
        filePanel.add(browseButton2, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(checkButton);
        buttonPanel.add(saveReportButton);
        filePanel.add(buttonPanel, gbc);

        resultLabel = new JLabel("Result will appear here", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        matchArea = new JTextArea();
        matchArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        matchArea.setMargin(new Insets(10, 10, 10, 10));
        matchArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(matchArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Similarity Report"));

        add(filePanel, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        browseButton1.addActionListener(e -> chooseFile(fileField1));
        browseButton2.addActionListener(e -> chooseFile(fileField2));

        checkButton.addActionListener((ActionEvent e) -> {
            String file1 = fileField1.getText();
            String file2 = fileField2.getText();
            if (!file1.isEmpty() && !file2.isEmpty()) {
                try {
                    String reportPath = "plagiarism_report.txt";
                    PlagiarismChecker.generateReport(file1, file2, reportPath);

                    // Read and display the generated report
                    reportContent = new String(java.nio.file.Files.readAllBytes(new File(reportPath).toPath()));
                    matchArea.setText(reportContent);
                    resultLabel.setText("Report generated successfully.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(MainGUI.this, "Error generating report.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(MainGUI.this, "Please select both files.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveReportButton.addActionListener(e -> saveReport());
    }

    private void chooseFile(JTextField field) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            field.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveReport() {
        if (reportContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(reportContent);
                JOptionPane.showMessageDialog(this, "Report saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}