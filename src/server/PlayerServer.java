package server;

import controller.GameController;
import model.gameStatus.GameStatus;
import listener.DataReceiverListener;
import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import rmi.RemoteGameInterface;
import rmi.RemoteMessageServiceImpl;
import rmi.RemoteMessageServiceInt;
import view.board.Board;


/**
 * the server of the player, responsible of creating RMI registry
 * */
public class PlayerServer implements RemoteGameInterface{
	
	private final static Logger logger = Logger.getLogger(PlayerServer.class.getName());
	private DataReceiverListener mListener;

    public static void setupRMIregistryAndServer(String host, int port,
                                                 BlockingQueue<GameStatus> buffer,
                                                 GameController gameController){


        //System.setProperty("java.rmi.server.hostname", host.toString());

        System.setProperty("java.security.policy", "file:./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
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

            // instance of the remote obj implementation
            RemoteMessageServiceInt messageService = new RemoteMessageServiceImpl(buffer, gameController);
            String location = "rmi://" + host + ":" + port + "/messageService";
            registry.rebind(location, messageService);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public void setupGame(GameStatus gameStatus) throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "Received game:"+ gameStatus.toString());
		mListener.setupRemoteClient(gameStatus);	
		
	}

	@Override
	public void sendGame(GameStatus gameStatus) throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		//logger.log(Level.INFO, "Received game:"+ gameStatus.toString());
//		System.out.println("[PlayerServer]: Received GameStatus " + gameStatus.getPlayersList().get(0).getMyTurn());
//		System.out.println("[PlayerServer]: Received GameStatus " + gameStatus.getPlayersList().get(1).getMyTurn());
		mListener.setGame(gameStatus);
		
	}
    public static void setupBoard(){

    }

}
