package SuperiorPro.SuperiorPOS.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportIdSummaryDTO {

    private String importId;
    private String supplierName;
    private String importerName;
    private LocalDate importDate;
    private LocalTime importTime;
    private int itemCount;
    private double totalAmount;
}
