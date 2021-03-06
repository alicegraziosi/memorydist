package model.move;

import model.card.Card;

import java.io.Serializable;

/**
 * @desc Class representing the game move
*/
public class Move implements Serializable{

	private Card card1; /** first card of the move */
	private Card card2; /** second card of the move */
	private boolean match = false; /** match between the previous two cards */

	
	/**
     * @desc move constructor, move null
   */
	public Move() {
		this.card1 = null;
		this.card2 = null;
	}

	/**
     * @desc move constructor, when the player moves the first card
     * @param card $card1
     * @return void
   */
	public Move(Card card1) {
		this.card1 = card1;
		this.card2 = null;
	}

	/**
     * @desc move constructor, when the player moves both the cards
     * @param card $card1, card $card2
     * @return void
   */
	public Move(Card card1, Card card2) {
		this.card1 = card1;
		this.card2 = card2;
		if (card1.getValue() == card2.getValue()){
			this.match = true;
		}
	}
	
	/**
     * @desc get the first card
     * @param 
     * @return card $card1
   */
	public Card getCard1() {
		return card1;
	}
	
	/**
     * @desc get the second card
     * @param 
     * @return card $card2
   */
	public Card getCard2() {
		return card2;
	}

	public boolean isMatch() {
		return match;
	}

	@Override
	public String toString() {
		if(card2 == null){
			return "Move{" +
					"card1=" + card1.toString() +
					", card2=null" +
					", match=" + match +
					'}';
		} else {
			return "Move{" +
					"card1=" + card1.toString() +
					", card2=" + card2.toString() +
					", match=" + match +
					'}';
		}
	}
}