package nhom17.OneShop.controller.admin;

import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String listInventory(@RequestParam(name = "filterKeyword", required = false) String keyword,
                                @RequestParam(name = "filterSort", required = false) String sort,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model) {

        Page<Inventory> inventoryPage = inventoryService.findAll(keyword, sort, page, size);
        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "admin/warehouse/inventory";
    }
}
