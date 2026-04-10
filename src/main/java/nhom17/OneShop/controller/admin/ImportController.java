package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.ImportDetail;
import nhom17.OneShop.entity.Import;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.request.ImportDetailRequest;
import nhom17.OneShop.request.ImportRequest;
import nhom17.OneShop.service.ImportService;
import nhom17.OneShop.service.ProductService;
import nhom17.OneShop.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/import")
public class ImportController {

    @Autowired
    private ImportService importService;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private ProductService productService;

    @GetMapping
    public String listImports(@RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterSupplierId", required = false) Integer supplierId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model) {
        Page<Import> importPage = importService.findAll(keyword, supplierId, page, size);
        model.addAttribute("importPage", importPage);
        model.addAttribute("suppliers", supplierService.findAll(Sort.by("supplierName")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        return "admin/warehouse/imports";
    }

    @GetMapping("/{id}")
    public String viewImportDetail(@PathVariable int id, Model model) {
        Import importReceipt = importService.findById(id);
        if (importReceipt == null) {
            return "redirect:/admin/import";
        }
        model.addAttribute("receipt", importReceipt);
        return "admin/warehouse/importDetail";
    }

    @GetMapping("/add")
    public String showAddForm(Model model,
                              @RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterSupplierId", required = false) Integer supplierId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size) {
        model.addAttribute("importRequest", new ImportRequest());
        model.addAttribute("suppliers", supplierService.findAll(Sort.by("supplierName")));
        model.addAttribute("products", productService.findAll(Sort.by("name")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/warehouse/importForm";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model,
                               @RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterSupplierId", required = false) Integer supplierId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        Import importReceipt = importService.findById(id);

        ImportRequest request = new ImportRequest();
        request.setImportId(importReceipt.getImportId());
        request.setSupplierId(importReceipt.getSupplier().getSupplierId());

        List<ImportDetailRequest> detailRequests = new ArrayList<>();
        for (ImportDetail detail : importReceipt.getImportDetails()) {
            ImportDetailRequest detailDto = new ImportDetailRequest();
            detailDto.setProductId(detail.getProduct().getProductId());
            detailDto.setQuantity(detail.getQuantity());
            detailDto.setImportPrice(detail.getImportPrice());
            detailRequests.add(detailDto);
        }
        request.setImportDetails(detailRequests);

        model.addAttribute("importRequest", request);
        model.addAttribute("suppliers", supplierService.findAll(Sort.by("supplierName")));
        model.addAttribute("products", productService.findAll(Sort.by("name")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/warehouse/importForm";
    }

    @GetMapping("/history/{productId}")
    public String showImportHistory(@PathVariable Integer productId, Model model) {
        Product product = productService.findById(productId);
        List<ImportDetail> historyList = importService.getHistoryForProduct(productId);
        model.addAttribute("product", product);
        model.addAttribute("historyList", historyList);
        return "admin/warehouse/importHistory";
    }

    @PostMapping("/save")
    public String saveImport(@Valid @ModelAttribute ImportRequest importRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "filterKeyword", required = false) String keyword,
                             @RequestParam(name = "filterSupplierId", required = false) Integer supplierId,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

            model.addAttribute("errorMessage", errorMessage);

            model.addAttribute("suppliers", supplierService.findAll(Sort.by("supplierName")));
            model.addAttribute("products", productService.findAll(Sort.by("name")));
            model.addAttribute("keyword", keyword);
            model.addAttribute("supplierId", supplierId);
            model.addAttribute("page", page);
            model.addAttribute("size", size);

            return "admin/warehouse/importForm";
        }

        try {
            importService.save(importRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Lưu phiếu nhập thành công!");
            redirectAttributes.addAttribute("filterKeyword", keyword);
            redirectAttributes.addAttribute("filterSupplierId", supplierId);
            redirectAttributes.addAttribute("page", page);
            redirectAttributes.addAttribute("size", size);
            return "redirect:/admin/import";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("suppliers", supplierService.findAll(Sort.by("supplierName")));
            model.addAttribute("products", productService.findAll(Sort.by("name")));
            model.addAttribute("keyword", keyword);
            model.addAttribute("supplierId", supplierId);
            model.addAttribute("page", page);
            model.addAttribute("size", size);

            return "admin/warehouse/importForm";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteImport(@PathVariable int id,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterSupplierId", required = false) Integer supplierId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        importService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa phiếu nhập thành công!");
        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterSupplierId", supplierId);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/import";
    }
}
