package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.Player;
import rmi.RemoteMessageServiceImpl;
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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.PlayerClient;
import listener.DataReceiverListener;
import view.board.BoardView;
import view.card.CardView;

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
    private PlayerClient playerClient; 
    private PlayerServer playerServer;
    private int msgId;

    // timer mossa
    private Timer timer;
    private TimerTask timerTask;

    private int turnNumber;

	private BoardView boardView;
	
	private Future f;

    /**
     * GameController constructor
     * */
    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BoardView boardView) {
        this.currentId = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.turnNumber = 0;
        this.msgId = 0;
        this.boardView = boardView;

        // viene inizializzato e mostrato il tavolo di gioco
        boardView.init();
    }

    /**
     * @desc function which manages the single round of the game
     * */
    public void playGame() {

        gameStatus.setMove(null);
//        boardView.reset(gameStatus);
        if( f != null)
        	this.f.cancel(true);

        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");

            // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
            if (gameStatus.getShowingCards().size() < 20){

                if(gameStatus.countPlayersActive() != 1) {

                    if (isMyTurn()) { // se è il mio turno

                        boardView.getInfoView().update(gameStatus, currentId);

                        System.out.println("[GameController]: It is my turn (Player " + currentId + ").");

                        /**
                         * gestione passaggio del turno, setto prossimo giocatore
                         * */
                        gameStatus.setNextPlayer();

                        /**
                         * setto giocatori nel gameStatus e idSender
                         * */
                        if(gameStatus.getPlayersList()==null)
                            gameStatus.setPlayersList(players);

                        gameStatus.setIdSender(currentId);

                        boardView.reset(gameStatus);
                        boardView.unblockCards();

                    } else { // Se non è il suo turno
                        System.out.println("NOT my turn, it is turn of player " + gameStatus.getCurrentPlayer().getId());
                        System.out.println("(I'm player " + currentId + ").");
                        System.out.println("I'm listening for messages...");

                        // la boardView è bloccata
                        boardView.reset(gameStatus);
                        boardView.blockCards();
                        boardView.getInfoView().update(gameStatus, gameStatus.getCurrentPlayer().getId());


                        /**
                         * controllo che giocatore corrente sia vivo
                         * faccio questo solo se sono il successivo
                         * **/
                        Player nextPlayer = gameStatus.getNextPlayer();
                        int currentPlayerId = gameStatus.getCurrentPlayer().getId();

                        if (nextPlayer != null && nextPlayer.getId() == currentId
                                && !nextPlayer.isCrashed()) {
                            System.out.println("[GameCtrl] sono player " + currentId + " e pingo player " +
                                    gameStatus.getCurrentPlayer().getId());
                            pingAPlayer(currentPlayerId, true);
                        }
                    }
                } else {
                    // todo controllare se è rimasto un solo giocatore
                    // todo lasciar giocare da solo l'ultimo giocatore
                    System.out.println("Sei l'ultimo giocatore rimasto in gioco!");
                }
            } else {
                // all cards are matched
                // current player is the winner
                if (isMyTurn()) { // se è il mio turno
                    // todo this is the winner
                    System.out.println("You are the winner!");
                    boardView.showGameWinnerMessage();
                } else {
                    // todo all other players need to know that the game ended
                    System.out.println("Another player won the game.");
                    boardView.showAnotherPlayerIsWinnerMessage(gameStatus.getCurrentPlayer());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @desc function that pings a player to check that is alive
     * @param GameStatus $gameStatus, int $playerId 
     * */
    public void pingAPlayer(final int playerId, final boolean isCurrentPlayerCrashed) {
    	 Runnable runnable = new Runnable() {
             public void run() {
            	 /**
            	  * pingo giocatore con id $playerId
            	  * */
                 System.out.println("[GameCtrl]: ping to player " + playerId +
                		 " isCurrentPlayerCrashed = " + isCurrentPlayerCrashed);
                 // task to run goes here
                 pingAHost(playerId, isCurrentPlayerCrashed);
                 
             }
         };
         
         // ping every ms 
         int ms = 1000;
         ScheduledExecutorService service = Executors
                 .newSingleThreadScheduledExecutor();
         this.f = service.scheduleAtFixedRate(runnable, 0, 10 * ms, TimeUnit.MILLISECONDS);
         //service.shutdown();
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
                    
//                    RemoteMessageServiceInt stub = getAndLookupRegistry(remoteHost, remotePort, 
//                    		"messageService");
//                    
                    Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
                    String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);
                    
                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
                    gamestatus.setId(gamestatus.getId());
                    int response = stub.sendMessage(gamestatus);
                    
                    if (response == 1)
                		System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");

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

        if(gameStatus.getMove().getCard2() != null){
            playGame();
        }
    }

    /**
     * function which ping a det player to check if is alive
     * @param GameStatus $message
     * */
    public void pingAHost(int idPlayer, boolean isCurrentPlayerCrashed) {

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
                
                /** apro registro RMI */
                Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
    			String location = "rmi://" + remoteHost + ":" + remotePort + "/" + "messageService";
    		    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);
    		    
    		    /** faccio il ping */
                int response = stub.ping();
                if (response == 1)
            		System.out.println("[GameCtrl]: Response from player " + idPlayer + ": ALIVE");

            } catch (RemoteException e) { 
            	/** player pingato CRASHATO*/
                
                System.out.println("Player " + idPlayer + " crashed.");
                
                int currentPlayerId = gameStatus.getCurrentPlayer().getId();
                int playersNumber = gameStatus.getPlayersList().size();
                
                /** setto nel gameStatus giocatore crashato */ 
                System.out.println("Setto crashato player " + idPlayer);
                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayersList(players);  
                
                /**
                 * se è crashato il giocatore corrente
                 * (entro qui solo se sono giocatore successivo)
                 * */
                if( isCurrentPlayerCrashed ) { 
                	/**
                	 * setto nuovo giocatore successivo e mittente
                	 * */
                	gameStatus.setNextPlayer();  
	                gameStatus.setIdSender(currentId); 
	                
                	/** 
                	* gestione aggiornamento gameStatus dopo crash giocatore CORRENTE
            		* broadcast a tutti nuovo game status
            		*/
            		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
	            }
                else {
                	/**
            	 	* gestione aggiornamento gameStatus dopo crash giocatore NON CORRENTE
                	* in questo stato il successivo  è già stato settato
                	*/
                	System.out.println("gamestatus crash giocatore non corrente " + gameStatus.toString());
                	if ( idPlayer == gameStatus.getCurrentPlayer().getId()) {
                		/** crash giocatore SUCCESSIVO */
                		/**
                    	 * setto nuovo giocatore successivo e mittente
                    	 * */
                		gameStatus.setNextPlayer(); 
    	                gameStatus.setIdSender(currentId);
    	                /** broadcast a tutti gameStatus */
                		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                	}
                	else { 
                		/** crash giocatore NON SUCCESSIVO */
                		/** non ho bisogno di settare nuovo giocatore successivo, setto mittente */
                		gameStatus.setIdSender(currentId); 
                		/** broadcast a tutti gameStatus */
                		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                    }
                		
                }
                
            } catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     * function which broadcasts a crash message to other player
     * @param GameStatus $message
     * */
    public void broadcastCrashMessage(GameStatus gamestatus, Player crashedPlayer, boolean isCurrentPlayerCrashed) {

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
                    
                    System.out.println("[GameCtrl.broadcastCrashMessage]: invio new game a player " + playerId);
                    // mi sa che nel client non serve ma non ne sono sicura
                    // in lab funziona con la riga seguente commentata (NON MODIFICARE)
                    // System.setProperty("java.rmi.server.hostname", remoteHost);
                    
                    /** apro registro RMI */
                    RemoteMessageServiceInt stub = getAndLookupRegistry(remoteHost, remotePort, 
                    		"messageService");
                    
                    /** setto mittente messaggio */
                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
                    gamestatus.setId(gamestatus.getId());
                    
                    /** invio messaggio di crash */
                    int response = stub.sendCrashMessage(gamestatus, crashedPlayer, isCurrentPlayerCrashed);
                    
                    /** se è crashato il giocatore corrente allora devo ricominciare il turno */
                    if ( isCurrentPlayerCrashed )
                    	if (response == 1)
                    		playGame();
                    
                    if (response == 1)
                		System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");

                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.out.println("Player " + i + " crashed.");

                    // todo settarlo in crash
                    gameStatus.getPlayersList().get(i).setCrashed(true);

                    // todo notificare l' informazione a tutti
                    gameStatus.setPlayersList(players);
                    

                }
            }
        }

        if(gameStatus.getMove().getCard2() != null){
            playGame();
        }
    }

    /**
     * @desc function that connect to a remote Registry
     * @param String $remoteHost, int $remotePort, String $serviceName
     * @return RMSInt stub
     * */
    public RemoteMessageServiceInt getAndLookupRegistry(String remoteHost, int remotePort, String serviceName) {
    	Registry registry;
		try {
			registry = LocateRegistry.getRegistry(remoteHost, remotePort);
			String location = "rmi://" + remoteHost + ":" + remotePort + "/" + serviceName;
		    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);
		    
		    return stub;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;        
    }
    
    /**
     * ?
     * */
    // todo
	private boolean isMyTurn() {
		return this.gameStatus.getCurrentPlayer().getId() == this.currentId;
	}

	public void updateCardsView() {
		boardView.update();
//		ArrayList<CardView> newCardViews = new ArrayList();
//		ArrayList<CardView> newCardViewsMatch = new ArrayList();
//		
//		for(int i = 0; i < gameStatus.getShowingCards().size(); i++)
//			newCardViewsMatch.add(new CardView(gameStatus.getShowingCards().get(i)) );
//		
//		System.out.println("Carte matchate: " + gameStatus.getShowingCards());
//		 
//		for(int i = 0; i < gameStatus.getNotShowingCards().size(); i++)
//			newCardViews.add(new CardView(gameStatus.getNotShowingCards().get(i)) );
//
//		System.out.println("Carte non matchate: " + gameStatus.getNotShowingCards());
//		
//		boardView.setCardViews(newCardViews);
//		boardView.setCardViewsMatch(newCardViewsMatch);
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
    
    public int getTurnNumber() {
 		return turnNumber;
 	}

 	public void setTurnNumber(int turnNumber) {
 		this.turnNumber = turnNumber;
 	}
}

