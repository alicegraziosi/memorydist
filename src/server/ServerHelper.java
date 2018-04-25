// todo eliminare file se non serve

package server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmi.RemoteGameInterface;

public class ServerHelper {

	/**
	 * Setup a remoteInterface
	 * 
	 * @param remoteServer Class implementing a remote interface
	 * @param name Server's name
	 * @param port RMI registry's port
	 * @throws RemoteException
	 * @throws AccessException
	 */
	public static void setupServer(Remote remoteServer,
			String name, int port) throws RemoteException, AccessException {
		LocateRegistry.createRegistry(port);
		Remote stub = (Remote) UnicastRemoteObject.exportObject(remoteServer,
				port);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(name, stub);
	}

	/**
	 * Setup the client side of the caller, creating the host reference
	 * @param host
	 * @param name
	 * @return
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static RemoteGameInterface setupClient(String host, int port, String name) throws RemoteException, NotBoundException{
		System.out.println("[ServerHelper.setupClient]: host + port : " + host + port);
		Registry registry = LocateRegistry.getRegistry(port);
		RemoteGameInterface remoteClient = (RemoteGameInterface) registry.lookup("messageService");
        remoteClient.helloThere();
        return remoteClient;
	}
}
