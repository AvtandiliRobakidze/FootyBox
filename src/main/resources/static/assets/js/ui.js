const FootyBoxUI = (() => {
  const TOKEN_KEY = "footybox.token";
  const SPOILER_KEY = "footybox-spoiler-free";
  const localPhotos = [
    "/assets/editorial/library/stadium-night.webp",
    "/assets/editorial/library/bernabeu.webp",
    "/assets/editorial/library/supporter-smoke.webp",
    "/assets/editorial/library/goalkeeper-action.webp",
    "/assets/editorial/library/tackle-action.webp",
    "/assets/editorial/library/womens-action.webp",
    "/assets/editorial/library/running-action.webp",
    "/assets/editorial/library/match-ball.webp"
  ];

  const icons = {
    ball: '<svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9.4" fill="currentColor"/><path d="M12 6.6l2.7 2-1 3.2h-3.4l-1-3.2 2.7-2z" fill="rgba(0,0,0,.55)"/></svg>',
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4-4"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 5v14M5 12h14"/></svg>',
    compass: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M15.5 8.5l-2 5-5 2 2-5 5-2z"/></svg>',
    archive: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="4" rx="1"/><path d="M5 8v11h14V8M10 12h4"/></svg>',
    burger: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 7h16M4 12h16M4 17h16"/></svg>',
    close: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6l12 12M18 6L6 18"/></svg>',
    heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s-7-4.4-9.5-8.5C.9 9.8 2.2 6 5.5 6 8 6 9.4 8 12 10c2.6-2 4-4 6.5-4 3.3 0 4.6 3.8 3 6.5C19 16.6 12 21 12 21z"/></svg>',
    comment: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a8 8 0 01-11.6 7.1L4 20l1-5A8 8 0 1121 12z"/></svg>',
    share: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="2.5"/><circle cx="6" cy="12" r="2.5"/><circle cx="18" cy="19" r="2.5"/><path d="M8.2 10.8l7.6-4.4M8.2 13.2l7.6 4.4"/></svg>',
    bookmark: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 4h12v16l-6-4-6 4z"/></svg>',
    stadium: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 8c0-1.5 4-2.5 9-2.5s9 1 9 2v8c0 1.5-4 2.5-9 2.5S3 17.5 3 16V8z"/><path d="M3 8c0 1.5 4 2.5 9 2.5s9-1 9-2M9 11v5M15 11v5"/></svg>',
    lock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4.5" y="10" width="15" height="10" rx="2"/><path d="M8 10V7a4 4 0 018 0v3"/></svg>',
    play: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>',
    calendar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 9h18M8 3v4M16 3v4"/></svg>',
    pin: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s7-5.5 7-11a7 7 0 10-14 0c0 5.5 7 11 7 11z"/><circle cx="12" cy="10" r="2.5"/></svg>',
    list: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 6h12M8 12h12M8 18h12M3.5 6h.01M3.5 12h.01M3.5 18h.01"/></svg>',
    trophy: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 4h10v5a5 5 0 01-10 0V4zM7 6H4v1a3 3 0 003 3M17 6h3v1a3 3 0 01-3 3M9 16h6M10 20h4M12 14v2"/></svg>',
    users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3 2.7-5 6-5s6 2 6 5M16 6a3 3 0 010 6M22 20c0-2.5-1.6-4-4-4.5"/></svg>',
    user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7"/></svg>',
    image: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="8.5" cy="9.5" r="1.5"/><path d="M21 16l-5-5L5 20"/></svg>',
    check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M4 12l5 5L20 6"/></svg>',
    eye: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg>',
    fire: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M13 2c.5 4-2.5 5-2.5 8 0 1.4-1 2-1.8 1.2-.7-.7-.7-2-.7-2C5.5 11 4 13.5 4 16a8 8 0 1016 0c0-5-4-7-4-10 0 1.5-1 2.5-2 2.5C13 8.5 13 5 13 2z"/></svg>'
  };

  function isAuthed() {
    return Boolean(localStorage.getItem(TOKEN_KEY));
  }

  function localizeImages() {
    document.querySelectorAll('img[src^="http"], img[src*="picsum.photos"]').forEach((img, index) => {
      img.src = localPhotos[index % localPhotos.length];
      img.loading = "lazy";
      img.removeAttribute("referrerpolicy");
    });
  }

  function setSpoilerMode(toggle, enabled) {
    document.body.classList.toggle("spoiler-hide", enabled);
    toggle?.classList.toggle("on", enabled);
    document.querySelectorAll(".spoiler-veil").forEach(item => item.classList.toggle("locked", enabled));
    document.querySelectorAll("[data-spoiler-free], [data-detail-spoiler-free], [data-spoiler-toggle-state]").forEach(input => {
      input.checked = enabled;
      input.dispatchEvent(new Event("change", { bubbles: true }));
    });
  }

  function buildNav(active) {
    const authed = isAuthed();
    const links = [
      ["feed", "Feed", "/"],
      ["discover", "Discover", "/discover.html"],
      ["archive", "Archive", "/archive.html"],
      ["lists", "Lists", "/lists.html"],
      ["profile", "Profile", authed ? "/profile.html" : "/register.html"]
    ];
    const right = authed
      ? `<a class="btn btn-primary btn-sm" href="/archive.html">${icons.plus} Log</a>
         <div class="avatar-menu" id="avatarMenu">
           <button class="avatar-btn" id="avatarBtn" aria-label="Account"><img class="avatar" data-account-avatar src="/assets/img/footybox-logo.png" alt="Account"></button>
           <div class="menu"><div class="menu-head"><img data-account-avatar src="/assets/img/footybox-logo.png" alt=""><div><div class="nm" data-account-name>FootyBox account</div><div class="hd" data-account-subtitle>Your football diary</div></div></div><a href="/profile.html">${icons.user} Your diary</a><a href="/archive.html">${icons.plus} Log a match</a><button id="signOut">${icons.lock} Sign out</button></div>
         </div>`
      : `<a class="btn btn-ghost btn-sm" href="/login.html">Sign in</a><a class="btn btn-primary btn-sm" href="/register.html">Sign up</a>`;

    const nav = document.createElement("header");
    nav.className = "nav";
    nav.innerHTML = `<div class="wrap">
      <a class="brand" href="/" aria-label="FootyBox">Footy<span class="b">box</span></a>
      <nav class="nav-links">${links.map(([id, label, href]) => `<a href="${href}" class="${id === active ? "active" : ""}">${label}</a>`).join("")}</nav>
      <label class="nav-search" for="globalSearch">${icons.search}<input id="globalSearch" type="text" placeholder="Search teams, matches, players..." autocomplete="off"><kbd>/</kbd></label>
      <div class="nav-right"><label class="spoiler-toggle" title="Hide scores and outcomes"><span class="lbl">Spoiler-free</span><span class="switch" id="spoilerSwitch"></span></label>${right}<button class="nav-burger" id="navBurger" aria-label="Menu">${icons.burger}</button></div>
    </div>`;
    document.body.prepend(nav);

    nav.querySelector("#navBurger")?.addEventListener("click", () => nav.classList.toggle("menu-open"));
    nav.querySelector("#avatarBtn")?.addEventListener("click", event => {
      event.stopPropagation();
      nav.querySelector("#avatarMenu")?.classList.toggle("open");
    });
    document.addEventListener("click", () => nav.querySelector("#avatarMenu")?.classList.remove("open"));
    nav.querySelector("#signOut")?.addEventListener("click", () => {
      localStorage.removeItem(TOKEN_KEY);
      window.location.href = "/";
    });
    if (authed) fetch("/api/auth/me", { headers: { Authorization: `Bearer ${localStorage.getItem(TOKEN_KEY)}` } }).then(response => response.ok ? response.json() : null).then(user => {
      if (!user) return;
      nav.querySelector("[data-account-name]").textContent = user.displayName || user.username;
      nav.querySelector("[data-account-subtitle]").textContent = `@${user.username}`;
      nav.querySelectorAll("[data-account-avatar]").forEach(image => { image.src = user.avatarUrl || "/assets/img/footybox-logo.png"; });
    }).catch(() => {});
    const search = nav.querySelector("#globalSearch");
    search?.addEventListener("keydown", event => {
      if (event.key === "Enter") window.location.href = `/archive.html?q=${encodeURIComponent(search.value.trim())}`;
    });
    document.addEventListener("keydown", event => {
      if (event.key === "/" && !["INPUT", "TEXTAREA"].includes(document.activeElement.tagName)) {
        event.preventDefault();
        search?.focus();
      }
    });
    const spoiler = nav.querySelector("#spoilerSwitch");
    let enabled = localStorage.getItem(SPOILER_KEY) === "1";
    setSpoilerMode(spoiler, enabled);
    spoiler?.addEventListener("click", () => {
      enabled = !enabled;
      localStorage.setItem(SPOILER_KEY, enabled ? "1" : "0");
      setSpoilerMode(spoiler, enabled);
    });
  }

  function buildFooter() {
    if (document.querySelector(".footer")) return;
    const footer = document.createElement("footer");
    footer.className = "footer compact-footer";
    footer.innerHTML = `<div class="wrap"><div class="cols"><div><a class="brand" href="/">Footy<span class="b">box</span></a><p class="muted" style="margin-top:14px;max-width:34ch;font-size:13.5px;">Your football life, logged forever.</p></div><div><h5>Explore</h5><a href="/discover.html">Discover</a><a href="/archive.html">Match Archive</a><a href="/lists.html">Lists</a></div><div><h5>Your FootyBox</h5><a href="/profile.html">Your diary</a><a href="/archive.html">Log a match</a></div></div></div>`;
    document.body.appendChild(footer);
  }

  function injectIcons() {
    document.querySelectorAll("[data-icon]").forEach(element => {
      const icon = icons[element.dataset.icon];
      if (icon && !element.dataset.iconReady) {
        element.innerHTML = icon + element.innerHTML;
        element.dataset.iconReady = "true";
      }
    });
  }

  function renderRatings() {
    document.querySelectorAll(".rating[data-rating]").forEach(element => {
      const value = Number(element.dataset.rating || 0);
      element.setAttribute("role", "img");
      element.setAttribute("aria-label", `${value.toFixed(1)} out of 5 stars`);
      element.innerHTML = Array.from({ length: 5 }, (_, index) => {
        const fill = value >= index + 1 ? "full" : value >= index + 0.5 ? "half" : "";
        return `<span class="star ${fill}" aria-hidden="true"><span>★</span></span>`;
      }).join("") + (element.dataset.num === "true" ? `<span class="rating-num">${value.toFixed(1)}</span>` : "");
    });
  }

  function initRateInputs() {
    document.querySelectorAll(".rate-input").forEach(input => {
      let value = Number(input.dataset.value || 0);
      const paint = preview => input.querySelectorAll("button").forEach((item, index) => {
        const active = index < preview;
        item.classList.toggle("lit", active);
        item.setAttribute("aria-pressed", String(index + 1 === value));
      });
      input.setAttribute("role", "group");
      input.setAttribute("aria-label", "Choose a rating out of 5 stars");
      input.innerHTML = Array.from({ length: 5 }, (_, index) => `<button type="button" class="star ${index < value ? "lit" : ""}" data-value="${index + 1}" aria-label="${index + 1} out of 5 stars" aria-pressed="${index + 1 === value}">★</button>`).join("");
      input.addEventListener("click", event => {
        const button = event.target.closest("button[data-value]");
        if (!button) return;
        value = Number(button.dataset.value);
        input.dataset.value = String(value);
        paint(value);
        const ratingField = input.closest("form")?.querySelector('[name="rating"]') || document.querySelector('[name="rating"]');
        if (ratingField) ratingField.value = String(value);
      });
      input.addEventListener("pointerover", event => {
        const button = event.target.closest("button[data-value]");
        if (button) paint(Number(button.dataset.value));
      });
      input.addEventListener("pointerleave", () => paint(value));
    });
  }

  function initTabs() {
    document.querySelectorAll(".tabs").forEach(tabs => {
      tabs.addEventListener("click", event => {
        const button = event.target.closest("button[data-tab]");
        if (!button) return;
        const group = tabs.dataset.group;
        tabs.querySelectorAll("button").forEach(item => item.classList.toggle("active", item === button));
        document.querySelectorAll(`.tab-panel[data-group="${group}"]`).forEach(panel => panel.classList.toggle("active", panel.dataset.tab === button.dataset.tab));
      });
    });
    document.querySelectorAll(".pill-tabs").forEach(tabs => tabs.addEventListener("click", event => {
      const button = event.target.closest("button");
      if (!button) return;
      tabs.querySelectorAll("button").forEach(item => item.classList.toggle("active", item === button));
    }));
  }

  function initInteractions() {
    document.querySelectorAll(".filter-chip:not([data-code]):not([data-list-filter])").forEach(chip => chip.addEventListener("click", () => chip.classList.toggle("active")));
    document.querySelectorAll(".score-blur").forEach(score => score.addEventListener("click", () => score.classList.remove("score-blur")));
    document.querySelectorAll("[data-modal-open]").forEach(trigger => trigger.addEventListener("click", event => {
      event.preventDefault();
      document.querySelector(trigger.dataset.modalOpen)?.classList.add("open");
    }));
    document.querySelectorAll("[data-modal-close]").forEach(trigger => trigger.addEventListener("click", () => trigger.closest(".modal-backdrop")?.classList.remove("open")));
    const revealItems = document.querySelectorAll(".reveal");
    if (!("IntersectionObserver" in window)) revealItems.forEach(item => item.classList.add("in"));
    else {
      const observer = new IntersectionObserver(entries => entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("in");
          observer.unobserve(entry.target);
        }
      }), { threshold: 0.1 });
      revealItems.forEach(item => observer.observe(item));
    }
  }

  function initListFilters() {
    const grid = document.querySelector("[data-list-grid]");
    const buttons = document.querySelectorAll("[data-list-filter]");
    if (!grid || !buttons.length) return;
    buttons.forEach(button => button.addEventListener("click", () => {
      buttons.forEach(item => item.classList.toggle("active", item === button));
      const filter = button.dataset.listFilter;
      const cards = [...grid.querySelectorAll("[data-list-card]")];
      cards.forEach(card => { card.hidden = !["all", "newest"].includes(filter) && card.dataset.category !== filter; });
      cards.sort((a, b) => filter === "newest" ? Number(b.dataset.date) - Number(a.dataset.date) : Number(a.dataset.rank) - Number(b.dataset.rank)).forEach(card => grid.appendChild(card));
    }));
  }

  function init() {
    localizeImages();
    if (document.body.dataset.chrome !== "false") {
      buildNav(document.body.dataset.nav || "");
      if (document.body.dataset.nav !== "feed") buildFooter();
    }
    injectIcons();
    renderRatings();
    initRateInputs();
    initTabs();
    initInteractions();
    initListFilters();
  }

  return { init, icons, renderRatings, injectIcons };
})();

document.addEventListener("DOMContentLoaded", FootyBoxUI.init);
