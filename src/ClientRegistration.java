import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import static java.lang.Integer.compare;
import static java.lang.Thread.sleep;

public class ClientRegistration {

    private RegistrationWindow registrationWindow;

    public ClientRegistration(RegistrationWindow registrationWindow) {
        this.registrationWindow = registrationWindow;
    }



    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];

        String nomeGiocatore = "default name";

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Richiesta servizio di registrazione...");
                    Registry registry = LocateRegistry.getRegistry(host);
                    RemoteServerRegistrationInterface stub = (RemoteServerRegistrationInterface) registry.lookup("registrazione");

                    // restituisce l'id del giocatore
                    int idResponse = stub.registraGiocatore(nomeGiocatore);
                    if(idResponse<0){  // -1
                        System.out.println("Raggiunto numero massimo di giocatori registrati.");
                    } else {
                        System.out.println("Risposta dal server di registrazione: " +
                                "Giocatore registrato con id " + idResponse);

                        ArrayList<Player> players = stub.getPlayers();
                        System.out.println("Numero giocatori: " + players.size());

                        System.out.println("Tu sei il giocatore: ");
                        System.out.println("nome: " + players.get(idResponse-1).getNomeGiocatore() +
                                ", id: " + players.get(idResponse-1).getId() +
                                ", leader: " + players.get(idResponse-1).isLeader() + "\n");

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

                    // Se è il suo turno

                    // Se non è il suo turno

                    sleep(20 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}



