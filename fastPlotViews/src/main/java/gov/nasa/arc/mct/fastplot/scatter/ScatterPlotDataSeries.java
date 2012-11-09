package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataSeries;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.LegendEntry;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

public class ScatterPlotDataSeries implements AbstractPlotDataSeries {
	private AbstractPlotLine plotLine;
	private SortedMap<Long, Double> dependent;
	private SortedMap<Long, Double> independent;
	private long        lastUpdate = Long.MIN_VALUE;
	private AbstractLegendEntry legend;
	private TimestampList timestamps = new TimestampList();
	
	public ScatterPlotDataSeries(AbstractPlottingPackage plot, SortedMap<Long, Double> independent, SortedMap<Long, Double> dependent, LegendEntry legend) {
		super();
		this.dependent = dependent;
		this.independent = independent;		
		this.legend = legend;
		this.plotLine = plot.createPlotLine();
		// TODO: Setup plot line, etc!
	}

	@Override
	public AbstractLegendEntry getLegendEntry() {
		return legend;
	}
	
	public AbstractPlotLine getPlotLine() {
		return plotLine;
	}
	
	public void updatePlotLine() {
		for (Entry<Long, Double> entry : dependent.tailMap(lastUpdate + 1).entrySet()) {
			long t = entry.getKey();
			if (independent.containsKey(t)) {
				double depValue = entry.getValue();
				double indValue = independent.get(t);
				plotLine.addData(indValue, depValue);
				timestamps.add(t);
				lastUpdate = t;
			}
		}
	}

	public void clearBefore(long timestamp) {
		int count = timestamps.clearOlder(timestamp);
		if (count > 0) {
			plotLine.removeFirst(count);
		}
	}
	
	@Override
	public void setLegendEntry(AbstractLegendEntry entry) {
		legend = entry;
		entry.attachPlotLine(plotLine);
	}
	
	private class TimestampList {
		private static final int BLOCK_SIZE = 1024;
		private int assigned  = 0;
		private int start     = 0;
		private List<long[]> timestamps = new ArrayList<long[]>();
		
		public void add(long t) {
			if (assigned >= timestamps.size() * BLOCK_SIZE) {
				timestamps.add(new long[BLOCK_SIZE]);
			}
			long[] dest = timestamps.get(timestamps.size() - 1);
			dest[assigned++ % BLOCK_SIZE] = t;
		}
		
		public int clearOlder(long t) {
			int c = 0;
			for (long[] block : timestamps) {
				for (long timestamp : block) {
					if (timestamp >= t) return clear(c - start);
					c++;
				}
			}
			return clear(c - start);
		}
		
		private int clear(int count) {
			int c = count;			
			while (c >= BLOCK_SIZE) {
				timestamps.remove(0);
				c -= BLOCK_SIZE;
			}
			start = (start + count) % BLOCK_SIZE;
			return count;
		}
	}

}
