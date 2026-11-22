function mfaProviderPropertiesMutable() {
    return PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig();
}

/**
 * Move to the configuration tab's dynamic sources panel.
 *
 * The configuration tab is initialized on first activation, so this waits for that to finish before
 * selecting the inner panel: its handlers are not bound until then, and restoring the operator's
 * stored inner selection would otherwise run afterwards and undo this one.
 *
 * @returns {Promise<void>} resolved once the panel is selected
 */
async function switchToConfigurationDynamicSourcesTab() {
    PalantirPages.open(Tabs.CONFIGURATION);
    await initializeDashboardTab(Tabs.CONFIGURATION);
    $("#configuration-tabs").tabs("option", "active", $("#mutableConfigSources").index());
}

async function overrideMfaProviderConfigurationPropertyValue(propertyName, propertyValue) {
    if (mfaProviderPropertiesMutable()) {
        await switchToConfigurationDynamicSourcesTab();
        overrideConfigPropertyValue(propertyName, propertyValue);
    }
}

async function showMfaProviderConfigurationPropertyHelp(propertyName) {
    if (CasActuatorEndpoints.configurationMetadata()) {
        await switchToConfigurationDynamicSourcesTab();
        searchForConfigPropertyButton(propertyName);
    }
}

async function populateMultifactorProviderTables() {
    if (!CasActuatorEndpoints.discoveryProfile()) {
        hideElements($("#mfaProvidersTab").parent());
        return;
    }
    await new Promise(resolve => CasDiscoveryProfile.fetchIfNeeded().always(resolve));
    if (Object.keys(CasDiscoveryProfile.multifactorAuthenticationProviders()).length === 0) {
        hideElements($("#mfaProvidersTab").parent());
        return;
    }
    showElements($("#mfaProvidersTab").parent());
    PalantirMfaProviders.initialize().catch(error => console.error("The multifactor providers could not be shown:", error));
    updateNavigationSidebar();
}

/**
 * The multifactor authentication providers CAS registers, listed with their rank and state, and the selected one
 * shown with its key facts and its settings. A setting is shown once, with the value that takes effect, the property
 * source it comes from, and, from the configuration metadata, its description and default. Duo Security providers
 * report whether Duo answers, from the Duo ping endpoint.
 */
class PalantirMfaProviders {
    static providers = [];

    static selected = null;

    static showUnset = false;

    static chosen = false;

    static GROUPS = ["Identity & ranking", "Behavior", "Bypass", "Storage", "Security"];

    static duoAccounts = {};

    static tableProvider = null;

    static DUO_STATUSES = {
        AUTH: {style: "is-ok", text: "Active", title: "Signs in with Duo"},
        ALLOW: {style: "is-warn", text: "Bypass", title: "Skips Duo at sign in"},
        DENY: {style: "is-alert", text: "Disabled", title: "Duo refuses to sign this user in"},
        ENROLL: {style: "is-warn", text: "Not Enrolled", title: "Has to enroll with Duo at the next sign in"},
        UNAVAILABLE: {style: "is-alert", text: "Duo Unavailable", title: "Duo could not be asked"}
    };

    static IDENTITY = new Set(["id", "name", "rank", "order", "failure-mode"]);

    static STORAGE = new Set(["jpa", "json", "rest", "mongo", "redis", "ldap", "dynamo-db", "couch-db", "git",
        "groovy", "cosmos-db", "memcached", "hazelcast", "jdbc", "cassandra"]);

    static SENSITIVE = /(secret|password|private-key|encryption\.key|signing\.key|token|credential|-key$)/i;

    static TYPES = {
        "gauth": {id: "mfa-gauth", name: "Google Authenticator", repository: "gauthCredentialRepository"},
        "web-authn": {id: "mfa-webauthn", name: "WebAuthn", repository: "webAuthnDevices"},
        "yubikey": {id: "mfa-yubikey", name: "YubiKey", repository: "yubikeyAccountRepository"},
        "duo": {id: "mfa-duo", name: "Duo Security"},
        "radius": {id: "mfa-radius", name: "RADIUS (RSA,WiKID,etc)"},
        "simple": {id: "mfa-simple", name: "CAS Simple Multifactor Authentication"},
        "twilio": {id: "mfa-twilio", name: "CAS Twilio Multifactor Authentication"},
        "inwebo": {id: "mfa-inwebo", name: "Inwebo"}
    };

    /**
     * The type of a provider, from the setting that gives it its id, else from its fixed name or its default id.
     *
     * @param id the provider id
     * @param name the provider name
     * @param identifiers the provider ids the settings give, keyed by type
     * @returns {string|null} the type, such as gauth or duo
     */
    static typeOf(id, name, identifiers = new Map()) {
        const types = Object.entries(PalantirMfaProviders.TYPES);
        return [...identifiers.entries()].find(([, ids]) => ids.includes(id))?.[0]
            ?? types.find(([type, entry]) => type !== "duo" && entry.name === name)?.[0]
            ?? types.find(([, entry]) => entry.id === id)?.[0]
            ?? (/duo/i.test(id) ? "duo" : null);
    }

    /**
     * The provider ids that the settings give, keyed by type, so a provider with an id of its own still finds its
     * settings.
     *
     * @returns {Promise<Map<string, string[]>>} the ids per type
     */
    static async identifiers() {
        const identifiers = new Map();
        if (!CasActuatorEndpoints.env()) {
            return identifiers;
        }
        try {
            const response = await $.get(`${CasActuatorEndpoints.env()}?pattern=${encodeURIComponent("^cas\\.authn\\.mfa\\.[a-z-]+(\\[\\d+\\])?\\.id$")}`);
            (response?.propertySources ?? []).forEach(source => Object.entries(source.properties ?? {}).forEach(([name, entry]) => {
                const type = name.match(/^cas\.authn\.mfa\.([a-z-]+)(\[\d+\])?\.id$/)?.[1];
                if (type && PalantirMfaProviders.TYPES[type] && entry?.value !== undefined) {
                    identifiers.set(type, [...identifiers.get(type) ?? [], String(entry.value)]);
                }
            }));
        } catch (xhr) {
            console.warn("The multifactor provider ids could not be read", xhr);
        }
        return identifiers;
    }

    static type(provider, type) {
        provider.type = type;
        provider.prefix = type ? `cas.authn.mfa.${type}` : `cas.authn.mfa.${provider.id.replace(/^mfa-/, "")}`;
        provider.repository = PalantirMfaProviders.TYPES[type]?.repository ?? null;
    }

    static isDuo(provider) {
        return provider?.type === "duo";
    }

    static initials(name) {
        return String(name).split(/\s+/).filter(Boolean).slice(0, 2).map(word => word[0].toUpperCase()).join("") || "MF";
    }

    static async initialize() {
        PalantirMfaProviders.providers = Object.entries(CasDiscoveryProfile.multifactorAuthenticationProviders())
            .map(([id, name]) => ({id, name: String(name), settings: [], metadata: new Map(), status: null, summary: null, loaded: false}));
        PalantirMfaProviders.providers.forEach(provider => PalantirMfaProviders.type(provider,
            PalantirMfaProviders.typeOf(provider.id, provider.name)));
        PalantirMfaProviders.selected = PalantirMfaProviders.providers[0]?.id ?? null;
        PalantirMfaProviders.chosen = false;
        PalantirMfaProviders.bind();
        PalantirMfaProviders.renderList();
        PalantirMfaProviders.renderDetail();
        const identifiers = await PalantirMfaProviders.identifiers();
        PalantirMfaProviders.providers.forEach(provider => PalantirMfaProviders.type(provider,
            PalantirMfaProviders.typeOf(provider.id, provider.name, identifiers)));
        await Promise.all(PalantirMfaProviders.providers.map(provider => PalantirMfaProviders.load(provider).then(() => {
            PalantirMfaProviders.renderList();
            if (provider.id === PalantirMfaProviders.selected) {
                PalantirMfaProviders.renderDetail();
            }
        })));
        PalantirMfaProviders.providers.sort((left, right) => PalantirMfaProviders.rank(left) - PalantirMfaProviders.rank(right)
            || left.name.localeCompare(right.name));
        if (!PalantirMfaProviders.chosen) {
            PalantirMfaProviders.selected = PalantirMfaProviders.providers[0]?.id ?? null;
        }
        PalantirMfaProviders.renderList();
        PalantirMfaProviders.renderDetail();
    }

