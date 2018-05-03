package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.PLAYER_STATE;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
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

    public int playerId;
 	public ArrayList<Player> players; // array list of player
    public GameStatus gameStatus; // global status of the game, with info of current player
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
        this.playerId = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.turnNumber = 0;
        this.boardView = boardView;

        // viene inizializzato e mostrato il tavolo di gioco
        boardView.init();
    }

    /**
     * @desc function which manages the single round of the game
     * */
    public void playGame() {

        gameStatus.setMove(null);

        if( f != null)
        	this.f.cancel(true);

        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");

            System.out.println("size" + gameStatus.getShowingCards().size());
            // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
            if (gameStatus.getShowingCards().size() < 20){
            	
            	// boardView.showMessage("Giocatori rimasti: " +  gameStatus.countPlayersActive());
                if(gameStatus.countPlayersActive() != 1) {

                    if (isMyTurn()) { // se è il mio turno

                        System.out.println("[GameController]: It is my turn (Player " + playerId + ").");

                        /**
                         * gestione passaggio del turno, setto prossimo giocatore
                         * */
                        gameStatus.setNextPlayer();

                        /**
                         * setto giocatori nel gameStatus e idSender
                         * */

                        // todo forse non serve settare i giocatori nel gamestatus
                        /*
                        if(gameStatus.getPlayersList()==null)
                            gameStatus.setPlayersList(players);
                        */

                        gameStatus.setIdSender(playerId);

                        // update delle info di gioco
                        boardView.resetAndUpdateInfoView(gameStatus, playerId);

                        // la boardView è sbloccata
                        boardView.unblockCards();

                        /**
                         * il giocatore corrente fa ping ai giocatori non correnti
                         * */
                        for(int i = 0; i < gameStatus.getPlayersList().size(); i++)
                        	if( gameStatus.getPlayersList().get(i).getId() != playerId &&
                        			!gameStatus.getPlayersList().get(i).isCrashed())
                        		pingAPlayer(gameStatus.getPlayersList().get(i).getId(), false);

                    } else { // Se non è il mio turno

                        // giocatore corrente
                        int currentPlayerId = gameStatus.getCurrentPlayer().getId();

                        // update delle info di gioco
                        boardView.resetAndUpdateInfoView(gameStatus, currentPlayerId);

                        // la boardView è bloccata
                        boardView.blockCards();

                        System.out.println("It is NOT my turn, it is turn of player " + currentPlayerId);
                        System.out.println("(I'm player " + playerId + ").");
                        System.out.println("I'm listening for messages...");

                        /**
                         * controllo che giocatore corrente sia vivo
                         * (faccio questo solo se sono il successivo)
                         * **/

                        // todo, decidere se far pingare solo il prossimo giocatore o tutti quanti
                        /*
                        Player nextPlayer = gameStatus.getNextPlayer();
                        if (nextPlayer != null && nextPlayer.getId() == playerId
                                && !nextPlayer.isCrashed()) {
                            pingAPlayer(currentPlayerId, true);
                        }
                        */

                        // tutti pingano il giocatore corrente
                        pingAPlayer(currentPlayerId, true);
                    }
                } else {
                    System.out.println("You are the last player!");
                    boardView.showMessage("You are the last player!");

                    boardView.resetAndUpdateInfoView(gameStatus, playerId);
                    boardView.unblockCards();
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
            	 if(isCurrentPlayerCrashed)
                    System.out.println("[GameCtrl]: ping to CURRENT player " + playerId);
                 else
                     System.out.println("[GameCtrl]: ping to NOT CURRENT player " + playerId);

                 /**
                  * pingo giocatore con id $playerId
                  * */
                 pingAHost(playerId, isCurrentPlayerCrashed);
                 
             }
         };
         
         // ping every ms 
         int ms = 1000;
         ScheduledExecutorService service = Executors
                 .newSingleThreadScheduledExecutor();
         this.f = service.scheduleAtFixedRate(runnable, 0, 15 * ms, TimeUnit.MILLISECONDS);
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
            if(i != playerId && !gameStatus.getPlayersList().get(i).isCrashed()) {
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
                    
                    if (response == 1)
                		System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");

                } catch (RemoteException e) {
                    //e.printStackTrace();
                    System.out.println("[GameCtrl]: Player " + i + " crashed.");

                    /** setto nel gameStatus giocatore crashato */

                    players.get(i).setCrashed(true);
                    gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                    gameStatus.setPlayersList(players);

                    System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                    // todo notificare l' informazione a tutti

                } catch (NotBoundException e) {
                    // todo, è corretto gestire anche quest'altra eccezione
                    // todo come sopra nella  catch (RemoteException e) ???

                    //e.printStackTrace();
                    System.out.println("[GameCtrl]: Player " + i + " crashed.");

                    /** setto nel gameStatus giocatore crashato */

                    players.get(i).setCrashed(true);
                    gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                    gameStatus.setPlayersList(players);

                    System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                    // todo notificare l' informazione a tutti
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

        /** controllo che giocatore da pingare non sia crashato */
        if(!gameStatus.getPlayersList().get(idPlayer).isCrashed()) {
            try {
                String remoteHost = gameStatus.getPlayersList().get(idPlayer).getHost().toString();
                int remotePort = gameStatus.getPlayersList().get(idPlayer).getPort();

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
                
                System.out.println("[GameCtrl]: Player " + idPlayer + " crashed");
                
                /** setto nel gameStatus giocatore crashato */ 
                System.out.println("[GameCtrl]: Setto crashato player " + idPlayer);

                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayerState(idPlayer, PLAYER_STATE.CRASH);
                gameStatus.setPlayersList(players);

                System.out.println("Nuova lista giocatori " + gameStatus.getPlayersList());
                
                /**
                 * se è crashato il giocatore corrente
                 * (entro qui solo se sono giocatore successivo)
                 * */
                if( isCurrentPlayerCrashed ) { 
                	/**
                	 * non ho bisogno di settare nuovo giocatore successivo (già stato settato in playGame
                	 * dal giocatore corrente)
                	 * e mittente
                	 * */
                	gameStatus.setNextPlayer();
	                gameStatus.setIdSender(playerId);
	                
                	/** 
                	* gestione aggiornamento gameStatus dopo crash giocatore CORRENTE
            		* broadcast a tutti nuovo game status
            		*/
            		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
	            }
                else {
                	/**
            	 	* gestione aggiornamento gameStatus dopo crash giocatore NON CORRENTE
                	* il giocatore successivo è già stato settato
                	*/
                	if ( idPlayer == gameStatus.getCurrentPlayer().getId()) {
                		/** crash giocatore SUCCESSIVO */
                		/**
                    	 * setto nuovo giocatore successivo e mittente
                    	 * */
                		gameStatus.setNextPlayer(); 
    	                gameStatus.setIdSender(playerId);
    	                /** broadcast a tutti gameStatus */
                		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                	}
                	else { 
                		/** crash giocatore NON SUCCESSIVO */
                		/** non ho bisogno di settare nuovo giocatore successivo, setto mittente */
                		gameStatus.setIdSender(playerId);
                		/** broadcast a tutti gameStatus */
                		broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                    }
                		
                }
                
            } catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

                /** player pingato CRASHATO*/

                System.out.println("[GameCtrl]: Player " + idPlayer + " crashed");

                /** setto nel gameStatus giocatore crashato */
                System.out.println("[GameCtrl]: Setto crashato player " + idPlayer);

                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayerState(idPlayer, PLAYER_STATE.CRASH);
                gameStatus.setPlayersList(players);

                System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                /**
                 * se è crashato il giocatore corrente
                 * (entro qui solo se sono giocatore successivo)
                 * */
                if( isCurrentPlayerCrashed ) {
                    /**
                     * non ho bisogno di settare nuovo giocatore successivo (già stato settato in playGame
                     * dal giocatore corrente)
                     * e mittente
                     * */
                    gameStatus.setIdSender(playerId);

                    /**
                     * gestione aggiornamento gameStatus dopo crash giocatore CORRENTE
                     * broadcast a tutti nuovo game status
                     */
                    broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                }
                else {
                    /**
                     * gestione aggiornamento gameStatus dopo crash giocatore NON CORRENTE
                     * il giocatore successivo è già stato settato
                     */
                    if ( idPlayer == gameStatus.getCurrentPlayer().getId()) {
                        /** crash giocatore SUCCESSIVO */
                        /**
                         * setto nuovo giocatore successivo e mittente
                         * todo setto nuovo giocatore ?
                         * */
                        //gameStatus.setNextPlayer();
                        gameStatus.setIdSender(playerId);
                        /** broadcast a tutti gameStatus */
                        broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                    }
                    else {
                        /** crash giocatore NON SUCCESSIVO */
                        /** non ho bisogno di settare nuovo giocatore successivo, setto mittente */
                        gameStatus.setIdSender(playerId);
                        /** broadcast a tutti gameStatus */
                        broadcastCrashMessage(gameStatus, players.get(idPlayer), isCurrentPlayerCrashed);
                    }

                }
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
        /** caso unico giocatore rimasto*/
        if( gameStatus.countPlayersActive() == 1 && 
        		gameStatus.getPlayerState(playerId) == PLAYER_STATE.ACTIVE) {
        	playGame();
        }
        else {
        	/** creo struttura di controllo ricezione messaggi da parte di ogni player */
        	Map<String, Boolean> responsesCtrl = new HashMap<>();
        	
        	/** ciclo che si occupa di inviare nuovo gameStatus a tutti i players attivi rimasti */
	        for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
	            // non lo rimanda a se stesso e ai nodi in crash

	            if(i != playerId && gameStatus.getPlayerState(i)!= PLAYER_STATE.CRASH){
	                try {
	                    String remoteHost = gameStatus.getPlayersList().get(i).getHost().toString();
	                    int remotePort = gameStatus.getPlayersList().get(i).getPort();
	                    int playerId = gameStatus.getPlayersList().get(i).getId();
	                    responsesCtrl.put(remoteHost + remotePort, false);
	                    
	                    System.out.println("[GameCtrl.broadcastCrashMessage]: invio new game a player " + playerId);
	                    // mi sa che nel client non serve ma non ne sono sicura
	                    // in lab funziona con la riga seguente commentata (NON MODIFICARE)
	                    // System.setProperty("java.rmi.server.hostname", remoteHost);
	                    
	                    /** apro registro RMI */
                        Registry registry;
                        registry = LocateRegistry.getRegistry(remoteHost, remotePort);
                        String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
                        RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);

	                    /** setto mittente messaggio */
	                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
	                    gamestatus.setId(gamestatus.getId());
	                    
	                    /** invio messaggio di crash */
	                    int response = stub.sendCrashMessage(gamestatus, crashedPlayer, isCurrentPlayerCrashed);
	                    
	                    /** se è crashato il giocatore corrente allora popolo la struttura di Ctrl risposte */
	                    if ( isCurrentPlayerCrashed )
	                    	if (response == 1)
	                    		responsesCtrl.put(remoteHost + remotePort, true);
	                    
	                    if (response == 1)
	                		System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");
	
	                } catch (RemoteException e) {
                        //e.printStackTrace();
                        System.out.println("[GameCtrl]: Player " + i + " crashed.");

                        /** setto nel gameStatus giocatore crashato */

                        players.get(i).setCrashed(true);
                        gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                        gameStatus.setPlayersList(players);

                        System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                        // todo notificare l' informazione a tutti
	                } catch (NotBoundException e) {
                        //e.printStackTrace();
                        System.out.println("[GameCtrl]: Player " + i + " crashed.");

                        /** setto nel gameStatus giocatore crashato */

                        players.get(i).setCrashed(true);
                        gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                        gameStatus.setPlayersList(players);

                        System.out.println("[GameCtrl]: Nuova lista giocatori " + gameStatus.getPlayersList());

                        // todo notificare l' informazione a tutti
                    }
                }
	        }

            // todo serve controllare che tutti abbiano riposto?
	        /** controllo effettivo che tutti abbiano ricevuto il messaggio e quindi ricomincio turno*/

	        if( isCurrentPlayerCrashed) {

	        	int playerResponse = 0;
	        	System.out.println("[GameCtrl]: map: " + responsesCtrl.keySet().toString() +
            		" - " + responsesCtrl.values().toString());
	        	for (Entry<String, Boolean> entry : responsesCtrl.entrySet())
	        	{
	        	    System.out.println("responseCtrl: " + entry.getKey() + " / " + entry.getValue());
	        	    if( entry.getValue() == true)
	        	    	playerResponse ++;
	        	}
	        	
	        	/** se tutti mi hanno risposto comincia nuovo turno*/
	        	//if( playerResponse == responsesCtrl.size())



                playGame();
	        }
        }
        
        if(gameStatus.getMove().getCard2() != null){
            playGame();
        }
    }
    
    /**
     * ?
     * */
    // todo cosa c'è da fare?
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
     * @return int $playerId
     * */
    public int getPlayerId() {
        return playerId;
    }
}

