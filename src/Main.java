import javax.swing.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Qery");
        JTextArea text = new JTextArea();

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem exit = new JMenuItem("Exit");

        newFile.addActionListener(e -> text.setText(""));

        openFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == 0) {
                try {
                    text.setText(java.nio.file.Files.readString(
                        chooser.getSelectedFile().toPath()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error opening file");
                }
            }
        });

        saveFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == 0) {
                try {
                    java.nio.file.Files.writeString(
                        chooser.getSelectedFile().toPath(), text.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving file");
                }
            }
        });

        exit.addActionListener(e -> frame.dispose());

        file.add(newFile);
        file.add(openFile);
        file.add(saveFile);
        file.addSeparator();
        file.add(exit);
        bar.add(file);
        frame.setJMenuBar(bar);
        frame.add(new JScrollPane(text));
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}