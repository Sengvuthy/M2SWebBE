package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.ExpenseIdSummaryDTO;
import SuperiorPro.SuperiorPOS.DTO.ExpenseReportDTO;
import SuperiorPro.SuperiorPOS.DTO.ExpenseReportItem;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelExpenseReportExportService;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.mapper.ExpenseReportMapper;
import SuperiorPro.SuperiorPOS.repository.ExpenseReportRepository;
import SuperiorPro.SuperiorPOS.service.ExpenseReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseReportServiceImpl implements ExpenseReportService {

	private final ExpenseReportRepository expenseRepository;
	private final ExpenseReportMapper expenseMapper;
	private final ExcelExpenseReportExportService excelExpenseReportExportService;

	@Override
	public ExpenseReportDTO createExpense(ExpenseReportDTO expenseReportDTO) {
		String expenseId = generateNextExpenseId();
		String supplierName = Optional.ofNullable(expenseReportDTO.getSupplierName()).filter(s -> !s.isBlank())
				.orElse("General");
		String payerName = Optional.ofNullable(expenseReportDTO.getPayerName()).filter(s -> !s.isBlank())
				.orElse("Unknown");

		List<ExpenseReportItem> savedItems = new ArrayList<>();

		if (expenseReportDTO.getItems() != null && !expenseReportDTO.getItems().isEmpty()) {
			// Manual batch entry
			for (ExpenseReportItem item : expenseReportDTO.getItems()) {
				ExpenseReport expenseReport = new ExpenseReport();
				expenseReport.setExpenseId(expenseId);
				expenseReport.setExpenseDate(LocalDate.now());
				expenseReport.setExpenseTime(LocalTime.now());
				expenseReport.setSupplierName(supplierName);
				expenseReport.setPayerName(payerName);
				expenseReport.setProductName(item.getProductName()); // manual input
				expenseReport.setExpenseUnit(item.getExpenseUnit());
				expenseReport.setExpensePrice(item.getExpensePrice());
				expenseReport
						.setExpenseAmount(item.getExpensePrice().multiply(item.getExpenseUnit()));
				expenseReport.setSource("Other"); // 👈

				expenseRepository.save(expenseReport);
				savedItems.add(item);
			}
		} else {
			// Single manual entry
			ExpenseReport expenseReport = new ExpenseReport();
			expenseReport.setExpenseId(expenseId);
			expenseReport.setExpenseDate(LocalDate.now());
			expenseReport.setExpenseTime(LocalTime.now());
			expenseReport.setSupplierName(supplierName);
			expenseReport.setPayerName(payerName);
			expenseReport.setProductName(expenseReportDTO.getProductName()); // manual input
			expenseReport.setExpenseUnit(expenseReportDTO.getExpenseUnit());
			expenseReport.setExpensePrice(expenseReportDTO.getExpensePrice());
			expenseReport.setExpenseAmount(
					expenseReportDTO.getExpensePrice().multiply(expenseReportDTO.getExpenseUnit()));
			expenseReport.setSource("Other"); // 👈

			expenseRepository.save(expenseReport);

			ExpenseReportItem item = new ExpenseReportItem();
			item.setProductName(expenseReportDTO.getProductName());
			item.setExpenseUnit(expenseReportDTO.getExpenseUnit());
			item.setExpensePrice(expenseReportDTO.getExpensePrice());
			savedItems.add(item);
		}

		exportToExcel();

		ExpenseReportDTO response = new ExpenseReportDTO();
		response.setExpenseId(expenseId);
		response.setSupplierName(supplierName);
		response.setPayerName(payerName);
		response.setExpenseDate(LocalDate.now());
		response.setExpenseTime(LocalTime.now());
		response.setItems(savedItems);

		return response;
	}

	@Override
	public List<ExpenseReportDTO> searchExpenseReportsByExpenseIdKeyword(String keyword) {
		return expenseRepository.findByExpenseIdContainingIgnoreCase(keyword).stream().map(this::toDTO).toList();
	}

	@Override
	public List<ExpenseIdSummaryDTO> getExpenseIdSummaries() {
	    List<ExpenseReport> expenseReports = expenseRepository.findAll();
	    Map<String, List<ExpenseReport>> grouped = expenseReports.stream()
	        .collect(Collectors.groupingBy(ExpenseReport::getExpenseId));

	    return grouped.entrySet().stream().map(entry -> {
	        List<ExpenseReport> items = entry.getValue();
	        ExpenseReport first = items.get(0);
	        double total = items.stream()
	            .mapToDouble(pi -> pi.getExpenseAmount() != null ? pi.getExpenseAmount().doubleValue() : 0d)
	            .sum();

	        String source = items.stream()
	            .map(ExpenseReport::getSource)
	            .filter(Objects::nonNull)
	            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
	            .entrySet().stream()
	            .max(Map.Entry.comparingByValue())
	            .map(Map.Entry::getKey)
	            .orElse("Unknown");

	        return new ExpenseIdSummaryDTO(
	            entry.getKey(),
	            first.getSupplierName(),
	            first.getPayerName(),
	            first.getExpenseDate(),
	            first.getExpenseTime(),
	            items.size(),
	            total,
	            source
	        );
	    }).sorted((a, b) -> b.getExpenseId().compareTo(a.getExpenseId())).toList();
	}

	@Override
	@Transactional
	public void cancelExpenseReportByExpenseId(String expenseId) {
		List<ExpenseReport> expenseReports = expenseRepository.findByExpenseId(expenseId);
		if (expenseReports.isEmpty()) {
			throw new API_Exception(HttpStatus.NOT_FOUND, "No expenses found for expense id: " + expenseId);
		}

		// Just delete the expense entries — no stock adjustment
		expenseRepository.deleteAll(expenseReports);

		log.info("🗑️ ExpenseReport cancelled: expenseId={}, deletedItems={}", expenseId, expenseReports.size());
		exportToExcel();
	}

	@Override
	@Transactional
	public void updateExpenseReport(ExpenseReportDTO expenseReportDTO) {
		if (expenseReportDTO.getExpenseId() == null || expenseReportDTO.getExpenseId().isBlank()) {
			throw new API_Exception(HttpStatus.BAD_REQUEST, "ExpenseId is required for update");
		}

		List<ExpenseReport> existingExpenses = expenseRepository.findByExpenseId(expenseReportDTO.getExpenseId());
		if (existingExpenses.isEmpty()) {
			throw new API_Exception(HttpStatus.NOT_FOUND,
					"No expenses found for expense id: " + expenseReportDTO.getExpenseId());
		}

		// Delete old expense entries
		expenseRepository.deleteAll(existingExpenses);

		// Recreate with new items
		for (ExpenseReportItem item : expenseReportDTO.getItems()) {
			ExpenseReport newExpense = new ExpenseReport();
			newExpense.setExpenseId(expenseReportDTO.getExpenseId());
			newExpense.setExpenseDate(LocalDate.now());
			newExpense.setExpenseTime(LocalTime.now());
			newExpense.setSupplierName(expenseReportDTO.getSupplierName());
			newExpense.setPayerName(expenseReportDTO.getPayerName());
			newExpense.setProductName(item.getProductName()); // manual input
			newExpense.setExpenseUnit(item.getExpenseUnit());
			newExpense.setExpensePrice(item.getExpensePrice());
			newExpense.setExpenseAmount(item.getExpensePrice().multiply(item.getExpenseUnit()));
			newExpense.setSource("Other"); // 👈 important

			expenseRepository.save(newExpense);
		}

		log.info("✏️ Expense Id {} overwritten with {} items", expenseReportDTO.getExpenseId(),
				expenseReportDTO.getItems().size());
		exportToExcel();
	}

	@Override
	public Map<String, Object> getPaginatedExpenseReports(int page, int limit, String sortBy, String sortDir,
			String expenseId) {
		Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
		Pageable pageable = PageRequest.of(page - 1, limit, sort);

		Page<ExpenseReport> expenseReportPage;
		if (expenseId != null && !expenseId.isBlank()) {
			expenseReportPage = expenseRepository.findDistinctByExpenseIdContainingIgnoreCase(expenseId, pageable);
		} else {
			expenseReportPage = expenseRepository.findAll(pageable);
		}

		List<ExpenseReportDTO> list = expenseReportPage.getContent().stream().map(this::toDTO).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("list", list);
		response.put("paginationDTO", Map.of("totalPages", expenseReportPage.getTotalPages(), "totalElements",
				expenseReportPage.getTotalElements(), "currentPage", page));

		return response;
	}

	@Override
	public Page<ExpenseIdSummaryDTO> getExpenseIds(Map<String, String> params) {
		int page = Integer.parseInt(params.getOrDefault("_page", "1"));
		int limit = Integer.parseInt(params.getOrDefault("_limit", "5"));
		String sortBy = params.getOrDefault("_sortBy", "importId");
		String sortDir = params.getOrDefault("_sortDir", "desc");

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page - 1, limit, sort);

		String expenseIdFilter = params.get("expenseId");

		Page<String> expenseIdPage = expenseRepository.findDistinctExpenseIds(expenseIdFilter, pageable);

		List<ExpenseIdSummaryDTO> summaries = expenseIdPage.getContent().stream().map(imp -> {
		    List<ExpenseReport> items = expenseRepository.findByExpenseId(imp);
		    ExpenseReport first = items.get(0);
		    double total = items.stream()
		        .mapToDouble(pi -> pi.getExpenseAmount() != null ? pi.getExpenseAmount().doubleValue() : 0d)
		        .sum();
		    String source = first.getSource(); // ✅ get source from entity
		    return new ExpenseIdSummaryDTO(
		        imp,
		        first.getSupplierName(),
		        first.getPayerName(),
		        first.getExpenseDate(),
		        first.getExpenseTime(),
		        items.size(),
		        total,
		        source // ✅ include source in DTO
		    );
		}).toList();

		return new PageImpl<>(summaries, pageable, expenseIdPage.getTotalElements());
	}

	@Override
	public ExpenseReportDTO getExpenseReportsByExpenseId(String expenseId) {
		List<ExpenseReport> expenseReports = expenseRepository.findByExpenseId(expenseId);
		if (expenseReports.isEmpty())
			return null;

		ExpenseReport first = expenseReports.get(0);
		ExpenseReportDTO dto = new ExpenseReportDTO();
		dto.setExpenseId(first.getExpenseId());
		dto.setSupplierName(first.getSupplierName());
		dto.setPayerName(first.getPayerName());
		dto.setExpenseDate(first.getExpenseDate());
		dto.setExpenseTime(first.getExpenseTime());

		List<ExpenseReportItem> items = expenseReports.stream().map(pi -> {
			ExpenseReportItem item = new ExpenseReportItem();
			item.setProductName(pi.getProductName());
			item.setExpenseUnit(pi.getExpenseUnit());
			item.setExpensePrice(pi.getExpensePrice());
			return item;
		}).toList();

		dto.setItems(items);
		BigDecimal total = expenseReports.stream().map(ExpenseReport::getExpenseAmount).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		dto.setExpenseAmount(total);

		return dto;
	}

	@Override
	public List<ExpenseReportDTO> getExpenseReportsByDate(LocalDate date) {
		return expenseRepository.findByExpenseDate(date).stream().map(this::toDTO).toList();
	}

	@Override
	public List<ExpenseReportDTO> getExpenseReportsByDateRange(LocalDate start, LocalDate end) {
		return expenseRepository.findByExpenseDateBetween(start, end).stream().map(this::toDTO).toList();
	}

	private ExpenseReportDTO toDTO(ExpenseReport expenseReport) {
		ExpenseReportDTO dto = new ExpenseReportDTO();
		dto.setExpenseId(expenseReport.getExpenseId());
		dto.setProductName(expenseReport.getProductName());
		dto.setExpenseUnit(expenseReport.getExpenseUnit());
		dto.setExpenseAmount(expenseReport.getExpenseAmount());
		dto.setExpenseDate(expenseReport.getExpenseDate());
		dto.setExpenseTime(expenseReport.getExpenseTime());
		dto.setSupplierName(expenseReport.getSupplierName());
		dto.setPayerName(expenseReport.getPayerName());
		dto.setSource(expenseReport.getSource());
		return dto;
	}

	public String generateNextExpenseId() {
		ExpenseReport lastExpenseReport = expenseRepository.findTopByOrderByIdDesc();
		String lastExpenseId = (lastExpenseReport != null) ? lastExpenseReport.getExpenseId() : null;
		int nextNumber = 1;

		if (lastExpenseId != null && lastExpenseId.startsWith("EXP-")) {
			try {
				nextNumber = Integer.parseInt(lastExpenseId.substring(4)) + 1;
			} catch (NumberFormatException e) {
				log.warn("⚠️ ExpenseId format corrupted: {}", lastExpenseId);
			}
		}

		return String.format("EXP-%04d", nextNumber);
	}

	private void exportToExcel() {
		try {
			int recordCount = excelExpenseReportExportService.exportExpenseReportsToExcel();
			log.info("📤 Export completed successfully — {} expense records exported to Excel", recordCount);
		} catch (IOException e) {
			log.error("❌ Export to Excel failed after expense mutation. Reason: {}", e.getMessage(), e);
		}
	}
}