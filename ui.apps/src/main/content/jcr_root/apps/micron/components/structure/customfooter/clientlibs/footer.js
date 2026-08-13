(function () {
    "use strict";

    function initialiseFooter(footer) {
        var toggles = footer.querySelectorAll(".micron-footer__section-toggle");

        toggles.forEach(function (toggle) {
            toggle.addEventListener("click", function () {
                var expanded = toggle.getAttribute("aria-expanded") === "true";
                toggle.setAttribute("aria-expanded", String(!expanded));
            });
        });
    }

    function initialiseAll() {
        document.querySelectorAll('[data-cmp-is="micron-footer"]').forEach(initialiseFooter);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initialiseAll);
    } else {
        initialiseAll();
    }
}());
