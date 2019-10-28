package net.flyingdragon.max.fall2019physflightaware;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

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
public class Fall2019PhysFlightAwareApplication implements InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(Fall2019PhysFlightAwareApplication.class);
		
	
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    Set<String> observationsAlreadySaved = new HashSet<>();
    
    SimpleJdbcInsert flightInsertor;
    	
	public static void main(String[] args) {
		SpringApplication.run(Fall2019PhysFlightAwareApplication.class, args);
	}
	

	private Config getConfig() {
		return Config.get();
	}

	// Do initializations
	@Override
	public void afterPropertiesSet() throws Exception {
		flightInsertor = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("flights")
				.usingColumns("faflightid","t","location", "flight_info_type", "flight_data");
				
		
		// Read in the existing observation keys
		jdbcTemplate.query("SELECT faFlightId || '@' || cast(flight_data->'timestamp' as text) as key FROM flights WHERE flight_info_type='searchBirdseyeInFlight'",
				(rs, rowNum) -> observationsAlreadySaved.add(rs.getString("key"))
				);
		
		log.info("Observations laoded from db: {}", observationsAlreadySaved);				
	}

	
	public void saveFlightData(JsonNode flightData) throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("flight_data", flightData.toString());
		data.put("faflightid", flightData.path("faFlightID").asText());
		
		// Date objects can be initialized with the number of milliseconds since the
		// unix epoch
		data.put("t", new Date(1000L * flightData.path("timestamp").asLong()));
		data.put("flight_info_type", getConfig().flightAwareFlightSearchMethod);

		double latitude = flightData.path("latitude").asDouble();
		double longitude= flightData.path("longitude").asDouble();
		
		data.put("location", String.format("(%f, %f)", latitude, longitude));
		
		flightInsertor.execute(data);
	}
	
	/** 
	 * Where all the activity happens, run by Spring
	 * 
	 * @param restTemplate REST/HTTP Client magically provided by Spring
	 * @return
	 * @throws Exception
	 */

	@Bean
	@Order(2)
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String responseString = fetchFlightAwareResourceAsString(restTemplate, "SetMaximumResultSize", "max_size", getConfig().maxNum);

			log.info("Response to maxSize: {}", responseString);

			while (true) {
				int sleepTime_secs = getConfig().loopInterval_secs;
				
				try {
					searchForFlights_andProcessResults(restTemplate);
				} catch (Exception e) {
					log.error("Problem fetching or processing results: {}", e.getMessage());
				}
				log.info("Sleeping for {} seconds before querying again", sleepTime_secs);
				Thread.sleep(1000L*sleepTime_secs);
			}

		};
	}



	private void searchForFlights_andProcessResults(RestTemplate restTemplate) throws Exception {
		JsonNode searchResult;
		
		// This will be an array of flight info objects which must have faFlightID and timestamp properties
		JsonNode allFlightData;
		
		if ( getConfig().flightAwareFlightSearchMethod.equals(Config.FLIGHT_SEARCH_METHOD_BIRDSEYE_IN_FLIGHT) ) {
			searchResult = searchBirdseyeInFlight(restTemplate);
			allFlightData = searchResult.path("aircraft");
		} else {
			throw new IllegalStateException("Unknown searchMethod: " + getConfig().flightAwareFlightSearchMethod);
		}

		for (JsonNode aFlight : allFlightData) {
			
			String faFlightId = aFlight.path("faFlightID").asText();
			long t = aFlight.path("timestamp").asLong();
			Date t_date = new Date(1000L * t);
			
			if ( faFlightId.length()==0 || t==0 ) {
				log.error("Ignoring invalid flight: faFlightID or timestamp not defined: {}", aFlight);
				continue;
			}
			
			String key = String.format("%s@%d", faFlightId, t);
			log.info("Aircraft key {} ({})", key, t_date);
			
			// Skip the ones that are repeats 
			if ( observationsAlreadySaved.contains(key) )
			{
				log.info("==> Repeated");
				continue;
			}
			log.info("==> New: {}", aFlight);
			
			saveFlightData(aFlight);
			// Mark that we've processed it now
			observationsAlreadySaved.add(key);
		}
	}

	private String fetchFlightAwareResourceAsString(RestTemplate restTemplate, String operation, Object...parametersAndValues ) {
		String result = restTemplate.getForObject(
				getFlightAwareUrl(operation, parametersAndValues),
				String.class);
		log.info("Response as string: {}", result);
		
		return result;
	}

	
	
	/**
	 * Returns a JsonNode that contains an array of flights.
	 * @param restTemplate
	 * @return
	 */
	public JsonNode searchBirdseyeInFlight(RestTemplate restTemplate) {
		log.info("Querying for flights in bounds of {}", getConfig().locationName);

		String responseString = fetchFlightAwareResourceAsString(restTemplate, 
					"SearchBirdseyeInFlight",
					"query",  String.format("{range lat %s %s} {range lon %s %s}",
							getConfig().getLocaton1Lat(), getConfig().getLocaton2Lat(),
							getConfig().getLocaton1Long(), getConfig().getLocaton2Long()),
					"howMany", getConfig().maxNum,
					"uniqueFlights", 0);

		JsonNode responseJsonNode = JsonUtilities.parseJson(responseString);
		log.info("Response as JsonNode: {}", responseJsonNode);
		
		JsonNode result = responseJsonNode.path("SearchBirdseyeInFlightResult");
		
		if ( result.path("next_offset").intValue() != -1 ) {
			throw new IllegalStateException("FlightAware API response requires paging, which we don't know how to do");
		}
		
		return result;
	}
	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate(getClientHttpRequestFactory());
	}	

	
	URI getFlightAwareUrl(String operation, Object... parameterNamesAndValues) {
		if ( parameterNamesAndValues.length % 2 != 0 )
			throw new IllegalArgumentException(String.format("Must provide an even number of parameterNamesAndValues, not %d", parameterNamesAndValues.length));

		String url = String.format("%s/%s", getConfig().flightAwareBaseUrl, operation);
		
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
