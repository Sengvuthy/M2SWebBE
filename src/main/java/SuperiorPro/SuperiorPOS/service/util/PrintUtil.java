package SuperiorPro.SuperiorPOS.service.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import com.github.anastaciocintra.escpos.EscPos;

public class PrintUtil {

    // Header row
    public static BufferedImage createHeaderImage(int fontSize) {
        String header = String.format("%-25s %15s %10s %10s", "Product", "Qty", "Price", "Total");
        return FontUtil.renderMixedText(header, fontSize, true);
    }

    // Product row (Khmer + English product name + numbers)
    public static BufferedImage createRowImage(String product, BigDecimal qty, double price, double total, int fontSize) {
        int width = 500;
        int height = fontSize + 12;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        BufferedImage productImg = FontUtil.renderMixedText(product, fontSize, false);
        g2d.drawImage(productImg, 0, 0, null);

        Font latinFont = new Font("Noto Sans", Font.PLAIN, fontSize);
        g2d.setFont(latinFont);
        g2d.setColor(Color.BLACK);

        int y = fontSize;
        g2d.drawString(qty.stripTrailingZeros().toPlainString(), 295, y); // Qty
        g2d.drawString("$" + String.format("%.2f", price), 335, y);       // Price
        g2d.drawString("$" + String.format("%.2f", total), 425, y);       // Total

        g2d.dispose();
        return img;
    }

    // Totals section (simplified: only grand total in USD + Riel)
    public static BufferedImage createTotals(double grandTotalUSD, int totalRiel, double totalUnits, int fontSize) {
        String totals = String.format("(%.1f units)\nTotal: $%.2f / R %d",
                totalUnits, grandTotalUSD, totalRiel);
        return FontUtil.renderMixedText(totals, fontSize, true);
    }

    // Printer lookup
    public static PrintService getPrintServiceByName(String printerName) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services == null || services.length == 0) return null;
        for (PrintService ps : services) {
            if (ps.getName().equalsIgnoreCase(printerName)) return ps;
        }
        return null;
    }

    // TCP/IP printer
    public static EscPos getTcpIpPrinter(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            OutputStream out = socket.getOutputStream();
            return new EscPos(out);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hybrid printer (local first, fallback to TCP/IP)
    public static EscPos getEscPosPrinter(String printerName, String fallbackIp, int fallbackPort) {
        PrintService ps = getPrintServiceByName(printerName);
        if (ps != null) {
            try {
                return new EscPos(new com.github.anastaciocintra.output.PrinterOutputStream(ps));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getTcpIpPrinter(fallbackIp, fallbackPort);
    }
}
