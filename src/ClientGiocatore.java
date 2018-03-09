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
                            players.get(i).setListaGiocatori(players);
                            System.out.println("nome: " + players.get(i).getNomeGiocatore() +
                                    ", id: " + players.get(i).getId() +
                                    ", leader: " + players.get(i).isLeader());
                        }

                        // ogni client ha il suo registro rmi sulla propria porta
                        setupRMIregistryAndServer();

                        playGame();
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Inizio del gioco.");

                    // viene mostrato il tavolo di gioco

                    // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore

                    // Se è il suo turno
                    if (players.get(id-1).isLeader()) {
                        System.out.println("E' il mio turno.");
                        //timeout

                        // mossa
                        // notifica mossa
                        // aggiorna game status

                        // aggiornamento punteggio

                        // prova per vedere se un altro processo è in crash
                        sleep(10 * 1000);
                        System.out.println("Faccio broadcast di un messaggio..");
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);

                        sleep(10 * 1000);
                        System.out.println("Faccio un altro broadcast di un messaggio..");
                        gameStatus.setIdSender(id);
                        broadcastMessage(gameStatus);

                        // nel frattempo può andare in crash

                        // alla fine cambia il turno

                    } else {
                        System.out.println("E' il turno di un altro giocatore.");
                        System.out.println("Resto in ascolto di messaggi...");
                        // Se non è il suo turno

                        // nel frattempo può andare in crash

                        // nel frattempo riceve messaggi
                        // In remoteMessageServiceImpl

                        // la board è bloccata

                        //loop
                        sleep(20 * 1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    // manda un messaggio a tutti gli altri players
    public static void broadcastMessage(GameStatus message) {
        for (int i = 0; i < nodes.size(); i++) {
            if(i+1 != id) { // non lo rimanda a se stesso
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(nodes.get(i).getPort());
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
                    System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i + 1) + ": " + stub.sendMessage(message));
                } catch (RemoteException e) {
                    //e.printStackTrace();
                    System.out.println("Il giocatore con id " + Integer.valueOf(i + 1) + " non ha ricevuto il messaggio, è in crash.");

                    // settarlo in crash

                    // todo  notificare l' informazione a tutti

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

            // todo  notificare l' informazione a tutti

        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static void setupRMIregistryAndServer(){

        ServerGiocatore.setupRMIregistryAndServer(port);

    }
}



