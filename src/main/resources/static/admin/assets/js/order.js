// File: order.js

document.addEventListener('DOMContentLoaded', function() {
    
    // --- LOGIC CHO MODAL SỬA ĐƠN VẬN CHUYỂN ---
    const editModalShipping = document.getElementById('editModal');
    if (editModalShipping) {
        editModalShipping.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const modal = this;

            // Lấy dữ liệu từ các thuộc tính data-* của nút
            const shippingId = button.dataset.id;
            const orderId = button.dataset.orderId;
            const trackingCode = button.dataset.trackingCode;
            const carrierId = button.dataset.carrierId;
            const carrierName = button.dataset.carrierName;
            const status = button.dataset.status;
            const method = button.dataset.method;

            // Tìm các trường input/select trong modal
            const shippingIdInput = modal.querySelector('#editShippingId');
            const orderIdInput = modal.querySelector('#editOrderId');
            const trackingCodeText = modal.querySelector('#editTrackingCodeText');
            const carrierIdInput = modal.querySelector('#editCarrierId');
            const carrierNameInput = modal.querySelector('#editCarrierName');
            const statusSelect = modal.querySelector('#editStatus');
            const shippingMethodInput = modal.querySelector('#editShippingMethod');

            // Gán dữ liệu vào các trường trong form
            if (shippingIdInput) shippingIdInput.value = shippingId;
            if (orderIdInput) orderIdInput.value = orderId;
            if (trackingCodeText) trackingCodeText.textContent = trackingCode;
            if (carrierIdInput) carrierIdInput.value = carrierId;
            if (carrierNameInput) carrierNameInput.value = carrierName;
            if (statusSelect) statusSelect.value = status;
            if (shippingMethodInput) shippingMethodInput.value = method;
        });
    }

    // --- LOGIC CHO NÚT TẠO MÃ KHUYẾN MÃI ---
    const generateBtn = document.getElementById('generate-voucher-code-btn');
    const codeInput = document.querySelector('input[name="voucherCode"]');

    if (generateBtn && codeInput && !codeInput.readOnly) {
        generateBtn.addEventListener('click', function() {
            const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
            let result = '';
            for (let i = 0; i < 10; i++) {
                result += chars.charAt(Math.floor(Math.random() * chars.length));
            }
            codeInput.value = result;
        });
    }

    const createShippingModal = document.getElementById('createShippingModal');
    if (createShippingModal) {
        createShippingModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const modal = this;

            const orderId = button.dataset.orderId;
            const shippingMethod = button.dataset.shippingMethod;

            modal.querySelector('#createShipping_orderId').value = orderId;
            modal.querySelector('#orderIdInModal').textContent = orderId;
            modal.querySelector('#createShippingMethodDisplay').value = shippingMethod;

            const allGroups = modal.querySelectorAll('#nhaVanChuyenSelect optgroup');
            allGroups.forEach(g => g.style.display = 'none');

            const currentGroup = modal.querySelector(`#nhaVanChuyenSelect optgroup[label='Đơn hàng #${orderId}']`);
            if (currentGroup) {
                currentGroup.style.display = '';
            }

            modal.querySelector('#nhaVanChuyenSelect').value = '';
        });
    }
});