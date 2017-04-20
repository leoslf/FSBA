package FSBA;

import java.awt.*;
import javax.swing.*;

class GradientPanel extends JPanel {

	/**
	 * Create a gradient panel of grey color
	 */
	public GradientPanel() {
		//this.setBorder(BorderFactory.createEmptyBorder(N, N, N, N));
		this.setBorder(BorderFactory.createEmptyBorder());
	}
	
	/**
	 * Overrides the paintComponents inherited from JPanel
	 */
	@Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2d = (Graphics2D) graphics;
        Color color1 = new Color(0x44,0x44,0x44);
        Color color2 = new Color(0x20,0x20,0x20);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(
            0, 0, color1, 0, h, color2);
        graphics2d.setPaint(gp);
        graphics2d.fillRect(0, 0, w, h);
    }
}
