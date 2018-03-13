package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import rmi.RemoteRegistrationServerImpl;
import rmi.RemoteRegistrationServerInt;

/**
 * @desc class that manage the registration service
 * */

public class RegistrationServiceServer {

    public static void main(String[] args) {
        final int timeout; // registration service timeout
        if (args.length == 0) {
            timeout = 30; // default timeout in seconds
        } else {
            timeout = Integer.parseInt(args[0]);
        }

        //Registry registry = LocateRegistry.getRegistry();

        // per non dover tutte le volte fare kill del processo rmiregistry
        // https://stackoverflow.com/questions/8386001/how-to-close-rmiregistry-running-on-particular-port

        try {

            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(1099); // creating registry
            } catch (ExportException ex) {
                registry = LocateRegistry.getRegistry(1099);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }


            // instance of remote obj implementation
            RemoteRegistrationServerImpl registration = new RemoteRegistrationServerImpl();

            final RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) 
            		UnicastRemoteObject.exportObject(registration, 0);

            registry.bind("registrazione", stub);

            Thread serverThread = new Thread() {
                public void run() {
                try {
                    System.out.println("Servizio di registrazione in attesa di giocatori...");
                    sleep(timeout * 1000);
                    stub.stopService();
                    Naming.unbind("registrazione");
                    System.out.println("Servizio di registrazione chiuso.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                }
            };
            serverThread.start();

        } catch (Exception e) {
            System.err.println("Servizio di registrazione exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