    static bind() {
        $("#mfaProvidersFilter").off("input").on("input", () => {
            const shown = PalantirMfaProviders.providers.filter(PalantirMfaProviders.matchesProvider);
            if (shown.length > 0 && !shown.some(provider => provider.id === PalantirMfaProviders.selected)) {
                PalantirMfaProviders.selected = shown[0].id;
            }
            PalantirMfaProviders.renderList();
            PalantirMfaProviders.renderDetail();
        });
        $("#mfaProvidersList").off("click keydown")
            .on("click", "[data-mfa-provider-id]", event => PalantirMfaProviders.select(event.currentTarget.dataset.mfaProviderId))
            .on("keydown", "[data-mfa-provider-id]", event => {
                const step = {ArrowDown: 1, ArrowUp: -1}[event.key];
                if (step) {
                    event.preventDefault();
                    const items = $("#mfaProvidersList [data-mfa-provider-id]").toArray();
                    const next = items[(items.indexOf(event.currentTarget) + step + items.length) % items.length];
                    PalantirMfaProviders.select(next?.dataset.mfaProviderId);
                    $(`#mfaProvidersList [data-mfa-provider-id="${CSS.escape(next?.dataset.mfaProviderId ?? "")}"]`).trigger("focus");
                }
            });
        $("#mfaProviderDetail").off("click").on("click", "[data-mfa-provider-action]", event => {
            const button = event.currentTarget;
            const actions = {
                help: () => showMfaProviderConfigurationPropertyHelp(button.dataset.property),
                override: () => overrideMfaProviderConfigurationPropertyValue(button.dataset.property, button.dataset.value ?? ""),
                unset: () => {
                    PalantirMfaProviders.showUnset = !PalantirMfaProviders.showUnset;
                    PalantirMfaProviders.renderDetail();
                },
                ping: () => PalantirMfaProviders.ping(PalantirMfaProviders.current(), true),
                "duo-edit": () => PalantirMfaProviders.editDuoAccount(true),
                "duo-cancel": () => PalantirMfaProviders.editDuoAccount(false),
                "duo-bypass": () => PalantirMfaProviders.createDuoBypassCodes(),
                repository: () => PalantirMfaProviders.openTab("#mfaRepositoriesTab"),
                devices: () => PalantirMfaProviders.openTab("#mfaDevicesTab")
            };
            actions[button.dataset.mfaProviderAction]?.();
        });
        $("#mfaProviderDetail")
            .off("submit input")
            .on("submit", "form[data-mfa-duo-form]", event => {
                event.preventDefault();
                if (event.currentTarget.dataset.mfaDuoForm === "lookup") {
                    PalantirMfaProviders.lookUpDuoAccount(String($("#mfaDuoUsername").val() ?? "").trim());
                } else {
                    PalantirMfaProviders.saveDuoAccount(event.currentTarget);
                }
            })
            .on("input", "#mfaDuoUsername", event => {
                PalantirMfaProviders.duoAccountOf(PalantirMfaProviders.current()).typed = event.currentTarget.value;
            });
    }

    static openTab(selector) {
        const tabs = $("#mfa-tabs");
        tabs.tabs("option", "active", $(selector).parent().index());
    }

    static current() {
        return PalantirMfaProviders.providers.find(provider => provider.id === PalantirMfaProviders.selected) ?? null;
    }

    static select(id) {
        if (!id || id === PalantirMfaProviders.selected) {
            return;
        }
        PalantirMfaProviders.selected = id;
        PalantirMfaProviders.chosen = true;
        PalantirMfaProviders.showUnset = false;
        PalantirMfaProviders.renderList();
        PalantirMfaProviders.renderDetail();
    }

    /**
     * Read a provider's settings, their metadata and what it holds, then, for Duo Security, ask whether Duo answers,
     * without holding the settings back until it does.
     *
     * @param provider the provider
     * @returns {Promise<void>} resolved once the settings, metadata and registrations are read
     */
    static async load(provider) {
        await Promise.all([
            PalantirMfaProviders.loadSettings(provider),
            PalantirMfaProviders.loadMetadata(provider),
            PalantirMfaProviders.loadSummary(provider)
        ]);
        provider.loaded = true;
        PalantirMfaProviders.ping(provider, true);
    }

