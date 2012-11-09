package gov.nasa.arc.mct.plot.data;

import java.util.List;
import java.util.Map;

public interface DataHandler {
	public void updateFromFeed(Map<String, List<Map<String, String>>> data);	
	public void synchronizeTime(Map<String, List<Map<String, String>>> data, long syncTime);
}
