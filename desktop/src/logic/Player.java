package logic;

public class Player {
	
	private String name;
	private Scoring scores;
	
	/**
	 * Construtor da Classe
	 * 
	 * @param n O nome do jogador
	 * 
	 */
	public Player(String n)
	{
		name = n;
		scores = new Scoring();
	}

	/**
	 * Getter do nome do jogador
	 * 
	 * @return O nome do Jogador.
	 * 
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Setter do nome do jogador
	 * 
	 * @param n Novo nome do jogador
	 * 
	 */
	public void setName(String n){
		name = n;
	}
	
	/**
	 * Efectua uma jogada de Bowling, invocando o metodo makePlay() equivalente
	 * na tabela de Scoring. O valor de retorno indica se o jogador deve lançar
	 * mais bolas antes de acabar um Frame
	 * 
	 * @param pinsDown Numero de pinos tombado na jogada
	 * 
	 * @return False se acabou um Frame, True se continua no mesmo Frame
	 * 
	 * @see Scoring#makePlay(int pinsDown)
	 * 
	 */
	public boolean makePlay(int pinsDown){
		scores.makePlay(pinsDown);
		int jogada = scores.getNextPlay();		
		if (jogada % 2 != 0 && jogada != 21)
			return false;
		return true;
	}
	/**
	 * Getter do objecto Scoring do jogador
	 * 
	 * @see Scoring
	 *
	 * @return a Scoring do jogador
	 */
	public Scoring getScoreBoard(){
		return scores;
	}
	
}
