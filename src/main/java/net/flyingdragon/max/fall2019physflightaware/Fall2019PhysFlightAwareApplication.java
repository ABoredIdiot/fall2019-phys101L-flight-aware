package net.flyingdragon.max.fall2019physflightaware;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The main application class.
 * 
 * The skeleton of this was guided by:
 * https://spring.io/guides/gs/consuming-rest/
 * 
 * The RestTemplate/BasicAuth setup was guided by:
 * 	https://howtodoinjava.com/spring-boot2/resttemplate/resttemplate-basicauth-example/
 * 
 * @author bert
 *
 */
@SpringBootApplication
public class Fall2019PhysFlightAwareApplication {
	private static final Logger log = LoggerFactory.getLogger(Fall2019PhysFlightAwareApplication.class);
		
	public static void main(String[] args) {
		SpringApplication.run(Fall2019PhysFlightAwareApplication.class, args);
	}
	
	
	/** 
	 * Where all the activity happens, run by Spring
	 * 
	 * @param restTemplate REST/HTTP Client magically provided by Spring
	 * @return
	 * @throws Exception
	 */

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String responseString = restTemplate.getForObject(
					getFlightAwareUrl("SearchBirdseyePositions",
						"query", "{range lat 37.601258 37.636882} {range lon -122.413616 -122.291737}",
						"howMany", 15,
						"uniqueFlights", 0), 
					String.class);
			
			log.info("Response as string: {}", responseString);
			
			Map<String, Object> responseMap = JsonUtilities.toMap(responseString);
			log.info("Response as Map: {}", responseMap);

			JsonNode responseJsonNode = JsonUtilities.parseJson(responseString);
			log.info("Response as JsonNode: {}", responseJsonNode);
			
			JsonNode flights = responseJsonNode.path("SearchBirdseyePositionsResult").path("data");
			
			for (JsonNode flight : flights) {
				log.info("Flight {}: {}", flight.path("faFlightID"), flight);
			}
		};
	}

	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate(getClientHttpRequestFactory());
	}	

	
	URI getFlightAwareUrl(String operation, Object... parameterNamesAndValues) {
		if ( parameterNamesAndValues.length % 2 != 0 )
			throw new IllegalArgumentException(String.format("Must provide an even number of parameterNamesAndValues, not %d", parameterNamesAndValues.length));

		String url = String.format("%s/%s", Config.get().flightAwareBaseUrl, operation);
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
		
		for (int i=0; i<parameterNamesAndValues.length; i+=2) {
			uriBuilder.queryParam((String) parameterNamesAndValues[i], parameterNamesAndValues[i+1]);
		}
		
		URI result = uriBuilder.build().toUri();
		
		log.info("Built FlightAware URI: {}", result);
		
		return result;
	}

		
	
    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory()
    {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                          = new HttpComponentsClientHttpRequestFactory();
         
        clientHttpRequestFactory.setHttpClient(httpClient());
              
        return clientHttpRequestFactory;
    }
     
    private HttpClient httpClient()
    {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
 
        credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(Config.get().flightAwareUsername, Config.get().flightAwarePassword));
 
        HttpClient client = HttpClientBuilder
                                .create()
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .build();
        return client;
    }
}
