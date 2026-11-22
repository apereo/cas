/**
 * Turning applications off and on.
 *
 * Only the default access strategy has an on/off switch. An application with any other strategy is disabled by
 * combining its strategy with a disabled default strategy, so that both must allow access; enabling it removes that
 * again and leaves the strategy as it was.
 */
class PalantirApplicationAccess {
    static DEFAULT = "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy";

    static CHAINING = "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy";

    /**
     * The entries of a list as CAS writes it, either plain or wrapped with its Java type.
     *
     * @param value the list
     * @returns {Array} the entries
     */
    static entries(value) {
        if (Array.isArray(value) && value.length === 2 && typeof value[0] === "string" && Array.isArray(value[1])) {
            return value[1];
        }
        return Array.isArray(value) ? value : [];
    }

    static isChain(strategy) {
        return strategy?.["@class"] === PalantirApplicationAccess.CHAINING;
    }

    static isAndChain(strategy) {
        return PalantirApplicationAccess.isChain(strategy) && (strategy.operator ?? "AND") === "AND";
    }

    /**
     * Whether an access strategy turns the application off.
     *
     * @param strategy the access strategy
     * @returns {boolean} true for a default strategy turned off, an AND chain holding one, or an OR chain of only those
     */
    static isDisabled(strategy) {
        if (!strategy) {
            return false;
        }
        if (strategy["@class"] === PalantirApplicationAccess.DEFAULT) {
            return strategy.enabled === false;
        }
        if (PalantirApplicationAccess.isChain(strategy)) {
            const strategies = PalantirApplicationAccess.entries(strategy.strategies);
            return PalantirApplicationAccess.isAndChain(strategy)
                ? strategies.some(PalantirApplicationAccess.isDisabled)
                : strategies.length > 0 && strategies.every(PalantirApplicationAccess.isDisabled);
        }
        return false;
    }

    static isServiceDisabled(serviceId) {
        return PalantirApplicationAccess.isDisabled(registeredServicesById.get(String(serviceId))?.accessStrategy);
    }

    /**
     * Whether disabling an application with this strategy combines it with a disabled default strategy.
     *
     * @param strategy the access strategy
     * @returns {boolean} true when the strategy has no switch of its own
     */
    static wraps(strategy) {
        return Boolean(strategy) && strategy["@class"] !== PalantirApplicationAccess.DEFAULT
            && !PalantirApplicationAccess.isAndChain(strategy);
    }

    /**
     * The access strategy that turns the application off.
     *
     * @param strategy the current access strategy, if any
     * @returns {object} the disabled access strategy
     */
    static disable(strategy) {
        const off = {"@class": PalantirApplicationAccess.DEFAULT, enabled: false};
        if (!strategy) {
            return off;
        }
        if (strategy["@class"] === PalantirApplicationAccess.DEFAULT) {
            return {...strategy, enabled: false};
        }
        if (PalantirApplicationAccess.isAndChain(strategy)) {
            return {...strategy, strategies: ["java.util.ArrayList", [off, ...PalantirApplicationAccess.entries(strategy.strategies)]]};
        }
        const chain = {
            "@class": PalantirApplicationAccess.CHAINING,
            operator: "AND",
            strategies: ["java.util.ArrayList", [off, strategy]]
        };
        if (strategy.unauthorizedRedirectUrl) {
            chain.unauthorizedRedirectUrl = strategy.unauthorizedRedirectUrl;
        }
        return chain;
    }

