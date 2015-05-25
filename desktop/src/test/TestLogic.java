package test;

import static org.junit.Assert.*;

import org.junit.Test;
import logic.*;

public class TestLogic {

	@Test
	public void test_ScoreTableCreation() {
		Scoring test = new Scoring();
		for (int i = 1; i <= 21; i++ )
			assertEquals("Sucesso!",  test.getPinsFelled(i), 0);
		
		assertEquals("Sucesso!",  test.getPinsFelled(0), -1);
		assertEquals("Sucesso!",  test.getPinsFelled(22), -1);
		
	}

}
