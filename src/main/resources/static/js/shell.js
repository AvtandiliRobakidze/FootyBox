(() => {
  function normalizedPath(pathname) {
    if (pathname === "/index.html") {
      return "/";
    }
    if (pathname === "/archive.html") {
      return "/matches.html";
    }
    if (pathname === "/list.html") {
      return "/lists.html";
    }
    if (pathname === "/team.html" || pathname === "/player.html" || pathname === "/competition.html") {
      return "/matches.html";
    }
    return pathname;
  }

  function enhanceBrand() {
    document.querySelectorAll(".brand").forEach(brand => {
      const label = brand.querySelector("span:last-child");
      if (!label || label.querySelector(".brand-box")) return;
      label.innerHTML = "Footy<span class=\"brand-box\">Box</span>";
    });
  }

  function addGlobalSearch() {
    const nav = document.querySelector(".topbar .nav");
    const authSlot = document.querySelector("[data-auth-nav]");
    if (!nav || !authSlot || document.querySelector(".nav-search")) {
      return;
    }

    const form = document.createElement("form");
    form.className = "nav-search";
    form.setAttribute("role", "search");
    form.innerHTML = `
      <span aria-hidden="true">⌕</span>
      <label class="visually-hidden" for="global-archive-search">Search archive</label>
      <input id="global-archive-search" name="q" autocomplete="off" placeholder="Search teams, matches, players...">
      <kbd>/</kbd>
    `;
    form.addEventListener("submit", event => {
      event.preventDefault();
      const query = form.elements.q.value.trim();
      window.location.href = query ? `/matches.html?q=${encodeURIComponent(query)}` : "/matches.html";
    });
    nav.insertBefore(form, authSlot);

    document.addEventListener("keydown", event => {
      const active = document.activeElement;
      const isTyping = active && ["INPUT", "TEXTAREA", "SELECT"].includes(active.tagName);
      if (event.key === "/" && !isTyping) {
        event.preventDefault();
        form.elements.q.focus();
      }
    });
  }

  function addGlobalSpoilerToggle() {
    const nav = document.querySelector(".topbar .nav");
    const authSlot = document.querySelector("[data-auth-nav]");
    if (!nav || !authSlot || document.querySelector("[data-global-spoiler]")) {
      return;
    }

    const label = document.createElement("label");
    label.className = "global-spoiler";
    label.innerHTML = `
      <span>Spoiler-free</span>
      <input type="checkbox" data-global-spoiler>
    `;
    nav.insertBefore(label, authSlot);

    const input = label.querySelector("input");
    const apply = checked => {
      document.body.classList.toggle("spoiler-hide", checked);
      document.querySelectorAll("[data-platform-spoiler], [data-spoiler-free], [data-detail-spoiler-free]").forEach(toggle => {
        if (toggle !== input && toggle.checked !== checked) {
          toggle.checked = checked;
          toggle.dispatchEvent(new Event("change", { bubbles: true }));
        }
      });
    };
    input.checked = localStorage.getItem("footybox.spoilerFree") === "true";
    apply(input.checked);
    input.addEventListener("change", () => {
      localStorage.setItem("footybox.spoilerFree", String(input.checked));
      apply(input.checked);
    });
  }

  function addSkipLink() {
    const main = document.querySelector("main");
    if (!main || document.querySelector(".skip-link")) {
      return;
    }

    if (!main.id) {
      main.id = "main-content";
    }

    const link = document.createElement("a");
    link.className = "skip-link";
    link.href = `#${main.id}`;
    link.textContent = "Skip to main content";
    document.body.prepend(link);
  }

  function markCurrentNavigation() {
    const currentPath = normalizedPath(window.location.pathname);
    document.querySelectorAll(".nav a[href]").forEach(link => {
      const linkPath = normalizedPath(new URL(link.href, window.location.origin).pathname);
      if (linkPath === currentPath) {
        link.setAttribute("aria-current", "page");
      }
    });
  }

  function init() {
    document.documentElement.classList.add("app-shell-ready");
    enhanceBrand();
    addSkipLink();
    addGlobalSearch();
    addGlobalSpoilerToggle();
    markCurrentNavigation();
  }

  document.addEventListener("DOMContentLoaded", init);
})();
