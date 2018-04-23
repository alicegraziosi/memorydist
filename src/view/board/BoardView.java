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

    private JPanel panel1;
    private JLabel label1;
    private JPanel panel2;

    private GameStatus gameStatus;
    private ArrayList<CardView> cardViews;
   	private ArrayList<CardView> cardViewsMatch;
    private CardView selectedCard1;
    private CardView selectedCard2;
    private Move move;
    private InfoView infoView;
    private int id; // id player
    private JFrame frame;
    private JPanel gridPanelCards;

    private PlayerClient playerClient;
    private GameController gameController;
    private Timer t;

    public BoardView(GameStatus gameStatus, int id, PlayerClient playerClient) {
        this.gameStatus = gameStatus;
        this.cardViews = new ArrayList<>();
        this.cardViewsMatch = new ArrayList<>();
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

    //public static void main(String[] args) {
    public void init(){

        /*
         * Il timer mi permette di avere un margine di secondi per vedere le carte,
         * di default e' settato a 750 ma si può variare
         */
        t = new javax.swing.Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        t.setRepeats(false);

        // look and feel (superfluo e quindi commentato)
        /*
        try {
            for (UIManager.LookAndFeelInfo infoView : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(infoView.getName())) {
                    UIManager.setLookAndFeel(infoView.getClassName());
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

        // pannello laterale con infoView di gioco
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.setSize(200, 500);
        panel1.add(infoView, BorderLayout.NORTH);
        borderPanelBoard.add(panel1, BorderLayout.WEST);

        // riempio il tavolo con tutte le carte ancora non girate
        for(int i=0; i<gameStatus.getNotShowingCards().size(); i++){
            CardView cardView = new CardView(gameStatus.getNotShowingCards().get(i));
            cardViews.add(cardView);
            gridPanelCards.add(cardView);
        }
        
      
        // when click a card
        setCardClickActionListener();

        borderPanelBoard.add(gridPanelCards,BorderLayout.EAST);

        frame.add(borderPanelBoard);

        frame.setSize(800, 650);
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
            cardView.setEnabled(false);
//            gridPanelCards.add(cardView);
        	System.out.println("sto coprendo " +  cardView.getCard());
//        	
        }
        for (CardView cardView: cardViewsMatch) {
            cardView.setImage();
            cardView.setEnabled(false);
//            gridPanelCards.add(cardView);
        }
        showMatchedCards();
    }

    // make cards clickable
    public void unblockCards(){
//    	gridPanelCards.removeAll();
//    	gridPanelCards.revalidate();
//    	gridPanelCards.repaint();
    	
    	// aggiornare da GameStatus
    	
        for (CardView cardView: cardViews) {
            cardView.setLogo();
            cardView.setEnabled(true);
        }
	    for (CardView cardView: cardViewsMatch) {
	        cardView.setImage();
	        cardView.setEnabled(false);
	    }
//	    for(int i=0; i<gameStatus.getShowingCards().size(); i++){
//            CardView cardViewMatch = new CardView(gameStatus.getShowingCards().get(i));
//            cardViewsMatch.add(cardViewMatch);
//            gridPanelCards.add(cardViewMatch);
//        }
        showMatchedCards();
    }

    //show already matched cards
    public void showMatchedCards(){
        for (int i=0; i<gameStatus.getShowingCards().size(); i++) {
            for (CardView cardView: cardViews) {
                if(gameStatus.getShowingCards().get(i).getIndex() == cardView.getCard().getIndex() &&
                        gameStatus.getShowingCards().get(i).getValue() == cardView.getCard().getValue()){
                    cardView.setImage();
                    cardView.setEnabled(false);
                    break;
                }
            }
        }
    }

    public void update() {
    	for ( int i = 0; i < gameStatus.getShowingCards().size(); i++) {
    		if ( cardViews.get(i).getCard().getValue() == gameStatus.getShowingCards().get(i).getValue() )
    			cardViews.get(i).setImage();
    				
    	}
    }
    
    public void reset(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        selectedCard1 = null;
        selectedCard2 = null;
    }

    // called by not current players in gamecontroller
    public void updateBoardAfterMove(Move move) {
        for (CardView cardView: cardViews) {
            if(cardView.getCard().getIndex() == move.getCard1().getIndex() &&
                    cardView.getCard().getValue() == move.getCard1().getValue()){
                cardView.setImage();
                break;
            }
        }
        if(move.getCard2() != null){
            for (CardView cardView: cardViews) {
                if(cardView.getCard().getIndex() == move.getCard2().getIndex() &&
                        cardView.getCard().getValue() == move.getCard2().getValue()){
                    cardView.setImage();
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
                  
                    if(selectedCard1 == null && selectedCard2 == null){ // prima mossa eseguita

                        selectedCard1 = cardView;
                        cardView.setImage();
                        move = new Move(selectedCard1.getCard());
                        gameStatus.setMove(move);

                        System.out.println("[BoardView]: First selected card: " + move.getCard1().getValue());
                        
                    } else if (selectedCard2==null) { // seconda mossa eseguita
                    	
                		selectedCard2 = cardView;
                		int card1Value = selectedCard1.getCard().getValue();
                		int card2Value = selectedCard2.getCard().getValue();
                		int card1Index = selectedCard1.getCard().getIndex();
                		int card2Index = selectedCard2.getCard().getIndex();

                		/** controllo match delle carte solo se non ho selezionato due volte la stessa*/
                        if ( (card1Index != card2Index) ) {                                 
	                		cardView.setImage();
	                        move = new Move(selectedCard1.getCard(), selectedCard2.getCard());
	                        gameStatus.setMove(move);
	                
	                        if(move.isMatch()){
	                        	
	                        	JOptionPane.showMessageDialog(null, "Matched!");
	                            //update score of current player
	                            int score = gameStatus.getPlayersList().get(id).getScore() + 100;
	                            gameStatus.getPlayersList().get(id).setScore(score);
	                            //infoView.updateScores(gameStatus);
	                            infoView.update(gameStatus, id);
	
	                            // add matched card to showingCard array
	                            gameStatus.getShowingCards().add(move.getCard1());
	                            gameStatus.getShowingCards().add(move.getCard2());
	                            // remove matched card 
	                            gameStatus.getNotShowingCards().remove(move.getCard1());
	                            gameStatus.getNotShowingCards().remove(move.getCard2());
	                            // add card matched
	                            cardViewsMatch.add(selectedCard1);
	                            cardViewsMatch.add(selectedCard2);
	                            // remove card matched from cardViews
                                cardViews.remove(selectedCard1);
	                            cardViews.remove(selectedCard2);
		                       
	                            showMatchedCards();
	
	                            if(gameStatus.getShowingCards().size()==20){
	                                gameStatus.getPlayersList().get(0).setState(PLAYER_STATE.WINNER);
	                                showGameWinnerMessage();
	                            }
	                        }
	                        
	                        
	                        System.out.println("[BoardView]: Second selected card: " + move.getCard2().getValue());
	                        System.out.println("[BoardView]: Match: " + move.isMatch());
	                     
                        }    
                        else if (card1Index == card2Index){
                        	/** Doppio click sulla stessa carta*/
                        	selectedCard2 = null;
                        	System.out.println("[BoardView]: Same card already selected.");
                        	JOptionPane.showMessageDialog(null, "Same card already selected!");
       
                        } else {
                            /** controllo match delle carte solo se non ho selezionato due volte la stessa*/
                            cardView.setImage();

                            move = new Move(selectedCard1.getCard(), selectedCard2.getCard());
                            gameStatus.setMove(move);

                            if (move.isMatch()) {
                                //update score of current player
                                gameStatus.getPlayersList().get(id).updateScore();

                                // add matched card to showingCard array
                                gameStatus.getShowingCards().add(move.getCard1());
                                gameStatus.getShowingCards().add(move.getCard2());

                                cardViewsMatch.add(selectedCard1);
                                cardViewsMatch.add(selectedCard2);

                                if (gameStatus.getShowingCards().size() == 20) {
                                    gameStatus.getPlayersList().get(0).setState(PLAYER_STATE.WINNER);
                                }
                            }

                            System.out.println("[BoardView]: Second selected card: " + move.getCard2().getValue());
                            System.out.println("[BoardView]: Match: " + move.isMatch());
                        }
                    } else {
                    	/** Due carte già selezionate*/
                        System.out.println("[BoardView]: Two cards in this turn have been already selected.");
                    } 
        
            /**
             * inserito ritardo per mostrare la seconda carta al giocatore corrente giocata 
             * da se stesso
             * dopo di che invio del gameStatus in BROADCAST
             * -----DA SISTEMARE -----
             * */
                t = new javax.swing.Timer(400, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	System.out.println("[BoardView]: SELECTED CARDS" + selectedCard1 +
                    			" e " + selectedCard2);
                    	System.out.println("[BoardView]: AL CLICK DI DUE CARTE ");
                    	//System.out.println("[BoardView]: gameStatus: " + gameStatus);
                    	
                    	if ( gameStatus.getMove() != null )
	                        broadcastMessageMove(gameStatus);
                        
                        t.stop();
                    }
                });

                t.setRepeats(false);
                
            	t.start();
                	
                }
            });
        }
    }

    public void showTurnMessage() {
    	
    	if(gameStatus.getCurrentPlayer().getId() == id && !gameStatus.getCurrentPlayer().isCrashed())
	    	JOptionPane.showMessageDialog(null, "It is YOUR turn !");
    	else
    		JOptionPane.showMessageDialog(null, "It is the turn of " + gameStatus.getCurrentPlayer().getId() + " !");
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
    public void showGameWinnerMessage(){
        int input = JOptionPane.showConfirmDialog(null,
                "You are the winner!\n\nDo you want to exit the game?",
                "You are the winner!",
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

	 
    public ArrayList<CardView> getCardViews() {
		return cardViews;
	}

	public void setCardViews(ArrayList<CardView> cardViews) {
		this.cardViews = cardViews;
	}

	public ArrayList<CardView> getCardViewsMatch() {
		return cardViewsMatch;
	}

	public void setCardViewsMatch(ArrayList<CardView> cardViewsMatch) {
		this.cardViewsMatch = cardViewsMatch;
	}


}


