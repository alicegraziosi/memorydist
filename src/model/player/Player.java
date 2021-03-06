package model.player;

import java.io.Serializable;

/**
 * @desc Class representing the memory player
 */
public class Player extends Node implements Serializable{

	private int id; /** player id*/
	private String nickName; /** player nickname*/
    private PLAYER_STATE state; /** player state (ACTIVE, WINNER, CRASH)*/
    private int score;

	/** player score*/
    private boolean isMyTurn;
    
	/** constructor to use in case of first initialization of object Player, calls the constructor of the Node class*/
    public Player(int id, String nickName, String host, int port) {
       super(host, port);
       this.id = id;
       this.nickName = nickName;
       this.state = PLAYER_STATE.ACTIVE;
       this.score = 0;
    }
    
    /**
	  * @desc check if player is crashed
	  * @return bool 
	*/
    public Boolean isCrashed() {
    	if(this.state == PLAYER_STATE.CRASH)
    		return true;
    	else
    		return false;

    }
    
    /**
	  * @desc set the player state as crashed
	  * @param boolean $crashed
	*/
    public void setCrashed(Boolean crashed) {
    	if (crashed == true)
    		this.state = PLAYER_STATE.CRASH;
    }
	
    /**
	  * @desc get the id of the player
	  * @return int $id
	*/
    public int getId() {
		return id;
	}

	/**
	  * @desc get the nickName of the player
	  * @return String $nickName
	*/
	public String getNickName() {
		return nickName;
	}
	
	/**
	  * @desc get the state of the player
	  * @return PLAYER_STATE $state
	*/
	public PLAYER_STATE getState() {
		return state;
	}

	/**
     * @desc set state of a player
     * @param PLAYER_STATE $state
     * @return void
   */
	public void setState(PLAYER_STATE state) {
		this.state = state;
	}
	
	/**
	  * @desc get the score of the player
	  * @return int $score
	*/
	public int getScore() {
		return score;
	}
	
	/**
     * @desc set score of a player
     * @param int $score
     * @return void
   */
	public void setScore(int score) {
		this.score = score;
	}

	public void setMyTurn(boolean myTurn) {
		isMyTurn = myTurn;
	}

	/**
	 * @desc set to string player info
	 */
	@Override
	public String toString() {
		return "Player " + id + " { " +
				"id=" + id +
				", nickName='" + nickName +
				", state=" + state +
				", score=" + score +
				", host=" + host +
				", port=" + port +
				" }";
	}

	public void updateScore(){
		this.setScore(this.getScore()+2);
	}
}
