package model.move;

import model.card.Card;

/**
 * @desc Class representing the game move
*/
public class Move{

	private Card card1; /** first card of the move */
	private Card card2;

	/** second card of the move */
	private boolean match = false; /** match between the previous two cards */


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
     * @desc check if two cards match or not setting match 
     * @param card $card1, card $card2
     * @return void
   */
	public void match(Card card1, Card card2) {
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
     * @desc set the first card
     * @param card $card1
     * @return void
   */
	public void setCard1(Card card1) {
		this.card1 = card1;
	}
	
	/**
     * @desc get the second card
     * @param 
     * @return card $card2
   */
	public Card getCard2() {
		return card2;
	}

	/**
     * @desc set the second card
     * @param card $card2
     * @return void
   */
	public void setCard2(Card card2) {
		this.card2 = card2;
	}

	public boolean isMatch() {
		return match;
	}
}