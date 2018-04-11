package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import server.PlayerServer;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.PlayerClient;
import listener.DataReceiverListener;
import view.board.BoardView;

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

public class GameController implements DataReceiverListener {

    public int currentId; // current player id
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

    private BoardView boardView;

    /**
     * GameController constructor
     * */
    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer,
                          BoardView boardView) {
        this.currentId = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
        this.turnNumber = 0;
        this.msgId = 0;
        this.boardView = boardView;

        // viene inizializzato e mostrato il tavolo di gioco
        boardView.init();
    }

    /**
     * function which manages the single round of the game
     * */
    public void playGame() {

        gameStatus.setMove(null);
        boardView.reset(gameStatus);

        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");

            //se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
            if (gameStatus.getShowingCards().size() < 20) {
                // altrimenti si lascia giocare l'ultimo giocatore

                // 0) se ci sono ancora carte da scoprire e se è rimasto più di un giocatore, altrimenti si lascia giocare l'ultimo giocatore

                if (isMyTurn()) {
                    System.out.println("[GameController]: It is my turn (Player " + currentId + ").");

                    boardView.unblockCards();
                    boardView.getInfoView().update(gameStatus, currentId);

                    /** E' il turno del giocatore, cose da fare:
                     1) accertarsi che il giocatore corrente abbia un $gameStatus aggiornato
                     2) giocatore gioca la prima carta
                     a) gioca la carta
                     b) aggiorna $gameStatus facendo BROADCAST
                     3) giocatore gioca la seconda carta
                     a) gioca la carta con controllo matching e tutto il resto
                     b) aggiorna $gameStatus facendo BROADCAST
                     4) gestire il "passaggio" del turno
                     5) aggiorna $gameStatus facendo BROADCAST
                     */


                    /**
                     * 4) gestione passaggio del turno
                     * */
                    gameStatus.setNextPlayer();

                    /**
                    *5) aggiorna $gameStatus facendo BROADCAST
                    * */
                    //System.out.println("Broadcast a message containing this info.");
                    gameStatus.setPlayersList(players);
                    gameStatus.setIdSender(currentId);
                    //broadcastMessage(gameStatus);


                    // mossa
                    // notifica mossa
                    // aggiorna game status
                    // aggiorna boardView
                    // aggiornamento punteggio

                    // nel frattempo può andare in crash


                } else {
                    // Se non è il suo turno
                    System.out.println("NOT my turn, it is turn of player " + gameStatus.getCurrentPlayer().getId());
                    System.out.println("(I'm player " + currentId + ").");
                    System.out.println("I'm listening for messages...");

                    for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
                        if (gameStatus.getCurrentPlayer().getId() != currentId) {
                            // la boardView è bloccata
                            boardView.blockCards();
                            boardView.getInfoView().update(gameStatus, i);
                            break;
                        }
                    }

                    // todo controllare se non arrivano mess
                    // todo pingare il giocatore corrente
                    // un giocatore è in crash oppure non ha fatto mosse

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

                }
            } else {
                // all cards are matched
                // current player is the winner
                if (gameStatus.getPlayersList().get(currentId).isMyTurn()) {
                    // todo this is the winner
                    System.out.println("You are the winner!");
                } else {
                    // todo all other players need to know that the game ended
                    System.out.println("Another player won the game.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * function which manages the timers of the round
     * @param int $delay, int $period
     * */
    public void startTimeout(int delay, int period){

        /*
        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println();
                // task to run goes here
                playGame();
            }
        };

        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS);
        */

        playGame();
    }

    /**
     * function which broadcasts the global game status to the other players
     * @param GameStatus $message
     * */
    public void broadcastMessage(GameStatus gamestatus) {

        System.setProperty("java.security.policy", "file:./security.policy");

        // I download server's stubs so I must set a SecurityManager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i != currentId && !gameStatus.getPlayersList().get(i).isCrashed()) {
                try {
                    String remoteHost = gameStatus.getPlayersList().get(i).getHost().toString();
                    int remotePort = gameStatus.getPlayersList().get(i).getPort();
                    int playerId = gameStatus.getPlayersList().get(i).getId();
                    
                    // mi sa che nel client non serve ma non ne sono sicura
                    // in lab funziona con la riga seguente commentata (NON MODIFICARE)
                    // System.setProperty("java.rmi.server.hostname", remoteHost);

                    Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
                    String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);
                    
                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
                    gamestatus.setId(gamestatus.getId());
                    int response = stub.sendMessage(gamestatus);

                    System.out.println("[GameCtrl]: Response from player " + i + ": " + response);

                    if(response == 2){

                        playGame();
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.out.println("Player " + i + " crashed.");

                    // todo settarlo in crash
                    gameStatus.getPlayersList().get(i).setCrashed(true);

                    // todo notificare l' informazione a tutti
                    gameStatus.setPlayersList(players);

                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * function which broadcasts the global game status to the other players
     * @param GameStatus $message
     * */
    public void sendMessageToHost(GameStatus gamestatus, int idPlayer) {

        System.setProperty("java.security.policy", "file:./security.policy");

        // I download server's stubs so I must set a SecurityManager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        if(!players.get(idPlayer).isCrashed()) {
            try {
                String remoteHost = players.get(idPlayer).getHost().toString();
                int remotePort = players.get(idPlayer).getPort();

                // mi sa che nel client non serve ma non ne sono sicura
                System.setProperty("java.rmi.server.hostname", remoteHost);

                Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
                String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
                RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);

                int response = stub.sendMessage(gamestatus);
                System.out.println("Response from player " + idPlayer + ": " + response);

            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Player " + idPlayer + " crashed.");

                // todo settarlo in crash
                players.get(idPlayer).setCrashed(true);

                // todo notificare l' informazione a tutti
                gameStatus.setPlayersList(players);

            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?
     * */
    // todo
	private boolean isMyTurn() {
		return this.gameStatus.getCurrentPlayer().getId() == this.currentId;
	}


    /**
     * @desc called by remoteMessageServiceImpl when a player receives a messages
     * containing a new move from the current player
     * @param int $move
     * */
    public void updateBoardAfterMove(Move move){
        boardView.updateBoardAfterMove(move);
    }

    /**
     * ?
     * */
    @Override
	public void setupRemoteClient(GameStatus gameStatus) throws RemoteException, NotBoundException {
		//this.playerClient = new PlayerClient(game, id);
		//setGame(game);
	}

    /**
     * ?
     * */
	@Override
	public void setGame(GameStatus gameStatus) throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
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

    /**
     * @desc getting id of current player
     * @return int $currentId
     * */
    public int getCurrentId() {
        return currentId;
    }

    /**
     * @desc setting id of current player
     * @param int $currentId
     * */
    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }
}

