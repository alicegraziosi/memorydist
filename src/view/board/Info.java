package view.board;

import javax.swing.*;
import java.awt.*;

class Info extends JPanel {

    public Info() {
        JLabel labelPlayers = new JLabel("Players:");
        JLabel labelTurnOf = new JLabel("It's turn of: ");
        JLabel labelYourTurn = new JLabel("It's your turn.");
        JLabel labelTime = new JLabel("Time:");
        JLabel labelScore = new JLabel("Score:");

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(labelPlayers);
        this.add(labelTurnOf);
        this.add(labelYourTurn);
        this.add(labelTime);
        this.add(labelScore);
    }
}