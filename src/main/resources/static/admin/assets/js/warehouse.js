document.addEventListener('DOMContentLoaded', function () {

    const suppliersTable = document.getElementById('suppliersTable');
    if (suppliersTable) {
        const editModal = document.getElementById('editSupplierModal');
        editModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const phone = button.getAttribute('data-phone');
            const address = button.getAttribute('data-address');

            const modalForm = editModal.querySelector('form');
            modalForm.querySelector('[name="supplierId"]').value = id;
            modalForm.querySelector('[name="supplierName"]').value = name;
            modalForm.querySelector('[name="phoneNumber"]').value = phone;
            modalForm.querySelector('[name="address"]').value = address;
        });
    }

    function formatNumber(numStr) {
        if (!numStr) return '';
        const num = parseInt(numStr.toString().replace(/[^0-9]/g, ''), 10);
        if (isNaN(num)) return '';
        return new Intl.NumberFormat('vi-VN').format(num);
    }

    const importForm = document.getElementById('import-form');
    if (importForm) {
        const container = document.getElementById('details-container');
        const template = document.getElementById('detail-row-template');
        const addButton = document.getElementById('add-detail-row');
        let rowIndex = 0;

        // Hàm để thêm một dòng mới vào bảng
        function addRow(detail) {
            const newRowFragment = template.content.cloneNode(true);
            const tr = newRowFragment.querySelector('tr');

            // Lấy các thẻ input/select trong dòng mới
            const productSelect = tr.querySelector('.product-select');
            const quantityInput = tr.querySelector('.quantity-input');
            const priceInput = tr.querySelector('.price-input');
            const priceDisplay = tr.querySelector('.current-price-display');

            // THÊM MỚI: Lấy thẻ <a> "Xem lịch sử"
            const historyLink = tr.querySelector('.history-link');

            // Cập nhật thuộc tính 'name'
            productSelect.name = `importDetails[${rowIndex}].productId`;
            quantityInput.name = `importDetails[${rowIndex}].quantity`;
            priceInput.name = `importDetails[${rowIndex}].importPrice`;

            // Nếu có dữ liệu ban đầu (chế độ Sửa)
            if(detail) {
                productSelect.value = detail.productId;
                quantityInput.value = detail.quantity;
                priceInput.value = detail.importPrice;

                // Xử lý giá bán
                const selectedOption = productSelect.querySelector(`option[value="${detail.productId}"]`);
                if (priceDisplay && selectedOption) {
                    const num = parseFloat(selectedOption.dataset.price);
                    priceDisplay.value = formatNumber(num);
                }

                // THÊM MỚI: Xử lý nút "Xem lịch sử" khi tải trang
                if (historyLink && detail.productId) {
                    // Lấy link gốc từ 'data-base-href'
                    const baseUrl = historyLink.dataset.baseHref;
                    historyLink.href = baseUrl + detail.productId;
                    historyLink.style.display = 'inline-block'; // Hiển thị nút
                }
            }

            container.appendChild(newRowFragment);
            rowIndex++;
        }

        // Code xử lý tải trang (chế độ Sửa hoặc Thêm mới)
        if (typeof initialDetails !== 'undefined' && initialDetails && initialDetails.length > 0) {
            initialDetails.forEach(detail => addRow(detail));
        } else {
            addRow(null);
        }

        // Nút "Thêm sản phẩm"
        addButton.addEventListener('click', () => addRow(null));

        function reindexRows() {
                    const rows = container.querySelectorAll('tr');
                    rows.forEach((tr, newIndex) => {
                        // Tìm tất cả input/select trong dòng này
                        const inputs = tr.querySelectorAll('input, select');
                        inputs.forEach(input => {
                            const name = input.name;
                            if (name) {
                                // Regex này thay thế chiTietPhieuNhapList[NUMBER] bằng chiTietPhieuNhapList[newIndex]
                                input.name = name.replace(/^(importDetails)\[\d+\]/, `$1[${newIndex}]`);
                            }
                        });
                    });
                    // Cập nhật lại chỉ số chung để sẵn sàng cho lần "Thêm sản phẩm" tiếp theo
                    rowIndex = rows.length;
                }
       // Nút "Xóa"
       container.addEventListener('click', function (event) {
           // Tìm nút xóa gần nhất
           const removeButton = event.target.closest('.remove-detail-row');
           if (removeButton) {
               // Xóa thẻ <tr> cha
               removeButton.closest('tr').remove();
               // (THÊM MỚI) Gọi hàm đánh số lại sau khi xóa
               reindexRows();
           }
       });

        container.addEventListener('change', function(event) {
        if (event.target && event.target.classList.contains('product-select')) {
            const selectedOption = event.target.options[event.target.selectedIndex];
            const price = selectedOption.dataset.price;
            const row = event.target.closest('tr');
            const priceDisplay = row.querySelector('.current-price-display');

            // THÊM MỚI: Lấy nút "Xem" và ID sản phẩm
            const historyLink = row.querySelector('.history-link');
            const productId = selectedOption.value; // Lấy ID

            // Cập nhật giá bán
            if (priceDisplay) {
            const num = parseFloat(price);
                priceDisplay.value = formatNumber(num);
            }

            // THÊM MỚI: Logic hiển thị / ẩn nút "Xem"
            if (historyLink) {
                if (productId) {
                    // 1. Nếu có ID (đã chọn 1 sản phẩm)
                    const baseUrl = historyLink.dataset.baseHref;
                    historyLink.href = baseUrl + productId;
                    historyLink.style.display = 'inline-block'; // HIỆN
                } else {
                    // 2. Nếu không có ID (chọn "-- Chọn sản phẩm --")
                    historyLink.href = '#';
                    historyLink.style.display = 'none'; // ẨN
                }
            }
        }
    });
}
});