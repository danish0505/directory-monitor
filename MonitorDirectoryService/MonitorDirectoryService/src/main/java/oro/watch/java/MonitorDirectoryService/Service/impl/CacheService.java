package oro.watch.java.MonitorDirectoryService.Service.impl;

import java.io.File;

import javax.naming.ConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import oro.watch.java.MonitorDirectoryService.constant.AppConstants;
import oro.watch.java.MonitorDirectoryService.model.ItemData;
import oro.watch.java.MonitorDirectoryService.model.ItemDataBuilder;

@Service
public class CacheService {
	private String cacheDir;
	private String rootFolderPath;
	private ItemData cache;
	@Autowired
	private WriterService writerService;

	public void init(Environment env) throws ConfigurationException {
		cacheDir = env.getProperty(AppConstants.WATCH_DIR_PATH_PROPERTY);
		File cacheFolder = new File(cacheDir);
		rootFolderPath = cacheFolder.getAbsolutePath();
		cache = new ItemDataBuilder().setItemPath(rootFolderPath).setItemName(cacheFolder.getName()).toItemData();
	}

	public void persist(String key) {
		ItemData cacheFolder = cache;
		if (!StringUtils.isEmpty(key)) {
			String[] filePart = splitByFileSeparator(key);
			for (int index = 0; index < filePart.length - 1; index++) {
				cacheFolder = cacheFolder.getOrAddChildFolder(filePart[index]);
			}
			cacheFolder.getOrAddChildFolder(filePart[filePart.length - 1]);
		}

	}

	private String[] splitByFileSeparator(String key) {
		return key.split(AppConstants.FILE_SEPARATOR_PATTERN);
	}

	public void add(String key, ItemData itemData) {
		String[] filePart = splitByFileSeparator(key);
		ItemData cacheFolder = cache;
		for (int index = 0; index < filePart.length - 1; index++) {
			cacheFolder = cacheFolder.getOrAddChildFolder(filePart[index]);
		}
		cacheFolder.addFileData(filePart[filePart.length - 1], itemData);

		this.compute(filePart, cache, 0);

	}

	/**
     * Using this method to write into the different type of files.
     */
	private void compute(String[] filePart, ItemData cacheFolder, int index) {
		if (index < filePart.length) {
			compute(filePart, cacheFolder.getOrAddChildFolder(filePart[index]), ++index);
			cacheFolder.computeAgg();
			if (cacheFolder.isFile()) {
				writerService.writeMTD(cacheFolder);
			} else {
				for (ItemData item : cacheFolder.getChildItemData().values()) {
					if (item.isFile()) {
						writerService.writeMTD(item);
					}
				}
				writerService.writeDMTD(cacheFolder);
				writerService.writeSMTD(cacheFolder);
			}
		}
	}

}
