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
import view.board.BoardView;

/**
 * Controller of the game
 * */
public class GameController {

    public int playerId; // current player id
   	public CircularArrayList<Player> players;
    public GameStatus gameStatus; // global status of the game, with info of current player
    private int turnNumber;
    private BoardView boardView;
    private javax.swing.Timer timer;

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

        if(timer!=null)
            timer.stop();

        gameStatus.setPenalized(false);
        gameStatus.setMove(null);
        
        resetAndUpdatePlayerScores(gameStatus);

        System.out.println("[GameCtrl] giocatori rimanenti: " + gameStatus.getPlayersList().toString());
        turnNumber++;
        System.out.println("\n\n******** Turn number " + turnNumber + " * Turn of player " + gameStatus.getCurrentPlayer().getId() + " ********");
        System.out.println("[I am player " + playerId +"]");

        // woz?
        if (gameStatus.getShowingCards().size() < 20){

            if(gameStatus.getPlayersList().size() > 1) {

                // current player
                if (isMyTurn()) {

                    System.out.println("[GameCtrl.playGame] ******* My turn ********");
                    /** setto prossimo giocatore e mittente, aggiorno view e sblocco carte */
                    gameStatus.setNextPlayer();
                    gameStatus.setIdSender(playerId);
                    resetAndUpdateInfoView(gameStatus, playerId);
                    boardView.unblockCards();

                } else { // all other player

                    System.out.println("[GameCtrl.playGame] ******* Not my turn ********");
                    int currentPlayerId = gameStatus.getCurrentPlayer().getId();

                    /** aggiorno view e blocco carte */
                    resetAndUpdateInfoView(gameStatus, currentPlayerId);
                    boardView.blockCards();

                    System.out.println("[GameCtrl.playGame] I'm listening for messages...");

                    handleLazyCurrentPlayer();
                }
            } else { // ultimo giocatore rimasto

                System.out.println("[GameCtrl.playGame] You are the last player in the game.");

                resetAndUpdateInfoView(gameStatus, playerId);
                boardView.unblockCards();
            }
        } else { // tutte le carte matchate, c'è un vincitore, quello con lo score più alto

            if (gameStatus.getWinner().getId() == playerId) {
            	
            	resetAndUpdateInfoView(gameStatus, playerId);
                
                // this is the winner
                System.out.println("[GameCtrl.playGame] You are the winner!" +
                        "\n Score : " + gameStatus.getWinner().getScore());
                boardView.showGameWinnerMessage("You are the winner! " +
                        "\n Score : " + gameStatus.getWinner().getScore());
            } else {
            	
            	resetAndUpdateInfoView(gameStatus, playerId);
                   
                // all other players need to know that the game ended
                System.out.println("[GameCtrl.playGame] The winner is player " + gameStatus.getWinner().getId() +
                        "\n Score : " + gameStatus.getWinner().getScore());
                boardView.showGameWinnerMessage("The winner is player " + gameStatus.getWinner().getId() +
                        "\n Score : " + gameStatus.getWinner().getScore());
            }
        }
    }

    /**
     * function which broadcasts the global game status from the current player to the other players
     * @param GameStatus $message
     * @descr
     * GESTIONE CRASH NODO GENERICO
     * a) detentore token (= current player) invia oggetto GameStatus
     * b) tramite RMI riconosce impossibilità di raggiungere un nodo
     * c) detentore del token aggiorna l'oggetto
     * d) detentore del token rinvia l'oggetto a tutti i nodi attivi
     * */
    public void broadcastMessage(GameStatus gamestatus) {

        System.setProperty("java.security.policy", "file:./security.policy");

        // I download server's stubs so I must set a SecurityManager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        /**a) scorro tutti i giocatori rimasti */
        for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
        
        	if(isNotMe(i)) {
                
        		String remoteHost = gameStatus.getPlayersList().get(i).getHost().toString();
                int remotePort = gameStatus.getPlayersList().get(i).getPort();
                int playerId = gameStatus.getPlayersList().get(i).getId();
            	
                try {
                    getRegistryAndSendMessage(remoteHost, remotePort, playerId);
                	
                } catch (RemoteException  | NotBoundException e) {
                	/**b)*/
                	// Se c'è eccezione nella chiamata RMI allora la macchina è un crash
                    //e.printStackTrace();
                    System.out.println("[GameCtrl.broadcastMessage]: Player " + playerId + " crashed.");
                    
                    /**c) setto nel gameStatus giocatore crashato 
                     * 	1) se occorre devo settare il nuovo giocatore successivo*/
                    //players.get(i).setCrashed(true); //utilizzare ancora??
                    gameStatus.setPlayerState(playerId, PLAYER_STATE.CRASH); //utilizzare ancora??
                    
                    /**1) Crashato successivo */
                    Player curr = gameStatus.getCurrentPlayer();
                    if( i ==  gameStatus.getPlayersList().indexOf(curr)) {
                        System.out.println("[GameCtrl.broadcastMessage]: Next player " + i + " crashed.");
                    	gameStatus.setNextPlayer();
                    }
                    
                	System.out.println("[GameCtrl.broadcastMessage]: rimuovo giocatore con id " + i );
                    players.remove(i); // rimuovo il giocatore crashato dalla lista (in questo modo più semplice la gestione del successivo)
                    gameStatus.setPlayersList(players);
          
                    System.out.println("[GameCtrl.broadcastMessage]: giocatore successivo: " + gameStatus.getCurrentPlayer().getId());
                    System.out.println("[GameCtrl.broadcastMessage]: Nuova lista giocatori " + gameStatus.getPlayersList());

                    /**d) reinvio gamestatus a tutti i giocatori */
                    for (int j = 0; j < gameStatus.getPlayersList().size(); j++) {
                        // non lo rimanda a se stesso e ai nodi in crash
                        if(isNotMe(j)) {
                      
                        	String remoteHost2 = gameStatus.getPlayersList().get(j).getHost().toString();
                            int remotePort2 = gameStatus.getPlayersList().get(j).getPort();
                            int playerId2 = gameStatus.getPlayersList().get(j).getId();
                      
                        	try {
	                        	  
	                            getRegistryAndSendMessage(remoteHost2, remotePort2, playerId2);
                            
							} catch (RemoteException | NotBoundException e3) {
								// TODO Auto-generated catch block
								System.out.println("[GameCtrl.broadcastMessage]: MULTIPLE CRASHES EXCEPTION HERE");
								e.printStackTrace();
							}
                        }
                     }
                }
            }
        }

        if( gameStatus.isPenalized()) {
        	gameStatus.setPenalized(false);
        	gameStatus.setMove(null);
        	playGame();
        }
        else if( gameStatus.getMove().getCard2() != null ){
            gameStatus.setPenalized(false);
            gameStatus.setMove(null);
            playGame();
        }
        else if ( gameStatus.getWinner() != null ) {
        	gameStatus.setPenalized(false);
            gameStatus.setMove(null);
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
    	System.out.println("[GameCtrl.handleLazyCurrentPlayer]: ***** START TIMER PENALIZZAZIONE *****");
    	timer = new javax.swing.Timer(20*1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	int nextPlayerId = -1;
            	
            	if(gameStatus.getMove() == null) { 
            		//significa che non è ancora stato inviato gameStatus che dice chi è il prossimo
                    System.out.println("***** [GameCtrl.handleLazyCurrentPlayer]: non è ancora stato inviato gameStatus che dice chi è il prossimo");

                    nextPlayerId = gameStatus.getNextPlayer().getId();
                    System.out.println("***** [GameCtrl.handleLazyCurrentPlayer]: nextPlayerId: " + nextPlayerId);

            	}else {
            		//significa che è già stato inviato gameStatus che dice chi è il prossimo
                    System.out.println("***** [GameCtrl.handleLazyCurrentPlayer]: significa che è già stato inviato gameStatus che dice chi è il prossimo");

                    nextPlayerId = gameStatus.getCurrentPlayer().getId();
                    System.out.println("***** [GameCtrl.handleLazyCurrentPlayer]: nextPlayerId: " + nextPlayerId);

                }
            	
            	/** 2)*/
            	if(nextPlayerId == playerId && gameStatus.isPenalized() == false) {
        			System.out.println("***** [GameCtrl.handleLazyCurrentPlayer]: PROCEDURA DI PENALIZZAZIONE *****");
                    System.out.println("[GameCtrl.handleLazyCurrentPlayer]: SONO IL SUCCESSIVO E IL CORRENTE NON HA GIOCATO");
                	
                    if(gameStatus.getMove() == null)
                    	gameStatus.setNextPlayer();
                	System.out.println("[GameCtrl.handleLazyCurrentPlayer]: nuovo corrente: " + gameStatus.getCurrentPlayer());
                	
            		gameStatus.setPenalized(true);
            		gameStatus.setMove(null);
            		gameStatus.setIdSender(playerId);
                    broadcastMessage(gameStatus);
        		} else {
                    System.out.println("[GameCtrl.handleLazyCurrentPlayer]: else (nextPlayerId == playerId && gameStatus.isPenalized() == false");
                }
            	timer.stop();
            }
        });

        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * @param remoteHost
     * @param remotePort
     * @param playerId
     * @throws RemoteException
     * @throws NotBoundException
     */
    public void	getRegistryAndSendMessage(String remoteHost, int remotePort, int playerId) throws RemoteException, NotBoundException {
    	
    	//***************************************************************
        // mi sa che nel client non serve ma non ne sono sicura
        // in lab funziona con la riga seguente commentata (NON MODIFICARE)
        // System.setProperty("java.rmi.server.hostname", remoteHost);
        //***************************************************************

        Registry registry = LocateRegistry.getRegistry(remoteHost, remotePort);
        String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";
        RemoteMessageServiceInt stub = (RemoteMessageServiceInt) registry.lookup(location);

        System.out.println("[GameCtrl]: Sending gameStatus to player " + playerId);
        gameStatus.setId(gameStatus.getId());
        int response = stub.sendMessage(gameStatus);
    }
    
    
    /**
     * @descr calling the board to update infos
     * @param GameStatus $gameStatus, int $playerId
     * */
    public void resetAndUpdateInfoView(GameStatus gameStatus,int playerId) {
    	boardView.resetAndUpdateInfoView(gameStatus, playerId);
    }
    
    /**
     * @descr given a player in the list of players, checks if i am not me 
     * @param int $index
     * @return boolean true if i am not me, false otw
   	*/
    public boolean isNotMe(int index) {
    	return gameStatus.getPlayersList().get(index).getId() != playerId;
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
    
    public void resetAndUpdatePlayerScores(GameStatus gameStatus){
        boardView.getInfoView().resetAndUpdatePlayerScores(gameStatus);
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
     * @descr get player id
     * @return int $id of the player
     */
    public int getPlayerId() {
		return playerId;
	}
    
    /**
     * @descr set player id
     * @param int $id of the player
     */
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}


}
