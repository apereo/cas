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
 * is the redis module of the TicketRegistry feature, and Reports is a feature registered on its own. Features
 * and modules that are not running but are switched by a property, such as those turned off from the catalog,
 * are listed too, so they can be turned on again.
 *
 * @param features the catalog names, without the CasFeatureModule prefix
 * @returns {Array<{name: string, title: string, core: boolean, modules: string[]}>} one entry per feature, sorted by title
 */
function groupCasFeatures(features) {
    const byName = new Map();
    const add = entry => {
        const separator = entry.indexOf(".");
        const name = separator < 0 ? entry : entry.substring(0, separator);
        const module = separator < 0 ? null : entry.substring(separator + 1);
        if (!byName.has(name)) {
            byName.set(name, {core: false, modules: new Set()});
        }
        if (module) {
            byName.get(name).modules.add(module);
        } else {
            byName.get(name).core = true;
        }
    };
    features.forEach(add);
    CasFeatureToggles.switched().forEach(add);
    return [...byName.entries()]
        .map(([name, group]) => ({
            name,
            title: humanizeCasFeature(name),
            core: group.core,
            modules: [...group.modules].sort((first, second) => first.localeCompare(second))
        }))
        .sort((first, second) => first.title.localeCompare(second.title));
}

/**
 * Turn CAS features and their modules on or off by storing their feature flags in a dynamic configuration source.
 * CAS decides which features run when it starts, so a change takes effect once CAS restarts; until then the
 * catalog shows what the stored flags will do.
 */
class CasFeatureToggles {
    static PATTERN = /^CasFeatureModule\.(.+)\.enabled$/;

    static flags = new Map();

    static loaded = false;

    /**
     * The property that switches a feature, or one of its modules.
     *
     * @param feature the feature name
     * @param module the module, or null for the feature itself
     * @returns {string} the property name
     */
    static property(feature, module = null) {
        return `CasFeatureModule.${feature}${module ? `.${module}` : ""}.enabled`;
    }

    /**
     * Whether features can be switched from the catalog: dynamic configuration sources are available and the
     * configuration endpoints answer.
     *
     * @returns {boolean} true when the catalog offers to switch features
     */
    static available() {
        return PalantirDashboardConfiguration.mutablePropertySourcesAvailable()
            && PalantirDashboardConfiguration.mutablePropertySources().length > 0
            && Boolean(CasActuatorEndpoints.casConfig())
            && Boolean(CasActuatorEndpoints.env());
    }

    /**
     * Read the feature flags CAS sees, the value of each taken from the source that wins, and remember which
     * dynamic source holds each flag.
     *
     * @returns {Promise<void>} resolved once the flags are read, or could not be
     */
    static async load() {
        if (!CasFeatureToggles.available()) {
            return;
        }
        try {
            const environment = await $.get(CasActuatorEndpoints.env());
            const flags = new Map();
            for (const source of environment?.propertySources ?? []) {
                const sourceName = configPropertySourceName(String(source.name ?? ""));
                const mutable = PalantirDashboardConfiguration.mutablePropertySources().find(entry => sourceName.endsWith(entry)) ?? null;
                for (const [name, entry] of Object.entries(source.properties ?? {})) {
                    const match = name.match(CasFeatureToggles.PATTERN);
                    if (!match) {
                        continue;
                    }
                    const flag = flags.get(name) ?? {feature: match[1], value: null, source: null};
                    if (flag.value === null) {
                        flag.value = String(entry?.value ?? "").trim().toLowerCase();
                    }
                    if (flag.source === null && mutable !== null) {
                        flag.source = mutable;
                    }
                    flags.set(name, flag);
                }
            }
            CasFeatureToggles.flags = flags;
            CasFeatureToggles.loaded = true;
        } catch (xhr) {
            console.warn("Palantir could not read the feature flags", xhr);
        }
    }

    /**
     * Catalog names of features and modules whose flags are set, running or not.
     *
     * @returns {string[]} the catalog names
     */
    static switched() {
        return [...CasFeatureToggles.flags.values()].filter(flag => flag.value === "false" || flag.value === "true").map(flag => flag.feature);
    }

    /**
     * What a feature or module does now and will do after a restart.
     *
     * @param entry the catalog name, without the CasFeatureModule prefix
     * @returns {{running: boolean, value: string|null, state: string}} running now, the stored flag, and one of
     *     on, off, turning-off or turning-on
     */
    static state(entry) {
        const running = CAS_FEATURES.includes(entry);
        const value = CasFeatureToggles.flags.get(`CasFeatureModule.${entry}.enabled`)?.value ?? null;
        if (running) {
            return {running, value, state: value === "false" ? "turning-off" : "on"};
        }
        return {running, value, state: value === "true" ? "turning-on" : "off"};
    }

