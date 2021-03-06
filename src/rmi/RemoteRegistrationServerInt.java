package rmi;

import model.gameStatus.GameStatus;
import model.player.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @desc interface used for registration step
 */
public interface RemoteRegistrationServerInt extends Remote {

	/**
	 * @desc registration player service
	 */
	int registerPlayer(String player, String hostAddress) throws RemoteException;

	/**
	 * @desc stop service
	 */
	void stopService() throws RemoteException;

	/**
	 * @desc get players list
	 * @return array list of players
	 */
	ArrayList<Player> getPlayers() throws RemoteException;

	/**
	 * @desc get current game status
	 * @return gameStatus object
	 */
	GameStatus getGameStatus() throws RemoteException;
}