import model.player.Player;

import java.util.ArrayList;

public class RemoteRegistrationServerImpl implements RemoteRegistrationServerInt {

    private int maxNumeroGiocatori;
    private ArrayList<Player> players;
    private int indexGiocatori;
    private boolean servizioAperto;
    private boolean start;

    public RemoteRegistrationServerImpl() {
        this.maxNumeroGiocatori = 4;
        this.players = new ArrayList();
        this.servizioAperto = true;
        start = false;
    }

    public synchronized int registraGiocatore(String nomeGiocatore) {
        if (indexGiocatori < maxNumeroGiocatori){
            if(servizioAperto) {
                indexGiocatori++;
                Player player = new Player(false, false, nomeGiocatore, indexGiocatori);
                players.add(player);
                System.out.println("Nuovo giocatore: nome: " + nomeGiocatore + ", id: " + Integer.toString(indexGiocatori));
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
}
