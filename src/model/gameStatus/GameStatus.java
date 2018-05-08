package model.gameStatus;

import model.card.Card;
import model.player.*;
import exception.NextPlayerNotFoundException;
import model.move.Move;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *  @desc class representing the global state of the game, gameStatus is the msg updated each turn
 */
public class GameStatus implements Serializable {

	private int id;
    private ArrayList<Player> playersList;
    private int idSender; // ?
    private ArrayList<Card> showingCards;
 	private ArrayList<Card> notShowingCards;
    private Move move;
    Player currentPlayer;
    private HashMap<Integer,PLAYER_STATE> playersAvailability = new HashMap<Integer,PLAYER_STATE>();
    
    /**
     * Starting game constructor
     * */
    public GameStatus(ArrayList<Player> playersList,
    				  int idSender,
					  ArrayList<Card> showingCards,
					  ArrayList<Card> notShowingCards,
					  Move move) {

		this.playersList = playersList;
		this.idSender = idSender;
		this.showingCards = showingCards;
		this.notShowingCards = notShowingCards;
		
		// Draw the starting player
		this.currentPlayer = playersList.get(0);

		// Setting each player active
		for (Player p : playersList){
			this.playersAvailability.put(p.getId(), PLAYER_STATE.ACTIVE);
		}

		this.move = move;
	}
   
    
    
    /**
     * setting the next turn managing players array list in gameStatus
     * */
    public void setNextPlayer(){
    	// 1
    	int indexCurrentPlayer = currentPlayer.getId();

		iterateNextPlayer(indexCurrentPlayer);
    }

    private void iterateNextPlayer(int indexCurrentPlayer) {
    	// 2 - > 3
		int indexNextPlayer = indexCurrentPlayer + 1;

		// se il giocatore corrente è l'ultimo, il prossimo è il primo
		if(indexNextPlayer >= playersList.size()){
			// 3 -> 0
			indexNextPlayer = 0;
		}

		// is crashed = true
		if(playersList.get(indexNextPlayer).isCrashed()){
			// 3
			iterateNextPlayer(indexNextPlayer);
		} else {
			Player nextPlayer = playersList.get(indexNextPlayer);
			currentPlayer = nextPlayer;
			System.out.println("[GameStatus]: Next player is " + nextPlayer);
		}

	}

    /**
     * @desc checking if some player has won
     * @return boolean, true if one player has won, otherwise false
     * */
    public boolean playerWon() {
		for (Player player : playersList) {
			if (player.getState() == PLAYER_STATE.WINNER) {
				return true;
			}
		}

		return false;
	}
    
    public int countPlayersActive() {
    	int playersActive = 0;
    	for(int i = 0; i < this.playersAvailability.size(); i++) {
    		PLAYER_STATE value = this.playersAvailability.get(i);
    		if ( value == PLAYER_STATE.ACTIVE)
    			playersActive ++;
    	}

    	return playersActive;
    }
    
    
    /**
     * @desc setting the state of det player
     * @param int $id of the player, PLAYER_STATE $state of the player
     * */
    public void setPlayerState(int id, PLAYER_STATE state){
		playersAvailability.put(id, state);
	}
	
    /**
     * @desc getting the state of det player
     * @param int $id of the player
     * @return PLAYER_STATE $state
     * */
	public PLAYER_STATE getPlayerState(int id){
		return playersAvailability.get(id);
	}
    
    /**
	 * @desc set to string game status
	 */
	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");
		return "GameStatus " +" { " + newLine +	
				"playerList=" + playersList + newLine +	
				", showingCards='" + showingCards + newLine +	
				"', notShowingCards=" + notShowingCards + newLine +	
				", move=" + move + newLine +
				", currentPlayer=" + currentPlayer + newLine +
				" }";
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
     *  @desc get the not showing cards
     *  @return array list $notShowingCards
     */
 	public ArrayList<Card> getNotShowingCards() {
 		return notShowingCards;
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
 	
	/**
     *  @desc get currentPlayer
     *  @return Player $currentPlayer
     */
    public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}