    /**
     * The flags a feature or a module is switched by: a module's own flag; for a feature with modules, the flag of
     * each of its modules and never the feature's own, which also governs what the feature does without them; and
     * for a feature without modules, its own flag.
     *
     * @param group the feature as the catalog groups it
     * @param module the module, or null for the whole feature
     * @returns {string[]} the property names
     */
    static properties(group, module) {
        if (module) {
            return [CasFeatureToggles.property(group.name, module)];
        }
        if (group.modules.length > 0) {
            return group.modules.map(name => CasFeatureToggles.property(group.name, name));
        }
        return [CasFeatureToggles.property(group.name)];
    }

    /**
     * How a change is named: the feature, or the feature with the modules it applies to.
     *
     * @param group the feature as the catalog groups it
     * @param module the module, or null for the whole feature
     * @returns {string} the subject, such as "Service Registry with mongo"
     */
    static subject(group, module) {
        const modules = module ? [module] : group.modules;
        if (modules.length === 0) {
            return group.title;
        }
        if (modules.length <= 3) {
            const names = modules.length === 1 ? modules[0] : `${modules.slice(0, -1).join(", ")} and ${modules.at(-1)}`;
            return `${group.title} with ${names}`;
        }
        return `${group.title} with its ${modules.length} modules`;
    }

    /**
     * Store the flags that turn a feature or one of its modules on or off, in the dynamic source the operator
     * chooses when there is more than one.
     *
     * @param group the feature as the catalog groups it
     * @param module the module, or null for the whole feature
     * @param enable whether to turn it on
     * @returns {Promise<void>} resolved once the flags are stored or the operator declined
     */
    static async change(group, module, enable) {
        const properties = CasFeatureToggles.properties(group, module);
        const sources = PalantirDashboardConfiguration.mutablePropertySources();
        const holder = properties.map(name => CasFeatureToggles.flags.get(name)?.source).find(Boolean) ?? sources[0];
        const subject = CasFeatureToggles.subject(group, module);
        const value = enable ? "true" : "false";
        const result = await Swal.fire({
            titleText: `${enable ? "Turn On" : "Turn Off"} ${subject}?`,
            html: `
                <p class="palantir-feature-toggle__note">${sources.length > 1 ? "Choose the configuration source to store" : "Palantir stores"}
                ${properties.length === 1 ? "this feature flag" : "these feature flags"}${sources.length > 1 ? " in" : ` in <strong>${escapePalantirMarkup(sources[0])}</strong>`}:</p>
                <pre class="palantir-feature-toggle__flags"><code>${properties.map(name => escapePalantirMarkup(`${name}=${value}`)).join("\n")}</code></pre>
                <p class="palantir-feature-toggle__note">CAS decides which features run when it starts, so the change takes effect once CAS restarts.</p>`,
            icon: "question",
            input: sources.length > 1 ? "select" : undefined,
            inputOptions: sources.length > 1 ? Object.fromEntries(sources.map(source => [source, source])) : undefined,
            inputValue: sources.length > 1 ? holder : undefined,
            showCancelButton: true,
            confirmButtonText: enable ? "Turn on" : "Turn off",
            cancelButtonText: "Cancel"
        });
        if (!result.isConfirmed) {
            return;
        }
        const source = sources.length > 1 ? String(result.value) : sources[0];
        try {
            await $.ajax({
                url: `${CasActuatorEndpoints.casConfig()}/update`,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(properties.map(name => ({name: name, value: value, propertySource: source})))
            });
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
            return;
        }
        await CasFeatureToggles.load();
        renderCasFeatureCatalog(CAS_FEATURES);
        if ($.fn.dataTable.isDataTable("#configurationTable") && typeof reloadConfigurationTable === "function") {
            $.get(CasActuatorEndpoints.env(), response => reloadConfigurationTable(response));
        }
        PalantirDialogs.done(`${subject.charAt(0).toUpperCase()}${subject.slice(1)} Is Turned ${enable ? "On" : "Off"} in ${source}`, "Restart CAS for the change to take effect.");
    }

