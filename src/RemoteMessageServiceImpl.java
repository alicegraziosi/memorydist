import model.gameStatus.GameStatus;

import java.rmi.RemoteException;

public class RemoteMessageServiceImpl implements RemoteMessageServiceInt{
    public int sendMessage(GameStatus message) throws RemoteException {
        return 1;
    }
}
