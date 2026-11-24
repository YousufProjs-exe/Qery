
import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    static File currentFile;
    static JTextArea text = new JTextArea();
    static boolean changed = false;

    public static void main(String args []) {
        JFrame frame = new JFrame("Qery");
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
        JMenu edit = new JMenu("Edit");
        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        JMenuItem selectAll = new JMenuItem("Select All");

        newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openFile.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveFile.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

        cut.addActionListener(e -> text.cut());
        copy.addActionListener(e -> text.copy());
        paste.addActionListener(e -> text.paste());
        selectAll.addActionListener(e -> text.selectAll());

        edit.add(undo);
        edit.add(redo);
        edit.addSeparator();
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.addSeparator();
        edit.add(selectAll);
        bar.add(edit);

        text.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }
        });
        newFile.addActionListener(e -> {
            if (confirm(frame)) {
                text.setText("");
                currentFile = null;
                changed = false;
                updateTitle(frame);
            }
        });
        openFile.addActionListener(e -> {
            if (!confirm(frame)) return;

            JFileChooser chooser = new JFileChooser();

            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    currentFile = chooser.getSelectedFile();
                    text.setText(Files.readString(currentFile.toPath()));
                    changed = false;
                    updateTitle(frame);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Could not open file");
                }
            }
        });
        saveFile.addActionListener(e -> save(frame));
        saveAs.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                currentFile = chooser.getSelectedFile();
                save(frame);
            }
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

        updateTitle(frame);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static boolean confirm(JFrame frame) {
        if (!changed) return true;

        int result = JOptionPane.showConfirmDialog(
            frame,
            "You have unsaved changes. Continue?",
            "Qery",
            JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }

    static void save(JFrame frame) {
        if (currentFile == null) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
            currentFile = chooser.getSelectedFile();
        }

        try {
            Files.writeString(currentFile.toPath(), text.getText());
            changed = false;
            updateTitle(frame);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Could not save file");
        }
    }

    static void updateTitle(JFrame frame) {
        String name = currentFile == null ? "Untitled" : currentFile.getName();
        frame.setTitle((changed ? "* " : "") + name + " - Qery");
    }
}
