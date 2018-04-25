package model.player;

public class CurrentPlayer {
	
	private int id;
	private String nickname;
	private String host;
		
    private static CurrentPlayer instance = null;
   
    public static CurrentPlayer getInstance() {
	   if(instance == null) instance = new CurrentPlayer();
	   return instance;
    }
	
	protected CurrentPlayer() {}

}