package oro.watch.java.MonitorDirectoryService.Service.impl;

import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Mockito;

import oro.watch.java.MonitorDirectoryService.model.ItemData;

public class ParserServiceTest {
	
	@Test
	public void parseTest(){
		ParserService parserService = Mockito.mock(ParserService.class);
		Mockito.when(parserService.parse(Mockito.mock(Path.class))).thenReturn(Mockito.mock(ItemData.class));
		
	}
	
}
