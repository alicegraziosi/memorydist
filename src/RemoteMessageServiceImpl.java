import model.gameStatus.GameStatus;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;

/**
 * @desc implementation of remote message service interface
 * */
public class RemoteMessageServiceImpl implements RemoteMessageServiceInt{

    private BlockingQueue<GameStatus> inputBuffer;

    public RemoteMessageServiceImpl(BlockingQueue<GameStatus> inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public int sendMessage(GameStatus message) throws RemoteException {
        // un processo invoca sendMessage su un altro processo
        // il processo su cui è invocato sendMessage riceve il messaggio in questo punto
        inputBuffer.add(message);
        System.out.println("Messaggio ricevuto dal giocatore " + message.getIdSender());
        return 1;
    }

    public BlockingQueue<GameStatus> getInputBuffer() {
        return inputBuffer;
    }

    public void setInputBuffer(BlockingQueue<GameStatus> inputBuffer) {
        this.inputBuffer = inputBuffer;
    }
}
