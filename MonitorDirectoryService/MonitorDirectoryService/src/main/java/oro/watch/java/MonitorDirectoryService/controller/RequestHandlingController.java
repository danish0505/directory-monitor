package oro.watch.java.MonitorDirectoryService.controller;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import oro.watch.java.MonitorDirectoryService.Service.impl.MonitorServiceImpl;

@RestController
public class RequestHandlingController {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandlingController.class);
	@Autowired
	private MonitorServiceImpl monitorservice;

	@GetMapping
	public String handleRequest() throws ConfigurationException {
		logger.info("RequestHandlingController handleRequest method is called");
		monitorservice.init();
		monitorservice.start();
		return "Success";

	}

}
