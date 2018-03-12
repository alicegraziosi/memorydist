package view.board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BoardGUI {

    private JPanel panel1;
    private JLabel label1;

    public BoardGUI() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("C:/Users/alice/IdeaProjects/memory/images/tavolo-verde.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // background image (todo non si vede)
        label1 = new JLabel(new ImageIcon(img));
        label1.setLayout(new BorderLayout());
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Memory");
        frame.setContentPane(new BoardGUI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
