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
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.println("Registry created on port " + port);
            } catch (ExportException ex) {
                registry = LocateRegistry.getRegistry(port);
                System.out.println("Registry found on port " + port);
            } catch (RemoteException ex) {
                System.out.println("Error creating registry on port " + port);
                ex.printStackTrace();
            }

            RemoteRegistrationServerImpl registration = new RemoteRegistrationServerImpl();

            //final RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt)
            //		UnicastRemoteObject.exportObject(registration, 0);

            String location = "rmi://" + host + ":" + port + "/registrazione";
            registry.rebind(location, registration);

            Thread serverThread = new Thread() {
                public void run() {
                    try {
                        System.out.println("Servizio di registrazione in attesa di giocatori...");
                        sleep(timeout * 1000);
                        registration.stopService();
                        System.out.println("Servizio di registrazione chiuso.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            serverThread.start();

        } catch (Exception e) {
            System.err.println("Errore servizio di registrazione");
            e.printStackTrace();
        }
    }
}
