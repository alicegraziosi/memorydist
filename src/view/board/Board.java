package view.board;

import model.card.Card;
import model.gameStatus.GameStatus;
import view.card.CardView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Board extends Container{

    private JPanel panel1;
    private JLabel label1;
    private JPanel panel2;

    private GameStatus gameStatus;
    private ArrayList<CardView> cardViews;
    private CardView selectedCard;

    public Board(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        this.cardViews = new ArrayList<>();
    }

    //public static void main(String[] args) {
    public void init(){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./images/board.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // background image
        JLabel label1 = new JLabel(new ImageIcon(img));

        JFrame frame = new JFrame("Memory");

        JPanel borderPanelBoard = new JPanel();
        borderPanelBoard.setLayout(new BorderLayout());

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.setSize(200, 500);
        Info info = new Info();
        panel1.add(info, BorderLayout.NORTH);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        //panel2.setSize(400, 500);
        panel2.add(label1);

        borderPanelBoard.add(panel1, BorderLayout.WEST);
        //borderPanelBoard.add(panel2, BorderLayout.EAST);

        // cards
        for(int i=0; i<gameStatus.getNotShowingCards().size(); i++){
            CardView cardView = new CardView(gameStatus.getNotShowingCards().get(i));
            cardViews.add(cardView);
        }

        // display cards
        JPanel gridPanelCards = new JPanel();
        gridPanelCards.setLayout(new GridLayout(4, 5));
        for (CardView cardView: cardViews) {
            gridPanelCards.add(cardView);
        }
        borderPanelBoard.add(gridPanelCards,BorderLayout.EAST);

        frame.add(borderPanelBoard);

        frame.setSize(800, 650);
        //frame.pack(); // o setSize o pack
        frame.setVisible(true);

        // exit the application on close
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int input = JOptionPane.showOptionDialog(null,
                        "Do you want to exit the game?",
                        "Exit game",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,null,null);
                if(input == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
}
