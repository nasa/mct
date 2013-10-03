package gov.nasa.arc.mct.fastplot.view.legend;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;
import gov.nasa.arc.mct.gui.FeedView;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

public class LegendEntryView extends FeedView implements AbstractLegendEntry {
	private static final long serialVersionUID = -2885137579175013142L;
	public static final ViewInfo VIEW_INFO = new ViewInfo(LegendEntryView.class, "Legend Entry", ViewType.EMBEDDED);

	private Collection<FeedProvider> feedProviders;
	private JLabel label = new JLabel();
	private Font baseFont;
	private Font strikeThroughFont;

	public LegendEntryView(AbstractComponent ac, ViewInfo vi) {
		super(ac, vi);

		setOpaque(false);

		label.setText(ac.getDisplayName());
		label.setFont(updateBaseFont(Font.ITALIC));
		label.setForeground(Color.LIGHT_GRAY);

		add(label);

		FeedProvider fp = ac.getCapability(FeedProvider.class);
		feedProviders = fp==null ? Collections.<FeedProvider>emptyList() : Collections.singleton(fp);

		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		
		MouseAdapter adapter = new LegendEntryMouseAdapter();
		label.addMouseListener(adapter);
		label.addMouseMotionListener(adapter);
		label.setTransferHandler(new LegendEntryTransferHandler());
	}

	public void setAppearance(Color c, Icon i) {
		label.setForeground(c);
		label.setFont(updateBaseFont(Font.PLAIN));
		label.setIcon(new Icon() {

			@Override
			public int getIconHeight() {
				return 9; //TODO: if line has icon...
			}

			@Override
			public int getIconWidth() {
				return 9;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {

			}

		});
		// TODO: Attach popup!
		
	}

	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		for (FeedProvider fp : feedProviders) {
			if (data.containsKey(fp.getSubscriptionId())) {
				List<Map<String, String>> series = data.get(fp.getSubscriptionId());
				if (!series.isEmpty()){
					String display = getManifestedComponent().getDisplayName();
					RenderingInfo ri = fp.getRenderingInfo(series.get(series.size() - 1));
					String status = ri.getStatusText();
					if (!status.trim().isEmpty()) display = "(" + status + ") " + display;
					label.setText(display);
					label.setFont(ri.isPlottable() ? baseFont : strikeThroughFont); 
				}
			}
		}

	}

	@Override
	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		updateFromFeed(data);
	}

	@Override
	public Collection<FeedProvider> getVisibleFeedProviders() {
		return feedProviders;
	}

	@SuppressWarnings("unchecked")
	private Font updateBaseFont(int style) {
		baseFont = label.getFont().deriveFont(9f).deriveFont(style);

		@SuppressWarnings("rawtypes")
		Map attrs = baseFont.getAttributes();
		attrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		strikeThroughFont = baseFont.deriveFont(attrs);

		return baseFont;
	}

	@Override
	public void attachPlotLine(AbstractPlotLine plotLine) {
		Color c = plotLine.getColor();
		Icon  i = plotLine.getIcon();
		setAppearance(c, i);
	}

	@Override
	public String getDisplayedName() {
		return label.getText();
	}

	private class LegendEntryMouseAdapter extends MouseAdapter {
		private boolean mousePressed = false;
		
		@Override
		public void mouseClicked(MouseEvent evt) {
			if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
				getManifestedComponent().open();
			} // TODO: Popup?
			
		}
		
		@Override
		public void mousePressed(MouseEvent evt) {
			mousePressed = true;
		}
		
		public void mouseReleased(MouseEvent evt) {
			if (evt.isPopupTrigger()) {
				// Not quite - the menu manager assumes you will have a selection (and thus a selection manager)
				// for this method to work (otherwise, just ends up referring to the view you're sitting on.)
//				PlatformAccess.getPlatform().getMenuManager().getManifestationPopupMenu(ReferenceView.this)
//					.show(ReferenceView.this, evt.getX(), evt.getY());
			}
			mousePressed = false;
		}
    	
		public void mouseDragged(MouseEvent evt) {
			if (mousePressed) {
				label.getTransferHandler().exportAsDrag(label, evt, TransferHandler.COPY);		
			}
		}
	}

	private class LegendEntryTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1868502829348745345L;

		@Override
        public int getSourceActions(JComponent c) {
        	return COPY;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
        	View v[] = { LegendEntryView.this };
            return new ViewRoleSelection(v);
        }        
	}

	@Override
	public void setBaseDisplayName(String displayName) {
		label.setText(displayName);
	}

}
