package SuperiorPro.SuperiorPOS.controller;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;

import SuperiorPro.SuperiorPOS.DTO.ExchangeRateDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleItem;
import SuperiorPro.SuperiorPOS.service.ExchangeRateService;
import SuperiorPro.SuperiorPOS.service.SaleService;
import SuperiorPro.SuperiorPOS.service.util.CoffeeImageIO;
import SuperiorPro.SuperiorPOS.service.util.FontUtil;
import SuperiorPro.SuperiorPOS.service.util.PrintUtil;

@RestController
@RequestMapping("/api")
public class PrintController {

    private final SaleService saleService;
    private final ExchangeRateService exchangeRateService;

    public PrintController(SaleService saleService, ExchangeRateService exchangeRateService) {
        this.saleService = saleService;
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping("/print-receipt")
    public ResponseEntity<String> printReceipt(@RequestBody Map<String, String> payload) {
        String invoice = payload.get("invoice");
        SaleDTO sale = saleService.getSalesByInvoice(invoice);

        if (sale == null) {
            return ResponseEntity.status(404).body("Invoice not found: " + invoice);
        }

        ExchangeRateDTO rateDTO = exchangeRateService.getRate();
        int rate = rateDTO.getRate();

        try {
            EscPos escpos = PrintUtil.getEscPosPrinter("i4B Receipt", "192.168.1.87", 9100);
            if (escpos == null) {
                return ResponseEntity.status(500).body("No printer available (local or TCP/IP).");
            }

            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();

            // Invoice header
            String invoiceDateLine = String.format("Invoice: %-12s        Date: %s %s",
                    sale.getInvoice(),
                    sale.getSaleDate(),
                    sale.getSaleTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            BufferedImage invoiceImg = FontUtil.renderMixedText(invoiceDateLine, 24, false);
            escpos.write(imageWrapper, new EscPosImage(CoffeeImageIO.read(invoiceImg), new BitonalThreshold()));

            // ✅ Exchange Rate only (no cashier)
            String rateLine = "Exchange Rate: " + rate;
            BufferedImage rateImg = FontUtil.renderMixedText(rateLine, 24, false);
            escpos.write(imageWrapper, new EscPosImage(CoffeeImageIO.read(rateImg), new BitonalThreshold()));

            // ✅ Customer
            String customerLine = "Customer: " + sale.getCustomerName();
            BufferedImage customerImg = FontUtil.renderMixedText(customerLine, 24, false);
            escpos.write(imageWrapper, new EscPosImage(CoffeeImageIO.read(customerImg), new BitonalThreshold()));

            escpos.writeLF("");

            // Header row
            BufferedImage headerImage = PrintUtil.createHeaderImage(24);
            escpos.write(imageWrapper, new EscPosImage(CoffeeImageIO.read(headerImage), new BitonalThreshold()));

            escpos.writeLF("---------------------------------------------");

            // Item rows
            for (SaleItem item : sale.getItems()) {
                BigDecimal total = item.getUnitPrice().multiply(item.getNumberOfUnit());
                BufferedImage rowImage = PrintUtil.createRowImage(
                        item.getKhmerName(),
                        item.getNumberOfUnit(),
                        item.getUnitPrice().doubleValue(),
                        total.doubleValue(),
                        24
                );
                escpos.write(imageWrapper, new EscPosImage(CoffeeImageIO.read(rowImage), new BitonalThreshold()));
            }

            escpos.writeLF("---------------------------------------------");

            // ✅ Totals only (no receive/change)
            double grandTotalUSD = sale.getSoldAmount().doubleValue();
            int totalRiel = (int) (Math.ceil((grandTotalUSD * rate) / 100.0) * 100);

            double totalUnits = sale.getItems().stream()
                    .mapToDouble(item -> item.getNumberOfUnit().doubleValue())
                    .sum();

            String totalLine = String.format("(%.1f units)   Total: $%.2f / R %d",
                    totalUnits, grandTotalUSD, totalRiel);

            escpos.writeLF(totalLine);

            escpos.writeLF("");
            escpos.writeLF("******* Goods sold are not returnable *******");
            escpos.writeLF("Thank you for shopping!! Please come again...");

            escpos.feed(5);
            escpos.cut(EscPos.CutMode.FULL);
            escpos.close();

            return ResponseEntity.ok("Printed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Print failed: " + e.getMessage());
        }
    }
}
