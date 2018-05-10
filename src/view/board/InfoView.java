package view.board;

import model.gameStatus.GameStatus;
import model.player.PLAYER_STATE;
import model.player.Player;
import utils.CircularArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InfoView extends JPanel {

    private GameStatus localGameStatus;
    private int playerId;
    private CircularArrayList<JLabel> labels;

    public InfoView(GameStatus gameStatus, int playerId) {
        this.labels = new CircularArrayList<>();
        this.localGameStatus = gameStatus;
        this.playerId = playerId;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    }

    public void setLocalGameStatus(GameStatus localGameStatus) {
        this.localGameStatus = localGameStatus;
    }

    public void update(GameStatus gameStatus, int currentPlayerId){

        setLocalGameStatus(gameStatus);
        this.removeAll();
        labels = new CircularArrayList<>();

        // players info
        for(int i=0; i<localGameStatus.getPlayersList().size(); i++){
            if(localGameStatus.getPlayerState(i).equals(PLAYER_STATE.CRASH)) {
                JLabel labelPlayerId = new JLabel("Player id: " + 
                		localGameStatus.getPlayersList().get(i).getId() +
                        "nickname " + localGameStatus.getPlayersList().get(i).getNickName() + 
                        " left the game :(");
                labels.add(labelPlayerId);
            } else {
                JLabel labelPlayerId = new JLabel("Player id: " + localGameStatus.getPlayersList().get(i).getId());
                labels.add(labelPlayerId);
                JLabel labelPlayerNickname = new JLabel("Nickname: " + localGameStatus.getPlayersList().get(i).getNickName());
                labels.add(labelPlayerNickname);
                JLabel labelPlayerScore = new JLabel("Score: " + localGameStatus.getPlayersList().get(i).getScore());
                labels.add(labelPlayerScore);
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
            labels.add(labelTurnOf);
            Player playerToUpd = localGameStatus.findPlayerById(playerId);
            int playerIndex = -1;
            if(playerToUpd != null) {
            	playerIndex = localGameStatus.getPlayersList().indexOf(playerToUpd);
	            System.out.println("playerId: " + playerId + " player index: " + playerIndex);
	            JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(playerIndex).getScore());
	            labelScore.setForeground(Color.red);
	            labels.add(labelScore);
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