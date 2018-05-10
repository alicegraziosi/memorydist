package model.card;

import java.io.Serializable;

/**
 * @desc Class representing the Card object
 */
public class Card implements Serializable{

	private int index; /** index of a card */
	private int value; /** value of a card */
    
    /**
     * @desc card constructor
     * @paramint int $index, int $value
     * @return void
   */
	public Card(int index, int value){
		this.index = index;
		this.value = value;
	}
	
	/**
	  * @desc get the index of a card
	  * @return int $index
	*/
    public int getIndex() {
        return index;
    }
    
    /**
     * @desc get value of a card
     * @return int $value of a card
   */
    public int getValue() {
        return value;
    }

	/**
	 * @desc set to string card
	 */
	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");
		return "Card " +" { " +
				"index=" + index +
				", value='" + value +
				" }" + newLine;
	}
}