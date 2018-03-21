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


    private int id; // current player id
    private ArrayList<Player> players; // array list of player
    private GameStatus gameStatus; // global status of the game, with info of current player
    private BlockingQueue<GameStatus> buffer; // todo togliere? probabilmente non serve
    private int msgId;

    // timer mossa
    private Timer timer;
    private TimerTask timerTask;

    private int turnNumber;

    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer) {
        this.id = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
        this.turnNumber = 0;
        this.msgId = 0;
    }


    public void playGame(){

        // todo con i thread o senza!? non lo so...
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");

            //se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
            if(gameStatus.getShowingCards().size()<20) {

                // altrimenti si lascia giocare l'ultimo giocatore

                // id parte da 1, l'indice da 0
                if (gameStatus.getPlayersList().get(id - 1).isMyTurn()) {

                    // Se è il suo turnNumber

                    // la board è sbloccata
                    //board.unblock();

                    System.out.println("E' il mio turnNumber (Giocatore " + id + ").");


                    // todo setto il giocatore che ha il prossimo turnNumber (
                    // (funziona solo con due giocatori)
                    // todo è un casino con i .sleep.. ho provato con timer


                    // id parte da 1, l'indice da 0
                    for (int i = 0; i < players.size(); i++) {
                        // setto tutti a isLeader false
                        players.get(i).setMyTurn(false);
                    }

                    // id prossimo giocatore
                    int index = id + 1;
                    // se il giocatore corrente è l'ultimo, il prossimo è il primo
                    if (index > players.size()) {
                        index = 1;
                    }

                    // setto il turnNumber al prossimo giocatore non in crash
                    for (int i = index; i <= players.size(); i++) {
                        if (!players.get(i - 1).isCrashed()) {
                            players.get(i - 1).setMyTurn(true);
                            System.out.println("Il prossimo giocatore è : " + Integer.valueOf(i).toString());
                            break;
                        } else {
                            System.out.println("sarebbe stato il turnNumber di " + players.get(i - 1).getId() + " ma è crashed");
                        }
                    }

                    System.out.println("Faccio broadcast di questa informazione.");
                    gameStatus.setPlayersList(players);
                    gameStatus.setIdSender(id);
                    gameStatus.setId(gameStatus.getId() + 1);
                    broadcastMessage(gameStatus);


                    // mossa
                    // notifica mossa
                    // aggiorna game status
                    // aggiorna board
                    // board.update(gameStatus);

                    // aggiornamento punteggio

                    // prova per vedere se un altro processo è in crash
                    // todo da togliere, è solo una prova
                /*
                System.out.println("Faccio broadcast di un messaggio..");
                gameStatus.setIdSender(id);
                broadcastMessage(gameStatus);

                // prova per vedere se un altro processo è in crash
                // todo da togliere, è solo una prova
                System.out.println("Faccio un altro broadcast di un messaggio..");
                gameStatus.setIdSender(id);
                broadcastMessage(gameStatus);
                */

                    // nel frattempo può andare in crash

                    //sleep(10 * 1000);
                    //playGame();

                } else {
                    // Se non è il suo turnNumber

                    // la board è bloccata
                    //board.block();

                    for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
                        if (gameStatus.getPlayersList().get(i).isMyTurn()) {
                            System.out.println("NON è il mio turnNumber, è il turnNumber del giocatore " + Integer.valueOf(i + 1).toString());
                        }
                    }
                    System.out.println("(Io sono il giocatore " + id + ").");
                    System.out.println("Resto in ascolto di messaggi...");

                    // se non arrivano mess
                    // un giocatore è in crash oppure non ha fatto mosse


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
            } else {
                // all cards are matched
                // current player is the winner
                // id parte da 1, l'indice da 0
                if (gameStatus.getPlayersList().get(id - 1).isMyTurn()) {
                    // todo this is the winner
                    System.out.println("You are the winner!");
                    // broadcast(gamestatus)
                } else {
                    // todo all other playes need to know that the game ended
                    System.out.println("Another player won the game.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

            }
        });
        t.start();

    }

    public void startTimeout(int delay, int period){
        timerTask  = new TimerTask() {
            @Override
            public void run() {

                playGame();
            }
        };

        timer = new Timer();
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

            InetAddress host = players.get(idGiocatore-1).getHost();
            int port = players.get(idGiocatore-1).getPort();

            String name = "rmi://" + host + ":" + port + "/messageService";
            //String name = "messageService";

            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(name);
            System.out.println("Incremento id msg: " + stub);
            System.out.println("Risposta dal giocatore con id " + idGiocatore+ ": " + stub.sendMessage(message));
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + idGiocatore + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(idGiocatore).setCrashed(true);

            // todo notificare l' informazione a tutti
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