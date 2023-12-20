package com.etl.search.index;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticConfiguration {


	@Value("${elastic.server.username}")
	private String username;

	@Value("${elastic.server.password}")
	private String password;

	@Value("${elastic.server.hostname}")
	private String host;

	@Value("${elastic.server.port}")
	private Integer port;

	@Value("${elastic.server.protocol}")
	private String protocol;

	@Bean
	ElasticsearchClient createClient() {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, protocol))
				.setHttpClientConfigCallback(new HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						httpClientBuilder.disableAuthCaching();
						return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
				});
		RestClient restClient = builder.build();
		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		// And create the API client
		return new ElasticsearchClient(transport);

	}

}
