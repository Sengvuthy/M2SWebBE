package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.CustomerDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCustomerExportService;
import SuperiorPro.SuperiorPOS.entity.Customer;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.mapper.CustomerMapper;
import SuperiorPro.SuperiorPOS.repository.CustomerRepository;
import SuperiorPro.SuperiorPOS.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

	private final CustomerMapper mapper;
	private final CustomerRepository customerRepository;
	private final TelegramBotServiceImpl telegramBotService;
	private final ExcelCustomerExportService excelCustomerExportService;

	@Override
	public Customer save(Customer customer) {
	    if (customer.getName() == null || customer.getName().trim().isEmpty()) {
	        throw new API_Exception(HttpStatus.BAD_REQUEST, "Customer name is required");
	    }
	    if (customer.getPhones() != null) {
	        for (String phone : customer.getPhones()) {
	            if (customerRepository.existsByPhone(phone)) {
	                throw new API_Exception(HttpStatus.CONFLICT, "Customer phone already exists: " + phone);
	            }
	        }
	    }
	    
	    if (customer.getTelegramId() != null &&
	    	    customerRepository.existsByTelegramId(customer.getTelegramId())) {
	    	    throw new API_Exception(HttpStatus.CONFLICT,
	    	        "Telegram account already bound");
	    	}

	    Customer saved = customerRepository.save(customer);
	    exportToExcel();
	    return saved;
	}
	
	@Override
	public Customer getById(Long id) {
		return customerRepository.findById(id).orElseThrow(
				() -> new API_Exception(HttpStatus.NOT_FOUND, "Customer with ID %d is not found".formatted(id)));
	}

	@Override
	public Customer updateById(Long id, CustomerDTO dto) {
	    Customer customer = getById(id);

	    if (dto.getPhones() != null) {
	        for (String phone : dto.getPhones()) {
	            if (!customer.getPhones().contains(phone) && customerRepository.existsByPhone(phone)) {
	                throw new API_Exception(HttpStatus.CONFLICT, "Customer phone already exists: " + phone);
	            }
	        }
	        customer.setPhones(dto.getPhones());
	    }

	    if (dto.getTelegramId() != null &&
	    	    !dto.getTelegramId().equals(customer.getTelegramId()) &&
	    	    customerRepository.existsByTelegramId(dto.getTelegramId())) {
	    	    throw new API_Exception(HttpStatus.CONFLICT,
	    	        "Telegram account already bound");
	    	}

	    	customer.setTelegramId(dto.getTelegramId() != null
	    	    ? dto.getTelegramId()
	    	    : customer.getTelegramId());
	    	
	    customer.setName(dto.getName());
	    customer.setAddresses(dto.getAddresses() != null ? dto.getAddresses() : customer.getAddresses());
	    customer.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : customer.getIsDefault());

	    Customer updated = customerRepository.save(customer);
	    exportToExcel();
	    return updated;
	}
	
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (!customerRepository.existsById(id)) {
			throw new ResourceNotFoundException("Customer", id.toString());
		}
		customerRepository.deleteById(id);
		exportToExcel();
	}

	@Override
	public Page<Customer> getCustomers(String name, Pageable pageable) {
		if (name == null || name.trim().isEmpty()) {
			return customerRepository.findAll(pageable);
		}
		return customerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
	}

	@Override
	public boolean existsByPhone(String phone) {
		return customerRepository.existsByPhone(phone);
	}

	@Override
	public Optional<Customer> findByPhone(String phone) {
		return customerRepository.findByPhone(phone);
	}
	
	@Override
	public boolean existsByTelegramId(Long telegramId) {
	    return customerRepository.existsByTelegramId(telegramId);
	}

	@Override
	public Optional<Customer> findByTelegramId(Long telegramId) {
	    return customerRepository.findByTelegramId(telegramId);
	}
	
	@Override
	public CustomerDTO bindTelegramByPhone(String phone) {
	    Long telegramId = telegramBotService.verifyAndGetTelegramId(phone); // stub
	    Customer customer = customerRepository.findByPhone(phone)
	        .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND, "Customer not found"));

	    customer.setTelegramId(telegramId);
	    Customer saved = customerRepository.save(customer);
	    exportToExcel();
	    return mapper.toDTO(saved);
	}

	@Override
	public CustomerDTO bindTelegramByPhone(String phone, Long chatId) {
	    Customer customer = customerRepository.findByPhone(phone)
	        .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND, "Customer not found"));

	    customer.setTelegramId(chatId);
	    Customer saved = customerRepository.save(customer);
	    exportToExcel();

	    telegramBotService.sendMessage(chatId, "✅ Your account is now linked!");
	    return mapper.toDTO(saved);
	}

	private void exportToExcel() {
		try {
			excelCustomerExportService.exportCustomersToExcel();
		} catch (IOException e) {
			log.error("❌ Failed to export customers", e);
		}
	}
}
