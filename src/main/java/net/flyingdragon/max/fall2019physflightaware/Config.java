package net.flyingdragon.max.fall2019physflightaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config implements InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(Config.class);

	private static Config sharedInstance;
	
	@Value("${loop_interval_secs:300}")
	int loopInterval_secs;
	
	@Value("${flightaware.username}")
	String flightAwareUsername;
	
	@Value("${flightaware.password}")
	String flightAwarePassword;
	
	@Value("${flightaware.baseurl}")
	String flightAwareBaseUrl;
	
	public static final String FLIGHT_SEARCH_METHOD_BIRDSEYE_IN_FLIGHT="searchBirdseyeInFlight";
	public static final String FLIGHT_SEARCH_METHOD_BIRDSEYE_POSITIONS="searchBirdseyePositions";
	@Value("${flightaware.flightSearchMethod:searchBirdseyeInFlight}")
	String flightAwareFlightSearchMethod;

	@Value("${location_name}")
	String locationName;
	
	@Value("${location1}")
	String location1;

	@Value("${location2}")
	String location2;

	@Value("${flightaware.maxNum:15}")
	int maxNum;

	@Value("${aws.access_key}")
	String awsAccessKey;

	@Value("${aws.secret_key}")
	String awsSecretKey;
	
	@Value("${admin_users}")
	public String adminUsers;

	String getLocaton1Lat(){
		return location1.substring(0,location1.indexOf(' '));
	}
	String getLocaton2Lat(){
		return location2.substring(0,location2.indexOf(' '));
	}
	String getLocaton1Long(){
		return location1.substring(location1.indexOf(' ') + 1);
	}
	String getLocaton2Long(){
		return location2.substring(location2.indexOf(' ') + 1);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Configuration: username={}, password={}, url={}", 
				flightAwareUsername, flightAwarePassword, flightAwareBaseUrl);	
		
		System.setProperty("aws.accessKeyId", awsAccessKey);
		System.setProperty("aws.secretKey", awsSecretKey);
	}
	
	// Constructed by Spring
	protected Config() {
		sharedInstance = this;
	}
	
    public static Config get() {
    	if ( sharedInstance == null )
    		sharedInstance = new Config();
    	
    	return sharedInstance;
    }
}
