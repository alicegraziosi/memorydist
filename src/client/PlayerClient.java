package client;

import controller.GameController;
import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import rmi.RemoteRegistrationServerInt;
import server.PlayerServer;
import utils.Node;
import view.board.Board;

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

    // timer mossa
    private static Timer timer;
    private static TimerTask timerTask;

    public PlayerClient() {

    }

    public static void main(final String[] args) {

        Thread t = new Thread(new Runnable() {
            //@Override
            public void run() {
                try {

                    regServerHost = (args.length < 1) ? "localhost" : args[0];
                    regServerPort = 1099;
                    
                    System.out.println("regServerHost: " + regServerHost);
                    host = null;

                    try{
                        host = InetAddress.getLocalHost();
                    } catch (UnknownHostException ex){
                        ex.printStackTrace();
                    }

                    System.out.println("[Client]: Richiesta servizio di registrazione...");
                    Registry registry = LocateRegistry.getRegistry(regServerHost);
                    //old
                    //RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) registry.lookup("registrazione");

                    //new
                    RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt)
                            registry.lookup("rmi://" + regServerHost+ ":" + regServerPort+ "/registrazione");

                    
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

                        GameController gameController = new GameController(id, players, gameStatus, buffer);

                        // ogni client ha il suo registro rmi sulla propria porta
                        setupRMIregistryAndServer(gameController);

                        // viene mostrato il tavolo di gioco
                        // funziona! ma è commentato per comodità, per ora
                        Board board = new Board(gameStatus);
                        //board .init();

                        // params: int delay, int period
                        int delay = 5;
                        int period = 15;
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

    // spostato in controller
    public static void playGame(){
        /*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            */
                try {
                    // viene mostrato il tavolo di gioco

                    // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore

                    // Se è il suo turno
                    // id parte da 1, l'indice da 0
                    if (gameStatus.getPlayersList().get(id-1).isMyTurn()) {
                        System.out.println("E' il mio turno (Giocatore " + id + ").");

                        // todo setto il giocatore che ha il prossimo turno (
                        // (funziona solo con due giocatori)
                        // todo è un casino con i .sleep.. ho provato con timer


                        // id parte da 1, l'indice da 0
                        for (int i = 0; i < players.size(); i++) {
                            // setto tutti a isLeader false
                            players.get(0).setMyTurn(false);
                        }

                        // id prossimo giocatore
                        int index = id+1;
                        // se il giocatore corrente è l'ultimo, il prossimo è il primo
                        if(index>players.size()){
                            index = 1;
                        }

                        // setto il turno al prossimo giocatore non in crash
                        for (int i = index; i<players.size(); i++){
                            if(!players.get(i-1).isCrashed()) {
                                players.get(i - 1).setMyTurn(true);
                                System.out.println("Il prossimo giocatore è : " + Integer.valueOf(index).toString());
                                break;
                            }
                        }

                        System.out.println("Faccio broadcast di questa informazione.");
                        gameStatus.setPlayersList(players);
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);


                        // mossa
                        // notifica mossa
                        // aggiorna game status

                        // aggiornamento punteggio

                        // prova per vedere se un altro processo è in crash
                        System.out.println("Faccio broadcast di un messaggio..");
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);

                        // prova per vedere se un altro processo è in crash
                        System.out.println("Faccio un altro broadcast di un messaggio..");
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);

                        // nel frattempo può andare in crash

                        //sleep(10 * 1000);
                        //playGame();

                    } else {
                        // Se non è il suo turno
                        for (int i = 0; i < players.size(); i++) {
                            if(players.get(i).isMyTurn()){
                                System.out.println("E' il turno del giocatore " + Integer.valueOf(i+1).toString());
                            }
                        }
                        System.out.println("(Io sono il giocatore " + id + ").");
                        System.out.println("Resto in ascolto di messaggi...");

                        // la board è bloccata

                        //sleep(20 * 1000);

                        // nel frattempo può andare in crash

                        // nel frattempo riceve messaggi
                        /*
                        GameStatus receivedMessage = buffer.poll();
                        if(receivedMessage!=null){
                            System.out.println("Processo il messaggio.");
                            gameStatus = receivedMessage;
                        } else {
                            System.out.println("receivedMessage poll null");
                        }*/

                        //playGame();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        /*
            }
        });
        t.start();
        */
    }

    public static void setupRMIregistryAndServer(GameController gameController){
        buffer = new LinkedBlockingQueue();
        PlayerServer.setupRMIregistryAndServer(port, buffer, gameController);
    }

    // spostato in controller
    public static void startTimeout(int delay, int period){
        timerTask  = new TimerTask() {
            @Override
            public void run() {
                System.out.println("******** Nuovo turno ********\n\n");
                playGame();
                //stopTimeout
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, delay * 1000, period * 1000);
    }

    public static void stopTimeout(){
        timerTask.cancel();
        timer.cancel();
    }

    // spostato in controller
    // manda un messaggio a tutti gli altri players
    public static void broadcastMessage(GameStatus message) {
        for (int i = 0; i < players.size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i+1 != id && !players.get(i).isCrashed()) {
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(players.get(i).getPort());
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
                    System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i + 1) + ": " + stub.sendMessage(message));
                } catch (RemoteException e) {
                    //e.printStackTrace();
                    System.out.println("Il giocatore con id " + Integer.valueOf(i + 1) + " non ha ricevuto il messaggio, è in crash.");

                    // todo settarlo in crash
                    players.get(i).setCrashed(true);

                    // todo notificare l' informazione a tutti

                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // spostato in controller
    // manda un messaggio a un particolare giocatore per vedere se è attivo
    public static void sendMessageToHost(GameStatus message, int idGiocatore) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(players.get(idGiocatore-1).getPort());
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
            System.out.println("Risposta dal giocatore con id " + idGiocatore+ ": " + stub.sendMessage(message));
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + idGiocatore + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(idGiocatore).setCrashed(true);

            // todo  notificare l' informazione a tutti
            gameStatus.setPlayersList(players);
            broadcastMessage(gameStatus);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }
}



