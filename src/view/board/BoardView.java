package view.board;

import client.PlayerClient;
import controller.GameController;
import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.PLAYER_STATE;
import model.player.Player;
import view.card.CardView;
import view.listener.GameGUIListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.Timer;

public class BoardView extends Container implements GameGUIListener{
    private GameStatus gameStatus;
    private ArrayList<CardView> cardViews;
    private CardView selectedCard1;
    private CardView selectedCard2;
    private Move move;
    private InfoView infoView;
    private int id; // id player
    private JFrame frame;
    private JPanel gridPanelCards;

    private PlayerClient playerClient;
    private GameController gameController;
    private Timer timer;

    public BoardView(GameStatus gameStatus, int id, PlayerClient playerClient) {
        this.gameStatus = gameStatus;
        this.cardViews = new ArrayList<>();
        this.selectedCard1 = null;
        this.selectedCard2 = null;
        this.move = new Move();
        this.id = id;
        this.infoView = new InfoView(gameStatus, id);
        this.frame = new JFrame("Memory Game - Player " + id);
        this.gridPanelCards = new JPanel();
        this.gridPanelCards.setLayout(new GridLayout(4, 5));
        this.playerClient = playerClient;
    }

    public void init(){

        JPanel borderPanelBoard = new JPanel();
        borderPanelBoard.setLayout(new BorderLayout());

        // pannello laterale con infoView di gioco
        JPanel panelInfoView = new JPanel();
        panelInfoView.setLayout(new BorderLayout());
        panelInfoView.setSize(200, 500);
        panelInfoView.add(infoView, BorderLayout.NORTH);
        //panelInfoView.setBackground(Color.getHSBColor(120, 100, 50));
        borderPanelBoard.add(panelInfoView, BorderLayout.WEST);

        // riempio il tavolo con tutte le carte ancora non girate
        for(int i=0; i<gameStatus.getNotShowingCards().size(); i++){
            CardView cardView = new CardView(gameStatus.getNotShowingCards().get(i));
            cardViews.add(cardView);
            gridPanelCards.add(cardView);
        }

        // when click a card
        setCardClickActionListener();


        //gridPanelCards.setBackground(Color.getHSBColor(120, 100, 50));
        borderPanelBoard.add(gridPanelCards,BorderLayout.EAST);
        //borderPanelBoard.setBackground(Color.getHSBColor(120, 100, 50));
        frame.add(borderPanelBoard);
        frame.setSize(675, 550);
        //frame.pack(); // o setSize o pack

        ImageIcon img = new ImageIcon("./images/frameIcon.jpg");
        frame.setIconImage(img.getImage());
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
    public void blockCards(){
        for (CardView cardView: cardViews) {
            cardView.setLogo();
            cardView.setDisabledIconLogo();
            cardView.setEnabled(false);
        }
        showMatchedCards();
    }

    // make cards clickable
    public void unblockCards(){
        // aggiornare da GameStatus

        for (CardView cardView: cardViews) {
            cardView.setLogo();
            cardView.setEnabled(true);
        }
        showMatchedCards();
    }

    //show already matched cards
    public void showMatchedCards(){
        for (int i=0; i<gameStatus.getShowingCards().size(); i++) {
            for (CardView cardView: cardViews) {
                if(gameStatus.getShowingCards().get(i).getIndex() == cardView.getCard().getIndex() &&
                        gameStatus.getShowingCards().get(i).getValue() == cardView.getCard().getValue()){
                    cardView.setImage();
                    cardView.setDisabledIconImage();
                    cardView.setEnabled(false);
                    break;
                }
            }
        }
    }


    public void resetAndUpdateInfoView(GameStatus gameStatus, int currentId) {
        this.gameStatus = gameStatus;
        selectedCard1 = null;
        selectedCard2 = null;

        this.getInfoView().update(gameStatus, currentId);

    }

    // called by not current players in gamecontroller
    public void updateBoardAfterMove(Move move) {
        for (CardView cardView: cardViews) {
            if(cardView.getCard().getIndex() == move.getCard1().getIndex() &&
                    cardView.getCard().getValue() == move.getCard1().getValue()){
                cardView.setImage();
                cardView.setDisabledIconImage();
                cardView.setEnabled(false);
                break;
            }
        }
        if(move.getCard2() != null){
            for (CardView cardView: cardViews) {
                if(cardView.getCard().getIndex() == move.getCard2().getIndex() &&
                        cardView.getCard().getValue() == move.getCard2().getValue()){
                    cardView.setImage();
                    cardView.setDisabledIconImage();
                    cardView.setEnabled(false);
                    break;
                }
            }
        }
    }
    /**
     * When a card is selected.
     * When it is the first card selected
     * When it is the second card selected
     * */
    public void setCardClickActionListener(){
        for (final CardView cardView: cardViews) {
            cardView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    boolean matched = false;

                    for (int i = 0; i < gameStatus.getShowingCards().size(); i++) {

                        if (gameStatus.getShowingCards().get(i).getIndex() == cardView.getCard().getIndex() &&
                                gameStatus.getShowingCards().get(i).getValue() == cardView.getCard().getValue()) {
                            matched = true;
                            break;
                        }
                    }

                    if(!matched){
                            if (selectedCard1 == null && selectedCard2 == null) { // prima mossa eseguita

                                selectedCard1 = cardView;
                                cardView.setImage();
                                move = new Move(selectedCard1.getCard());
                                gameStatus.setMove(move);

                                System.out.println("[BoardView]: First selected card: " + move.getCard1().getValue());

                            } else if (selectedCard2 == null) { // seconda mossa eseguita

                                selectedCard2 = cardView;
                                int card1Index = selectedCard1.getCard().getIndex();
                                int card2Index = selectedCard2.getCard().getIndex();

                                /** controllo match delle carte solo se non ho selezionato due volte la stessa*/
                                if ((card1Index != card2Index)) {
                                    cardView.setImage();
                                    move = new Move(selectedCard1.getCard(), selectedCard2.getCard());
                                    gameStatus.setMove(move);

                                    if (move.isMatch()) {

                                        //update score of current player
                                        System.out.println("[BoardView] aggiorno score di " + id);
                                        Player playerToUpd = gameStatus.findPlayerById(id);
                                        int playerIndex = gameStatus.getPlayersList().indexOf(playerToUpd);
                                        int score = gameStatus.getPlayersList().get(playerIndex).getScore() + 100;
                                        gameStatus.getPlayersList().get(playerIndex).setScore(score);
                                        infoView.update(gameStatus, id);

                                        // add matched card to showingCard array
                                        gameStatus.getShowingCards().add(move.getCard1());
                                        gameStatus.getShowingCards().add(move.getCard2());

                                        if (gameStatus.getShowingCards().size() == 20) {
                                            gameStatus.getPlayersList().get(0).setState(PLAYER_STATE.WINNER);
                                            showGameWinnerMessage("Game Ended, wait for the winner");
                                        }
                                    }
                                    System.out.println("[BoardView]: Second selected card: " + move.getCard2().getValue());
                                    System.out.println("[BoardView]: Match: " + move.isMatch());

                                } else {
                                    /** Doppio click sulla stessa carta*/
                                    selectedCard2 = null;
                                    System.out.println("[BoardView]: Same card already selected.");
                                    JOptionPane.showMessageDialog(null, "Same card already selected!");

                                }
                            } else {
                                /** Due carte già selezionate*/
                                System.out.println("[BoardView]: Two cards in this turn have been already selected.");
                            }

                            /**
                             * inserito ritardo per mostrare la seconda carta al giocatore corrente giocata
                             * da se stesso dopo di che invio del gameStatus in BROADCAST
                             * */
                            timer = new javax.swing.Timer(400, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (gameStatus.getMove() != null)
                                        broadcastMessageMove(gameStatus);

                                    timer.stop();
                                }
                            });

                            timer.setRepeats(false);

                            timer.start();
                        }
                    }

            });
        }
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    /**
     * @desc Called when a move is performed
     * @param GameStatus $gameStatus
     * @return void
     * */
    public void broadcastMessageMove(GameStatus gameStatus){

        gameController.broadcastMessage(gameStatus);
    }

    /**
     * Show dialog box to ask before exit the application on close
     * */
    private void setCloseOperation(){
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

    /**
     * Show dialog box to notify the winner
     * */
    public void showGameWinnerMessage(String msg){
        int input = JOptionPane.showConfirmDialog(null,
                msg,
                "Winner info \n\nDo you want to exit the game?",
                JOptionPane.CLOSED_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null);
        if (input == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void showAnotherPlayerIsWinnerMessage(Player winner){
        int input = JOptionPane.showConfirmDialog(null,
                "Player " + winner.getId() + " " + winner.getNickName() + " won :(" +
                        " \n\nDo you want to exit the game?",
                "Game over.",
                JOptionPane.CLOSED_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null);
        if (input == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public InfoView getInfoView() {
        return infoView;
    }

    @Override
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
}


