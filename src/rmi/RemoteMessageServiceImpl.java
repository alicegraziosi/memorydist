package rmi;

import controller.GameController;
import model.gameStatus.GameStatus;
import view.board.Board;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @desc implementation of remote message service interface
 */
public class RemoteMessageServiceImpl implements RemoteMessageServiceInt {

	private BlockingQueue<GameStatus> inputBuffer;
	private GameController gameController;

	public int getMessageCounter() {
		return messageCounter;
	}

	public void setMessageCounter(int messageCounter) {
		this.messageCounter = messageCounter;
	}

	private int messageCounter;
	private ReentrantLock msgCounterLock;

	public RemoteMessageServiceImpl(BlockingQueue<GameStatus> inputBuffer,
									GameController gameController) {
		this.inputBuffer = inputBuffer;
		this.gameController = gameController;
		msgCounterLock = new ReentrantLock();
		messageCounter = 0;
	}

	public int sendMessage(GameStatus gameStatus) throws RemoteException {
		// un processo invoca sendMessage su un altro processo
		// il processo su cui è invocato sendMessage riceve il messaggio in questo punto

		inputBuffer.add(gameStatus);
		// todo non fare questo quando l id del msg è minore dell id corrente
		gameController.setGameStatus(gameStatus);
		//board.update(gameStatus);
        System.out.println("[RMISImpl]: Messaggio ricevuto dal giocatore " + gameStatus.getIdSender());
        //System.out.println("[RMISImpl]: gameStatus " + gameStatus.toString());
		return 1;
	}

	public BlockingQueue<GameStatus> getInputBuffer() {
		return inputBuffer;
	}

	public void setInputBuffer(BlockingQueue<GameStatus> inputBuffer) {
		this.inputBuffer = inputBuffer;
	}

	public void incMessageCounter() throws RemoteException{
		msgCounterLock.lock();
		try {
			messageCounter++;
		} finally {
			msgCounterLock.unlock();
		}
	}
}
