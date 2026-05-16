package SuperiorPro.SuperiorPOS.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.SellerDTO;
import SuperiorPro.SuperiorPOS.entity.Seller;

public interface SellerService {

    Seller save(Seller seller);

    Seller getById(Long id);

    Page<Seller> getSellers(String name, Pageable pageable);

    Seller updateById(Long id, SellerDTO dto);

    void deleteById(Long id);

    boolean existsByEmployeeCode(String employeeCode);
}
