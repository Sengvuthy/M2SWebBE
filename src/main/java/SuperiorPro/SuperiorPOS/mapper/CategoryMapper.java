package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;

import SuperiorPro.SuperiorPOS.DTO.CategoryDTO;
import SuperiorPro.SuperiorPOS.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryDTO dto);
    CategoryDTO toCategoryDTO(Category entity);
}
