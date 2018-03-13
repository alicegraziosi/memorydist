package server;
import model.gameStatus.GameStatus;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

import rmi.RemoteMessageServiceImpl;
import rmi.RemoteMessageServiceInt;

/**
 * the server of the player, responsible of creating RMI registry
 * */
public class PlayerServer {


    public static void setupRMIregistryAndServer(int port,
                                                 BlockingQueue<GameStatus> buffer){
        // each client has its own RMI registry on its own port
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port); // create registry

            // instance of the remote obj implementation
            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl(buffer);
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) 
            		UnicastRemoteObject.exportObject(messageService, 0);
            
            registry.bind("messageService", stub); // binding registry with the message service

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
