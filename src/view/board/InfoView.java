package view.board;

import model.gameStatus.GameStatus;
import model.player.PLAYER_STATE;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InfoView extends JPanel {

    private GameStatus localGameStatus;
    private int playerId;
    private ArrayList<JLabel> labels;

    public InfoView(GameStatus gameStatus, int playerId) {
        this.labels = new ArrayList<>();
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
        labels = new ArrayList<>();

        // players info
        for(int i=0; i<localGameStatus.getPlayersList().size(); i++){
            if(localGameStatus.getPlayerState(i).equals(PLAYER_STATE.CRASH)) {
                JLabel labelPlayerId = new JLabel("Player id: " + localGameStatus.getPlayersList().get(i).getId()
                        + "nickname " + localGameStatus.getPlayersList().get(i).getNickName() + " left the game :(");
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

        labels = new ArrayList<>();

        this.add(new JSeparator(SwingConstants.HORIZONTAL));

        if(playerId == currentPlayerId) {
            JLabel labelTurnOf = new JLabel("It's your turn!");
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
            JLabel labelScore = new JLabel("Score: " + localGameStatus.getPlayersList().get(playerId).getScore());
            labelScore.setForeground(Color.red);
            labels.add(labelScore);
        } else {
            JLabel labelTurnOf = new JLabel("It's turn of: " + currentPlayerId + " " + localGameStatus.getPlayersList().get(currentPlayerId).getNickName());
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
        }

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }
    }
}