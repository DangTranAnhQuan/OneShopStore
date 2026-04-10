package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.request.BrandRequest;
import nhom17.OneShop.service.BrandService;
import nhom17.OneShop.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private StorageService storageService;

    @GetMapping
    public String listBrands(@RequestParam(name = "filterKeyword", required = false) String keyword,
                             @RequestParam(name = "filterStatus", required = false) Boolean status,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size,
                             Model model) {
        Page<Brand> brandPage = brandService.searchAndFilter(keyword, status, page, size);
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        if (!model.containsAttribute("brand")) {
            model.addAttribute("brand", new BrandRequest());
        }
        return "admin/products/brands";
    }

    @PostMapping("/save")
    public String saveBrand(@Valid @ModelAttribute BrandRequest brandRequest,
                            BindingResult bindingResult,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(name = "filterKeyword", required = false) String keyword,
                            @RequestParam(name = "filterStatus", required = false) Boolean status,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("brand", brandRequest);
        }

        else {
            try {
                if (!imageFile.isEmpty()) {
                    String fileName = storageService.storeFile(imageFile, "brands");
                    brandRequest.setImageUrl(fileName);
                }
                brandService.save(brandRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu thương hiệu thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("brand", brandRequest);
            }
        }

        // Giữ lại tham số phân trang và filter khi redirect
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("filterKeyword", keyword);
        if (status != null) redirectAttributes.addAttribute("filterStatus", status);
        return "redirect:/admin/brand";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable int id,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              @RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterStatus", required = false) Boolean status,
                              RedirectAttributes redirectAttributes) {
        try {
            brandService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("filterKeyword", keyword);
        if (status != null) redirectAttributes.addAttribute("filterStatus", status);
        return "redirect:/admin/brand";
    }
}
