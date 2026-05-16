package SuperiorPro.SuperiorPOS.DTO;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageDTO<T> {
    private List<T> list;
    private PaginationDTO paginationDTO;

    public PageDTO(Page<T> page) {
        this.list = page.getContent();
        this.paginationDTO = PaginationDTO.builder()
            .pageNumber(page.getPageable().getPageNumber() + 1)
            .pageSize(page.getPageable().getPageSize())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .numberOfElements(page.getNumberOfElements())
            .first(page.isFirst())
            .last(page.isLast())
            .empty(page.isEmpty())
            .build();
    }
}
