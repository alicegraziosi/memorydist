import model.gameStatus.GameStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMessageServiceInt extends Remote {
    int sendMessage(GameStatus message) throws RemoteException;
}
