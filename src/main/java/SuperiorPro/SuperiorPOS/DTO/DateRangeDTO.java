package SuperiorPro.SuperiorPOS.DTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DateRangeDTO {
	
    private LocalDate startDate;
    private LocalDate endDate;
}
