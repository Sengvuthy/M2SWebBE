package SuperiorPro.SuperiorPOS.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.CustomerDTO;
import SuperiorPro.SuperiorPOS.entity.Customer;

public interface CustomerService {
	
    Customer save(Customer customer);
    Customer getById(Long id);
    Customer updateById(Long id, CustomerDTO customerDTO);
    void deleteById(Long id);
    Page<Customer> getCustomers(String name, Pageable pageable);

    boolean existsByPhone(String phone);
    Optional<Customer> findByPhone(String phone);
    
    boolean existsByTelegramId(Long telegramId);
    Optional<Customer> findByTelegramId(Long telegramId);
    
    CustomerDTO bindTelegramByPhone(String phone);
    CustomerDTO bindTelegramByPhone(String phone, Long chatId);
}
