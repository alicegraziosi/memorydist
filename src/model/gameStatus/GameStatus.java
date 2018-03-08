package model.gameStatus;

import model.card.Card;
import model.player.Player;
import model.move.Move;
import java.io.Serializable;
import java.util.ArrayList;

// stato globale, inviato come messaggio tra i nodi
public class GameStatus implements Serializable {

    private ArrayList<Player> listaGiocatori;
    private int idSender;
    private Player giocatoreCorrente;
    private Player giocatoreSuccessivo;
    private ArrayList<Card> carteScoperte;
    private ArrayList<Card> carteNonScoperte;
    private Move move;

    public GameStatus(ArrayList<Player> listaGiocatori,
                      int idSender,
                      Player giocatoreCorrente,
                      Player giocatoreSuccessivo,
                      ArrayList<Card> carteScoperte,
                      ArrayList<Card> carteNonScoperte,
                      Move move) {
        this.listaGiocatori = listaGiocatori;
        this.idSender = idSender;
        this.giocatoreCorrente = giocatoreCorrente;
        this.giocatoreSuccessivo = giocatoreSuccessivo;
        this.carteScoperte = carteScoperte;
        this.carteNonScoperte = carteNonScoperte;
        this.move = move;
    }

    public ArrayList<Player> getListaGiocatori() {
        return listaGiocatori;
    }

    public void setListaGiocatori(ArrayList<Player> listaGiocatori) {
        this.listaGiocatori = listaGiocatori;
    }

    public int getIdSender() {
        return idSender;
    }

    public void setIdSender(int idSender) {
        this.idSender = idSender;
    }

    public Player getGiocatoreCorrente() {
        return giocatoreCorrente;
    }

    public void setGiocatoreCorrente(Player giocatoreCorrente) {
        this.giocatoreCorrente = giocatoreCorrente;
    }

    public Player getGiocatoreSuccessivo() {
        return giocatoreSuccessivo;
    }

    public void setGiocatoreSuccessivo(Player giocatoreSuccessivo) {
        this.giocatoreSuccessivo = giocatoreSuccessivo;
    }

    public ArrayList<Card> getCarteScoperte() {
        return carteScoperte;
    }

    public void setCarteScoperte(ArrayList<Card> carteScoperte) {
        this.carteScoperte = carteScoperte;
    }

    public ArrayList<Card> getCarteNonScoperte() {
        return carteNonScoperte;
    }

    public void setCarteNonScoperte(ArrayList<Card> carteNonScoperte) {
        this.carteNonScoperte = carteNonScoperte;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }
}
