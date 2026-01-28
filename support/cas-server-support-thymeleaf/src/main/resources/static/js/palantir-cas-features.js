const CAS_FEATURES = [];

async function fetchCasFeatures() {
    return new Promise((resolve, reject) => {
        if (!CasActuatorEndpoints.casFeatures()) {
            console.error("CAS Features endpoint is not available.");
            resolve(CAS_FEATURES);
        } else if (CAS_FEATURES.length === 0) {
            $.get(CasActuatorEndpoints.casFeatures(), response => {
                for (const element of response) {
                    const featureName = element.trim().replace("CasFeatureModule.", "");
                    CAS_FEATURES.push(featureName);
                }
                resolve(CAS_FEATURES);
            });
        } else {
            resolve(CAS_FEATURES);
        }
    });
}

async function initializeCasFeatures() {
    return new Promise(async (resolve, reject) => {
        const features = await fetchCasFeatures();
        $("#casFeaturesChipset").empty();
        for (const featureName of features) {
            let feature = `
                            <div class="mdc-chip" role="row">
                                <div class="mdc-chip__ripple"></div>
                                <span role="gridcell">
                                  <span class="mdc-chip__text">${featureName}</span>
                                </span>
                            </div>
                        `.trim();
            $("#casFeaturesChipset").append($(feature));
        }
        resolve(CAS_FEATURES);
    });
}