    /**
     * The access strategy that turns the application on again: a default strategy is switched on, the disabled
     * default strategy added when the application was disabled is removed, and a chain left with one strategy gives
     * way to it. Inside a chain, only the strategies that keep the application off are turned on; a strategy that
     * already lets people in, such as an OR chain with one branch switched off, is left as it was, so enabling never
     * allows more than the strategy did before the application was disabled.
     *
     * @param strategy the current access strategy
     * @returns {object} the enabled access strategy
     */
    static enable(strategy) {
        if (!strategy) {
            return strategy;
        }
        if (strategy["@class"] === PalantirApplicationAccess.DEFAULT) {
            return {...strategy, enabled: true};
        }
        if (!PalantirApplicationAccess.isChain(strategy)) {
            return strategy;
        }
        const added = entry => entry?.["@class"] === PalantirApplicationAccess.DEFAULT && entry.enabled === false
            && Object.keys(entry).every(key => key === "@class" || key === "enabled");
        const kept = PalantirApplicationAccess.entries(strategy.strategies)
            .filter(entry => !added(entry))
            .map(entry => PalantirApplicationAccess.isDisabled(entry) ? PalantirApplicationAccess.enable(entry) : entry);
        if (kept.length === 0) {
            return {"@class": PalantirApplicationAccess.DEFAULT, enabled: true};
        }
        if (kept.length === 1) {
            const only = {...kept[0]};
            if (strategy.unauthorizedRedirectUrl && !only.unauthorizedRedirectUrl) {
                only.unauthorizedRedirectUrl = strategy.unauthorizedRedirectUrl;
            }
            return only;
        }
        return {...strategy, strategies: ["java.util.ArrayList", kept]};
    }

    /**
     * Disable or enable an application once the operator confirms: read it again, change its access strategy, save
     * it and reload the applications.
     *
     * @param serviceId the numeric id of the application
     * @param disable true to disable it, false to enable it
     * @returns {Promise<void>} resolved once the change is saved or declined
     */
    static async change(serviceId, disable) {
        const known = registeredServicesById.get(String(serviceId)) ?? {};
        const name = known.name ?? String(serviceId);
        const strategyName = getLastWord(known.accessStrategy?.["@class"] ?? PalantirApplicationAccess.DEFAULT)
            .replace(/(Registered)?ServiceAccessStrategy$/, "");
        const wraps = disable && PalantirApplicationAccess.wraps(known.accessStrategy);
        const result = await Swal.fire({
            titleText: disable ? `Disable ${name}?` : `Enable ${name}?`,
            text: disable
                ? `Nobody can sign in to this application until it is enabled again.${wraps
                    ? ` Its ${strategyName} access strategy has no switch of its own, so it is combined with a disabled default strategy; enabling the application removes that again.`
                    : ""}`
                : "People can sign in to this application again, as its access strategy allows.",
            icon: disable ? "warning" : "question",
            showCancelButton: true,
            confirmButtonText: disable ? "Disable" : "Enable",
            focusCancel: disable
        });
        if (!result.isConfirmed) {
            return;
        }
        try {
            await PalantirApplicationAccess.save(serviceId, disable);
            await PalantirApplicationSelection.reload();
            notyf.success(`${name} is ${disable ? "disabled" : "enabled"}.`);
        } catch (xhr) {
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                showUnreadableDefinition(xhr, `${name} was not ${disable ? "disabled" : "enabled"}`);
            }
        }
    }

    /**
     * Read an application again, switch its access strategy off or on and save it.
     *
     * @param serviceId the numeric id of the application
     * @param disable true to disable it, false to enable it
     * @returns {Promise<void>} resolved once the application is saved
     */
    static async save(serviceId, disable) {
        const endpoint = CasActuatorEndpoints.registeredServices();
        const service = await $.ajax({
            url: `${endpoint}/${encodeURIComponent(serviceId)}`,
            dataType: "json",
            headers: {"Accept": "application/json"}
        });
        service.accessStrategy = disable
            ? PalantirApplicationAccess.disable(service.accessStrategy)
            : PalantirApplicationAccess.enable(service.accessStrategy);
        await $.ajax({
            url: endpoint,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(service)
        });
    }

    /**
     * Disable or enable the selected applications once the operator confirms, one by one, showing the progress.
     * Applications that are already in that state are left alone.
     *
     * @param disable true to disable them, false to enable them
     * @returns {Promise<void>} resolved once they are saved or the operator declined
     */
    static async changeSelected(disable) {
        const ids = [...PalantirApplicationSelection.selected]
            .filter(id => PalantirApplicationAccess.isServiceDisabled(id) !== disable);
        if (ids.length === 0) {
            return;
        }
        const verb = disable ? "Disable" : "Enable";
        const names = ids.map(id => registeredServicesById.get(id)?.name ?? id);
        const listed = names.slice(0, 8).map(name => `<li>${escapePalantirMarkup(name)}</li>`).join("");
        const more = names.length > 8 ? `<li>and ${names.length - 8} more</li>` : "";
        const skipped = PalantirApplicationSelection.selected.size - ids.length;
        const note = skipped > 0
            ? `<p>${skipped} selected application${skipped === 1 ? " is" : "s are"} already ${disable ? "disabled" : "enabled"} and ${skipped === 1 ? "is" : "are"} left as ${skipped === 1 ? "it is" : "they are"}.</p>`
            : "";
        const result = await Swal.fire({
            titleText: `${verb} ${ids.length} Application${ids.length === 1 ? "" : "s"}?`,
            html: `<p>${disable
                ? "Nobody can sign in to these applications until they are enabled again."
                : "People can sign in to these applications again, as their access strategies allow."}</p>
                   <ul class="palantir-selection-list">${listed}${more}</ul>${note}`,
            icon: disable ? "warning" : "question",
            showCancelButton: true,
            confirmButtonText: `${verb} ${ids.length}`,
            focusCancel: disable
        });
        if (!result.isConfirmed) {
            return;
        }
        const failed = [];
        const progressive = disable ? "Disabling" : "Enabling";
        PalantirProgress.open(`${progressive} applications`, `${progressive} ${names[0]}...`, `${ids.length} selected`, 0);
        for (let index = 0; index < ids.length; index++) {
            PalantirProgress.update(`${progressive} ${names[index]} (${index + 1} of ${ids.length})...`, index / ids.length * 100);
            try {
                await PalantirApplicationAccess.save(ids[index], disable);
            } catch (xhr) {
                failed.push(names[index]);
            }
        }
        PalantirProgress.update("Refreshing the applications...", null);
        await PalantirApplicationSelection.reload();
        PalantirProgress.close();
        if (failed.length === 0) {
            PalantirApplicationSelection.clear();
        }
        const changed = ids.length - failed.length;
        Swal.fire({
            icon: failed.length === 0 ? "success" : "warning",
            titleText: `${changed} Application${changed === 1 ? "" : "s"} ${disable ? "Disabled" : "Enabled"}`,
            text: failed.length === 0 ? "" : `These were not changed, and the selection is kept: ${failed.join(", ")}.`
        });
    }
}

