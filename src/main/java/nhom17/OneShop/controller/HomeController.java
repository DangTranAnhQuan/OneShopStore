package nhom17.OneShop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.dto.ProductApiDTO;
import nhom17.OneShop.entity.*;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.repository.BrandRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.repository.RatingRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private BrandRepository brandRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private ContactRepository contactRepository;
    @Autowired private EmailService emailService;
    @Autowired private VoucherService voucherService;
    @Autowired private CookieUtil cookieUtil;

    private Collection<List<Product>> groupProducts(List<Product> products, int chunkSize) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        final AtomicInteger counter = new AtomicInteger();
        return products.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        List<Product> sliderProducts = productService.findNewestProducts(4);
        model.addAttribute("sliderProducts", sliderProducts);

        int productLimit = 16;
        int chunkSize = 8;

        List<Product> allProducts = productService.searchUserProducts(null, null, null, "newest", null, 1, productLimit).getContent();
        model.addAttribute("groupedProducts", groupProducts(allProducts, chunkSize));

        model.addAttribute("newestProductsGrouped", groupProducts(productService.findNewestProducts(productLimit), chunkSize));
        model.addAttribute("topSellingProductsGrouped", groupProducts(productService.findTopSellingProducts(productLimit), chunkSize));
        model.addAttribute("mostDiscountedProductsGrouped", groupProducts(productService.findMostDiscountedProducts(productLimit), chunkSize));

        return "user/index";
    }

    @GetMapping("/shop")
    public String shopPage(Model model,
                           @RequestParam(name = "keyword", required = false) String keyword,
                           @RequestParam(name = "categoryId", required = false) Integer categoryId,
                           @RequestParam(name = "minPrice", required = false) String minPriceStr,
                           @RequestParam(name = "maxPrice", required = false) String maxPriceStr,
                           @RequestParam(name = "brandIds", required = false) List<Integer> brandIds,
                           @RequestParam(name = "sort", required = false) String sort,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "size", defaultValue = "9") int size) {
        Page<Product> productPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProductsForUser(keyword, page, size);
            model.addAttribute("searchResult", "Kết quả tìm kiếm cho: '" + keyword + "'");
        } else {
            BigDecimal minPrice = null;
            if (minPriceStr != null && !minPriceStr.isEmpty()) {
                try { minPrice = new BigDecimal(minPriceStr.replaceAll("[.,₫\\s]", "")); } catch (NumberFormatException e) { }
            }
            BigDecimal maxPrice = null;
            if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
                try { maxPrice = new BigDecimal(maxPriceStr.replaceAll("[.,₫\\s]", "")); } catch (NumberFormatException e) { }
            }
            productPage = productService.searchUserProducts(categoryId, minPrice, maxPrice, sort, brandIds, page, size);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
        }
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageNumber", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandIds", brandIds);
        model.addAttribute("keyword", keyword);
        return "user/shop/shop-sidebar";
    }

    @GetMapping("/product/{id}")
    public String productDetailPage(@PathVariable("id") int productId, Model model, HttpServletRequest request, HttpServletResponse response, Principal principal) {
        Product product = productService.findById(productId);
        if (product == null) {
            return "redirect:/shop";
        }
        model.addAttribute("product", product);

        // --- Code xử lý reviews (giữ nguyên) ---
        List<Rating> allReviews = ratingRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId);
        boolean canReview = false;
        Rating currentUserReview = null;
        if (principal != null) {
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                Integer currentUserId = currentUser.getUserId();
                Optional<Rating> reviewOpt = ratingRepository.findByUser_UserIdAndProduct_ProductId(currentUserId, productId);
                if (reviewOpt.isPresent()) {
                    currentUserReview = reviewOpt.get();
                    allReviews.remove(currentUserReview);
                } else {
                    canReview = orderService.hasCompletedPurchase(currentUserId, productId);
                }
            }
        }
        model.addAttribute("reviews", allReviews);
        model.addAttribute("currentUserReview", currentUserReview);
        model.addAttribute("canReview", canReview);
        List<Rating> allReviewsForCalculation = ratingRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId); // Lấy lại nếu đã remove
        double averageRating = allReviewsForCalculation.stream().mapToInt(Rating::getRatingScore).average().orElse(0.0);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalReviews", allReviewsForCalculation.size());
        // --- Kết thúc code reviews ---

        // --- Code xử lý recently viewed (giữ nguyên) ---
        String viewedIdsString = cookieUtil.readCookie(request, "viewedProductIds");
        List<Integer> viewedProductIds = new LinkedList<>();

        if (viewedIdsString != null && !viewedIdsString.isEmpty()) {
            try {
                // Chuyển string "5,2,10" thành List<Integer>
                viewedProductIds = Arrays.stream(viewedIdsString.split("_"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(LinkedList::new));
            } catch (Exception e) {
                viewedProductIds = new LinkedList<>(); // Reset nếu cookie bị lỗi
            }
        }

        // Logic xử lý list (giống hệt code cũ của bạn)
        viewedProductIds.remove(Integer.valueOf(productId));
        viewedProductIds.add(0, productId);
        if (viewedProductIds.size() > 10) viewedProductIds = viewedProductIds.subList(0, 10);

        // Chuyển List<Integer> thành string "7,5,2,10"
        String newViewedIdsString = viewedProductIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("_"));

        // Ghi lại cookie (lưu 30 ngày)
        int expiryInDays = 30 * 24 * 60 * 60;
        cookieUtil.createCookie(response, "viewedProductIds", newViewedIdsString, expiryInDays);

        // Logic lấy sản phẩm từ DB (giống hệt code cũ của bạn)
        if (viewedProductIds.size() > 1) {
            List<Integer> idsToFetch = new ArrayList<>(viewedProductIds);
            idsToFetch.remove(Integer.valueOf(productId));
            if (!idsToFetch.isEmpty()) {
                model.addAttribute("recentlyViewedProducts", productRepository.findAllById(idsToFetch));
            } else {
                model.addAttribute("recentlyViewedProducts", Collections.emptyList());
            }
        } else {
            model.addAttribute("recentlyViewedProducts", Collections.emptyList());
        }
        // --- Kết thúc code recently viewed ---

        // === BƯỚC 3: LẤY VÀ THÊM KHUYẾN MÃI VÀO MODEL ===
        List<Voucher> activePromotions = voucherService.findActivePromotions();
        model.addAttribute("promotions", activePromotions);
        // === KẾT THÚC THÊM KHUYẾN MÃI ===

        return "user/shop/single-product"; // Đảm bảo trả về đúng view
    }
    @GetMapping("/contact")
    public String contactPage(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                model.addAttribute("currentUser", userOpt.get());
            }
        }
        return "user/general/contact";
    }

    @PostMapping("/contact")
    public String handleContactForm(
            @RequestParam("contact-subject") String subject,
            @RequestParam("contact-message") String message,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Yêu cầu: Chỉ người dùng đã đăng nhập mới được gửi
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để gửi liên hệ.");
            return "redirect:/sign-in";
        }

    Optional<User> userOpt = userRepository.findByEmail(principal.getName());
    if (userOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy thông tin người dùng.");
        return "redirect:/sign-in";
    }

    User currentUser = userOpt.get();

    Contact contact = new Contact(currentUser, subject, message);

    try {
        contactRepository.save(contact);
        emailService.sendContactEmail(currentUser, subject, message);
        redirectAttributes.addFlashAttribute("successMessage", "Tin nhắn của bạn đã được gửi. Chúng tôi sẽ sớm phản hồi!");

    } catch (Exception e) {
        System.err.println("Lỗi khi xử lý form liên hệ: " + e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi gửi tin nhắn. Vui lòng thử lại.");
    }

    return "redirect:/contact";
}

    @GetMapping("/about-us")
    public String aboutUsPage() { return "user/general/about-us"; }

    @GetMapping("/privacy-policy")
    public String privacyPolicyPage() { return "user/general/privacy-policy"; }

    @GetMapping("/api/product/{id}")
    @ResponseBody
    public ResponseEntity<ProductApiDTO> getProductForQuickView(@PathVariable("id") int productId) {
        Product product = productService.findById(productId);
        ProductApiDTO dto = ProductApiDTO.fromEntity(product);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<ProductApiDTO>> liveSearchProducts(@RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Page<Product> productPage = productService.searchProductsForUser(keyword, 1, 5);
        List<ProductApiDTO> result = productPage.getContent().stream()
                .map(ProductApiDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}