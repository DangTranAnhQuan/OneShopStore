package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.repository.BrandRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.request.BrandRequest;
import nhom17.OneShop.service.BrandService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.specification.BrandSpecification;
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
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StorageService storageService;

    @Autowired private
    ProductRepository productRepository;

    @Override
    public Page<Brand> searchAndFilter(String keyword, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("brandId").ascending());

        Specification<Brand> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(BrandSpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(BrandSpecification.hasStatus(status));
        }

        return brandRepository.findAll(spec, pageable);
    }

    @Override
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    @Override
    public Brand findById(int id) {
        return brandRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(BrandRequest brandRequest) {
        validateUniqueBrandName(brandRequest);
        Brand brand = prepareBrandEntity(brandRequest);
        String oldImage = brand.getImageUrl();
        mapRequestToEntity(brandRequest, brand);
        brandRepository.save(brand);

        if (StringUtils.hasText(brandRequest.getImageUrl()) && StringUtils.hasText(oldImage) && !oldImage.equals(brandRequest.getImageUrl())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueBrandName(BrandRequest request) {
        if (request.getBrandId() == null) {
            if (brandRepository.existsByBrandNameIgnoreCase(request.getBrandName())) {
                throw new DuplicateRecordException("Tên thương hiệu '" + request.getBrandName() + "' đã tồn tại.");
            }
        } else {
            if (brandRepository.existsByBrandNameIgnoreCaseAndBrandIdNot(request.getBrandName(), request.getBrandId())) {
                throw new DuplicateRecordException("Tên thương hiệu '" + request.getBrandName() + "' đã được sử dụng.");
            }
        }
    }

    private Brand prepareBrandEntity(BrandRequest request) {
        if (request.getBrandId() != null) {
            return findById(request.getBrandId());
        }
        return new Brand(request.getBrandName(), request.getImageUrl(), request.getDescription(), request.isActive());
    }

    private void mapRequestToEntity(BrandRequest request, Brand brand) {
        String imageUrl = StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl() : brand.getImageUrl();
        brand.update(request.getBrandName(), imageUrl, request.getDescription(), request.isActive());
    }

    @Override
    @Transactional
    public void delete(int id) {
        Brand brandToDelete = findById(id);
        if (productRepository.existsByBrand_BrandId(id)) {
            throw new DataIntegrityViolationException("Không thể xóa thương hiệu '" + brandToDelete.getBrandName() + "' vì vẫn còn sản phẩm thuộc thương hiệu này.");
        }
        if (StringUtils.hasText(brandToDelete.getImageUrl())) {
            storageService.deleteFile(brandToDelete.getImageUrl());
        }
        brandRepository.delete(brandToDelete);
    }
}
