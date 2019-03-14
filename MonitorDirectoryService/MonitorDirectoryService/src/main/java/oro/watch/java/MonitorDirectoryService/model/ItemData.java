package oro.watch.java.MonitorDirectoryService.model;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import oro.watch.java.MonitorDirectoryService.constant.AppConstants;

public class ItemData {
	private String itemName;
	private long wordCount;
	private long vowelCount;
	private long specialCharCount;
	private final String itemPath;
	private final boolean isFile;
	/**
     * For every parent and child directory or file we are creating this map with value of ItemData in map
     */
	private final ConcurrentHashMap<String, ItemData> itemData;

	public ItemData(String itemPath, String itemName, long wordCount, long vowelCount, long specialCharCount,
			boolean isFile) {
		this.itemPath = itemPath;
		this.itemName = itemName;
		this.wordCount = wordCount;
		this.vowelCount = vowelCount;
		this.specialCharCount = specialCharCount;

		this.itemData = new ConcurrentHashMap<>();
		this.isFile = isFile;
	}

	public void computeAgg() {
		if (!isFile()) {
			synchronized (this) {
				this.wordCount = 0;
				this.vowelCount = 0;
				this.specialCharCount = 0;
				for (ItemData itemData : itemData.values()) {
					this.wordCount += itemData.getWordCount();
					this.vowelCount += itemData.getVowelCount();
					this.specialCharCount += itemData.getSpecialCharCount();
				}
			}
		}
	}

	public ConcurrentHashMap<String, ItemData> getChildItemData() {
		return itemData;
	}

	public boolean containsChildFolder(String childFolder) {
		return itemData.containsKey(childFolder);
	}


	public ItemData getOrAddChildFolder(String childFolder) {
		if (containsChildFolder(childFolder)) {
			return itemData.get(childFolder);
		} else {
			ItemData data = new ItemDataBuilder().setItemPath(itemPath + File.separator + childFolder)
					.setItemName(childFolder).toItemData();
			itemData.put(childFolder, data);
			return data;
		}
	}


	public void addFileData(String fileName, ItemData fileData) {
		itemData.put(fileName, fileData);
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public long getWordCount() {
		return wordCount;
	}

	public void setWordCount(long wordCount) {
		this.wordCount = wordCount;
	}

	public long getVowelCount() {
		return vowelCount;
	}

	public void setVowelCount(long vowelCount) {
		this.vowelCount = vowelCount;
	}

	public long getSpecialCharCount() {
		return specialCharCount;
	}

	public void setSpecialCharCount(long specialCharCount) {
		this.specialCharCount = specialCharCount;
	}

	public String getItemPath() {
		return itemPath;
	}

	public ConcurrentHashMap<String, ItemData> getItemData() {
		return itemData;
	}

	public boolean isFile() {
		return isFile;
	}

	public long getCount() {
		long count = 0;
		String sortingOrder = AppConstants.SORTINGORDER;
		if (!StringUtils.isEmpty(sortingOrder)) {
			switch (sortingOrder.toLowerCase()) {
			case "words":
				count = getWordCount();
				break;
			case "vowels":
				count = getVowelCount();
				break;
			case "specialChars":
				count = getSpecialCharCount();
				break;
			}
		}

		return count;
	}
}
