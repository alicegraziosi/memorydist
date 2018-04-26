package rmi;

import model.card.Card;
import model.gameStatus.GameStatus;
import model.player.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @desc implementation class of remote registration server
 *
 */
public class RemoteRegistrationServerImpl extends UnicastRemoteObject implements RemoteRegistrationServerInt {

	private int maxPlayersNumber; // max number of players
	private ArrayList<Player> players; //players list
	private int playerIndex = -1; // index of each new player, incrementing each time
	private boolean isServiceOpen;  // tells if service is opened
	private boolean startGame; // tells if game can starts

	private ArrayList<Card> showingCards; // list of matched cards
	private ArrayList<Card> notShowingCards; // initial set of cards, all hidde

	/** constructor */
	public RemoteRegistrationServerImpl() throws RemoteException {
		this.maxPlayersNumber = 8;
		this.players = new ArrayList();
		this.isServiceOpen = true;
		this.startGame = false;

		// card generation
		int cardNumber = 20;
		showingCards = new ArrayList<Card>(); // no one uncovered on start
		notShowingCards = new ArrayList<Card>(); // all covered on start

		for (int i = 0; i < 20; i++) {
			if (i >= 10) { // if true generate cards with same values of previous cards, (10,0), (11,1)
				Card card = new Card(i, i - 10);
				notShowingCards.add(card);
			} else { // generate cards with this pattern: (0,0), (1,1)
				Card card = new Card(i, i);
				notShowingCards.add(card);
			}
		}
		// todo commentata per fare prove con carte matched, SCOMMENTARE!!
		//Collections.shuffle(notShowingCards);
	}

	/**
	 * @desc player game registration function
	 * @param String $nickName, InetAddress $hostAddress, int $port
	 * @return int playerIndex or -1 if time is over or reached max players number
	 */
	public synchronized int registerPlayer(String nickName, String hostAddress) {

		if (playerIndex < maxPlayersNumber) {

			if (isServiceOpen) {

				playerIndex++; // player id
				nickName = "player " + playerIndex;
				int port = 2000 + playerIndex; // player port, each player has a different port
				Player player = new Player(playerIndex, nickName, hostAddress, port); // create player
				System.out.println("[RMIRegServ]: New player: " + player.toString());
				players.add(player); // adding player to list of players
				return playerIndex;

			} else {
				// service is closed due to timeout
				return -1;
			}
		} else {
			// reached max number of players
			return -2;
		}
	}

	/**
	 * @desc stop the registration service, setting the game turn
	 * @return void
	 */
	public synchronized void stopService() {
		isServiceOpen = false;
		System.out.println("[RMIRegServ]: Time expired to register.");

		if(players.size()>0){
            // setting the turn to the 1st registered player
			players.get(0).setMyTurn(true);

			System.out.println("[RMIRegServ]: Player list:");
			for (int i = 0; i < players.size(); i++) { // iterate over the players print infos
				System.out.println("[RMIRegServ]: " + players.get(i).toString()); // players infos
			}

			System.out.println("[RMIRegServ]: Game started.");
			startGame = true;
			notifyAll();
		}
	}

	/**
	 * @desc get the arraylist of players
	 * @return arraylist of players
	 */
	public synchronized ArrayList<Player> getPlayers() {
		if (startGame == false)
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
		
		GameStatus gameStatus = new GameStatus(players, // list of players
				-1, // id sender = -1 means that the sender is the registration service
				showingCards, // list of showing cards
				notShowingCards, // list of not showing cards
				null // move is null when it is the first time that the gameStatus is initialized
//				players.get(0) // setting the current player of the first turn
				);

        if (startGame == false)
			try {
				wait();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		return gameStatus;
	}
}