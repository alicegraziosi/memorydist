package model.gameStatus;

import model.player.Player;
import model.move.Move;
import java.io.Serializable;
import java.util.ArrayList;

// stato globale, inviato come messaggio tra i nodi
public class GameStatus implements Serializable {

    private ArrayList<Player> listaGiocatori;
    private Player giocatoreCorrente;
    private Player giocatoreSuccessivo;
    private ArrayList carteScoperte;
    private ArrayList carteNonScoperte;
    private Move move;

    public GameStatus() {
    }
}
