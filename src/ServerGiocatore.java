import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerGiocatore {


    public static void setupRMIregistryAndServer(int port){
        // ogni client ha il suo registro rmi sulla propria porta
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);

            // istanza dell'implementazione dell'oggetto remoto
            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl();
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) UnicastRemoteObject.exportObject(messageService, 0);
            //Naming.rebind("messageService", stub);
            registry.bind("messageService", stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
