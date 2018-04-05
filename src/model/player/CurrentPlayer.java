package model.player;

public class CurrentPlayer {

	/**
	 * 
	 */
	
	private int id;
	private String nickname;
	private String host;
		
    private static CurrentPlayer instance = null;
   
    public static CurrentPlayer getInstance() {
	   if(instance == null) instance = new CurrentPlayer();
	   return instance;
    }
	
	protected CurrentPlayer() {}

	public int getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public String getHost() {
		return host;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setHost(String host) {
		this.host = host;
	}
		

}