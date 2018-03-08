import model.card.Card;
import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.util.ArrayList;

public class RemoteRegistrationServerImpl implements RemoteRegistrationServerInt {

    private int maxNumeroGiocatori;
    private ArrayList<Player> players;
    private ArrayList<Node> nodes;
    private int indexGiocatori;
    private boolean servizioAperto;
    private boolean start;

    public RemoteRegistrationServerImpl() {
        this.maxNumeroGiocatori = 4;
        this.players = new ArrayList();
        this.nodes = new ArrayList();
        this.servizioAperto = true;
        start = false;
    }

    public synchronized int registraGiocatore(String nomeGiocatore, InetAddress hostAddress, int port) {
        if (indexGiocatori < maxNumeroGiocatori){
            if(servizioAperto) {
                // id del giocatore
                indexGiocatori++;
                System.out.println("Nuovo giocatore: nome: " + nomeGiocatore + ", id: " + Integer.toString(indexGiocatori));

                Player player = new Player(false, false, nomeGiocatore, indexGiocatori);
                players.add(player);

                port = 2000 + indexGiocatori;
                Node node = new Node(hostAddress, port, indexGiocatori);
                nodes.add(node);

                return indexGiocatori;
            } else {
                System.out.println("Tempo scaduto per registrarsi come giocatore.\n");
                return -1;
            }
        } else {
            System.out.println("Raggiunto numero massimo di giocatori registrati.\n");
            return -1;
        }
    }

    public synchronized void stopServizio(){
        servizioAperto = false;
        System.out.println("Tempo scaduto per registrarsi come giocatore.");

        // il leader è il giocatore che si è registrato per primo, che ha id = 1, con indice 0 nell'arraylist
        players.get(0).setLeader(true);

        System.out.println("Lista dei giocatori:");
        for(int i=0; i<players.size(); i++){
            players.get(i).setListaGiocatori(players);
            System.out.println("nome: " + players.get(i).getNomeGiocatore() +
                    ", id: " + players.get(i).getId() +
                    ", leader: " + players.get(i).isLeader() + "\n");
        }

        // todo creare l'anello!!!!


        System.out.println("Inizio del gioco.");
        start = true;
        notifyAll();
    }

    public synchronized ArrayList<Player> getPlayers() {
        if (!start)
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        return players;
    }

    public synchronized ArrayList<Node> getNodes() {
        if (!start)
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        return nodes;
    }

    public synchronized GameStatus getGameStatus() {
        // gamestatus iniziale
        int numCarte = 20;
        ArrayList<Card> carteScoperte = new ArrayList<Card>(); // 0
        ArrayList<Card> carteNonScoperte = new ArrayList<Card>(); // tutte
        // todo carte
        for(int i=0; i<20; i++){
            Card card = new Card(i, i);
            carteNonScoperte.add(card);
        }
        // idSender -1 significa che il sender è il servizio di registrazione
        GameStatus gameStatus = new GameStatus(
                players, -1, null,
                null, carteScoperte, carteNonScoperte, null
        );

        if (!start)
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        return gameStatus;
    }
}
