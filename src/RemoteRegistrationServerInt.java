import model.gameStatus.GameStatus;
import model.player.Player;
import utils.Node;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteRegistrationServerInt extends Remote {
    int registraGiocatore(String giocatore, InetAddress hostAddress, int port) throws RemoteException;
    void stopServizio() throws RemoteException;
    ArrayList<Player> getPlayers() throws RemoteException;
    ArrayList<Node> getNodes() throws RemoteException;
    GameStatus getGameStatus() throws RemoteException;
}
