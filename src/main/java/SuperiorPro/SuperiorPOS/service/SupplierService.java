package SuperiorPro.SuperiorPOS.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.SupplierDTO;

public interface SupplierService {

    SupplierDTO save(SupplierDTO dto);

    SupplierDTO getById(Long id);

    SupplierDTO updateById(Long id, SupplierDTO dto);

    void deleteById(Long id);

    Page<SupplierDTO> getSuppliers(String name, Pageable pageable);

    boolean existsByName(String name);
}
