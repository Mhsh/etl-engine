package com.etl.fuel;

import java.io.FileInputStream;

import com.etl.jms.ETLMessage;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RSSDelegate implements Delegate {

	private SyndFeed feed;

	@Override
	public void intialiseDelegate(ETLMessage etlMessage) throws Exception {
		FileInputStream inputStream = new FileInputStream(etlMessage.getRawFilePath());
		// Create a SyndFeedInput to read the feed
		SyndFeedInput input = new SyndFeedInput();
		feed = input.build(new XmlReader(inputStream));
	}

	/**
	 * @return the feed
	 */
	public SyndFeed getFeed() {
		return feed;
	}

}
