package listener;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import model.gameStatus.*;

public interface DataReceiverListener {
	/**
	 * Setup remote client with given game
	 * 
	 * @param GameStatus $game
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 */
	void setupRemoteClient(GameStatus gameStatus) throws RemoteException, NotBoundException;
	
	/**
	 * Set the Game received from client
	 * @param game
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 */
	void setGame(GameStatus gameStatus) throws RemoteException, NotBoundException;
}