    /**
     * Whether the feature itself, apart from its modules, will run after a restart: its stored flag decides when there
     * is one, and otherwise whether it runs now. A feature that only runs through its modules has nothing of its own
     * that is off, so it counts as on.
     *
     * @param group the feature as the catalog groups it
     * @returns {boolean} true when the feature itself is on
     */
    static featureOn(group) {
        const value = CasFeatureToggles.flags.get(CasFeatureToggles.property(group.name))?.value ?? null;
        if (value === "false" || value === "true") {
            return value === "true";
        }
        return !group.core || CAS_FEATURES.includes(group.name);
    }

    /**
     * Turn a feature and any of its modules on or off in one go: a switch for the feature itself and one for each
     * module, each starting at what it will do after a restart. Switching the feature itself off turns off every module
     * with it, since CAS reads the flag of each module on its own. Only the flags that change are stored.
     *
     * @param group the feature as the catalog groups it
     * @returns {Promise<void>} resolved once the flags are stored or the operator closed the dialog
     */
    static async manage(group) {
        const sources = PalantirDashboardConfiguration.mutablePropertySources();
        const initial = {
            feature: CasFeatureToggles.featureOn(group),
            modules: new Map(group.modules.map(module => {
                const state = CasFeatureToggles.state(`${group.name}.${module}`).state;
                return [module, state === "on" || state === "turning-on"];
            }))
        };
        const holder = [CasFeatureToggles.property(group.name), ...group.modules.map(module => CasFeatureToggles.property(group.name, module))]
            .map(name => CasFeatureToggles.flags.get(name)?.source).find(Boolean) ?? sources[0];
        const switchMarkup = (id, on, title, detail, extra = "") => `
            <label class="palantir-feature-switch${extra}" for="${id}">
                <input type="checkbox" role="switch" id="${id}" ${on ? "checked" : ""}>
                <span class="palantir-feature-switch__track" aria-hidden="true"></span>
                <span class="palantir-feature-switch__text">
                    <strong>${escapePalantirMarkup(title)}</strong>
                    <small>${escapePalantirMarkup(detail)}</small>
                </span>
            </label>`;
        const moduleId = index => `casFeatureModuleSwitch${index}`;
        const desired = () => {
            const feature = $("#casFeatureItselfSwitch").prop("checked") === true;
            return {
                feature,
                modules: new Map(group.modules.map((module, index) => [module, feature
                    ? $(`#${moduleId(index)}`).prop("checked") === true
                    : initial.feature ? false : initial.modules.get(module)]))
            };
        };
        const changes = () => {
            const wanted = desired();
            const flags = [];
            if (wanted.feature !== initial.feature) {
                flags.push({name: CasFeatureToggles.property(group.name), value: String(wanted.feature)});
            }
            group.modules.forEach(module => {
                if (wanted.modules.get(module) !== initial.modules.get(module)) {
                    flags.push({name: CasFeatureToggles.property(group.name, module), value: String(wanted.modules.get(module))});
                }
            });
            return {wanted, flags};
        };
        const holdModules = featureOn => group.modules.forEach((module, index) => {
            const input = $(`#${moduleId(index)}`);
            if (featureOn) {
                input.prop("checked", input.data("choice") ?? initial.modules.get(module));
            } else {
                input.data("choice", input.prop("checked")).prop("checked", false);
            }
        });
        const list = names => names.length === 1 ? names[0] : `${names.slice(0, -1).join(", ")} and ${names.at(-1)}`;
        const describe = () => {
            const {wanted, flags} = changes();
            const off = group.modules.filter(module => initial.modules.get(module) && !wanted.modules.get(module));
            const on = group.modules.filter(module => !initial.modules.get(module) && wanted.modules.get(module));
            const kept = group.modules.filter(module => initial.modules.get(module) && wanted.modules.get(module));
            const parts = [];
            if (initial.feature && !wanted.feature) {
                parts.push(`<strong>${escapePalantirMarkup(group.title)}</strong> turns off, with every module`);
            } else if (!initial.feature && wanted.feature) {
                parts.push(`<strong>${escapePalantirMarkup(group.title)}</strong> turns on`);
            }
            if (wanted.feature) {
                if (off.length > 0) {
                    parts.push(`${list(off.map(name => `<strong>${escapePalantirMarkup(name)}</strong>`))} turn${off.length === 1 ? "s" : ""} off`);
                }
                if (on.length > 0) {
                    parts.push(`${list(on.map(name => `<strong>${escapePalantirMarkup(name)}</strong>`))} turn${on.length === 1 ? "s" : ""} on`);
                }
                if (kept.length > 0 && (off.length > 0 || on.length > 0)) {
                    parts.push(`${list(kept.map(escapePalantirMarkup))} keep${kept.length === 1 ? "s" : ""} running`);
                }
            }
            $("#casFeatureManageSummary").html(flags.length === 0
                ? "Nothing changes yet: switch the feature or its modules to choose what runs after a restart."
                : `After a restart: ${parts.join("; ")}.`).toggleClass("has-changes", flags.length > 0);
            $("#casFeatureManageFlagCount").text(`${flags.length} feature flag${flags.length === 1 ? "" : "s"}`);
            $("#casFeatureManageFlags").text(flags.map(flag => `${flag.name}=${flag.value}`).join("\n"));
            $("#casFeatureManageFlagsPanel").prop("hidden", flags.length === 0);
            group.modules.forEach((module, index) => {
                const input = $(`#${moduleId(index)}`);
                input.prop("disabled", !wanted.feature);
                const now = wanted.modules.get(module);
                input.closest(".palantir-feature-switch")
                    .toggleClass("is-off", !input.prop("checked"))
                    .toggleClass("is-changed", now !== initial.modules.get(module))
                    .find("small")
                    .text(!wanted.feature ? (initial.feature ? "off with the feature" : "left as it is") : now === initial.modules.get(module)
                        ? (now ? "keeps running" : "stays off")
                        : (now ? "turns on after a restart" : "turns off after a restart"));
            });
            $("#casFeatureItselfSwitch").closest(".palantir-feature-switch")
                .toggleClass("is-off", !wanted.feature)
                .toggleClass("is-changed", wanted.feature !== initial.feature)
                .find("small")
                .text(wanted.feature
                    ? `Switch off to turn off ${group.title} and all of its modules`
                    : `${group.title} and all of its modules are off after a restart; switch on to choose its modules again`);
            const confirm = Swal.getConfirmButton();
            if (confirm) {
                confirm.disabled = flags.length === 0;
            }
        };
        const result = await Swal.fire({
            titleText: group.title,
            width: Math.min(640, $(window).width() - 32),
            html: `
                <div class="palantir-feature-manage">
                    ${switchMarkup("casFeatureItselfSwitch", initial.feature, "The feature itself",
        `Switch off to turn off ${group.title} and all of its modules`, " is-feature")}
                    <div class="palantir-feature-manage__modules" role="group" aria-label="Modules of ${escapePalantirMarkup(group.title)}">
                        ${group.modules.map((module, index) => switchMarkup(moduleId(index), initial.modules.get(module), module, "")).join("")}
                    </div>
                    <p class="palantir-feature-manage__summary" id="casFeatureManageSummary" aria-live="polite"></p>
                    ${sources.length > 1 ? `
                        <label class="palantir-feature-manage__source" for="casFeatureManageSource">Store the flags in
                            <select id="casFeatureManageSource">${sources.map(source => `<option value="${escapePalantirMarkup(source)}" ${source === holder ? "selected" : ""}>${escapePalantirMarkup(source)}</option>`).join("")}</select>
                        </label>` : ""}
                    <details class="palantir-feature-manage__flags" id="casFeatureManageFlagsPanel" hidden>
                        <summary><span id="casFeatureManageFlagCount"></span>${sources.length > 1 ? "" : `, stored in ${escapePalantirMarkup(sources[0])}`}</summary>
                        <pre class="palantir-feature-toggle__flags"><code id="casFeatureManageFlags"></code></pre>
                    </details>
                    <p class="palantir-feature-toggle__note">CAS decides which features run when it starts, so the change takes effect once CAS restarts.</p>
                </div>`,
            showCancelButton: true,
            confirmButtonText: "Save Changes",
            cancelButtonText: "Cancel",
            focusConfirm: false,
            didOpen: popup => {
                if (!initial.feature) {
                    holdModules(false);
                }
                $(popup).on("change", ".palantir-feature-manage input[role=switch]", event => {
                    if (event.currentTarget.id === "casFeatureItselfSwitch") {
                        holdModules(event.currentTarget.checked);
                    }
                    describe();
                });
                describe();
            },
            preConfirm: () => ({flags: changes().flags, source: sources.length > 1 ? String($("#casFeatureManageSource").val()) : sources[0]})
        });
        if (!result.isConfirmed || result.value.flags.length === 0) {
            return;
        }
        const {flags, source} = result.value;
        try {
            await $.ajax({
                url: `${CasActuatorEndpoints.casConfig()}/update`,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(flags.map(flag => ({name: flag.name, value: flag.value, propertySource: source})))
            });
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
            return;
        }
        await CasFeatureToggles.load();
        renderCasFeatureCatalog(CAS_FEATURES);
        if ($.fn.dataTable.isDataTable("#configurationTable") && typeof reloadConfigurationTable === "function") {
            $.get(CasActuatorEndpoints.env(), response => reloadConfigurationTable(response));
        }
        PalantirDialogs.done(`${group.title} Is Updated in ${source}`, "Restart CAS for the change to take effect.");
    }

