package model.move;

import model.card.Card;

// Mossa fatta da un giocatore
public class Move{

	private Card card1;
	private Card card2;
	private boolean match = false;

	// quando il giocatore scopre la prima carta
	public Move(Card card1) {
		this.card1 = card1;
		this.card2 = null;
	}

	//quando il giocatore scopre due carte
	public Move(Card card1, Card card2) {
		this.card1 = card1;
		this.card2 = card2;
		if (card1.getValue() == card2.getValue()){
			this.match = true;
		}
	}

	public void match(Card card1, Card card2) {
		if (card1.getValue() == card2.getValue()){
			this.match = true;
		}
	}

	public Card getCard1() {
		return card1;
	}

	public void setCard1(Card card1) {
		this.card1 = card1;
	}

	public Card getCard2() {
		return card2;
	}

	public void setCard2(Card card2) {
		this.card2 = card2;
	}
}