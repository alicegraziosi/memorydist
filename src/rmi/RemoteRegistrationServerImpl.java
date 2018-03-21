package rmi;

import model.card.Card;
import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @desc implementation class of remote registration server
 *
 */
public class RemoteRegistrationServerImpl implements RemoteRegistrationServerInt {

	private int maxPlayersNumber;
	/** max number of players */
	private ArrayList<Player> players;
	/** players list */
	private int playerIndex = -1;
	/** index of each new player, incrementing each time */
	private boolean isServiceOpen;
	/** tells if service is opened */
	private boolean start;

	/** tells if service is started */

	/** constructor */
	public RemoteRegistrationServerImpl() {
		this.maxPlayersNumber = 4;
		this.players = new ArrayList();
		this.isServiceOpen = true;
		start = false;
	}

	/**
	 * @desc player game registration function
	 * @param String $nickName, InetAddress $hostAddress, int $port
	 * @return int playerIndex or -1 if time is over or reached max players number
	 */
	public synchronized int registerPlayer(String nickName, InetAddress hostAddress, int port) {

		if (playerIndex < maxPlayersNumber) {

			if (isServiceOpen) {

				playerIndex++; // player id				
				System.out.println("[RMIRegServ]: Nuovo giocatore: nome: " + nickName + ", id: " + Integer.toString(playerIndex));

				port = 2000 + playerIndex; // setting port of player with index $playerIndex
				Player player = new Player(playerIndex, nickName, hostAddress, port); // create player
				players.add(player); // adding player to list of players

				return playerIndex;

			} else {
				// time out
				System.out.println("[RMIRegServ]: Tempo scaduto per registrarsi come giocatore.\n");
				return -1;
			}
		} else {
			// reached max number of players
			System.out.println("[RMIRegServ]: Raggiunto numero massimo di giocatori registrati.\n");
			return -1;
		}

	}

	/**
	 * @desc stop the registration service, setting the game turn
	 * @return void
	 */
	public synchronized void stopService() {
		isServiceOpen = false;
		System.out.println("[RMIRegServ]: Tempo scaduto per registrarsi come giocatore.");

		// the turn is assigned to the first registered player, which has id = 1, with
		// index 0 in the arraylist
		if(players.size()>0){
			players.get(0).setMyTurn(true);  // setting the turn to the 1st registered player

			System.out.println("[RMIRegServ]: Lista dei giocatori:");
			for (int i = 0; i < players.size(); i++) { // iterate over the players print infos
				System.out.println("[RMIRegServ]: " + players.get(i).toString()); // players infos
			}

			System.out.println("[RMIRegServ]: Inizio del gioco.");
			start = true;
			notifyAll();
		}
	}

	/**
	 * @desc get the arraylist of players
	 * @return arraylist of players
	 */
	public synchronized ArrayList<Player> getPlayers() {
		if (!start)
			try {
				wait();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		return players;
	}

	/**
	 * @desc creating and getting the game status, creating not showing cards
	 * @return gameStatus object
	 */
	public synchronized GameStatus getGameStatus() {
		// initial gamestatus setting
		int cardNumber = 20;
		ArrayList<Card> showingCards = new ArrayList<Card>(); // no one
		ArrayList<Card> notShowingCards = new ArrayList<Card>(); // all

		// card generation
		for (int i = 0; i < 20; i++) {
			if (i >= 10) { // if true generate cards with same values of previous cards, (10,0), (11,1)
				Card card = new Card(i, i - 10);
				notShowingCards.add(card);
			} else { // generate cards with this pattern: (0,0), (1,1)
				Card card = new Card(i, i);
				notShowingCards.add(card);
			}
		}
		Collections.shuffle(notShowingCards);

		// creating new gameStatus
		GameStatus gameStatus = new GameStatus(0, players, // list of players
				-1, // id sender = -1 means that the sender is the registration service
				showingCards, // list of showing cards
				notShowingCards, // list of not showing cards
				null // move is null when it is the first time that the gameStatus is initialized
		);

		if (!start)
			try {
				wait();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

		return gameStatus;
	}
}
