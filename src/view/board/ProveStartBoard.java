package view.board;

import model.card.Card;
import model.gameStatus.GameStatus;

import java.util.ArrayList;
import java.util.Collections;

// da cancellare è solo per provare la grafica della board senza dover lanciare il gioco
public class ProveStartBoard {
    public static void main(String[] args) {
        ArrayList<Card> showingCards = new ArrayList<Card>(); // no one
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

        GameStatus gameStatus = new GameStatus(null, -1,showingCards, notShowingCards, null);
        Board board = new Board(gameStatus);
        board.init();
    }
}
