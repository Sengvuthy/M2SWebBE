package SuperiorPro.SuperiorPOS.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import SuperiorPro.SuperiorPOS.DTO.CustomerDTO;
import SuperiorPro.SuperiorPOS.DTO.PageDTO;
import SuperiorPro.SuperiorPOS.Excel.config.ExcelPathResolver;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerExportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerImportService;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerImportService.ImportSummary;
import SuperiorPro.SuperiorPOS.entity.Customer;
import SuperiorPro.SuperiorPOS.mapper.CustomerMapper;
import SuperiorPro.SuperiorPOS.repository.CustomerRepository;
import SuperiorPro.SuperiorPOS.service.CustomerService;
import SuperiorPro.SuperiorPOS.service.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customers")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final ExcelCustomerImportService excelCustomerImportService;
    private final ExcelCustomerExportService excelCustomerExportService;
    private final CustomerMapper customerMapper;

    // ✅ Create customer
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO dto) {
        if (dto.getPhones() == null || dto.getPhones().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Customer customer = customerMapper.toCustomer(dto);
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(customerMapper.toDTO(saved));
    }

    // 📞 Search by phone (normalized)
    @GetMapping("/search-by-phone")
    public ResponseEntity<CustomerDTO> findByPhone(@RequestParam String phone) {
        return customerService.findByPhone(phone)
                .map(customerMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔍 Get by ID
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return ResponseEntity.ok(customerMapper.toDTO(customer));
    }

    // 🔄 Update by ID
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomerById(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        Customer updated = customerService.updateById(id, dto);
        return ResponseEntity.ok(customerMapper.toDTO(updated));
    }

    // ❌ Delete by ID
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable Long id) {
        customerService.deleteById(id);
        return ResponseEntity.ok("✅ Customer deleted successfully!");
    }

    // 🔎 Search with pagination
    @GetMapping("/search")
    public ResponseEntity<PageDTO<CustomerDTO>> searchCustomers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name,asc") String sort) {

        Pageable pageable = PageUtil.getPageable(page, size, Sort.by(sort.split(",")[0]));
        Page<Customer> customers = customerService.getCustomers(name, pageable);
        PageDTO<CustomerDTO> pageDTO = new PageDTO<>(customers.map(customerMapper::toDTO));
        return ResponseEntity.ok(pageDTO);
    }

    // 🔍 Search by Telegram ID
    @GetMapping("/search-by-telegram-id")
    public ResponseEntity<CustomerDTO> findByTelegramId(@RequestParam Long telegramId) {
        return customerService.findByTelegramId(telegramId)
                .map(customerMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 // For testing/admin use only
    @PostMapping("/bind-telegram-by-phone")
    public ResponseEntity<CustomerDTO> bindTelegramByPhone(@RequestParam String phone) {
        CustomerDTO updated = customerService.bindTelegramByPhone(phone);
        return ResponseEntity.ok(updated);
    }

    // For real Telegram binding (webhook)
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleUpdate(@RequestBody Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (message != null) {
            Map<String, Object> from = (Map<String, Object>) message.get("from");
            Long chatId = ((Number) from.get("id")).longValue();
            String text = (String) message.get("text");

            if (text != null && text.startsWith("/bind")) {
                String phone = text.replace("/bind", "").trim();
                customerService.bindTelegramByPhone(phone, chatId); // ✅ pass both
            }
        }
        return ResponseEntity.ok().build();
    }

    // 📥 Import from Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<String> importCustomersFromExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Customers");
            ImportSummary summary = excelCustomerImportService.importCustomersFromExcel(filePath);

            StringBuilder response = new StringBuilder()
                    .append("📥 Imported customers from: ").append(filePath)
                    .append("\n✅ Created: ").append(summary.created())
                    .append("\n🔄 Updated: ").append(summary.updated())
                    .append("\n❌ Errors: ").append(summary.errors().size());

            return ResponseEntity.ok(response.toString());
        } catch (IOException e) {
            log.error("❌ Import failed", e);
            return ResponseEntity.internalServerError().body("❌ Import failed: " + e.getMessage());
        }
    }

    // 📤 Export to Excel
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportCustomersToExcel() {
        try {
            String filePath = ExcelPathResolver.resolveFixedPath("Customers");
            int count = excelCustomerExportService.exportCustomersToExcel();
            log.info("📤 Exported {} customers to {}", count, filePath);
            return ResponseEntity.ok("✅ Exported " + count + " customers to: " + filePath);
        } catch (IOException e) {
            log.error("❌ Export failed", e);
            return ResponseEntity.internalServerError().body("❌ Export failed: " + e.getMessage());
        }
    }
}
