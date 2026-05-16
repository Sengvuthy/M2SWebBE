package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;

import SuperiorPro.SuperiorPOS.DTO.CustomerDTO;
import SuperiorPro.SuperiorPOS.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDTO toDTO(Customer customer);
    Customer toCustomer(CustomerDTO dto);
}
