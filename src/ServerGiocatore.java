import model.gameStatus.GameStatus;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

public class ServerGiocatore {


    public static void setupRMIregistryAndServer(int port, BlockingQueue<GameStatus> buffer){
        // ogni client ha il suo registro rmi sulla propria porta
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);

            // istanza dell'implementazione dell'oggetto remoto
            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl(buffer);
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) UnicastRemoteObject.exportObject(messageService, 0);
            registry.bind("messageService", stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
