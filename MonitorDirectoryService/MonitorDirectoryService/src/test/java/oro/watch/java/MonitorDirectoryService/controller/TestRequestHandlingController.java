package oro.watch.java.MonitorDirectoryService.controller;

import javax.naming.ConfigurationException;

import org.junit.Test;
import org.mockito.Mockito;
public class TestRequestHandlingController {

	@Test
	public void testhandleRequest() throws ConfigurationException {
		RequestHandlingController controller = Mockito.mock(RequestHandlingController.class);
		Mockito.when(controller.handleRequest()).thenReturn(Mockito.anyString());
		
	}
	


}
