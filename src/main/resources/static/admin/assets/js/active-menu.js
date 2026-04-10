// /static/js/active-menu.js
(function () {
  const SIDEBAR_SCROLL_KEY = "oneshop.admin.sidebar.scrollTop";

  function norm(p) {
    return (p || "").replace(/\/+$/, "");
  }

  function getMenuInner() {
    return document.querySelector("#layout-menu .menu-inner");
  }

  function saveSidebarScroll() {
    const menuInner = getMenuInner();
    if (!menuInner) return;
    sessionStorage.setItem(SIDEBAR_SCROLL_KEY, String(menuInner.scrollTop || 0));
  }

  function restoreSidebarScroll() {
    const menuInner = getMenuInner();
    if (!menuInner) return;
    const stored = sessionStorage.getItem(SIDEBAR_SCROLL_KEY);
    if (!stored) return;
    const scrollTop = parseInt(stored, 10);
    if (Number.isNaN(scrollTop)) return;

    // Restore after layout/menu scripts finish sizing the sidebar.
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => {
        menuInner.scrollTop = scrollTop;
      });
    });
  }

  function bindSidebarPersistence() {
    const menuInner = getMenuInner();
    if (!menuInner) return;

    menuInner.addEventListener("scroll", saveSidebarScroll, { passive: true });
    menuInner.addEventListener("ps-scroll-y", saveSidebarScroll);

    document.querySelectorAll(".menu-item > .menu-link").forEach((a) => {
      a.addEventListener("click", saveSidebarScroll);
    });

    window.addEventListener("beforeunload", saveSidebarScroll);
  }

  function applyActive() {
    const current = norm(location.pathname);

    // Chọn tất cả link trong menu dạng: <li class="menu-item"><a class="menu-link" href="...">
    document.querySelectorAll(".menu-item > .menu-link").forEach((a) => {
      try {
        const href = a.getAttribute("href");
        if (!href || href.startsWith("javascript:")) return;

        // Lấy path tuyệt đối (hoạt động cả khi href là relative)
        const hrefPath = norm(new URL(href, location.origin).pathname);

        // Điều kiện active: đúng trang hoặc là trang con
        const isActive = current === hrefPath || current.startsWith(hrefPath + "/");

        const li = a.closest(".menu-item");
        if (!li) return;

        // Nhiều theme cần active trên <li>, một số cần trên <a>
        li.classList.toggle("active", isActive);
        a.classList.toggle("active", isActive);

        // Nếu menu nhiều cấp, mở cha khi con active (đổi selector cho hợp theme của bạn)
        if (isActive) {
          const parent = li.closest(".menu-item.has-sub, .menu-item.dropdown, .menu-item.open");
          if (parent) parent.classList.add("open");
        }
      } catch (_) {
        // Bỏ qua link không hợp lệ
      }
    });
  }

  // Chạy ngay nếu DOM đã sẵn; nếu không, đợi DOMContentLoaded
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => {
      applyActive();
      bindSidebarPersistence();
      restoreSidebarScroll();
    });
  } else {
    applyActive();
    bindSidebarPersistence();
    restoreSidebarScroll();
  }
})();
