package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;

import SuperiorPro.SuperiorPOS.DTO.ExchangeRateDTO;
import SuperiorPro.SuperiorPOS.entity.ExchangeRate;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    ExchangeRateDTO toDTO(ExchangeRate exchange_rate);

    ExchangeRate toEntity(ExchangeRateDTO dto);
}
