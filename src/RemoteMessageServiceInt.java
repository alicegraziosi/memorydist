import model.gameStatus.GameStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface RemoteMessageServiceInt extends Remote {

    int sendMessage(GameStatus message) throws RemoteException;
}
