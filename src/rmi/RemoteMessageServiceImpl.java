package rmi;

import controller.GameController;
import model.gameStatus.GameStatus;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @desc implementation of remote message service interface
 */
public class RemoteMessageServiceImpl extends UnicastRemoteObject implements RemoteMessageServiceInt {

	private BlockingQueue<GameStatus> inputBuffer;
	private GameController gameController;
	private int messageCounter;
	private ReentrantLock msgCounterLock;

	public RemoteMessageServiceImpl(BlockingQueue<GameStatus> inputBuffer,
									GameController gameController) throws RemoteException {
		this.inputBuffer = inputBuffer;
		this.gameController = gameController;
		msgCounterLock = new ReentrantLock();
		messageCounter = 0;
	}

	public int sendMessage(GameStatus gameStatus) throws RemoteException {
		// un processo invoca sendMessage su un altro processo
		// il processo su cui è invocato sendMessage riceve il messaggio in questo punto

		inputBuffer.add(gameStatus);

		// todo processare messaggio quando l id del msg è minore dell id corrente

		System.out.println("[RMISImpl]: Message received from player " + gameStatus.getIdSender());
		//System.out.println("[RMISImpl]: gameStatus " + gameStatus.toString());

		System.out.println("[RMISImpl]: Message said that next player is: " + gameStatus.getCurrentPlayer().getId());
	
		gameController.setGameStatus(gameStatus); // setting the new updated gameStatus

		if(gameStatus.getMove() != null){
			System.out.println("[RMISImpl]: Message " + gameStatus.getId() + " contains a move");

			gameController.updateBoardAfterMove(gameStatus.getMove());

			if(gameStatus.getMove().getCard2() != null) {
				// due carte girate
				// riparte play game

				/*
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				*/

				gameController.playGame();
				return 2;
			}
		}

		return 1;
	}

	public void incMessageCounter() throws RemoteException{
		msgCounterLock.lock();
		try {
			messageCounter++;
		} finally {
			msgCounterLock.unlock();
		}
	}

	public BlockingQueue<GameStatus> getInputBuffer() {
		return inputBuffer;
	}

	public void setInputBuffer(BlockingQueue<GameStatus> inputBuffer) {
		this.inputBuffer = inputBuffer;
	}

	public int getMessageCounter() {
		return messageCounter;
	}

	public void setMessageCounter(int messageCounter) {
		this.messageCounter = messageCounter;
	}
}