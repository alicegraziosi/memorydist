//todo eliminare se non serve
package rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import model.gameStatus.GameStatus;

public interface RemoteGameInterface {
	
	/**
	 * Setup game to server
	 * @param GameStatus $gameStatus
	 * @throws RemoteException
	 * @throws NotBoundException 
	 */
	void setupGame(GameStatus gameStatus) throws RemoteException, NotBoundException;
	
	/**
	 * Send the current game to other players
	 * @param GameStatus $gameStatus
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	void sendGame(GameStatus gameStatus) throws RemoteException, NotBoundException;

	void helloThere()  throws RemoteException, NotBoundException;
}
