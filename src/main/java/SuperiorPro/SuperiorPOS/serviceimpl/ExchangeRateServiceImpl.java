package SuperiorPro.SuperiorPOS.serviceimpl;

import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.DTO.ExchangeRateDTO;
import SuperiorPro.SuperiorPOS.entity.ExchangeRate;
import SuperiorPro.SuperiorPOS.mapper.ExchangeRateMapper;
import SuperiorPro.SuperiorPOS.repository.ExchangeRateRepository;
import SuperiorPro.SuperiorPOS.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository repo;
    private final ExchangeRateMapper mapper;

    @Override
    public ExchangeRateDTO getRate() {
        ExchangeRate er = repo.findTopByOrderByIdDesc()
            .orElseGet(() -> {
                ExchangeRate defaultRate = new ExchangeRate();
                defaultRate.setRate(4000);
                return repo.save(defaultRate);
            });
        return mapper.toDTO(er);
    }

    @Override
    public ExchangeRateDTO setRate(ExchangeRateDTO dto) {
        // Always create a new record with the updated rate
        ExchangeRate er = new ExchangeRate();
        er.setRate(dto.getRate());
        ExchangeRate saved = repo.save(er);
        return mapper.toDTO(saved);
    }
}
