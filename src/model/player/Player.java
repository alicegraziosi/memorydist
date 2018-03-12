package model.player;

import java.io.Serializable;
import java.util.ArrayList;

import model.player.PLAYER_STATE;
import utils.Node;
import java.net.InetAddress;

/**
 * @desc Class representing the memory player
 */
public class Player extends Node implements Serializable{

	private String nickName; /** player nickname*/
    private PLAYER_STATE state; /** player state (ACTIVE, WINNER, CRASH)*/
    private int score; /** player score*/
    
    /** GIANLUCA E' in DUBBIO SU GLI ATTRIBUTI DI QUI SOTTO*/
    private boolean isLeader; /** true if player is the leader, otherwise false*/
    private ArrayList<Player> playerList; /** player list in the game*/

    /** constructor to use in case of first initialization of object Player, calls the constructor of the Node class*/
    public Player(int id, String nickName, InetAddress host, int port) {
       super(host, port);
       this.nickName = nickName;
       this.state = PLAYER_STATE.ACTIVE;
       this.score = 0;
    }
	
	/**
	  * @desc get the nickName of the player
	  * @return String $nickName
	*/
	public String getNickName() {
		return nickName;
	}

	/**
     * @desc set nickName of a player
     * @param int $nick-name
     * @return void
   */
	public void setNickName(String nickName) {
		this.nickName = nickName;
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
	
	/**
	  * @desc get the player list
	  * @return array list of player $playerList
	*/
	public ArrayList<Player> getPlayerList() {
		return playerList;
	}
	
	/**
     * @desc set playerlist
     * @param array list  $playerList
     * @return void
   */
	public void setPlayerList(ArrayList<Player> playerList) {
		this.playerList = playerList;
	}



}
