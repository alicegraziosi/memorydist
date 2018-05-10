package server;

import controller.GameController;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import rmi.RemoteMessageServiceImpl;
import rmi.RemoteMessageServiceInt;

/**
 * the server of the player, responsible of creating RMI registry
 * */
public class PlayerServer{

    public static void setupRMIregistryAndServer(String host, int port,
                                                 GameController gameController){

        // in lab funziona con la riga seguente non commentata (non modificare!!!)
        System.setProperty("java.rmi.server.hostname", host);

        System.setProperty("java.security.policy", "file:./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.println("Registry created on " + host + ":" + port);
            } catch (ExportException ex) {
                registry = LocateRegistry.getRegistry(port);
                System.out.println("Registry found on " + host + ":" + port);
            } catch (RemoteException ex) {
                System.out.println("Error creating registry on " + host + ":" + port);
                ex.printStackTrace();
            }

            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl(gameController);
            String location = "rmi://" + host + ":" + port + "/messageService";
            registry.rebind(location, messageService);

        } catch (RemoteException e) {
            System.err.println("Message service error.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Message service error.");
            e.printStackTrace();
        }
    }

}