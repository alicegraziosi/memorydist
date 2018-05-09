package rmi;

import model.gameStatus.GameStatus;
import model.player.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @desc interface for the remote message service
 */
public interface RemoteMessageServiceInt extends Remote {

	/**
	 * @desc function which sends a message, a gameStatus msg
	 * @param GameStatus $message
	 */
	int sendMessage(GameStatus message) throws RemoteException;
		
}