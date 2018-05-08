package controller;

import model.gameStatus.GameStatus;
import model.move.Move;
import model.player.PLAYER_STATE;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import utils.CircularArrayList;

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
public class GameController implements DataReceiverListener {

    public int playerId; // current player id
    public CircularArrayList<Player> players; // array list of player
    public GameStatus gameStatus; // global status of the game, with info of current player
    private int turnNumber;
    private BoardView boardView;
    private Future f;

    /**
     * GameController constructor
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

        System.out.println("[GameCtrl] giocatori rimanenti: " + gameStatus.getPlayersList().toString());
        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");
            
            if (gameStatus.getShowingCards().size() < 20){

            	if(gameStatus.getPlayersList().size() > 1) {

                    if (isMyTurn()) { 

                    	System.out.println("[GameController]: It is my turn (Player " + playerId + ").");

                        /**
                         * setto prossimo giocatore e mittente
                         * aggiorno view e sblocco carte
                         * */
                        gameStatus.setNextPlayer();
                        gameStatus.setIdSender(playerId);
                        boardView.resetAndUpdateInfoView(gameStatus, playerId);
                        boardView.unblockCards();

                    } else { // Se non è il suo turno
                    	
                    	int currentPlayerId = gameStatus.getCurrentPlayer().getId();

                    	/**
                         * aggiorno view e blocco carte
                         * */
                        boardView.resetAndUpdateInfoView(gameStatus, currentPlayerId);
                        boardView.blockCards();

                        System.out.println("NOT my turn, it is turn of player " + currentPlayerId);
                        System.out.println("(I'm player " + playerId + ").");
                        System.out.println("I'm listening for messages...");

                        //handleLazyCurrentPlayer(this.gameStatus);
                    
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
     * @desc function that pings a player to check that is alive
     * @param GameStatus $gameStatus, int $playerId
     * */
    public void pingAPlayer(final int playerId, final boolean isCurrentPlayerCrashed) {
        Runnable runnable = new Runnable() {
            public void run() {
                /**
                 * pingo giocatore con id $playerId
                 * */
                if(isCurrentPlayerCrashed)
                    System.out.println("[GameCtrl]: ping to CURRENT player " + playerId);
                else
                    System.out.println("[GameCtrl]: ping to NOT CURRENT player " + playerId);

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

//                    if (response == 1)
//                        System.out.println("[GameCtrl]: Response from player " + i + ": ALIVE");

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
                    	System.out.println("[gameCtrl] rimuovo giocatore con id " + i );
//	                	 players.remove(i); // rimuovo il giocatore crashato dalla lista (in questo modo più semplice la gestione del successivo)
//	                     gameStatus.setPlayersList(players);
                    }
                    
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
        if( gameStatus.isPenalized() != false) {
        	playGame();
        }
        else if( gameStatus.getMove().getCard2() != null ){
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

                System.out.println("XXXXXXX Player " + idPlayer + " crashed XXXXXXX");

                /** setto nel gameStatus giocatore crashato */
                System.out.println("[GameCtrl]: Setto crashato player " + idPlayer);

                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayerState(idPlayer, PLAYER_STATE.CRASH);
                gameStatus.setPlayersList(players);

                System.out.println("XXXXXXX Nuova lista giocatori " + gameStatus.getPlayersList() + " XXXXXXX");

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

                    // todo, va settato o no?
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
                /** player pingato CRASHATO*/

                System.out.println("XXXXXXX Player " + idPlayer + " crashed XXXXXXX");

                /** setto nel gameStatus giocatore crashato */
                System.out.println("[GameCtrl]: Setto crashato player " + idPlayer);

                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayerState(idPlayer, PLAYER_STATE.CRASH);
                gameStatus.setPlayersList(players);

                System.out.println("XXXXXXX Nuova lista giocatori " + gameStatus.getPlayersList() + " XXXXXXX");

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

                    // todo, va settato o no?
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

                        Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
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
                        e.printStackTrace();
                        int currentPlayerId = gameStatus.getCurrentPlayer().getId();
                        int crashedPlayerId = gameStatus.getPlayersList().get(i).getId();

                        /** caso CRASH giocatore a cui si invia messaggio di crash*/
                        System.out.println("caso CRASH giocatore a cui si invia messaggio di crash");
                        System.out.println("Player " + i + " crashed.");

                        /** setto giocatore come crashato */
                        players.get(i).setCrashed(true);
                        gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                        gameStatus.setPlayersList(players);

                        /** controllare se era giocatore corrente o non corrente (successivo o non succ)*/
                        if( crashedPlayerId == currentPlayerId ) {
                            /**
                             * setto nuovo giocatore successivo
                             * e mittente
                             * */
                            gameStatus.setNextPlayer();
                            gameStatus.setIdSender(playerId);

                            /**
                             * gestione aggiornamento gameStatus dopo crash giocatore CORRENTE
                             * broadcast a tutti nuovo game status
                             */
                            broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                        }
                        else {
                            /**
                             * gestione aggiornamento gameStatus dopo crash giocatore NON CORRENTE
                             * il giocatore successivo è già stato settato
                             */
                            if ( crashedPlayerId == gameStatus.getCurrentPlayer().getId()) {
                                /** crash giocatore SUCCESSIVO */
                                /**
                                 * setto nuovo giocatore successivo e mittente
                                 * */
                                gameStatus.setNextPlayer();
                                gameStatus.setIdSender(playerId);
                                /** broadcast a tutti gameStatus */
                                broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                            }
                            else {
                                /** crash giocatore NON SUCCESSIVO */
                                /** non ho bisogno di settare nuovo giocatore successivo, setto mittente */
                                gameStatus.setIdSender(playerId);
                                /** broadcast a tutti gameStatus */
                                broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                            }

                        }
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                        int currentPlayerId = gameStatus.getCurrentPlayer().getId();
                        int crashedPlayerId = gameStatus.getPlayersList().get(i).getId();

                        /** caso CRASH giocatore a cui si invia messaggio di crash*/
                        System.out.println("caso CRASH giocatore a cui si invia messaggio di crash");
                        System.out.println("Player " + i + " crashed.");

                        /** setto giocatore come crashato */
                        players.get(i).setCrashed(true);
                        gameStatus.setPlayerState(i, PLAYER_STATE.CRASH);
                        gameStatus.setPlayersList(players);

                        /** controllare se era giocatore corrente o non corrente (successivo o non succ)*/
                        if( crashedPlayerId == currentPlayerId ) {
                            /**
                             * setto nuovo giocatore successivo
                             * e mittente
                             * */
                            gameStatus.setNextPlayer();
                            gameStatus.setIdSender(playerId);

                            /**
                             * gestione aggiornamento gameStatus dopo crash giocatore CORRENTE
                             * broadcast a tutti nuovo game status
                             */
                            broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                        }
                        else {
                            /**
                             * gestione aggiornamento gameStatus dopo crash giocatore NON CORRENTE
                             * il giocatore successivo è già stato settato
                             */
                            if ( crashedPlayerId == gameStatus.getCurrentPlayer().getId()) {
                                /** crash giocatore SUCCESSIVO */
                                /**
                                 * setto nuovo giocatore successivo e mittente
                                 * */
                                gameStatus.setNextPlayer();
                                gameStatus.setIdSender(playerId);
                                /** broadcast a tutti gameStatus */
                                broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                            }
                            else {
                                /** crash giocatore NON SUCCESSIVO */
                                /** non ho bisogno di settare nuovo giocatore successivo, setto mittente */
                                gameStatus.setIdSender(playerId);
                                /** broadcast a tutti gameStatus */
                                broadcastCrashMessage(gameStatus, players.get(crashedPlayerId), isCurrentPlayerCrashed);
                            }

                        }
                    }
                }
            }

            /** controllo effettivo che tutti abbiano ricevuto il messaggio e quindi ricomincio turno*/
            if( isCurrentPlayerCrashed) {
                int playerResponse = 0;
                System.out.println("***** map: " + responsesCtrl.keySet().toString() +
                        " - " + responsesCtrl.values().toString());
                for (Entry<String, Boolean> entry : responsesCtrl.entrySet())
                {
                    System.out.println("responseCtrl: " + entry.getKey() + " / " + entry.getValue());
                    if( entry.getValue() == true)
                        playerResponse ++;
                }

                /** se tutti mi hanno risposto comincia nuovo turno*/
                // todo serve controllare che tutti abbiano riposto?
                // if( playerResponse == responsesCtrl.size())
                    playGame();
            }
        }

        if(gameStatus.getMove().getCard2() != null){
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
    public void handleLazyCurrentPlayer(final GameStatus gameStatus) {
        /** 1) */
        new java.util.Timer().schedule( 
                new java.util.TimerTask() {
                    @Override
                    public void run() {
          
                    	/** 2)*/
                    	if(gameStatus.getNextPlayer().getId() == playerId) {
                    		gameStatus.setNextPlayer();
                    		System.out.println("SONO IL SUCCESSIVO E IL CORRENTE NON HA GIOCATO");
                        	System.out.println("nuovo corrente: " + gameStatus.getCurrentPlayer());
                        	System.out.println("***** PROCEDURA DI PENALIZZAZIONE *****");
                        	
                    		gameStatus.setPenalized(true);
                    		broadcastMessage(gameStatus);
                    	}
                    }
                }, 
                35000 
        );
    }
    
    /**
     * ?
     * */
    // todo
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
