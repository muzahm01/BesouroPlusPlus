package besouro.integration;

import junit.framework.Assert;

import org.junit.Test;

import besouro.classification.zorro.ZorroEpisodeClassifierStream;
import besouro.classification.zorro.ZorroTDDConformance;

public class TDDMeasureTest extends IntegrationTestBaseClass {

	@Test
	public void testIncrementalMeasure() throws Exception {
	
		addTestFirst1Actions();
		Assert.assertEquals(1, ((ZorroEpisodeClassifierStream)stream).getTDDMeasure().getTDDPercentageByNumber(), 0.01);
		
		addRefactoring1A_Actions();
		Assert.assertEquals(1, ((ZorroEpisodeClassifierStream)stream).getTDDMeasure().getTDDPercentageByNumber(), 0.01);
		
		addTestLast1Actions();
		Assert.assertEquals(2f/3f, ((ZorroEpisodeClassifierStream)stream).getTDDMeasure().getTDDPercentageByNumber(), 0.01);
		
		addRefactoring1A_Actions();
		Assert.assertEquals(2f/4f, ((ZorroEpisodeClassifierStream)stream).getTDDMeasure().getTDDPercentageByNumber(), 0.01);
		
		
	}

	
}
