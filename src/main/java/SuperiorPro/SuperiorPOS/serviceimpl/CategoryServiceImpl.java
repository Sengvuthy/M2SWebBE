package SuperiorPro.SuperiorPOS.serviceimpl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SuperiorPro.SuperiorPOS.DTO.CategoryDTO;
import SuperiorPro.SuperiorPOS.ExcelImportExport.ExcelCategoryExportService;
import SuperiorPro.SuperiorPOS.entity.Category;
import SuperiorPro.SuperiorPOS.exception.API_Exception;
import SuperiorPro.SuperiorPOS.mapper.CategoryMapper;
import SuperiorPro.SuperiorPOS.repository.CategoryRepository;
import SuperiorPro.SuperiorPOS.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ExcelCategoryExportService excelCategoryExportService;

    @Override
    public CategoryDTO save(CategoryDTO dto) {
        String name = dto.getName().trim();
        if (existsByName(name)) {
            throw new API_Exception(HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = categoryMapper.toCategory(dto);
        category.setName(name);

        Category saved = categoryRepository.save(category);
        exportToExcel();
        return categoryMapper.toCategoryDTO(saved);
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "Category with ID %d not found".formatted(id)));
        return categoryMapper.toCategoryDTO(category);
    }

    @Override
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "Category with ID %d not found".formatted(id)));

        String newName = dto.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) && existsByName(newName)) {
            throw new API_Exception(HttpStatus.CONFLICT, "Category name already exists");
        }

        category.setName(newName);
        Category updated = categoryRepository.save(category);
        exportToExcel();
        return categoryMapper.toCategoryDTO(updated);
    }

    @Override
    public Page<CategoryDTO> getCategories(String name, Pageable pageable) {
        Page<Category> categories;
        if (name == null || name.trim().isEmpty()) {
            categories = categoryRepository.findAll(pageable);
        } else {
            categories = categoryRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        }
        return categories.map(categoryMapper::toCategoryDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new API_Exception(HttpStatus.NOT_FOUND,
                        "Category with ID %d not found".formatted(id)));
        categoryRepository.delete(category);
        exportToExcel();
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name.trim());
    }

    private void exportToExcel() {
        try {
            excelCategoryExportService.exportCategoriesToExcel();
        } catch (IOException e) {
            log.error("❌ Failed to export categories", e);
        }
    }
}
