package oro.watch.java.MonitorDirectoryService.Service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import oro.watch.java.MonitorDirectoryService.model.ItemData;

@Service
public class WriterService {

	private static final Logger logger = LoggerFactory.getLogger(WriterService.class);

	public void writeMTD(ItemData data) {
		logger.info("WriterService class writeMTD() method is called");
		String fileName = data.getItemName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		File file = new File(data.getItemPath() + File.separator + fileName + ".mtd");

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			bufferedWriter.write("Number of words: " + data.getWordCount());
			bufferedWriter.write("\nNumber of vowels: " + data.getVowelCount());
			bufferedWriter.write("\nNumber of special Characters: " + data.getSpecialCharCount());
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeDMTD(ItemData data) {
		logger.info("WriterService class writeDMTD() method is called");
		File file = new File(data.getItemPath() + File.separator + data.getItemName() + ".dmtd");
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			bufferedWriter.write("Total Number of words: " + data.getWordCount());
			bufferedWriter.write("\nTotal Number of vowels: " + data.getVowelCount());
			bufferedWriter.write("\nTotal Number of special Characters: " + data.getSpecialCharCount());
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeSMTD(ItemData data) {
		File file = new File(data.getItemPath() + File.separator + data.getItemName() + ".smtd");

		List<ItemData> currentDIRfiles = data.getItemData().values().stream().filter(f -> f.isFile())
				.collect(Collectors.toList());

		Collections.sort(currentDIRfiles, Comparator.comparing(ItemData::getCount));

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			for (ItemData currfile : currentDIRfiles) {
				bufferedWriter.write(currfile.getItemName());
				bufferedWriter.write("\n");
			}

			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
