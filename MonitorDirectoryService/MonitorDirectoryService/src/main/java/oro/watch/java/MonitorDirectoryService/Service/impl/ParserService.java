package oro.watch.java.MonitorDirectoryService.Service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import oro.watch.java.MonitorDirectoryService.constant.AppConstants;
import oro.watch.java.MonitorDirectoryService.model.ItemData;
import oro.watch.java.MonitorDirectoryService.model.ItemDataBuilder;

@Service
public class ParserService {
	private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

	/**
     * Prepare the Itemdata object of each file path object passed
     */
	public ItemData parse(Path path) {
		logger.info("ParserService class parse() method is called");
		return new ItemDataBuilder().setItemPath(path.toFile().getParent())
				                    .setItemName(path.toFile().getName())   
				                    .setWordCount(getWordCountInFile(path))
				                    .setVowelCount(getVowelsCountInFile(path))
				                    .setSpecialCharCount(getSpecialCharactersCountInFile(path))
				                    .setFile(true).toItemData();
	}
	
	public long getWordCountInFile(Path path) {
		logger.info("ParserService class getWordCountInFile() method is called");
		try {
			return Files.lines(path)
					.flatMap(str -> Stream.of(str.split("[ ,.!?\r\n]")))
					.filter(s -> s.length() > 0).count();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;

	}

	public long getVowelsCountInFile(Path path) {
		logger.info("ParserService class getVowelsCountInFile() method is called");
		return getCountOfChars(AppConstants.VOWELS, path);
	}
	

	private long getCountOfChars(List<Character> charsToFilter, Path path) {
		try {
			return Files
					.lines(path)
					.flatMap(str -> Stream.of(str.split("[ ,.!?\r\n]")))
					.flatMap(str -> str.toLowerCase().chars()
									.mapToObj(c -> (char) c)
									.collect(Collectors.toList()).stream())
					.filter(x -> charsToFilter.contains(x)).count();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}
	public long getSpecialCharactersCountInFile(Path path) {
		return getCountOfChars(AppConstants.SPECIALCHARS, path);
	}


}
