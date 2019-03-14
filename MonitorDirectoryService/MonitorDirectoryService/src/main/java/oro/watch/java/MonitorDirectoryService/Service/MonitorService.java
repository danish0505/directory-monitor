package oro.watch.java.MonitorDirectoryService.Service;

import javax.naming.ConfigurationException;

public interface MonitorService {
	void init() throws ConfigurationException;
	void start();
}
