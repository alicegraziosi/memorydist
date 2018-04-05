//package controller;
//
//import java.io.File;
//import java.io.IOException;
//import java.rmi.AccessException;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import client.RMIClient;
//import model.gameStatus.GameStatus;
//import model.player.CurrentPlayer;
//import model.player.Player;
//import rmi.RemoteGameInterface;
//import server.RMIServer;
//import server.ServerHelper;
///**
// * 
// * */
//public class Starter {
//
//private static final Logger logger = Logger.getLogger(Starter.class.getName());
//	
//	private static int id;
//
//	private static GameController gameController;
//	private static GameStatus gameStatus;
//	
//	public static void startGame(String host, int port, ArrayList<Player> players) throws IOException,NotBoundException {
//			
//			gameStatus = new GameStatus(players,null,null,null);
//			serverConfiguration(host, port);
//			
//			gameController = new GameController();
//			RMIClient client = new RMIClient(gameStatus, id);
//			gameController.setPlayerClient(client);
//			gameController.getPlayerClient().broadcastNewGame(gameStatus);
//			gameController.setGame(gameStatus);
//
//	}
//	
//	private static void serverConfiguration(String host, int port) throws RemoteException, AccessException {
//		
//		CurrentPlayer.getInstance().setId(id); 
//		CurrentPlayer.getInstance().setHost(host);
//		
//		GameController gameController = new GameController();
//		RemoteGameInterface remoteServer = new RMIServer(gameController);
//		ServerHelper.setupServer(remoteServer, "messageService" , port);
//		
//		System.out.println("[Starter.ServerConfiguration]: " + remoteServer);
//		gameController.setPlayerServer(remoteServer);
//		gameController.setId(id);
//		gameController.setName("messageService");
//		gameController.setPort(port);
//	}
//}
