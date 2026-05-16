package SuperiorPro.SuperiorPOS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SellerDTO {
    private Long id;

    @NotBlank(message = "Seller name is required")
    private String name;

    private String employeeCode;

    @Pattern(regexp = "^[0-9]{9,15}$", message = "Invalid phone number")
    private String phone;
}
