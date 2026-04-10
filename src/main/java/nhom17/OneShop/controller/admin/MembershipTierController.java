package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.request.MembershipTierRequest;
import nhom17.OneShop.service.MembershipTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/membership-tier")
public class MembershipTierController {

    @Autowired
    private MembershipTierService membershipTierService;

    @GetMapping
    public String listTiers(Model model) {
        List<MembershipTier> tiers = membershipTierService.findAllSorted();
        model.addAttribute("tiers", tiers);
        if (!model.containsAttribute("tierRequest")) {
            model.addAttribute("tierRequest", new MembershipTierRequest());
        }
        return "admin/system/membership-tiers";
    }

    @PostMapping("/save")
    public String saveTier(@Valid @ModelAttribute("tierRequest") MembershipTierRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("tierRequest", request);
        }
        else {
            try {
                membershipTierService.save(request);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu hạng thành viên thành công!");

            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("tierRequest", request);
            }
        }
        return "redirect:/admin/membership-tier";
    }

    @GetMapping("/delete/{id}")
    public String deleteTier(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            membershipTierService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hạng thành viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/membership-tier";
    }
}
