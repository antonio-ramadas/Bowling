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
		
	}

}
