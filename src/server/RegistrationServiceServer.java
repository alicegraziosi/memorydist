package server;

import java.awt.image.BufferedImage;
import java.io.*;
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

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * @desc class that manage the registration service
 * */

public class RegistrationServiceServer {

    public static void main(String[] args) throws FileNotFoundException {

        String host = "localhost";

        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader("util.txt");
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                host = sCurrentLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("java.rmi.server.hostname", host);

        System.setProperty("java.security.policy", "file:./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        int port = 1099;

        final int timeout; // registration service timeout
        if (args.length == 0) {
            timeout = 30; // default timeout in seconds
        } else {
            timeout = Integer.parseInt(args[0]);
        }

        try {
            //Registry registry = LocateRegistry.getRegistry();

            // per non dover tutte le volte fare kill del processo rmiregistry
            // https://stackoverflow.com/questions/8386001/how-to-close-rmiregistry-running-on-particular-port

            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(port); // creating registry
            } catch (ExportException ex) {
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }

            // instance of remote obj implementation
            RemoteRegistrationServerImpl registration = new RemoteRegistrationServerImpl();

            final RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) 
            		UnicastRemoteObject.exportObject(registration, 0);

            String location = "rmi://" + host + ":" + port + "/registrazione";
            registry.rebind(location, stub);

            Thread serverThread = new Thread() {
                public void run() {
                try {
                    System.out.println("Servizio di registrazione in attesa di giocatori...");
                    sleep(timeout * 1000);
                    stub.stopService();
                    System.out.println("Servizio di registrazione chiuso.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
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
