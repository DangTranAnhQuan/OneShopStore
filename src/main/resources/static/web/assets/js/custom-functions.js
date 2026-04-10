/**
 * Cập nhật icon giỏ hàng và dropdown mini-cart
 * @param {number} cartCount - Tổng số lượng item (loại sản phẩm)
 * @param {Array<CartItemDTO>} cartItemsDTO - Mảng các đối tượng DTO item giỏ hàng
 */
function updateGlobalCart(cartCount, cartItemsDTO) {
    // 1. Cập nhật số lượng trên icon
    $('.cart-count').text(cartCount);

    // 2. Cập nhật danh sách trong dropdown
    var $cartList = $('#cart-dropdown .cart-item-list');
    $cartList.empty(); // Xóa các item cũ

    if (cartItemsDTO && cartItemsDTO.length > 0) {
        $.each(cartItemsDTO, function(index, item) {
            // ===== START: FIX - Use DTO properties =====
            var formattedPrice = (item.unitPrice || 0).toLocaleString('vi-VN') + ' ₫'; // Use unitPrice
            var productUrl = '/product/' + item.productId; // Use productId
            var imageUrl = '/uploads/' + (item.productImage || 'placeholder.png'); // Use productImage
            var productName = item.productName || 'Sản phẩm không tên'; // Use productName
            var quantity = item.quantity || 0; // Use quantity
            // ===== END: FIX - Use DTO properties =====

            var itemHtml = `
                <li class="cart-item">
                    <div class="item-img">
                        <a href="${productUrl}">
                            <img src="${imageUrl}" alt="Product">
                        </a>
                    </div>
                    <div class="item-content">
                        <h3 class="item-title">
                            <a href="${productUrl}">${productName}</a>
                        </h3>
                        <div class="item-price">${quantity} x ${formattedPrice}</div>
                    </div>
                </li>
            `;
            $cartList.append(itemHtml);
        });
    } else {
        // Nếu giỏ hàng trống
        $cartList.append('<li class="cart-item" style="justify-content: center;"><p>Giỏ hàng của bạn đang trống.</p></li>');
    }
}

// Hàm kiểm tra đăng nhập
function isLoggedIn() {
    const meta = document.querySelector('meta[name="_is_logged_in"]');
    return meta ? meta.content === 'true' : false;
}

