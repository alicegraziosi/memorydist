import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

public class RegistrationServiceServer {
    public static void main(String[] args) {
        int timeout;
        if (args.length == 0) {
            timeout = 60; // timeout di default in secondi
        } else {
            timeout = Integer.parseInt(args[0]);
        }

        try {
            // istanza dell'implementazione dell'oggetto remoto
            RemoteServerRegistrationImpl registrazione = new RemoteServerRegistrationImpl();

            RemoteServerRegistrationInterface stub = (RemoteServerRegistrationInterface) UnicastRemoteObject.exportObject(registrazione, 0);

            //Registry registry = LocateRegistry.getRegistry();

            // per non dover tutte le volte fare kill del processo rmiregistry
            // https://stackoverflow.com/questions/8386001/how-to-close-rmiregistry-running-on-particular-port
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (ExportException ex) {
                registry = LocateRegistry.getRegistry(1099);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }

            registry.bind("registrazione", stub);

            Thread serverThread = new Thread() {
                public void run() {
                try {
                    System.out.println("Servizio di registrazione in attesa di giocatori...");
                    sleep(timeout * 1000);
                    stub.stopServizio();
                    Naming.unbind("registrazione");
                    System.out.println("Servizio di registrazione chiuso.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                }
            };
            serverThread.start();



        } catch (Exception e) {
            System.err.println("Servizio di registrazione exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
