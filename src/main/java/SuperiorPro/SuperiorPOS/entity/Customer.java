package SuperiorPro.SuperiorPOS.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String name;

    // Multiple phone numbers
    @ElementCollection
    @CollectionTable(name = "customer_phones", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "phone", nullable = false)
    private List<String> phones = new ArrayList<>();

    // Multiple addresses
    @ElementCollection
    @CollectionTable(name = "customer_addresses", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "address")
    private List<String> addresses = new ArrayList<>();

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "telegram_id")
    private Long telegramId;
}
