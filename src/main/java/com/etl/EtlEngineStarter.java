package com.etl;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EtlEngineStarter {

	@Autowired
	private ETLEngineGearBox engineGearBox;

	@Bean
	Consumer<ETLMessage> httpWorker() {
		return engineGearBox::startTransformation;
	}
}
