package logic;

public class Scoring {

	
	private class Frame
	{
		int firstChance;
		int secondChance;
		int thirdChance;	
	}


	private Frame[] scoreChart = new Frame[11];
	private int nextPlay = 0;
	private int lastPlay = 0;

	/**
	 * Indica qual a jogada anterior. Util devido a Strikes avançarem duas jogadas
	 *
	 * @see getNextPlay()
	 * @see makePlay(int pinsDown)
	 * 
	 * @return lastPlay 
	 */
	public int getLastPlay() {
		return lastPlay;
	}


	/**
	 * Construtor da Classe
	 *
	 */
	public Scoring(){
		for (int i = 1; i <= 10 ; i++ )
		{
			scoreChart[i] = new Frame();
			scoreChart[i].firstChance = 0;
			scoreChart[i].secondChance = 0;
			scoreChart[i].thirdChance = 0;
		}

		nextPlay = 1;
	}


	/**
	 * Indica qual o numero da próxima jogada. Util para não ter de calcular Strikes
	 * ou situações excepcionais (Frame 10) fora desta classe
	 * 
	 * 
	 * @see getLastPlay()
	 * @see makePlay(int pinsDown)
	 * 
	 * @return nextPlay
	 */
	public int getNextPlay()
	{
		return nextPlay;
	}

	/**
	 * Devolve o numero de pinos deitados abaixo numa jogada.
	 * Os pinos NÃO a pontuação.
	 *
	 * @param PlayNumber  O numero da jogada, 1 a 21.
	 *
	 * @see makePlay(int pinsDown)
	 *
	 * @return Pinos caidos
	 */
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

	/**
	 * Efectua uma jogada, efectuando um update a todos os campos da Classe
	 * 
	 * @param pinsDown pinosc caidos
	 *
	 * @return see o jogo acabou
	 */
	public boolean makePlay(int pinsDown)
	{
		if (nextPlay < 1 || nextPlay > 21 || pinsDown < 0 || pinsDown > 10)
			return false;

		if (nextPlay == 20)
		{
			scoreChart[10].secondChance = pinsDown;
			if (scoreChart[10].firstChance == 10 || (scoreChart[10].secondChance+scoreChart[10].secondChance) == 10)
			{
				lastPlay = nextPlay;
				nextPlay++;
			}
			else
			{
				lastPlay = nextPlay;
				nextPlay = -1;
			}
			return true;
		}

		if (nextPlay == 21)
		{
			scoreChart[10].thirdChance = pinsDown;
			lastPlay = nextPlay;
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
			{
				lastPlay = nextPlay;
				nextPlay++;			
			}
		}	
		lastPlay = nextPlay;
		nextPlay++;	

		return true;
	}

	/**
	 * Recomeça o valor de todos os campos para o encontrado na contrução inicial
	 *
	 */
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

	/**
	 * 
	 * Indica qual o ultimo frame que foi pontuado. 
	 * Não o numero da jogada.
	 * 
	 * @see  getScoreFrame(int frameNumber)
	 * 
	 * @return ultimo frame com pontuação
	 */
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

	/**
	 * 
	 * Devolve a pontuação de um Frame. 
	 * Este valor faz update com mais jogadas efectuadas.
	 *
	 * @param frameNumber O numero do frame
	 * 
	 * @see latestScoredFrame()
	 * @see getTotalScore()
	 * 
	 * @return A pontuação de um frame
	 */
	public int getScoreFrame(int frameNumber)
	{
		if (frameNumber > 10 || frameNumber < 0 || frameNumber > latestScoredFrame())
			return 0;

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

	/**
	 * Recebe toda e completa pontução de jogo - o somatório dos 10 Frames.
	 * Este valor faz updates com mais jogadas e deve ser verificado constantemente.
	 *
	 * @return Recebe toda e completa pontução de jogo
	 */
	public int getTotalScore()
	{
		int sum = 0;
		for (int i = 1; i <= 10; i++)
		{
			sum += getScoreFrame(i);
		}
		return sum;
	}
}
