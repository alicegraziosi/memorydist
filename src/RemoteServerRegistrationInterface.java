import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteServerRegistrationInterface extends Remote {
    int registraGiocatore(String giocatore) throws RemoteException;
    void stopServizio() throws RemoteException;
    ArrayList<Player> getPlayers() throws RemoteException;
}
