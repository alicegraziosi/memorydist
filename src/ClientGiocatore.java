import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

public class ClientGiocatore {

    // dove sta rmi registry
    public static String host;

    public static ArrayList<Player> players;

    public static int id;

    public static void main(String[] args) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    host = (args.length < 1) ? null : args[0];

                    InetAddress hostAddress = null;
                    try{
                        hostAddress = InetAddress.getLocalHost();
                        System.out.println("My host is: " + hostAddress);
                    } catch (UnknownHostException ex){
                        ex.printStackTrace();
                    }

                    // Viene settata una porta a cui connettersi
                    Random random = new Random();
                    int port = random.nextInt(100)+2001;
                    System.out.println("host: " + hostAddress + ", port: " + port);

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
                        //sleep(20 * 1000);
                        playGame();
                    }

                } catch (Exception e) {
                    System.err.println("Client registrazione exception: " + e.toString());
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
                    System.out.println("Start playGame");

                    // viene mostrato il tavolo di gioco

                    // Se è il suo turno
                    if (players.get(id-1).isLeader()) {
                        // mossa

                        // messaggio
                        GameStatus message = new GameStatus();
                        Registry registry = LocateRegistry.getRegistry(host);
                        RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup("messageService");
                        System.out.println("sendMessage");
                        System.out.println("risposta: " + stub.sendMessage(message));
                    } else {
                        // Se non è il suo turno

                        //loop
                    }

                    sleep(20 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}



