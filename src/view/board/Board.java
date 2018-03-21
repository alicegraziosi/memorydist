package view.board;

import model.card.Card;
import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.Player;
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

import static java.lang.Thread.sleep;

public class Board extends Container{

    private JPanel panel1;
    private JLabel label1;
    private JPanel panel2;

    private GameStatus gameStatus;
    private ArrayList<CardView> cardViews;
    private ArrayList<CardView> cardViewsMatch;
    private CardView selectedCard1;
    private CardView selectedCard2;
    private Move move;
    private Info info;
    private int id; // id player
    private JFrame frame;
    private JPanel gridPanelCards;

    public Board(GameStatus gameStatus, int id) {
        this.gameStatus = gameStatus;
        this.cardViews = new ArrayList<>();
        this.cardViewsMatch = new ArrayList<>();
        this.selectedCard1 = null;
        this.selectedCard2 = null;
        this.move = new Move();
        this.id = id;
        this.info = new Info(gameStatus, id);
        this.frame = new JFrame("Memory Game");
        this.gridPanelCards = new JPanel();
        this.gridPanelCards.setLayout(new GridLayout(4, 5));
    }

    //public static void main(String[] args) {
    public void init(){

        // look and feel (superfluo e quindi commentato)
        /*
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
        */

        // background image sfondo tavolo verde (superfluo e quindi commentato)
        /*
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./images/board.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        JPanel borderPanelBoard = new JPanel();
        borderPanelBoard.setLayout(new BorderLayout());

        // background image sfondo tavolo verde (superfluo e quindi commentato)
        //JLabel label1 = new JLabel(new ImageIcon(img));
        //JPanel panel2 = new JPanel();
        //panel2.setLayout(new FlowLayout());
        //panel2.add(label1);
        //borderPanelBoard.add(panel2, BorderLayout.EAST);

        // pannello laterale con info di gioco
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.setSize(200, 500);
        panel1.add(info, BorderLayout.NORTH);
        borderPanelBoard.add(panel1, BorderLayout.WEST);

        // cards
        for(int i=0; i<gameStatus.getNotShowingCards().size(); i++){
            CardView cardView = new CardView(gameStatus.getNotShowingCards().get(i));
            cardViews.add(cardView);
            gridPanelCards.add(cardView);
        }

        // display cards
        displayCards();

        borderPanelBoard.add(gridPanelCards,BorderLayout.EAST);

        frame.add(borderPanelBoard);

        frame.setSize(800, 650);
        //frame.pack(); // o setSize o pack
        frame.setVisible(true);

        // ask before exit
        setCloseOperation();
    }

    // set selectedcard to null
    public void clearSelectedCards(){
        selectedCard1 = null;
        selectedCard2 = null;
    }

    // make carks unclickable
    public void block(){
        for (CardView cardView: cardViews) {
            cardView.setLogo();
            cardView.setEnabled(false);
        }
    }

    // make cards ckickable
    public void unblockCards(){
        for (CardView cardView: cardViews) {
            cardView.setLogo();
            cardView.setEnabled(true);
        }
    }

    public void showMatchedCards(){
        for (CardView cardView: cardViewsMatch) {
            cardView.setImage();
            cardView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardView.setImage();
                    cardView.removeActionListener(this);
                }
            });
        }
    }

    public void update(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        selectedCard1 = null;
        selectedCard2 = null;

        info.setGameStatus(gameStatus);
    }

    // called by not current players
    public void updateInterfaceAfterMove(Move move) {

        cardViews.get(move.getCard1().getIndex()).setImage();
        if(move.getCard2() != null){
            cardViews.get(move.getCard2().getIndex()).setImage();
        }
    }

    public void displayCards(){

        for (CardView cardView: cardViews) {
            cardView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if(selectedCard1==null && selectedCard2==null){
                        //cardView.removeActionListener(this);
                        selectedCard1 = cardView;
                        cardView.setImage();
                        move = new Move(selectedCard1.getCard());
                        System.out.println("First selected card: " + move.getCard1().getValue());
                        gameStatus.setMove(move);

                        // todo mandare msg
                        // sendMove();

                    } else if (selectedCard2==null) {
                        //cardView.removeActionListener(this);
                        selectedCard2 = cardView;
                        cardView.setImage();
                        move = new Move(selectedCard1.getCard(), selectedCard2.getCard());
                        System.out.println("Second selected card: " + move.getCard2().getValue());
                        System.out.println("Match: " + move.isMatch());

                        if(move.isMatch()){
                            int score = gameStatus.getPlayersList().get(id-1).getScore() + 1;
                            gameStatus.getPlayersList().get(id-1).setScore(score);
                            info.updateScore(gameStatus);
                            gameStatus.getShowingCards().add(move.getCard1());
                            gameStatus.getShowingCards().add(move.getCard2());
                            cardViewsMatch.add(selectedCard1);
                            cardViewsMatch.add(selectedCard2);
                            showMatchedCards();
                        }

                        gameStatus.setMove(move);


                    } else {
                    }
                }
            });
        }

    }

    // ask before exit the application on close
    private void setCloseOperation() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int input = JOptionPane.showOptionDialog(null,
                        "Do you want to exit the game?",
                        "Exit game",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, null);
                if (input == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    public void setGameWinner() {
        int input = JOptionPane.showConfirmDialog(null,
                "You are the winner!\n\nDo you want to exit the game?",
                "You are the winner!",
                JOptionPane.CLOSED_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null);
        if (input == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
