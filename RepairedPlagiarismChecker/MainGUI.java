import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MainGUI extends JFrame {
    private JTextField fileField1, fileField2;
    private JButton browseButton1, browseButton2, checkButton, saveReportButton;
    private JLabel resultLabel;
    private JTextArea matchArea;

    public MainGUI() {
        setTitle("AI Plagiarism Checker");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        fileField1 = new JTextField();
        fileField2 = new JTextField();
        browseButton1 = new JButton("Browse...");
        browseButton2 = new JButton("Browse...");
        checkButton = new JButton("Check Plagiarism");
        saveReportButton = new JButton("Save Report");

        resultLabel = new JLabel("Result will appear here", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        topPanel.add(new JLabel("Select File 1:"));
        topPanel.add(fileField1);
        topPanel.add(browseButton1);

        topPanel.add(new JLabel("Select File 2:"));
        topPanel.add(fileField2);
        topPanel.add(browseButton2);

        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(checkButton);
        centerPanel.add(saveReportButton);

        matchArea = new JTextArea(12, 50);
        matchArea.setEditable(false);
        matchArea.setBorder(BorderFactory.createTitledBorder("Matched Phrases"));
        JScrollPane scrollPane = new JScrollPane(matchArea);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.EAST);
        add(resultLabel, BorderLayout.SOUTH);

        browseButton1.addActionListener(e -> chooseFile(fileField1));
        browseButton2.addActionListener(e -> chooseFile(fileField2));

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path1 = fileField1.getText();
                String path2 = fileField2.getText();
                if (path1.isEmpty() || path2.isEmpty()) {
                    JOptionPane.showMessageDialog(MainGUI.this, "Please select both files.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    List<String> words1 = PlagiarismChecker.readFileSmart(path1);
                    List<String> words2 = PlagiarismChecker.readFileSmart(path2);

                    // Use both 3-grams and 4-grams for more robust checking
                    Set<String> ngrams1 = new HashSet<>();
                    Set<String> ngrams2 = new HashSet<>();
                    ngrams1.addAll(PlagiarismChecker.getNGrams(words1, 3));
                    ngrams1.addAll(PlagiarismChecker.getNGrams(words1, 4));
                    ngrams2.addAll(PlagiarismChecker.getNGrams(words2, 3));
                    ngrams2.addAll(PlagiarismChecker.getNGrams(words2, 4));

                    int matchCount = 0;
                    matchArea.setText("Matching Phrases (3-4 grams):\n\n");
                    for (String s : ngrams1) {
                        if (ngrams2.contains(s)) {
                            matchArea.append(s + "\n");
                            matchCount++;
                        }
                    }

                    double similarity = PlagiarismChecker.jaccardSimilarity(ngrams1, ngrams2);
                    resultLabel.setText(String.format("Plagiarism Detected: %.2f%% | Matches: %d", similarity, matchCount));

                    if (similarity > 70.0) {
                        resultLabel.setForeground(Color.RED);
                    } else if (similarity > 30.0) {
                        resultLabel.setForeground(Color.ORANGE);
                    } else {
                        resultLabel.setForeground(new Color(0, 128, 0));
                    }

                } catch (IOException ex) {
                    resultLabel.setText("Error processing files.");
                    matchArea.setText("Error loading matching phrases.");
                }
            }
        });

        saveReportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report");
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                    writer.write(resultLabel.getText() + "\n\n");
                    writer.write(matchArea.getText());
                    JOptionPane.showMessageDialog(this, "Report saved successfully.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save report.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void chooseFile(JTextField targetField) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            targetField.setText(file.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
