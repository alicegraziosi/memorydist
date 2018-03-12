package model.card;

import java.io.Serializable;

/**
 Class representing the Card object
 */
public class Card implements Serializable{

	private int index; /** index of a card */
	private int value; /** value of a card */
    
    /**
     * @desc card constructor
     * @param int $index, int $value
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
     * @desc set index of a card
     * @param int $index
     * @return void
   */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * @desc get value of a card
     * @return int $value of a card
   */
    public int getValue() {
        return value;
    }

    /**
     * @desc set value of a card
     * @param int $value
     * @return void
   */
    public void setValue(int value) {
        this.value = value;
    }
}