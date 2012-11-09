package gov.nasa.arc.mct.fastplot.settings;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;

/**
 * Contains settings for specific lines on a plot.
 */
public class LineSettings {
	private String  identifier       = "";
	private Integer colorIndex       = 0;
	private Integer thickness        = 1;
	private Integer marker           = 0;
	private String  character        = "";
	private boolean useCharacter     = false;
	private boolean hasRegression    = false;
	private Integer regressionPoints = PlotConstants.NUMBER_REGRESSION_POINTS;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Integer getColorIndex() {
		return colorIndex;
	}
	public void setColorIndex(Integer colorIndex) {
		this.colorIndex = colorIndex;
	}
	public Integer getThickness() {
		return thickness;
	}
	public void setThickness(Integer thickness) {
		this.thickness = thickness;
	}
	public Integer getMarker() {
		return marker;
	}
	public void setMarker(Integer marker) {
		this.marker = marker;
	}
	public String getCharacter() {
		return character;
	}
	public void setCharacter(String character) {
		this.character = character;
	}
	public boolean getUseCharacter() {
		return useCharacter;
	}
	public void setUseCharacter(boolean useCharacter) {
		this.useCharacter = useCharacter;
	}
	public boolean getHasRegression() {
		return hasRegression;
	}
	public void setHasRegression(boolean hasRegression) {
		this.hasRegression = hasRegression;
	}
	public Integer getRegressionPoints() {
		return regressionPoints;
	}
	public void setRegressionPoints(Integer regressionPoints) {
		this.regressionPoints = regressionPoints;
	}
}