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
 * Group catalog names by feature.
 *
 * A catalog name is a feature, optionally followed by the module that registered it: TicketRegistry.redis
 * is the redis module of the TicketRegistry feature, and Reports is a feature registered on its own.
 *
 * @param features the catalog names, without the CasFeatureModule prefix
 * @returns {Array<{name: string, title: string, modules: string[]}>} one entry per feature, sorted by title
 */
function groupCasFeatures(features) {
    const byName = new Map();
    for (const entry of features) {
        const separator = entry.indexOf(".");
        const name = separator < 0 ? entry : entry.substring(0, separator);
        const module = separator < 0 ? null : entry.substring(separator + 1);
        if (!byName.has(name)) {
            byName.set(name, new Set());
        }
        if (module) {
            byName.get(name).add(module);
        }
    }
    return [...byName.entries()]
        .map(([name, modules]) => ({
            name,
            title: humanizeCasFeature(name),
            modules: [...modules].sort((first, second) => first.localeCompare(second))
        }))
        .sort((first, second) => first.title.localeCompare(second.title));
}

/**
 * Feature names whose words the casing alone does not tell apart.
 */
const CAS_FEATURE_TITLES = Object.freeze({
    CasConfiguration: "CAS Configuration",
    GeoLocation: "Geolocation",
    OAuth: "OAuth",
    OpenIDConnect: "OpenID Connect",
    PasswordlessAuthn: "Passwordless Authentication",
    Radius: "RADIUS",
    RadiusMFA: "RADIUS MFA",
    RestProtocol: "REST Protocol",
    WebAuthn: "WebAuthn",
    WsFederation: "WS-Federation",
    WsFederationIdentityProvider: "WS-Federation Identity Provider",
    X509: "X.509",
    YubiKey: "YubiKey"
});

/**
 * Turn a feature name into words: SAMLIdentityProvider reads SAML Identity Provider.
 *
 * @param name the feature name
 * @returns {string} the name with spaces between its words
 */
function humanizeCasFeature(name) {
    return CAS_FEATURE_TITLES[name] ?? name
        .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .replace(/([A-Z]+)([A-Z][a-z])/g, "$1 $2");
}

/**
 * Escape text for HTML, marking the first occurrence of the search query.
 *
 * @param text the text to show
 * @param query the lower-cased search query, possibly empty
 * @returns {string} safe markup
 */
function markCasFeatureMatch(text, query) {
    const escape = value => String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    }[character]));
    const index = query ? text.toLowerCase().indexOf(query) : -1;
    if (index < 0) {
        return escape(text);
    }
    const before = escape(text.substring(0, index));
    const match = escape(text.substring(index, index + query.length));
    const after = escape(text.substring(index + query.length));
    return `${before}<mark>${match}</mark>${after}`;
}

/**
 * Draw the feature catalog as cards, one per feature with its modules, keeping those that match the search.
 *
 * A feature matches when its name, its words or one of its modules contains the query; a feature that
 * matches by name shows all its modules, one that matches by module shows only the matching ones.
 *
 * @param features the catalog names, without the CasFeatureModule prefix
 */
function renderCasFeatureCatalog(features) {
    const catalog = $("#casFeaturesCatalog");
    if (catalog.length === 0) {
        return;
    }
    const query = String($("#casFeaturesSearch").val() || "").trim().toLowerCase();
    const groups = groupCasFeatures(features);
    const moduleCount = groups.reduce((total, group) => total + group.modules.length, 0);
    const shown = groups
        .map(group => {
            const byName = !query || group.name.toLowerCase().includes(query) || group.title.toLowerCase().includes(query);
            const modules = byName ? group.modules : group.modules.filter(module => module.toLowerCase().includes(query));
            return byName || modules.length > 0 ? {...group, modules, byName, total: group.modules.length} : null;
        })
        .filter(Boolean);

    catalog.html(shown.map(group => {
        const property = module => `CasFeatureModule.${group.name}${module ? `.${module}` : ""}.enabled`;
        const modules = group.modules.length === 0
            ? "<span class=\"palantir-feature-card__core\">Registered on its own, without modules</span>"
            : group.modules.map(module => `
                <li class="palantir-tag" title="${property(module)}">${markCasFeatureMatch(module, query)}</li>`).join("");
        return `
            <article class="palantir-feature-card" role="listitem" title="${property(null)}">
                <header class="palantir-feature-card__head">
                    <span class="palantir-feature-card__initial" aria-hidden="true">${group.title.charAt(0)}</span>
                    <span class="palantir-feature-card__name">
                        <span>${group.byName ? markCasFeatureMatch(group.title, query) : markCasFeatureMatch(group.title, "")}</span>
                        <code>${group.byName ? markCasFeatureMatch(group.name, query) : markCasFeatureMatch(group.name, "")}</code>
                    </span>
                    ${group.total > 0 ? `<span class="palantir-feature-card__count">${group.total} ${group.total === 1 ? "module" : "modules"}</span>` : ""}
                </header>
                ${group.modules.length === 0 ? modules : `<ul class="palantir-feature-card__modules" aria-label="Modules">${modules}</ul>`}
            </article>`;
    }).join(""));

    const featureWord = count => `${count} ${count === 1 ? "feature" : "features"}`;
    $("#casFeaturesSummary").text(query
        ? `${shown.length} of ${featureWord(groups.length)} match "${query}"`
        : `${featureWord(groups.length)} with ${moduleCount} ${moduleCount === 1 ? "module" : "modules"}`);
    $("#casFeaturesEmpty").toggleClass("hide", shown.length > 0 || groups.length === 0);
}

/**
 * Read the feature catalog and draw it, searchable by feature and module.
 *
 * @returns {Promise<string[]>} the enabled CAS feature names
 */
async function initializeCasFeatures() {
    const features = await fetchCasFeatures();
    renderCasFeatureCatalog(features);
    $("#casFeaturesSearch").off("input.casFeatures").on("input.casFeatures", () => renderCasFeatureCatalog(CAS_FEATURES));
    return CAS_FEATURES;
}
