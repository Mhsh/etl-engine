package com.etl;

import java.util.List;

public abstract class Extractor {
	
	public abstract List<MappingInfo> createMagicalMappings(ETLMessage etlMessage);
}