$(document).ready(function() {

    // --- XỬ LÝ QUICK VIEW (Placeholder logic, actual logic is in quick-view.js) ---
    // This click handler might conflict or be redundant if quick-view.js handles it fully.
    // Consider removing this if quick-view.js works correctly.
    /*
    $(document).on('click', '.quick-view-btn', function(e) {
        e.preventDefault();
        var productId = $(this).data('product-id');
        // AJAX call to /api/product/ - might be different from /api/quick-view/ used elsewhere
        $.ajax({
            url: '/api/product/' + productId, // Different API endpoint?
            type: 'GET',
            success: function(product) {
                // ... (Update modal content - potentially conflicts with quick-view.js)
            },
            error: function() {
                // Handle error
            }
        });
    });
    */

    // --- XỬ LÝ WISHLIST ---
    $(document).on('click', '.toggle-wishlist-btn', function(e) {
        e.preventDefault(); // Prevent default link behavior first
        if (!isLoggedIn()) {
             // alert('Vui lòng đăng nhập để sử dụng chức năng này.'); // Optionally show alert
             window.location.href = '/sign-in'; // Redirect
             return; // Stop further execution
         }
        // If logged in, proceed with AJAX
        var button = $(this);
        var productId = button.data('product-id');

        $.ajax({
            url: '/wishlist/toggle/' + productId,
            type: 'POST',
            // No need for X-Requested-With header for POST if controller doesn't check it
            success: function(response) {
                // Update button appearance based on response status
                if (response && response.status === 'added') {
                    button.addClass('wishlist-added').find('i').removeClass('far').addClass('fas'); // Add class and change icon
                     // You might want to update a general wishlist count somewhere in the header
                     // if (response.count !== undefined) $('.wishlist-count').text(response.count);
                } else if (response && response.status === 'removed') {
                    button.removeClass('wishlist-added').find('i').removeClass('fas').addClass('far'); // Remove class and change icon back
                     // Update count if available
                     // if (response.count !== undefined) $('.wishlist-count').text(response.count);
                }
                 // Update general wishlist count if provided
                 if (response && response.count !== undefined) {
                    $('.wishlist-count').text(response.count); // Assuming you have an element with class 'wishlist-count'
                 }
            },
            error: function(xhr) {
                // Handle errors (like 401 Unauthorized if session expired, or other server errors)
                if(xhr.status === 401) {
                    // Redirect to login if unauthorized
                    alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                    window.location.href = '/sign-in';
                } else {
                    // General error message
                    alert('Đã xảy ra lỗi khi cập nhật danh sách yêu thích. Vui lòng thử lại.');
                    console.error("Wishlist toggle error:", xhr.responseText);
                }
            }
        });
    });

    // --- XỬ LÝ NÚT ADD-TO-CART (Trang chủ / Trang shop) ---
    $(document).on('click', '.add-to-cart-btn-ajax', function(e) {
        e.preventDefault(); // Prevent default link behavior
        if (!isLoggedIn()) { // Check login status first
            window.location.href = '/sign-in'; // Redirect if not logged in
            return; // Stop execution
        }
        // If logged in, proceed
        var $button = $(this);
        var productId = $button.data('product-id');
        var originalHtml = $button.html(); // Save original button text/icon

        $button.html('Đang thêm...').prop('disabled', true); // Show loading state

        $.ajax({
            url: '/cart/add', // Endpoint to add to cart
            type: 'POST',
            data: { // Data to send
                productId: productId,
                quantity: 1 // Default quantity for these buttons
            },
            headers: { 'X-Requested-With': 'XMLHttpRequest' }, // Identify as AJAX request
            success: function(response) {
                // On success, update the global cart display
                if (response && response.success && typeof updateGlobalCart === 'function') {
                    updateGlobalCart(response.cartCount, response.cartItems); // Pass DTO list
                     // Optionally show a success toast/notification instead of alert
                     // showToast('Đã thêm vào giỏ hàng!');
                } else if (response && !response.success) {
                     alert('Lỗi: ' + response.message); // Show error message from server if success is false
                }
            },
            error: function(xhr) {
                // On error, show an alert
                var errorMsg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'Đã xảy ra lỗi khi thêm vào giỏ hàng.';
                alert('Lỗi: ' + errorMsg);
                console.error("Add to cart (ajax) error:", xhr.responseText);
            },
            complete: function() {
                // Always restore the button state
                $button.html(originalHtml).prop('disabled', false);
            }
        });
    });

    // --- XỬ LÝ FORM ADD-TO-CART (Trang chi tiết sản phẩm) ---
    $(document).on('submit', '#detail-add-cart-form', function(e) {
        e.preventDefault(); // Prevent default form submission
        if (!isLoggedIn()) { // Check login status first
            window.location.href = '/sign-in'; // Redirect if not logged in
            return; // Stop execution
        }
        // If logged in, proceed
        var $form = $(this);
        var $button = $form.find('button[type="submit"]');
        var $messageDiv = $('#detail-cart-message'); // Div to show messages
        var productId = $form.find('input[name="productId"]').val();
        var quantity = $form.find('input[name="quantity"]').val();
        var originalHtml = $button.html(); // Save original button content

        $messageDiv.empty(); // Clear previous messages
        $button.html('<i class="far fa-spinner fa-spin"></i> Đang thêm...').prop('disabled', true); // Show loading state with spinner

        $.ajax({
            url: '/cart/add', // Endpoint to add to cart
            type: 'POST',
            data: { // Data from the form
                productId: productId,
                quantity: quantity
            },
            headers: { 'X-Requested-With': 'XMLHttpRequest' }, // Identify as AJAX
            success: function(response) {
                // On success, update global cart and show success message
                if (response && response.success && typeof updateGlobalCart === 'function') {
                    updateGlobalCart(response.cartCount, response.cartItems); // Pass DTO list
                    $messageDiv.html('<div class="alert alert-success alert-dismissible fade show" role="alert">' + (response.message || 'Đã thêm vào giỏ hàng thành công!') + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>');
                    // Optional: Auto-hide message after a few seconds
                    setTimeout(function() { $messageDiv.empty(); }, 4000);
                } else if (response && !response.success) {
                     $messageDiv.html('<div class="alert alert-danger alert-dismissible fade show" role="alert">' + (response.message || 'Có lỗi xảy ra.') + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>');
                }
            },
            error: function(xhr) {
                // On error, show error message
                var errorMsg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'Đã xảy ra lỗi khi thêm vào giỏ hàng.';
                $messageDiv.html('<div class="alert alert-danger alert-dismissible fade show" role="alert">' + errorMsg + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>');
                console.error("Add to cart (detail) error:", xhr.responseText);
            },
            complete: function() {
                // Always restore the button
                $button.html(originalHtml).prop('disabled', false);
            }
        });
    });

}); // End document ready