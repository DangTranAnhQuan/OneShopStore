package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Category;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.repository.CategoryRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.request.CategoryRequest;
import nhom17.OneShop.service.CategoryService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.specification.CategorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StorageService storageService;

    @Autowired private
    ProductRepository productRepository;

    @Override
    public Page<Category> searchAndFilter(String keyword, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("categoryId").ascending());

        Specification<Category> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(CategorySpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(CategorySpecification.hasStatus(status));
        }

        return categoryRepository.findAll(spec, pageable);
    }

    @Override
    public Category findById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }
    @Override
    @Transactional
    public void save(CategoryRequest categoryRequest) {
        validateUniqueCategoryName(categoryRequest);
        Category category = prepareCategoryEntity(categoryRequest);
        String oldImage = category.getImageUrl();
        mapRequestToEntity(categoryRequest, category);
        categoryRepository.save(category);

        if (StringUtils.hasText(categoryRequest.getImageUrl()) && StringUtils.hasText(oldImage) && !oldImage.equals(categoryRequest.getImageUrl())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueCategoryName(CategoryRequest request) {
        if (request.getCategoryId() == null) {
            if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
                throw new DuplicateRecordException("Tên danh mục '" + request.getCategoryName() + "' đã tồn tại.");
            }
        } else {
            if (categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(request.getCategoryName(), request.getCategoryId())) {
                throw new DuplicateRecordException("Tên danh mục '" + request.getCategoryName() + "' đã được sử dụng.");
            }
        }
    }

    private Category prepareCategoryEntity(CategoryRequest request) {
        if (request.getCategoryId() != null) {
            return findById(request.getCategoryId());
        }
        return new Category(request.getCategoryName(), request.getImageUrl(), request.isActive());
    }

    private void mapRequestToEntity(CategoryRequest request, Category category) {
        String imageUrl = StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl() : category.getImageUrl();
        category.update(request.getCategoryName(), imageUrl, request.isActive());
    }

    @Override
    @Transactional
    public void delete(int id) {
        Category categoryToDelete = findById(id);
        if (productRepository.existsByCategory_CategoryId(id)) {
            throw new DataIntegrityViolationException("Không thể xóa danh mục '" + categoryToDelete.getCategoryName() + "' vì vẫn còn sản phẩm thuộc danh mục này.");
        }
        if (StringUtils.hasText(categoryToDelete.getImageUrl())) {
            storageService.deleteFile(categoryToDelete.getImageUrl());
        }
        categoryRepository.delete(categoryToDelete);
    }
    
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
