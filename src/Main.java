
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import java.awt.Color;
import java.io.*;
import java.nio.file.*;

public class Main {
    static File currentFile;
    static JTextArea text = new JTextArea();
    static boolean changed = false;
    static UndoManager undoManager = new UndoManager();

    public static void main(String args[]) {
        JFrame frame = new JFrame("Qery");
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
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
        undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));

        text.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        text.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }
            public void removeUpdate(DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }
            public void changedUpdate(DocumentEvent e) {
                changed = true;
                updateTitle(frame);
            }
        });
        undo.addActionListener(e -> { if (undoManager.canUndo()) undoManager.undo(); });
        redo.addActionListener(e -> { if (undoManager.canRedo()) undoManager.redo(); });
        cut.addActionListener(e -> text.cut());
        copy.addActionListener(e -> text.copy());
        paste.addActionListener(e -> text.paste());
        selectAll.addActionListener(e -> text.selectAll());

        newFile.addActionListener(e -> {
            if (confirm(frame)) {
                text.setText("");
                currentFile = null;
                changed = false;
                undoManager.discardAllEdits();
                updateTitle(frame);
            }
        });
        openFile.addActionListener(e -> {
            if (!confirm(frame)) return;
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    String content = Files.readString(chooser.getSelectedFile().toPath());
                    text.setText(content);
                    currentFile = chooser.getSelectedFile();
                    changed = false;
                    undoManager.discardAllEdits();
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
                File oldFile = currentFile;
                currentFile = chooser.getSelectedFile();
                save(frame);
                if (changed) currentFile = oldFile;
            }
        });
        exit.addActionListener(e -> { if (confirm(frame)) frame.dispose(); });

        file.add(newFile);
        file.add(openFile);
        file.add(saveFile);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);
        edit.add(undo);
        edit.add(redo);
        edit.addSeparator();
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.addSeparator();
        edit.add(selectAll);
        bar.add(file);
        bar.add(edit);

        frame.setJMenuBar(bar);
        JTextArea lines = new JTextArea("1");
        lines.setBackground(text.getBackground());
        lines.setForeground(Color.GRAY);
        lines.setEditable(false);
        lines.setFocusable(false);

        text.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateLines();
            }

            public void removeUpdate(DocumentEvent e) {
                updateLines();
            }

            public void changedUpdate(DocumentEvent e) {
                updateLines();
            }

            void updateLines() {
                StringBuilder numbers = new StringBuilder();
                for (int i = 1; i <= text.getLineCount(); i++) {
                    numbers.append(i).append("\n");
                }
                lines.setText(numbers.toString());
            }
        });

        JScrollPane scroll = new JScrollPane(text);
        scroll.setRowHeaderView(lines);
        frame.add(scroll);
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