    static async loadSettings(provider) {
        if (!CasActuatorEndpoints.env()) {
            provider.settingsError = "The environment endpoint is not available, so the settings cannot be read.";
            return;
        }
        try {
            const pattern = provider.prefix.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
            const response = await $.get(`${CasActuatorEndpoints.env()}?pattern=${encodeURIComponent(pattern)}`);
            const seen = new Map();
            (response?.propertySources ?? []).forEach(source => {
                Object.entries(source.properties ?? {}).forEach(([name, entry]) => {
                    if (PalantirMfaProviders.within(name, provider.prefix) && !seen.has(name)) {
                        seen.set(name, {name, value: entry?.value, source: source.name});
                    }
                });
            });
            let settings = [...seen.values()];
            if (PalantirMfaProviders.isDuo(provider)) {
                const own = settings.find(setting => /\]\.(id|name)$/.test(setting.name)
                    && (setting.value === provider.id || setting.value === provider.name));
                const entry = own ? own.name.replace(/\.(id|name)$/, "") : `${provider.prefix}[0]`;
                provider.prefix = entry;
                settings = settings.filter(setting => setting.name.startsWith(`${entry}.`));
            }
            provider.settings = settings;
        } catch (xhr) {
            provider.settingsError = xhr?.status ? `CAS answered ${xhr.status}` : "The settings could not be read";
        }
    }

    static within(name, prefix) {
        return name.startsWith(`${prefix}.`) || name.startsWith(`${prefix}[`);
    }

    static async loadMetadata(provider) {
        if (!CasActuatorEndpoints.configurationMetadata()) {
            return;
        }
        try {
            const base = provider.prefix.replace(/\[\d+\]$/, "");
            const results = await $.get(`${CasActuatorEndpoints.configurationMetadata()}/${encodeURIComponent(base)}`);
            (Array.isArray(results) ? results : []).filter(entry => PalantirMfaProviders.within(String(entry.id ?? ""), base))
                .forEach(entry => provider.metadata.set(entry.id, entry));
        } catch (xhr) {
            console.warn("The configuration metadata of", provider.id, "could not be read", xhr);
        }
    }

    static async loadSummary(provider) {
        const repository = provider.repository ? PalantirCredentialRepositories.repository(provider.repository) : null;
        if (!repository) {
            return;
        }
        try {
            const summary = await PalantirCredentialRepositories.summary(repository);
            provider.summary = {users: Number(summary?.users ?? 0), records: Number(summary?.records ?? 0)};
        } catch (xhr) {
            provider.summary = null;
        }
    }

    /**
     * Ask Duo whether it answers, for a Duo Security provider, and how long it took.
     *
     * @param provider the provider
     * @param redraw whether to redraw once the answer is in
     * @returns {Promise<void>} resolved once Duo answered or the request failed
     */
    static async ping(provider, redraw) {
        if (!provider || !PalantirMfaProviders.isDuo(provider) || !CasActuatorEndpoints.duoPing()) {
            return;
        }
        provider.status = {state: "checking"};
        if (redraw) {
            PalantirMfaProviders.renderList();
            PalantirMfaProviders.renderDetail();
        }
        const started = performance.now();
        try {
            const response = await $.ajax({
                url: `${CasActuatorEndpoints.duoPing()}?providerId=${encodeURIComponent(provider.id)}`,
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
            const answer = response?.[provider.id] ?? Object.values(response ?? {})[0];
            const elapsed = Math.round(performance.now() - started);
            provider.status = answer === undefined
                ? {state: "unknown", text: "Duo did not report this provider"}
                : {state: answer.availability === true ? "up" : "down", elapsed, host: answer.duoApiHost};
        } catch (xhr) {
            provider.status = {state: "unknown", text: xhr?.status ? `The ping failed: CAS answered ${xhr.status}` : "The ping failed"};
        }
        if (redraw) {
            PalantirMfaProviders.renderList();
            PalantirMfaProviders.renderDetail();
        }
    }

    static setting(provider, leaf) {
        return provider.settings.find(setting => setting.name === `${provider.prefix}.${leaf}`) ?? null;
    }

    static metadataOf(provider, name) {
        return provider.metadata.get(name) ?? provider.metadata.get(name.replace(/\[\d+\]/g, "[0]")) ?? null;
    }

    static valueOf(provider, leaf) {
        const setting = PalantirMfaProviders.setting(provider, leaf);
        if (setting) {
            return setting.value;
        }
        return PalantirMfaProviders.metadataOf(provider, `${provider.prefix}.${leaf}`)?.defaultValue;
    }

    static rank(provider) {
        const value = Number(PalantirMfaProviders.valueOf(provider, "rank") ?? 0);
        return Number.isFinite(value) ? value : 0;
    }

    static failureMode(provider) {
        return String(PalantirMfaProviders.valueOf(provider, "failure-mode") ?? "CLOSED").toUpperCase();
    }

    static sensitive(name, value) {
        return PalantirMfaProviders.SENSITIVE.test(name) || /^\*+$/.test(String(value ?? ""));
    }

    static changed(provider, setting) {
        const defaultValue = PalantirMfaProviders.metadataOf(provider, setting.name)?.defaultValue;
        return defaultValue === undefined || defaultValue === null || String(defaultValue) !== String(setting.value);
    }

    static group(provider, name) {
        const rest = name.slice(provider.prefix.length + 1);
        const first = rest.split(".")[0];
        if (rest.split(".").length === 1 && PalantirMfaProviders.IDENTITY.has(first)) {
            return "Identity & ranking";
        }
        if (first === "bypass") {
            return "Bypass";
        }
        if (PalantirMfaProviders.STORAGE.has(first)) {
            return "Storage";
        }
        if (first === "crypto" || PalantirMfaProviders.SENSITIVE.test(rest)) {
            return "Security";
        }
        return "Behavior";
    }

    static source(name) {
        const text = String(name ?? "");
        const file = text.match(/\[(?:file:|classpath:)?([^\]]+)\]/)?.[1];
        return file ? file.split("/").pop() : text.replace(/^applicationConfig:\s*/, "");
    }

    static bypassRules(provider) {
        return provider.settings.filter(setting => setting.name.startsWith(`${provider.prefix}.bypass.`)
            && String(setting.value ?? "").trim() !== "" && String(setting.value) !== "false").length;
    }

    static term() {
        return String($("#mfaProvidersFilter").val() ?? "").trim().toLowerCase();
    }

    static matchesSetting(provider, setting) {
        const term = PalantirMfaProviders.term();
        if (term === "") {
            return true;
        }
        const description = PalantirMfaProviders.metadataOf(provider, setting.name)?.shortDescription ?? "";
        const value = PalantirMfaProviders.sensitive(setting.name, setting.value) ? "" : setting.value;
        return [setting.name, value, description].some(text => String(text ?? "").toLowerCase().includes(term));
    }

    static matchesProvider(provider) {
        const term = PalantirMfaProviders.term();
        return term === "" || `${provider.name} ${provider.id}`.toLowerCase().includes(term)
            || provider.settings.some(setting => PalantirMfaProviders.matchesSetting(provider, setting));
    }

    /**
     * The chip that says how a provider is doing: whether Duo answers, or how many users registered with it.
     *
     * @param provider the provider
     * @returns {{style: string, text: string, title: string}} the chip
     */
    static statusOf(provider) {
        const status = provider.status;
        if (status?.state === "checking") {
            return {style: "", text: "Checking…", title: "Asking Duo whether it answers"};
        }
        if (status?.state === "up") {
            return {style: "is-ok", text: `Reachable · ${status.elapsed} ms`, title: `Duo answered from ${status.host ?? "its API host"}`};
        }
        if (status?.state === "down") {
            return {style: "is-alert", text: "Unreachable", title: `Duo did not answer from ${status.host ?? "its API host"}`};
        }
        if (status?.state === "unknown") {
            return {style: "is-warn", text: "Not Checked", title: status.text};
        }
        if (provider.summary) {
            return provider.summary.users === 0
                ? {style: "is-warn", text: "No Registrations", title: "Nobody has registered with this provider yet"}
                : {style: "is-ok", text: `${provider.summary.users.toLocaleString()} Registered`, title: "Users with a registration"};
        }
        if (!provider.loaded) {
            return {style: "", text: "Reading…", title: ""};
        }
        return {style: "", text: "Registered", title: "CAS registers this provider"};
    }

    static renderList() {
        const escape = escapePalantirMarkup;
        const shown = PalantirMfaProviders.providers.filter(PalantirMfaProviders.matchesProvider);
        if (shown.length === 0) {
            $("#mfaProvidersList").html(`<p class="palantir-empty-state">No provider or setting matches that filter.</p>`);
            return;
        }
        $("#mfaProvidersList").html(shown.map(provider => {
            const status = PalantirMfaProviders.statusOf(provider);
            const selected = provider.id === PalantirMfaProviders.selected;
            return `
                <button type="button" role="tab" class="palantir-mfa-providers__item${selected ? " is-selected" : ""}"
                        data-mfa-provider-id="${escape(provider.id)}" aria-selected="${selected}" tabindex="${selected ? 0 : -1}">
                    <span class="palantir-mfa-providers__badge" aria-hidden="true">${escape(PalantirMfaProviders.initials(provider.name))}</span>
                    <span class="palantir-mfa-providers__name">
                        <strong>${escape(provider.name)}</strong>
                        <small><code>${escape(provider.id)}</code>${provider.loaded ? ` · rank ${escape(PalantirMfaProviders.rank(provider))}` : ""}</small>
                        <span class="palantir-mfa-providers__chip ${status.style}" title="${escape(status.title)}">${escape(status.text)}</span>
                    </span>
                </button>`;
        }).join(""));
    }

    static fact(label, value, title = "") {
        return `<div class="palantir-mfa-providers__fact" title="${escapePalantirMarkup(title)}"><small>${escapePalantirMarkup(label)}</small><strong>${escapePalantirMarkup(value)}</strong></div>`;
    }

    /**
     * One row of the provider's settings table: its group, name and description, the value that takes effect, the
     * property source it comes from, and its help and override buttons.
     *
     * @param provider the provider
     * @param setting the setting, or only its name when it is left at its default
     * @param unset whether the setting is left at its default
     * @returns {object} the row
     */
    static settingEntry(provider, setting, unset) {
        const escape = escapePalantirMarkup;
        const meta = PalantirMfaProviders.metadataOf(provider, setting.name);
        const masked = PalantirMfaProviders.sensitive(setting.name, setting.value);
        const known = provider.metadata.size > 0;
        const changed = known && !unset && PalantirMfaProviders.changed(provider, setting);
        const shownValue = unset ? `${meta?.defaultValue ?? "—"} (default)` : masked ? "••••••" : String(setting.value ?? "");
        const canOverride = typeof mfaProviderPropertiesMutable === "function" && mfaProviderPropertiesMutable();
        const canHelp = Boolean(CasActuatorEndpoints.configurationMetadata());
        return {
            group: PalantirMfaProviders.group(provider, setting.name),
            name: setting.name,
            state: unset ? "is-unset" : changed ? "is-changed" : "",
            setting: `<code title="${escape(setting.name)}">${escape(setting.name)}</code>
                ${meta?.shortDescription ? `<span class="palantir-mfa-providers__description">${escape(meta.shortDescription)}</span>` : ""}`,
            value: `<span class="palantir-mfa-providers__value" title="${escape(masked ? "Hidden" : shownValue)}">${escape(shownValue)}${known && !unset && !changed ? ` <small>(default)</small>` : ""}</span>`,
            sortValue: masked ? "" : shownValue,
            source: unset ? "" : `<span class="palantir-mfa-providers__source" title="${escape(setting.source)}">${escape(PalantirMfaProviders.source(setting.source))}</span>`,
            sortSource: unset ? "" : String(setting.source ?? ""),
            actions: `<div class="palantir-mfa-providers__row-actions">
                    ${canHelp ? `<button type="button" class="palantir-icon-button palantir-mfa-providers__icon" data-mfa-provider-action="help"
                        data-property="${escape(setting.name)}" title="Help" aria-label="Help for ${escape(setting.name)}">
                        <i class="mdi mdi-help-circle-outline" aria-hidden="true"></i></button>` : ""}
                    ${canOverride && masked ? `<span class="palantir-mfa-providers__icon" aria-hidden="true"></span>` : ""}
                    ${canOverride && !masked ? `<button type="button" class="palantir-icon-button palantir-mfa-providers__icon" data-palantir-write="casConfig"
                        data-mfa-provider-action="override" data-property="${escape(setting.name)}"
                        data-value="${escape(unset ? meta?.defaultValue ?? "" : setting.value ?? "")}" title="Override" aria-label="Override ${escape(setting.name)}">
                        <i class="mdi mdi-pencil-outline" aria-hidden="true"></i></button>` : ""}
                </div>`
        };
    }

    /**
     * Draw the provider's settings as the standard Palantir table, grouped like the configuration table: identity
     * and ranking, behavior, bypass, storage and security.
     *
     * @param entries the rows
     * @param state the page, search, order and page length the table had before it was drawn again for the same
     * provider, so that a redraw, such as after a Duo ping, does not move the operator back to the first page
     */
    static drawSettingsTable(entries, state) {
        const groupIndex = group => PalantirMfaProviders.GROUPS.indexOf(group);
        const display = (field, sortField) => (data, type, row) => type === "display" ? row[field] : row[sortField ?? field];
        $("#mfaProviderSettingsTable").DataTable({
            data: entries,
            pageLength: 10,
            autoWidth: false,
            orderFixed: [[0, "asc"]],
            order: [[1, "asc"]],
            columns: [
                {data: "group", visible: false, render: (data, type) => type === "display" || type === "filter" ? data : groupIndex(data)},
                {data: "name", render: display("setting", "name"), className: "palantir-mfa-providers__setting"},
                {data: "sortValue", render: display("value", "sortValue")},
                {data: "sortSource", render: display("source", "sortSource")},
                {data: null, orderable: false, searchable: false, render: display("actions", "name"), className: "palantir-mfa-providers__actions"}
            ],
            createdRow: (row, data) => {
                if (data.state) {
                    row.classList.add(data.state);
                }
            },
            drawCallback: settings => {
                $("#mfaProviderSettingsTable tr").addClass("mdc-data-table__row");
                $("#mfaProviderSettingsTable td").addClass("mdc-data-table__cell");
                const api = settings.api;
                const rows = api.rows({page: "current"}).nodes();
                let last = null;
                api.rows({page: "current"}).data().each((entry, index) => {
                    if (last !== entry.group) {
                        $(rows).eq(index).before(palantirTableGroupRow(entry.group, 4));
                        last = entry.group;
                    }
                });
                applyPalantirEndpointAccess(document.getElementById("mfaProviderSettingsTable"));
            }
        });
        if (state) {
            const table = $("#mfaProviderSettingsTable").DataTable();
            table.page.len(state.length).search(state.search).order(state.order).draw();
            table.page(Math.min(state.page, Math.max(table.page.info().pages - 1, 0))).draw(false);
        }
    }

    static renderDetail() {
        const escape = escapePalantirMarkup;
        const provider = PalantirMfaProviders.current();
        const detail = $("#mfaProviderDetail");
        let tableState = null;
        if ($.fn.dataTable.isDataTable("#mfaProviderSettingsTable")) {
            const previous = $("#mfaProviderSettingsTable").DataTable();
            tableState = {provider: PalantirMfaProviders.tableProvider, page: previous.page(), search: previous.search(),
                order: previous.order(), length: previous.page.len()};
            previous.destroy();
        }
        if (!provider) {
            detail.html(`<p class="palantir-empty-state">No multifactor authentication provider is registered.</p>`);
            return;
        }
        const typing = document.activeElement?.id === "mfaDuoUsername";
        $("#mfaProviderDetail").attr("aria-label", provider.name);
        if (!provider.loaded) {
            detail.html(`<p class="palantir-empty-state">Reading the settings of ${escape(provider.name)}…</p>`);
            return;
        }
        const settings = provider.settings.filter(setting => PalantirMfaProviders.matchesSetting(provider, setting));
        const unset = PalantirMfaProviders.showUnset
            ? [...provider.metadata.values()]
                .map(meta => ({name: PalantirMfaProviders.unsetName(provider, meta)}))
                .filter(entry => !provider.settings.some(setting => setting.name === entry.name)
                    && entry.name.startsWith(`${provider.prefix}.`)
                    && PalantirMfaProviders.matchesSetting(provider, entry))
            : [];
        const changedCount = provider.settings.filter(setting => PalantirMfaProviders.changed(provider, setting)).length;
        const total = Math.max(provider.metadata.size, provider.settings.length);
        const status = PalantirMfaProviders.statusOf(provider);
        const isDuo = PalantirMfaProviders.isDuo(provider);
        const repository = provider.repository;
        const facts = [
            PalantirMfaProviders.fact("Rank", PalantirMfaProviders.rank(provider)),
            PalantirMfaProviders.fact("Failure Mode", PalantirMfaProviders.failureMode(provider), "What happens when the provider cannot be reached"),
            provider.summary ? PalantirMfaProviders.fact("Registered Users", provider.summary.users.toLocaleString()) : "",
            isDuo && provider.status?.host ? PalantirMfaProviders.fact("API host", provider.status.host) : "",
            PalantirMfaProviders.fact("Bypass Rules", PalantirMfaProviders.bypassRules(provider) === 0 ? "None" : `${PalantirMfaProviders.bypassRules(provider)} set`),
            provider.metadata.size > 0
                ? PalantirMfaProviders.fact("Changed Settings", `${changedCount} of ${total}`, "Settings whose value differs from their default")
                : PalantirMfaProviders.fact("Settings Set", String(provider.settings.length), "Without the configuration metadata, defaults are not known")
        ].join("");
        const entries = [
            ...settings.map(setting => PalantirMfaProviders.settingEntry(provider, setting, false)),
            ...unset.map(entry => PalantirMfaProviders.settingEntry(provider, entry, true))
        ];
        const table = entries.length === 0 ? "" : `
            <table id="mfaProviderSettingsTable" class="mdc-data-table__table table table-striped noborder">
                <thead>
                <tr class="mdc-data-table__header-row">
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Group</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Setting</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Value</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Source</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col"><span class="palantir-sr-only">Actions</span></th>
                </tr>
                </thead>
                <tbody class="mdc-data-table__content"></tbody>
            </table>`;
        const empty = provider.settingsError
            ? `<p class="palantir-empty-state">${escape(provider.settingsError)}</p>`
            : `<p class="palantir-empty-state">${PalantirMfaProviders.term() === "" ? "Every setting of this provider has its default value." : "No setting matches that filter."}</p>`;
        detail.html(`
            <header class="palantir-mfa-providers__head">
                <span class="palantir-mfa-providers__badge is-large" aria-hidden="true">${escape(PalantirMfaProviders.initials(provider.name))}</span>
                <div class="palantir-mfa-providers__title">
                    <h3>${escape(provider.name)}</h3>
                    <code>${escape(provider.id)}</code>
                    <span class="palantir-mfa-providers__chip ${status.style}" title="${escape(status.title)}">${escape(status.text)}</span>
                </div>
                <div class="palantir-mfa-providers__buttons">
                    ${isDuo && CasActuatorEndpoints.duoPing() ? `<button type="button" class="palantir-button" data-mfa-provider-action="ping"${provider.status?.state === "checking" ? " disabled" : ""}><i class="mdi mdi-access-point-network" aria-hidden="true"></i>Ping Duo</button>` : ""}
                    ${repository && CasActuatorEndpoints[repository]?.() && !$("#mfaRepositoriesTab").parent().hasClass("d-none") ? `<button type="button" class="palantir-button" data-mfa-provider-action="repository"><i class="mdi mdi-database-outline" aria-hidden="true"></i>Credential Repository</button>` : ""}
                    ${CasActuatorEndpoints.mfaDevices() && !$("#mfaDevicesTab").parent().hasClass("d-none") ? `<button type="button" class="palantir-button" data-mfa-provider-action="devices"><i class="mdi mdi-cellphone-link" aria-hidden="true"></i>Devices</button>` : ""}
                </div>
            </header>
            <div class="palantir-mfa-providers__facts">${facts}</div>
            ${isDuo ? PalantirMfaProviders.duoAccountSection(provider) : ""}
            ${table || empty}
            ${PalantirMfaProviders.unsetCount(provider) > 0 || PalantirMfaProviders.showUnset ? `<footer class="palantir-mfa-providers__footer">
                <button type="button" class="palantir-mfa-providers__link" data-mfa-provider-action="unset">${PalantirMfaProviders.showUnset
            ? "Show only the settings that are set" : `Also show the ${PalantirMfaProviders.unsetCount(provider)} settings left at their default`}</button>
            </footer>` : ""}`);
        if (entries.length > 0) {
            PalantirMfaProviders.drawSettingsTable(entries, tableState?.provider === provider.id ? tableState : null);
        }
        PalantirMfaProviders.tableProvider = entries.length > 0 ? provider.id : null;
        applyPalantirEndpointAccess(detail[0]);
        if (typing) {
            const input = document.getElementById("mfaDuoUsername");
            input?.focus();
            input?.setSelectionRange(input.value.length, input.value.length);
        }
    }

    /**
     * What the Duo account tools hold for a provider: the username typed, the account looked up, and whether its
     * details are being edited.
     *
     * @param provider the provider
     * @returns {object} the provider's Duo account state
     */
    static duoAccountOf(provider) {
        const id = provider?.id ?? "";
        PalantirMfaProviders.duoAccounts[id] ??= {typed: "", username: "", loading: false, status: null, account: null,
            adminAnswered: false, error: "", adminError: "", editing: false};
        return PalantirMfaProviders.duoAccounts[id];
    }

    /**
     * Look a username up in Duo Security: how Duo would treat its next sign-in, from the account status endpoint, and
     * its account, devices, groups and bypass codes, from the Admin API.
     *
     * @param username the username
     * @returns {Promise<void>} resolved once both answers are in
     */
    static async lookUpDuoAccount(username) {
        const provider = PalantirMfaProviders.current();
        const state = PalantirMfaProviders.duoAccountOf(provider);
        if (!provider || username === "") {
            return;
        }
        Object.assign(state, {typed: username, username, loading: true, status: null, account: null, adminAnswered: false,
            error: "", adminError: "", editing: false});
        PalantirMfaProviders.renderDetail();
        const query = `?providerId=${encodeURIComponent(provider.id)}`;
        const read = url => $.ajax({url, dataType: "json", headers: {"Accept": "application/json"}});
        const [status, admin] = await Promise.allSettled([
            CasActuatorEndpoints.duoAccountStatus()
                ? read(`${CasActuatorEndpoints.duoAccountStatus()}/${encodeURIComponent(username)}${query}`)
                : Promise.resolve(null),
            CasActuatorEndpoints.duoAdmin()
                ? read(`${CasActuatorEndpoints.duoAdmin()}/${encodeURIComponent(username)}${query}`)
                : Promise.resolve(null)
        ]);
        if (state.username !== username) {
            return;
        }
        state.loading = false;
        if (status.status === "fulfilled" && status.value) {
            state.status = (status.value[provider.id] ?? Object.values(status.value)[0])?.accountStatus ?? null;
        }
        if (admin.status === "fulfilled" && admin.value) {
            state.adminAnswered = true;
            state.account = admin.value[provider.id] ?? Object.values(admin.value)[0] ?? null;
        }
        const failed = [status, admin].filter(result => result.status === "rejected").map(result => result.reason?.status);
        if (failed.length > 0 && state.status === null && state.account === null) {
            state.error = `Duo could not be asked about ${username}${failed[0] ? ` (CAS answered ${failed[0]})` : ""}.`;
        } else if (admin.status === "rejected") {
            state.adminError = `Duo's Admin API could not be asked about ${username}${admin.reason?.status ? ` (CAS answered ${admin.reason.status})` : ""}.`;
        }
        PalantirMfaProviders.renderDetail();
    }

    static editDuoAccount(editing) {
        PalantirMfaProviders.duoAccountOf(PalantirMfaProviders.current()).editing = editing;
        PalantirMfaProviders.renderDetail();
    }

    /**
     * Save the status, name and email address of the Duo account looked up, through the Admin API. Fields left
     * empty keep what Duo has.
     *
     * @param form the edit form
     * @returns {Promise<void>} resolved once Duo answered
     */
    static async saveDuoAccount(form) {
        const provider = PalantirMfaProviders.current();
        const state = PalantirMfaProviders.duoAccountOf(provider);
        if (!provider || !state.account) {
            return;
        }
        const field = name => String(form.elements[name]?.value ?? "").trim();
        const update = {status: field("status"), firstName: field("firstName"), lastName: field("lastName"), email: field("email")};
        try {
            await $.ajax({
                url: `${CasActuatorEndpoints.duoAdmin()}/${encodeURIComponent(state.username)}?providerId=${encodeURIComponent(provider.id)}`,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify(update),
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
            notyf.success(`The Duo account of ${state.username} is updated.`);
            state.editing = false;
            await PalantirMfaProviders.lookUpDuoAccount(state.username);
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
        }
    }

    /**
     * Create bypass codes for the Duo account looked up, once the operator confirms, and show them once.
     *
     * @returns {Promise<void>} resolved once the codes are shown or the operator declined
     */
    static async createDuoBypassCodes() {
        const provider = PalantirMfaProviders.current();
        const state = PalantirMfaProviders.duoAccountOf(provider);
        if (!provider || !state.account) {
            return;
        }
        const escape = escapePalantirMarkup;
        const confirmed = await Swal.fire({
            titleText: `Create Bypass Codes for ${state.username}?`,
            text: "Duo creates new bypass codes for this user. They are shown once, so pass them on before closing.",
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Create",
            cancelButtonText: "Cancel",
            focusCancel: true
        });
        if (!confirmed.isConfirmed) {
            return;
        }
        try {
            const query = `username=${encodeURIComponent(state.username)}&providerId=${encodeURIComponent(provider.id)}`;
            const response = await $.ajax({
                url: `${CasActuatorEndpoints.duoAdmin()}/bypassCodes?${query}`,
                type: "POST",
                contentType: "application/json",
                data: "{}",
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
            const codes = (response?.[provider.id] ?? Object.values(response ?? {})[0] ?? []).map(String);
            if (codes.length === 0) {
                Swal.fire({icon: "warning", titleText: "No Bypass Codes", text: `Duo created no bypass codes for ${state.username}.`});
                return;
            }
            const copied = await Swal.fire({
                titleText: `Bypass Codes for ${state.username}`,
                html: `<p>Each code signs ${escape(state.username)} in once. They are not shown again.</p>
                       <ul class="palantir-mfa-duo__codes">${codes.map(code => `<li><code>${escape(code)}</code></li>`).join("")}</ul>`,
                icon: "success",
                showCancelButton: true,
                confirmButtonText: "Copy Codes",
                cancelButtonText: "Close"
            });
            if (copied.isConfirmed) {
                copyToClipboard(codes.join("\n"));
                notyf.success(`${codes.length} bypass codes copied.`);
            }
            await PalantirMfaProviders.lookUpDuoAccount(state.username);
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
        }
    }

    /**
     * A date Duo reports, as CAS sends it: an ISO date, or seconds or milliseconds since the epoch. The start of the
     * epoch means never.
     *
     * @param value the date
     * @returns {{day: string, full: string}} the day, and the day with the time
     */
    static duoDate(value) {
        const numeric = Number(value);
        const date = value === null || value === undefined || value === ""
            ? null
            : Number.isFinite(numeric) ? new Date(Math.abs(numeric) >= 1e11 ? numeric : numeric * 1000) : new Date(value);
        return date === null || Number.isNaN(date.getTime()) || date.getTime() <= 0
            ? {day: "Never", full: "Never"}
            : {day: date.toLocaleDateString(), full: date.toLocaleString()};
    }

    /**
     * The Duo account tools of a Duo Security provider: look a username up, see how Duo treats it and what its
     * account holds, and, through the Admin API, create bypass codes or change its status, name and email address.
     *
     * @param provider the provider
     * @returns {string} the markup, empty when CAS offers neither Duo endpoint
     */
    static duoAccountSection(provider) {
        if (!CasActuatorEndpoints.duoAccountStatus() && !CasActuatorEndpoints.duoAdmin()) {
            return "";
        }
        const escape = escapePalantirMarkup;
        const state = PalantirMfaProviders.duoAccountOf(provider);
        let result = "";
        if (state.loading) {
            result = `<p class="palantir-mfa-duo__note">Asking Duo about ${escape(state.username)}…</p>`;
        } else if (state.error) {
            result = `<p class="palantir-mfa-duo__note is-alert">${escape(state.error)}</p>`;
        } else if (state.username !== "") {
            result = PalantirMfaProviders.duoAccountResult(provider, state);
        }
        return `
            <section class="palantir-mfa-providers__group palantir-mfa-duo" aria-label="Duo account">
                <h4>Duo Account</h4>
                <form class="palantir-mfa-duo__lookup" data-mfa-duo-form="lookup">
                    <label class="palantir-mfa-providers__filter">
                        <span class="palantir-sr-only">Username to look up in Duo Security</span>
                        <i class="mdi mdi-account-search-outline" aria-hidden="true"></i>
                        <input type="search" id="mfaDuoUsername" placeholder="Username" autocomplete="off" spellcheck="false"
                               value="${escape(state.typed)}"/>
                    </label>
                    <button type="submit" class="palantir-button"${state.loading ? " disabled" : ""}>
                        <i class="mdi mdi-magnify" aria-hidden="true"></i>Look Up</button>
                </form>
                ${result}
            </section>`;
    }

    static duoAccountResult(provider, state) {
        const escape = escapePalantirMarkup;
        const account = state.account;
        const signIn = state.status ? PalantirMfaProviders.DUO_STATUSES[state.status.status] ?? PalantirMfaProviders.DUO_STATUSES.AUTH : null;
        const signInMarkup = signIn ? `
            <div class="palantir-mfa-duo__status">
                <span class="palantir-mfa-providers__chip ${signIn.style}" title="${escape(signIn.title)}">${escape(signIn.text)}</span>
                <span>${escape(state.status.message || signIn.title)}</span>
                ${/^https:\/\//i.test(String(state.status.enrollPortalUrl ?? "")) ? `<a href="${escape(state.status.enrollPortalUrl)}" target="_blank" rel="noopener noreferrer">Enrollment Page</a>` : ""}
            </div>` : "";
        if (!account) {
            const missing = state.adminError || (state.adminAnswered
                ? `Duo's Admin API has no account named ${state.username}, or the Admin API is not set up for this provider.`
                : "");
            return `${signInMarkup}${missing ? `<p class="palantir-mfa-duo__note">${escape(missing)}</p>` : ""}`;
        }
        const name = account.realName || [account.firstName, account.lastName].filter(Boolean).join(" ") || "—";
        const lastLogin = PalantirMfaProviders.duoDate(account.lastLogin);
        const created = PalantirMfaProviders.duoDate(account.created);
        const adminStatus = PalantirMfaProviders.DUO_STATUSES[account.status] ?? PalantirMfaProviders.DUO_STATUSES.AUTH;
        const facts = [
            PalantirMfaProviders.fact("Name", name, name),
            PalantirMfaProviders.fact("Email", account.email || "—", account.email ?? ""),
            PalantirMfaProviders.fact("Account", adminStatus.text, adminStatus.title),
            PalantirMfaProviders.fact("Enrolled", account.enrolled ? "Yes" : "No"),
            PalantirMfaProviders.fact("Last Login", lastLogin.day, lastLogin.full),
            PalantirMfaProviders.fact("Created", created.day, created.full),
            PalantirMfaProviders.fact("Bypass Codes", String((account.bypassCodes ?? []).length), "Bypass codes the user has not used up")
        ].join("");
        const devices = (account.devices ?? []).map(device => `
            <li><i class="mdi ${String(device.type ?? "").toLowerCase() === "mobile" ? "mdi-cellphone" : "mdi-phone"}" aria-hidden="true"></i>
                <span>${escape(device.name || device.model || device.type || "Device")}</span>
                <small>${escape([device.number, device.platform, device.activated ? "activated" : "not activated"].filter(Boolean).join(" · "))}</small></li>`).join("");
        const groups = (account.groups ?? []).map(group => `<span class="palantir-tag">${escape(group.name)}</span>`).join("");
        const editable = Boolean(CasActuatorEndpoints.duoAdmin());
        const statusChoice = ["AUTH", "ALLOW", "DENY"].includes(account.status) ? account.status : "AUTH";
        const edit = state.editing ? `
            <form class="palantir-mfa-duo__edit" data-mfa-duo-form="edit">
                <label><span>Status</span>
                    <select name="status">
                        ${["AUTH", "ALLOW", "DENY"].map(value => `<option value="${value}"${value === statusChoice ? " selected" : ""}>${escape(PalantirMfaProviders.DUO_STATUSES[value].text)}</option>`).join("")}
                    </select></label>
                <label><span>First Name</span><input name="firstName" type="text" autocomplete="off" value="${escape(account.firstName ?? "")}"/></label>
                <label><span>Last Name</span><input name="lastName" type="text" autocomplete="off" value="${escape(account.lastName ?? "")}"/></label>
                <label><span>Email</span><input name="email" type="email" autocomplete="off" value="${escape(account.email ?? "")}"/></label>
                <p class="palantir-mfa-duo__note">A field left empty keeps what Duo has.</p>
                <div class="palantir-mfa-duo__actions">
                    <button type="button" class="palantir-button" data-mfa-provider-action="duo-cancel">Cancel</button>
                    <button type="submit" class="palantir-button" data-palantir-write="duoAdmin"><i class="mdi mdi-content-save-outline" aria-hidden="true"></i>Save</button>
                </div>
            </form>` : "";
        return `
            ${signInMarkup}
            <div class="palantir-mfa-providers__facts">${facts}</div>
            ${devices ? `<ul class="palantir-mfa-duo__devices" aria-label="Devices">${devices}</ul>` : ""}
            ${groups ? `<div class="palantir-mfa-duo__groups" aria-label="Groups">${groups}</div>` : ""}
            ${editable && !state.editing ? `<div class="palantir-mfa-duo__actions">
                <button type="button" class="palantir-button" data-palantir-write="duoAdmin" data-mfa-provider-action="duo-bypass">
                    <i class="mdi mdi-ticket-confirmation-outline" aria-hidden="true"></i>Create Bypass Codes</button>
                <button type="button" class="palantir-button" data-palantir-write="duoAdmin" data-mfa-provider-action="duo-edit">
                    <i class="mdi mdi-account-edit-outline" aria-hidden="true"></i>Edit Account</button>
            </div>` : ""}
            ${edit}`;
    }

    static unsetName(provider, meta) {
        const index = provider.prefix.match(/\[\d+\]/)?.[0];
        return index ? meta.id.replace(/\[0\]/, index) : meta.id;
    }

    static unsetCount(provider) {
        return [...provider.metadata.values()]
            .map(meta => PalantirMfaProviders.unsetName(provider, meta))
            .filter(name => name.startsWith(`${provider.prefix}.`) && !provider.settings.some(setting => setting.name === name)).length;
    }
}

/**
 * The kind of a multifactor device, from the provider that registered it, for the badge of its card.
 *
 * @param {object} device the device
 * @returns {{key: string, icon: string}} the badge style and its icon
 */
function mfaDeviceKind(device) {
    const source = `${device.source ?? ""} ${device.type ?? ""}`.toLowerCase();
    const kinds = [["duo", "mdi-shield-check-outline"], ["google", "mdi-two-factor-authentication"],
        ["webauthn", "mdi-fingerprint"], ["fido", "mdi-fingerprint"], ["yubi", "mdi-usb-flash-drive-outline"],
        ["simple", "mdi-message-text-lock-outline"], ["sms", "mdi-message-text-lock-outline"], ["mobile", "mdi-cellphone-key"]];
    const kind = kinds.find(([match]) => source.includes(match));
    return {key: kind ? kind[0] : "other", icon: kind ? kind[1] : "mdi-key-chain-variant"};
}

/**
 * One registered multifactor device as a card: its name and provider, when it was last used and when it expires,
 * and what identifies it.
 *
 * @param {object} device the device as the endpoint answers it
 * @returns {string} the card markup
 */
function mfaDeviceCard(device) {
    const escape = escapePalantirMarkup;
    const kind = mfaDeviceKind(device);
    const id = String(device.id ?? "");
    const providerId = device?.details?.providerId ?? "Unknown";
    const name = device.name || id || "Unnamed device";
    const source = device.source || "Multifactor device";
    const lastUsed = palantirRecordDate(device.lastUsedDateTime);
    const expires = palantirRecordDate(device.expirationDateTime);
    const expired = expires !== null && expires.getTime() <= Date.now();
    const details = [["ID", id, "is-wrap"], ["Number", device.number, ""], ["Model", device.model, "is-text"],
        ["Last used", lastUsed ? palantirRecordWhen(lastUsed) : null, "is-text"]]
        .filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== "");
    return `
        <article class="palantir-record-card is-mfa${expired ? " is-expired" : ""}" data-mfa-device="${escape(id)}"
                 data-mfa-provider="${escape(providerId)}" aria-label="${escape(name)}">
            <header class="palantir-record-card__head">
                <span class="palantir-protocol-badge is-mfa" title="${escape(source)}"><i class="mdi ${kind.icon}" aria-hidden="true"></i></span>
                <div class="palantir-record-card__title">
                    <h4 title="${escape(name)}">${escape(name)}</h4>
                    <p>${escape(source)}${device.type ? ` · ${escape(device.type)}` : ""}</p>
                </div>
            </header>
            <ul class="palantir-record-card__flags">
                <li class="${lastUsed ? "is-on" : ""}"><i class="mdi mdi-history" aria-hidden="true"></i>${lastUsed
        ? `Used ${escape(palantirRecordAgo(lastUsed))}` : "No recorded use"}</li>
                <li class="${expired ? "is-alert" : expires ? "is-on" : ""}"><i class="mdi ${expired ? "mdi-alert-circle-outline" : "mdi-calendar-clock"}" aria-hidden="true"></i>${expired
        ? "Expired" : expires ? `Expires ${escape(palantirRecordWhen(expires))}` : "Does not expire"}</li>
            </ul>
            ${details.length > 0 ? `<dl class="palantir-record-card__details">${details.map(([label, value, style]) => `
                <div><dt>${label}</dt><dd class="${style}" title="${escape(value)}">${escape(value)}</dd></div>`).join("")}</dl>` : ""}
            <footer class="palantir-record-card__actions">
                <button type="button" class="palantir-button" data-mfa-action="copy"${id === "" ? " disabled" : ""}>
                    <i class="mdi mdi-content-copy" aria-hidden="true"></i>Copy ID
                </button>
                <button type="button" class="palantir-icon-button palantir-record-card__delete" data-mfa-action="remove"
                        data-palantir-write="mfaDevices" aria-label="Remove ${escape(name)}" title="Remove device">
                    <i class="mdi mdi-delete-outline" aria-hidden="true"></i>
                </button>
            </footer>
        </article>`;
}

async function initializeMultifactorOperations() {
    const mfaDevicesTable = $("#mfaDevicesTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        columnDefs: [
            {visible: false, targets: 8}
        ],
        drawCallback: function () {
            $("#mfaDevicesTable tr").addClass("mdc-data-table__row");
            $("#mfaDevicesTable td").addClass("mdc-data-table__cell");
            renderPalantirRecordCards(this.api(), "mfaDevicesCards", mfaDeviceCard,
                username => `No multifactor devices are registered for ${username}.`);
        }
    });
    placePalantirRecordCards("mfaDevicesTable", "mfaDevicesCards", "Search for a username to see their devices.");

    function removeMfaDevice({key, providerId, username}) {
        Swal.fire({
            title: "Are you sure you want to delete this entry?",
            text: "Once deleted, you may not be able to recover this entry.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}/${encodeURIComponent(providerId)}/${encodeURIComponent(key)}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: () => mfaDevicesTable
                            .rows((index, data) => data.key === key && data.providerId === providerId)
                            .remove()
                            .draw(),
                        error: (xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    }

    initializeDataTableContextMenu({
        table: mfaDevicesTable,
        selector: "#mfaDevicesTable tbody tr",
        items: {
            remove: {name: "Remove MFA Device", write: "mfaDevices", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeMfaDevice(context.rowData);
            }
        }
    });

    $("#mfaDevicesCards").off("click").on("click", "[data-mfa-action]", event => {
        const button = event.currentTarget;
        const card = button.closest("[data-mfa-device]");
        const row = mfaDevicesTable.rows((index, data) => String(data.key) === card.dataset.mfaDevice
            && data.providerId === card.dataset.mfaProvider).data().toArray()[0];
        if (!row) {
            return;
        }
        if (button.dataset.mfaAction === "copy") {
            copyToClipboard(String(row.key));
        } else if (button.dataset.mfaAction === "remove") {
            removeMfaDevice(row);
        }
    });

    $("#mfaDevicesButton").off().on("click", () => {
        function fetchMfaDevices() {
            const username = $("#mfaUsername").val();
            $("#mfaDevicesButton").prop("disabled", true);
            Swal.fire({
                icon: "info",
                title: `Fetching Devices for ${username}`,
                text: "Please wait while registered multifactor devices are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            mfaDevicesTable.clear();
            $.get(`${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}`, response => {
                for (const device of Object.values(response)) {
                    mfaDevicesTable.row.add({
                        0: `${palantirCode(device.name ?? "N/A")}`,
                        1: `${palantirCode(device.type ?? "N/A")}`,
                        2: `${palantirCode(device.id ?? "N/A")}`,
                        3: `${palantirCode(device.number ?? "N/A")}`,
                        4: `${palantirCode(device.model ?? "N/A")}`,
                        5: `${palantirCode(device.lastUsedDateTime ?? "N/A")}`,
                        6: `${palantirCode(device.expirationDateTime ?? "N/A")}`,
                        7: `${palantirCode(device.source ?? "N/A")}`,
                        8: palantirCode(device.payload ?? ""),
                        providerId: device?.details?.providerId ?? "Unknown",
                        key: device.id,
                        username: username,
                        record: device
                    });
                }
                $("#mfaDevicesCards").data("searched", username);
                mfaDevicesTable.draw();
                $("#mfaDevicesButton").prop("disabled", false);
                Swal.close();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                $("#mfaDevicesButton").prop("disabled", false);
                Swal.close();
            });
        }

        const fmMfaDevices = document.getElementById("fmMfaDevices");
        if (!fmMfaDevices.checkValidity()) {
            fmMfaDevices.reportValidity();
            return false;
        }

        fetchMfaDevices();
    });
    await populateMultifactorProviderTables();
}

async function initializeTrustedMultifactorOperations() {
    const mfaTrustedDevicesTable = $("#mfaTrustedDevicesTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        columnDefs: [
            {visible: false, targets: 7}
        ],
        drawCallback: settings => {
            $("#mfaTrustedDevicesTable tr").addClass("mdc-data-table__row");
            $("#mfaTrustedDevicesTable td").addClass("mdc-data-table__cell");
        }
    });

    function removeMfaTrustedDevice(context) {
        const key = context.rowData.key;
        Swal.fire({
            title: "Are you sure you want to delete this entry?",
            text: "Once deleted, you may not be able to recover this entry.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.multifactorTrustedDevices()}/${key}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: (response, status, xhr) => context.row.remove().draw(),
                        error: (xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    }

    initializeDataTableContextMenu({
        table: mfaTrustedDevicesTable,
        selector: "#mfaTrustedDevicesTable tbody tr",
        items: {
            remove: {name: "Remove Trusted MFA Device", write: "multifactorTrustedDevices", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeMfaTrustedDevice(context);
            }
        }
    });

    $("#mfaTrustedDevicesButton").off().on("click", () => {
        if (CasActuatorEndpoints.multifactorTrustedDevices()) {
            mfaTrustedDevicesTable.clear();
            const username = $("#mfaTrustedUsername").val();
            $("#mfaTrustedDevicesButton").prop("disabled", true);
            Swal.fire({
                icon: "info",
                title: `Fetching Multifactor Trusted Devices for ${username}`,
                text: "Please wait while registered multifactor trusted devices are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            $.get(`${CasActuatorEndpoints.multifactorTrustedDevices()}/${username}`, response => {
                for (const device of Object.values(response)) {
                    mfaTrustedDevicesTable.row.add({
                        0: `${palantirCode(device.id ?? "N/A")}`,
                        1: `${palantirCode(device.principal ?? "N/A")}`,
                        2: `${palantirCode(device.deviceFingerprint ?? "N/A")}`,
                        3: `${palantirCode(device.recordDate ?? "N/A")}`,
                        4: `${palantirCode(device.name ?? "N/A")}`,
                        5: `${palantirCode(device.expirationDate ?? "N/A")}`,
                        6: `${palantirCode(device.multifactorAuthenticationProvider ?? "N/A")}`,
                        7: `${palantirCode(device.recordKey ?? "N/A")}`,
                        key: device.recordKey
                    });
                }
                mfaTrustedDevicesTable.draw();
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();
            });
        }
    });

}

/**
 * The multifactor credential repositories: for each repository whose actuator endpoint is enabled, how many records
 * it holds, and a way to export them all, import an export or a single record, and remove them all.
 */
class PalantirCredentialRepositories {
    static REPOSITORIES = [
        {
            key: "gauthCredentialRepository",
            title: "Google Authenticator",
            records: "account",
            icon: "mdi-cellphone-key",
            endpoint: () => CasActuatorEndpoints.gauthCredentialRepository()
        },
        {
            key: "webAuthnDevices",
            title: "WebAuthn",
            records: "device registration",
            icon: "mdi-fingerprint",
            endpoint: () => CasActuatorEndpoints.webAuthnDevices()
        },
        {
            key: "yubikeyAccountRepository",
            title: "YubiKey",
            records: "account",
            icon: "mdi-usb-flash-drive-outline",
            endpoint: () => CasActuatorEndpoints.yubikeyAccountRepository()
        }
    ];

    static importing = null;

    static summaries = new Map();

    /**
     * What one repository holds, read once and shared by the provider overview and the repository card.
     *
     * @param repository the repository
     * @param fresh whether to ask CAS again rather than reuse the last answer
     * @returns {Promise<object>} the summary
     */
    static summary(repository, fresh = false) {
        if (fresh || !PalantirCredentialRepositories.summaries.has(repository.key)) {
            const request = Promise.resolve($.ajax({
                url: `${repository.endpoint()}?summary=true`,
                dataType: "json",
                headers: {"Accept": "application/json"}
            }));
            request.catch(() => PalantirCredentialRepositories.summaries.delete(repository.key));
            PalantirCredentialRepositories.summaries.set(repository.key, request);
        }
        return PalantirCredentialRepositories.summaries.get(repository.key);
    }

    /**
     * The repositories whose actuator endpoint is enabled.
     *
     * @returns {object[]} the repositories
     */
    static available() {
        return PalantirCredentialRepositories.REPOSITORIES.filter(repository => Boolean(repository.endpoint()));
    }

    static repository(key) {
        return PalantirCredentialRepositories.available().find(repository => repository.key === key);
    }

    static count(number, noun) {
        return `${number} ${noun}${number === 1 ? "" : "s"}`;
    }

    /**
     * The card of one repository, with its counts once they are known.
     *
     * @param repository the repository
     * @param summary what the repository holds, or null while it is read, or an error message
     * @returns {string} the card
     */
    static card(repository, summary) {
        const escape = escapePalantirMarkup;
        const failed = typeof summary === "string";
        const known = summary !== null && !failed;
        const subtitle = failed ? "The repository could not be read"
            : known ? `${PalantirCredentialRepositories.count(summary.records, repository.records)} · ${PalantirCredentialRepositories.count(summary.users, "user")}`
                : "Counting records…";
        const devices = known && summary.devices !== undefined
            ? `<li class="is-on"><i class="mdi mdi-usb-flash-drive-outline" aria-hidden="true"></i>${escape(PalantirCredentialRepositories.count(summary.devices, "device"))}</li>` : "";
        const empty = known && summary.records === 0;
        return `
            <article class="palantir-record-card is-mfa" data-mfa-repository="${escape(repository.key)}" aria-label="${escape(repository.title)}">
                <header class="palantir-record-card__head">
                    <span class="palantir-protocol-badge is-mfa"><i class="mdi ${repository.icon}" aria-hidden="true"></i></span>
                    <div class="palantir-record-card__title">
                        <h4>${escape(repository.title)}</h4>
                        <p>${escape(subtitle)}</p>
                    </div>
                </header>
                <ul class="palantir-record-card__flags">
                    <li class="${known && !empty ? "is-on" : ""}"><i class="mdi ${failed ? "mdi-alert-circle-outline" : "mdi-database-outline"}" aria-hidden="true"></i>${failed
            ? escape(summary) : empty ? "Empty" : known ? "Holds records" : "Reading"}</li>
                    ${devices}
                </ul>
                <footer class="palantir-record-card__actions">
                    <button type="button" class="palantir-button" data-mfa-repository-action="export"${empty ? " disabled" : ""}>
                        <i class="mdi mdi-download" aria-hidden="true"></i>Export
                    </button>
                    <button type="button" class="palantir-button" data-mfa-repository-action="import" data-palantir-write="${escape(repository.key)}">
                        <i class="mdi mdi-upload" aria-hidden="true"></i>Import
                    </button>
                    <button type="button" class="palantir-icon-button palantir-record-card__delete" data-mfa-repository-action="delete"
                            data-palantir-write="${escape(repository.key)}"${empty ? " disabled" : ""}
                            aria-label="Delete every ${escape(repository.title)} record" title="Delete all records">
                        <i class="mdi mdi-delete-sweep-outline" aria-hidden="true"></i>
                    </button>
                </footer>
            </article>`;
    }

    static draw(repository, summary) {
        const current = $(`#mfaRepositoriesCards [data-mfa-repository="${repository.key}"]`);
        const card = $(PalantirCredentialRepositories.card(repository, summary));
        if (current.length > 0) {
            current.replaceWith(card);
        } else {
            $("#mfaRepositoriesCards").append(card);
        }
        applyPalantirEndpointAccess(card[0]);
    }

    /**
     * Read what one repository holds and redraw its card.
     *
     * @param repository the repository
     * @param fresh whether to ask CAS again rather than reuse what the provider overview read
     * @returns {Promise<void>} resolved once the card is drawn
     */
    static async refresh(repository, fresh = true) {
        PalantirCredentialRepositories.draw(repository, null);
        try {
            const summary = await PalantirCredentialRepositories.summary(repository, fresh);
            (PalantirMfaProviders.providers ?? []).filter(provider => provider.repository === repository.key)
                .forEach(provider => {
                    provider.summary = {users: Number(summary?.users ?? 0), records: Number(summary?.records ?? 0)};
                });
            PalantirCredentialRepositories.draw(repository, {
                records: Number(summary?.records ?? 0),
                users: Number(summary?.users ?? 0),
                devices: summary?.devices === undefined ? undefined : Number(summary.devices)
            });
        } catch (xhr) {
            PalantirCredentialRepositories.draw(repository, xhr?.status ? `CAS answered ${xhr.status}` : "No answer from CAS");
        }
    }

    static async export(repository) {
        try {
            const response = await fetch(`${repository.endpoint()}/export`, {credentials: "include"});
            if (!response.ok) {
                throw new Error(`CAS answered ${response.status}`);
            }
            const disposition = response.headers.get("Content-Disposition") ?? "";
            const filename = disposition.match(/filename="?([^";]+)"?/)?.[1] ?? `${repository.key}.zip`;
            const blob = await response.blob();
            const link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(link.href);
        } catch (error) {
            Swal.fire({icon: "error", titleText: `${repository.title} Records Could Not Be Exported`, text: error.message});
        }
    }

    static chooseImport(repository) {
        PalantirCredentialRepositories.importing = repository;
        $("#mfaRepositoryImportFile").val("").trigger("click");
    }

    /**
     * Tell the operator how many records CAS accepted and how many it skipped.
     *
     * @param repository the repository
     * @param file the imported file
     * @param response the body CAS answered with
     */
    static reportImport(repository, file, response) {
        let counts = {};
        try {
            counts = JSON.parse(response || "{}") ?? {};
        } catch (e) {
            counts = {};
        }
        const imported = Number(counts.imported ?? 0);
        const skipped = Number(counts.skipped ?? 0);
        const records = repository.records;
        if (imported === 0) {
            Swal.fire({
                icon: "error",
                titleText: "Nothing Imported",
                text: skipped > 0
                    ? `CAS refused all ${PalantirCredentialRepositories.count(skipped, records)} in ${file.name}.`
                    : `${file.name} holds no ${repository.title} records.`
            });
        } else if (skipped > 0) {
            Swal.fire({
                icon: "warning",
                titleText: "Partly Imported",
                text: `${PalantirCredentialRepositories.count(imported, records)} imported into ${repository.title}; CAS refused ${PalantirCredentialRepositories.count(skipped, records)}.`
            });
        } else {
            Swal.fire({
                icon: "success",
                titleText: "Imported",
                text: `${PalantirCredentialRepositories.count(imported, records)} imported into ${repository.title}.`
            });
        }
    }

    /**
     * Import a zip of records, as the export writes it, or one JSON record, after the operator confirms.
     *
     * @param repository the repository
     * @param file the chosen file
     * @returns {Promise<void>} resolved once the records are imported or the operator declined
     */
    static async import(repository, file) {
        const archive = /\.zip$/i.test(file.name) || /zip/i.test(file.type);
        const result = await Swal.fire({
            titleText: `Import ${file.name} into ${repository.title}?`,
            text: archive
                ? `Every record in the archive is added to the ${repository.title} repository.`
                : `The record is added to the ${repository.title} repository.`,
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Import"
        });
        if (!result.isConfirmed) {
            return;
        }
        PalantirProgress.open(`Importing ${repository.title} records`, `Uploading ${file.name}...`,
            `${file.name} · ${PalantirProgress.size(file.size)}`);
        try {
            const response = await $.ajax({
                url: `${repository.endpoint()}/import`,
                type: "POST",
                contentType: archive ? "application/zip" : "application/json",
                data: file,
                processData: false,
                dataType: "text"
            });
            PalantirProgress.close();
            PalantirCredentialRepositories.reportImport(repository, file, archive ? response : "{\"imported\":1}");
        } catch (xhr) {
            PalantirProgress.close();
            if (xhr?.status === 422) {
                PalantirCredentialRepositories.reportImport(repository, file, xhr.responseText);
            } else if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                Swal.fire({
                    icon: "error",
                    titleText: "Nothing Imported",
                    text: xhr?.status === 413
                        ? `${file.name} is too large to import: a record, or the records together, exceed what CAS accepts.`
                        : `CAS could not import ${file.name} into ${repository.title}${xhr?.status ? ` (it answered ${xhr.status})` : ""}.`
                });
            }
        }
        await PalantirCredentialRepositories.refresh(repository);
    }

    /**
     * Remove every record of a repository once the operator confirms it.
     *
     * @param repository the repository
     * @returns {Promise<void>} resolved once the records are removed or the operator declined
     */
    static async deleteAll(repository) {
        const result = await Swal.fire({
            titleText: `Delete Every ${repository.title} Record?`,
            html: `<p>Every user registered with ${escapePalantirMarkup(repository.title)} has to register again, and this
                   cannot be undone. Export the records first to keep a copy.</p>`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Delete All",
            cancelButtonText: "Cancel",
            focusCancel: true
        });
        if (!result.isConfirmed) {
            return;
        }
        try {
            await $.ajax({url: repository.endpoint(), type: "DELETE"});
            notyf.success(`Every ${repository.title} record is deleted.`);
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
        }
        await PalantirCredentialRepositories.refresh(repository);
    }

    static initialize() {
        const repositories = PalantirCredentialRepositories.available();
        $("#mfaRepositoriesCards").empty();
        repositories.forEach(repository => PalantirCredentialRepositories.refresh(repository, false));
        $("#mfaRepositoriesCards").off("click").on("click", "[data-mfa-repository-action]", event => {
            const button = event.currentTarget;
            const repository = PalantirCredentialRepositories.repository(button.closest("[data-mfa-repository]")?.dataset.mfaRepository);
            if (!repository) {
                return;
            }
            const actions = {
                export: () => PalantirCredentialRepositories.export(repository),
                import: () => PalantirCredentialRepositories.chooseImport(repository),
                delete: () => PalantirCredentialRepositories.deleteAll(repository)
            };
            actions[button.dataset.mfaRepositoryAction]?.();
        });
        $("#mfaRepositoryImportFile").off("change").on("change", function () {
            const file = this.files?.[0];
            const repository = PalantirCredentialRepositories.importing;
            PalantirCredentialRepositories.importing = null;
            if (file && repository) {
                PalantirCredentialRepositories.import(repository, file);
            }
        });
    }
}
