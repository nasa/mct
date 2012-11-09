package gov.nasa.arc.mct.plot.data;

import gov.nasa.arc.mct.components.FeedProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataInterpreter implements DataHandler {	
	private FeedProvider dependentFeed;
	private List<DataReceiver> dataReceivers;

	public DataInterpreter(FeedProvider fp) {
		dependentFeed = fp;
		dataReceivers  = new ArrayList<DataReceiver>();
	}
	
	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		if (data.containsKey(dependentFeed.getSubscriptionId())) {
			for (Map<String, String> datum : data.get(dependentFeed.getSubscriptionId())) {
				for (DataReceiver dataReceiver : dataReceivers) {
					dataReceiver.addData(getDependentValue(datum, data), getIndependentValue(datum, data));
				}
			}
		}
	}

	@Override
	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		
	}
	
	public void addDataReceiver(DataReceiver dataReceiver) {
		dataReceivers.add(dataReceiver);
	}
	
	protected double getDependentValue(Map<String, String> datum,
			Map<String, List<Map<String, String>>> context) {
		// TODO : Also NaN on status
		String v = dependentFeed.getRenderingInfo(datum).getValueText();
		try {
			return Double.parseDouble(v);
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	protected double getIndependentValue(Map<String, String> datum,
			Map<String, List<Map<String, String>>> context) {
		// TODO : Also NaN on status
		String v = datum.get(FeedProvider.NORMALIZED_TIME_KEY);
		try {
			return Double.parseDouble(v);
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
}
