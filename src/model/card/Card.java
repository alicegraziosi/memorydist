package model.card;

/**
 Class representing the Card object
 */
public class Card {

	private int index;

    private int value;

	public Card(int index, int value){
		this.index = index;
		this.value = value;
	}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}