package SuperiorPro.SuperiorPOS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleDTO {

    private Long id; // ✅ include ID for updates/references

    @NotBlank
    @Size(max = 255)
    private String roleName;

    @Size(max = 255)
    private String description;
}
