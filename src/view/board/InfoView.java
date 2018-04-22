package view.board;

import model.gameStatus.GameStatus;

import javax.swing.*;
import java.awt.*;

public class InfoView extends JPanel {

    private GameStatus gameStatus;
    private int id;
    private JLabel labelTurnOf;
    private JLabel labelScore;

    public InfoView(GameStatus gameStatus, int id) {
        this.gameStatus = gameStatus;
        this.id = id;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel labelPlayers = new JLabel("Players:");
        this.add(labelPlayers);
        labelTurnOf = null;
        labelScore = null;

        // players info
        for(int i=0; i<gameStatus.getPlayersList().size(); i++){
            if(!gameStatus.getPlayersList().get(i).isCrashed()) {
                JLabel labelPlayerId = new JLabel("Id: " + gameStatus.getPlayersList().get(i).getId());
                this.add(labelPlayerId);
                JLabel labelPlayerNickname = new JLabel("Nickname: " + gameStatus.getPlayersList().get(i).getNickName());
                this.add(labelPlayerNickname);
                JLabel labelPlayerScore = new JLabel("Score: " + gameStatus.getPlayersList().get(i).getScore());
                this.add(labelPlayerScore);
                JLabel labelPlayerTurn = new JLabel("Turn: " + gameStatus.getPlayersList().get(i).isMyTurn());
                this.add(labelPlayerTurn);
            }
        }

        if(!gameStatus.getPlayersList().get(id).isCrashed() && gameStatus.getPlayersList().get(id).isMyTurn()) {
            labelTurnOf = new JLabel("It's your turn!");
            labelScore = new JLabel("Score: " + gameStatus.getPlayersList().get(id).getScore());

        } else {
            for(int i=0; i<gameStatus.getPlayersList().size(); i++) {
                if (!gameStatus.getPlayersList().get(i).isCrashed() && gameStatus.getPlayersList().get(i).isMyTurn()) {
                    labelTurnOf = new JLabel("It's turn of: " + gameStatus.getPlayersList().get(i).getId() + " " + gameStatus.getPlayersList().get(i).getNickName());
                    break;
                }
            }
            labelScore = new JLabel("");
        }
        labelTurnOf.setForeground(Color.red);
        this.add(labelTurnOf);

        labelScore.setForeground(Color.red);
        this.add(labelScore);
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void updateScores(GameStatus gameStatus){
        labelScore.setText("Score: " + gameStatus.getPlayersList().get(id).getScore());
    }

    public void update(GameStatus gameStatus, int currentId){

        setGameStatus(gameStatus);

        //todo aggiornare punteggi


        if(currentId == id) {
            labelTurnOf.setText("It's your turn!");
            labelScore.setVisible(true);
            labelScore.setText("Score: " + this.gameStatus.getPlayersList().get(currentId).getScore());
        } else {
            labelTurnOf.setText("It's turn of: " + currentId + " " + this.gameStatus.getPlayersList().get(currentId).getNickName());
            labelScore.setVisible(false);
        }
    }
}