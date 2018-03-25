package view.board;

import model.card.Card;
import model.gameStatus.GameStatus;
import model.player.Player;

import java.util.ArrayList;
import java.util.Collections;

// da cancellare è solo per provare la grafica della board senza dover lanciare il gioco
public class ProveStartBoard {
    public static void main(String[] args) {
        ArrayList<Player> players = new ArrayList<>();
        Player player1 = new Player(1, "Nick", null, 0);
        player1.setMyTurn(true);
        players.add(player1);
        Player player2 = new Player(2, "Tom", null, 0);
        players.add(player2);
        Player player3 = new Player(3, "Sam", null, 0);
        players.add(player3);

        ArrayList<Card> showingCards = new ArrayList<>(); // no one
        ArrayList<Card> notShowingCards = new ArrayList<Card>(); // all

        // card generation
        for (int i = 0; i < 20; i++) {
            if (i >= 10) { // if true generate cards with same values of previous cards, (10,0), (11,1)
                Card card = new Card(i, i - 10);
                notShowingCards.add(card);
            } else { // generate cards with this pattern: (0,0), (1,1)
                Card card = new Card(i, i);
                notShowingCards.add(card);
            }
        }
        Collections.shuffle(notShowingCards);

        GameStatus gameStatus = new GameStatus(players, -1, showingCards, notShowingCards, null, null);
        Board board = new Board(gameStatus);
        board.init();
    }
}
