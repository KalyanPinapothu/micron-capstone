(function (document, Granite, $) {
    "use strict";
    const REQUIRED_PROPERTIES = [
        "pversion",
        "audience",
        "complianceRegion"
    ];

    function addBadge(card) {
        if (card.find(".micron-validation-badge").length) {
            return;
        }
        card.append(
            '<coral-badge class="micron-validation-badge">Metadata Missing</coral-badge>'
        );
    }

    function removeBadge(card) {
        card.find(".micron-validation-badge").remove();
    }

    function validate(card, metadata) {
        if (!metadata.validationStatus) {
            removeBadge(card);
            return;
        }
        const missing = REQUIRED_PROPERTIES.some(function (prop) {
            const value = metadata[prop];
            return value === undefined ||
                   value === null ||
                   value.toString().trim() === "";
        });
        if (missing) {
            addBadge(card);
        } else {
            removeBadge(card);
        }
    }

    function refreshBadges() {
        $(".foundation-collection-item").each(function () {
            const card = $(this);
            const assetPath = card.data("foundationCollectionItemId");
            if (!assetPath) {
                return;
            }
            $.getJSON(assetPath + "/jcr:content/metadata.json")
                .done(function (metadata) {
                    validate(card, metadata);
                })
                .fail(function () {
                    removeBadge(card);
                });
        });
    }

    $(document).on("foundation-contentloaded", refreshBadges);
})(document, Granite, Granite.$);