    /**
     * Offer to turn features and modules on or off from their cards.
     */
    static initializeMenu() {
        if (!CasFeatureToggles.available()) {
            return;
        }
        initializeContextMenu({
            selector: "#casFeaturesCatalog [data-cas-feature], #casFeaturesCatalog [data-cas-feature-module]",
            build: $trigger => {
                const module = $trigger.is("[data-cas-feature-module]") ? $trigger.data("cas-feature-module") : null;
                const name = String($trigger.closest("[data-cas-feature]").data("cas-feature"));
                const group = groupCasFeatures(CAS_FEATURES).find(entry => entry.name === name);
                return group ? {group, module: module === null ? null : String(module)} : false;
            },
            items: context => {
                if (!context.module && context.group.modules.length > 0) {
                    return {
                        manage: {
                            name: `Manage ${context.group.title}…`,
                            icon: contextMenuIcon("mdi-tune-variant"),
                            write: "casConfig"
                        }
                    };
                }
                const entries = context.module
                    ? [`${context.group.name}.${context.module}`]
                    : CasFeatureToggles.properties(context.group, null).map(name => name.replace(/^CasFeatureModule\./, "").replace(/\.enabled$/, ""));
                const states = entries.map(entry => CasFeatureToggles.state(entry).state);
                const subject = CasFeatureToggles.subject(context.group, context.module);
                return {
                    disable: {
                        name: `Turn Off ${subject}`,
                        icon: contextMenuIcon("mdi-toggle-switch-off-outline"),
                        write: "casConfig",
                        visible: () => states.some(state => state === "on" || state === "turning-on")
                    },
                    enable: {
                        name: `Turn On ${subject}`,
                        icon: contextMenuIcon("mdi-toggle-switch-outline"),
                        write: "casConfig",
                        visible: () => states.some(state => state === "off" || state === "turning-off")
                    }
                };
            },
            callback: (key, options) => key === "manage"
                ? CasFeatureToggles.manage(options.context.group)
                : CasFeatureToggles.change(options.context.group, options.context.module, key === "enable")
        });
    }
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
    const escape = escapePalantirMarkup;
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

