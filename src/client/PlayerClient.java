package client;

import controller.GameController;
import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteRegistrationServerInt;
import server.PlayerServer;
import view.board.BoardView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * the client used by player
 * */
public class PlayerClient {

    public static int id;
    public static String host; // player host
    public static int port; // player port

    public static String regServerHost; // registration server host (localhost o per esempio Gisella: 130.136.4.121)
    public static int regServerPort; // default on 1099

    public static ArrayList<Player> players; // array list of player
    public static GameStatus gameStatus; // global status of the game
    public static BlockingQueue<GameStatus> buffer; // ?

    public static BoardView board;
    public static GameController gameController;

    public PlayerClient() {

    }

    public static void main(final String[] args) {

        /*
        Thread thread = new Thread(new Runnable() {
            //@Override
            public void run() {
        */
                try {

                    // the registration server ip is read from util.txt
                    BufferedReader br = null;
                    FileReader fr = null;
                    try {
                        fr = new FileReader("util.txt");
                        br = new BufferedReader(fr);
                        String sCurrentLine;
                        while ((sCurrentLine = br.readLine()) != null) {
                            regServerHost = sCurrentLine;
                        }
                    } catch (IOException e) {
                        // if the registration server ip is not an argument, so it is localhost
                        regServerHost = (args.length < 1) ? "localhost" : args[0];
                        e.printStackTrace();
                    }

                    // the registration server port
                    regServerPort = 1099;

                    // in lab funziona con la riga seguente non commentata
                    System.setProperty("java.rmi.server.hostname", regServerHost);

                    System.setProperty("java.security.policy", "file:./security.policy");

                    if (System.getSecurityManager() == null) {
                        System.setSecurityManager(new SecurityManager());
                    }

                    // player host
                    host = null;
                    try{
                        host = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException ex){
                        ex.printStackTrace();
                    }

                    System.out.println("[Client]: Request to registration server on " + regServerHost + ":" + regServerPort);

                    Registry registry = LocateRegistry.getRegistry(regServerHost, regServerPort);

                    String name = "rmi://" + regServerHost + ":" + regServerPort + "/registrazione";
                    RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) registry.lookup(name);

                    // todo prenderlo da terminale
                    String nomeGiocatore = "default name";

                    // registration server returns id of the player
                    // it returns -1 if it is reached maximum number of registered players
                    int response = stub.registerPlayer(nomeGiocatore, host);

                    if(response == -1) {
                        System.out.println("[Client]: Response from registration server: " +
                                "time expired to register.");
                        System.exit(0);
                    } else if (response == -2){
                        System.out.println("[Client]: Response from registration server: " +
                                "Reached maximum number of registered players.");
                        System.exit(0);
                    } else {
                        id = response;
                        System.out.println("[Client]: Response from registration server: " +
                                "Player registered with id: " + id);

                        // get all other players
                        players = stub.getPlayers();
                        System.out.println("[Client]: I'm player: " + players.get(id).toString());
                        System.out.println("[Client]: Players list:");
                        for(int i=0; i<players.size(); i++){
                            System.out.println(players.get(i).toString());
                        }

                        // get the global game status
                        gameStatus = stub.getGameStatus();
                        System.out.println("[Client]: GameStatus: " + gameStatus.toString());

                        // create a board and a controller
                        PlayerClient playerClient = new PlayerClient();
                        board = new BoardView(gameStatus, id, playerClient);
                        gameController = new GameController(id, players, gameStatus, buffer, board);

                        // each player has a registry and an remote object on his host and port
                        // player port (player host is set before)
                        port = players.get(id).getPort();
                        setupRMIregistryAndServer(gameController);

                        int delay = 0;
                        int period = 15;
                        gameController.startTimeout(delay, period);
                        System.out.println("[Client]: Game started.");
                    }

                } catch (AccessException e) {
                    System.err.println("[Client]: time expired to register.");
                    e.printStackTrace();
                    System.exit(-1);
                } catch (RemoteException e) {
                    System.err.println("[Client]: time expired to register.");
                    e.printStackTrace();
                    System.exit(-1);
                } catch (NotBoundException e) {
                    System.err.println("[Client]: time expired to register.");
                    e.printStackTrace();
                    System.exit(-1);
                }
                /*
            }
        });
        thread.start();
        */
    }

    /**
     * Setup a registry and a remote object for each player
     * */
    public static void setupRMIregistryAndServer(GameController gameController){
        // todo buffer non so se serve o no
        buffer = new LinkedBlockingQueue();
        PlayerServer.setupRMIregistryAndServer(host, port, buffer, gameController);
    }

    /**
     * Called by BoardView class when a move is performed
     * */
    public void broadcastMessageMove(GameStatus gameStatus){
        gameController.broadcastMessage(gameStatus);
    }
}