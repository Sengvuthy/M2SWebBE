package SuperiorPro.SuperiorPOS.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import SuperiorPro.SuperiorPOS.DTO.ProductDTO;
import SuperiorPro.SuperiorPOS.DTO.ProductImportItem;
import SuperiorPro.SuperiorPOS.entity.Product;
import SuperiorPro.SuperiorPOS.entity.ProductImport;
import SuperiorPro.SuperiorPOS.mapper.ProductMapper;

public abstract class ProductMapperDecorator implements ProductMapper {

    @Autowired
    private ProductMapper delegate;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ProductDTO toProductDTO(Product product) {
        ProductDTO dto = delegate.toProductDTO(product);

        if (product.getImagePath() != null && !product.getImagePath().isBlank()) {
            String cleanBaseUrl = baseUrl.replaceAll("/$", "");
            String cleanPath = product.getImagePath().replaceFirst("^/+", "");
            dto.setImagePath(cleanBaseUrl + "/uploads/products/" + cleanPath);
        }

        return dto;
    }

    @Override
    public Product toProduct(ProductDTO dto) {
        return delegate.toProduct(dto);
    }

    @Override
    public ProductImport toEntity(ProductImportItem item) {
        return delegate.toEntity(item);
    }

    @Override
    public ProductImportItem toItem(ProductImport entity) {
        return delegate.toItem(entity);
    }
}
