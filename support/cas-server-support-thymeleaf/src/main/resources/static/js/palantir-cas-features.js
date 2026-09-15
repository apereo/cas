const CAS_FEATURES = [];

let casFeaturesRequest = null;

/**
 * Read the feature catalog, once.
 *
 * The dashboard cannot decide which tabs to show until this resolves, so a failed request resolves
 * with an empty catalog rather than leaving the promise pending and the dashboard hidden. Repeat
 * callers share the first request.
 *
 * @returns {Promise<string[]>} the enabled CAS feature names
 */
async function fetchCasFeatures() {
    if (!CasActuatorEndpoints.casFeatures() || CAS_FEATURES.length > 0) {
        return CAS_FEATURES;
    }
    if (!casFeaturesRequest) {
        casFeaturesRequest = new Promise(resolve => {
            $.get(CasActuatorEndpoints.casFeatures(), response => {
                for (const element of response) {
                    CAS_FEATURES.push(element.trim().replace("CasFeatureModule.", ""));
                }
                resolve(CAS_FEATURES);
            }).fail((xhr, status, error) => {
                console.error("Unable to fetch the CAS feature catalog:", error);
                resolve(CAS_FEATURES);
            });
        });
    }
    return casFeaturesRequest;
}

/**
 * Read the feature catalog and render it as a chip set.
 *
 * @returns {Promise<string[]>} the enabled CAS feature names
 */
async function initializeCasFeatures() {
    const features = await fetchCasFeatures();
    const chipset = $("#casFeaturesChipset");
    if (chipset.length > 0) {
        const chips = features.map(featureName => `
            <div class="mdc-chip" role="row">
                <div class="mdc-chip__ripple"></div>
                <span role="gridcell">
                  <span class="mdc-chip__text">${featureName}</span>
                </span>
            </div>`.trim());
        chipset.empty().append(chips.join(""));
    }
    return CAS_FEATURES;
}
