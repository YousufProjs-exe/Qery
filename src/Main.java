
import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    static File currentFile;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Qery");
        JTextArea text = new JTextArea();

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");

        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");

        newFile.addActionListener(e -> {
            text.setText("");
            currentFile = null;
        });

        openFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    currentFile = chooser.getSelectedFile();
                    text.setText(Files.readString(currentFile.toPath()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Could not open file");
                }
            }
        });

        saveFile.addActionListener(e -> {
            if (currentFile == null) {
                saveAs.doClick();
            } else {
                try {
                    Files.writeString(currentFile.toPath(), text.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Could not save file");
                }
            }
        });

        saveAs.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                currentFile = chooser.getSelectedFile();
                saveFile.doClick();
            }
        });

        exit.addActionListener(e -> frame.dispose());

        file.add(newFile);
        file.add(openFile);
        file.add(saveFile);
        file.add(saveAs);
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
