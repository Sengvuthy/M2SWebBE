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
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_name", nullable = false)
    private String name;

    private String phone;

    @Column(name = "email", nullable = true)
    private String email;

    private String address;

    // for General Supplier
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    // for Delete to not relate to Product
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
