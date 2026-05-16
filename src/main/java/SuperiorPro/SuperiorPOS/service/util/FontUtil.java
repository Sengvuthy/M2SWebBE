package SuperiorPro.SuperiorPOS.service.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.awt.font.TextAttribute;

public class FontUtil {

    // Render mixed Khmer + English text with automatic font selection
    public static BufferedImage renderMixedText(String text, int fontSize, boolean bold) {
        if (text == null) text = "";

        Font latinFont = new Font("Noto Sans", bold ? Font.BOLD : Font.PLAIN, fontSize);
        FontMetrics fm = new Canvas().getFontMetrics(latinFont);
        int width = fm.stringWidth(text) + 20;
        int height = fm.getHeight() + 10;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        AttributedString attrStr = new AttributedString(text);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u1780' && c <= '\u17FF') {
                attrStr.addAttribute(TextAttribute.FAMILY, "Noto Sans Khmer", i, i + 1);
            } else {
                attrStr.addAttribute(TextAttribute.FAMILY, "Noto Sans", i, i + 1);
            }
            attrStr.addAttribute(TextAttribute.SIZE, fontSize, i, i + 1);
            if (bold) {
                attrStr.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, i, i + 1);
            }
        }

        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(attrStr.getIterator(), 5, fm.getAscent() + 2);
        g.dispose();

        return image;
    }

    // Divider line image
    public static BufferedImage renderDivider(int width) {
        BufferedImage img = new BufferedImage(width, 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, 2);
        g.dispose();
        return img;
    }
}
