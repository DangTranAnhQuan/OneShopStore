// File: assets/js/products.js

document.addEventListener('DOMContentLoaded', function () {
    console.log("Custom JS for Admin pages loaded.");

    // === XỬ LÝ CHO TRANG DANH MỤC (CATEGORY) ===
    const editCategoryModal = document.getElementById('editCategoryModal');
    if (editCategoryModal) {
        editCategoryModal.addEventListener('show.bs.modal', function (event) {
            // Nút đã kích hoạt modal
            const button = event.relatedTarget;
            // Lấy dữ liệu từ các thuộc tính data-* của nút
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const image = button.getAttribute('data-image');
            const status = button.getAttribute('data-status') === 'true';

            // Gán dữ liệu vào các trường input trong form
            const modalForm = editCategoryModal.querySelector('form');
            modalForm.querySelector('[name="categoryId"]').value = id;
            modalForm.querySelector('[name="categoryName"]').value = name;
            modalForm.querySelector('[name="imageUrl"]').value = image || '';
            modalForm.querySelector('[name="active"]').checked = status;

            // Xử lý hiển thị ảnh preview
            const imagePreview = modalForm.querySelector('#editImagePreview');
            const noImageText = modalForm.querySelector('#noImageText');
            if (image && image.trim() !== '') {
                imagePreview.src = '/uploads/' + image;
                imagePreview.style.display = 'block';
                noImageText.style.display = 'none';
            } else {
                imagePreview.style.display = 'none';
                noImageText.style.display = 'block';
            }
        });
    }

    // === XỬ LÝ CHO TRANG THƯƠNG HIỆU (BRAND) ===
    const editBrandModal = document.getElementById('editBrandModal');
    if (editBrandModal) {
        editBrandModal.addEventListener('show.bs.modal', function (event) {
            // Nút đã kích hoạt modal
            const button = event.relatedTarget;
            // Lấy dữ liệu từ các thuộc tính data-* của nút
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const description = button.getAttribute('data-description');
            const image = button.getAttribute('data-image');
            const status = button.getAttribute('data-status') === 'true';

            // Gán dữ liệu vào các trường input trong form
            const modalForm = editBrandModal.querySelector('form');
            modalForm.querySelector('[name="brandId"]').value = id;
            modalForm.querySelector('[name="brandName"]').value = name;
            modalForm.querySelector('textarea[name="description"]').value = description;
            modalForm.querySelector('[name="active"]').checked = status;
            modalForm.querySelector('input[name="imageUrl"]').value = image || '';

            // Xử lý hiển thị ảnh preview
            const imagePreview = modalForm.querySelector('#editImagePreview');
            const noImageText = modalForm.querySelector('#noImageText');
            if (image && image.trim() !== '') {
                imagePreview.src = '/uploads/' + image;
                imagePreview.style.display = 'block';
                noImageText.style.display = 'none';
            } else {
                imagePreview.style.display = 'none';
                noImageText.style.display = 'block';
            }
        });
    }
});