/**
 * Selecting applications, in the table or as cards, and deleting them: the selected ones, or every application.
 * Once an action on the selection succeeds for every selected application, the selection is cleared and its bar goes
 * away; when it fails for any of them, the selection is kept so the action can be tried again.
 */
class PalantirApplicationSelection {
    static selected = new Set();

    static listening = false;

    static has(serviceId) {
        return PalantirApplicationSelection.selected.has(String(serviceId));
    }

    /**
     * The checkbox that selects an application.
     *
     * @param serviceId the numeric id of the application
     * @param name the application's name
     * @returns {string} the markup
     */
    static checkbox(serviceId, name) {
        return `<input type="checkbox" class="palantir-application-select palantir-sr-only" data-select-service="${escapePalantirMarkup(serviceId)}"
                       aria-label="Select ${escapePalantirMarkup(name)}"${PalantirApplicationSelection.has(serviceId) ? " checked" : ""}>`;
    }

    static set(serviceId, selected) {
        if (selected) {
            PalantirApplicationSelection.selected.add(String(serviceId));
        } else {
            PalantirApplicationSelection.selected.delete(String(serviceId));
        }
        PalantirApplicationSelection.refresh();
    }

    static last = null;

    /**
     * Select or deselect the application of a card or a row that was clicked. A click on a control inside it, such
     * as a button, link or menu, or one that ends a text selection, is left alone; Shift extends the selection from
     * the application clicked last to this one, in the order the applications are shown.
     *
     * @param event the click
     */
    static toggleFromClick(event) {
        if ($(event.target).closest("a, button, input, select, textarea, label, [data-application-action]").length > 0
            || (!event.shiftKey && String(window.getSelection?.() ?? "").length > 0)) {
            return;
        }
        const checkbox = $(event.currentTarget).find("[data-select-service]").first();
        if (checkbox.length === 0) {
            return;
        }
        const id = String(checkbox[0].dataset.selectService);
        const shown = PalantirApplicationSelection.shown();
        const from = shown.indexOf(PalantirApplicationSelection.last ?? "");
        const to = shown.indexOf(id);
        if (event.shiftKey && from >= 0 && to >= 0) {
            const select = !PalantirApplicationSelection.has(id);
            shown.slice(Math.min(from, to), Math.max(from, to) + 1)
                .forEach(entry => select ? PalantirApplicationSelection.selected.add(entry) : PalantirApplicationSelection.selected.delete(entry));
            window.getSelection?.()?.removeAllRanges();
            PalantirApplicationSelection.refresh();
        } else {
            PalantirApplicationSelection.set(id, !PalantirApplicationSelection.has(id));
        }
        PalantirApplicationSelection.last = id;
    }

