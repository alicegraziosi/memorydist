package rmi;

import controller.GameController;
import model.gameStatus.GameStatus;
import model.player.Player;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Timer;

/**
 * @desc implementation of remote message service interface
 */
public class RemoteMessageServiceImpl extends UnicastRemoteObject implements RemoteMessageServiceInt {

	private GameController gameController;
	private Timer t;


	public RemoteMessageServiceImpl(GameController gameController) throws RemoteException {
		this.gameController = gameController;
	}
	
	public int sendMessage(GameStatus gameStatus) throws RemoteException {

		// un processo invoca sendMessage su un altro processo
		// il processo su cui è invocato sendMessage riceve il messaggio in questo punto
		System.out.println("[RMISImpl]: Message received from player " + gameStatus.getIdSender());

		System.out.println("[RMISImpl]: Message said that next player is: " + gameStatus.getCurrentPlayer().getId());
	
		gameController.setGameStatus(gameStatus); // setting the new updated gameStatus

		if(gameStatus.getMove() != null){
			System.out.println("[RMISImpl]: Message " + gameStatus.getId() + " contains a move");


			gameController.updateBoardAfterMove(gameStatus.getMove());

			if(gameStatus.getMove().getCard2() != null) {
				/** minimo ritardo prima di riniziare il turno*/
				t = new javax.swing.Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	
                    	gameController.playGame();
                        
                        t.stop();
                    }
                });

                t.setRepeats(false);
                
            	t.start();
				// due carte girate
				// riparte play game
				;
				return 2;
			}
		} else {
			System.out.println("[RMISImpl]: gameStatus " + gameStatus.toString());
		}

		return 1;
	}

	public int ping() {
		return 1;
	}
	

	@Override
	public int sendCrashMessage(GameStatus gameStatus, Player crashedPlayer, 
			boolean isCurrentPlayerCrashed) throws RemoteException {

		System.out.println("[RMISImpl]: Crash message received from player " + gameStatus.getIdSender());
		System.out.println("[RMISImpl]: Crash message said that next player is: " + gameStatus.getCurrentPlayer().getId());
		
		/** se non è crashato il giocatore corrente allora devo aggiorna il next player*/
//		if (!isCurrentPlayerCrashed)
//			gameStatus.getNextPlayer();
		gameController.setGameStatus(gameStatus); // setting the new updated gameStatus

		/** controllo che io non sia diventato il giocatore successivo
		 * 	in tal caso devo pingare il giocatore corrente */
		if (!isCurrentPlayerCrashed) {
			if ( gameStatus.getCurrentPlayer().getId() == gameController.getCurrentId()) {
				System.out.println("***** Sono il nuovo successivo causa CRASH *****");
				gameController.pingAPlayer( gameStatus.getIdSender(),
						true);
			}
		}
			
		
		/** se è crashato il giocatore corrente allora ricomincio il turno (sono giocatore non corrente)*/
		if (isCurrentPlayerCrashed ) 
			gameController.playGame(); // solo se è crashato il current player ho bisogno di chiamare playGame
				
		return 1;
	}
}