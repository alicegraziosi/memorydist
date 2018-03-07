package model.gameStatus;

import model.card.Card;
import model.player.Player;
import model.move.Move;
import java.io.Serializable;
import java.util.ArrayList;

// stato globale, inviato come messaggio tra i nodi
public class GameStatus implements Serializable {

    private ArrayList<Player> listaGiocatori;
    private Player giocatoreCorrente;
    private Player giocatoreSuccessivo;
    private ArrayList<Card> carteScoperte;
    private ArrayList<Card> carteNonScoperte;
    private Move move;

    public GameStatus(ArrayList<Player> listaGiocatori,
                      Player giocatoreCorrente,
                      Player giocatoreSuccessivo,
                      ArrayList<Card> carteScoperte,
                      ArrayList<Card> carteNonScoperte,
                      Move move) {
        this.listaGiocatori = listaGiocatori;
        this.giocatoreCorrente = giocatoreCorrente;
        this.giocatoreSuccessivo = giocatoreSuccessivo;
        this.carteScoperte = carteScoperte;
        this.carteNonScoperte = carteNonScoperte;
        this.move = move;
    }
}
