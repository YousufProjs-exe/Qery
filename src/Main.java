import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.Color;
import java.io.*;
import java.nio.file.*;

public class Main {
    static File currentFile;
    static JTextArea text = new JTextArea();
    static boolean changed = false;
    static UndoManager undoManager = new UndoManager();
    static JTextField findField = new JTextField(18);
    static JTextField replaceField = new JTextField(18);
    static JLabel matchCount = new JLabel("0 of 0");
    static ArrayList<Integer> matches = new ArrayList<>();
    static Object currentHighlight;
    static Highlighter.HighlightPainter normalPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    static Highlighter.HighlightPainter selectedPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

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
        JMenuItem find = new JMenuItem("Find");
        JMenuItem replace = new JMenuItem("Replace");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel findLabel = new JLabel("Find:");
        JLabel replaceLabel = new JLabel("Replace:");
        JButton previous = new JButton("↑");
        JButton next = new JButton("↓");
        JButton replaceOne = new JButton("Replace");
        JButton replaceEvery = new JButton("Replace All");
        JButton closeSearch = new JButton("X");

        find.setAccelerator(KeyStroke.getKeyStroke("control F"));
        replace.setAccelerator(KeyStroke.getKeyStroke("control H"));
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

        find.addActionListener(e -> {
            searchPanel.setVisible(true);
            replaceLabel.setVisible(false);
            replaceField.setVisible(false);
            replaceOne.setVisible(false);
            replaceEvery.setVisible(false);
            findField.requestFocus();
            findField.selectAll();
        });

        replace.addActionListener(e -> {
            searchPanel.setVisible(true);
            replaceLabel.setVisible(true);
            replaceField.setVisible(true);
            replaceOne.setVisible(true);
            replaceEvery.setVisible(true);
            findField.requestFocus();
            findField.selectAll();
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
        edit.add(find);
        edit.add(replace);
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
                updateSearch();
            }

            public void removeUpdate(DocumentEvent e) {
                updateLines();
                updateSearch();
            }

            public void changedUpdate(DocumentEvent e) {
                updateLines();
                updateSearch();
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

        searchPanel.add(findLabel);
        searchPanel.add(findField);
        searchPanel.add(previous);
        searchPanel.add(next);
        searchPanel.add(matchCount);
        searchPanel.add(replaceLabel);
        searchPanel.add(replaceField);
        searchPanel.add(replaceOne);
        searchPanel.add(replaceEvery);
        searchPanel.add(closeSearch);
        searchPanel.setVisible(false);
        frame.add(searchPanel, BorderLayout.NORTH);

        replaceLabel.setVisible(false);
        replaceField.setVisible(false);
        replaceOne.setVisible(false);
        replaceEvery.setVisible(false);

        findField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }
            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }
        });

        previous.addActionListener(e -> {
            if (!matches.isEmpty()) {
                int index = matches.indexOf(text.getSelectionStart());
                selectMatch(index == -1 ? matches.size() - 1 : index - 1);
            }
        });

        next.addActionListener(e -> {
            if (!matches.isEmpty()) {
                int index = matches.indexOf(text.getSelectionStart());
                selectMatch(index == -1 ? 0 : index + 1);
            }
        });

        replaceOne.addActionListener(e -> replaceCurrent());
        replaceEvery.addActionListener(e -> replaceAll());

        closeSearch.addActionListener(e -> {
            searchPanel.setVisible(false);
            text.getHighlighter().removeAllHighlights();
            currentHighlight = null;
            text.requestFocus();
        });

        frame.getRootPane().getInputMap(
            JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(KeyStroke.getKeyStroke("control F"), "find");

        frame.getRootPane().getActionMap().put("find", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                find.doClick();
            }
        });

        frame.getRootPane().getInputMap(
            JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(KeyStroke.getKeyStroke("control H"), "replace");

        frame.getRootPane().getActionMap().put("replace", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                replace.doClick();
            }
        });

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

    static void updateSearch() {
        Highlighter highlighter = text.getHighlighter();
        highlighter.removeAllHighlights();
        matches.clear();
        currentHighlight = null;

        String content = text.getText();
        String word = findField.getText();

        if (word.isEmpty()) {
            matchCount.setText("0 of 0");
            return;
        }

        for (int i = 0; i <= content.length() - word.length(); i++) {
            if (content.regionMatches(true, i, word, 0, word.length())) {
                matches.add(i);
                try {
                    highlighter.addHighlight(i, i + word.length(), normalPainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                i += word.length() - 1;
            }
        }

        if (matches.isEmpty()) {
            matchCount.setText("0 of 0");
        } else {
            selectMatch(0);
        }
    }

    static void selectMatch(int index) {
        if (matches.isEmpty()) return;

        index = (index + matches.size()) % matches.size();

        if (currentHighlight != null) {
            text.getHighlighter().removeHighlight(currentHighlight);
        }

        int start = matches.get(index);
        int end = start + findField.getText().length();

        try {
            currentHighlight = text.getHighlighter().addHighlight(start, end, selectedPainter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        text.setCaretPosition(end);
        text.select(start, end);
        matchCount.setText((index + 1) + " of " + matches.size());
    }

    static void replaceCurrent() {
        if (matches.isEmpty()) return;

        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();

        if (start == end) return;

        text.replaceSelection(replaceField.getText());
        updateSearch();
    }

    static void replaceAll() {
        String word = findField.getText();
        if (word.isEmpty()) return;

        String content = text.getText();
        String replacement = replaceField.getText();
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < content.length()) {
            if (i <= content.length() - word.length()
                    && content.regionMatches(true, i, word, 0, word.length())) {
                result.append(replacement);
                i += word.length();
            } else {
                result.append(content.charAt(i));
                i++;
            }
        }

        text.setText(result.toString());
        updateSearch();
    }
}