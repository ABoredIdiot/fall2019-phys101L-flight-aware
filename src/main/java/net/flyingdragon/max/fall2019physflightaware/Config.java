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
	
	@Value("${flightaware.username}")
	String flightAwareUsername;
	
	@Value("${flightaware.password}")
	String flightAwarePassword;
	
	@Value("${flightaware.baseurl}")
	String flightAwareBaseUrl;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Configuration: username={}, password={}, url={}", 
				flightAwareUsername, flightAwarePassword, flightAwareBaseUrl);		
	}
	
	// Constructed by Spring
	protected Config() {
		sharedInstance = this;
	}
	
    static Config get() {
    	if ( sharedInstance == null )
    		sharedInstance = new Config();
    	
    	return sharedInstance;
    }
}
