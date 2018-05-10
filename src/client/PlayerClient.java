package client;

import controller.GameController;
import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteRegistrationServerInt;
import server.PlayerServer;
import utils.CircularArrayList;
import view.board.BoardView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * the client used by player
 * */
public class PlayerClient {

    private static int id; // player id
    private static String host; // player host
    private static int port; // player port

    private static CircularArrayList<Player> players; // array list of player
    private static GameStatus gameStatus; // global status of the game

    private static BoardView board;
    private static GameController gameController;

    public PlayerClient() {

    }

    public static void main(final String[] args) {
        try {
            // the registration server host ip is read from serverHost.txt
            BufferedReader br;
            FileReader fr;

            // registration server host ip (contenuto nel file serverHost.txt)
            // default localhost
            String regServerHost = "localhost";

            // the registration server port
            // default registration server port: 1099
            int regServerPort = 1099;

            try {
                fr = new FileReader("serverHost.txt");
                br = new BufferedReader(fr);
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    regServerHost = sCurrentLine;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }

            // in lab funziona con la riga seguente non commentata
            System.setProperty("java.rmi.server.hostname", regServerHost);

            System.setProperty("java.security.policy", "file:./security.policy");

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            System.out.println("[Client]: Request to registration server on " + regServerHost + ":" + regServerPort);

            Registry registry = LocateRegistry.getRegistry(regServerHost, regServerPort);

            String location = "rmi://" + regServerHost + ":" + regServerPort + "/registrazione";
            RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) registry.lookup(location);

            // player host
            host = null;
            try{
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex){
                ex.printStackTrace();
            }

            // registration server returns id of the player
            // it returns -1 if it is reached maximum number of registered players
            int response = stub.registerPlayer("default name", host);

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
                System.out.println("[Client]: Initial GameStatus: " + gameStatus.toString());

                // create a board and a controller
                PlayerClient playerClient = new PlayerClient();
                board = new BoardView(gameStatus, id, playerClient);
                gameController = new GameController(id, players, gameStatus, board);
                board.setGameController(gameController);
                
                // each player has a registry and an remote object on his host and port
                // player port (player host is set before)
                port = players.get(id).getPort();
                setupRMIregistryAndServer(gameController);

                System.out.println("[Client]: Game started.");

                gameController.playGame();
            }

        } catch (AccessException e) {
            System.err.println("[Client]: time expired to register.");
            //e.printStackTrace();
            System.exit(-1);
        } catch (RemoteException e) {
            System.err.println("[Client]: time expired to register.");
            //e.printStackTrace();
            System.exit(-1);
        } catch (NotBoundException e) {
            System.err.println("[Client]: time expired to register.");
            //e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Setup a registry and a remote object for each player
     * */
    public static void setupRMIregistryAndServer(GameController gameController){
        PlayerServer.setupRMIregistryAndServer(host, port, gameController);
    }
}