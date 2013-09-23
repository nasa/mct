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
package gov.nasa.arc.mct.fastplot.bridge;

/**
 * Observer in the Observer pattern. Informed of updates to the subject plot's.
 * 
 *  http://en.wikipedia.org/wiki/Observer_pattern
 */
public interface PlotObserver {
   
   /**
    * Observer is informed about changes to the start and end times of the subject plot.
    * @param startTime
    * @param endTime
    */
   public void updateTimeAxis(PlotSubject subject, long startTime, long endTime);
   
   /**
    * Observer is informed about any axis changes
    * @param subject
    * @param axis
    */
   public void plotAxisChanged(PlotSubject subject, AbstractAxis axis);
   
   /**
    * Observer is informed whenever data is plotted (typically at the end of the feed 
    * cycle)
    */
   public void dataPlotted();
}
