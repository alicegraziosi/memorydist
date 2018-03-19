package controller;

import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import java.net.InetAddress;
import server.PlayerServer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import client.PlayerClient;

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
    private Timer timer;     // timer mossa
    private TimerTask timerTask;

    
    /**
     * GameController constructor
     * */
    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer) {
        this.id = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
    }

    
    /**
     * function which manages the single round of the game
     * */
    public void playGame(){
        /*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            */
        try {
        	
            // 0) se ci sono ancora carte da scoprire e se è rimasto più di un giocatore, altrimenti si lascia giocare l'ultimo giocatore

            // Se è il suo turno, id parte da 1, l'indice da 0
            if (gameStatus.getPlayersList().get(id).isMyTurn()) {
                System.out.println("[GameController]: E' il mio turno (Giocatore " + id + ").");
                //System.out.println("[GameController[: gameStatus corrente: " + gameStatus.toString());
                
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
    
    /**
     * function which manages the timers of the round
     * @param int $delay, int $period
     * */
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

    /**
     * function which stops time
     * */
    public void stopTimeout(){
        timerTask.cancel();
        timer.cancel();
    }

    /**
     * function which broadcast the global game status to the other players
     * @param GameStatus $message
     * */
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
    
    /**
     * function which checks if a det player is active sending to him a message
     * @param GameStatus $message ?, int $playerId
     * */
    public void sendMessageToHost(GameStatus message, int playerId) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(players.get(playerId).getPort());
            registry = LocateRegistry.getRegistry(players.get(playerId).getPort());

            InetAddress host = players.get(playerId).getHost();
            int port = players.get(playerId).getPort();

            String name = "rmi://" + host + ":" + port + "/messageService";
            //String name = "messageService";

            RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(name);
            System.out.println("Risposta dal giocatore con id " + playerId+ ": " + stub.sendMessage(message));
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("Il giocatore con id " + playerId + " non ha ricevuto il messaggio, è in crash.");

            // settarlo in crash
            players.get(playerId).setCrashed(true);

            // todo  notificare l' informazione a tutti
            gameStatus.setPlayersList(players);
            //broadcastMessage(gameStatus);

        } catch (NotBoundException e) {
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
}