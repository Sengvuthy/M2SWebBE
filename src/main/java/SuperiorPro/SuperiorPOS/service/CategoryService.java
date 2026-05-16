package SuperiorPro.SuperiorPOS.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.CategoryDTO;

public interface CategoryService {

    CategoryDTO save(CategoryDTO dto);

    CategoryDTO getById(Long id);

    CategoryDTO update(Long id, CategoryDTO dto);

    Page<CategoryDTO> getCategories(String name, Pageable pageable);

    void deleteById(Long id);

    boolean existsByName(String name);
}
