import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

public class ClientGiocatore {

    // dove sta rmi registry di server registrazione
    public static String host;

    public static ArrayList<Player> players;
    public static ArrayList<Node> nodes;

    public static int id;

    public static int port;

    public static InetAddress hostAddress;

    public static void main(String[] args) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    host = (args.length < 1) ? null : args[0];

                    hostAddress = null;

                    try{
                        hostAddress = InetAddress.getLocalHost();
                        System.out.println("My host is: " + hostAddress);
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

                        port = nodes.get(id-1).getPort();

                        System.out.println("host: " + hostAddress + ", port: " + port);

                        System.out.println("Numero giocatori: " + players.size());

                        System.out.println("Giocatore:");
                        System.out.println("nome: " + players.get(id-1).getNomeGiocatore() +
                                ", id: " + players.get(id-1).getId() +
                                ", leader: " + players.get(id-1).isLeader() + "\n");

                        System.out.println("Lista dei giocatori:");
                        for(int i=0; i<players.size(); i++){
                            players.get(i).setListaGiocatori(players);
                            System.out.println("nome: " + players.get(i).getNomeGiocatore() +
                                    ", id: " + players.get(i).getId() +
                                    ", leader: " + players.get(i).isLeader() + "\n");
                        }

                        setupRMI();

                        playGame();
                    }

                } catch (AccessException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    //e.printStackTrace();
                } catch (RemoteException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    //e.printStackTrace();
                } catch (NotBoundException e) {
                    System.out.println("Tempo scaduto per registrarsi come giocatore.");
                    //e.printStackTrace();
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
                    System.out.println("Start playGame");

                    // viene mostrato il tavolo di gioco

                    // Se è il suo turno
                    if (players.get(id-1).isLeader()) {
                        // mossa

                        sleep(10 * 1000);

                        System.out.println("provo a fare broadcast di un messaggio");
                        broadcastMessage();
                    } else {
                        // Se non è il suo turno

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

    // manda un messggio a tutti gli altri players
    public static void broadcastMessage() {
        for (int i = 0; i < nodes.size(); i++) {
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(nodes.get(i).getPort());
                RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService" + nodes.get(i).getPort());
                GameStatus message = new GameStatus();
                System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i+1).toString() + ": " + stub.sendMessage(message));
            } catch (RemoteException e) {
                //e.printStackTrace();
                System.out.println("Il giocatore con id " + Integer.valueOf(i+1).toString() + " non ha ricevuto il messaggio, è in crash.");

                // settarlo in crash

            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setupRMI(){
        // ogni client ha il suo registro rmi sulla propria porta
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);

            // istanza dell'implementazione dell'oggetto remoto
            RemoteMessageServiceImpl messageService = new RemoteMessageServiceImpl();
            RemoteMessageServiceInt messageServiceStub = (RemoteMessageServiceInt) UnicastRemoteObject.exportObject(messageService, 0);
            registry.bind("messageService"+port, messageServiceStub);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}



