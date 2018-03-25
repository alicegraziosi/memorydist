package controller;

import model.gameStatus.GameStatus;
import model.player.Player;
import rmi.RemoteMessageServiceInt;
import java.net.InetAddress;
import server.PlayerServer;
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
import listener.DataReceiverListener;
import server.PlayerServer;

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
    
    /**
     * GameController constructor
     * */
    public GameController(int id,
                          ArrayList<Player> players,
                          GameStatus gameStatus,
                          BlockingQueue<GameStatus> buffer) {
        this.currentId = id;
        this.gameStatus = gameStatus;
        this.players = players;
        this.buffer = buffer;
        this.turnNumber = 0;
        this.msgId = 0;
    }

    /**
     * function which manages the single round of the game
     * */
    public void playGame() {}
//    public void playGame(){
//
//        // todo con i thread o senza!? non lo so...
////        Thread t = new Thread(new Runnable() {
////            @Override
////            public void run() {
////
////            
//        try {
//        	
//        	PlayerClient playerClient = new PlayerClient();
//        	
//        	Player newCurrent = gameStatus.getCurrentPlayer();
//			gameStatus.setCurrentPlayer(newCurrent);
//        	
//			
//			 /** E' il turno del giocatore, cose da fare:
//        	1) accertarsi che il giocatore corrente abbia un $gameStatus aggiornato
//         	2) giocatore gioca la prima carta
//     			a) gioca la carta
//        		b) aggiorna $gameStatus facendo BROADCAST
//            3) giocatore gioca la seconda carta
//        		a) gioca la carta con controllo matching e tutto il resto
//        		b) aggiorna $gameStatus facendo BROADCAST
//            4) gestire il "passaggio" del turno 
//            5) aggiorno $gameStatus facendo BROADCAST
//            */
//			
//			/**
//             * 4) gestione passaggio del turno
//             * */
//            System.out.println("[GameController]: fase 4");
//            
//            /**
//             * 5) BROADCAST $gameStatus
//             * */
//            if(isMyTurn(gameStatus)) {
//	            System.out.println("[GameController]: fase 5");
//	            playerClient.broadcastUpdatedGame(gameStatus);
//            }
//			
//			
//			//setting new player
//            // 0) se ci sono ancora carte da scoprire e se è rimasto più di un giocatore, altrimenti si lascia giocare l'ultimo giocatore
//
//            // Se è il suo turno, id parte da 1, l'indice da 0
////        	System.out.println("[GameController]: Turno di " + currentId);
////        	for(Player remote: players){
////        		System.out.println("Player: " + remote.toString());
////        	}
////        	
////            if (isMyTurn(gameStatus)) {
////                System.out.println("[GameController]: E' il mio turno (Giocatore " + currentId + ").");
////                //System.out.println("[GameController[: gameStatus corrente: " + gameStatus.toString());
//                
//               
//                
//                
//                // todo setto il giocatore che ha il prossimo turno (
//                // (funziona solo con due giocatori)
//                // todo è un casino con i .sleep.. ho provato con timer
//                
//                // controllo variabile setMyTurn di tutti i giocatori
////                for (int i = 0; i < players.size(); i++) {
////                	System.out.println("[GameController: checking the players turns...");
////                	System.out.println("[GameController]: player " + players.get(i).getId() + " turn :" + Boolean.toString(players.get(i).getMyTurn()) );
////                    
////                }
//
//                // PER IL MOMENTO COMMENTATO CODICE PER SETTARE A FALSE IL TURNO DI TUTTI GLI ALTRI
//                // id parte da 1, l'indice da 0
////                for (int i = 0; i < players.size(); i++) {
////                    // setto tutti a myTurn false
////                    players.get(i).setMyTurn(false);
////                }
//
//                
////                System.out.println("Faccio broadcast di questa informazione.");
////                gameStatus.setPlayersList(players);
////                gameStatus.setIdSender(id);
////                broadcastMessage(gameStatus);
//
//
//                // mossa
//                // notifica mossa
//                // aggiorna game status
//                // aggiorna board
//
//                // aggiornamento punteggio
//
//                // prova per vedere se un altro processo è in crash
////                System.out.println("Faccio broadcast di un messaggio..");
////                gameStatus.setIdSender(id);
////                broadcastMessage(gameStatus);
//
//                // prova per vedere se un altro processo è in crash
////                System.out.println("Faccio un altro broadcast di un messaggio..");
////                gameStatus.setIdSender(id);
////                broadcastMessage(gameStatus);
//
//                // nel frattempo può andare in crash
//
//                //sleep(10 * 1000);
//                //playGame();
//
////            } else {
////                // Se non è il suo turno
////                for (int i = 0; i < players.size(); i++) {
////                    if(players.get(i).isMyTurn()){
////                        System.out.println("E' il turno del giocatore " + Integer.valueOf(i).toString());
////                    }
////                }
////                System.out.println("(Io sono il giocatore " + currentId + ").");
////                System.out.println("Resto in ascolto di messaggi...");
//
//                // la board è bloccata
//
//                try {
//                    turnNumber++;
//                    System.out.println("\n\n******** Turn number " + turnNumber + " ********");
//
//                    //se ci sono ancora carte da scoprire e se è rimasto più di un giocatore
//                    if (gameStatus.getShowingCards().size() < 20) {
//
//                        // altrimenti si lascia giocare l'ultimo giocatore
//
//                            // 0) se ci sono ancora carte da scoprire e se è rimasto più di un giocatore, altrimenti si lascia giocare l'ultimo giocatore
//
////                            if (gameStatus.getPlayersList().get(currentId).isMyTurn()) {
////                                System.out.println("[GameController]: E' il mio turno (Giocatore " + currentId + ").");
//                                //System.out.println("[GameController[: gameStatus corrente: " + gameStatus.toString());
//
//                                /** E' il turno del giocatore, cose da fare:
//                                 1) accertarsi che il giocatore corrente abbia un $gameStatus aggiornato
//                                 2) giocatore gioca la prima carta
//                                 a) gioca la carta
//                                 b) aggiorna $gameStatus facendo BROADCAST
//                                 3) giocatore gioca la seconda carta
//                                 a) gioca la carta con controllo matching e tutto il resto
//                                 b) aggiorna $gameStatus facendo BROADCAST
//                                 4) gestire il "passaggio" del turno
//                                 */
//
//
//                                // todo setto il giocatore che ha il prossimo turno (
//                                // (funziona solo con due giocatori)
//                                // todo è un casino con i .sleep.. ho provato con timer
//
//                                // controllo variabile setMyTurn di tutti i giocatori
//                                //                for (int i = 0; i < players.size(); i++) {
//                                //                	System.out.println("[GameController: checking the players turns...");
//                                //                	System.out.println("[GameController]: player " + players.get(i).getId() + " turn :" + Boolean.toString(players.get(i).getMyTurn()) );
//                                //
//                                //                }
//
//                                // PER IL MOMENTO COMMENTATO CODICE PER SETTARE A FALSE IL TURNO DI TUTTI GLI ALTRI
//                                // id parte da 1, l'indice da 0
//                                //                for (int i = 0; i < players.size(); i++) {
//                                //                    // setto tutti a myTurn false
//                                //                    players.get(i).setMyTurn(false);
//                                //                }
//
//                                /**
//                                 * 4) gestione passaggio del turno
//                                 * */
//                                //gameStatus.setNextTurn(id) da impl
//
//                                //
//                                for (int i = 0; i < players.size(); i++) {
//                                    // setto tutti a myTurn false
////                                    players.get(i).setMyTurn(false);
//                                }
//
//                                int index = id + 1;
//                                // se il giocatore corrente è l'ultimo, il prossimo è il primo
//                                if (index == players.size()) {
//                                    index = 0;
//                                }
//
//                                // setto il turnNumber al prossimo giocatore non in crash
//                                for (int i = index; i<players.size(); i++) {
//                                    if (!players.get(i).isCrashed()) {
////                                        players.get(i).setMyTurn(true);
//                                        System.out.println("Il prossimo giocatore è : " + Integer.valueOf(i).toString());
//                                        break;
//                                    } else {
//                                        System.out.println("sarebbe stato il turnNumber di " + players.get(i).getId() + " ma è crashed");
//                                    }
//                                }
//
//                                System.out.println("Faccio broadcast di questa informazione.");
//                                gameStatus.setPlayersList(players);
//                                gameStatus.setIdSender(currentId);
//                                gameStatus.setId(gameStatus.getId() + 1);
//                                broadcastMessage(gameStatus);
//
//
//                                // mossa
//                                // notifica mossa
//                                // aggiorna game status
//                                // aggiorna board
//                                // board.update(gameStatus);
//
//                                // aggiornamento punteggio
//
//                                // prova per vedere se un altro processo è in crash
//                                // todo da togliere, è solo una prova
//                                /*
//                                System.out.println("Faccio broadcast di un messaggio..");
//                                gameStatus.setIdSender(id);
//                                broadcastMessage(gameStatus);
//
//                                // prova per vedere se un altro processo è in crash
//                                // todo da togliere, è solo una prova
//                                System.out.println("Faccio un altro broadcast di un messaggio..");
//                                gameStatus.setIdSender(id);
//                                broadcastMessage(gameStatus);
//                                */
//
//                                // nel frattempo può andare in crash
//
//                                //sleep(10 * 1000);
//                                //playGame();
//
//                            } else {
//                                // Se non è il suo turnNumber
//
//                                // la board è bloccata
//                                //board.block();
//
//                                for (int i = 0; i < gameStatus.getPlayersList().size(); i++) {
//                                    if (gameStatus.getPlayersList().get(i).isMyTurn()) {
//                                        System.out.println("NON è il mio turno, è il turno del giocatore " + Integer.valueOf(i).toString());
//                                    }
//                                }
//                                System.out.println("(Io sono il giocatore " + id + ").");
//                                System.out.println("Resto in ascolto di messaggi...");
//
//                                // se non arrivano mess
//                                // un giocatore è in crash oppure non ha fatto mosse
//
//
//                                //sleep(20 * 1000);
//
//                                // nel frattempo può andare in crash
//
//                                // nel frattempo riceve messaggi
//                                /*
//                                GameStatus receivedMessage = buffer.poll();
//                                if(receivedMessage!=null){
//                                    System.out.println("Processo il messaggio.");
//                                    gameStatus = receivedMessage;
//                                } else {
//                                    System.out.println("receivedMessage poll null");
//                                }*/
//
//                                //playGame();
//                            }
////                        } else {
////                            System.out.println("receivedMessage poll null");
////                        }
//
//                //playGame();
////            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        /*
//                            // all cards are matched
//                            // current player is the winner
//                            // id parte da 1, l'indice da 0
//                            if (gameStatus.getPlayersList().get(id).isMyTurn()) {
//                                // todo this is the winner
//                                System.out.println("You are the winner!");
//                                // broadcast(gamestatus)
//                            } else {
//                                // todo all other playes need to know that the game ended
//                                System.out.println("Another player won the game.");
//                            }
//                        }
//                } catch(Exception e){
//                    e.printStackTrace();
//                }
//>>>>>>> 1a56fc670b23ffbda2352cd601253ae645bdaebe
//            }
//        });
//
//        t.start();
//
//    }
    
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
    public void broadcastMessage(GameStatus gamestatus) {

        // mi sa che nel client non serve
        //System.setProperty("java.rmi.server.hostname", host);

        System.setProperty("java.security.policy", "file:./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        for (int i = 0; i < players.size(); i++) {
            // non lo rimanda a se stesso e ai nodi in crash
            if(i+1 != currentId && !players.get(i).isCrashed()) {
                Registry registry = null;
            if(i != currentId && !players.get(i).isCrashed()) {

            	try {
                    String remoteHost = players.get(i).getHost().toString();
                    int remotePort = players.get(i).getPort();

                    String location = "rmi://" + remoteHost + ":" + remotePort + "/messageService";

                    RemoteMessageServiceInt stub = (RemoteMessageServiceInt) Naming.lookup(location);

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
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
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
    
	private boolean isMyTurn(GameStatus gameStatus) {
		return gameStatus.getCurrentPlayer().getId() == this.currentId;
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


    @Override
	public void setupRemoteClient(GameStatus gameStatus) throws RemoteException, NotBoundException {
		//this.playerClient = new PlayerClient(game, id);
		//setGame(game);
	}

	@Override
	public void setGame(GameStatus gameStatus) throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		
	}


	
}

	