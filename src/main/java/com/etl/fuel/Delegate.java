package com.etl.fuel;

import com.etl.jms.ETLMessage;

public interface Delegate {

	void intialiseDelegate(ETLMessage etlMessage) throws Exception;

}
