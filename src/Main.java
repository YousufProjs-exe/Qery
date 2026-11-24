
import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    static File currentFile;
    static JTextArea text = new JTextArea();
    static boolean changed = false;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Qery");
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");

        newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openFile.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveFile.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

        text.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
            }
        });

        newFile.addActionListener(e -> {
            if (confirm(frame)) {
                text.setText("");
                currentFile = null;
                changed = false;
            }
        });
        openFile.addActionListener(e -> {
            if (!confirm(frame)) return;

            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == 0) {
                try {
                    currentFile = chooser.getSelectedFile();
                    text.setText(Files.readString(currentFile.toPath()));
                    changed = false;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Could not OPEN file");
                }
            }
        });
        saveFile.addActionListener(e -> save(frame));
        saveAs.addActionListener(e -> {
            currentFile = null;
            save(frame);
        });
        exit.addActionListener(e -> {
            if (confirm(frame)) frame.dispose();
        });

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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (confirm(frame)) frame.dispose();
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static boolean confirm(JFrame frame) {
        if (!changed) return true;

        int result = JOptionPane.showConfirmDialog(
            frame, "You have unsaved changes. Continue?",
            "Qery", JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }

    static void save(JFrame frame) {
        if (currentFile == null) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) != 0) return;
            currentFile = chooser.getSelectedFile();
        }

        try {
            Files.writeString(currentFile.toPath(), text.getText());
            changed = false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Could not SAVE file");
        }
    }
}
