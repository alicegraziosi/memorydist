/**
Class representing the Card object
*/
public class Card {
	
	private int index;
	private int value;
	
	/**
	 * constructor
	 * @param index, value
	 */
	public Card(int index, int value){
		this.index = index;
		this.value = value;
	}
	/**
	 * Set index
	 * @param index
	 */
	public setIndex(int index){
		this.index = index;
	}
	/**
	 * Set value
	 * @param value
	 */
	public setValue(int value){
		this.value = value;
	}
	/**
	 * get index
	 * @return index
	 */
	public int getIndex(){
		return this.index;
	}
	/**
	 * get value
	 * @return value
	 */
	public int getValue() {
		return this.value;
	}
}
