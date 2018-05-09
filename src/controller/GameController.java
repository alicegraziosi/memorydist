package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.PLAYER_STATE;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import utils.CircularArrayList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class GameController {

    public int playerId; // current player id
    public CircularArrayList<Player> players;
    public GameStatus gameStatus; // global status of the game, with info of current player
    private int turnNumber;
    private BoardView boardView;
    private Future f; 
    private javax.swing.Timer t;

    /**
     * @descr GameController constructor
     * */
    public GameController(int id,
                          CircularArrayList<Player> players,
                          GameStatus gameStatus,
                          BoardView boardView) {
        this.playerId = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.turnNumber = 0;
        this.boardView = boardView;
        boardView.init(); // viene inizializzato e mostrato il tavolo di gioco
    }

    /**
     * @desc function which manages the single round of the game
     * */
    public void playGame() {

        gameStatus.setMove(null);
        if( f != null)
            this.f.cancel(true);

        System.out.println("[GameCtrl] giocatori rimanenti: " + gameStatus.getPlayersList().toString());
        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " * Turn of player " + gameStatus.getCurrentPlayer().getId() + " ********");
            System.out.println("[I am player " + playerId +"]");
            if (gameStatus.getShowingCards().size() < 20){

            	if(gameStatus.getPlayersList().size() > 1) {

                    if (isMyTurn()) { 
                    	
                    	System.out.println("******* My turn ********");
                        /** setto prossimo giocatore e mittente, aggiorno view e sblocco carte */
                        gameStatus.setNextPlayer();
                        gameStatus.setIdSender(playerId);
                        boardView.resetAndUpdateInfoView(gameStatus, playerId);
                        boardView.unblockCards();

                    } else { // Se non è il suo turno

                    	System.out.println("******* Not my turn ********");
                    	int currentPlayerId = gameStatus.getCurrentPlayer().getId();

                    	/** aggiorno view e blocco carte */
                        boardView.resetAndUpdateInfoView(gameStatus, currentPlayerId);
                        boardView.blockCards();

                        System.out.println("I'm listening for messages...");

                        handleLazyCurrentPlayer();
                    
                    }
                } else { // ultimo giocatore rimasto in gioco

                	System.out.println("Sei l'ultimo giocatore rimasto in gioco!");
                    
                	boardView.showMessage("Sei l'ultimo giocatore rimasto in gioco!");
                    boardView.resetAndUpdateInfoView(gameStatus, playerId);
                    boardView.unblockCards();
                }
            } else { // tutte le carte matchate
                
            	if (gameStatus.getWinner().getId() == playerId) {
                    // todo this is the winner
                    System.out.println("You are the winner!");
                    boardView.showGameWinnerMessage("You are the winner!");
                } else {
                    // todo all other players need to know that the game ended
                    System.out.println("The winner is player " + gameStatus.getWinner().getId());
                    boardView.showGameWinnerMessage("The winner is player " + gameStatus.getWinner().getId());
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
        playGame();
    }

    /**
     * function which broadcasts the global game status to the other players
     * @param GameStatus $message
     * @descr
     * GESTIONE CRASH NODO GENERICO
     * a) detentore token invia oggeto GameStatus
     * b) RMI riconosce impossibilità di raggiungere un nodo
     * c) detentore del token aggiorna l'oggetto
     * d) detentore del token rinvia l'oggetto a tutti i nodi attivi
     * */
    public void broadcastMessage(GameStatus gamestatus) {

        System.setProperty("java.security.policy", "file:./security.policy");

        // I download server's stubs so I must set a SecurityManager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        /**a) scorro tutti i giocatori rimasti*/
        for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
        	// non lo rimanda a se stesso e (i nodi in crash sono esclusi dai players)
            if( gameStatus.getPlayersList().get(i).getId() != playerId ) {
                try {

                	String remoteHost = gameStatus.getPlayersList().get(i).getHost().toString();
                    int remotePort = gameStatus.getPlayersList().get(i).getPort();
                    int playerId = gameStatus.getPlayersList().get(i).getId();

                    //***************************************************************
                    // mi sa che nel client non serve ma non ne sono sicura
                    // in lab funziona con la riga seguente commentata (NON MODIFICARE)
                    // System.setProperty("java.rmi.server.hostname", remoteHost);
                    //***************************************************************

                    Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
                    String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);

                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
                    gamestatus.setId(gamestatus.getId());
                    int response = stub.sendMessage(gamestatus);

                } catch (RemoteException  | NotBoundException e2) {
                	/**b)*/
                    //e.printStackTrace();
                    System.out.println("[GameCtrl]: Player " + i + " crashed.");
                    
                    /**c) setto nel gameStatus giocatore crashato 
                     * 	1) se occorre devo settare il nuovo giocatore successivo*/
                    players.get(i).setCrashed(true); //utilizzare ancora??
                    gameStatus.setPlayerState(i, PLAYER_STATE.CRASH); //utilizzare ancora??
                    
                    /**1) Crashato successivo */
                    if( i ==  gameStatus.getCurrentPlayer().getId()) {
                    	System.out.println("crashato successivo");
                    	gameStatus.setNextPlayer();
                    }
                    
                	System.out.println("[gameCtrl] rimuovo giocatore con id " + i );
                    players.remove(i); // rimuovo il giocatore crashato dalla lista (in questo modo più semplice la gestione del successivo)
                    gameStatus.setPlayersList(players);
          
                    System.out.println("[GameCtrl] giocatore successivo: " + gameStatus.getCurrentPlayer().getId());
                    System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                    /**d) reinvio gamestatus a tutti i giocatori */
                    for (int j = 0; j < gameStatus.getPlayersList().size(); j++) {
                        // non lo rimanda a se stesso e ai nodi in crash
                        if(gameStatus.getPlayersList().get(j).getId() != playerId ) {
                        	
                        	String remoteHost = gameStatus.getPlayersList().get(j).getHost().toString();
                            int remotePort = gameStatus.getPlayersList().get(j).getPort();
                            int playerId = gameStatus.getPlayersList().get(j).getId();
                            
                            //***************************************************************
                            // mi sa che nel client non serve ma non ne sono sicura
                            // in lab funziona con la riga seguente commentata (NON MODIFICARE)
                            // System.setProperty("java.rmi.server.hostname", remoteHost);
                            //***************************************************************
                            
                            Registry registry;
							try {
								registry = LocateRegistry.getRegistry(remoteHost, remotePort);
								String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
	                            RemoteMessageServiceInt stub;
								stub = (RemoteMessageServiceInt) registry.lookup(location);
								System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
	                            gamestatus.setId(gamestatus.getId());
	                            int response;
	                            response = stub.sendMessage(gamestatus);
	                            
//	                            if (response == 1)
//	                                System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");
	                            
							} catch (RemoteException | NotBoundException e3) {
								// TODO Auto-generated catch block
								System.out.println("EXCEPTION HERE");
								e2.printStackTrace();
							}
                        }
                     }
                    // todo notificare l' informazione a tutti
                }
            }
        }
        
        System.out.println("nuovo gamestatus: " + gameStatus.toString());
        if( gameStatus.isPenalized()) {
        	gameStatus.setPenalized(false);
        	gameStatus.setMove(null);
        	playGame();
        }
        else if( gameStatus.getMove().getCard2() != null ){
            playGame();
        }
        
    }

    /**
     * 1) inizia timer 30 secondi
     * al termine penalizzo corrente, il token va al successivo
     * 2) il successivo fa broadcast message per informare
     * 	2a) in questo momento se mi accorgo che il vecchio corrente è crashato,
     *	2b) lo rimuovo dai players
     * **/
    public void handleLazyCurrentPlayer() {
        /** 1) */
    	System.out.println("***** START TIMER PENALIZZAZIONE *****");
    	t = new javax.swing.Timer(35000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            	/** 2)*/
            	if(gameStatus.getNextPlayer().getId() == playerId) {
            		System.out.println("***** PROCEDURA DI PENALIZZAZIONE *****");
                    System.out.println("SONO IL SUCCESSIVO E IL CORRENTE NON HA GIOCATO");
                	
            		gameStatus.setNextPlayer();
            		System.out.println("nuovo corrente: " + gameStatus.getCurrentPlayer());
                	
            		gameStatus.setPenalized(true);
            		gameStatus.setMove(null);
            		broadcastMessage(gameStatus);
            		//gameStatus.setPenalized(false);
            		
            	}
                t.stop();
            }
        });

        t.setRepeats(false);
        t.start();
    }
    
    /**
     * @descr checking if it is my turn
     * @return boolean
     * */
    private boolean isMyTurn() {
        return this.gameStatus.getCurrentPlayer().getId() == this.playerId;
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
    public int getPlayerId() {
        return playerId;
    }

    /**
     * @desc setting id of current player
     * @param int $currentId
     * */
    public void setCurrentId(int currentId) {
        this.playerId = currentId;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }
}
