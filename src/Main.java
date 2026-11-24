import javax.swing.*;

public class Main {
    public static void main(String args []) {
        JFrame frame = new JFrame("Qery");
        JTextField editor = new JTextField();

        frame.add(new JScrollPane(editor));
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}