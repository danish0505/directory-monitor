package oro.watch.java.MonitorDirectoryService.Service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import oro.watch.java.MonitorDirectoryService.Service.MonitorService;
import oro.watch.java.MonitorDirectoryService.constant.AppConstants;

@Service
public class MonitorServiceImpl implements MonitorService{
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
	
	@Autowired
	private Environment env;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private ParserService parserService;
	private List<String> fileExtensions;
	private ExecutorService executorService;
	private WatchService watcher;
	/**
     * maintain a map of watch keys and directories Map<WatchKey, Path> 
     * keys to correctly identify which directory has been modified
     */
	private Map<WatchKey, Path> keys;
	private boolean startWatch;
	private String watchDir;

	@Override
	public void init() throws ConfigurationException {
		logger.info("MonitorServiceImpl init() method is called"); 
		if(!env.containsProperty(AppConstants.WATCH_DIR_PATH_PROPERTY)) {
			throw new ConfigurationException("Watch directory not defined.");
		}
		
		watchDir = env.getProperty(AppConstants.WATCH_DIR_PATH_PROPERTY);
		
		File cacheFolder = new File(watchDir);
		if (!cacheFolder.exists()) {
			throw new ConfigurationException("Cache directory does not exist.");
		}
		
		if (!cacheFolder.isDirectory()) {
			throw new ConfigurationException("Cache directory property is not a directory.");
		}
		
		watchDir = cacheFolder.getAbsolutePath();
		
		if (env.containsProperty(AppConstants.FILE_EXTENSIONS_PROPERTY)) {
			String fileExtensionSeparator = env.containsProperty(AppConstants.FILE_EXTENSIONS_SEPARATOR_PROPERTY)
					? env.getProperty(AppConstants.FILE_EXTENSIONS_SEPARATOR_PROPERTY)
					: AppConstants.FILE_EXTENSIONS_SEPARATOR_DEFAULT;
			fileExtensions = Arrays
					.asList(env.getProperty(AppConstants.FILE_EXTENSIONS_PROPERTY).split(fileExtensionSeparator));
		} else {
			fileExtensions = Arrays.asList(AppConstants.FILE_EXTENSIONS_DEFAULT);

		}

		/**
	     * Creates a WatchService and registers the given directory
	     */
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			keys = new HashMap<WatchKey, Path>();
			executorService = Executors.newFixedThreadPool(5);
		} catch (IOException e) {
			throw new ConfigurationException(e.getMessage());
		}
		cacheService.init(env);

	}
	@Override
	public void start() {
		logger.info("MonitorServiceImpl start() method is called"); 
		try {
			this.walkAndRegisterDirectories(Paths.get(watchDir));
			startWatch = true;
			this.startWatching();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
     * Watching the existing and new directory for new file
     */
	private void startWatching() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (startWatch) 
				{
					WatchKey key;
					try{
						key = watcher.take();
					}
					catch(InterruptedException x){
						return;
					}
					Path dir = keys.get(key);
		            if (dir == null) {
		                System.err.println("WatchKey not recognized!!");
		                continue;
		            }
		            
		            for(WatchEvent<?> event : key.pollEvents())
		            {
		            	Path name = ((WatchEvent<Path>)event).context();
		                
		                Path child = dir.resolve(name);
		                if (Files.isDirectory(child)) {
		                	try {
		                		walkAndRegisterDirectories(child);
		                    } catch (IOException|InterruptedException x) {
		                        // throw runtime exception
		                    }
		                }else{
		                	if(checkFileType(name.toString())) {
		                		createCache(child);                		
		                	}
		                }
		            }
		            
		         // reset key and remove from set if directory no longer accessible
		            boolean valid = key.reset();
		            if (!valid) {
		                keys.remove(key);
		                // all directories are inaccessible
		                if (keys.isEmpty()) {
		                    break;
		                }
		            }
					
				}
				
			}
		}).start();
		
	}

	  private boolean checkFileType(String fileName) {
	    	String extension = fileName.split("\\.")[1];
			return fileExtensions.stream().anyMatch(e -> e.trim().equalsIgnoreCase(extension));
		}

	private void createCache(Path path) {
		createCache(path, true);
	}
	
	private void createCache(Path path, boolean persist){
		executorService.execute(() -> {
    		File fileFolder = path.toFile();
    		String relativePath = this.relativePath(path.toString());
    		if(fileFolder.isFile()) {
    			if(checkFileType(path.getFileName().toString())) {
    				cacheService.add(relativePath, parserService.parse(path));
        			if(persist) {    				
        				cacheService.persist(relativePath);
        			}
    			}    			 
    		} else {
    			File[] files = fileFolder.listFiles();
    			CountDownLatch latch = new CountDownLatch(files.length);
    			for(File file: files) {
    				try {
    					createCache(Paths.get(file.getAbsolutePath()), false);
					} finally {
						latch.countDown();
					}
    			}
    			try {
					latch.await();					
				} catch (Exception e) {
					//
				}
    			if(persist) {
    				cacheService.persist(relativePath);
    			}    			
    		}
		});
		
	}

	private String relativePath(String filePath) {
    	if(filePath.length() == watchDir.length()) {
    		return "";
    	}
    	return filePath.substring(watchDir.length() + 1);
    }

	/**
	 * Register the given directory with the WatchService; This function will be
	 * called by FileVisitor
	 */
	private void registerDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		createCache(dir);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 * 
	 * @throws InterruptedException
	 */
	private void walkAndRegisterDirectories(final Path start) throws IOException, InterruptedException {
		if (start.toFile().isDirectory()) {
			File[] files = start.toFile().listFiles();
			for (int index = 0; index < files.length; index++) {
				Thread.sleep(100);
				File file = files[index];
				if (file.isDirectory()) {
					walkAndRegisterDirectories(Paths.get(file.getAbsolutePath()));
				}
			}
			registerDirectory(start);
		}
	}

}
