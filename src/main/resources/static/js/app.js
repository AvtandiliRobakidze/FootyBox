const FootyBox = (() => {
  const tokenKey = "footybox.token";
  const avatarPaths = {
    pitch: "/assets/avatars/pitch.webp",
    trophy: "/assets/avatars/trophy.webp",
    floodlights: "/assets/avatars/floodlights.webp",
  };
  const placeholderPosters = [
    "/assets/editorial/library/stadium-night.webp",
    "/assets/editorial/library/bernabeu.webp",
    "/assets/editorial/library/supporter-smoke.webp",
    "/assets/editorial/library/goalkeeper-action.webp",
    "/assets/editorial/library/tackle-action.webp",
    "/assets/editorial/library/womens-action.webp",
    "/assets/editorial/library/running-action.webp",
    "/assets/editorial/library/match-ball.webp"
  ];
  let activeMatchState = null;
  let activeMatch = null;

  function picturePath(filename) {
    return `/assets/pictures/${encodeURI(filename)}`;
  }

  const competitionPosters = {
    CL: picturePath("ucl.webp"),
    PL: picturePath("flag-of-premier-league.webp"),
    PD: picturePath("la-liga.webp"),
    SA: picturePath("Seria A.webp"),
    BL1: "/assets/editorial/library/bayern-night.webp",
    FL1: "/assets/editorial/library/lens-supporters.webp",
    DED: picturePath("Logo_Eredivisie.webp"),
    PPL: picturePath("primera liga.webp"),
    ELC: picturePath("EFL Championship.webp"),
    BSA: picturePath("BRASILEIRAO.webp"),
    EC: picturePath("Uefa Euro.webp"),
    WC: "/assets/editorial/library/mexico-opening.webp"
  };
  const competitionArtwork = competitionPosters;

  const competitionNameToCode = {
    "premier league": "PL",
    "uefa champions league": "CL",
    "champions league": "CL",
    "primera division": "PD",
    "la liga": "PD",
    "serie a": "SA",
    "bundesliga": "BL1",
    "ligue 1": "FL1",
    "eredivisie": "DED",
    "primeira liga": "PPL",
    "campeonato brasileiro serie a": "BSA",
    "campeonato brasileiro série a": "BSA",
    "championship": "ELC",
    "european championship": "EC",
    "uefa european championship": "EC",
    "fifa world cup": "WC",
    "world cup": "WC"
  };

  function resolveCompetitionCode(source) {
    const direct = source?.competitionCode;
    if (direct && competitionPosters[direct]) {
      return direct;
    }
    const label = String(source?.competition || direct || "").trim();
    if (!label) return "";
    if (competitionPosters[label]) return label;
    if (label.length <= 4 && /^[A-Z0-9]+$/i.test(label)) {
      return label.toUpperCase();
    }
    return competitionNameToCode[label.toLowerCase()] || "";
  }

  function token() {
    return localStorage.getItem(tokenKey);
  }

  function setToken(value) {
    localStorage.setItem(tokenKey, value);
  }

  function logout() {
    localStorage.removeItem(tokenKey);
    window.location.href = "/";
  }

  async function api(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (!(options.body instanceof FormData) && !headers["Content-Type"]) headers["Content-Type"] = "application/json";
    if (token()) {
      headers.Authorization = `Bearer ${token()}`;
    }
    const response = await fetch(path, { ...options, headers });
    if (!response.ok) {
      let message = `Request failed (${response.status})`;
      try {
        const body = await response.json();
        message = body.message || message;
      } catch {
        // Keep the generic message if the response is not JSON.
      }
      if (response.status === 401 && token()) {
        localStorage.removeItem(tokenKey);
        if (document.body.dataset.protected === "true") window.location.href = "/login.html";
      }
      throw new Error(message);
    }
    if (response.status === 204) {
      return null;
    }
    return response.json();
  }

  function authNav() {
    const slot = document.querySelector("[data-auth-nav]");
    if (!slot) return;
    slot.innerHTML = token()
      ? `<button class="button" type="button" data-logout>Log out</button>`
      : `<a class="button" href="/login.html">Sign in</a><a class="button primary" href="/register.html">Sign up</a>`;
    const button = slot.querySelector("[data-logout]");
    if (button) button.addEventListener("click", logout);
  }

  function safe(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function fmtDate(value) {
    return new Intl.DateTimeFormat(undefined, {
      dateStyle: "medium",
      timeStyle: "short"
    }).format(new Date(value));
  }

  function shortDate(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "Date TBC";
    }
    return new Intl.DateTimeFormat(undefined, {
      day: "2-digit",
      month: "short",
      year: "numeric"
    }).format(date);
  }

  function matchYear(match) {
    const date = new Date(match.utcDate);
    return Number.isNaN(date.getTime()) ? null : date.getFullYear();
  }

  function teamInitials(name) {
    return String(name || "?")
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part[0])
      .join("")
      .toUpperCase();
  }

  function hashString(value) {
    let hash = 0;
    const text = String(value || "");
    for (let index = 0; index < text.length; index += 1) {
      hash = (hash << 5) - hash + text.charCodeAt(index);
      hash |= 0;
    }
    return Math.abs(hash);
  }

  function teamBadgeStyle(name) {
    const hash = hashString(name);
    const hueA = hash % 360;
    const hueB = (hash * 7 + 120) % 360;
    return `background:linear-gradient(135deg,hsl(${hueA} 62% 42%),hsl(${hueB} 58% 28%))`;
  }

  function avatarMarkup(user, className = "avatar crest") {
    const path = user?.avatarUrl || avatarPaths[user?.avatarKey];
    return path
      ? `<img class="${className}" src="${path}" alt="${safe(user.displayName || user.username)} avatar">`
      : `<span class="${className}">${safe(teamInitials(user?.displayName || user?.username))}</span>`;
  }

  function crestUrl(match, side) {
    const prefix = side === "home" ? "home" : "away";
    return match[`${prefix}CrestUrl`] || match[`${prefix}TeamCrestUrl`] || match[`${prefix}TeamCrest`] || "";
  }

  function crest(match, side, name, sizeClass = "s") {
    const url = crestUrl(match, side);
    const style = teamBadgeStyle(name);
    const initials = safe(teamInitials(name));
    if (url) {
      return `<span class="crest ${sizeClass} has-badge is-fallback" style="${style}"><span class="crest-fallback-text">${initials}</span><img class="crest-badge" src="${safe(url)}" alt="" loading="lazy" onerror="this.remove(); this.parentElement.classList.remove('has-badge');"></span>`;
    }
    return `<span class="crest ${sizeClass} is-fallback" style="${style}"><span class="crest-fallback-text">${initials}</span></span>`;
  }

  function competitionKey(match) {
    return resolveCompetitionCode(match);
  }

  function matchPosterUrl(match) {
    const code = resolveCompetitionCode(match);
    if (code && competitionPosters[code]) {
      return competitionPosters[code];
    }
    const seed = hashString(`${match.id || ""}-${match.homeTeam}-${match.awayTeam}`);
    return placeholderPosters[seed % placeholderPosters.length];
  }

  function posterClass(match) {
    const code = String(competitionKey(match)).toLowerCase();
    return code ? `poster--${code}` : "";
  }

  function clubStripColors(match) {
    const home = teamBadgeStyle(match.homeTeam);
    const away = teamBadgeStyle(match.awayTeam);
    const homeColor = home.match(/hsl\(([^)]+)\)/)?.[1] || "210 60% 45%";
    const awayColor = away.match(/hsl\(([^)]+)\)/)?.[1] || "350 55% 40%";
    return `<span style="background:hsl(${homeColor})"></span><span style="background:hsl(${awayColor})"></span>`;
  }

  function score(match) {
    if (match.spoilerFree) {
      return "Hidden";
    }
    if (match.homeScore === null || match.awayScore === null) return "Upcoming";
    return `${match.homeScore}-${match.awayScore}`;
  }

  function competitionName(name) {
    return name === "Primera Division" ? "La Liga" : name;
  }

  function archiveScore(match) {
    if (match.spoilerFree || match.homeScore === null || match.awayScore === null) {
      return `<div class="archive-score archive-score-hidden"><span>Score hidden</span><small>Spoiler-free</small></div>`;
    }
    return `<div class="archive-score"><span>${safe(score(match))}</span><small>Final score</small></div>`;
  }

  function providerLabel(match) {
    return match.provider || match.providerName || "local archive";
  }

  function matchCard(match, compact = false) {
    const title = `${match.homeTeam} vs ${match.awayTeam}`;
    const year = matchYear(match);
    const poster = matchPosterUrl(match);
    const posterModifier = posterClass(match);
    return `
      <a class="match-card${compact ? " match-card--compact" : ""}" href="/matches.html?id=${safe(match.id)}" aria-label="Open ${safe(title)}">
        <div class="poster ${posterModifier}">
          <div class="club-strip">${clubStripColors(match)}</div>
          <img src="${safe(poster)}" alt="" loading="lazy">
          <div class="poster-top"><span class="comp-tag">${safe(competitionName(match.competition) || "Competition TBC")}${year ? ` · ${year}` : ""}</span></div>
          <div class="poster-bottom">
            <div class="match-teams" style="font-size:15px;">
              ${crest(match, "home", match.homeTeam)}
              <span class="vs">vs</span>
              ${crest(match, "away", match.awayTeam)}
            </div>
          </div>
        </div>
        <div class="body">
          <h3 style="font-size:16px;margin-bottom:8px;">${safe(title)}</h3>
          <div class="row center" style="justify-content:space-between;">
            <span class="meta-line">${safe(shortDate(match.utcDate))}</span>
            <span class="score hide-on-spoiler">${safe(score(match))}</span>
            <span class="spoiler-only muted">score hidden</span>
          </div>
          <div class="meta-line" style="margin-top:8px;">${safe(match.venue || providerLabel(match))}</div>
        </div>
      </a>
    `;
  }

  function savedMatchCard(item) {
    const code = resolveCompetitionCode(item);
    const poster = code && competitionPosters[code]
      ? competitionPosters[code]
      : placeholderPosters[hashString(item.matchId) % placeholderPosters.length];
    const homeStyle = teamBadgeStyle(item.homeTeam || item.matchTitle);
    const awayStyle = teamBadgeStyle(item.awayTeam || `${item.matchTitle}-away`);
    return `
      <a class="match-card match-card--compact" href="/matches.html?id=${safe(item.matchId)}">
        <div class="poster ${code ? `poster--${code.toLowerCase()}` : ""}">
          <img src="${safe(poster)}" alt="" loading="lazy">
          <div class="poster-bottom">
            <div class="match-teams">
              <span class="crest s is-fallback" style="${homeStyle}"><span class="crest-fallback-text">${safe(teamInitials(item.homeTeam || item.matchTitle))}</span>${item.homeTeamCrestUrl ? `<img class="crest-badge" src="${safe(item.homeTeamCrestUrl)}" alt="" loading="lazy" onerror="this.remove()">` : ""}</span>
              <span class="vs">vs</span>
              <span class="crest s is-fallback" style="${awayStyle}"><span class="crest-fallback-text">${safe(teamInitials(item.awayTeam || "?"))}</span>${item.awayTeamCrestUrl ? `<img class="crest-badge" src="${safe(item.awayTeamCrestUrl)}" alt="" loading="lazy" onerror="this.remove()">` : ""}</span>
            </div>
          </div>
        </div>
        <div class="body">
          <h3>${safe(item.matchTitle)}</h3>
          <p class="meta-line">${safe(item.competition)} · ${safe(shortDate(item.utcDate))}</p>
        </div>
      </a>
    `;
  }

  function top4Slot(log, index, detail) {
    if (!log) {
      return `
        <div class="t4 is-empty">
          <div class="empty-slot">
            <div class="empty-slot-num">${index + 1}</div>
            <div class="empty-slot-label">Empty slot</div>
            <span class="muted">Log a match</span>
          </div>
        </div>
      `;
    }
    const poster = detail ? matchPosterUrl(detail) : placeholderPosters[index % placeholderPosters.length];
    const crests = detail
      ? `<div class="top4-crests">${crest(detail, "home", detail.homeTeam)}${crest(detail, "away", detail.awayTeam)}</div>`
      : "";
    return `
      <a href="/matches.html?id=${safe(log.matchId)}" class="t4 has-log">
        <img src="${safe(poster)}" alt="">
        ${crests}
        <div class="cap">
          <div class="tt">${safe(log.matchTitle)}</div>
          ${log.rating ? `<span class="rating sm" data-rating="${safe(log.rating)}"></span>` : ""}
        </div>
      </a>
    `;
  }

  function archiveState(kind, title, body) {
    const tone = kind === "error" ? " notice error" : "";
    return `
      <div class="card card-pad${tone}" style="grid-column:1/-1;text-align:center;">
        <h3>${safe(title)}</h3>
        <p>${safe(body)}</p>
      </div>
    `;
  }

  function friendlyArchiveError(error) {
    const original = error?.message || "The archive API did not respond.";
    if (/unexpected server error|request failed \(5\d\d\)|failed to fetch/i.test(original)) {
      return "The local archive API is unavailable right now. Check that the app and database are running, then refresh.";
    }
    return original;
  }

  function archiveLoading(count = 6) {
    return Array.from({ length: count }, () => `
      <article class="match-card" aria-hidden="true">
        <div class="poster"><img src="/assets/placeholders/match-poster.webp" alt=""></div>
        <div class="body">
          <span class="skeleton-line short"></span>
          <span class="skeleton-line title"></span>
          <span class="skeleton-line"></span>
        </div>
      </article>
    `).join("");
  }

  function archiveSkeleton(count = 4) {
    return archiveLoading(count);
  }

  function updateArchiveCount(total, shown) {
    const target = document.querySelector("[data-archive-count]");
    if (!target) return;
    if (total === 0 && shown === 0) {
      target.textContent = "";
      return;
    }
    if (total === shown) {
      target.textContent = `${shown} match${shown === 1 ? "" : "es"}`;
      return;
    }
    target.textContent = `${shown} of ${total} matches`;
  }

  function displayStars(value) {
    const rating = Number(value || 0);
    return Array.from({ length: 5 }, (_, index) => index < rating ? "\u2605" : "\u2606").join("");
  }

  function averageRating(reviews) {
    const rated = reviews
      .map(review => Number(review.rating))
      .filter(rating => Number.isFinite(rating) && rating > 0);
    if (!rated.length) return null;
    return rated.reduce((total, rating) => total + rating, 0) / rated.length;
  }

  function matchResult(match) {
    if (match.spoilerFree || match.homeScore === null || match.awayScore === null) {
      return {
        label: "Score hidden",
        sublabel: "Spoiler-free mode",
        className: "is-hidden"
      };
    }
    return {
      label: `${match.homeScore} - ${match.awayScore}`,
      sublabel: match.status || "Final score",
      className: ""
    };
  }

  function detailHero(match) {
    const result = matchResult(match);
    const poster = matchPosterUrl(match);
    return `
      <div class="row center" style="font-size:12.5px;color:var(--text-mut);margin-bottom:18px;gap:8px;">
        <a href="/competition.html">${safe(competitionName(match.competition) || "Competition")}</a><span>/</span>
        <span class="muted">${safe(match.season || "Season")}</span><span>/</span>
        <span class="soft">${match.matchday ? `Matchday ${safe(match.matchday)}` : safe(match.status || "Match")}</span>
      </div>
      <section class="match-hero reveal in">
        <div class="bg"><img src="${safe(poster)}" alt="Match night"></div>
        <div class="top-meta">
          <span class="comp-tag">${safe(competitionName(match.competition) || "Competition TBC")}</span>
        </div>
        <div class="content">
          <div class="scoreline">
            <div class="team">
              ${crest(match, "home", match.homeTeam, "l")}
              <span class="tn">${safe(match.homeTeam)}</span>
            </div>
            <div class="mid">
              <div class="hide-on-spoiler"><div class="big-score">${safe(result.label)}</div><div class="ft">${safe(result.sublabel)}</div></div>
              <div class="spoiler-only" style="flex-direction:column;align-items:center;gap:6px;"><div class="big-score" style="font-size:34px;color:var(--green-bright);">VS</div><div class="ft" style="color:var(--gold-soft);">Score hidden · spoiler-free</div></div>
            </div>
            <div class="team">
              ${crest(match, "away", match.awayTeam, "l")}
              <span class="tn">${safe(match.awayTeam)}</span>
            </div>
          </div>
          <div class="sub-meta">
            <span>${safe(shortDate(match.utcDate))}</span>
          </div>
        </div>
      </section>
    `;
  }

  function renderCommunityRating(reviews) {
    const target = document.querySelector("[data-community-rating]");
    if (!target) return;
    const average = averageRating(reviews);
    const count = reviews.filter(review => review.rating).length;
    target.innerHTML = average
      ? `
        <h3><span class="community-star">★</span> Community rating</h3>
        <div class="rating-summary"><div class="big">${average.toFixed(1)}</div><div><span class="rating" data-rating="${average.toFixed(1)}" data-num="true"></span><div class="meta" style="margin-top:5px;">Based on ${count} rated ${count === 1 ? "review" : "reviews"}</div></div></div>
      `
      : `
        <h3><span class="community-star">★</span> Community rating</h3>
        <p class="muted">No ratings yet. Be the first to shape this match's reputation.</p>
      `;
    if (typeof FootyBoxUI !== "undefined") {
      FootyBoxUI.injectIcons();
      FootyBoxUI.renderRatings();
    }
  }

  function renderDiaryState(matchId) {
    const target = document.querySelector("[data-diary-state]");
    if (!target) return;
    if (!token()) {
      target.innerHTML = `
        <h3>Your diary</h3>
        <p class="muted">Sign in to log this match, rate it, and mark stadium memories.</p>
        <a class="btn btn-primary btn-sm" href="/login.html">Sign in</a>
      `;
      return;
    }
    const entry = activeMatchState?.log;
    target.innerHTML = entry
        ? `
          <h3>Your diary</h3>
          <p style="color:var(--green);font-weight:700;">Logged${entry.rating ? ` · ${safe(entry.rating)} out of 5` : ""}</p>
          <p class="muted">${entry.seenInStadium ? "Seen in stadium" : "Watched"}${entry.playerOfMatchName ? ` / POTM: ${safe(entry.playerOfMatchName)}` : ""}</p>
          <button class="btn btn-primary btn-sm" type="button" data-open-log data-match-id="${safe(matchId)}">Edit diary entry</button>
        `
        : `
          <h3>Your diary</h3>
          <p class="muted">Not logged yet. Open the dialog to rate, review, and save this match.</p>
          <button class="btn btn-primary btn-sm" type="button" data-open-log data-match-id="${safe(matchId)}">Log / Review</button>
        `;
  }

  function toDateInput(value) {
    if (!value) return "";
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  function toInstantDate(value) {
    if (!value) return null;
    const date = value === toDateInput(new Date()) ? new Date() : new Date(`${value}T12:00:00`);
    return Number.isNaN(date.getTime()) ? null : date.toISOString();
  }

  function setLogMessage(message, error = false) {
    const target = document.querySelector("[data-log-message]");
    if (!target) return;
    target.textContent = message;
    target.classList.toggle("error", error);
  }

  function openLogDialog(button) {
    const dialog = document.querySelector("[data-log-dialog]");
    const form = document.querySelector("[data-log-form]");
    if (!dialog || !form) return;
    if (!token()) {
      window.location.href = "/login.html";
      return;
    }
    const params = new URLSearchParams(window.location.search);
    const matchId = button?.dataset.matchId || params.get("id");
    if (!matchId) {
      showError(new Error("Open a match before logging it."));
      return;
    }
    form.reset();
    form.elements.matchId.value = matchId;
    const entry = activeMatchState?.log;
    form.dataset.logId = entry?.id || "";
    form.elements.rating.value = entry?.rating || "";
    form.elements.reviewText.value = entry?.reviewText || "";
    form.elements.seenInStadium.checked = Boolean(entry?.seenInStadium);
    form.elements.containsSpoilers.checked = Boolean(entry?.containsSpoilers);
    form.elements.playerOfMatchName.value = entry?.playerOfMatchName || "";
    form.elements.watchedAt.value = toDateInput(entry?.watchedAt);
    form.elements.watchedAt.max = toDateInput(new Date());
    const ratingInput = form.querySelector(".rate-input");
    if (ratingInput) {
      ratingInput.dataset.value = String(entry?.rating || 0);
      ratingInput.querySelectorAll("button").forEach((item, index) => {
        item.classList.toggle("lit", index < Number(entry?.rating || 0));
        item.setAttribute("aria-pressed", String(index + 1 === Number(entry?.rating || 0)));
      });
    }
    const deleteButton = form.querySelector("[data-delete-log]");
    if (deleteButton) deleteButton.classList.toggle("hidden", !entry);
    const title = button?.dataset.matchTitle || (activeMatch ? `${activeMatch.homeTeam} vs ${activeMatch.awayTeam}` : "Selected match");
    const titleSlot = document.querySelector("[data-log-match-title]");
    if (titleSlot) titleSlot.textContent = title;
    setLogMessage("");
    dialog.showModal();
    form.elements.watchedAt.focus();
  }

  function closeLogDialog() {
    document.querySelector("[data-log-dialog]")?.close();
  }

  function validateLogForm(form) {
    const rating = form.elements.rating.value;
    const reviewText = form.elements.reviewText.value.trim();
    const watchedAt = form.elements.watchedAt.value;
    if (!watchedAt) return "Choose when you watched this match.";
    if (watchedAt > toDateInput(new Date())) return "Watched date cannot be in the future.";
    if (rating && (Number(rating) < 1 || Number(rating) > 5)) return "Rating must be between 1 and 5.";
    if (reviewText.length > 4000) return "Review must be 4000 characters or less.";
    if (form.elements.playerOfMatchName.value.trim().length > 160) return "Player of the Match must be 160 characters or less.";
    return "";
  }

  async function loadMatches() {
    const list = document.querySelector("[data-match-list]");
    if (!list) return;
    const queryInput = document.querySelector("[data-match-query]");
    if (queryInput && !queryInput.value) {
      queryInput.value = new URLSearchParams(window.location.search).get("q") || "";
    }
    const query = queryInput?.value || "";
    const spoilerFree = document.querySelector("[data-spoiler-free]")?.checked || false;
    const competitionCode = document.querySelector("[data-archive-competition]")?.value || "";
    const decade = document.querySelector("[data-archive-decade]")?.value || "";
    const sort = document.querySelector("[data-archive-sort]")?.value || "newest";
    const message = document.querySelector("[data-page-message]");
    if (message) {
      message.textContent = "";
      message.classList.add("hidden");
      message.classList.remove("error");
    }
    updateArchiveCount(0, 0);
    list.innerHTML = archiveLoading();
    try {
      const params = new URLSearchParams({ query, spoilerFree: String(spoilerFree), sort });
      if (competitionCode) params.set("competitionCode", competitionCode);
      if (decade) params.set("decade", decade);
      const matches = await api(`/api/matches?${params}`);
      updateArchiveCount(matches.length, matches.length);
      list.innerHTML = matches.length
        ? matches.map(matchCard).join("")
        : archiveState("empty", "No matches found", "Try a broader search, another decade, or clearing the competition filter.");
      if (typeof FootyBoxUI !== "undefined") {
        FootyBoxUI.renderRatings();
        FootyBoxUI.injectIcons();
      }
    } catch (error) {
      updateArchiveCount(0, 0);
      list.innerHTML = archiveState("error", "Archive temporarily unavailable", friendlyArchiveError(error));
      throw error;
    }
  }

  async function loadCompetitions() {
    const select = document.querySelector("[data-archive-competition]");
    const list = document.querySelector("[data-competition-list]");
    if (!select && !list) return;
    const status = document.querySelector("[data-competition-status]");
    try {
      const competitions = await api("/api/competitions");
      if (select) {
        select.innerHTML = `<option value="">All competitions</option>` + competitions.map(competition =>
          `<option value="${safe(competition.code || "")}">${safe(competitionName(competition.name))} (${safe(competition.matchCount)})</option>`
        ).join("");
        const requested = new URLSearchParams(window.location.search).get("competition") || "";
        if (requested && competitions.some(competition => competition.code === requested)) select.value = requested;
      }
      if (list) {
        list.innerHTML = competitions.map((competition, index) => {
          const image = competitionArtwork[competition.code];
          return `<a class="competition-choice ${index === 0 ? "featured" : ""}" href="/archive.html?competition=${encodeURIComponent(competition.code || "")}">
            ${image ? `<img src="${image}" alt="" loading="lazy">` : `<span class="competition-monogram">${safe(competition.code || teamInitials(competition.name))}</span>`}
            <span><strong>${safe(competitionName(competition.name))}</strong><small>${safe(competition.country || "International")} · ${safe(competition.matchCount)} matches</small></span>
          </a>`;
        }).join("");
      }
      if (status) status.textContent = `${competitions.length} competitions available`;
    } catch (error) {
      if (status) status.textContent = "Competition list unavailable";
      if (list) list.innerHTML = archiveState("error", "Competitions unavailable", friendlyArchiveError(error));
    }
  }

  async function loadDiscover() {
    const listTarget = document.querySelector("[data-discover-list]");
    const classicsTarget = document.querySelector("[data-discover-classics]");
    const filters = document.querySelector("[data-discover-filters]");
    if (!listTarget && !classicsTarget) return;
    const render = async code => {
      if (listTarget) listTarget.innerHTML = archiveSkeleton(4);
      if (classicsTarget) classicsTarget.innerHTML = archiveSkeleton(4);
      const query = new URLSearchParams({ spoilerFree: String(document.body.classList.contains("spoiler-hide")), sort: "newest" });
      if (code) query.set("competitionCode", code);
      const matches = await api(`/api/matches?${query}`);
      const emptyState = archiveState("empty", "No spoiler-free matches yet", "Import archive seasons to populate Discover.");
      if (listTarget) {
        listTarget.innerHTML = matches.length
          ? matches.slice(0, 4).map(match => matchCard(match, true)).join("")
          : emptyState;
      }
      if (classicsTarget) {
        classicsTarget.innerHTML = matches.length > 4
          ? matches.slice(4, 8).map(match => matchCard(match, true)).join("")
          : matches.length
            ? matches.slice(0, Math.min(4, matches.length)).map(match => matchCard(match, true)).join("")
            : emptyState;
      }
      if (typeof FootyBoxUI !== "undefined") {
        FootyBoxUI.injectIcons();
        FootyBoxUI.renderRatings();
      }
    };
    try {
      const competitions = await api("/api/competitions");
      if (filters) {
        filters.innerHTML = `<button class="filter-chip active" type="button" data-code="">All</button>` + competitions.map(item => `<button class="filter-chip" type="button" data-code="${safe(item.code)}">${safe(competitionName(item.name))}</button>`).join("");
        filters.addEventListener("click", async event => {
          const button = event.target.closest("button[data-code]");
          if (!button) return;
          filters.querySelectorAll("button").forEach(item => item.classList.toggle("active", item === button));
          await render(button.dataset.code);
        });
      }
      document.querySelector("[data-spoiler-toggle-state]")?.addEventListener("change", () => render(filters?.querySelector(".active")?.dataset.code || ""));
      await render("");
    } catch (error) {
      const errorCard = archiveState("error", "Discover temporarily unavailable", friendlyArchiveError(error));
      if (listTarget) listTarget.innerHTML = errorCard;
      if (classicsTarget) classicsTarget.innerHTML = errorCard;
    }
  }

  async function loadMatchDetail() {
    const detail = document.querySelector("[data-match-detail]");
    if (!detail) return;
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    if (!id) {
      window.location.replace("/archive.html");
      return;
    }
    document.querySelector("[data-browse-view]")?.classList.add("hidden");
    document.querySelector("[data-detail-view]")?.classList.remove("hidden");
    const spoilerFree = document.querySelector("[data-detail-spoiler-free]")?.checked || false;
    const match = await api(`/api/matches/${id}?spoilerFree=${spoilerFree}`);
    activeMatch = match;
    if (token()) {
      try { activeMatchState = await api(`/api/matches/${id}/me`); } catch { activeMatchState = null; }
    } else {
      activeMatchState = null;
    }
    detail.innerHTML = detailHero(match);
    if (typeof FootyBoxUI !== "undefined") {
      FootyBoxUI.injectIcons();
    }
    document.querySelectorAll("[data-favorite]").forEach(button => {
      button.dataset.matchId = id;
      button.dataset.favorite = String(Boolean(activeMatchState?.favorite));
      button.textContent = activeMatchState?.favorite ? "Favourited" : "Favourite";
      button.classList.toggle("btn-primary", Boolean(activeMatchState?.favorite));
    });
    await loadReviews(id, spoilerFree);
    renderDiaryState(id);
    if (params.get("editLog") && activeMatchState?.log && String(activeMatchState.log.id) === params.get("editLog")) {
      openLogDialog({ dataset: { matchId: id } });
    }
  }

  async function loadReviews(matchId, spoilerFree = document.body.classList.contains("spoiler-hide")) {
    const target = document.querySelector("[data-review-list]");
    if (!target) return;
    const reviews = await api(`/api/matches/${matchId}/reviews?spoilerFree=${spoilerFree}`);
    renderCommunityRating(reviews);
    target.innerHTML = reviews.length
      ? reviews.map(review => `
        <article class="card review-card">
          <div class="head">
            <span class="avatar crest">${safe(teamInitials(review.displayName))}</span>
            <div class="info">
              <div class="action"><b>${safe(review.displayName)}</b></div>
              <div class="muted" style="font-size:12px;">@${safe(review.username)} · ${safe(shortDate(review.watchedAt))}</div>
            </div>
            ${review.rating ? `<span class="rating" data-rating="${safe(review.rating)}" style="margin-left:auto;"></span>` : `<span class="muted" style="margin-left:auto;">Not rated</span>`}
          </div>
          <div class="review-body"><p>${review.reviewHidden ? "Review hidden in spoiler-free mode." : safe(review.reviewText || "Logged this match.")}</p></div>
          <div class="chip-row" style="margin-top:12px;">
            ${review.seenInStadium ? `<span class="badge stadium">Seen in stadium</span>` : ""}
            ${review.containsSpoilers ? `<span class="badge spoiler">Contains spoilers</span>` : ""}
            ${review.playerOfMatchName ? `<span class="chip gold">POTM: ${safe(review.playerOfMatchName)}</span>` : ""}
          </div>
          <div class="thread" style="margin-top:12px;">
            ${(review.comments || []).length ? review.comments.map(comment => `
              <div class="comment"><span class="avatar crest">${safe(teamInitials(comment.displayName))}</span><div class="grow"><div class="c-head"><span class="nm">${safe(comment.displayName)}</span></div><div class="c-body">${safe(comment.body)}</div></div></div>
            `).join("") : `<p class="muted">No comments yet.</p>`}
          </div>
        </article>
      `).join("")
      : archiveState("empty", "No reviews yet", "This match is waiting for its first diary entry.");
    if (typeof FootyBoxUI !== "undefined") {
      FootyBoxUI.renderRatings();
      FootyBoxUI.injectIcons();
    }
  }

  function bindLoginForms() {
    const login = document.querySelector("[data-login-form]");
    if (login) {
      login.addEventListener("submit", async event => {
        event.preventDefault();
        await submitAuth("/api/auth/login", new FormData(login), false);
      });
    }

    const register = document.querySelector("[data-register-form]");
    if (register) {
      register.addEventListener("submit", async event => {
        event.preventDefault();
        await submitAuth("/api/auth/register", new FormData(register), true);
      });
    }
  }

  async function submitAuth(path, data, register) {
    const message = document.querySelector("[data-auth-message]");
    try {
      const payload = register
        ? {
            username: data.get("username"),
            email: data.get("email"),
            password: data.get("password"),
            displayName: data.get("displayName")
          }
        : { login: data.get("login"), password: data.get("password") };
      const result = await api(path, { method: "POST", body: JSON.stringify(payload) });
      setToken(result.token);
      window.location.href = "/profile.html";
    } catch (error) {
      if (message) {
        message.textContent = error.message;
        message.classList.add("error");
      }
    }
  }

  function bindMatchActions() {
    document.querySelector("[data-search-form]")?.addEventListener("submit", event => {
      event.preventDefault();
      loadMatches().catch(showError);
    });

    document.querySelector("[data-spoiler-free]")?.addEventListener("change", () => {
      loadMatches().catch(showError);
    });

    document.querySelector("[data-archive-decade]")?.addEventListener("change", () => {
      loadMatches().catch(showError);
    });

    document.querySelector("[data-archive-sort]")?.addEventListener("change", () => {
      loadMatches().catch(showError);
    });

    document.querySelector("[data-archive-competition]")?.addEventListener("change", () => {
      loadMatches().catch(showError);
    });

    document.querySelector("[data-detail-spoiler-free]")?.addEventListener("change", () => {
      loadMatchDetail().catch(showError);
    });

    document.querySelector("[data-log-form]")?.addEventListener("submit", async event => {
      event.preventDefault();
      const form = new FormData(event.currentTarget);
      const formElement = event.currentTarget;
      const validationError = validateLogForm(formElement);
      if (validationError) {
        setLogMessage(validationError, true);
        return;
      }
      const submit = formElement.querySelector("[data-log-submit]");
      try {
        if (submit) {
          submit.disabled = true;
          submit.textContent = "Saving...";
        }
        setLogMessage("Saving your diary entry...");
        const logId = formElement.dataset.logId;
        await api(logId ? `/api/diary/logs/${logId}` : "/api/diary/logs", {
          method: logId ? "PUT" : "POST",
          body: JSON.stringify({
            matchId: Number(form.get("matchId")),
            rating: form.get("rating") ? Number(form.get("rating")) : null,
            reviewText: form.get("reviewText"),
            seenInStadium: form.get("seenInStadium") === "on",
            containsSpoilers: form.get("containsSpoilers") === "on",
            playerOfMatchName: form.get("playerOfMatchName"),
            watchedAt: toInstantDate(form.get("watchedAt"))
          })
        });
        activeMatchState = await api(`/api/matches/${form.get("matchId")}/me`);
        await loadReviews(form.get("matchId"));
        renderDiaryState(form.get("matchId"));
        setLogMessage("Saved.");
        closeLogDialog();
      } catch (error) {
        setLogMessage(error.message, true);
      } finally {
        if (submit) {
          submit.disabled = false;
          submit.textContent = "Save diary entry";
        }
      }
    });

    document.querySelectorAll("[data-close-log]").forEach(button => {
      button.addEventListener("click", closeLogDialog);
    });

    document.addEventListener("click", async event => {
      const deleteLogButton = event.target.closest("[data-delete-log]");
      if (deleteLogButton) {
        const form = document.querySelector("[data-log-form]");
        const logId = form?.dataset.logId;
        if (!logId || !window.confirm("Delete this diary entry and review?")) return;
        try {
          await api(`/api/diary/logs/${logId}`, { method: "DELETE" });
          activeMatchState = { log: null, favorite: Boolean(activeMatchState?.favorite) };
          closeLogDialog();
          await loadReviews(form.elements.matchId.value);
          renderDiaryState(form.elements.matchId.value);
        } catch (error) {
          setLogMessage(error.message, true);
        }
        return;
      }

      const logButton = event.target.closest("[data-open-log]");
      if (logButton) {
        openLogDialog(logButton);
        return;
      }

      const favoriteButton = event.target.closest("[data-favorite]");
      if (!favoriteButton) return;
      const matchId = favoriteButton.dataset.matchId || new URLSearchParams(window.location.search).get("id");
      if (!matchId) return;
      try {
        favoriteButton.textContent = "Saving...";
        const isFavorite = favoriteButton.dataset.favorite === "true";
        await api(`/api/matches/${matchId}/favorite`, { method: isFavorite ? "DELETE" : "POST", body: isFavorite ? undefined : "{}" });
        const next = !isFavorite;
        if (activeMatchState) activeMatchState.favorite = next;
        document.querySelectorAll("[data-favorite]").forEach(button => {
          button.dataset.favorite = String(next);
          button.textContent = next ? "Favourited" : "Favourite";
          button.classList.toggle("btn-primary", next);
        });
        const state = document.querySelector("[data-favourite-state] .muted");
        if (state) state.textContent = next ? "Saved to your favourites." : "Not currently saved.";
      } catch (error) {
        favoriteButton.textContent = "Favourite";
        showError(error);
      }
    });
  }

  async function loadProfile() {
    const profile = document.querySelector("[data-profile]");
    if (!profile) return;
    if (!token()) {
      window.location.href = "/login.html";
      return;
    }
    const user = await api("/api/auth/me");
    const diary = await api("/api/diary/me");
    const favorites = await api("/api/favorites/me");
    const reviews = diary.filter(log => log.reviewText);
    const rated = diary.filter(log => log.rating);
    const average = rated.length
      ? (rated.reduce((total, log) => total + Number(log.rating), 0) / rated.length).toFixed(1)
      : "N/A";
    const stadiumCount = diary.filter(log => log.seenInStadium).length;
    const topLogs = diary.slice(0, 4);
    const topDetails = await Promise.all(
      topLogs.map(async log => {
        try {
          return await api(`/api/matches/${log.matchId}?spoilerFree=true`);
        } catch {
          return null;
        }
      })
    );
    const diaryRows = diary.map(log => {
      const date = new Date(log.watchedAt);
      const day = Number.isNaN(date.getTime()) ? "--" : String(date.getDate()).padStart(2, "0");
      const month = Number.isNaN(date.getTime()) ? "TBC" : new Intl.DateTimeFormat(undefined, { month: "short" }).format(date);
      const thumbPoster = (() => {
        const code = resolveCompetitionCode({ competition: log.competition });
        return code && competitionPosters[code]
          ? competitionPosters[code]
          : placeholderPosters[hashString(log.matchId) % placeholderPosters.length];
      })();
      return `
        <div class="diary-row">
          <div class="date"><div class="d">${safe(day)}</div><div class="m">${safe(month)}</div></div>
          <div class="thumb-wrap"><img src="${safe(thumbPoster)}" alt=""></div>
          <div><div class="dteams">${safe(log.matchTitle)}</div><div class="dmeta">${safe(log.competition || "Competition")}${log.seenInStadium ? ` · <span class="badge stadium">Stadium</span>` : " · Watched"}</div>${log.reviewText ? `<p class="muted" style="margin-top:6px;">${safe(log.reviewText)}</p>` : ""}</div>
          ${log.rating ? `<span class="rating sm" data-rating="${safe(log.rating)}"></span>` : `<span class="muted">Logged</span>`}
        </div>`;
    }).join("");
    profile.innerHTML = `
      <div class="profile-banner reveal in"><img src="${safe(user.bannerUrl)}" alt="${safe(user.displayName)} profile banner"></div>
      <div class="profile-head reveal in">
        ${avatarMarkup(user, "pfp crest")}
        <div class="pinfo">
          <h1>${safe(user.displayName)}</h1>
          <div class="handle">@${safe(user.username)} · ${safe(user.email)}</div>
          <div class="profile-stats">
            <div class="ps"><div class="n">${safe(diary.length)}</div><div class="l">Matches</div></div>
            <div class="ps"><div class="n">${safe(rated.length)}</div><div class="l">Reviews</div></div>
            <div class="ps"><div class="n">${safe(stadiumCount)}</div><div class="l">In stadium</div></div>
            <div class="ps"><div class="n">${safe(average)}</div><div class="l">Avg rating</div></div>
          </div>
        </div>
        <div class="row center" style="gap:10px;"><button class="btn btn-ghost" type="button" data-edit-profile>Edit profile</button><a href="/archive.html" class="btn btn-primary">Log a match</a></div>
      </div>
      <p class="soft" style="max-width:70ch;margin-top:18px;">${safe(user.bio || "Building a lifelong football diary of matches, reviews, stadium nights, and classics to revisit.")}</p>

      <div class="detail-layout" style="margin-top:30px;">
        <div>
          <section>
            <div class="section-head"><h2>Top 4 matches</h2></div>
            <div class="top4">
              ${Array.from({ length: 4 }, (_, index) => top4Slot(topLogs[index], index, topDetails[index])).join("")}
            </div>
          </section>
          <section class="section">
            <div class="section-head"><h2>Your diary</h2><a class="link" href="/archive.html">Log another match</a></div>
            <div class="card card-pad"><div class="diary">${diary.length ? diaryRows : `<div class="center-text"><h3>No diary entries yet</h3><p class="muted">Use the archive to log your first match.</p><a class="btn btn-primary" href="/archive.html" style="margin-top:14px;">Browse archive</a></div>`}</div></div>
          </section>
          <section class="section">
            <div class="section-head"><h2>Your reviews</h2><span class="chip">${reviews.length}</span></div>
            <div style="display:flex;flex-direction:column;gap:14px;">${reviews.length ? reviews.map(log => `<article class="card review-card"><div class="head"><div class="info"><div class="action"><b>${safe(log.matchTitle)}</b></div><div class="muted">${safe(shortDate(log.watchedAt))}</div></div>${log.rating ? `<span class="rating" data-rating="${safe(log.rating)}" style="margin-left:auto"></span>` : ""}</div><div class="review-body"><p>${safe(log.reviewText)}</p></div><div class="review-actions"><a href="/matches.html?id=${safe(log.matchId)}&editLog=${safe(log.id)}">Edit</a><button type="button" data-profile-delete-log="${safe(log.id)}">Delete</button></div></article>`).join("") : `<div class="card card-pad"><p class="muted">No reviews yet.</p></div>`}</div>
          </section>
          <section class="section">
            <div class="section-head"><h2>Saved matches</h2><span class="chip">${favorites.length}</span></div>
            <div class="grid grid-3">${favorites.length ? favorites.map(savedMatchCard).join("") : `<div class="card card-pad" style="grid-column:1/-1"><p class="muted">No saved matches yet.</p></div>`}</div>
          </section>
        </div>
        <aside class="side">
          <div class="card widget reveal"><h3>Diary summary</h3><div class="stat-grid" style="grid-template-columns:1fr 1fr;gap:10px;"><div class="stat-box"><div class="sn">${safe(diary.length)}</div><div class="sl">Matches</div></div><div class="stat-box"><div class="sn">${safe(stadiumCount)}</div><div class="sl">Stadiums</div></div><div class="stat-box"><div class="sn">${safe(average)}</div><div class="sl">Average</div></div><div class="stat-box"><div class="sn">${safe(rated.length)}</div><div class="sl">Rated</div></div></div></div>
          <div class="card widget reveal"><h3>Favourite team</h3><p class="muted">${safe(user.favoriteTeam?.name || "Not selected yet")}</p></div>
        </aside>
      </div>
    `;
    if (typeof FootyBoxUI !== "undefined") {
      FootyBoxUI.renderRatings();
      FootyBoxUI.injectIcons();
    }
    profile.hidden = false;
    setupProfileEditor(user);
  }

  function setupProfileEditor(user) {
    const dialog = document.querySelector("[data-profile-dialog]");
    const form = document.querySelector("[data-profile-form]");
    if (!dialog || !form) return;
    const avatarKeys = Object.keys(avatarPaths);
    form.querySelector("[data-avatar-picker]").innerHTML = `<button type="button" class="avatar-choice" data-avatar-key="">Initials</button>` + avatarKeys.map(key => `<button type="button" class="avatar-choice" data-avatar-key="${key}"><img src="${avatarPaths[key]}" alt="${key} avatar"></button>`).join("");
    const selectAvatar = key => {
      form.elements.avatarKey.value = key || "";
      form.querySelectorAll("[data-avatar-key]").forEach(button => button.classList.toggle("selected", button.dataset.avatarKey === (key || "")));
    };
    document.querySelector("[data-edit-profile]")?.addEventListener("click", () => {
      form.elements.displayName.value = user.displayName || "";
      form.elements.bio.value = user.bio || "";
      form.elements.favoriteTeamId.value = user.favoriteTeam?.id || "";
      form.querySelector("[data-team-search]").value = user.favoriteTeam?.name || "";
      selectAvatar(user.avatarKey);
      form.elements.avatarFile.value = "";
      form.elements.bannerFile.value = "";
      form.querySelector("[data-avatar-preview]").src = user.avatarUrl;
      form.querySelector("[data-banner-preview]").src = user.bannerUrl;
      dialog.showModal();
    });
    form.querySelectorAll("[data-avatar-key]").forEach(button => button.addEventListener("click", () => selectAvatar(button.dataset.avatarKey)));
    form.querySelector("[data-team-search]").oninput = async event => {
      const results = form.querySelector("[data-team-results]");
      const query = event.target.value.trim();
      if (query.length < 2) { results.innerHTML = ""; return; }
      const items = (await api(`/api/search?q=${encodeURIComponent(query)}`)).filter(item => item.type === "team");
      results.innerHTML = items.map(item => `<button type="button" data-team-id="${safe(item.id)}" data-team-name="${safe(item.title)}">${safe(item.title)}</button>`).join("");
    };
    form.querySelector("[data-team-results]").onclick = event => {
      const button = event.target.closest("[data-team-id]");
      if (!button) return;
      form.elements.favoriteTeamId.value = button.dataset.teamId;
      form.querySelector("[data-team-search]").value = button.dataset.teamName;
      form.querySelector("[data-team-results]").innerHTML = "";
    };
    form.querySelector("[data-clear-team]").onclick = () => { form.elements.favoriteTeamId.value = ""; form.querySelector("[data-team-search]").value = ""; };
    document.querySelector("[data-close-profile]").onclick = () => dialog.close();
    for (const kind of ["avatar", "banner"]) form.elements[`${kind}File`].onchange = event => {
      const file = event.target.files[0];
      if (file) form.querySelector(`[data-${kind}-preview]`).src = URL.createObjectURL(file);
    };
    form.querySelector("[data-remove-avatar]").onclick = async () => { await api("/api/users/me/avatar", { method: "DELETE" }); dialog.close(); await loadProfile(); };
    form.querySelector("[data-remove-banner]").onclick = async () => { await api("/api/users/me/banner", { method: "DELETE" }); dialog.close(); await loadProfile(); };
    form.onsubmit = async event => {
      event.preventDefault();
      try {
        await api("/api/users/me/profile", { method: "PUT", body: JSON.stringify({ displayName: form.elements.displayName.value, bio: form.elements.bio.value, favoriteTeamId: form.elements.favoriteTeamId.value ? Number(form.elements.favoriteTeamId.value) : null, avatarKey: form.elements.avatarKey.value || null }) });
        for (const kind of ["avatar", "banner"]) {
          const file = form.elements[`${kind}File`].files[0];
          if (file) {
            const body = new FormData();
            body.append("file", file);
            await api(`/api/users/me/${kind}`, { method: "POST", body });
          }
        }
        dialog.close();
        await loadProfile();
      } catch (error) { form.querySelector("[data-profile-message]").textContent = error.message; }
    };
    document.querySelectorAll("[data-profile-delete-log]").forEach(button => button.addEventListener("click", async () => {
      if (!window.confirm("Delete this diary entry and review?")) return;
      await api(`/api/diary/logs/${button.dataset.profileDeleteLog}`, { method: "DELETE" });
      await loadProfile();
    }));
  }

  function showError(error) {
    const target = document.querySelector("[data-page-message]");
    if (target) {
      target.textContent = friendlyArchiveError(error);
      target.classList.add("error");
      target.classList.remove("hidden");
    }
  }

  function init() {
    authNav();
    bindLoginForms();
    bindMatchActions();
    const urlParams = new URLSearchParams(window.location.search);
    const decadeSelect = document.querySelector("[data-archive-decade]");
    if (decadeSelect && urlParams.get("decade")) decadeSelect.value = urlParams.get("decade");
    const hasMatchId = urlParams.has("id");
    if (hasMatchId) {
      loadMatchDetail().catch(showError);
    } else if (document.querySelector("[data-competition-list]")) {
      loadCompetitions().then(loadMatches).catch(showError);
    } else {
      loadMatches().catch(showError);
    }
    loadProfile().catch(showError);
    loadDiscover().catch(showError);
  }

  return { init, logout };
})();

document.addEventListener("DOMContentLoaded", FootyBox.init);
