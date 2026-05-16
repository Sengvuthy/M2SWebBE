package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.SupplierDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelSupplierExportService;
import SuperiorPro.SuperiorPOS.entity.Supplier;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.mapper.SupplierMapper;
import SuperiorPro.SuperiorPOS.repository.SupplierRepository;
import SuperiorPro.SuperiorPOS.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ExcelSupplierExportService excelSupplierExportService;

    @Override
    public SupplierDTO save(SupplierDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "Supplier name is required");
        }

        Supplier supplier = supplierMapper.toSupplier(dto);
        supplier.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail());

        Supplier saved = supplierRepository.save(supplier);
        exportToExcel();
        return supplierMapper.toDTO(saved);
    }

    @Override
    public SupplierDTO getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                "Supplier with ID %d is not found".formatted(id)));
        return supplierMapper.toDTO(supplier);
    }

    @Override
    public SupplierDTO updateById(Long id, SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                "Supplier with ID %d is not found".formatted(id)));

        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());

        Supplier updated = supplierRepository.save(supplier);
        exportToExcel();
        return supplierMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", id.toString()));

        // 🚫 Prevent deleting General Supplier
        if (Boolean.TRUE.equals(supplier.getIsDefault()) || "General".equalsIgnoreCase(supplier.getName())) {
            throw new API_Exception(HttpStatus.BAD_REQUEST, "General Supplier cannot be deleted");
        }

        supplier.setActive(false);
        supplierRepository.save(supplier);
        exportToExcel();
    }
    
    @Override
    public Page<SupplierDTO> getSuppliers(String name, Pageable pageable) {
        Page<Supplier> suppliers;
        if (name == null || name.trim().isEmpty()) {
            suppliers = supplierRepository.findByActiveTrue(pageable);
        } else {
            suppliers = supplierRepository.findByActiveTrueAndNameContainingIgnoreCase(name.trim(), pageable);
        }
        return suppliers.map(supplierMapper::toDTO);
    }

    @Override
    public boolean existsByName(String name) {
        return supplierRepository.existsByNameIgnoreCase(name.trim());
    }

    private void exportToExcel() {
        try {
            excelSupplierExportService.exportSuppliersToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export suppliers", e);
        }
    }
}
