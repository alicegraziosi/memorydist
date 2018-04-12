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
     * @desc function which manages the single round of the game
     * */
    public void playGame() {

        gameStatus.setMove(null);
        boardView.reset(gameStatus);

        try {
            turnNumber++;
            System.out.println("\n\n******** Turn number " + turnNumber + " ********");

            // todo controllare se è rimasto un solo giocatore
            // todo lasciar giocare da solo l'ultimo giocatore
            // se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
            if (gameStatus.getShowingCards().size() < 20) {
                if (isMyTurn()) { // se è il mio turno
                    System.out.println("[GameController]: It is my turn (Player " + currentId + ").");

                    boardView.unblockCards();
                    boardView.getInfoView().update(gameStatus, currentId);

                    /**
                     * gestione passaggio del turno, setto prossimo giocatore
                     * */
                    gameStatus.setNextPlayer();

                    /**
                    * setto giocatori nel gameStatus e idSender
                    * */
                    gameStatus.setPlayersList(players);
                    gameStatus.setIdSender(currentId);
           
                } else { // Se non è il suo turno
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
                    // pingo giocatore corrente per controllare che sia vivo      
                    // faccio questo solo se sono il successivo 
                    Player nextPlayer = gameStatus.getNextPlayer();
                    if ( nextPlayer != null && nextPlayer.getId() == currentId ) {
                    	System.out.println("[GameCtrl] sono player " + currentId + " e pingo player " + 
                    			gameStatus.getCurrentPlayer().getId());
                    	pingAPlayer(gameStatus, gameStatus.getCurrentPlayer().getId());
                    }
                   
                    // todo controllare se non arrivano mess
                    
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
     * @desc function that pings a player to check that is alive
     * @param GameStatus $gameStatus, int $playerId 
     * */
    public void pingAPlayer(final GameStatus gameStatus,final int playerId) {
    	 Runnable runnable = new Runnable() {
             public void run() {
                 System.out.println("[GameCtrl]: sto pingando giocatore corrente.");
                 // task to run goes here
                 pingAHost(gameStatus, playerId);
                 ;
             }
         };
         
         // ping every ms 
         int ms = 10000;
         ScheduledExecutorService service = Executors
                 .newSingleThreadScheduledExecutor();
         service.scheduleAtFixedRate(runnable, 0, ms, TimeUnit.MILLISECONDS);
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
    public void pingAHost(GameStatus gamestatus, int idPlayer) {

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

//                RemoteMessageServiceInt stub = getAndLookupRegistry(remoteHost, remotePort, 
//                		"messageService");
                Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
    			String location = "rmi://" + remoteHost + ":" + remotePort + "/" + "messageService";
    		    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);
                
                int response = stub.ping();
                if (response == 1)
            		System.out.println("[GameCtrl]: Response from player " + idPlayer + ": ALIVE");

            } catch (RemoteException e) {
                //e.printStackTrace();
            	// PLAYER PINGATO CRASHATO
                System.out.println("Player " + idPlayer + " crashed.");
                
                int currentPlayerId = gameStatus.getCurrentPlayer().getId();
                int playersNumber = gameStatus.getPlayersList().size();
                
                players.get(idPlayer).setCrashed(true);
                gameStatus.setPlayersList(players);
                
                //gestione aggiornamento gameStatus dopo crash giocatore corrente
                for (int i = currentPlayerId; i < playersNumber; i++) {
                	if(i == currentId && !gameStatus.getPlayersList().get(i).isCrashed()) {
                        // setto in crash 
		                gameStatus.setNextPlayer();
		                gameStatus.setIdSender(currentId);
		                broadcastCrashMessage(gameStatus, gameStatus.getCurrentPlayer());
		                break;
                	}else {
                		
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
    public void broadcastCrashMessage(GameStatus gamestatus, Player crashedPlayer) {

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
                    
                    RemoteMessageServiceInt stub = getAndLookupRegistry(remoteHost, remotePort, 
                    		"messageService");
                   
                    System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
                    gamestatus.setId(gamestatus.getId());
                    
                    int response = stub.sendCrashMessage(gamestatus, crashedPlayer);
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

