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
/**
 * 
 */
package gov.nasa.arc.mct.fastplot.view;

import javax.swing.ImageIcon;

/**
 * This singleton loads images in the plotViews project.
 */
public enum IconLoader {
	INSTANCE;

	public static enum Icons {
		PLOT_TIME_ON_X_NORMAL,
		PLOT_TIME_ON_X_REVERSED,
		PLOT_TIME_ON_Y_NORMAL,
		PLOT_TIME_ON_Y_REVERSED,
		
		PLOT_TIME_ON_Z,
	
		PLOT_UP_ARROW_SOLID_ICON,
        PLOT_UP_ARROW_HOLLOW_ICON,
        PLOT_UP_ARROW_TRANSLUCENT_ICON,
       
        PLOT_DOWN_ARROW_SOLID_ICON,
        PLOT_DOWN_ARROW_HOLLOW_ICON,
        PLOT_DOWN_ARROW_TRANSLUCENT_ICON,
        
        PLOT_RIGHT_ARROW_SOLID_ICON,
        PLOT_RIGHT_ARROW_HOLLOW_ICON,
        PLOT_RIGHT_ARROW_TRANSLUCENT_ICON,
       
        PLOT_LEFT_ARROW_SOLID_ICON,
        PLOT_LEFT_ARROW_HOLLOW_ICON,
        PLOT_LEFT_ARROW_TRANSLUCENT_ICON,
        
        PLOT_PAUSE_ICON,
        PLOT_PLAY_ICON,
        PLOT_PAN_DOWN_ARROW_ICON,
        PLOT_PAN_UP_ARROW_ICON,
        PLOT_PAN_LEFT_ARROW_ICON,
        PLOT_PAN_RIGHT_ARROW_ICON,
        PLOT_ZOOM_IN_X_ICON,
        PLOT_ZOOM_OUT_X_ICON,
        PLOT_ZOOM_IN_Y_ICON,
        PLOT_ZOOM_OUT_Y_ICON,
        
        PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY,
        PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_ORANGE,
        PLOT_CORNER_RESET_BUTTON_TOP_LEFT_GREY,
        PLOT_CORNER_RESET_BUTTON_BOTTOM_RIGHT_GREY,
        PLOT_CORNER_RESET_BUTTON_BOTTOM_LEFT_GREY ;
		
		public ImageIcon getIcon() {
			return IconLoader.INSTANCE.getIcon(this);
		}
	}
	
	private static ImageIcon plotTimeOnXNormalImage;
	private static ImageIcon plotTimeOnXReversedImage;
	private static ImageIcon plotTimeOnYNormalImage;
	private static ImageIcon plotTimeOnYReversedImage;
	private static ImageIcon plotTimeOnZ = null;
	private static ImageIcon plotUpArrowSolidImage = null;
    private static ImageIcon plotUpArrowHollowImage = null;
    private static ImageIcon plotUpArrowTranslucentImage = null;
    
    private static ImageIcon plotDownArrowSolidImage = null;
    private static ImageIcon plotDownArrowHollowImage = null;
    private static ImageIcon plotDownArrowTranslucentImage = null;
    
    private static ImageIcon plotLeftArrowSolidImage = null;
    private static ImageIcon plotLeftArrowHollowImage = null;
    private static ImageIcon plotLeftArrowTranslucentImage = null;
 
    private static ImageIcon plotRightArrowSolidImage = null;
    private static ImageIcon plotRightArrowHollowImage = null;
    private static ImageIcon plotRightArrowTranslucentImage = null;
     
    private static ImageIcon plotPauseIconImage = null;
    private static ImageIcon plotPlayIconImage = null;
    private static ImageIcon plotPanDownArrowImage = null;
    private static ImageIcon plotPanUpArrowImage = null;
    private static ImageIcon plotPanLeftArrowImage = null;
    private static ImageIcon plotPanRightArrowImage = null;
    
    private static ImageIcon plotZoomInXImage = null;
    private static ImageIcon plotZoomOutXImage = null;
    private static ImageIcon plotZoomInYImage = null;
    private static ImageIcon plotZoomOutYImage = null;
    
