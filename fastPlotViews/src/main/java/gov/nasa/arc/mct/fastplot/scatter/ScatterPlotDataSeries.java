package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxisBoundManager;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataSeries;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.LegendEntry;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class ScatterPlotDataSeries implements AbstractPlotDataSeries {
	private AbstractPlottingPackage plot;
	private AbstractPlotLine plotLine;
	private SortedMap<Long, Double> dependent;
	private SortedMap<Long, Double> independent;
	private SortedSet<Long>         timestamps = new TreeSet<Long>();
	private AbstractLegendEntry legend;	
	
	
	public ScatterPlotDataSeries(AbstractPlottingPackage plot, SortedMap<Long, Double> independent, SortedMap<Long, Double> dependent, LegendEntry legend) {
		super();
		this.dependent = dependent;
		this.independent = independent;		
		this.legend = legend;
		this.plotLine = plot.createPlotLine();
		this.plot = plot;
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
		if (timestamps.size() > 0) {
			addData(independent.headMap(timestamps.first()),
			        dependent.headMap(timestamps.first()),
			        false);
			addData(independent.tailMap(timestamps.last() + 1),
			        dependent.tailMap(timestamps.last() + 1),
			        true);
		} else {
			addData(independent,
			        dependent,
			        true);
		}
		
	}
	
	private void addData(SortedMap<Long, Double> iMap, SortedMap<Long, Double> dMap, boolean comesAfter) {
		if (iMap.size() > 0 && dMap.size() > 0) {
			Long[] ts = getMatchingTimestamps(iMap, dMap);
			if (ts.length > 0) {
				double[][] pairs = getPairs(iMap, dMap, ts);
				if (comesAfter) {
					plotLine.appendData(pairs[0], pairs[1]);
				} else {
					plotLine.prependData(pairs[0], pairs[1]);
				}
				informBoundManagers(ts, pairs[0], pairs[1]);
			}
			Collections.addAll(timestamps, ts);
		}
	}
	
	private double[][] getPairs(SortedMap<Long, Double> a, SortedMap<Long, Double> b, Long[] timestamps) {
		int sz = timestamps.length;
		int i = 0;
		double[][] values = { new double[sz], new double[sz] };
		for(Long key : timestamps) {
			values[0][i] = a.get(key);
			values[1][i] = b.get(key);
			i++;
		}
		
		return values;
	}
	
	private void informBoundManagers(Long[] timestamps, double[] independent, double[] dependent) {
		Collection<AbstractAxisBoundManager> indMgr = plot.getBoundManagers(AxisVisibleOrientation.HORIZONTAL);
		Collection<AbstractAxisBoundManager> depMgr = plot.getBoundManagers(AxisVisibleOrientation.VERTICAL);
		for (int i = 0; i < timestamps.length; i++) {
			for (AbstractAxisBoundManager mgr : indMgr) {
				mgr.informPointPlottedAtTime(timestamps[i], independent[i]);
			}
			for (AbstractAxisBoundManager mgr : depMgr) {
				mgr.informPointPlottedAtTime(timestamps[i],   dependent[i]);	
			}			
		}
	}
	
	private Long[] getMatchingTimestamps(SortedMap<Long, Double> a, SortedMap<Long, Double> b) {
		SortedMap<Long, Double> small = a.size() > b.size() ? b : a;
		SortedMap<Long, Double> large = small == a ? b : a;
		int count = 0;
		for (Long key : small.keySet()) if (large.containsKey(key)) count++;
		Long[] ts = new Long[count];
		int i = 0;
		for (Long key : small.keySet()) if (large.containsKey(key)) ts[i++] = key;
		return ts;
	}

	public void clearBefore(long timestamp) {
		if (!timestamps.isEmpty() && timestamps.first() < timestamp) {
			SortedSet<Long> head = timestamps.headSet(timestamp);
			plotLine.removeFirst(head.size());
			timestamps.retainAll(timestamps.tailSet(timestamp+1));
		}
	}
	
	public void clearAfter(long timestamp) {
		if (!timestamps.isEmpty() && timestamps.last() > timestamp) {
			SortedSet<Long> tail = timestamps.tailSet(timestamp + 1);
			plotLine.removeLast(tail.size());
			timestamps.retainAll(timestamps.headSet(timestamp + 1));			
		}
	}
	
	@Override
	public void setLegendEntry(AbstractLegendEntry entry) {
		legend = entry;
		entry.attachPlotLine(plotLine);
	}


}
