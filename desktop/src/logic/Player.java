package logic;

public class Player {
	
	private String name;
	private Scoring scores;
	
	public Player(String n)
	{
		name = n;
		scores = new Scoring();
	}
	

	public String getName()
	{
		return name;
	}
	public void setName(String n){
		name = n;
	}
	
	public void makePlay(int pinsDown){
		scores.makePlay(pinsDown);
	}
	public Scoring getScoreBoard(){
		return scores;
	}
	
}
