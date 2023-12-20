package com.etl;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.etl.jms.ETLMessage;

@Configuration
public class EtlEngineStarter {

	@Autowired
	private ETLEngineGearBox engineGearBox;

	@Bean
	Consumer<ETLMessage> startTransformation() {
		return engineGearBox::startTransformation;
	}
}
