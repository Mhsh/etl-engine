package com.etl;

import java.util.List;

import com.etl.jms.ETLMessage;

public abstract class Extractor {
	
	public abstract List<MappingInfo> createMagicalMappings(ETLMessage etlMessage);
}