    static clear() {
        PalantirApplicationSelection.selected.clear();
        PalantirApplicationSelection.refresh();
    }

    /**
     * The ids of the applications the search and type filter show, on every page.
     *
     * @returns {string[]} the ids
     */
    static shown() {
        if (!$.fn.dataTable.isDataTable("#applicationsTable")) {
            return [];
        }
        return $("#applicationsTable").DataTable().rows({search: "applied"}).data().toArray().map(row => String(row.serviceId));
    }

    /**
     * Select every application shown, or clear them when all of them already are.
     */
    static toggleShown() {
        const shown = PalantirApplicationSelection.shown();
        const all = shown.length > 0 && shown.every(id => PalantirApplicationSelection.selected.has(id));
        shown.forEach(id => all ? PalantirApplicationSelection.selected.delete(id) : PalantirApplicationSelection.selected.add(id));
        PalantirApplicationSelection.refresh();
    }

    /**
     * Bring the checkboxes, rows, cards and the selection bar in line with the selection, dropping applications that
     * are no longer registered.
     */
    static refresh() {
        for (const id of [...PalantirApplicationSelection.selected]) {
            if (!registeredServicesById.has(id)) {
                PalantirApplicationSelection.selected.delete(id);
            }
        }
        $("[data-select-service]").each((index, checkbox) => {
            const selected = PalantirApplicationSelection.has(checkbox.dataset.selectService);
            checkbox.checked = selected;
            $(checkbox).closest("tr, .palantir-application-card").toggleClass("is-checked", selected);
        });
        const shown = PalantirApplicationSelection.shown();
        const count = shown.filter(id => PalantirApplicationSelection.selected.has(id)).length;
        const header = document.getElementById("applicationsSelectAll");
        if (header) {
            header.checked = shown.length > 0 && count === shown.length;
            header.indeterminate = count > 0 && count < shown.length;
        }
        const total = PalantirApplicationSelection.selected.size;
        const bar = $("#applicationsSelectionBar");
        const page = document.getElementById(`attribute-tab-${Tabs.APPLICATIONS.index}`);
        const pageShown = page !== null && page.offsetParent !== null;
        bar.prop("hidden", total === 0 || !pageShown);
        document.body.classList.toggle("palantir-has-selection-bar", !bar.prop("hidden"));
        if (!bar.prop("hidden")) {
            const covered = Math.max(0, ...[...document.querySelectorAll("footer")]
                .filter(element => element.offsetParent !== null)
                .map(element => element.getBoundingClientRect())
                .filter(bounds => bounds.top < window.innerHeight && bounds.bottom >= window.innerHeight - 1)
                .map(bounds => window.innerHeight - bounds.top));
            bar[0].style.setProperty("--palantir-selection-bar-offset", `${Math.round(covered) + 16}px`);
            document.body.style.setProperty("--palantir-selection-bar-space", `${Math.round(covered) + 16 + bar.outerHeight() + 12}px`);
        }
        const writable = CasActuatorEndpoints.writable("registeredServices");
        const selectedIds = [...PalantirApplicationSelection.selected];
        bar.find("button[name=disableSelectedServices]")
            .prop("disabled", !writable || !selectedIds.some(id => !PalantirApplicationAccess.isServiceDisabled(id)));
        bar.find("button[name=enableSelectedServices]")
            .prop("disabled", !writable || !selectedIds.some(id => PalantirApplicationAccess.isServiceDisabled(id)));
        bar.find(".palantir-selection-bar__count").text(`${total} application${total === 1 ? "" : "s"} selected`);
    }

