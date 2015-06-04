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
	
	public boolean makePlay(int pinsDown){
		scores.makePlay(pinsDown);
		int jogada = scores.getNextPlay();
		if (jogada % 2 != 0 && jogada != 21)
			return false;
		return true;
	}
	public Scoring getScoreBoard(){
		return scores;
	}
	
}
