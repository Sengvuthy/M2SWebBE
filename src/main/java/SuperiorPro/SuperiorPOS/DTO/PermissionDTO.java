package SuperiorPro.SuperiorPOS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionDTO {

    private Long id; // ✅ include ID for updates and references

    @NotBlank
    @Size(max = 255)
    private String permissionName;

    @Size(max = 255)
    private String description;
}
