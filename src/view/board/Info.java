package view.board;

import model.gameStatus.GameStatus;

import javax.swing.*;
import java.awt.*;

class Info extends JPanel {

    private GameStatus gameStatus;

    public Info(GameStatus gameStatus) {

        this.gameStatus = gameStatus;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel labelPlayers = new JLabel("Players:");
        this.add(labelPlayers);
        JLabel labelTurnOf = null;
        for(int i=0; i<gameStatus.getPlayersList().size(); i++){
            if(!gameStatus.getPlayersList().get(i).isCrashed()) {
                JLabel labelPlayerNickname = new JLabel("Nickname: " + gameStatus.getPlayersList().get(i).getNickName());
                this.add(labelPlayerNickname);
                JLabel labelPlayerScore = new JLabel("Score: " + gameStatus.getPlayersList().get(i).getScore());
                this.add(labelPlayerScore);

                if (gameStatus.getPlayersList().get(i).isMyTurn()) {
                    labelTurnOf = new JLabel("It's turn of: " + gameStatus.getPlayersList().get(i).getNickName());
                }
            }
        }

        this.add(labelTurnOf);

        JLabel labelTime = new JLabel("Time:");
        this.add(labelTime);
        JLabel labelScore = new JLabel("Score:");
        this.add(labelScore);
    }
}