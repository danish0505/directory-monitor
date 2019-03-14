package oro.watch.java.MonitorDirectoryService.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> genericExceptionHandler(HttpRequest request, Exception ex){
		logger.info("Exception Has occurred");
		
		return new ResponseEntity<Object>("Exception Occurred", HttpStatus.BAD_REQUEST);
		
	}

}
