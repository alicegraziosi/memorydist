package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import client.PlayerClient;
import server.PlayerServer;
import view.board.Board;
import view.board.Info;

/**
 * Controller of the game, contains the following attributes
 * 1) current player id
 * 2) players list
 * 3) game status (global)
 * 4) player Client
 * 5) player Server
 * 6) timer
 * 7) timer task
 * */

public class GameController{


    public int id; // current player id
    public ArrayList<Player> players; // array list of player
    public GameStatus gameStatus; // global status of the game, with info of current player
    public BlockingQueue<GameStatus> buffer; // ?
    private PlayerClient playerClient; 
    private PlayerServer playerServer;
    private int msgId;

    // timer mossa
    private Timer timer;
    private TimerTask timerTask;

    private int turnNumber;

    private Board board;
    /**
     * GameController constructor
     * */
    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer,
                          Board board) {
        this.id = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
        this.turnNumber = 0;
        this.msgId = 0;
        this.board = board;
        board.init();
    }

    /**
     * function which manages the single round of the game
     * */
    public void playGame(){

        // todo con i thread o senza!? non lo so...
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                gameStatus.setMove(null);
                board.reset(gameStatus);

                try {
                    turnNumber++;
                    System.out.println("\n\n******** Turn number " + turnNumber + " ********");

                    //se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
                    if (gameStatus.getShowingCards().size() < 20) {
                        // altrimenti si lascia giocare l'ultimo giocatore

                            // 0) se ci sono ancora carte da scoprire e se è rimasto più di un giocatore, altrimenti si lascia giocare l'ultimo giocatore

                            if (gameStatus.getPlayersList().get(id).isMyTurn()) {
                                System.out.println("[GameController]: E' il mio turno (Giocatore " + id + ").");
                                //System.out.println("[GameController[: gameStatus corrente: " + gameStatus.toString());

                                board.unblockCards();
                                board.getInfo().update(gameStatus, id);

                                /** E' il turno del giocatore, cose da fare:
                                 1) accertarsi che il giocatore corrente abbia un $gameStatus aggiornato
                                 2) giocatore gioca la prima carta
                                 a) gioca la carta
                                 b) aggiorna $gameStatus facendo BROADCAST
                                 3) giocatore gioca la seconda carta
                                 a) gioca la carta con controllo matching e tutto il resto
                                 b) aggiorna $gameStatus facendo BROADCAST
                                 4) gestire il "passaggio" del turno
                                 */


                                // todo setto il giocatore che ha il prossimo turno (
                                // (funziona solo con due giocatori)
                                // todo è un casino con i .sleep.. ho provato con timer

                                // controllo variabile setMyTurn di tutti i giocatori
                                //                for (int i = 0; i < players.size(); i++) {
                                //                	System.out.println("[GameController: checking the players turns...");
                                //                	System.out.println("[GameController]: player " + players.get(i).getId() + " turn :" + Boolean.toString(players.get(i).getMyTurn()) );
                                //
                                //                }

                                // PER IL MOMENTO COMMENTATO CODICE PER SETTARE A FALSE IL TURNO DI TUTTI GLI ALTRI
                                // id parte da 1, l'indice da 0
                                //                for (int i = 0; i < players.size(); i++) {
                                //                    // setto tutti a myTurn false
                                //                    players.get(i).setMyTurn(false);
                                //                }

                                /**
                                 * 4) gestione passaggio del turno
                                 * */
                                //gameStatus.setNextTurn(id) da impl

                                //
                                for (int i = 0; i < players.size(); i++) {
                                    // setto tutti a myTurn false
                                    players.get(i).setMyTurn(false);
                                }

                                int index = id + 1;
                                // se il giocatore corrente è l'ultimo, il prossimo è il primo
                                if (index == players.size()) {
                                    index = 0;
                                }

                                // setto il turnNumber al prossimo giocatore non in crash
                                for (int i = index; i<players.size(); i++) {
                                    if (!players.get(i).isCrashed()) {
                                        players.get(i).setMyTurn(true);
                                        System.out.println("Il prossimo giocatore è : " + Integer.valueOf(i).toString());
                                        break;
                                    } else {
                                        System.out.println("sarebbe stato il turnNumber di " + players.get(i).getId() + " ma è crashed");
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

                                for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
                                    if (gameStatus.getPlayersList().get(i).isMyTurn()) {
                                        System.out.println("NON è il mio turno, è il turno del giocatore " + Integer.valueOf(i).toString());
                                        // la board è bloccata
                                        board.blockCards();
                                        board.getInfo().update(gameStatus, i);
                                        break;
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
                            if (gameStatus.getPlayersList().get(id).isMyTurn()) {
                                // todo this is the winner
                                System.out.println("You are the winner!");
                                // broadcast(gamestatus)
                            } else {
                                // todo all other playes need to know that the game ended
                                System.out.println("Another player won the game.");
                            }
                        }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        t.start();

    }
    
    /**
     * function which manages the timers of the round
     * @param int $delay, int $period
     * */
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

    /**
     * function which broadcast the global game status to the other players
     * @param GameStatus $message
     * */
    public void broadcastMessage(GameStatus gamestatus) {

        System.setProperty("java.security.policy", "file:./security.policy");

        // I download server's stubs ==> must set a SecurityManager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        for (int i = 0; i < players.size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i != id && !players.get(i).isCrashed()) {
                try {
                    String remoteHost = players.get(i).getHost().toString();
                    int remotePort = players.get(i).getPort();

                    // mi sa che nel client non serve
                    System.setProperty("java.rmi.server.hostname", remoteHost);

                    Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);

                    String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";

                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);

                    int response = stub.sendMessage(gamestatus);
                    System.out.println("Risposta dal giocatore con id " + Integer.valueOf(i) + ": " + response);

                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.out.println("Il giocatore con id " + Integer.valueOf(i) + " non ha ricevuto il messaggio, è in crash.");

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
    
    /**
     * function which checks if a det player is active sending to him a message
     * @param GameStatus $message ?, int $playerId
     * */
    public void sendMessageToHost(GameStatus gamestatus, int playerId) {
        try {

            String remoteHost = players.get(playerId).getHost().toString();
            int remotePort = players.get(playerId).getPort();

            String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";

            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) Naming.lookup(location);

            int response = stub.sendMessage(gamestatus);
            System.out.println("Risposta dal giocatore con id " + Integer.valueOf(playerId) + ": " + response);

        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + playerId + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(playerId).setCrashed(true);

            // todo notificare l' informazione a tutti
            gameStatus.setPlayersList(players);
            //broadcastMessage(gameStatus);

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
    
    
    /**
     * getting the game status
     * @return GameStatus $gameStatus
     * */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * setting the game status
     * @param GameStatus $gameStatus
     * */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void updateBoardAfterMove(Move move){
        board.updateBoardAfterMove(move);
    }
}