package view.board;

import model.gameStatus.GameStatus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InfoView extends JPanel {

    private GameStatus gameStatus;
    private int id;
    private ArrayList<JLabel> labels;

    public InfoView(GameStatus gameStatus, int id) {
        this.labels = new ArrayList<>();
        this.gameStatus = gameStatus;
        this.id = id;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void update(GameStatus gameStatus, int currentId){

        setGameStatus(gameStatus);

        for(int i = 0; i<labels.size(); i++){
            this.remove(labels.get(i));
        }
        labels = new ArrayList<>();

        // players info
        for(int i=0; i<gameStatus.getPlayersList().size(); i++){
            if(!gameStatus.getPlayersList().get(i).isCrashed()) {
                JLabel labelPlayerId = new JLabel("Player id: " + gameStatus.getPlayersList().get(i).getId());
                labels.add(labelPlayerId);
                JLabel labelPlayerNickname = new JLabel("Nickname: " + gameStatus.getPlayersList().get(i).getNickName());
                labels.add(labelPlayerNickname);
                JLabel labelPlayerScore = new JLabel("Score: " + gameStatus.getPlayersList().get(i).getScore());
                labels.add(labelPlayerScore);
            }
        }

        if(currentId == id) {
            JLabel labelTurnOf = new JLabel("It's your turn!");
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
            JLabel labelScore = new JLabel("Score: " + gameStatus.getPlayersList().get(id).getScore());
            labelScore.setForeground(Color.red);
            labels.add(labelScore);
        } else {
            JLabel labelTurnOf = new JLabel("It's turn of: " + currentId + " " + gameStatus.getPlayersList().get(currentId).getNickName());
            labelTurnOf.setForeground(Color.red);
            labels.add(labelTurnOf);
        }

        for(int i = 0; i< labels.size(); i++){
            this.add(labels.get(i));
        }
    }
}