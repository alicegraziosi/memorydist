package view.board;

import model.gameStatus.GameStatus;

import javax.swing.*;
import java.awt.*;

class Info extends JPanel {

    private GameStatus gameStatus;
    private int id;
    private JLabel labelTurnOf;
    private JLabel labelTime;
    private JLabel labelScore;

    public Info(GameStatus gameStatus, int id) {
        this.gameStatus = gameStatus;
        this.id = id;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel labelPlayers = new JLabel("Players:");
        this.add(labelPlayers);
        labelTurnOf = null;
        labelTime = null;
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
            this.add(labelTurnOf);
            labelTime = new JLabel("Time:");
            this.add(labelTime);
            labelScore = new JLabel("Score: " + gameStatus.getPlayersList().get(id).getScore());
            this.add(labelScore);
        } else {
            for(int i=0; i<gameStatus.getPlayersList().size(); i++) {
                if (!gameStatus.getPlayersList().get(i).isCrashed() && gameStatus.getPlayersList().get(i).isMyTurn()) {
                    labelTurnOf = new JLabel("It's turn of: " + gameStatus.getPlayersList().get(i).getId() + " " + gameStatus.getPlayersList().get(i).getNickName());
                    this.add(labelTurnOf);
                    break;
                }
            }
        }

    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void updateScore(GameStatus gameStatus){
        for(int i=0; i<gameStatus.getPlayersList().size(); i++) {
            if(!gameStatus.getPlayersList().get(i).isCrashed()
                    && gameStatus.getPlayersList().get(i).isMyTurn()
                    && gameStatus.getPlayersList().get(i).getId() == id){

                labelScore.setText("Score: " + gameStatus.getPlayersList().get(id).getScore());
            }
        }
    }
}