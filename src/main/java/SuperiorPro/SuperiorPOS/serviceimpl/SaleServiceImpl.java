package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.InvoiceSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleDTO;
import SuperiorPro.SuperiorPOS.DTO.SaleItem;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSaleExportService;
import SuperiorPro.SuperiorPOS.entity.Customer;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.Sale;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.repository.CustomerRepository;
import SuperiorPro.SuperiorPOS.repository.ProductRepository;
import SuperiorPro.SuperiorPOS.repository.SaleRepository;
import SuperiorPro.SuperiorPOS.service.ProductService;
import SuperiorPro.SuperiorPOS.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final ExcelSaleExportService excelSaleExportService;
    private final TelegramBotServiceImpl telegramBotService; // ✅ Inject Telegram service

    @Override
    @Transactional
    public String sell(SaleDTO saleDTO) {
        String invoice = generateNextInvoice();

        Long customerId = saleDTO.getCustomerId();
        String customerName = (saleDTO.getCustomerName() != null && !saleDTO.getCustomerName().isBlank())
                ? saleDTO.getCustomerName().trim()
                : (customerId != null ? customerRepository.findById(customerId).map(Customer::getName).orElse("General")
                        : "General");

        BigDecimal grandTotal = BigDecimal.ZERO;

        if (saleDTO.getItems() != null && !saleDTO.getItems().isEmpty()) {
            for (SaleItem item : saleDTO.getItems()) {
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh"));
                Product product = productService.getByBarcode(item.getBarcode());
                validateStock(product, item.getNumberOfUnit());
                validatePrice(product);

                Sale sale = new Sale();
                sale.setInvoice(invoice);
                sale.setSaleDate(now.toLocalDate());
                sale.setSaleTime(now.toLocalTime());
                sale.setCustomerId(customerId);
                sale.setCustomerName(customerName);
                sale.setBarcode(product.getBarcode());
                sale.setProductName(product.getName());
                sale.setKhmerName(product.getKhmerName());
                sale.setNumberOfUnit(item.getNumberOfUnit());

                BigDecimal soldAmount = item.getUnitPrice().multiply(item.getNumberOfUnit());
                sale.setUnitPrice(item.getUnitPrice());
                sale.setSoldAmount(soldAmount);

                grandTotal = grandTotal.add(soldAmount);

                saleRepository.save(sale);
                product.setAvailableUnit(product.getAvailableUnit().subtract(item.getNumberOfUnit()));
                productRepository.save(product);
            }
        }

        saleDTO.setSoldAmount(grandTotal);
        exportToExcel();

        // ✅ Send Telegram notification
        sendTelegramNotification(invoice, customerName, saleDTO.getItems(), grandTotal);

        return invoice;
    }

    @Override
    @Transactional
    public void updateSale(SaleDTO saleDTO) {
        if (saleDTO.getInvoice() == null || saleDTO.getInvoice().isBlank()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Invoice is required for update");
        }

        List<Sale> existingSales = saleRepository.findByInvoice(saleDTO.getInvoice());
        if (existingSales.isEmpty()) {
            throw new API_Exception(HttpStatus.NOT_FOUND, "No sales found for invoice: " + saleDTO.getInvoice());
        }

        // Restore stock
        for (Sale oldSale : existingSales) {
            Product product = productService.getByBarcode(oldSale.getBarcode());
            product.setAvailableUnit(product.getAvailableUnit().add(oldSale.getNumberOfUnit()));
            productRepository.save(product);
        }

        saleRepository.deleteAll(existingSales);

        Long customerId = saleDTO.getCustomerId();
        String customerName = (saleDTO.getCustomerName() != null && !saleDTO.getCustomerName().isBlank())
                ? saleDTO.getCustomerName().trim()
                : (customerId != null ? customerRepository.findById(customerId).map(Customer::getName).orElse("General")
                        : "General");

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SaleItem item : saleDTO.getItems()) {
            Product product = productService.getByBarcode(item.getBarcode());
            validateStock(product, item.getNumberOfUnit());
            validatePrice(product);

            LocalDate originalDate = existingSales.get(0).getSaleDate();
            LocalTime originalTime = existingSales.get(0).getSaleTime();

            Sale sale = new Sale();
            sale.setInvoice(saleDTO.getInvoice());
            sale.setSaleDate(originalDate);
            sale.setSaleTime(originalTime);
            sale.setCustomerId(customerId);
            sale.setCustomerName(customerName);
            sale.setBarcode(product.getBarcode());
            sale.setProductName(product.getName());
            sale.setKhmerName(product.getKhmerName());
            sale.setNumberOfUnit(item.getNumberOfUnit());

            BigDecimal soldAmount = item.getUnitPrice().multiply(item.getNumberOfUnit());
            sale.setUnitPrice(item.getUnitPrice());
            sale.setSoldAmount(soldAmount);

            grandTotal = grandTotal.add(soldAmount);

            saleRepository.save(sale);
            product.setAvailableUnit(product.getAvailableUnit().subtract(item.getNumberOfUnit()));
            productRepository.save(product);
        }

        saleDTO.setSoldAmount(grandTotal);
        log.info("✏️ Invoice {} overwritten with {} items", saleDTO.getInvoice(), saleDTO.getItems().size());
        exportToExcel();

        // ✅ Send Telegram notification
        sendTelegramNotification(saleDTO.getInvoice(), customerName, saleDTO.getItems(), grandTotal);
    }

    // 🔹 Helper method for Telegram notifications
    private void sendTelegramNotification(String invoice, String customerName, List<SaleItem> items, BigDecimal grandTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧾 Invoice: ").append(invoice).append("\n");
        sb.append("Customer: ").append(customerName).append("\n\n");

        for (SaleItem item : items) {
            sb.append(item.getProductName())
              .append(" x").append(item.getNumberOfUnit())
              .append(" = $").append(item.getSoldAmount())
              .append("\n");
        }

        sb.append("\nTotal: $").append(grandTotal);

        // Replace with Mr. A’s actual chat_id
        telegramBotService.sendMessage(655254730L, sb.toString());
    }

	@Override
	public List<SaleDTO> searchSalesByInvoiceKeyword(String keyword) {
		return saleRepository.findByInvoiceContainingIgnoreCase(keyword).stream().map(this::toDTO).toList();
	}

	@Override
	public List<InvoiceSummaryDTO> getInvoiceSummaries() {
		List<Sale> sales = saleRepository.findAll();
		Map<String, List<Sale>> grouped = sales.stream().collect(Collectors.groupingBy(Sale::getInvoice));

		return grouped.entrySet().stream().map(entry -> {
			List<Sale> items = entry.getValue();
			Sale first = items.get(0);

			BigDecimal total = items.stream().map(Sale::getSoldAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			// ✅ Removed sellerName
			return new InvoiceSummaryDTO(entry.getKey(), first.getCustomerName(), null, // no seller
					first.getSaleDate(), first.getSaleTime(), items.size(), total);
		}).sorted((a, b) -> b.getInvoice().compareTo(a.getInvoice())).toList();
	}

	@Override
	@Transactional
	public void cancelSaleByInvoice(String invoice) {
		List<Sale> sales = saleRepository.findByInvoice(invoice);
		if (sales.isEmpty()) {
			throw new API_Exception(HttpStatus.NOT_FOUND, "No sales found for invoice: " + invoice);
		}
		for (Sale sale : sales) {
			Product product = productService.getByBarcode(sale.getBarcode());
			product.setAvailableUnit(product.getAvailableUnit().add(sale.getNumberOfUnit()));
			productRepository.save(product);
		}
		saleRepository.deleteAll(sales);
		log.info("️ Sale cancelled: invoice={}, restoredItems={}", invoice, sales.size());
		exportToExcel();
	}

	@Override
	public Map<String, Object> getPaginatedSales(int page, int limit, String sortBy, String sortDir, String invoice) {
		Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
		Pageable pageable = PageRequest.of(page - 1, limit, sort);

		Page<Sale> salePage;
		if (invoice != null && !invoice.isBlank()) {
			salePage = saleRepository.findDistinctByInvoiceContainingIgnoreCase(invoice, pageable);
		} else {
			salePage = saleRepository.findAll(pageable);
		}

		List<SaleDTO> list = salePage.getContent().stream().map(this::toDTO).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("list", list);
		response.put("paginationDTO", Map.of("totalPages", salePage.getTotalPages(), "totalElements",
				salePage.getTotalElements(), "currentPage", page));

		return response;
	}

	@Override
	public Page<InvoiceSummaryDTO> getInvoices(Map<String, String> params) {
		int page = Integer.parseInt(params.getOrDefault("_page", "1"));
		int limit = Integer.parseInt(params.getOrDefault("_limit", "5"));
		String sortBy = params.getOrDefault("_sortBy", "invoice");
		String sortDir = params.getOrDefault("_sortDir", "desc");

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page - 1, limit, sort);

		String invoiceFilter = params.get("invoice");

		Page<String> invoicePage = saleRepository.findDistinctInvoices(invoiceFilter, pageable);

		List<InvoiceSummaryDTO> summaries = invoicePage.getContent().stream().map(inv -> {
			List<Sale> items = saleRepository.findByInvoice(inv);
			if (items.isEmpty()) {
				return new InvoiceSummaryDTO(inv, "", null, null, null, 0, BigDecimal.ZERO);
			}
			Sale first = items.get(0);
			BigDecimal total = items.stream().map(Sale::getSoldAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			// ✅ Removed sellerName
			return new InvoiceSummaryDTO(inv, first.getCustomerName(), null, // no seller
					first.getSaleDate(), first.getSaleTime(), items.size(), total);
		}).toList();

		return new PageImpl<>(summaries, pageable, invoicePage.getTotalElements());
	}

	@Override
	public SaleDTO getSalesByInvoice(String invoice) {
		List<Sale> sales = saleRepository.findByInvoice(invoice);
		if (sales.isEmpty())
			return null;

		Sale first = sales.get(0);
		SaleDTO dto = new SaleDTO();
		dto.setInvoice(first.getInvoice());
		dto.setCustomerId(first.getCustomerId());
		dto.setCustomerName(first.getCustomerName());
		dto.setSaleDate(first.getSaleDate());
		dto.setSaleTime(first.getSaleTime());

		List<SaleItem> items = sales.stream().map(s -> {
			SaleItem item = new SaleItem();
			item.setBarcode(s.getBarcode());
			item.setProductName(s.getProductName());
			item.setKhmerName(s.getKhmerName());
			item.setNumberOfUnit(s.getNumberOfUnit());
			item.setUnitPrice(s.getUnitPrice());
			item.setDiscount(BigDecimal.ZERO);
			item.setSoldAmount(s.getSoldAmount());
			return item;
		}).toList();

		dto.setItems(items);
		BigDecimal total = sales.stream().map(Sale::getSoldAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		dto.setSoldAmount(total);
		return dto;
	}

	public BigDecimal getLastPriceForCustomerProduct(Long customerId, String productName) {
		Sale lastSale = saleRepository.findTopByCustomerIdAndProductNameOrderBySaleDateDescSaleTimeDesc(customerId,
				productName);
		return lastSale != null ? lastSale.getUnitPrice() : null;
	}

	@Override
	public List<SaleDTO> getSalesByDate(LocalDate date) {
		return saleRepository.findBySaleDate(date).stream().map(this::toDTO).toList();
	}

	@Override
	public List<SaleDTO> getSalesByDateRange(LocalDate start, LocalDate end) {
		return saleRepository.findBySaleDateBetween(start, end).stream().map(this::toDTO).toList();
	}

	// Allow stock to be under zero
	private void validateStock(Product product, BigDecimal units) {
		if (product.getAvailableUnit() != null && product.getAvailableUnit().compareTo(units) < 0) {
			log.warn("⚠️ Overselling product '{}'. Requested: {}, Available: {}", product.getName(), units,
					product.getAvailableUnit());
		}
	}

	private void validatePrice(Product product) {
		if (product.getSalePrice() == null || product.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new API_Exception(HttpStatus.BAD_REQUEST, String.format(
					"Invalid sale price for product '%s'. Price: %s", product.getName(), product.getSalePrice()));
		}
	}

	private SaleDTO toDTO(Sale sale) {
		SaleDTO dto = new SaleDTO();
		dto.setInvoice(sale.getInvoice());
		dto.setBarcode(sale.getBarcode());
		dto.setProductName(sale.getProductName());
		dto.setKhmerName(sale.getKhmerName());
		dto.setNumberOfUnit(sale.getNumberOfUnit());
		dto.setUnitPrice(sale.getUnitPrice());
		dto.setSoldAmount(sale.getSoldAmount());
		dto.setSaleDate(sale.getSaleDate());
		dto.setSaleTime(sale.getSaleTime());
		dto.setCustomerName(sale.getCustomerName());
		dto.setCustomerId(sale.getCustomerId());
		return dto;
	}

	private String generateNextInvoice() {
		Sale lastSale = saleRepository.findTopByOrderByInvoiceDesc();
		String lastInvoice = (lastSale != null) ? lastSale.getInvoice() : null;
		int nextNumber = 1;

		if (lastInvoice != null && lastInvoice.startsWith("INV-")) {
			try {
				nextNumber = Integer.parseInt(lastInvoice.substring(4)) + 1;
			} catch (NumberFormatException e) {
				log.warn("⚠️ Invoice format corrupted: {}", lastInvoice);
			}
		}

		return String.format("INV-%04d", nextNumber);
	}

	private void exportToExcel() {
		try {
			int count = excelSaleExportService.exportSalesToExcel();
			log.info(" Successfully exported {} sales to Excel", count);
		} catch (IOException e) {
			log.error("❌ Failed to export sales after mutation: {}", e.getMessage(), e);
		}
	}
}