    /**
     * Read the applications again once they were changed, and wait for them. When they cannot be read, the change
     * itself was still made: the operator is told the list may be out of date rather than kept waiting.
     *
     * @returns {Promise<boolean>} resolved once the applications are read again, with whether they could be
     */
    static async reload() {
        const refreshed = await fetchServices(() => updateNavigationSidebar());
        PalantirApplicationSelection.refresh();
        if (refreshed === false) {
            notyf.error("The change is saved, but the applications could not be read again; the list may be out of date.");
        }
        return refreshed !== false;
    }

    /**
     * Download the selected applications: the definition itself when one is selected, otherwise a zip of their
     * definitions that Import accepts as it is.
     *
     * @returns {Promise<void>} resolved once the download is handed to the browser
     */
    static async exportSelected() {
        const ids = [...PalantirApplicationSelection.selected];
        if (ids.length === 0 || !CasActuatorEndpoints.registeredServices()) {
            return;
        }
        const url = ids.length === 1
            ? `${CasActuatorEndpoints.registeredServices()}/export/${encodeURIComponent(ids[0])}`
            : `${CasActuatorEndpoints.registeredServices()}/export?${ids.map(id => `ids=${encodeURIComponent(id)}`).join("&")}`;
        const button = $("button[name=exportSelectedServices]").prop("disabled", true);
        try {
            const response = await fetch(url, {credentials: "include"});
            if (!response.ok) {
                throw new Error(`CAS answered ${response.status} ${response.statusText}`.trim());
            }
            const blob = await response.blob();
            const link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = response.headers.get("filename") || (ids.length === 1 ? `${ids[0]}.json` : "services.zip");
            document.body.appendChild(link);
            link.click();
            link.remove();
            setTimeout(() => window.URL.revokeObjectURL(link.href), 1000);
            notyf.success(`Exported ${ids.length} application${ids.length === 1 ? "" : "s"}.`);
            PalantirApplicationSelection.clear();
        } catch (error) {
            Swal.fire({icon: "error", titleText: "The Selected Applications Were Not Exported", text: String(error?.message ?? error)});
        } finally {
            button.prop("disabled", false);
        }
    }