    private static ImageIcon plotCornerResetButtonTopRightGreyImage = null;
    private static ImageIcon plotCornerResetButtonTopRightOrangeImage = null;
    private static ImageIcon plotCornerResetButtonTopLeftGreyImage = null;
    private static ImageIcon plotCornerResetButtonBottomRightGreyImage = null;
    private static ImageIcon plotCornerResetButtonBottomLeftGreyImage = null;
    
	public ImageIcon getIcon(Icons iconID) {
		switch(iconID) {
		case PLOT_TIME_ON_X_NORMAL:
			if (plotTimeOnXNormalImage == null) {
				plotTimeOnXNormalImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_Xtime_maxRight.png"));
			}
			return plotTimeOnXNormalImage;
		case PLOT_TIME_ON_X_REVERSED:
			if (plotTimeOnXReversedImage == null) {
				plotTimeOnXReversedImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_Xtime_maxLeft.png"));
			}
			return plotTimeOnXReversedImage;
		case PLOT_TIME_ON_Y_NORMAL:
			if (plotTimeOnYNormalImage == null) {
				plotTimeOnYNormalImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_Ytime_maxTop.png"));
			}
			return plotTimeOnYNormalImage;
		case PLOT_TIME_ON_Y_REVERSED:
			if (plotTimeOnYReversedImage == null) {
				plotTimeOnYReversedImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_Ytime_maxBottom.png"));
			}
			return plotTimeOnYReversedImage;
		case PLOT_TIME_ON_Z:
			if (plotTimeOnZ == null) {
				plotTimeOnZ = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_ZtimeSmall.png"));
			}
			return plotTimeOnZ;
			
        case PLOT_UP_ARROW_SOLID_ICON:
            if (plotUpArrowSolidImage == null)
                plotUpArrowSolidImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_up_arrow_solid.png"));
            return plotUpArrowSolidImage;

        case PLOT_UP_ARROW_HOLLOW_ICON:
            if (plotUpArrowHollowImage == null)
                plotUpArrowHollowImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_up_arrow_hollow.png"));
            return plotUpArrowHollowImage;
            
        case PLOT_UP_ARROW_TRANSLUCENT_ICON:
            if (plotUpArrowTranslucentImage == null)
                plotUpArrowTranslucentImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_up_arrow_translucent.png"));
            return plotUpArrowTranslucentImage;
            
        case PLOT_DOWN_ARROW_SOLID_ICON:
            if (plotDownArrowSolidImage == null)
                plotDownArrowSolidImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_down_arrow_solid.png"));
            return plotDownArrowSolidImage;

        case PLOT_DOWN_ARROW_HOLLOW_ICON:
            if (plotDownArrowHollowImage == null)
                plotDownArrowHollowImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_down_arrow_hollow.png"));
            return plotDownArrowHollowImage;
            
        case PLOT_DOWN_ARROW_TRANSLUCENT_ICON:
            if (plotDownArrowTranslucentImage == null)
                plotDownArrowTranslucentImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_down_arrow_translucent.png"));
            return plotDownArrowTranslucentImage;
      
        case PLOT_LEFT_ARROW_SOLID_ICON:
            if (plotLeftArrowSolidImage == null)
                plotLeftArrowSolidImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_left_arrow_solid.png"));
            return plotLeftArrowSolidImage;

        case PLOT_LEFT_ARROW_HOLLOW_ICON:
            if (plotLeftArrowHollowImage == null)
                plotLeftArrowHollowImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_left_arrow_hollow.png"));
            return plotLeftArrowHollowImage;
            
        case PLOT_LEFT_ARROW_TRANSLUCENT_ICON:
            if (plotLeftArrowTranslucentImage == null)
                plotLeftArrowTranslucentImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_left_arrow_translucent.png"));
            return plotLeftArrowTranslucentImage;
            
        case PLOT_RIGHT_ARROW_SOLID_ICON:
            if (plotRightArrowSolidImage == null)
                plotRightArrowSolidImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_right_arrow_solid.png"));
            return plotRightArrowSolidImage;

        case PLOT_RIGHT_ARROW_HOLLOW_ICON:
            if (plotRightArrowHollowImage == null)
                plotRightArrowHollowImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_right_arrow_hollow.png"));
            return plotRightArrowHollowImage;
            
        case PLOT_RIGHT_ARROW_TRANSLUCENT_ICON:
            if (plotRightArrowTranslucentImage == null)
                plotRightArrowTranslucentImage = new ImageIcon(getClass().getClassLoader().getResource("images/plot_right_arrow_translucent.png"));
            return plotRightArrowTranslucentImage;
        case PLOT_PAUSE_ICON:
			if (plotPauseIconImage == null) {
				plotPauseIconImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_pause_button.png"));
			}
			return plotPauseIconImage;
		case PLOT_PLAY_ICON:
			if (plotPlayIconImage == null) {
				plotPlayIconImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_play_button.png"));
			}
			return plotPlayIconImage;   
		case PLOT_PAN_DOWN_ARROW_ICON:
			if (plotPanDownArrowImage == null) {
				plotPanDownArrowImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_pan_down_arrow.png"));
			}
			return plotPanDownArrowImage; 
		case PLOT_PAN_UP_ARROW_ICON:
			if (plotPanUpArrowImage == null) {
				plotPanUpArrowImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_pan_up_arrow.png"));
			}
			return plotPanUpArrowImage;  
		case PLOT_PAN_LEFT_ARROW_ICON:
			if (plotPanLeftArrowImage == null) {
				plotPanLeftArrowImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_pan_left_arrow.png"));
			}
			return plotPanLeftArrowImage;  
		case PLOT_PAN_RIGHT_ARROW_ICON:
			if (plotPanRightArrowImage == null) {
				plotPanRightArrowImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_pan_right_arrow.png"));
			}
			return plotPanRightArrowImage;  
		case PLOT_ZOOM_IN_X_ICON:
			if (plotZoomInXImage == null) {
				plotZoomInXImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_zoom_magnifier_x_axis_plus.png"));
			}
			return plotZoomInXImage;  
		case PLOT_ZOOM_OUT_X_ICON:
			if (plotZoomOutXImage == null) {
				plotZoomOutXImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_zoom_magnifier_x_axis_neg.png"));
			}
			return plotZoomOutXImage;  
		case PLOT_ZOOM_IN_Y_ICON:
			if (plotZoomInYImage == null) {
				plotZoomInYImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_zoom_magnifier_y_axis_plus.png"));
			}
			return plotZoomInYImage;  
		case PLOT_ZOOM_OUT_Y_ICON:
			if (plotZoomOutYImage == null) {
				plotZoomOutYImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_zoom_magnifier_y_axis_neg.png"));
			}
			return plotZoomOutYImage;  
		case  PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY:
			if (plotCornerResetButtonTopRightGreyImage == null) {
				plotCornerResetButtonTopRightGreyImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_corner_reset_button_top_right_grey.png"));
			}
			return plotCornerResetButtonTopRightGreyImage;  
		case  PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_ORANGE:
			if (plotCornerResetButtonTopRightOrangeImage == null) {
				plotCornerResetButtonTopRightOrangeImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_corner_reset_button_top_right_orange.png"));
			}
			return plotCornerResetButtonTopRightOrangeImage;  
		case  PLOT_CORNER_RESET_BUTTON_TOP_LEFT_GREY:
			if (plotCornerResetButtonTopLeftGreyImage == null) {
				plotCornerResetButtonTopLeftGreyImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_corner_reset_button_top_left_grey.png"));
			}
			return plotCornerResetButtonTopLeftGreyImage;  
		case  PLOT_CORNER_RESET_BUTTON_BOTTOM_LEFT_GREY:
			if (plotCornerResetButtonBottomLeftGreyImage == null) {
				plotCornerResetButtonBottomLeftGreyImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_corner_reset_button_bottom_left_grey.png"));
			}
			return plotCornerResetButtonBottomLeftGreyImage;
		case  PLOT_CORNER_RESET_BUTTON_BOTTOM_RIGHT_GREY:
			if (plotCornerResetButtonBottomRightGreyImage == null) {
				plotCornerResetButtonBottomRightGreyImage = new ImageIcon(
						getClass().getClassLoader().getResource("images/plot_corner_reset_button_bottom_right_grey.png"));
			}
			return plotCornerResetButtonBottomRightGreyImage;
					
		default:
			return null;
		}

	}
}

