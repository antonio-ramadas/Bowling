package logic;

public class Scoring {
	
	private class Frame
	{
		int frame_number;
		int firstChance;
		int secondChance;
		int thirdChance;	
	}
	
	
	private Frame[] scoreChart = new Frame[11];
	private int nextPlay = 0;
	
	public Scoring(){
		for (int i = 1; i <= 10 ; i++ )
		{
			scoreChart[i] = new Frame();
			scoreChart[i].frame_number = i;
			scoreChart[i].firstChance = 0;
			scoreChart[i].secondChance = 0;
			scoreChart[i].thirdChance = 0;
		}
		
		nextPlay = 1;
	}
	
	
	public int getNextPlay()
	{
		return nextPlay;
	}

	public int getPinsFelled(int PlayNumber)
	{
		if (PlayNumber < 1 || PlayNumber > 21)
			return -1;
		int FramePlay; int ChanceNumber;
		if (PlayNumber == 21)
		{
			FramePlay = 10;
			ChanceNumber = 3;
		}
		else
		{
			FramePlay = (PlayNumber+1) / 2;
			ChanceNumber = 2 - (PlayNumber % 2);
		}
		
		if (ChanceNumber == 3)
			return scoreChart[FramePlay].thirdChance;
		else if(ChanceNumber == 2)
			return scoreChart[FramePlay].secondChance;
		else
			return scoreChart[FramePlay].firstChance;
	}
	
	
}
