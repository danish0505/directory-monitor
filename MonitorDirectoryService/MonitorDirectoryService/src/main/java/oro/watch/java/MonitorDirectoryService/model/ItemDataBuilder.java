package oro.watch.java.MonitorDirectoryService.model;

public class ItemDataBuilder {
	
	private String itemName;
	private long wordCount;
	private long vowelCount;
	private long specialCharCount;
	private String itemPath;
	private boolean file;
	
	public ItemDataBuilder setItemName(String itemName) {
		this.itemName = itemName;
		return this; 
	}
	
	public ItemDataBuilder setWordCount(long wordCount) {
		this.wordCount = wordCount;
		return this;
	}
	
	public ItemDataBuilder setVowelCount(long vowelCount) {
		this.vowelCount = vowelCount;
		return this;
	}
	
	public ItemDataBuilder setSpecialCharCount(long specialCharCount) {
		this.specialCharCount = specialCharCount;
		return this;
	}
	
	public ItemDataBuilder setItemPath(String itemPath) {
		this.itemPath = itemPath;
		return this;
	}
	
	public ItemDataBuilder setFile(boolean file) {
		this.file = file;
		return this;
	}
	
	public ItemData toItemData() {
		return new ItemData(itemPath, itemName, wordCount, vowelCount, specialCharCount, file);
	}
	

}
