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
    private ArrayList<JLabel> labelsTurn;
    

    /**
     * 
     * @param gameStatus
     * @param playerId
     */
    public InfoView(GameStatus gameStatus, int playerId) {
        this.labels = new CircularArrayList<>();
        this.labelsTurn = new CircularArrayList<>();
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
    
    public void resetAndUpdatePlayerScores(GameStatus gameStatus){
    	this.removeAll();
    	System.out.println("resetAndUpdatePlayerScores");

        labels = new CircularArrayList<>();
    	//iterare su hashmap playersAvailibility
        HashMap<Integer,PLAYER_STATE> playersAvailability = gameStatus.getPlayersAvailability();
        for (Entry<Integer, PLAYER_STATE>  entry : playersAvailability.entrySet())
        {
        	System.out.println("OOOOOOO playersA: " + entry.getKey() + " - " + entry.getValue());
        	if (entry.getValue().equals(PLAYER_STATE.CRASH)) {
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() );
        		ImageIcon image = new ImageIcon("./images/crash.jpg");
        		labelPlayerId.setIcon(image);
        		labels.add(labelPlayerId);
        	}
        	else {
        		Player playerToUpd = gameStatus.findPlayerById(entry.getKey());
        		int index = gameStatus.getPlayersList().indexOf(playerToUpd);
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() + " \n Score: " + gameStatus.getPlayersList().get(index).getScore());
                labels.add(labelPlayerId);
        	}
         }
        

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }

        labels = new CircularArrayList<>();
    }

    /** 
     * @param gameStatus
     * @param currentPlayerId
     */
    public void update(GameStatus gameStatus, int currentPlayerId){

        setLocalGameStatus(gameStatus);
        
        System.out.println("update");
        

        this.removeAll();
        
        labels = new CircularArrayList<>();
        labelsTurn = new ArrayList<>();

        //iterare su hashmap playersAvailibility
        HashMap<Integer,PLAYER_STATE> playersAvailability = localGameStatus.getPlayersAvailability();
        for (Entry<Integer, PLAYER_STATE>  entry : playersAvailability.entrySet())
        {
        	System.out.println("OOOOOOO playersA: " + entry.getKey() + " - " + entry.getValue());
        	if (entry.getValue().equals(PLAYER_STATE.CRASH)) {
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() );
        		
        		ImageIcon image = new ImageIcon("./images/crash.jpg");
        		labelPlayerId.setIcon(image);
        		labelPlayerId.setOpaque(true);
        		labelPlayerId.setBackground(Color.gray);
                labels.add(labelPlayerId);
        	}
        	else {

        		Player playerToUpd = localGameStatus.findPlayerById(entry.getKey());
        		int index = localGameStatus.getPlayersList().indexOf(playerToUpd);
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() + " \n Score: " + localGameStatus.getPlayersList().get(index).getScore());
        		ImageIcon image = new ImageIcon("./images/user.jpg");
        		labelPlayerId.setIcon(image);
        		
                labels.add(labelPlayerId);
        	}
         }
        

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }

        labels = new CircularArrayList<>();

        this.add(new JSeparator(SwingConstants.HORIZONTAL));

        if(playerId == currentPlayerId) {
            JLabel labelTurnOf = new JLabel("It's your turn!");
            labelTurnOf.setForeground(Color.red);
            labelsTurn.add(labelTurnOf);
            Player playerToUpd = localGameStatus.findPlayerById(playerId);
            int playerIndex = -1;
            if(playerToUpd != null) {
                playerIndex = localGameStatus.getPlayersList().indexOf(playerToUpd);
                System.out.println("playerId: " + playerId + " player index: " + playerIndex);
                JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(playerIndex).getScore());
                labelScore.setForeground(Color.red);
                labelsTurn.add(labelScore);
                if ( localGameStatus.getWinner() != null) {
	                if ( localGameStatus.getWinner().getId() == localGameStatus.getPlayersList().get(playerIndex).getId()) {
	                	JLabel labelWinnerPlayer = new JLabel("[ You Win ]");
	                	labelWinnerPlayer.setForeground(Color.red);
	                	labelsTurn.add(labelWinnerPlayer);
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
            labelsTurn.add(labelTurnOf);
            JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(myPlayerIndex).getScore());
            labelScore.setForeground(Color.red);
            labelsTurn.add(labelScore);
        }

        for(int i = 0; i< labelsTurn.size(); i++){
            this.add(labelsTurn.get(i));
        }
        
        labelsTurn = new ArrayList<>();
    }
}