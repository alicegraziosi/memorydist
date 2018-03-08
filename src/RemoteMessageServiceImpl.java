import model.gameStatus.GameStatus;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteMessageServiceImpl implements RemoteMessageServiceInt{
    private static BlockingQueue<GameStatus> buffer = new LinkedBlockingQueue<GameStatus>();

    public int sendMessage(GameStatus message) throws RemoteException {
        //un processo invoca sendMessage su un altro processo
        // il processo su cui è invocato sendMessage riceve il messaggio in questo punto
        buffer.add(message);
        System.out.println("Messaggio ricevuto dal processo " + message.getIdSender());
        return 1;
    }
}
