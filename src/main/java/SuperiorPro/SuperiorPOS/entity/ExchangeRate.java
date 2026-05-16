package SuperiorPro.SuperiorPOS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "exchange_rate")
public class ExchangeRate {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 1L;
	
	@Column(name = "exchange_rate", nullable = false)
    private int rate;
}
