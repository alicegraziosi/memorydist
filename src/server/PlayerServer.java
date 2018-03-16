package server;
import controller.GameController;
import model.gameStatus.GameStatus;

import java.net.InetAddress;
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


    public static void setupRMIregistryAndServer(InetAddress host, int port,
                                                 BlockingQueue<GameStatus> buffer,
                                                 GameController gameController){

        //System.setProperty("java.security.policy", "./../../security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        // each client has its own RMI registry on its own port
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port); // create registry

            // instance of the remote obj implementation
            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl(buffer, gameController);
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt)
            		UnicastRemoteObject.exportObject(messageService, 0);

            //todo mettere url macchina su name (??)
            String name = "rmi://" + host + ":" + port + "/messageService";
            //String name = "messageService";
            registry.bind(name, stub); // binding registry with the message service


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
