package view.card;

import model.card.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardView extends JButton {

    private Card card;
    private boolean matched;

    /**
     * constructor
     * @param card
     */
    public CardView(Card card) {
        this.card = card;
        this.matched = false;
        this.setPreferredSize(new Dimension(100, 130));
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(3, 3, 5, 5)));

        this.setImage();
    }

    /**
     * value is 1, 2, 3, ...
     */
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

    /**
     * retro della carta
     */
    public void setLogo(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./images/back.png"));
            this.setIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDisabledIconImage(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(
                    new File("./images/" + card.getValue() + ".jpg"));
            this.setDisabledIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDisabledIconLogo(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./images/backDisabled.jpg"));
            this.setDisabledIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @return Card
     */
    public Card getCard() {
        return card;
    }

    @Override
    public String toString() {
        return "CardView{" +
                "card value=" + card.getValue() +
                ", card index=" + card.getIndex() +
                ", matched=" + matched +
                '}';
    }

}
