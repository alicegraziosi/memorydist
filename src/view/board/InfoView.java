package view.board;

import model.gameStatus.GameStatus;
import model.player.PLAYER_STATE;
import model.player.Player;
import utils.CircularArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class InfoView extends JPanel {

    private GameStatus localGameStatus;
    private int playerId;
    private CircularArrayList<JLabel> labels;

    /**
     * 
     * @param gameStatus
     * @param playerId
     */
    public InfoView(GameStatus gameStatus, int playerId) {
        this.labels = new CircularArrayList<>();
        this.localGameStatus = gameStatus;
        this.playerId = playerId;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    }
    
    /**
     * @param localGameStatus
     */
    public void setLocalGameStatus(GameStatus localGameStatus) {
        this.localGameStatus = localGameStatus;
    }

    /** 
     * @param gameStatus
     * @param currentPlayerId
     */
    public void update(GameStatus gameStatus, int currentPlayerId){

        setLocalGameStatus(gameStatus);
        this.removeAll();
        labels = new CircularArrayList<>();

        //iterare su hashmap playersAvailibility
        HashMap<Integer,PLAYER_STATE> playersAvailability = localGameStatus.getPlayersAvailability();
        for (Entry<Integer, PLAYER_STATE>  entry : playersAvailability.entrySet())
        {
        	System.out.println("OOOOOOO playersA: " + entry.getKey() + " - " + entry.getValue());
        	if (entry.getValue().equals(PLAYER_STATE.CRASH)) {
        		JLabel labelPlayerId = new JLabel("Player id: " +
                		entry.getKey() );
        		ImageIcon image = new ImageIcon("./images/crash.jpg");
        		labelPlayerId.setIcon(image);
                labels.add(labelPlayerId);
        	}
        	else {
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey());
        		ImageIcon image = new ImageIcon("./images/user.jpg");
        		labelPlayerId.setIcon(image);
                labels.add(labelPlayerId);
        	}
            }
        
        // players info
//        for(int i=0; i<localGameStatus.getPlayersList().size(); i++){
//        	int playerId = localGameStatus.getPlayersList().get(i).getId();
//            if(localGameStatus.getPlayerState(playerId).equals(PLAYER_STATE.CRASH)) {
//                JLabel labelPlayerId = new JLabel("Player id: " +
//                		playerId +
//                        " CRASH" );
//                      //  " left the game :(");
//                labels.add(labelPlayerId);
//            } else {
//            	JLabel labelPlayerId = new JLabel("Player id: " + playerId);
//                labels.add(labelPlayerId);
//                JLabel labelPlayerNickname = new JLabel("Nickname: " + localGameStatus.getPlayersList().get(i).getNickName());
//                labels.add(labelPlayerNickname);
//                JLabel labelPlayerScore = new JLabel("Score: " + localGameStatus.getPlayersList().get(i).getScore());
//                labels.add(labelPlayerScore);
//                if ( localGameStatus.getWinner() != null) {
//	                if ( localGameStatus.getWinner().getId() == localGameStatus.getPlayersList().get(i).getId()) {
//	                	JLabel labelWinnerPlayer = new JLabel("WINNER");
//	                	labelWinnerPlayer.setForeground(Color.red);
//	                    labels.add(labelWinnerPlayer);
//	                }
//                }     
//            }
//        }

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }

        labels = new CircularArrayList<>();

        this.add(new JSeparator(SwingConstants.HORIZONTAL));

        if(playerId == currentPlayerId) {
            JLabel labelTurnOf = new JLabel("It's your turn!");
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
            Player playerToUpd = localGameStatus.findPlayerById(playerId);
            int playerIndex = -1;
            if(playerToUpd != null) {
                playerIndex = localGameStatus.getPlayersList().indexOf(playerToUpd);
                System.out.println("playerId: " + playerId + " player index: " + playerIndex);
                JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(playerIndex).getScore());
                labelScore.setForeground(Color.red);
                labels.add(labelScore);
                if ( localGameStatus.getWinner() != null) {
	                if ( localGameStatus.getWinner().getId() == localGameStatus.getPlayersList().get(playerIndex).getId()) {
	                	JLabel labelWinnerPlayer = new JLabel("[ You Win ]");
	                	labelWinnerPlayer.setForeground(Color.red);
	                    labels.add(labelWinnerPlayer);
	                }
                }  
            }
        } else {
            Player playerToUpd = localGameStatus.findPlayerById(playerId);
            int myPlayerIndex = localGameStatus.getPlayersList().indexOf(playerToUpd);
            JLabel labelTurnOf = new JLabel("It's turn of: " + currentPlayerId);
//            		+ " " + 
//        	localGameStatus.getPlayersList().get(currentPlayerIndex).getNickName());
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
            JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(myPlayerIndex).getScore());
            labelScore.setForeground(Color.red);
            labels.add(labelScore);
        }

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }
    }
}