package SuperiorPro.SuperiorPOS.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.DTO.ExchangeRateDTO;
import SuperiorPro.SuperiorPOS.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    public ExchangeRateDTO getRate() {
        return service.getRate();
    }

    @PostMapping
    public ExchangeRateDTO setRate(@RequestBody ExchangeRateDTO dto) {
        return service.setRate(dto);
    }
}
