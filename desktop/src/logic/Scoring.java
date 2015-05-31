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

	public boolean makePlay(int pinsDown)
	{
		if (nextPlay < 1 || nextPlay > 21 || pinsDown < 0 || pinsDown > 10)
			return false;

		if (nextPlay == 20)
		{
			scoreChart[10].secondChance = pinsDown;
			if (scoreChart[10].firstChance == 10 || (scoreChart[10].secondChance+scoreChart[10].secondChance) == 10)
				nextPlay++;
			else
				nextPlay = -1;
			return true;
		}

		if (nextPlay == 21)
		{
			scoreChart[10].thirdChance = pinsDown;
			nextPlay = -1;
			return true;
		}

		int FramePlay; int ChanceNumber;
		FramePlay = (nextPlay+1) / 2;
		ChanceNumber = 2 - (nextPlay % 2);


		if(ChanceNumber == 2)
			scoreChart[FramePlay].secondChance = pinsDown;
		else
		{
			scoreChart[FramePlay].firstChance = pinsDown;
			if (pinsDown == 10 && nextPlay != 19) //Strike, skip the second play
				nextPlay++;			
		}	
		nextPlay++;	

		return true;
	}

	public void restartScoring()
	{
		for (int i = 1; i <= 10; i++)
		{
			scoreChart[i].firstChance = 0;
			scoreChart[i].secondChance = 0;
			scoreChart[i].thirdChance = 0;
			nextPlay = 1;
		}
	}

	public int latestScoredFrame()
	{
		if (nextPlay == -1)
			return 10;
		if (nextPlay == 21)
			return 9;

		int FramePlay;
		FramePlay = (nextPlay+1) / 2;

		return FramePlay-1;

	}

	public int getScoreFrame(int frameNumber)
	{
		if (frameNumber > 10 || frameNumber < 0 || frameNumber > latestScoredFrame())
			return -1;

		int score = 0;

		if (frameNumber < 9)
		{
			score += scoreChart[frameNumber].firstChance;
			if (score == 10)// Strike
			{ 
				score += scoreChart[frameNumber+1].firstChance;
				if(score == 20) //Nextframe was a strike
				{
					score += scoreChart[frameNumber+2].firstChance;
				} else
				{
					score += scoreChart[frameNumber+1].secondChance;
				}
			} else //Not a Strike
			{ 
				score += scoreChart[frameNumber].secondChance;
				if (score == 10) //First Spare
				{
					score += scoreChart[frameNumber+1].firstChance;
				}
			}
		}

		if (frameNumber == 9)
		{
			score += scoreChart[frameNumber].firstChance;
			if (score == 10)// Strike
			{ 
				score += scoreChart[frameNumber+1].firstChance;
				score += scoreChart[frameNumber+1].secondChance;
			} else //Not a Strike
			{ 
				score += scoreChart[frameNumber].secondChance;
				if (score == 10) //First Spare
				{
					score += scoreChart[frameNumber+1].firstChance;
				}
			}
		}
		
		if (frameNumber == 10)
		{
			score += scoreChart[frameNumber].firstChance;
			if (score == 10)// Strike
			{ 
				score += scoreChart[frameNumber].secondChance;
				score += scoreChart[frameNumber].thirdChance;
			} else 
			{ 
				score += scoreChart[frameNumber].secondChance;
				if (score == 10) // Spare
					score += scoreChart[frameNumber].thirdChance;
			}
		}


		return score;
	}


}
