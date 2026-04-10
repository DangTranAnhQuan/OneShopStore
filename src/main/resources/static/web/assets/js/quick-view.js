(function ($) {
    'use strict';

    function hideQuickViewModal() {
        var modalEl = document.getElementById('quick-view-modal');
        if (!modalEl) return;

        if (window.bootstrap && window.bootstrap.Modal) {
            var instance = window.bootstrap.Modal.getInstance(modalEl) || new window.bootstrap.Modal(modalEl);
            instance.hide();
            return;
        }

        if (typeof $('#quick-view-modal').modal === 'function') {
            $('#quick-view-modal').modal('hide');
        }
    }

    // Hàm kiểm tra đăng nhập
    function isLoggedIn() {
        const meta = document.querySelector('meta[name="_is_logged_in"]');
        return meta ? meta.content === 'true' : false;
    }

    // Xử lý khi nhấn nút Quick View (AJAX để lấy thông tin sản phẩm)
    $('body').on('click', '.quick-view-btn', function (e) {
        e.preventDefault();
        var productId = $(this).data('product-id');
        var quickViewModal = $('#quick-view-modal');

        // Hiển thị spinner trong khi tải
        quickViewModal.find('.modal-body').html('<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>');
        // Mở modal (nếu chưa mở)
        // quickViewModal.modal('show'); // Bootstrap 5 might handle this via data-bs-toggle/target

        $.ajax({
            url: '/api/quick-view/' + productId, // API endpoint để lấy DTO chi tiết cho quick view
            type: 'GET',
            success: function (product) { // Expecting a DTO (e.g., QuickViewDTO) here
                var formContent = '';
                // Nút yêu thích
                var wishlistBtn = `
                    <a href="javascript:void(0)" class="qv-wishlist-btn toggle-wishlist-btn" data-product-id="${product.id}" title="Thêm vào danh sách yêu thích">
                        <i class="far fa-heart"></i>
                    </a>
                `;

                // Tạo form thêm vào giỏ nếu còn hàng
                if ((product.stockQuantity || 0) > 0) {
                    formContent = `
                        <form id="qv-add-cart-form" class="d-flex align-items-center gap-3"> <input type="hidden" name="productId" value="${product.id}">
                            <div class="product-variation">
                                <div class="cart-quantity">
                                    <div class="pro-qty">
                                        <span class="dec qtybtn">-</span>
                                        <input type="number" name="quantity" class="cart-plus-minus-box" value="1" min="1" max="${product.stockQuantity}">
                                        <span class="inc qtybtn">+</span>
                                    </div>
                                </div>
                            </div>
                            <div class="product-add-to-cart d-flex align-items-center gap-3">
                                <button type="submit" class="axil-btn btn-bg-primary">Thêm vào giỏ</button>
                                ${wishlistBtn}
                            </div>
                        </form>
                    `;
                } else { // Hiển thị nút hết hàng
                    formContent = `
                         <div class="product-add-to-cart d-flex align-items-center gap-3">
                            <button type="button" class="axil-btn btn-bg-secondary" disabled>Hết hàng</button>
                            ${wishlistBtn}
                        </div>
                    `;
                }

                // Tạo nội dung HTML cho modal body
                var modalBody = `
                    <div class="row">
                        <div class="col-lg-6">
                             <div class="pro-large-img mb-3 mb-lg-0"> <img src="${(product.images && product.images.length > 0) ? product.images[0] : '/path/to/placeholder.png'}" alt="${product.name}" class="img-fluid" />
                            </div>
                        </div>
                        <div class="col-lg-6">
                            <div class="product-details-content">
                                <h2 class="product-details-title">${product.name || 'Sản phẩm'}</h2>
                                <div class="product-details-rating mb-2">
                                    <div class="rating-list d-inline-block">${renderStars(product.rating)}</div>
                                    <span class="review-count">(${product.reviewCount || 0} lượt đánh giá)</span>
                                </div>
                                <div class="product-details-price-wrapper mb-3">
                                    <span class="price current-price me-2">${formatCurrency(product.price)}</span>
                                    ${(product.oldPrice && product.oldPrice > product.price) ? `<span class="price old-price text-decoration-line-through">${formatCurrency(product.oldPrice)}</span>` : ''}
                                </div>
                                <div class="product-details-meta mb-3"><ul>
                                    <li><span class="meta-title">THƯƠNG HIỆU:</span> ${product.brandName || 'N/A'}</li>
                                    <li><span class="meta-title">Tình trạng:</span> <span class="${product.inStock ? 'text-success' : 'text-danger'}">${product.inStock ? 'Còn hàng' : 'Hết hàng'}</span></li>
                                </ul></div>
                                <p class="product-details-short-desc mb-4">${product.shortDesc || ''}</p>
                                ${formContent} <div class="product-details-action-wrapper mt-4">
                                    <a href="/product/${product.id}" class="btn-link">Xem chi tiết sản phẩm <i class="far fa-long-arrow-right ms-1"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                `;

                quickViewModal.find('.modal-body').html(modalBody); // Điền nội dung vào modal

                // Kích hoạt lại logic +/- cho số lượng (nếu cần, có thể main.js đã làm)
                // This might cause double event binding if main.js also initializes .pro-qty on modal show.
                // Test carefully. If +/- increments by 2, remove this block.
                 if (quickViewModal.find('.pro-qty').length) {
                     quickViewModal.find('.qtybtn').off('click').on('click', function () { // Use .off('click') to prevent double binding
                         var $button = $(this);
                         var input = $button.parent().find('input');
                         var oldValue = parseFloat(input.val()) || 0; // Ensure it's a number
                         var max = parseFloat(input.attr('max'));
                         var min = parseFloat(input.attr('min') || 1);
                         var newVal;
                         if ($button.hasClass('inc')) {
                             newVal = oldValue + 1;
                             if (!isNaN(max) && newVal > max) newVal = max; // Check max if exists
                         } else { // dec
                             newVal = oldValue - 1;
                             if (newVal < min) newVal = min; // Check min
                         }
                         input.val(newVal);
                     });
                     quickViewModal.find('.pro-qty input').off('input').on('input', function() {
                          var $input = $(this);
                          var currentVal = parseInt($input.val());
                          var max = parseInt($input.attr('max'));
                          var min = parseInt($input.attr('min') || 1);
                          if (isNaN(currentVal) || currentVal < min) { $input.val(min); }
                          else if (!isNaN(max) && currentVal > max) { $input.val(max); }
                     });
                 }

            },
            error: function (error) {
                // Hiển thị lỗi trong modal
                quickViewModal.find('.modal-body').html('<div class="alert alert-danger text-center p-4">Lỗi khi tải thông tin sản phẩm. Vui lòng thử lại.</div>');
                console.error("Quick View AJAX Error:", error.statusText, error.responseText);
            }
        });
    });

    // Xử lý submit form Thêm vào giỏ trong Quick View (AJAX)
    $('body').on('submit', '#qv-add-cart-form', function(e) {
        e.preventDefault(); // Ngăn submit form mặc định
        if (!isLoggedIn()) { // Kiểm tra đăng nhập
            window.location.href = '/sign-in'; // Chuyển hướng nếu chưa đăng nhập
            return; // Dừng lại
        }
        // Nếu đã đăng nhập, tiến hành AJAX
        var $form = $(this);
        var $button = $form.find('button[type="submit"]');
        var productId = $form.find('input[name="productId"]').val();
        var quantity = $form.find('input[name="quantity"]').val();
        var originalHtml = $button.html(); // Lưu nội dung nút gốc

        $button.html('<i class="far fa-spinner fa-spin"></i> Đang thêm...').prop('disabled', true); // Hiển thị loading

        $.ajax({
            url: '/cart/add', // Endpoint thêm vào giỏ
            type: 'POST',
            data: { // Dữ liệu gửi đi
                productId: productId,
                quantity: quantity
            },
            headers: { 'X-Requested-With': 'XMLHttpRequest' }, // Báo là AJAX
            success: function(response) {
                hideQuickViewModal(); // Đóng modal khi thành công
                if (response && response.success && typeof updateGlobalCart === 'function') {
                    // Cập nhật mini cart với dữ liệu DTO trả về
                    updateGlobalCart(response.cartCount, response.cartItems);
                    // Có thể hiển thị thông báo thành công ngắn gọn (toast) ở đây
                } else if (response && !response.success) {
                     alert('Lỗi: ' + response.message); // Hiển thị lỗi từ server nếu success=false
                }
            },
            error: function(xhr) {
                // Hiển thị lỗi nếu AJAX thất bại
                var errorMsg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : "Đã xảy ra lỗi. Vui lòng thử lại.";
                alert('Lỗi: ' + errorMsg);
                console.error("Add to cart (quick view) error:", xhr.responseText);
            },
            complete: function() {
                // Luôn khôi phục lại nút sau khi AJAX hoàn tất
                $button.html(originalHtml).prop('disabled', false);
            }
        });
    });

    // Xử lý nút Wishlist trong Quick View (giữ nguyên logic toggle class/icon)
    $('body').on('click', '#quick-view-modal .toggle-wishlist-btn', function(e) {
        e.preventDefault(); // Ngăn hành động mặc định
        if (!isLoggedIn()) { // Kiểm tra đăng nhập
            alert('Vui lòng đăng nhập để sử dụng chức năng này.');
            window.location.href = '/sign-in';
            return; // Dừng lại
        }
        // Nếu đã đăng nhập, thực hiện AJAX (logic AJAX đầy đủ nằm ở custom-functions.js)
        // Chỉ xử lý UI ở đây nếu cần phản hồi tức thì, nhưng nên đồng bộ với custom-functions.js
        var $this = $(this);
        var icon = $this.find('i');
        // $this.toggleClass('active wishlist-added'); // Cập nhật class
        // if ($this.hasClass('active')) {
        //     icon.removeClass('far').addClass('fas');
        // } else {
        //     icon.removeClass('fas').addClass('far');
        // }
        // Gọi hàm xử lý AJAX wishlist từ custom-functions.js nếu có
         if (typeof $(this).trigger === 'function') {
            // Re-trigger the click event for the handler in custom-functions.js to handle AJAX
            // This might cause issues if not handled carefully.
            // A better approach is to have a single handler in custom-functions.js
            // that also updates the UI inside the modal.
            // For now, let's assume the custom-functions handler handles everything.
         }
    });

    // Hàm định dạng tiền tệ (giữ nguyên)
    function formatCurrency(number) {
        if (isNaN(number)) return "0 ₫";
        const roundedAmount = Math.round(number);
        return roundedAmount.toLocaleString('vi-VN') + ' ₫';
    }

    // Hàm render sao (giữ nguyên)
    function renderStars(rating) {
        if (rating == null || rating <= 0) return Array(5).fill('<span><i class="far fa-star text-secondary"></i></span>').join(''); // Use secondary color for empty
        let stars = '', fullStars = Math.floor(rating), halfStar = rating % 1 >= 0.5;
        let emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
        for (let i = 0; i < fullStars; i++) stars += '<span><i class="fas fa-star text-warning"></i></span>';
        if (halfStar) stars += '<span><i class="fas fa-star-half-alt text-warning"></i></span>';
        for (let i = 0; i < emptyStars; i++) stars += '<span><i class="far fa-star text-warning"></i></span>'; // Use warning for empty if rating > 0
        return stars;
    }
})(jQuery);