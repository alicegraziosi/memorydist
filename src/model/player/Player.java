import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{

    private boolean isLeader;
    private boolean isCrashed;
    private String nomeGiocatore;
    private ArrayList<Player> listaGiocatori;

    private int id;

    public Player(boolean isLeader, boolean isCrashed, String nomeGiocatore, int id) {
        this.isLeader = isLeader;
        this.isCrashed = isCrashed;
        this.nomeGiocatore = nomeGiocatore;
        this.id = id;
        this.listaGiocatori = null;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public boolean isCrashed() {
        return isCrashed;
    }

    public void setCrashed(boolean crashed) {
        isCrashed = crashed;
    }

    public String getNomeGiocatore() {
        return nomeGiocatore;
    }

    public void setNomeGiocatore(String nomeGiocatore) {
        this.nomeGiocatore = nomeGiocatore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Player> getListaGiocatori() {
        return listaGiocatori;
    }

    public void setListaGiocatori(ArrayList<Player> listaGiocatori) {
        this.listaGiocatori = listaGiocatori;
    }
}