    /**
     * Delete the selected applications once the operator confirms, one by one, showing the progress.
     *
     * @returns {Promise<void>} resolved once they are deleted or the operator declined
     */
    static async deleteSelected() {
        const ids = [...PalantirApplicationSelection.selected];
        if (ids.length === 0) {
            return;
        }
        const names = ids.map(id => registeredServicesById.get(id)?.name ?? id);
        const listed = names.slice(0, 8).map(name => `<li>${escapePalantirMarkup(name)}</li>`).join("");
        const more = names.length > 8 ? `<li>and ${names.length - 8} more</li>` : "";
        const result = await Swal.fire({
            titleText: `Delete ${ids.length} Application${ids.length === 1 ? "" : "s"}?`,
            html: `<p>These applications are removed from the service registry. This cannot be undone.</p>
                   <ul class="palantir-selection-list">${listed}${more}</ul>`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: `Delete ${ids.length}`,
            focusCancel: true
        });
        if (!result.isConfirmed) {
            return;
        }
        const failed = [];
        PalantirProgress.open("Deleting applications", `Deleting ${names[0]}...`, `${ids.length} selected`, 0);
        for (let index = 0; index < ids.length; index++) {
            PalantirProgress.update(`Deleting ${names[index]} (${index + 1} of ${ids.length})...`, index / ids.length * 100);
            try {
                await $.ajax({
                    url: `${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(ids[index])}`,
                    type: "DELETE",
                    headers: {"Content-Type": "application/json"}
                });
                PalantirApplicationSelection.selected.delete(ids[index]);
            } catch (xhr) {
                failed.push(names[index]);
            }
        }
        PalantirProgress.update("Refreshing the applications...", null);
        await PalantirApplicationSelection.reload();
        PalantirProgress.close();
        if (failed.length === 0) {
            PalantirApplicationSelection.clear();
        }
        const deleted = ids.length - failed.length;
        Swal.fire({
            icon: failed.length === 0 ? "success" : "warning",
            titleText: `${deleted} Application${deleted === 1 ? "" : "s"} Deleted`,
            text: failed.length === 0 ? "" : `These were not deleted and stay selected: ${failed.join(", ")}.`
        });
    }

    /**
     * Delete every application from the service registry once the operator confirms.
     *
     * @returns {Promise<void>} resolved once they are deleted or the operator declined
     */
    static async deleteAll() {
        const count = registeredServicesById.size;
        if (count === 0) {
            Swal.fire({icon: "info", titleText: "No Applications Are Registered"});
            return;
        }
        const result = await Swal.fire({
            titleText: `Delete All ${count} Application${count === 1 ? "" : "s"}?`,
            text: "Every application is removed from the service registry, including its stored definition, and nobody can "
                + "sign in to any of them. This cannot be undone; export them first if you may need them.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Yes",
            cancelButtonText: "Cancel",
            focusCancel: true
        });
        if (!result.isConfirmed) {
            return;
        }
        PalantirProgress.open("Deleting all applications", "CAS is removing every application from the service registry...",
            `${count} application${count === 1 ? "" : "s"}`);
        try {
            await $.ajax({
                url: CasActuatorEndpoints.registeredServices(),
                type: "DELETE",
                headers: {"Content-Type": "application/json"}
            });
            PalantirProgress.update("Refreshing the applications...");
            PalantirApplicationSelection.selected.clear();
            await PalantirApplicationSelection.reload();
            PalantirProgress.close();
            Swal.fire({icon: "success", titleText: "All Applications Are Deleted"});
        } catch (xhr) {
            PalantirProgress.close();
            if (xhr?.statusText !== PALANTIR_READ_ONLY) {
                displayBanner(xhr);
            }
        }
    }

    /**
     * Wire the selection: the checkboxes of rows and cards, the header checkbox, the selection bar and Delete All.
     * Their changes are taken before they reach the table, whose Material component would take them for its own
     * row checkboxes; a click on the header checkbox does not sort the column.
     */
    static initialize() {
        if (!PalantirApplicationSelection.listening) {
            PalantirApplicationSelection.listening = true;
            document.addEventListener("change", event => {
                const target = event.target;
                if (!(target instanceof HTMLInputElement) || !target.classList.contains("palantir-application-select")) {
                    return;
                }
                event.stopPropagation();
                if (target.id === "applicationsSelectAll") {
                    PalantirApplicationSelection.toggleShown();
                } else {
                    PalantirApplicationSelection.set(target.dataset.selectService, target.checked);
                }
            }, true);
        }
        const bar = document.getElementById("applicationsSelectionBar");
        if (bar && bar.parentElement !== document.body) {
            document.body.appendChild(bar);
        }
        $(document)
            .off("click.palantirSelectionPage")
            .on("click.palantirSelectionPage", "nav.sidebar-navigation [data-tab-index]",
                () => setTimeout(() => PalantirApplicationSelection.refresh(), 0));
        $(document)
            .off("click.palantirSelection")
            .on("click.palantirSelection", "#applicationsCards .palantir-application-card, #applicationsTable tbody tr",
                event => PalantirApplicationSelection.toggleFromClick(event));
        $("#applicationsSelectAll").off("click.palantirSort keydown.palantirSort")
            .on("click.palantirSort keydown.palantirSort", event => event.stopPropagation());
        $("button[name=exportSelectedServices]").off("click").on("click", () => PalantirApplicationSelection.exportSelected());
        $("button[name=disableSelectedServices]").off("click").on("click", () => PalantirApplicationAccess.changeSelected(true));
        $("button[name=enableSelectedServices]").off("click").on("click", () => PalantirApplicationAccess.changeSelected(false));
        $("button[name=deleteSelectedServices]").off("click").on("click", () => PalantirApplicationSelection.deleteSelected());
        $("button[name=clearServiceSelection]").off("click").on("click", () => PalantirApplicationSelection.clear());
        $("button[name=deleteAllServices]").off("click").on("click", () => PalantirApplicationSelection.deleteAll());
        PalantirApplicationSelection.refresh();
    }
}

