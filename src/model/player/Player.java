package model.player;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{

    private boolean isLeader;
    private boolean isCrashed;
    private int punteggio;
    private String nomeGiocatore;
    private int id;

    public Player() {

    }

    public Player(boolean isLeader, boolean isCrashed, String nomeGiocatore, int id) {
        this.isLeader = isLeader;
        this.isCrashed = isCrashed;
        this.nomeGiocatore = nomeGiocatore;
        this.id = id;
        this.punteggio = 0;
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

    public int getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(int punteggio) {
        this.punteggio = punteggio;
    }

    @Override
    public String toString() {
        return "Player{" +
                "isLeader=" + isLeader +
                ", isCrashed=" + isCrashed +
                ", punteggio=" + punteggio +
                ", nomeGiocatore='" + nomeGiocatore + '\'' +
                ", id=" + id +
                '}';
    }
}
