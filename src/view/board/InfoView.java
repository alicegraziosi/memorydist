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
     * constructor
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

    /** 
     * updating infoview
     * @param gameStatus
     * @param currentPlayerId
     */
    public void update(GameStatus gameStatus, int currentPlayerId){

    	setLocalGameStatus(gameStatus);
    	removeAll();

        labels = new CircularArrayList<>();
        labelsTurn = new ArrayList<>();

        //iterare su hashmap playersAvailibility
        HashMap<Integer,PLAYER_STATE> playersAvailability = localGameStatus.getPlayersAvailability();
        for (Entry<Integer, PLAYER_STATE>  entry : playersAvailability.entrySet())
        {
        	System.out.println("*O*O*O* Players: " + entry.getKey() + " - " + entry.getValue());
        	if (entry.getValue().equals(PLAYER_STATE.CRASH)) {
        		
        		JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() + " quit ");
        		ImageIcon image = new ImageIcon("./images/crash.jpg");
        		labelPlayerId.setIcon(image);
        		labelPlayerId.setOpaque(true);
        		labelPlayerId.setBackground(Color.gray);
                labels.add(labelPlayerId);
            } else {

                Player playerToUpd = localGameStatus.findPlayerById(entry.getKey());
                int index = localGameStatus.getPlayersList().indexOf(playerToUpd);

                System.out.println("[Infoview.update]: player: " + entry.getKey() + " - "
                        + entry.getValue() + " score: " + localGameStatus.getPlayersList().get(index).getScore());

                JLabel labelPlayerId = new JLabel("Player id: " + entry.getKey() + " \n Score: " + localGameStatus.getPlayersList().get(index).getScore());
                ImageIcon image = new ImageIcon("./images/user.jpg");
                labelPlayerId.setIcon(image);
                labels.add(labelPlayerId);
            }
        }

        for (int i = 0; i < labels.size(); i++) {
            add(labels.get(i));
        }

        labels = new CircularArrayList<>();

        add(new JSeparator(SwingConstants.HORIZONTAL));

        if (playerId == currentPlayerId) {

        	JLabel labelTurnOf = new JLabel("It's your turn!");
            labelTurnOf.setForeground(Color.red);
            labelsTurn.add(labelTurnOf);
            Player playerToUpd = localGameStatus.findPlayerById(playerId);
            int playerIndex = -1;
            if (playerToUpd != null) {
            
            	playerIndex = localGameStatus.getPlayersList().indexOf(playerToUpd);
                System.out.println("playerId: " + playerId + " player index: " + playerIndex);
                JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(playerIndex).getScore());
                labelScore.setForeground(Color.red);
                labelsTurn.add(labelScore);
                
                if (localGameStatus.getWinner() != null) {
                
                	if (localGameStatus.getWinner().getId() == localGameStatus.getPlayersList().get(playerIndex).getId()) {
                    
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

            labelTurnOf.setForeground(Color.red);
            labelsTurn.add(labelTurnOf);
            JLabel labelScore = new JLabel("Your score: " + localGameStatus.getPlayersList().get(myPlayerIndex).getScore());
            labelScore.setForeground(Color.red);
            labelsTurn.add(labelScore);
        }

        for (int i = 0; i < labelsTurn.size(); i++) {add(labelsTurn.get(i));
        }

        labelsTurn = new ArrayList<>();

        revalidate();
    }

    @Override
    public void revalidate() {
        super.revalidate();
    }
}