/**
 * Rotating the client secrets of an OAuth or OpenID Connect application from its context menu, through the client
 * secrets endpoint: every secret, or only the expired ones, is replaced by a new random secret that expires after the
 * chosen duration. The new secrets are shown once, to be handed to the application, and the applications are read
 * again. A public client, without secrets, is told so instead.
 */
class PalantirClientSecretRotation {
    static DEFAULT_EXPIRATION = "P90D";

    static applies(service) {
        const type = String(service?.["@class"] ?? "");
        return (type.includes("OAuthRegisteredService") || type.includes("OidcRegisteredService"))
            && Boolean(CasActuatorEndpoints.oauthClientSecrets());
    }

    /**
     * The address that rotates the secrets of a client.
     *
     * @param clientId the client id
     * @param options expiredOnly to replace only expired secrets, expireIn for how long the new secrets last
     * @returns {string} the endpoint address with its query
     */
    static url(clientId, {expiredOnly = false, expireIn = ""} = {}) {
        const encodedClientId = encodeURIComponent(clientId);
        let endpoint = CasActuatorEndpoints.oauthClientSecrets();
        if (/\{clientId\}|%7BclientId%7D/i.test(endpoint)) {
            endpoint = endpoint.replace(/\{clientId\}|%7BclientId%7D/ig, encodedClientId);
        } else {
            endpoint = `${endpoint}/${encodedClientId}`;
        }
        const query = new URLSearchParams({expiredOnly: String(expiredOnly)});
        if (String(expireIn ?? "").trim()) {
            query.append("expireIn", String(expireIn).trim());
        }
        return `${endpoint}?${query}`;
    }

    static secrets(service) {
        return PalantirApplicationAccess.entries(service?.clientSecrets)
            .map(secret => typeof secret === "string" ? {value: secret} : secret)
            .filter(secret => secret?.value);
    }

    /**
     * Whether a duration is one CAS reads: an ISO-8601 duration such as P90D or PT12H, or a number of seconds.
     *
     * @param value the duration
     * @returns {boolean} true when it can be sent
     */
    static validDuration(value) {
        return /^(P(?=\d|T\d)(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(?=\d)(\d+H)?(\d+M)?(\d+(\.\d+)?S)?)?|\d+)$/i.test(String(value ?? "").trim());
    }

