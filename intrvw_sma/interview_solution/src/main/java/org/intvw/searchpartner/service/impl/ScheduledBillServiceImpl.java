/* *************************************************************
 *
 * COPYRIGHT (c) 2018-19 Saurabh Agrawal
 * All Rights Reserved. Saurabh Agrawal - Confidential.
 *
 * Date : Aug 04, 2018
 *
 * *************************************************************/
package org.intvw.searchpartner.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intvw.searchpartner.converter.factory.ConverterServiceFactory;
import org.intvw.searchpartner.converter.service.ConverterService;
import org.intvw.searchpartner.helper.TikaFileTypeDetector;
import org.intvw.searchpartner.model.Branch;
import org.intvw.searchpartner.model.CmFoodChain;
import org.intvw.searchpartner.model.OrderDetail;
import org.intvw.searchpartner.model.Orders;
import org.intvw.searchpartner.service.ScheduledBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * class implementing the scheduled bill service
 * 
 * @author saurabh_agrawal
 *
 */
@Component
public class ScheduledBillServiceImpl implements ScheduledBillService {
	public static final Logger logger = LogManager.getLogger(ScheduledBillService.class);

	private static final String RESTAURANT_INPUT_BILLS_LOCATION = "restaurant.input.bills.location";
	private static final String ENGINE_ANALYSIS_REPORT_LOCATION = "engine.analysis.report.location";

	private CmFoodChain restaurantsWithMistmatch = new CmFoodChain();
	private CmFoodChain restaurantsWithMatch = new CmFoodChain();

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private Environment env;

	@Scheduled(cron = "0 * * * * ?")
	public void scheduleTaskWithCronExpression() {
		String sourceFolderPath = env.getProperty(RESTAURANT_INPUT_BILLS_LOCATION);

		if (sourceFolderPath.equals("")) {
			logger.error("Source Folder Path is blank, Cannot proceed!");
		}

		File folder = new File(sourceFolderPath);
		processAllFilesInSourceFolder(folder);
		generateReports();
	}

	private void processAllFilesInSourceFolder(final File folder) {
		for (final File file : folder.listFiles()) {
			if (file.isDirectory()) {
				processAllFilesInSourceFolder(file);
			} else {
				checkCurrentFile(file);
			}
		}
	}

	private void generateReports() {
		ConverterService service = ConverterServiceFactory.getServiceInstance("application/json");

		if (service == null) {
			logger.error("Error while instantiating Converter service. ");
			return;
		}

		String matchedData = service.serialize(restaurantsWithMatch);
		logger.debug("Restaurants with matched data: {}", matchedData);
		writeToOutputFile(matchedData, "Match.json");

		String mismatchedData = service.serialize(restaurantsWithMistmatch);
		logger.debug("Restaurants with matched data: {}", mismatchedData);
		writeToOutputFile(mismatchedData, "Mismatch.json");
	}

	private void writeToOutputFile(String outputData, String outFileName) {
		try (FileWriter file = new FileWriter(ENGINE_ANALYSIS_REPORT_LOCATION + outFileName)) {
			file.write(outputData);
		} catch (IOException e) {
			logger.error("Error rgenerating report file: {}", outFileName);
		}
	}

	private void checkCurrentFile(final File file) {
		try {
			String fileType = determineFileType(file.getAbsolutePath());
			logger.debug("File type of current file is: {}", fileType);

			CmFoodChain foodChain = deserializeFile(fileType, file);

			if (foodChain == null) {
				logger.error("De-serialization failed for file: {}", file.getAbsolutePath());
				return;
			}

			double sumOfOrders = calculateSumOfOrders(foodChain.getOrders());

			if (sumOfOrders != foodChain.getBranch().getTotalCollection()) {
				Branch currentBranch = foodChain.getBranch();
				currentBranch.setSumOfOrder(sumOfOrders);
				getRestaurantsWithMistmatch().getBranches().add(currentBranch);
			} else {
				Branch currentBranch = foodChain.getBranch();
				currentBranch.setSumOfOrder(sumOfOrders);
				getRestaurantsWithMatch().getBranches().add(currentBranch);
			}
		} catch (IOException e) {
			logger.error("Error occurred while determining file type. ", e);
		}
	}

	private double calculateSumOfOrders(Orders orders) {
		if (orders == null) {
			return 0;
		}

		return orders.getOrderDetail().stream().collect(Collectors.summingDouble(OrderDetail::getBillAmount));
	}

	private CmFoodChain deserializeFile(String fileType, File file) {
		if (fileType == null || fileType.equals("")) {
			logger.error("Invalid file type: {}", fileType);
		}

		ConverterService service = ConverterServiceFactory.getServiceInstance(fileType);

		if (service == null) {
			logger.error("Error while instantiating Converter service. ");
			return null;
		}

		String input = loadFileAsString(file);
		return (CmFoodChain) service.deSerialize(input);
	}

	private String loadFileAsString(File file) {
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(file.getName()))) {
			String line = null;
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		} catch (IOException ioe) {
			logger.error("Error reading source input file: {}", file.getName());
		}
		return stringBuilder.toString();
	}

	private String determineFileType(String filePath) throws IOException {
		FileTypeDetector detector = new TikaFileTypeDetector();
		Path path = Paths.get(filePath);
		return detector.probeContentType(path);
	}

	public CmFoodChain getRestaurantsWithMistmatch() {
		return restaurantsWithMistmatch;
	}

	public void setRestaurantsWithMistmatch(CmFoodChain restaurantsWithMistmatch) {
		this.restaurantsWithMistmatch = restaurantsWithMistmatch;
	}

	public CmFoodChain getRestaurantsWithMatch() {
		return restaurantsWithMatch;
	}

	public void setRestaurantsWithMatch(CmFoodChain restaurantsWithMatch) {
		this.restaurantsWithMatch = restaurantsWithMatch;
	}
}
