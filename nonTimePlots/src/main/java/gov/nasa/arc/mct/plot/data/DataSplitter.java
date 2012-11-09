package gov.nasa.arc.mct.plot.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataSplitter extends ArrayList<DataHandler> implements DataHandler {
	private static final long serialVersionUID = 4650821336819192744L;

	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		for (DataHandler handler : this) {
			handler.updateFromFeed(data);
		}
	}

	@Override
	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		for (DataHandler handler : this) {
			handler.synchronizeTime(data, syncTime);
		}
	}

}