    static async open(serviceId) {
        let service;
        try {
            service = await $.get(`${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(serviceId)}`);
        } catch (xhr) {
            displayBanner(xhr);
            return;
        }
        const name = service?.name ?? String(serviceId);
        const secrets = PalantirClientSecretRotation.secrets(service);
        if (!service?.clientId || secrets.length === 0) {
            await Swal.fire({
                icon: "info",
                titleText: "This Application Has No Client Secrets",
                text: `${name} is a public client, so it has no secrets to rotate.`
            });
            return;
        }
        const result = await Swal.fire({
            titleText: `Rotate Client Secrets of ${name}?`,
            html: `
                <form class="palantir-client-secret-rotation" onsubmit="return false;">
                    <p>Each secret is replaced by a new one at once; ${escapePalantirMarkup(service.clientId)} stops working with the
                       replaced secrets until it is given the new ones.</p>
                    <label class="palantir-feature-switch" for="clientSecretRotationExpiredOnly">
                        <input type="checkbox" role="switch" id="clientSecretRotationExpiredOnly">
                        <span class="palantir-feature-switch__track" aria-hidden="true"></span>
                        <span class="palantir-feature-switch__text">
                            <strong>Only Expired Secrets</strong>
                            <small>Keep the secrets that are still valid and replace only the expired ones.</small>
                        </span>
                    </label>
                    <label for="clientSecretRotationExpireIn">New Secrets Expire In</label>
                    <input id="clientSecretRotationExpireIn" class="swal2-input" autocomplete="off" spellcheck="false"
                           value="${PalantirClientSecretRotation.DEFAULT_EXPIRATION}" placeholder="${PalantirClientSecretRotation.DEFAULT_EXPIRATION}">
                    <p class="palantir-client-secret-rotation__hint">A duration such as P90D or PT12H, or a number of seconds.</p>
                </form>`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Rotate Secrets",
            focusConfirm: false,
            preConfirm: () => {
                const expireIn = String($("#clientSecretRotationExpireIn").val() ?? "").trim() || PalantirClientSecretRotation.DEFAULT_EXPIRATION;
                if (!PalantirClientSecretRotation.validDuration(expireIn)) {
                    Swal.showValidationMessage("Enter a duration such as P90D or PT12H, or a number of seconds.");
                    return false;
                }
                return {expireIn, expiredOnly: $("#clientSecretRotationExpiredOnly").prop("checked") === true};
            }
        });
        if (!result.isConfirmed) {
            return;
        }
        let updated;
        try {
            updated = await $.ajax({
                url: PalantirClientSecretRotation.url(service.clientId, result.value),
                type: "POST",
                headers: {"Content-Type": "application/json"}
            });
        } catch (xhr) {
            displayBanner(xhr);
            return;
        }
        const before = new Set(secrets.map(secret => secret.value));
        const fresh = PalantirClientSecretRotation.secrets(updated).filter(secret => !before.has(secret.value));
        await fetchServices();
        await PalantirClientSecretRotation.show(name, fresh);
    }

    static async show(name, fresh) {
        if (fresh.length === 0) {
            await Swal.fire({
                icon: "info",
                titleText: "No Secrets Were Rotated",
                text: `None of the secrets of ${name} had expired, so they were all kept.`
            });
            return;
        }
        const rows = fresh.map((secret, index) => `
            <li class="palantir-client-secret-rotation__secret">
                <code>${escapePalantirMarkup(secret.value)}</code>
                <button type="button" class="mdc-button mdc-button--outlined" data-client-secret="${index}">
                    <i class="mdi mdi-content-copy" aria-hidden="true"></i><span class="mdc-button__label">Copy</span>
                </button>
                ${secret.expiration ? `<small>Expires ${escapePalantirMarkup(palantirRecordDate(secret.expiration)
        ? palantirRecordWhen(palantirRecordDate(secret.expiration)) : secret.expiration)}</small>` : ""}
            </li>`).join("");
        await Swal.fire({
            icon: "success",
            titleText: `${fresh.length} Client Secret${fresh.length === 1 ? "" : "s"} Rotated`,
            html: `<p>Give the new secret${fresh.length === 1 ? "" : "s"} to ${escapePalantirMarkup(name)} now; they are not shown again.</p>
                   <ul class="palantir-client-secret-rotation__secrets">${rows}</ul>`,
            didOpen: popup => {
                $(popup).on("click", "[data-client-secret]", event => {
                    const secret = fresh[Number(event.currentTarget.dataset.clientSecret)];
                    copyToClipboard(secret.value).then(() => notyf.success("Copied the client secret."));
                });
            }
        });
    }
}
