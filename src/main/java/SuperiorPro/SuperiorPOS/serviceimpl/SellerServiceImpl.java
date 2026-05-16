package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.SellerDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSellerExportService;
import SuperiorPro.SuperiorPOS.entity.Seller;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.repository.SellerRepository;
import SuperiorPro.SuperiorPOS.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final ExcelSellerExportService excelSellerExportService;

    @Override
    public Seller save(Seller seller) {
        if (seller.getName() == null || seller.getName().trim().isEmpty()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Seller name is required");
        }
        if (seller.getEmployeeCode() != null && sellerRepository.existsByEmployeeCode(seller.getEmployeeCode())) {
            throw new API_Exception(HttpStatus.CONFLICT, "Employee code already exists");
        }

        Seller saved = sellerRepository.save(seller);
        log.info("✅ Saved seller ID {} with name '{}'", saved.getId(), saved.getName());
        exportToExcel();
        return saved;
    }

    @Override
    public Seller getById(Long id) {
        return sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seller", String.valueOf(id)));
    }

    @Override
    public Page<Seller> getSellers(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return sellerRepository.findAll(pageable);
        }
        return sellerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Override
    @Transactional
    public Seller updateById(Long id, SellerDTO dto) {
        Seller existing = getById(id);

        existing.setName(dto.getName().trim());
        existing.setEmployeeCode(dto.getEmployeeCode() != null ? dto.getEmployeeCode().trim() : null);
        existing.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);

        Seller updated = sellerRepository.save(existing);
        log.info("🔄 Updated seller ID {} with name '{}'", updated.getId(), updated.getName());
        exportToExcel();
        return updated;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Seller seller = getById(id);
        sellerRepository.delete(seller);
        log.info("🗑️ Deleted seller ID {}", id);
        exportToExcel();
    }

    @Override
    public boolean existsByEmployeeCode(String employeeCode) {
        return sellerRepository.existsByEmployeeCode(employeeCode);
    }

    private void exportToExcel() {
        try {
            int count = excelSellerExportService.exportSellersToExcel();
            log.info("📤 Exported {} sellers to Excel", count);
        } catch (IOException e) {
            log.error("❌ Failed to export sellers after mutation", e);
        }
    }
}
