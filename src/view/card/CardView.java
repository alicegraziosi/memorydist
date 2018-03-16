package view.card;

import model.card.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardView extends JButton {

    private Card card;

    public CardView(Card card) {
        this.card = card;
        this.setPreferredSize(new Dimension(120, 140));
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(3, 3, 5, 5)));
        this.setLogo();
        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setImage();
            }
        });
    }

    // value is 1, 2, 3, ...
    public void setImage(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(
                    new File("./images/" + card.getValue() + ".jpg"));
            this.setIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // retro della carta
    public void setLogo(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./images/back.png"));
            this.setIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
