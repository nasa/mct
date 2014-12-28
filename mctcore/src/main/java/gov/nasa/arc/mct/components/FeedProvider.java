/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.components;

import gov.nasa.arc.mct.api.feed.FeedAggregator;
import gov.nasa.arc.mct.services.activity.TimeService;

import java.awt.Color;
import java.util.Map;

/**
 * This interface provides a time based feed. This standardizes
 * the connection between views and the feed providers, so that visualizations such as plots and 
 * tables can be provided without direct knowledge of the feed mechanism (this will handle
 * ISP and other standard based feeds such as ATOM). The rendering mechanism will handle subscriptions for feeds 
 * based on the id (see {@link #getSubscriptionId()}). 
 * FeedProvider instances must be available from {@link gov.nasa.arc.mct.components.AbstractComponent#getCapability(Class)}.
 */
public interface FeedProvider {
    
    /**
     * Required key for the map returned by {@link FeedAggregator#getData(java.util.Set, java.util.concurrent.TimeUnit, long, long)} to identify the normalized time data.
     * 
     */
    public static final String NORMALIZED_TIME_KEY = "time";
    
    /**
     * Required key for the map returned by {@link FeedAggregator#getData(java.util.Set, java.util.concurrent.TimeUnit, long, long)} to identify the normalized data value.
     */
    public static final String NORMALIZED_VALUE_KEY = "data"; 
    
    /**
     * Optional key for the map returned by {@link FeedAggregator#getData(java.util.Set, java.util.concurrent.TimeUnit, long, long)} to identify the normalized data value.
     */
    public static final String NORMALIZED_RENDERING_INFO = "ri"; 
    
    /**
     * key for the map returned by {@link FeedAggregator#getData(java.util.Set, java.util.concurrent.TimeUnit, long, long)} to identify if the data value is valid. 
     */
    public static final String NORMALIZED_IS_VALID_KEY = "isValid"; 
       
    /**
     * Optional key for the map returned by {@link FeedAggregator#getData(java.util.Set, java.util.concurrent.TimeUnit, long, long)} to identify the type of the status class using ISP data types.
     */
    public static final String NORMALIZED_TELEMETRY_STATUS_CLASS_KEY = "status";
   
    /** Rendering info. */  
    static class RenderingInfo {

        private String statusText;
        private Color statusColor;
        private String valueText;
        private Color valueColor;
        private boolean valid;
        private boolean plottable = true;
        static final String sep = "&";  

        public RenderingInfo(
                        String valueText, Color valueColor, 
                        String statusText, Color statusColor,
                        boolean valid) {
            super();
            this.statusText = statusText;
            this.statusColor = statusColor;
            this.valueColor = valueColor;
            this.valueText = valueText;
            this.valid = valid;
        }
        
        /**
         * Returns rendering info, given its string representation
         * 
         * @param riAsString string representation
         * @return rendering info instance
         */
        public static FeedProvider.RenderingInfo valueOf(String riAsString) {

            int start = -1;
            int end = riAsString.indexOf(sep);
            String valueColor = riAsString.substring(start + 1, end);  
            start = end;
            end = riAsString.indexOf(sep, end + 1); 
            String statusText = riAsString.substring(start + 1, end);
            start = end;
            end = riAsString.indexOf(sep, end + 1); 
            String statusColor = riAsString.substring(start + 1, end);
            start = end;
            end = riAsString.indexOf(sep, end + 1); 
            String isValid = riAsString.substring(start + 1, end);
            start = end;
            end = riAsString.indexOf(sep, end + 1);
            String isPlottable = riAsString.substring(start + 1, end);
            start = end;
            String valueText =  riAsString.substring(start + 1);

            FeedProvider.RenderingInfo ri = null;
            ri = new FeedProvider.RenderingInfo(
                            valueText, new Color(Integer.parseInt(valueColor)), 
                            statusText,  new Color(Integer.parseInt(statusColor)), 
                            Boolean.valueOf(isValid)                      
            );

            ri.setPlottable(Boolean.valueOf(isPlottable));
            return ri;
        }

        @Override
        public String toString() {                    
            return Integer.toString(valueColor.getRGB())  +sep+ 
                   statusText +sep+ Integer.toString(statusColor.getRGB()) +sep+ 
                   Boolean.toString(valid)  +sep+ 
                   Boolean.toString(plottable)  +sep+
                   valueText
                   ;          
        }
        
