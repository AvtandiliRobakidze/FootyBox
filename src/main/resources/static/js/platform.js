(() => {
  function updateSpoilerCards(enabled) {
    document.querySelectorAll("[data-spoiler-score]").forEach(score => {
      score.textContent = enabled ? score.dataset.hiddenLabel || "Score hidden" : score.dataset.revealedLabel || "Open archive to view result";
      score.classList.toggle("is-revealed", !enabled);
    });
  }

  function bindSpoilerToggle() {
    const toggle = document.querySelector("[data-platform-spoiler]");
    if (!toggle) return;
    updateSpoilerCards(toggle.checked);
    toggle.addEventListener("change", () => updateSpoilerCards(toggle.checked));
  }

  function bindScaffoldActions() {
    document.querySelectorAll("[data-scaffold-action]").forEach(button => {
      button.classList.add("is-pending");
      button.addEventListener("click", () => {
        button.textContent = button.dataset.pendingLabel || "Backend needed";
        button.setAttribute("aria-live", "polite");
        button.disabled = true;
      });
    });

    document.querySelectorAll("[data-platform-filter]").forEach(button => {
      button.addEventListener("click", () => {
        document.querySelectorAll("[data-platform-filter]").forEach(item => item.classList.remove("primary"));
        button.classList.add("primary");
      });
    });
  }

  function init() {
    bindSpoilerToggle();
    bindScaffoldActions();
  }

  document.addEventListener("DOMContentLoaded", init);
})();
