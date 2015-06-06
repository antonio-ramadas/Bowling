package test;

import static org.junit.Assert.*;

import org.junit.Test;
import logic.*;

public class TestLogic {

	@Test
	public void test_ScoreTable() {
		Scoring test = new Scoring();
		for (int i = 1; i <= 21; i++ )
			assertEquals("Sucesso!",  test.getPinsFelled(i), 0);
		assertEquals("Sucesso!",  test.getPinsFelled(0), -1);
		assertEquals("Sucesso!",  test.getPinsFelled(22), -1);
		assertEquals(test.makePlay(-1), false);
		assertEquals(test.makePlay(11), false);
		for (int i = 1; i <= 20; i++)
		{
			assertEquals(test.makePlay(1), true);
		}
		assertEquals(test.makePlay(0), false);
		
		test.restartScoring();
		for (int i = 1; i <= 10; i++)
		{
			assertEquals(test.latestScoredFrame(), i-1);
			assertEquals(test.makePlay(10), true);
		}
		assertEquals(test.latestScoredFrame(), 9);
		assertEquals(test.makePlay(10), true);
		assertEquals(test.latestScoredFrame(), 9);
		assertEquals(test.makePlay(10), true);
		assertEquals(test.latestScoredFrame(), 10);
		assertEquals(test.makePlay(10), false);
		int expectedscore = 0;
		for (int i = 1; i <= 10; i++)
		{
			expectedscore += test.getScoreFrame(i); 
			assertEquals(i*30, expectedscore);
		}
		
		
		test.restartScoring();
		assertEquals(test.makePlay(1), true); //1
		assertEquals(test.makePlay(1), true);
		assertEquals(2, test.getScoreFrame(1));
		assertEquals(test.makePlay(1), true); //2
		assertEquals(test.makePlay(9), true);
		assertEquals(10, test.getScoreFrame(2));
		assertEquals(test.makePlay(1), true); //3
		assertEquals(test.makePlay(1), true);
		assertEquals(11, test.getScoreFrame(2));
		assertEquals(2, test.getScoreFrame(3));
		assertEquals(test.makePlay(10), true); //4
		assertEquals(test.makePlay(10), true); //5
		assertEquals(test.makePlay(1), true); //6
		assertEquals(test.makePlay(1), true);
		assertEquals(21, test.getScoreFrame(4));
		assertEquals(12, test.getScoreFrame(5));
		assertEquals(2+11+2+22+11+2, test.getTotalScore());
		assertEquals(test.makePlay(10), true);//7
		assertEquals(test.makePlay(10), true);//8
		assertEquals(20, test.getScoreFrame(7));
		assertEquals(10, test.getScoreFrame(8));
		assertEquals(50+20+10, test.getTotalScore());
		assertEquals(test.makePlay(10), true);//9
		assertEquals(test.makePlay(10), true);//10
		assertEquals(test.makePlay(10), true);
		assertEquals(test.makePlay(10), true);
		assertEquals(30, test.getScoreFrame(10));
		assertEquals((10+20)+80+30+30, test.getTotalScore());
		assertEquals(test.makePlay(10), false);//-1
	}

	@Test
	public void test_Player()
	{
		Player test = new Player("TEST");
		assertEquals("TEST", test.getName());
		test.setName("test");
		assertEquals("test", test.getName());
		
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//1
		assertEquals(2, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//2
		assertEquals(4, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//3
		assertEquals(6, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//4
		assertEquals(8, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//5
		assertEquals(10, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//6
		assertEquals(12, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//7
		assertEquals(14, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//8
		assertEquals(16, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//9
		assertEquals(18, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//10
		assertEquals(20, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),false);
		
		test = new Player("TEST");

		assertEquals(test.makePlay(-1),false);
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(9),false);	//1
		assertEquals(10, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(10),false);										//2
		assertEquals(30, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//3
		assertEquals(34, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//4
		assertEquals(36, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//5
		assertEquals(38, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//6
		assertEquals(40, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//7
		assertEquals(42, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//8
		assertEquals(44, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(1),true);assertEquals(test.makePlay(1),false);	//9
		assertEquals(46, test.getScoreBoard().getTotalScore());
		assertEquals(test.makePlay(10),true);										//10
		assertEquals(test.makePlay(10),true);
		assertEquals(test.makePlay(10),false);
		assertEquals(76, test.getScoreBoard().getTotalScore()); 
		assertEquals(test.makePlay(1),false);
		
		
		
		
	}
}