        /**
         * Gets the value.
         * @return the value as string
         */
        public String getValueText() {
            return valueText;
        }
   
        /**
         * Sets the text representation of the value
         * @param v the value
         */
        public void setValueText(String v) {
            this.valueText = v;
            
        }
        
        /**
         * Gets the value color.
         * @return the value color
         */
        public Color getValueColor() {
            return valueColor;
        }
        
        /**
         * Gets the status.
         * @return the status as string
         */
        public String getStatusText() {
            return statusText;
        }

        /**
         * Gets the status color.
         * @return the status color
         */
        public Color getStatusColor() {
            return statusColor;
        }
        
        /**
         * Gets the validity.
         * @return true if the status is valid.
         */
        public boolean isValid() {
            return valid;
        }
        
        /** gets plottable.
         * @return plottable 
         * */
        public boolean isPlottable() {
            return plottable;
        }

        /** sets plottable. 
         * @param plottable true if plottable
         * */
        public void setPlottable(boolean plottable) {
            this.plottable = plottable;
        }

    }
   
    /**
     * Returns the subscription id for the platform to manage. The id will be used as input for the
     * subscription service. 
     * 
     * @return subscription id to manage, the id must be unique across all feed providers (so this should include information on 
     * when the id is available flight number, segment, ...). This method must not return null, but should return
     * an empty list to signify that subscriptions should not be managed by the platform. 
     */
    public String getSubscriptionId();
    
    /**
     * Provides a reference to the time service for this feed. 
     * @return the time service used by this feed provider. 
     */
    public TimeService getTimeService();
    
    /**
     * Returns a description of the feed component in a form suitable for displaying to the user in, for example, a plot legend. 
     * The component author may include newline characters (\n) which the view may use to separate lines. 
     * @return description of this feed component
     */
    public String getLegendText();
    
    /**
     * Returns the maximum number of samples that can be returned per second (the actual rate may fluctuate). 
     * @return maximum number of samples
     */
    public int getMaximumSampleRate();
        
    /**
     * Represents the data type provided through this feed provider. 
     */
    public enum FeedType {
                         /**
                          * A value than can be cast to a {@link Double}.
                          */
                         FLOATING_POINT() {
                            @Override
                            public Object convert(String value) {
                                return Double.valueOf(value);
                            }
                         }, 
                         
                         
                         /**
                          * A raw string value.
                          */
                         STRING() {
                             @Override
                            public Object convert(String value) {
                                return value;
                            }
                         },
                         
                         /**
                          * A value than can be cast to a {@link Long}.
                          */
                         INTEGER() {
                            @Override
                            public Object convert(String value) {
                                return Long.valueOf(value);
                            }
                         };
                         
                         /**
                          * Converts the given String value into the data type represented by this object.
                          * @param value to convert
                          * @return the deserialized representation of the object
                          */
                         public abstract Object convert(String value);
    };

    /**
     * Gets a representation of the type for this feed. <b>BETA</b>
     * @return a representation of the data type for this feed.
     */
    public FeedType getFeedType();
    
    /**
     * Returns the name of the feed. The name should have enough information to identify the component and
     * also include consideration for name collapsing (use consistent naming conventions to allow collapsing
     * information where no information loss will occur. For example, a tabular view may use row and column
     * headers to collect common information). 
     * @return String representing human canonical display information
     */
    public String getCanonicalName();
    
    /**
     * Returns rendering info, given data status. 
     * @param data the feed data
     * @return information on how this data should be rendered
     */
    public RenderingInfo getRenderingInfo(Map<String,String> data);
    
    /**
     * Returns the maximum timestamp for valid data. This is used to support predictions that have values
     * beyond the current time.  
     * @return the long representing epoch time. 
     */
    public long getValidDataExtent();
    
    /**
     * Returns true if the feed represents predictive data. This can be used to determine how far into the 
     * future to make data requests. 
     * @return true if this feed represents a prediction, false otherwise. 
     */
    public boolean isPrediction();
    
    /**
     * Returns true if the data buffer is non-Change of Data (COD); otherwise returns false if COD.
     * @return boolean whether the feed provider is a non-COD data buffer or not.
     */
    public boolean isNonCODDataBuffer();
}
