package server;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AccessException;
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
 * @desc class that manages the registration service
 * */

public class RegistrationServiceServer {

    public static void main(String[] args) {

        String host = "localhost";

        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader("serverHost.txt");
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                host = sCurrentLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
            host = "localhost";
        }

        int port = 1099;

        System.setProperty("java.rmi.server.hostname", host);

        System.setProperty("java.security.policy", "file:./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        final int timeout; // registration service timeout
        if (args.length == 0) {
            timeout = 15; // default timeout in seconds
        } else {
            timeout = Integer.parseInt(args[0]); // passed as an argument
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

            final RemoteRegistrationServerImpl registration = new RemoteRegistrationServerImpl();
            String location = "rmi://" + host + ":" + port + "/registrazione";
            registry.rebind(location, registration);

            Thread thread = new Thread() {
                public void run() {
                try {
                    System.out.println("Registration service listening for players...");
                    sleep(timeout * 1000);
                    registration.stopService();
                    System.out.println("Registration service closed.");

                    // terminate execution after a while
                    sleep(5 * 1000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }
            };
            thread.start();

        } catch (AccessException e) {
            System.err.println("Registration service error.");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Registration service error.");
            e.printStackTrace();
        }
    }
}