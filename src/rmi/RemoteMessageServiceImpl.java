package rmi;

import controller.GameController;
import model.gameStatus.GameStatus;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;

/**
 * @desc implementation of remote message service interface
 */
public class RemoteMessageServiceImpl implements RemoteMessageServiceInt {

	private BlockingQueue<GameStatus> inputBuffer;
	private GameController gameController;

	public RemoteMessageServiceImpl(BlockingQueue<GameStatus> inputBuffer, GameController gameController) {
		this.inputBuffer = inputBuffer;
		this.gameController = gameController;
	}

	public int sendMessage(GameStatus message) throws RemoteException {
		// un processo invoca sendMessage su un altro processo
		// il processo su cui è invocato sendMessage riceve il messaggio in questo punto
		inputBuffer.add(message);
		gameController.setGameStatus(message);
		System.out.println("Messaggio ricevuto dal giocatore " + message.getIdSender());
        for(int i=0; i<gameController.getGameStatus().getPlayersList().size(); i++){
            System.out.println(gameController.getGameStatus().getPlayersList().get(i).toString());
        }
		return 1;
	}

	public BlockingQueue<GameStatus> getInputBuffer() {
		return inputBuffer;
	}

	public void setInputBuffer(BlockingQueue<GameStatus> inputBuffer) {
		this.inputBuffer = inputBuffer;
	}
}
