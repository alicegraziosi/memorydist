package rmi;

import controller.GameController;
import model.gameStatus.GameStatus;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.Timer;

/**
 * @desc implementation of remote message service interface
 */
public class RemoteMessageServiceImpl extends UnicastRemoteObject implements RemoteMessageServiceInt {

	private GameController gameController;
	private Timer timer;
	private int wait = 2000;

	/**
	 * @descr constructor
	 * */
	public RemoteMessageServiceImpl(GameController gameController) throws RemoteException {
		this.gameController = gameController;
	}
	
	/**
	 * @descr un processo invoca sendMessage su un altro processo
	 * il processo su cui è invocato sendMessage riceve il messaggio in questo punto
	 * */
	public int sendMessage(final GameStatus gameStatus) throws RemoteException {

		System.out.println("[RMISImpl]: Message received from player " + gameStatus.getIdSender());
		System.out.println("[RMISImpl]: Message said that next player is: " + gameStatus.getCurrentPlayer().getId());
	
		gameController.setGameStatus(gameStatus); // setting the new updated gameStatus
		
		if( gameStatus.getWinner() != null) {
		
			System.out.println("[RMISImpl] THE WINNER IS: " + gameStatus.getWinner().getId());
            /** minimo ritardo prima di riniziare il turno*/
			timer = new javax.swing.Timer(wait, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	
                	gameController.updateBoardAfterMove(gameStatus.getMove());
                    gameController.resetAndUpdateInfoView(gameStatus, gameController.getPlayerId());
        			gameStatus.setPenalized(false);
                    gameStatus.setMove(null);
                	gameController.playGame();
                    timer.stop();
                }
            });

            timer.setRepeats(false);
        	timer.start();
		}
		else if( gameStatus.isPenalized() ) { // procedura di penalizzazione attiva
			
			/** minimo ritardo prima di riniziare il turno*/
			timer = new javax.swing.Timer(wait, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	
                	gameStatus.setPenalized(false);
                    gameStatus.setMove(null);
                    gameController.setGameStatus(gameStatus);
                	gameController.playGame();
                    timer.stop();
                }
            });

            timer.setRepeats(false);
         	timer.start();
		}
		else if(gameStatus.getMove() != null){ // qualche mossa eseguita ( prima o seconda)

			System.out.println("[RMISImpl]: Message " + gameStatus.getId() + " contains a move");
			gameController.updateBoardAfterMove(gameStatus.getMove());
			
			 if(gameStatus.getMove().getCard2() != null) { // seconda mossa eseguita
				/** minimo ritardo prima di riniziare il turno*/
				timer = new javax.swing.Timer(wait, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	gameController.playGame();
                        timer.stop();
                    }
                });

                timer.setRepeats(false);
            	timer.start();
			 }
		} else { // gamestatus appena ricevuto senza mossa, cioè inizio nuovo turno
			System.out.println("[RMISImpl]: gameStatus " + gameStatus.toString());
		}

		return 1;
	}

}