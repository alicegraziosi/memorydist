import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class ClientGiocatore {

    public static int id;

    // dove sta rmi registry di server registrazione
    public static String host;

    // host e port del giocatore
    public static InetAddress hostAddress;
    public static int port;

    public static ArrayList<Player> players;
    public static ArrayList<Node> nodes;
    public static GameStatus gameStatus;

    public static BlockingQueue<GameStatus> buffer;

    private static Timer timer;
    private static TimerTask timerTask;
    public static void main(String[] args) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    host = (args.length < 1) ? null : args[0];

                    hostAddress = null;

                    try{
                        hostAddress = InetAddress.getLocalHost();
                    } catch (UnknownHostException ex){
                        ex.printStackTrace();
                    }

                    System.out.println("Richiesta servizio di registrazione...");
                    Registry registry = LocateRegistry.getRegistry(host);
                    RemoteRegistrationServerInt stub = (RemoteRegistrationServerInt) registry.lookup("registrazione");

                    // restituisce l'id del giocatore
                    String nomeGiocatore = "default name";
                    id = stub.registraGiocatore(nomeGiocatore, hostAddress, port);
                    if(id<0){  // -1
                        System.out.println("Raggiunto numero massimo di giocatori registrati.");
                    } else {
                        System.out.println("Risposta dal server di registrazione: " +
                                "Giocatore registrato con id " + id);

                        players = stub.getPlayers();
                        nodes = stub.getNodes();
                        gameStatus = stub.getGameStatus();

                        port = nodes.get(id-1).getPort();

                        System.out.println("Il mio nodo è: host: " + hostAddress + ", port: " + port);

                        System.out.println("Numero giocatori: " + players.size());

                        System.out.println("Sono il giocatore:");
                        System.out.println("nome: " + players.get(id-1).getNomeGiocatore() +
                                ", id: " + players.get(id-1).getId() +
                                ", leader: " + players.get(id-1).isLeader() + "\n");

                        System.out.println("Lista dei giocatori:");
                        for(int i=0; i<players.size(); i++){
                            System.out.println("nome: " + players.get(i).getNomeGiocatore() +
                                    ", id: " + players.get(i).getId() +
                                    ", leader: " + players.get(i).isLeader());
                        }

                        // ogni client ha il suo registro rmi sulla propria porta
                        setupRMIregistryAndServer();

                        System.out.println("Inizio del gioco.");

                        startTimeout();

                        //playGame();
                    }

                } catch (AccessException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                } catch (RemoteException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public static void playGame(){
        /*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            */
                System.out.println("**** Nuovo turno ****\n\n");

                try {
                    // viene mostrato il tavolo di gioco

                    // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore

                    // Se è il suo turno
                    // id parte da 1, l'indice da 0
                    if (gameStatus.getListaGiocatori().get(id-1).isLeader()) {
                        System.out.println("E' il mio turno (Giocatore " + id + ").");

                        // todo setto il giocatore che ha il prossimo turno (
                        // (funziona solo con due giocatori)
                        // todo è un casino con i .sleep.. ho provato con timer

                        /*

                        // id parte da 1, l'indice da 0
                        for (int i = 0; i < players.size(); i++) {
                            // setto tutti a isLeader false
                            players.get(0).setLeader(false);
                        }

                        // id prossimo giocatore
                        int index = id+1;
                        if(index>players.size()){
                            index = 1;
                        }

                        // setto il prossimo giocatore a isLeader true
                        if(!players.get(index-1).isCrashed()){
                            players.get(index-1).setLeader(true);
                            System.out.println("Il prossimo giocatore è : " + Integer.valueOf(index).toString());
                        }

                        System.out.println("Faccio broadcast di questa informazione.");
                        gameStatus.setListaGiocatori(players);
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);

                        */


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
                            if(players.get(i).isLeader()){
                                System.out.println("E' il turno del giocatore " + Integer.valueOf(i+1).toString());
                            }
                        }
                        System.out.println("(Io sono il giocatore " + id + ").");
                        System.out.println("Resto in ascolto di messaggi...");

                        // la board è bloccata
                        //timeout
                        //sleep(20 * 1000);

                        // nel frattempo può andare in crash

                        // nel frattempo riceve messaggi
                        // todo come funziona questo buffer.poll??
                        GameStatus receivedMessage = buffer.take();
                        if(receivedMessage!=null){
                            System.out.println("Processo il messaggio.");
                            gameStatus = receivedMessage;
                        } else {
                            System.out.println("receivedMessage poll null");
                        }

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

    public static void startTimeout(){
        timerTask  = new TimerTask() {
            @Override
            public void run() {
                playGame();
                //timerTask.cancel();
                //timer.cancel();
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 5 * 1000, 20 * 1000);
    }

    public static void stopTimeout(){
        timerTask.cancel();
        timer.cancel();
    }


    // manda un messaggio a tutti gli altri players
    public static void broadcastMessage(GameStatus message) {
        for (int i = 0; i < nodes.size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i+1 != id && !players.get(i).isCrashed()) {
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(nodes.get(i).getPort());
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
                    System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i + 1) + ": " + stub.sendMessage(message));
                } catch (RemoteException e) {
                    //e.printStackTrace();
                    System.out.println("Il giocatore con id " + Integer.valueOf(i + 1) + " non ha ricevuto il messaggio, è in crash.");

                    // todo settarlo in crash
                    players.get(i).setCrashed(true);

                    // todo notificare l' informazione a tutti
                    gameStatus.setListaGiocatori(players);
                    broadcastMessage(gameStatus);

                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // manda un messaggio a un particolare giocatore per vedere se è attivo
    public static void sendMessageToHost(GameStatus message, int idGiocatore) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(nodes.get(idGiocatore-1).getPort());
            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
            System.out.println("Risposta dal giocatore con id " + idGiocatore+ ": " + stub.sendMessage(message));
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + idGiocatore + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(idGiocatore).setCrashed(true);

            // todo  notificare l' informazione a tutti
            gameStatus.setListaGiocatori(players);
            broadcastMessage(gameStatus);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static void setupRMIregistryAndServer(){

        buffer = new LinkedBlockingQueue<>();
        ServerGiocatore.setupRMIregistryAndServer(port, buffer);

    }
}



