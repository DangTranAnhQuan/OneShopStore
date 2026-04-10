package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.Rating;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.request.ProductRequest;
import nhom17.OneShop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private RatingService ratingService;

    @GetMapping
    public String listProducts(@RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterStatus", required = false) Boolean status,
                               @RequestParam(name = "filterCategoryId", required = false) Integer categoryId,
                               @RequestParam(name = "filterBrandId", required = false) Integer brandId,
                               @RequestParam(name = "filterSort", required = false) String sort,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {

        Page<Product> productPage = productService.searchProducts(keyword, status, categoryId, brandId, sort, page, size);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        return "admin/products/products";
    }

    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/admin/product";
        }
        List<Rating> reviews = ratingService.findByProductId(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        return "admin/products/productDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showProductForm(@PathVariable(name = "id", required = false) Integer id, Model model,
                                  @RequestParam(name = "filterKeyword", required = false) String keyword,
                                  @RequestParam(name = "filterStatus", required = false) Boolean status,
                                  @RequestParam(name = "filterCategoryId", required = false) Integer categoryId,
                                  @RequestParam(name = "filterBrandId", required = false) Integer brandId,
                                  @RequestParam(name = "filterSort", required = false) String sort,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        ProductRequest productRequest = new ProductRequest();
        if (id != null) {
            Product product = productService.findById(id);

            productRequest.setProductId(product.getProductId());
            productRequest.setName(product.getName());
            productRequest.setDescription(product.getDescription());
            productRequest.setPrice(product.getPrice());
            productRequest.setOriginalPrice(product.getOriginalPrice());
            productRequest.setExpirationDays(product.getExpirationDays() != null ? product.getExpirationDays() : 0);
            productRequest.setImageUrl(product.getImageUrl());
            productRequest.setActive(product.isActive());
            productRequest.setCategoryId(product.getCategory().getCategoryId());
            productRequest.setBrandId(product.getBrand().getBrandId());
        }

        model.addAttribute("product", productRequest);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/products/addOrEditProduct";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") ProductRequest productRequest,
                              BindingResult bindingResult,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterStatus", required = false) Boolean status,
                              @RequestParam(name = "filterCategoryId", required = false) Integer categoryId,
                              @RequestParam(name = "filterBrandId", required = false) Integer brandId,
                              @RequestParam(name = "filterSort", required = false) String sort,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("brands", brandService.findAll());
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("brandId", brandId);
            model.addAttribute("sort", sort);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "admin/products/addOrEditProduct";
        }

        try {
            if (!imageFile.isEmpty()) {
                String fileName = storageService.storeFile(imageFile, "products");
                productRequest.setImageUrl(fileName);
            }
            productService.save(productRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công!");
            redirectAttributes.addAttribute("filterKeyword", keyword);
            redirectAttributes.addAttribute("filterStatus", status);
            redirectAttributes.addAttribute("filterCategoryId", categoryId);
            redirectAttributes.addAttribute("filterBrandId", brandId);
            redirectAttributes.addAttribute("filterSort", sort);
            redirectAttributes.addAttribute("page", page);
            redirectAttributes.addAttribute("size", size);
            return "redirect:/admin/product";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("brands", brandService.findAll());
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("brandId", brandId);
            model.addAttribute("sort", sort);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "admin/products/addOrEditProduct";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes redirectAttributes,
                                @RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(name = "filterStatus", required = false) Boolean status,
                                @RequestParam(name = "filterCategoryId", required = false) Integer categoryId,
                                @RequestParam(name = "filterBrandId", required = false) Integer brandId,
                                @RequestParam(name = "filterSort", required = false) String sort,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterStatus", status);
        redirectAttributes.addAttribute("filterCategoryId", categoryId);
        redirectAttributes.addAttribute("filterBrandId", brandId);
        redirectAttributes.addAttribute("filterSort", sort);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/product";
    }
}
