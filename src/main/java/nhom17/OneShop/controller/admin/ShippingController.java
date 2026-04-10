package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.Shipping;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.request.ShippingRequest;
import nhom17.OneShop.service.ShippingCarrierService;
import nhom17.OneShop.service.ShippingFeeService;
import nhom17.OneShop.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/shipping")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;
    @Autowired
    private ShippingCarrierService shippingCarrierService;
    @Autowired
    private ShippingFeeService shippingFeeService;

    @GetMapping
    public String listShippings(@RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(name = "filterCarrierId", required = false) Integer carrierId,
                                @RequestParam(name = "filterStatus", required = false) String status,
                                @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Page<Shipping> shippingPage = shippingService.search(keyword, carrierId, status, shippingMethod, page, size);
        List<ShippingMethod> shippingMethods = shippingFeeService.findDistinctShippingMethods();
        model.addAttribute("shippingPage", shippingPage);
        model.addAttribute("carriers", shippingCarrierService.findAll());
        if (!model.containsAttribute("shippingRequest")) {
            model.addAttribute("shippingRequest", new ShippingRequest());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("carrierId", carrierId);
        model.addAttribute("status", status);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("shippingMethods", shippingMethods);

        return "admin/orders/shippings";
    }

    @PostMapping("/save")
    public String saveShipping(@Valid @ModelAttribute ShippingRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterCarrierId", required = false) Integer carrierId,
                               @RequestParam(name = "filterStatus", required = false) String status,
                               @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("shippingRequest", request);
        }
        else {
            try {
                shippingService.save(request);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin vận chuyển thành công!");
            }
            catch (DataIntegrityViolationException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("shippingRequest", request);
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi lưu vận chuyển. Vui lòng thử lại.");
                redirectAttributes.addFlashAttribute("shippingRequest", request);
            }
        }

        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterCarrierId", carrierId);
        redirectAttributes.addAttribute("filterStatus", status);
        redirectAttributes.addAttribute("filterShippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/shipping";
    }

    @PostMapping("/saveFromOrder")
    public String saveFromOrder(@ModelAttribute ShippingRequest request,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(name = "filterOrderStatus", required = false) String status,
                                @RequestParam(name = "filterPaymentMethod", required = false) String paymentMethod,
                                @RequestParam(name = "filterPaymentStatus", required = false) String paymentStatus,
                                @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            shippingService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đơn vận chuyển cho đơn hàng #" + request.getOrderId());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo đơn vận chuyển lúc này. Vui lòng thử lại.");
        }

        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterStatus", status);
        redirectAttributes.addAttribute("filterPaymentMethod", paymentMethod);
        redirectAttributes.addAttribute("filterPaymentStatus", paymentStatus);
        redirectAttributes.addAttribute("filterShippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/order";
    }

    @GetMapping("/delete/{id}")
    public String deleteShipping(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(name = "filterKeyword", required = false) String keyword,
                                 @RequestParam(name = "filterCarrierId", required = false) Integer carrierId,
                                 @RequestParam(name = "filterStatus", required = false) String status,
                                 @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        try {
            shippingService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đơn vận chuyển thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterCarrierId", carrierId);
        redirectAttributes.addAttribute("filterStatus", status);
        redirectAttributes.addAttribute("filterShippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/shipping";
    }
}