    const labels = {
        "off": "Off",
        "turning-off": "Off after restart",
        "turning-on": "On after restart"
    };
    const badge = entry => {
        const state = CasFeatureToggles.state(entry).state;
        return labels[state] ? `<span class="palantir-feature-state is-${state}">${labels[state]}</span>` : "";
    };
    catalog.html(shown.map(group => {
        const property = module => CasFeatureToggles.property(group.name, module);
        const modules = group.modules.length === 0
            ? "<span class=\"palantir-feature-card__core\">Registered on its own, without modules</span>"
            : group.modules.map(module => `
                <li class="palantir-tag is-${CasFeatureToggles.state(`${group.name}.${module}`).state}" title="${property(module)}"
                    data-cas-feature-module="${markCasFeatureMatch(module, "")}">${markCasFeatureMatch(module, query)}${badge(`${group.name}.${module}`)}</li>`).join("");
        return `
            <article class="palantir-feature-card is-${group.core || group.modules.length === 0 ? CasFeatureToggles.state(group.name).state : "on"}"
                     role="listitem" title="${property(null)}" data-cas-feature="${markCasFeatureMatch(group.name, "")}">
                <header class="palantir-feature-card__head">
                    <span class="palantir-feature-card__initial" aria-hidden="true">${group.title.charAt(0)}</span>
                    <span class="palantir-feature-card__name">
                        <span>${group.byName ? markCasFeatureMatch(group.title, query) : markCasFeatureMatch(group.title, "")}</span>
                        <code>${group.byName ? markCasFeatureMatch(group.name, query) : markCasFeatureMatch(group.name, "")}</code>
                    </span>
                    ${group.total > 0 ? `<span class="palantir-feature-card__count">${group.total} ${group.total === 1 ? "module" : "modules"}</span>` : ""}
                    ${group.core || group.modules.length === 0 ? badge(group.name) : ""}
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
    if (CasFeatureToggles.available()) {
        CasFeatureToggles.initializeMenu();
        CasFeatureToggles.load().then(() => renderCasFeatureCatalog(CAS_FEATURES));
    }
    return CAS_FEATURES;
}
