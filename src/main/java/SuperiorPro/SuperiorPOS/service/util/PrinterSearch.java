package SuperiorPro.SuperiorPOS.service.util;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class PrinterSearch {

    public static void main(String[] args) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (services == null || services.length == 0) {
            System.out.println("No printers found on this system.");
        } else {
            System.out.println("=== Printers Java can see ===");
            for (PrintService ps : services) {
                System.out.println(ps.getName());
            }
        }
    }
}
