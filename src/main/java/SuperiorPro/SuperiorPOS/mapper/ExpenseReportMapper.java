package SuperiorPro.SuperiorPOS.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import SuperiorPro.SuperiorPOS.DTO.ExpenseReportDTO;
import SuperiorPro.SuperiorPOS.DTO.ExpenseReportItem;
import SuperiorPro.SuperiorPOS.entity.ExpenseReport;

@Mapper(componentModel = "spring")
public interface ExpenseReportMapper {

    ExpenseReportDTO toDTO(ExpenseReport expenseReport);

    ExpenseReport toEntity(ExpenseReportDTO expenseReportDTO);

    List<ExpenseReportDTO> toDTOs(List<ExpenseReport> expenseReports);

    List<ExpenseReport> toEntities(List<ExpenseReportDTO> expenseReportDTOs);

    ExpenseReportItem toItem(ExpenseReport entity);

    ExpenseReport toEntity(ExpenseReportItem item);

    List<ExpenseReportItem> toItems(List<ExpenseReport> expenseReports);

    List<ExpenseReport> toEntitiesFromItems(List<ExpenseReportItem> items);

    // 🔹 Automatically calculate expenseAmount after mapping
    @AfterMapping
    default void calculateExpenseAmount(ExpenseReportItem item, @MappingTarget ExpenseReport entity) {
        if (item.getExpensePrice() != null && item.getExpenseUnit() != null) {
            entity.setExpenseAmount(item.getExpensePrice().multiply(item.getExpenseUnit()));
        }
    }
}
