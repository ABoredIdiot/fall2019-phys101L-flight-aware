package net.flyingdragon.max.fall2019physflightaware.web;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    @ResponseBody
    public final String home() {
        final String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("User logged in: {}", username);
        StringBuilder result = new StringBuilder();
        
        result.append(String.format("User: %s\n<p>", username));
        
        result.append("<table border=\"1\">");
        result.append("<tr>");
        result.append("<th>flight#.rec#</th>");
        result.append("<th>flight</th>");
        result.append("<th>time</th>");
        result.append("<th>delta t</th>");
        result.append("<th>location</th>");
        result.append("<th>groundspeed</th>");
        result.append("<th>altitude</th>");
        result.append("</tr>\n");
        // Show recent flights... 
		jdbcTemplate.query("SELECT faFlightId as faFlightId FROM flights group by faFlightId ORDER BY max(t) desc limit 10",
				(rs1, flightNum) -> {appendFlightInfoToHtmlTable(rs1, flightNum, result); return 1;}
				);
        result.append("</table>");
        return result.toString();
    }

    public void appendFlightInfoToHtmlTable(ResultSet inputResultSet, int flightNum, StringBuilder htmlOutput) throws SQLException{
    	// Atomics are containers that hold a changeable value. These are needed because
    	// we want to save information across the multiple rows of data
    	final AtomicInteger flightRecordNumber=new AtomicInteger(0);
    	final AtomicReference<Timestamp> timestampOfPreviousFlightRecord = new AtomicReference<>();
    	
    	jdbcTemplate.query("select faFlightId, t, location, flight_data->'groundspeed' as groundspeed, flight_data->'altitude' as altitude FROM flights WHERE faFlightId=? order by t desc",
    			new Object[] {inputResultSet.getString("faFlightId")},
    			(rs) -> { 
    				Timestamp thisDate = rs.getTimestamp("t");
    				String timeChange="-";
    				if (timestampOfPreviousFlightRecord.get() != null ) {
    					timeChange = String.format("%d secs",(timestampOfPreviousFlightRecord.get().getTime() - thisDate.getTime())/1000);
    				}
    				timestampOfPreviousFlightRecord.set(thisDate);
    				htmlOutput.append("<tr>\n");
    				htmlOutput.append(String.format("<td>%d.%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>\n",
    							flightNum+1, flightRecordNumber.incrementAndGet(), rs.getString("faFlightId"), 
    							thisDate, timeChange, 
    							rs.getString("location"), 
    							rs.getString("groundspeed"), 100* rs.getInt("altitude")));
					htmlOutput.append("</tr>");
    			}
    			);
    }
    
}