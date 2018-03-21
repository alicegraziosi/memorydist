package client;

import controller.GameController;
import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import rmi.RemoteRegistrationServerInt;
import server.PlayerServer;
import utils.Node;
import view.board.Board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * the client used by player
 * */
public class PlayerClient {

    public static int id;
    public static String regServerHost; // registration server host (localhost o per esempio Gisella: 130.136.4.121)
    public static int regServerPort; // 1099
    public static InetAddress host; // player host
    public static int port; // player port
    public static ArrayList<Player> players; // array list of player
    public static GameStatus gameStatus; // global status of the game
    public static BlockingQueue<GameStatus> buffer; // ?
    //public static Board board;

    // timer mossa
    private static Timer timer;
    private static TimerTask timerTask;

    public PlayerClient() {

    }

    public static void main(final String[] args) {

        //System.setProperty("java.security.policy", "./../../security.policy");

        Thread t = new Thread(new Runnable() {
            //@Override
            public void run() {
                try {
                    // bisogna passare l ip del server di registrazione come argomento
                    regServerHost = (args.length < 1) ? "localhost" : args[0];
                    regServerPort = 1099;

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
                        e.printStackTrace();
                    }

                    System.setProperty("java.rmi.server.hostname", regServerHost);

                    System.setProperty("java.security.policy", "file:./security.policy");

                    host = null;

                    try{
                        host = InetAddress.getLocalHost();
                    } catch (UnknownHostException ex){
                        ex.printStackTrace();
                    }

                    System.out.println("[Client]: Richiesta registrazione a server " + regServerHost);
                    Registry registry = LocateRegistry.getRegistry(regServerHost);
                    //old
                    //RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) registry.lookup("registrazione");

                    //new
                    String name = "rmi://" + regServerHost + ":" + regServerPort + "/registrazione";
                    RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt)
                            registry.lookup(name);

                    
                    String nomeGiocatore = "default name"; // possiamo prender il  nome da argomento da terminale
                    id = stub.registerPlayer(nomeGiocatore, host, port); // restituisce l'id del giocatore
                    if(id<0){  // -1
                        System.out.println("[Client]: Raggiunto numero massimo di giocatori registrati.");
                    } else {
                        System.out.println("[Client]: Risposta dal server di registrazione: " +
                                "Giocatore registrato con id " + id);

                        players = stub.getPlayers();
                        gameStatus = stub.getGameStatus();

                        port = players.get(id-1).getPort();

                        System.out.println("[Client]: Il mio nodo è: host: " + host + ", port: " + port);

                        System.out.println("[Client]: Numero giocatori: " + players.size());

                        System.out.println("[Client]: Sono il giocatore:");
                        System.out.println("nome: " + players.get(id-1).getNickName() +
                                ", id: " + players.get(id-1).getId() +
                                ", isMyTurn: " + players.get(id-1).isMyTurn() + "\n");

                        System.out.println("[Client]: Lista dei giocatori:");
                        for(int i=0; i<players.size(); i++){
                            System.out.println(players.get(i).toString());
                        }

                        System.out.println("[Client]: Inizio del gioco.");

                        // viene mostrato il tavolo di gioco
                        // NON FUNZIONA è commentato per comodità, per ora
                        //board = new Board(gameStatus, id);
                        //board.init();
                        GameController gameController = new GameController(id, players, gameStatus, buffer);

                        // ogni client ha il suo registro rmi sulla propria porta
                        setupRMIregistryAndServer(gameController);

                        // params: int delay, int period
                        int delay = 5;
                        int period = 25;
                        gameController.startTimeout(delay, period);

                        //playGame();
                    }

                } catch (AccessException e) {
                    System.out.println("[Client]: Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                } catch (RemoteException e) {
                    System.out.println("[Client]: Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    System.out.println("[Client]: Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public static void setupRMIregistryAndServer(GameController gameController){
        buffer = new LinkedBlockingQueue();
        PlayerServer.setupRMIregistryAndServer(host, port, buffer, gameController);
    }

    /*
    public static void setupBoard(){
        PlayerServer.setupBoard();
    }
    */
}



