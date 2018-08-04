package org.intvw.searchpartner.converter.service.impl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intvw.searchpartner.converter.service.ConverterService;
import org.intvw.searchpartner.exception.JSONConversionFailedException;
import org.intvw.searchpartner.model.CmFoodChain;
import org.intvw.searchpartner.model.Convertable;
import org.intvw.searchpartner.model.SourceFileType;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XMLConverterServiceImpl implements ConverterService {
	private static Logger logger = LogManager.getLogger(XMLConverterServiceImpl.class);
	
	@Autowired
	XmlMapper xmlMapper;
	
	@Override
	public String getSupportedSourceFileType() {
		return SourceFileType.XML.getValue();
	}

	@Override
	public Convertable deSerialize(String input) {
		CmFoodChain foodChain = null;
		
		if (input == null || input.length() < 1) {
			logger.error("Something went wrong, input cannot be blank. Please check. ");
			return null;
		}
		
		try {
			foodChain = xmlMapper.readValue(input, CmFoodChain.class);
		} catch (IOException e) {
			logger.error("Error while deserializing the input XML string. ");
			throw new JSONConversionFailedException("Error while deserializing the input XML string. ");
		}

		return foodChain;
	}

	@Override
	public String serialize(Convertable foodChain) {
		String output = null;
		
		if (foodChain == null) {
			logger.error("Empty bean cannot be serialized. Please check. ");
			return null;
		}
		
		try {
			output = xmlMapper.writeValueAsString((CmFoodChain) foodChain);
		} catch (JsonProcessingException e) {
			logger.error("Error while serializing the input bean to XML string. ");
			throw new JSONConversionFailedException("Error while serializing the input bean to XML string. ");
		}
		
		return output;
	}
}
