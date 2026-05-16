package SuperiorPro.SuperiorPOS.service;

import SuperiorPro.SuperiorPOS.DTO.ExchangeRateDTO;

public interface ExchangeRateService {
	
    ExchangeRateDTO getRate();
    ExchangeRateDTO setRate(ExchangeRateDTO dto);
}
