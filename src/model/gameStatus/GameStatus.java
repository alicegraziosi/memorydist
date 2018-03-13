package model.gameStatus;

import model.card.Card;
import model.player.Player;
import model.move.Move;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *  @desc class representing the global state of the game, gameStatus is the msg updated each turn
 */
public class GameStatus implements Serializable {

    private ArrayList<Player> playersList;
    private int idSender;
    private ArrayList<Card> showingCards;
 	private ArrayList<Card> notShowingCards;
    private Move move;
    
    /** constructor */
    public GameStatus(ArrayList<Player> playersList,
                      int idSender,
                      ArrayList<Card> showingCards,
                      ArrayList<Card> notShowingCards,
                      Move move) {
        this.playersList = playersList;
        this.idSender = idSender;
        this.showingCards = showingCards;
        this.notShowingCards = notShowingCards;
        this.move = move;
    }

    /**
     *  @desc return the array list of players
     *  @return array list $playersList
     */
    public ArrayList<Player> getPlayersList() {
        return playersList;
    }
    
    /**
     *  @desc set the array list of players
     *  @param array list $playersList
     */
    public void setPlayersList(ArrayList<Player> playersList) {
        this.playersList = playersList;
    }
    
    /**
     *  @desc get the id of the gameStatus sender
     *  @return int $idSender
     */
    public int getIdSender() {
        return idSender;
    }
    
    /**
     *  @desc set the id of the gameStatus sender
     *  @param int $idSender
     */
    public void setIdSender(int idSender) {
        this.idSender = idSender;
    }
    
    /**
     *  @desc get the showing cards
     *  @return array list $showingCards
     */
    public ArrayList<Card> getShowingCards() {
 		return showingCards;
 	}
    
    /**
     *  @desc set the showing cards
     *  @param array list $showingCards
     */
 	public void setShowingCards(ArrayList<Card> showingCards) {
 		this.showingCards = showingCards;
 	}
 	
 	/**
     *  @desc get the not showing cards
     *  @return array list $notShowingCards
     */
 	public ArrayList<Card> getNotShowingCards() {
 		return notShowingCards;
 	}
 	
 	/**
     *  @desc set the not showing cards
     *  @param array list $notShowingCards
     */
 	public void setNotShowingCards(ArrayList<Card> notShowingCards) {
 		this.notShowingCards = notShowingCards;
 	}
 	
 	/**
     *  @desc get move
     *  @return Move $move
     */
 	public Move getMove() {
 		return move;
 	}
 	
 	/**
     *  @desc set the move
     *  @param Move $move
     */
 	public void setMove(Move move) {
 		this.move = move;
 	}

}
