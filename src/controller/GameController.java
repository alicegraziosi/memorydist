package controller;

import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteMessageServiceInt;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class GameController{


    public int id;
    public ArrayList<Player> players; // array list of player
    public GameStatus gameStatus; // global status of the game
    public BlockingQueue<GameStatus> buffer; // ?

    // timer mossa
    private Timer timer;
    private TimerTask timerTask;

    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer) {
        this.id = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
    }


    public void playGame(){
        /*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            */
        try {

            // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore

            // altrimenti si lascia giocare l'ultimo giocatore

            // Se è il suo turno
            // id parte da 1, l'indice da 0
            if (gameStatus.getPlayersList().get(id-1).isMyTurn()) {
                System.out.println("E' il mio turno (Giocatore " + id + ").");

                // todo setto il giocatore che ha il prossimo turno (
                // (funziona solo con due giocatori)
                // todo è un casino con i .sleep.. ho provato con timer


                // id parte da 1, l'indice da 0
                for (int i = 0; i < players.size(); i++) {
                    // setto tutti a isLeader false
                    players.get(i).setMyTurn(false);
                }

                // id prossimo giocatore
                int index = id+1;
                // se il giocatore corrente è l'ultimo, il prossimo è il primo
                if(index>players.size()){
                    index = 1;
                }

                // setto il turno al prossimo giocatore non in crash
                for (int i = index; i<=players.size(); i++){
                    if(!players.get(i-1).isCrashed()) {
                        players.get(i-1).setMyTurn(true);
                        System.out.println("Il prossimo giocatore è : " + Integer.valueOf(i).toString());
                        break;
                    } else {
                        System.out.println(players.get(i-1).toString());
                    }
                }

                System.out.println("Faccio broadcast di questa informazione.");
                gameStatus.setPlayersList(players);
                gameStatus.setIdSender(id);
                broadcastMessage(gameStatus);


                // mossa
                // notifica mossa
                // aggiorna game status
                // aggiorna board

                // aggiornamento punteggio

                // prova per vedere se un altro processo è in crash
                System.out.println("Faccio broadcast di un messaggio..");
                gameStatus.setIdSender(id);
                broadcastMessage(gameStatus);

                // prova per vedere se un altro processo è in crash
                System.out.println("Faccio un altro broadcast di un messaggio..");
                gameStatus.setIdSender(id);
                broadcastMessage(gameStatus);

                // nel frattempo può andare in crash

                //sleep(10 * 1000);
                //playGame();

            } else {
                // Se non è il suo turno
                for (int i = 0; i < players.size(); i++) {
                    if(players.get(i).isMyTurn()){
                        System.out.println("E' il turno del giocatore " + Integer.valueOf(i+1).toString());
                    }
                }
                System.out.println("(Io sono il giocatore " + id + ").");
                System.out.println("Resto in ascolto di messaggi...");

                // la board è bloccata

                //sleep(20 * 1000);

                // nel frattempo può andare in crash

                // nel frattempo riceve messaggi
                        /*
                        GameStatus receivedMessage = buffer.poll();
                        if(receivedMessage!=null){
                            System.out.println("Processo il messaggio.");
                            gameStatus = receivedMessage;
                        } else {
                            System.out.println("receivedMessage poll null");
                        }*/

                //playGame();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
            }
        });
        t.start();
        */
    }

    public void startTimeout(int delay, int period){
        timerTask  = new TimerTask() {
            @Override
            public void run() {
                System.out.println("******** Nuovo turno ********\n\n");
                playGame();

                //stopTimeout
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, delay * 1000, period * 1000);
    }

    public void stopTimeout(){
        timerTask.cancel();
        timer.cancel();
    }

    // manda un messaggio a tutti gli altri players
    public void broadcastMessage(GameStatus message) {
        for (int i = 0; i < players.size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i+1 != id && !players.get(i).isCrashed()) {
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(players.get(i).getPort());

                    InetAddress host = players.get(i).getHost();
                    int port = players.get(i).getPort();

                    String name = "rmi://" + host + ":" + port + "/messageService";
                    //String name = "messageService";

                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(name);
                    System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i + 1) + ": " + stub.sendMessage(message));
                } catch (RemoteException e) {
                    //e.printStackTrace();
                    System.out.println("Il giocatore con id " + Integer.valueOf(i + 1) + " non ha ricevuto il messaggio, è in crash.");

                    // todo settarlo in crash
                    players.get(i).setCrashed(true);

                    // todo notificare l' informazione a tutti
                    gameStatus.setPlayersList(players);

                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // manda un messaggio a un particolare giocatore per vedere se è attivo
    public void sendMessageToHost(GameStatus message, int idGiocatore) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(players.get(idGiocatore-1).getPort());
            registry = LocateRegistry.getRegistry(players.get(idGiocatore-1).getPort());

            InetAddress host = players.get(idGiocatore-1).getHost();
            int port = players.get(idGiocatore-1).getPort();

            String name = "rmi://" + host + ":" + port + "/messageService";
            //String name = "messageService";

            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(name);
            System.out.println("Risposta dal giocatore con id " + idGiocatore+ ": " + stub.sendMessage(message));
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + idGiocatore + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(idGiocatore).setCrashed(true);

            // todo  notificare l' informazione a tutti
            gameStatus.setPlayersList(players);
            //broadcastMessage(gameStatus);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}