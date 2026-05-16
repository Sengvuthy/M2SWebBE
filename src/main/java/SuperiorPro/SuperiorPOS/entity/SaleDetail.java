package SuperiorPro.SuperiorPOS.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class SaleDetail {
	// This class is used with SaleReport class to create Daily, Monthly, Yearly Report
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sale_detail_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "sale_id", nullable = false)
	private Sale sale;
	
	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	@Column(name = "sold_amount", nullable = false)
	private BigDecimal soldAmount;
	
	@Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
}
