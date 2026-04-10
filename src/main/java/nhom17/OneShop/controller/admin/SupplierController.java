package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.Supplier;
import nhom17.OneShop.request.SupplierRequest;
import nhom17.OneShop.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/supplier")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public String listSuppliers(@RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model) {
        Page<Supplier> supplierPage = supplierService.search(keyword, page, size);
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("keyword", keyword);
        if (!model.containsAttribute("supplier")) {
            model.addAttribute("supplier", new SupplierRequest());
        }
        return "admin/warehouse/suppliers";
    }

    @PostMapping("/save")
    public String saveSupplier(@Valid @ModelAttribute SupplierRequest supplierRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("supplier", supplierRequest);
        }
        else {
            try {
                supplierService.save(supplierRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu nhà cung cấp thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("supplier", supplierRequest);
            }
        }
        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/supplier";
    }

    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable int id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(name = "filterKeyword", required = false) String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int size) {
        try {
            supplierService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/supplier";
    }
}
