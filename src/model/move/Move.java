/**
Class representing the Move object
*/
class Move{
	
	private Card card1;
	private Card card2;
	private Bool match = false;
	
	/**
	Constructor for move that involves only one card
	* @param card1
	*/
	public Move(Card card1) {
		this.card1 = card1;
		this.card2 = null;
	}
	
	/**
	Constructor for move that involves two cards
	* @param card1, card2
	*/
	public Move(Card card1, Card card2) {
		this.card1 = card1;
		this.card2 = card2;
	}
	/**
	 * Set card1
	 * @param card1
	 */
	public setCard1(Card card1){
		this.card1 = card1;
	}
	/**
	 * Set card2
	 * @param card2
	 */
	public setCard2(Card card2){
		this.card2 = card2;
	}
	/**
	 * Set match
	 * @param match
	 */
	public setMatch(Bool match) {
		this.match = match;
	}
	/**
	 * Get card1
	 * @return card1
	 */
	public Card getCard1(){
		return this.card1;
	}
	/**
	 * Get card2
	 * @return card2
	 */
	public Card getCard2(){
		return this.card2;
	}
	/**
	 * Get match
	 * @return match
	 */
	public Bool getMatch() {
		return this.match;
	}
	/**
	 * compute if tow cards matches
	 * @param card1, card2
	 * @return match
	 */
	public computeMatch(Card card1, Card card2) {
		if (card1.value == card2.value){
			match = true;
		}
	}
}