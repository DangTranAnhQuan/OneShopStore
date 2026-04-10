package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.request.VoucherRequest;
import nhom17.OneShop.service.VoucherService;
import nhom17.OneShop.entity.enums.VoucherStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public String listVouchers(@RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterStatus", required = false) VoucherStatus status,
                               @RequestParam(required = false) Integer filterKieuApDung,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<Voucher> voucherPage = voucherService.findAll(keyword, status, filterKieuApDung, page, size);
        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("filterKieuApDung", filterKieuApDung);
        return "admin/orders/vouchers";
    }

    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable String id, Model model) {
        model.addAttribute("voucher", voucherService.findById(id));
        return "admin/orders/voucherDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showVoucherForm(@PathVariable(required = false) String id,
                                  Model model,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(name = "filterKeyword", required = false) String keyword,
                                  @RequestParam(name = "filterStatus", required = false) VoucherStatus status,
                                  @RequestParam(required = false) Integer filterKieuApDung) {
        VoucherRequest request = new VoucherRequest();
        boolean isEditMode = false;

        if (id != null) {
            isEditMode = true;
            Voucher voucher = voucherService.findById(id);
            request.setVoucherCode(voucher.getVoucherCode());
            request.setCampaignName(voucher.getCampaignName());
            request.setDiscountType(voucher.getDiscountType());
            request.setValue(voucher.getValue());
            request.setStartsAt(voucher.getStartsAt());
            request.setEndsAt(voucher.getEndsAt());
            request.setMinimumOrderAmount(voucher.getMinimumOrderAmount());
            request.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
            request.setTotalUsageLimit(voucher.getTotalUsageLimit());
            request.setPerUserLimit(voucher.getPerUserLimit());
            request.setStatus(voucher.getStatus());
        }
        model.addAttribute("isEditMode", isEditMode);
        model.addAttribute("voucherRequest", request);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("filterKieuApDung", filterKieuApDung);
        return "admin/orders/addOrEditVoucher";
    }

    @PostMapping("/save")
    public String saveVoucher(@Valid @ModelAttribute("voucherRequest") VoucherRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(defaultValue = "false") boolean isEditMode,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              @RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterStatus", required = false) VoucherStatus status,
                              @RequestParam(required = false) Integer filterKieuApDung) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("isEditMode", isEditMode);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("filterKieuApDung", filterKieuApDung);
            return "admin/orders/addOrEditVoucher";
        }

        try {
            voucherService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu khuyến mãi thành công!");
            redirectAttributes.addAttribute("page", page)
                    .addAttribute("size", size)
                    .addAttribute("filterKeyword", keyword)
                    .addAttribute("filterStatus", status)
                    .addAttribute("filterKieuApDung", filterKieuApDung);
            return "redirect:/admin/voucher";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEditMode", isEditMode);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("filterKieuApDung", filterKieuApDung);
            return "admin/orders/addOrEditVoucher";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(name = "filterStatus", required = false) VoucherStatus status,
                                @RequestParam(required = false) Integer filterKieuApDung) {
        try {
            voucherService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page)
                .addAttribute("size", size)
                .addAttribute("filterKeyword", keyword)
                .addAttribute("filterStatus", status)
                .addAttribute("filterKieuApDung", filterKieuApDung);
        return "redirect:/admin/voucher";
    }
}