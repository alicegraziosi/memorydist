package rmi;

import model.gameStatus.GameStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @desc interface for the remote message service
 */
public interface RemoteMessageServiceInt extends Remote {

	/**
	 * @desc function which sends a message, a gameStatus msg
	 * @param GameStatus
	 *            $message
	 */
	int sendMessage(GameStatus message) throws RemoteException;
}