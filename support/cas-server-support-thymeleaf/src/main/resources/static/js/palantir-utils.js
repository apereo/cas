/**
 * Build the group header row a DataTables draw callback inserts before the first row of each group.
 *
 * @param {string} label the group name, set as text
 * @param {number} columns the number of columns the row spans
 * @returns {jQuery} the row
 */
/**
 * The color of a health check by its status: green when up; amber when it reports a warning, is still running
 * (SLOW, as Palantir's overview reports a check that has not answered in time) or is unknown; red only when it is
 * down or out of service.
 *
 * @param {string} status the status of the check
 * @returns {string} the color
 */
function palantirHealthColor(status) {
    const value = String(status ?? "").toUpperCase();
    if (value === "UP") {
        return "rgb(5, 166, 31)";
    }
    if (value === "DOWN" || value === "OUT_OF_SERVICE") {
        return "rgba(166, 45, 15)";
    }
    return value === "UNKNOWN" ? "rgb(128, 140, 152)" : "rgb(201, 154, 6)";
}

/**
 * How a health check's status reads in a tooltip.
 *
 * @param {string} status the status of the check
 * @param {number|undefined} millis how long the check took, when known
 * @returns {string} the description
 */
function palantirHealthDescription(status, millis) {
    const value = String(status ?? "").toUpperCase();
    const took = Number.isFinite(Number(millis)) ? ` · ${Number(millis).toLocaleString()} ms` : "";
    if (value === "SLOW") {
        return `still running, no answer yet${took}`;
    }
    if (value === "WARN") {
        return `warning${took}`;
    }
    return `${value.toLowerCase().replace(/_/g, " ")}${took}`;
}

function palantirTableGroupRow(label, columns) {
    return $("<tr>", {class: "palantir-table-group-row"})
        .append($("<td>", {colspan: columns}).text(label));
}

function hideElements(elements) {
    $(elements)
        .css("display", "none")
        .addClass("hide")
        .addClass("d-none");
}

function showElements(elements) {
    $(elements)
        .css("display", "")
        .removeClass("hide")
        .removeClass("d-none");
}

let palantirCodeEditorSequence = 0;
let palantirGroovyCompleterRegistered = false;

function unwrapPalantirGroovyScript(value) {
    const script = String(value ?? "").trim();
    const match = script.match(/^groovy\s*\{([\s\S]*)\}\s*$/);
    return match ? match[1].trim() : script;
}

function formatPalantirGroovyScript(value) {
    const script = String(value ?? "").trim();
    return script ? `groovy { ${script} }` : "";
}

/**
 * Validate Groovy source through the Groovy script-cache actuator endpoint.
 * The endpoint returns an empty successful response when the script compiles
 * and a non-successful status when parsing or compilation fails.
 */
function validatePalantirGroovyScript(value) {
    const endpoint = CasActuatorEndpoints.groovyCache();
    if (!endpoint) {
        return Promise.reject(new Error("The Groovy script validation endpoint is not available."));
    }

    const url = `${String(endpoint).replace(/\/$/, "")}/resources/validate`;
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            method: "POST",
            contentType: "text/plain; charset=UTF-8",
            processData: false,
            data: String(value ?? "")
        }).done(() => resolve({
            valid: true,
            message: "Groovy script validation succeeded."
        })).fail(xhr => {
            const responseMessage = xhr.responseJSON?.message
                ?? xhr.responseJSON?.error
                ?? String(xhr.responseText ?? "").trim();
            const message = responseMessage
                || (xhr.status === 400
                    ? "Groovy script validation failed. Check the script syntax."
                    : `Unable to validate the Groovy script${xhr.status ? ` (HTTP ${xhr.status})` : ""}.`);
            reject(new Error(message));
        });
    });
}

function registerPalantirGroovyCompleter() {
    if (palantirGroovyCompleterRegistered) {
        return;
    }
    const languageTools = ace.require("ace/ext/language_tools");
    const completions = [
        {caption: "def", value: "def ", meta: "Groovy keyword"},
        {caption: "println", snippet: "println(${1:value})", meta: "Groovy method"},
        {caption: "each", snippet: "each { item ->\n    ${1}\n}", meta: "Groovy closure"},
        {caption: "eachWithIndex", snippet: "eachWithIndex { item, index ->\n    ${1}\n}", meta: "Groovy closure"},
        {caption: "collect", snippet: "collect { item ->\n    ${1:item}\n}", meta: "Groovy collection"},
        {caption: "find", snippet: "find { item -> ${1:condition} }", meta: "Groovy collection"},
        {caption: "findAll", snippet: "findAll { item -> ${1:condition} }", meta: "Groovy collection"},
        {caption: "any", snippet: "any { item -> ${1:condition} }", meta: "Groovy collection"},
        {caption: "every", snippet: "every { item -> ${1:condition} }", meta: "Groovy collection"},
        {caption: "inject", snippet: "inject(${1:initial}) { result, item ->\n    ${2:result}\n}", meta: "Groovy collection"},
        {caption: "if", snippet: "if (${1:condition}) {\n    ${2}\n}", meta: "Groovy statement"},
        {caption: "try/catch", snippet: "try {\n    ${1}\n} catch (${2:Exception} e) {\n    ${3}\n}", meta: "Groovy statement"},
        {caption: "return", value: "return ", meta: "Groovy keyword"},
        {caption: "true", value: "true", meta: "Groovy constant"},
        {caption: "false", value: "false", meta: "Groovy constant"},
        {caption: "null", value: "null", meta: "Groovy constant"},
        {caption: "Map", value: "Map", meta: "Groovy type"},
        {caption: "List", value: "List", meta: "Groovy type"},
        {caption: "Set", value: "Set", meta: "Groovy type"},
        {caption: "String", value: "String", meta: "Groovy type"}
    ].map(completion => ({score: 1000, ...completion}));
    languageTools.addCompleter({
        identifierRegexps: [/[a-zA-Z_0-9.$-]/],
        getCompletions: (_editor, session, _position, _prefix, callback) => {
            const modeId = session.$modeId ?? session.getMode()?.$id;
            callback(null, modeId === "ace/mode/groovy" ? completions : []);
        }
    });
    palantirGroovyCompleterRegistered = true;
}

/**
 * Open a reusable, editable Ace code-editor dialog.
 * Callers may supply prepareValue/formatValue, onAccept, and an asynchronous onValidate callback.
 * No validation request is attempted when onValidate is absent.
 */
function openPalantirCodeEditorDialog(options = {}) {
    const target = $(options.target);
    const mode = options.mode ?? "text";
    const dialogId = `palantirCodeEditorDialog${++palantirCodeEditorSequence}`;
    const editorId = `${dialogId}Editor`;
    const statusId = `${dialogId}Status`;
    const initialValue = options.initialValue !== undefined
        ? options.initialValue
        : target.val() ?? "";
    const preparedValue = typeof options.prepareValue === "function"
        ? options.prepareValue(initialValue)
        : initialValue;
    const infoMessage = options.infoMessage ?? (mode === "groovy"
        ? "Groovy is a dynamic language. Validate Syntax only parses the script; it does not execute it. "
            + "Runtime behavior requires an active CAS context and depends on the binding variables and services available when the script runs."
        : "");
    const dialog = $(
        `<div id="${dialogId}" class="palantir-code-editor-dialog">
            <div id="${statusId}" class="palantir-code-editor-status d-none" role="status" aria-live="polite"></div>
            <pre id="${editorId}" class="ace-editor ace-relative palantir-code-editor"></pre>
        </div>`);
    if (infoMessage) {
        const infoPanel = $("<div>", {class: "banner banner-info mb-0", role: "note"});
        infoPanel.append($("<i>", {class: "mdi mdi-information-outline", "aria-hidden": "true"}));
        infoPanel.append($("<span>").text(infoMessage));
        dialog.append(infoPanel);
    }
    $("body").append(dialog);

    let editor;
    const setStatus = (message, status = "info") => {
        const statusPanel = $(`#${statusId}`);
        statusPanel.removeClass("d-none palantir-code-editor-status-info palantir-code-editor-status-success palantir-code-editor-status-error")
            .addClass(`palantir-code-editor-status-${status}`)
            .text(message);
    };
    const content = () => editor?.getValue() ?? String(preparedValue ?? "");

    dialog.dialog({
        title: options.title ?? "Code Editor",
        modal: true,
        autoOpen: true,
        width: Math.min($(window).width() - 60, options.width ?? 1200),
        height: Math.min($(window).height() - 80, options.height ?? 760),
        position: {my: "center top", at: "center top+40", of: window},
        buttons: {
            "Validate Syntax": function () {
                const script = content();
                if (typeof options.onValidate !== "function") {
                    setStatus(options.validationUnavailableMessage
                        ?? "Script validation is not configured yet. No request was sent.", "info");
                    return;
                }
                setStatus("Validating script...", "info");
                Promise.resolve(options.onValidate(script, {editor: editor, dialog: dialog, target: target}))
                    .then(result => {
                        const message = typeof result === "string" ? result : result?.message ?? "Script validation succeeded.";
                        setStatus(message, result?.valid === false ? "error" : "success");
                    })
                    .catch(error => setStatus(error?.message ?? "Script validation failed.", "error"));
            },
            OK: function () {
                const value = typeof options.formatValue === "function"
                    ? options.formatValue(content())
                    : content();
                if (target.length > 0) {
                    target.val(value).trigger("input").trigger("change");
                }
                options.onAccept?.(value, {editor: editor, dialog: dialog, target: target});
                dialog.dialog("close");
            },
            Cancel: function () {
                dialog.dialog("close");
            }
        },
        open: function () {
            editor = initializeAceEditor(editorId, mode);
            editor.setReadOnly(false);
            editor.setValue(String(preparedValue ?? ""), -1);
            if (mode === "groovy") {
                registerPalantirGroovyCompleter();
            }
            cas.init(`#${dialogId}`);
            dialog.closest(".ui-dialog").addClass("palantir-code-editor-shell");
            cas.init(".palantir-code-editor-shell .ui-dialog-buttonpane");
            setTimeout(() => {
                editor.resize(true);
                editor.focus();
                editor.gotoLine(1);
            }, 50);
        },
        close: function () {
            editor?.destroy();
            dialog.dialog("destroy").remove();
            target.trigger("focus");
        }
    });
    return dialog;
}

/**
 * Attach an MDC-themed editor button to a Palantir outlined input field.
 * All dialog options are forwarded to openPalantirCodeEditorDialog.
 */
function attachPalantirCodeEditorButton(options = {}) {
    const target = $(options.target);
    if (target.length === 0) {
        return $();
    }
    const attachedButton = target.data("palantir-code-editor-button");
    if (attachedButton) {
        return attachedButton;
    }
    const textField = target.closest(".mdc-text-field");
    if (textField.parent().is("span")) {
        textField.parent().addClass("palantir-code-field-container");
    }
    const inputGroup = $("<div>", {class: "mdc-input-group d-flex mb-2 palantir-code-input-group"});
    const structuralInputClasses = new Set([
        "mdc-text-field__input", "form-control", "hide", "d-none", "d-block", "d-flex"
    ]);
    const visibilityClasses = String(target.attr("class") ?? "")
        .split(/\s+/)
        .filter(className => className && !structuralInputClasses.has(className));
    inputGroup.addClass(visibilityClasses.join(" "));
    const inputField = $("<div>", {class: "mdc-input-group-field mdc-input-group-field-append flex-grow-1"});
    const button = $(
        `<button type="button" class="mdc-button mdc-button--raised mdc-input-group-append palantir-code-editor-button"
                 title="${options.buttonTitle ?? "Open code editor"}">
            <span class="mdc-button__label"><i class="mdi mdi-code-braces" aria-hidden="true"></i>${options.buttonLabel ?? "Edit"}</span>
        </button>`);
    textField.removeClass("mb-2").before(inputGroup);
    inputField.append(textField);
    inputGroup.append(inputField, button);
    button.on("click", () => {
        if (!target[0]?.isConnected || !target.is(":visible") || target.prop("disabled")) {
            return;
        }
        openPalantirCodeEditorDialog({...options, target: target});
    });
    target.data("palantir-code-editor-button", button);
    return button;
}

/**
 * Attach the standard editable and syntax-validating Groovy editor to an input.
 */
function attachPalantirGroovyEditorButton(options = {}) {
    return attachPalantirCodeEditorButton({
        title: "Groovy Script",
        mode: "groovy",
        buttonTitle: "Open Groovy script editor",
        buttonLabel: "",
        prepareValue: unwrapPalantirGroovyScript,
        formatValue: formatPalantirGroovyScript,
        onValidate: validatePalantirGroovyScript,
        ...options
    });
}

const palantirLoadedScripts = new Map();

/**
 * Load a script once, on demand.
 *
 * Large libraries that only one panel needs -- the webflow diagram renderer above all -- are kept
 * off the dashboard's critical path and fetched the first time that panel is used. Repeat callers
 * share the first call's promise, so a rapid sequence of clicks loads the file once.
 *
 * @param url the script URL to load
 * @returns {Promise<void>} resolved once the script has executed
 */
function loadPalantirScriptOnce(url) {
    if (!url) {
        return Promise.reject(new Error("No script URL was provided."));
    }
    const loaded = palantirLoadedScripts.get(url);
    if (loaded) {
        return loaded;
    }
    const loading = new Promise((resolve, reject) => {
        const script = document.createElement("script");
        script.src = url;
        script.async = true;
        script.addEventListener("load", () => resolve());
        script.addEventListener("error", () => {
            palantirLoadedScripts.delete(url);
            reject(new Error(`Unable to load ${url}`));
        });
        document.head.append(script);
    });
    palantirLoadedScripts.set(url, loading);
    return loading;
}

/**
 * The editor, the diff library and the application wizard, fetched when something first needs them rather than on
 * every visit, so Home, the page most visits land on, renders without them. Every page but Home waits for them before
 * it is initialized, because its fields and editors are built with them; Home fetches them once the browser is idle,
 * so opening the wizard from it does not wait either. The wizard brings the editor and the diff library along, and
 * the scripts of a bundle are fetched together but run in the order they are listed. A script that ran is never
 * fetched again, so trying again after a failed fetch only fetches what failed.
 */
class PalantirScripts {
    static loading = new Map();

    static scripts = new Map();

    static load(...names) {
        return Promise.all(names.map(name => PalantirScripts.bundle(name))).then(() => undefined);
    }

    static bundle(name) {
        const existing = PalantirScripts.loading.get(name);
        if (existing) {
            return existing;
        }
        const needed = name === "wizard" ? [PalantirScripts.bundle("ace"), PalantirScripts.bundle("diff")] : [];
        const urls = PalantirDashboardConfiguration.all().scripts?.[name] ?? [];
        const loading = Promise.all([...needed, ...urls.map(url => PalantirScripts.script(url))])
            .then(() => undefined)
            .catch(error => {
                PalantirScripts.loading.delete(name);
                throw error;
            });
        PalantirScripts.loading.set(name, loading);
        return loading;
    }

    static script(url) {
        const existing = PalantirScripts.scripts.get(url);
        if (existing) {
            return existing;
        }
        const loading = new Promise((resolve, reject) => {
            const script = document.createElement("script");
            script.src = url;
            script.async = false;
            script.addEventListener("load", () => resolve());
            script.addEventListener("error", () => {
                script.remove();
                PalantirScripts.scripts.delete(url);
                reject(new Error(`Unable to load ${url}`));
            });
            document.head.append(script);
        });
        PalantirScripts.scripts.set(url, loading);
        return loading;
    }

    static forPage(tabIndex) {
        return tabIndex === Tabs.HOME.index ? Promise.resolve() : PalantirScripts.load("wizard");
    }

    static whenIdle() {
        return new Promise(resolve => {
            if (typeof requestIdleCallback === "function") {
                requestIdleCallback(() => resolve(), {timeout: 3000});
            } else {
                setTimeout(resolve, 500);
            }
        }).then(() => PalantirScripts.load("wizard"));
    }

    static failed(error) {
        console.error("Unable to load the application wizard:", error);
        displayBanner("The application wizard could not be loaded. Reload the page to try again.");
    }
}

/**
 * Open the application wizard, fetching it first when it has not been used yet.
 *
 * @param args what openRegisteredServiceWizardDialog takes
 * @returns {Promise<boolean>} true once the wizard is open, false when it could not be fetched
 */
async function openPalantirWizard(...args) {
    try {
        await PalantirScripts.load("wizard");
    } catch (error) {
        PalantirScripts.failed(error);
        return false;
    }
    await openRegisteredServiceWizardDialog(...args);
    return true;
}

function hideBanner() {
    notyf.dismissAll();
}

function closeAllDialogs() {
    $(".ui-dialog-content:visible").dialog("close");
}

const PALANTIR_READ_ONLY = "palantir-read-only";

/**
 * Read JSON keeping every id exactly as CAS wrote it. CAS ids, such as those of applications, are 64-bit numbers, and
 * JavaScript numbers are exact only up to 2^53, so two neighbouring large ids would read as the same number and an
 * action on one could reach the other. An id too large to be exact is read as the text of its digits instead; ids that
 * fit stay numbers.
 *
 * @param text the JSON text
 * @returns {*} the value it holds
 */
function palantirParseJson(text) {
    return JSON.parse(String(text).replace(/("id"\s*:\s*)(-?\d{16,})(?=\s*[,}\]])/g,
        (match, key, digits) => Number.isSafeInteger(Number(digits)) ? match : `${key}"${digits}"`));
}

/**
 * Write JSON the way CAS reads it: an id kept as the text of its digits by {@link palantirParseJson}, because it is too
 * large for an exact JavaScript number, is written as the number it is.
 *
 * @param value the value
 * @param indent the indentation, as JSON.stringify takes it
 * @returns {string} the JSON text
 */
function palantirStringifyJson(value, indent) {
    return JSON.stringify(value, null, indent).replace(/("id"\s*:\s*)"(-?\d{16,})"/g,
        (match, key, digits) => Number.isSafeInteger(Number(digits)) ? match : `${key}${digits}`);
}

if (typeof $ !== "undefined" && $.ajaxSetup) {
    $.ajaxSetup({converters: {"text json": palantirParseJson}});
}

/**
 * A modal progress dialog for work the operator has to wait for: a message saying what is happening, a bar that
 * shows how far along it is or, when the server does not say, that it is busy, and how long it has taken so far.
 */
class PalantirProgress {
    static timer = null;

    static started = 0;

    static detail = "";

    /**
     * Open the dialog.
     *
     * @param title what the work is
     * @param message what is happening first
     * @param detail what the work is about, such as a file name and size
     * @param percent how far along the work starts, or null to show it busy until progress is reported
     */
    static open(title, message, detail = "", percent = null) {
        PalantirProgress.stop();
        PalantirProgress.started = Date.now();
        PalantirProgress.detail = detail;
        Swal.fire({
            titleText: title,
            html: `
                <div class="palantir-progress">
                    <p class="palantir-progress__message" aria-live="polite"></p>
                    <div class="palantir-progress__track" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                        <span class="palantir-progress__bar"></span>
                    </div>
                    <p class="palantir-progress__detail"></p>
                </div>`,
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false,
            didOpen: popup => {
                $(popup).find(".palantir-progress__track").attr("aria-label", title);
                PalantirProgress.update(message, percent);
            }
        });
        PalantirProgress.timer = setInterval(() => PalantirProgress.tick(), 1000);
    }

    /**
     * Say what is happening and how far along it is.
     *
     * @param message what is happening
     * @param percent how far along it is, from 0 to 100, or null when only the server knows and it is busy
     */
    static update(message, percent = null) {
        const progress = $(Swal.getHtmlContainer()).find(".palantir-progress");
        if (progress.length === 0) {
            return;
        }
        progress.find(".palantir-progress__message").text(message);
        const track = progress.find(".palantir-progress__track");
        if (percent === null) {
            track.addClass("is-busy").removeAttr("aria-valuenow").attr("aria-valuetext", message);
            track.find(".palantir-progress__bar").css("width", "");
        } else {
            const value = Math.max(0, Math.min(100, Math.round(percent)));
            track.removeClass("is-busy").attr("aria-valuenow", value).attr("aria-valuetext", `${value}%`);
            track.find(".palantir-progress__bar").css("width", `${value}%`);
        }
        PalantirProgress.tick();
    }

    /**
     * Show how long the work has taken so far.
     */
    static tick() {
        const seconds = Math.floor((Date.now() - PalantirProgress.started) / 1000);
        const elapsed = `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
        $(Swal.getHtmlContainer()).find(".palantir-progress__detail")
            .text([PalantirProgress.detail, `Elapsed ${elapsed}`].filter(Boolean).join(" · "));
    }

    static stop() {
        clearInterval(PalantirProgress.timer);
        PalantirProgress.timer = null;
    }

    /**
     * Close the dialog, if it is still the one open.
     */
    static close() {
        PalantirProgress.stop();
        if ($(Swal.getHtmlContainer()).find(".palantir-progress").length > 0) {
            Swal.close();
        }
    }

    /**
     * Write a size in bytes the way people read it.
     *
     * @param bytes the size
     * @returns {string} the size
     */
    static size(bytes) {
        const units = ["bytes", "KB", "MB", "GB"];
        let value = Number(bytes) || 0;
        let unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return unit === 0 ? `${value} ${units[0]}` : `${value.toFixed(1)} ${units[unit]}`;
    }
}

/**
 * Let a SweetAlert opened over a modal jQuery UI dialog take focus. The dialog keeps focus inside itself and would
 * otherwise pull it back from the alert's fields and buttons, which sit outside the dialog.
 */
function allowAlertsOverModalDialogs() {
    if (!$.ui?.dialog || $.ui.dialog.prototype.palantirAllowsAlerts) {
        return;
    }
    $.widget("ui.dialog", $.ui.dialog, {
        palantirAllowsAlerts: true,
        _allowInteraction(event) {
            return $(event.target).closest(".swal2-container").length > 0 || this._super(event);
        }
    });
}

/**
 * Explain why a change cannot be made from Palantir.
 *
 * @param name the endpoint id
 * @returns {string} the explanation
 */
/**
 * Read a date as the actuator endpoints answer it: an ISO date, possibly followed by a zone id in brackets, or a
 * number of seconds or milliseconds since the epoch.
 *
 * @param value the date as answered
 * @returns {Date|null} the date, or null when it cannot be read
 */
function palantirRecordDate(value) {
    if (value === undefined || value === null || value === "") {
        return null;
    }
    const date = typeof value === "number"
        ? new Date(value < 1e12 ? value * 1000 : value)
        : new Date(Date.parse(String(value).replace(/\[[^\]]*\]$/, "")));
    return Number.isNaN(date.getTime()) ? null : date;
}

function palantirRecordAgo(date) {
    if (!date) {
        return "at an unknown time";
    }
    const seconds = Math.max(0, Math.round((Date.now() - date.getTime()) / 1000));
    if (seconds < 45) {
        return "just now";
    }
    const minutes = Math.round(seconds / 60);
    if (minutes < 60) {
        return `${minutes} min ago`;
    }
    const hours = Math.round(minutes / 60);
    if (hours < 48) {
        return `${hours} h ago`;
    }
    return `${Math.round(hours / 24)} days ago`;
}

function palantirRecordWhen(date) {
    return date ? date.toLocaleString(undefined, {dateStyle: "medium", timeStyle: "short"}) : "Unknown";
}

/**
 * Put a card view where a table sits, and show what to do before anything is searched.
 *
 * @param {string} tableId the table the cards stand in for
 * @param {string} cardsId the element that holds the cards
 * @param {string} prompt what to do before anything is searched
 */
function placePalantirRecordCards(tableId, cardsId, prompt) {
    const tableRow = $(`#${tableId}`).closest(".dt-layout-row");
    if (tableRow.length > 0) {
        $(`#${cardsId}`).insertAfter(tableRow);
    }
    $(`#${cardsId}`).html(`<p class="palantir-empty-state">${escapePalantirMarkup(prompt)}</p>`);
}

/**
 * Draw one card per row the table shows on its current page, once something was searched. A row carries the record
 * its card is drawn from.
 *
 * @param {object} table the table the cards stand in for
 * @param {string} cardsId the element that holds the cards
 * @param {Function} card draws the card of one record
 * @param {Function} emptyMessage what to say when the search found nothing, given what was searched for
 */
function renderPalantirRecordCards(table, cardsId, card, emptyMessage) {
    const container = $(`#${cardsId}`);
    if (container.length === 0 || container.data("searched") === undefined) {
        return;
    }
    const cards = table.rows({page: "current", search: "applied"}).data().toArray()
        .filter(row => row.record)
        .map(row => card(row.record));
    if (cards.length > 0) {
        container.html(cards.join(""));
        applyPalantirEndpointAccess(container[0]);
    } else {
        const message = table.search() === "" ? emptyMessage(container.data("searched")) : "Nothing matches that search.";
        container.html(`<p class="palantir-empty-state">${escapePalantirMarkup(message)}</p>`);
    }
}

/**
 * A value shown as code in a table cell or a card: the value is escaped, so text that came from a request, such as a
 * username, a user agent or an attribute value, is shown as written and never read as markup.
 *
 * @param value the value; null and undefined show as nothing
 * @returns {string} the markup
 */
function palantirCode(value) {
    const text = value === null || value === undefined ? "" : typeof value === "object" ? JSON.stringify(value) : String(value);
    return `<code>${text.replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    })[character])}</code>`;
}

function palantirReadOnlyMessage(name) {
    return `The ${name} endpoint is read-only on this CAS server, so this change cannot be made from Palantir.`;
}

/**
 * Disable the controls that change something through a read-only endpoint, under the given element. Such a
 * control names its endpoint in a data-palantir-write attribute.
 *
 * @param root the element to look under
 */
function applyPalantirEndpointAccess(root = document) {
    $(root).find("[data-palantir-write]").addBack("[data-palantir-write]").each((index, element) => {
        const name = element.dataset.palantirWrite;
        if (CasActuatorEndpoints.writable(name)) {
            return;
        }
        const message = palantirReadOnlyMessage(name);
        if ("disabled" in element) {
            element.disabled = true;
        } else {
            element.setAttribute("aria-disabled", "true");
            element.setAttribute("tabindex", "-1");
        }
        element.classList.add("palantir-read-only");
        element.title = message;
    });
}

/**
 * Hold back what a read-only endpoint refuses: disable the controls that would change something through it, also
 * when they are drawn later, and refuse a request that would change something through it before it is sent.
 */
function initializePalantirEndpointAccess() {
    if (!CasActuatorEndpoints.anyReadOnly()) {
        return;
    }
    applyPalantirEndpointAccess();
    new MutationObserver(mutations => {
        for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
                if (node.nodeType === Node.ELEMENT_NODE) {
                    applyPalantirEndpointAccess(node);
                }
            }
        }
    }).observe(document.body, {childList: true, subtree: true});
    $.ajaxPrefilter((options, originalOptions, xhr) => {
        const method = String(options.type ?? options.method ?? "GET").toUpperCase();
        if (["GET", "HEAD", "OPTIONS"].includes(method)) {
            return;
        }
        const name = CasActuatorEndpoints.endpointFor(options.url);
        if (name && !CasActuatorEndpoints.writable(name)) {
            xhr.abort(PALANTIR_READ_ONLY);
            notyf.dismissAll();
            notyf.error(palantirReadOnlyMessage(name));
        }
    });
}

/**
 * A context menu item that changes something through a read-only endpoint is shown disabled. Such an item names
 * its endpoint in its write property.
 *
 * @param item the menu item
 * @returns {object} the item as the menu shows it
 */
function restrictContextMenuItem(item) {
    if (item === null || typeof item !== "object" || !item.write) {
        return item;
    }
    const menuItem = {...item};
    delete menuItem.write;
    if (!CasActuatorEndpoints.writable(item.write)) {
        menuItem.disabled = true;
    }
    return menuItem;
}

function displayBanner(error) {
    if (error?.statusText === PALANTIR_READ_ONLY || error?.statusText === "abort") {
        return;
    }
    let message = "";
    if (error.hasOwnProperty("status")) {
        switch (error.status) {
        case 401:
            message = "You are not authorized to access this resource. Are you sure you are authenticated?";
            break;
        case 403:
            message = "You are forbidden from accessing this resource. Are you sure you have the necessary permissions and the entry is correctly registered with CAS?";
            break;
        case 400:
        case 500:
        case 503:
            message = "Unable to process or accept the request. Check CAS server logs for details.";
            break;
        case 0:
            message = error.statusText === "timeout"
                ? "The CAS server took too long to respond. Check CAS server logs for slow requests."
                : "Unable to contact the CAS server. Are you sure the server is reachable?";
            break;
        default:
            message = `HTTP error: ${error.status}. `;
            break;
        }
    }
    if (error.hasOwnProperty("path")) {
        message += `Unable to make an API call to ${error.path}. Is the endpoint enabled and available?`;
    }
    if (message.length === 0) {
        if (typeof error === "string") {
            message = error;
        } else {
            message = error.message;
        }
    }
    notyf.dismissAll();
    notyf.error(message);
}

function waitForActuator(endpoint, intervalMs = 2000) {
    return new Promise((resolve) => {
        function poll() {
            $.ajax({
                url: endpoint,
                method: "GET",
                dataType: "json",
                timeout: 3000
            })
                .done(function (data) {
                    resolve(data);
                })
                .fail(function () {
                    setTimeout(poll, intervalMs);
                });
        }

        poll();
    });
}

function highlightElements() {
    document
        .querySelectorAll("pre code[data-highlighted]")
        .forEach(el => delete el.dataset.highlighted);
    hljs.highlightAll();
}

const palantirInputIconRules = [
    {
        icon: "mdi-key-variant",
        pattern: /\b(client secrets?|api keys?|access tokens?|bearer tokens?|auth(?:orization)? headers?|secrets?|tokens?)\b/
    },
    {icon: "mdi-lock-outline", pattern: /\b(passwords?|passphrases?|credentials?)\b/},
    {icon: "mdi-card-account-details-outline", pattern: /\b(user ?name attribute|principal (?:id )?attribute)\b/},
    {
        icon: "mdi-identifier",
        pattern: /\b(client ids?|entity ids?|tenant ids?|store ids?|requester ids?|subject ids?|resource ids?|zone ids?)\b/
    },
    {icon: "mdi-account-search", pattern: /\b(user ?names?|user ids?|principal names?|principal ids?)\b/},
    {icon: "mdi-email-outline", pattern: /\b(e-?mails?|mail addresses?)\b/},
    {icon: "mdi-phone-outline", pattern: /\b(phones?|telephones?|mobiles?|sms numbers?)\b/},
    {icon: "mdi-ticket-confirmation-outline", pattern: /\btickets?\b/},
    {icon: "mdi-link-variant", pattern: /\b(urls?|uris?|endpoints?|redirects?|callbacks?|webhooks?)\b/},
    {icon: "mdi-file-outline", pattern: /\b(files?|file paths?|resource paths?|metadata locations?)\b/},
    {icon: "mdi-web", pattern: /\b(services?|applications?)\b/},
    {icon: "mdi-ip-network", pattern: /\b(ip address|network address)\b/},
    {icon: "mdi-server", pattern: /\b(host ?name|server address|server host)\b/},
    {icon: "mdi-ethernet", pattern: /\b(port number|server port|ldap port)\b/},
    {icon: "mdi-database", pattern: /\b(database|data source|datasource|jdbc|sql query|database query)\b/},
    {icon: "mdi-certificate", pattern: /\b(certificate|key store|keystore|trust store|truststore|public key)\b/},
    {icon: "mdi-regex", pattern: /\b(regex|regular expressions?|patterns?)\b/},
    {icon: "mdi-calendar-clock", pattern: /\b(date|time|duration|expiration|expiry|timeout)\b/},
    {icon: "mdi-card-account-details-outline", pattern: /\b(attributes?|claims?)\b/},
    {icon: "mdi-account-group", pattern: /\bgroups?\b/},
    {icon: "mdi-shield-account", pattern: /\b(roles?|permissions?)\b/},
    {icon: "mdi-target", pattern: /\bscopes?\b/},
    {icon: "mdi-domain", pattern: /\b(domains?|namespaces?|tenants?|organizations?)\b/},
    {icon: "mdi-translate", pattern: /\b(locales?|languages?)\b/},
    {icon: "mdi-database-search", pattern: /\bqueries?\b/}
];
const palantirIconInputTypes = new Set([
    "text", "password", "email", "tel", "url", "search", "number", "date", "datetime-local", "time"
]);

function normalizePalantirInputDescriptor(value) {
    return String(value ?? "")
        .replace(/([a-z\d])([A-Z])/g, "$1 $2")
        .replace(/[_./:-]+/g, " ")
        .replace(/[^a-z\d\s-]/gi, " ")
        .replace(/\s+/g, " ")
        .trim()
        .toLowerCase();
}

function palantirInputDescriptors(input) {
    const textField = input.closest(".mdc-text-field");
    const floatingLabel = textField?.querySelector(".mdc-floating-label")?.textContent;
    const associatedLabel = !floatingLabel && input.labels?.length > 0
        ? input.labels[0].textContent
        : "";
    const identity = normalizePalantirInputDescriptor([input.id, input.name].filter(Boolean).join(" "))
        .replace(/\bregistered service\b/g, " ");
    return [
        normalizePalantirInputDescriptor([
            input.dataset.paramName,
            input.autocomplete,
            input.placeholder,
            floatingLabel,
            associatedLabel
        ].filter(Boolean).join(" ")),
        normalizePalantirInputDescriptor(identity),
        normalizePalantirInputDescriptor(input.title)
    ].filter(Boolean);
}

function resolvePalantirInputIcon(input) {
    const type = String(input.type ?? "text").toLowerCase();
    const descriptors = palantirInputDescriptors(input);
    if (type === "password") {
        return descriptors.some(descriptor => /\b(secret|token|authorization header)\b/.test(descriptor))
            ? "mdi-key-variant"
            : "mdi-lock-outline";
    }
    const typeIcons = {
        email: "mdi-email-outline",
        tel: "mdi-phone-outline",
        url: "mdi-link-variant",
        search: "mdi-magnify",
        date: "mdi-calendar-clock",
        "datetime-local": "mdi-calendar-clock",
        time: "mdi-clock-outline"
    };
    if (typeIcons[type]) {
        return typeIcons[type];
    }
    for (const descriptor of descriptors) {
        const rule = palantirInputIconRules.find(candidate => candidate.pattern.test(descriptor));
        if (rule) {
            return rule.icon;
        }
    }
    return undefined;
}

function decoratePalantirInputIcon(input) {
    const type = String(input.type ?? "text").toLowerCase();
    const textField = input.closest(".mdc-text-field");
    if (!textField || !palantirIconInputTypes.has(type)
        || textField.querySelector(".mdc-text-field__icon--leading")) {
        return;
    }

    const iconName = resolvePalantirInputIcon(input);
    if (!iconName) {
        return;
    }

    const icon = document.createElement("i");
    icon.className = `mdi ${iconName} mdc-text-field__icon mdc-text-field__icon--leading palantir-input-icon`;
    icon.setAttribute("aria-hidden", "true");
    icon.setAttribute("tabindex", "-1");
    input.before(icon);
    const outline = Array.from(textField.children)
        .find(child => child.classList.contains("mdc-notched-outline"));
    if (outline) {
        textField.append(outline);
    }
    textField.classList.add("mdc-text-field--with-leading-icon");
}

function decoratePalantirInputIcons(root = document) {
    const selector = "input.mdc-text-field__input";
    if (root.matches?.(selector)) {
        decoratePalantirInputIcon(root);
    }
    root.querySelectorAll?.(selector).forEach(decoratePalantirInputIcon);
}

/**
 * Watch for inputs added after load and decorate them, in batches.
 *
 * The inputs present at load are decorated by initializePalantirWidgets, panel by panel, as each
 * tab opens; this observer covers everything created afterwards.
 *
 * The observer watches the whole dashboard, and a data table redraw inserts hundreds of nodes at
 * once. Decorating each node as its mutation record arrives made every redraw pay for one
 * querySelectorAll per inserted row; the nodes are collected instead and swept once per frame.
 */
function initializePalantirInputIcons() {
    const pending = new Set();
    let sweepScheduled = false;

    const sweep = () => {
        sweepScheduled = false;
        const nodes = [...pending];
        pending.clear();
        for (const node of nodes) {
            if (node.isConnected) {
                decoratePalantirInputIcons(node);
            }
        }
    };

    const observer = new MutationObserver(mutations => {
        for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
                if (node.nodeType === Node.ELEMENT_NODE) {
                    pending.add(node);
                }
            }
        }
        if (pending.size > 0 && !sweepScheduled) {
            sweepScheduled = true;
            requestAnimationFrame(sweep);
        }
    });
    observer.observe(document.body, {childList: true, subtree: true});
}

const palantirPollingContextEvent = "palantir:polling-context-changed";
let palantirPollingContextInitialized = false;
let palantirPollingContextNotificationScheduled = false;

function isPalantirPollingContextActive(dashboardTab, panelSelector) {
    const tabIndex = typeof dashboardTab === "number" ? dashboardTab : dashboardTab.index;
    if (currentActiveTab !== tabIndex || document.visibilityState !== "visible"
        || (typeof document.hasFocus === "function" && !document.hasFocus())) {
        return false;
    }

    const navigationItem = $(`nav.sidebar-navigation ul li[data-tab-index='${tabIndex}']`);
    const navigationButton = navigationItem.find(".sidebar-navigation-button");
    if (navigationItem.length === 0 || !navigationItem.is(":visible")
        || navigationItem.is("[disabled], [aria-disabled='true']") || navigationItem.hasClass("ui-state-disabled")
        || navigationButton.is("[disabled], [aria-disabled='true']")) {
        return false;
    }

    const panel = $(panelSelector);
    if (panel.length === 0 || !panel.is(":visible")
        || panel.is("[disabled], [aria-disabled='true']") || panel.hasClass("ui-state-disabled")) {
        return false;
    }

    const panelId = panel.attr("id");
    const innerTab = panelId ? $(`.jqueryui-tabs > ul a[href='#${panelId}']`).first().parent() : $();
    return innerTab.length === 0 || (innerTab.is(":visible")
        && !innerTab.is("[disabled], [aria-disabled='true']") && !innerTab.hasClass("ui-state-disabled"));
}

function notifyPalantirPollingContextChanged() {
    if (palantirPollingContextNotificationScheduled) {
        return;
    }
    palantirPollingContextNotificationScheduled = true;
    queueMicrotask(() => {
        palantirPollingContextNotificationScheduled = false;
        window.dispatchEvent(new CustomEvent(palantirPollingContextEvent));
    });
}

function initializePalantirPollingContext() {
    if (palantirPollingContextInitialized) {
        return;
    }
    palantirPollingContextInitialized = true;
    window.addEventListener("focus", notifyPalantirPollingContextChanged);
    window.addEventListener("blur", notifyPalantirPollingContextChanged);
    document.addEventListener("visibilitychange", notifyPalantirPollingContextChanged);
}


/**
 * Collect the elements matching a selector within a root.
 *
 * When the root is the whole document the dashboard's tab panels are skipped: their widgets are
 * built when that tab is first opened, so building them all at load would be paying for fifteen
 * panels to show one.
 *
 * @param selector the widget selector
 * @param root the subtree to search
 * @returns {jQuery} the matching elements
 */
function palantirWidgetTargets(selector, root) {
    const $root = $(root ?? document);
    const found = $root.find(selector).addBack(selector);
    return root && root !== document
        ? found
        : found.filter((_, element) => !element.closest(".attribute-tab"));
}

/**
 * Build the jQuery UI widgets and input decorations inside a subtree.
 *
 * Safe to call more than once and on overlapping subtrees: an element that already carries its
 * widget is left alone.
 *
 * @param root the subtree to initialize; the document, minus the tab panels, when omitted
 */
function initializePalantirWidgets(root) {
    if (root === null) {
        return;
    }
    initializeTabs(root);
    initializeMenus(root);
    initializeDropDowns(root);
    initializeDatePickers(root);
    palantirWidgetTargets("input.mdc-text-field__input", root)
        .each((_, input) => decoratePalantirInputIcon(input));
}

function initializeTabs(root) {
    palantirWidgetTargets(".jqueryui-tabs", root).each(function () {
        if ($(this).data("ui-tabs")) {
            return;
        }
        $(this).tabs({
            activate: function () {
                const tabId = $(this).attr("id");
                if (tabId) {
                    const active = $(this).tabs("option", "active");
                    const panel = $(this).find("> ul > li").eq(active).attr("aria-controls");
                    try {
                        const activeTabs = JSON.parse(localStorage.getItem("ActiveTabs") ?? "{}") ?? {};
                        activeTabs[tabId] = panel ?? active;
                        localStorage.setItem("ActiveTabs", JSON.stringify(activeTabs));
                    } catch (error) {
                        console.debug("The inner page shown is not remembered", error);
                    }
                }
                notifyPalantirPollingContextChanged();
            }
        }).off().on("click", () => updateNavigationSidebar());
    });
}

/**
 * The position of the inner page a container of jQuery UI tabs remembers: the page is remembered by the id of its
 * panel, which does not change when pages are added or moved; a position remembered by an older version of Palantir
 * is still read.
 *
 * @param tabs the container
 * @param remembered the id of the panel, or the position
 * @returns {number} the position of that page in the container, or -1 when it has no such page
 */
function palantirInnerTabIndex(tabs, remembered) {
    if (typeof remembered === "number" || /^\d+$/.test(String(remembered ?? ""))) {
        return Number(remembered);
    }
    return tabs.find("> ul > li").toArray().findIndex(item => item.getAttribute("aria-controls") === remembered
        || $(item).find("a").attr("href") === `#${remembered}`);
}

function initializeMenus(root) {
    palantirWidgetTargets(".jqueryui-menu", root).each(function () {
        if (!$(this).data("ui-menu")) {
            $(this).menu();
        }
    });
}

function initializeDropDowns(root) {
    palantirWidgetTargets(".jqueryui-selectmenu", root).each(function () {
        if ($(this).data("ui-selectmenu")) {
            return;
        }
        $(this).selectmenu({
            width: "360px",
            change: function (event, ui) {
                const $select = $(this);
                const handlerNames = $select.data("change-handler");
                if (!handlerNames) {
                    return;
                }
                for (const handlerName of handlerNames.split(",")) {
                    if (handlerName && handlerName.length > 0 && typeof window[handlerName] === "function") {
                        const result = window[handlerName]($select, ui);
                        if (result !== undefined && result === false) {
                            break;
                        }
                    }
                }
            }
        });
    });
}

/**
 * Attach the jQuery UI date picker to every input marked jquery-datepicker.
 *
 * An input chooses the text a picked date becomes through its dateFormat data value, in the date
 * picker's notation; quoted text is copied as is, so a date and time field can carry a fixed time of
 * day, such as yy-mm-dd'T'00:00:00. Without one, the picker's own format applies. A picked date is
 * announced as input, so the field's own handler validates it and regenerates the definition.
 *
 * @param root the subtree to search; the document, minus the tab panels, when omitted
 */
function initializeDatePickers(root) {
    palantirWidgetTargets("input.jquery-datepicker", root).each(function () {
        if ($(this).data("datepicker")) {
            return;
        }
        const format = $(this).data("dateFormat");
        $(this).datepicker({
            ...format ? {dateFormat: format} : {},
            changeMonth: true,
            changeYear: true,
            showAnim: "slideDown",
            onSelect: function () {
                $(this).trigger("input");
            }
        });
    });
}

/**
 * Close a tooltip whose element was removed from the page or hidden. Tooltips close when the pointer leaves their
 * element or when jQuery removes it, but a part of the page redrawn by assigning its markup, or hidden with its
 * dialog, takes its elements away without either, and their open tooltips would stay on the screen.
 */
function closeOrphanedTooltips() {
    const targets = new Map();
    $(document)
        .off("tooltipopen.palantirOrphans tooltipclose.palantirOrphans")
        .on("tooltipopen.palantirOrphans", (event, ui) => {
            const id = ui.tooltip.attr("id");
            const target = document.querySelector(`[aria-describedby~="${id}"]`);
            if (target) {
                targets.set(id, target);
            }
        })
        .on("tooltipclose.palantirOrphans", (event, ui) => targets.delete(ui.tooltip.attr("id")));
    new MutationObserver(() => {
        for (const [id, target] of targets) {
            if (!target.isConnected || target.getClientRects().length === 0) {
                targets.delete(id);
                $(document).tooltip("close", $.Event("mouseleave", {target: target, currentTarget: target}));
                $(`#${id}`).remove();
            }
        }
    }).observe(document.body, {childList: true, subtree: true, attributes: true, attributeFilter: ["style", "class", "hidden"]});
}

/**
 * Show a document, such as metadata or a JSON response, read-only in a code editor inside a dialog sized to the window.
 * The dialog is the element with the given id, holding an editor element whose id is the dialog's id followed by
 * "Editor".
 *
 * @param dialogId the id of the dialog element
 * @param title the dialog's title
 * @param text the document
 * @param mode the editor mode, such as json or xml
 */
function showPalantirCodeDialog(dialogId, title, text, mode = "json") {
    const element = $(`#${dialogId}`);
    if (!element.dialog("instance")) {
        element.dialog({
            autoOpen: false,
            modal: true,
            resizable: false,
            classes: {"ui-dialog": "ui-corner-all palantir-code-dialog"}
        });
    }
    element.dialog("option", {
        title: title,
        width: Math.min(1100, $(window).width() - 32),
        height: Math.min(760, $(window).height() - 48),
        position: {my: "center", at: "center", of: window}
    });
    element.dialog("open");
    const editor = initializeAceEditor(`${dialogId}Editor`, mode, {useWorker: mode === "json"});
    editor.setReadOnly(true);
    editor.setValue(text, -1);
    editor.resize(true);
}

function initializeTooltips() {
    $(function () {
        closeOrphanedTooltips();
        $(document).tooltip({
            items: "[title]:not(a)",
            content: function () {
                return $(this).data("tooltip-html") ?? $("<div>").text($(this).attr("title")).html();
            },
            show: {
                effect: "fade",
                delay: 800,
                duration: 500
            },
            hide: {
                effect: "fade",
                delay: 100
            },
            position: {
                my: "center bottom-20",
                at: "center top",
                using: function (position, feedback) {
                    $(this).css(position);
                    $("<div>")
                        .addClass("arrow")
                        .addClass(feedback.vertical)
                        .addClass(feedback.horizontal)
                        .appendTo(this);
                }
            }
        });
    });
}

function contextMenuIcon(icon) {
    return () => `context-menu-icon mdi ${icon}`;
}

const contextMenuActiveRowClass = "context-menu-active";

function contextMenuRow(options, context) {
    if (context?.$row?.length) {
        return context.$row;
    }
    if (options?.$trigger?.length) {
        return options.$trigger.closest("tr");
    }
    return $();
}

function highlightContextMenuRow(options, context) {
    $(`tr.${contextMenuActiveRowClass}`).removeClass(contextMenuActiveRowClass);
    const $row = contextMenuRow(options, context);
    if ($row.length) {
        $row.addClass(contextMenuActiveRowClass);
    }
}

function clearContextMenuRow(options, context) {
    const $row = contextMenuRow(options, context);
    if ($row.length) {
        $row.removeClass(contextMenuActiveRowClass);
    } else {
        $(`tr.${contextMenuActiveRowClass}`).removeClass(contextMenuActiveRowClass);
    }
}

function prepareContextMenuItems(items, context) {
    const sourceItems = typeof items === "function" ? items(context) : items;
    const menuItems = {};
    let separator = null;

    for (const [key, item] of Object.entries(sourceItems || {})) {
        if (typeof item === "string") {
            if (Object.keys(menuItems).length > 0) {
                separator = [key, item];
            }
            continue;
        }

        const visible = typeof item.visible === "function"
            ? item.visible(context)
            : item.visible !== false;
        if (!visible) {
            continue;
        }

        if (separator) {
            menuItems[separator[0]] = separator[1];
            separator = null;
        }

        const menuItem = {...restrictContextMenuItem(item)};
        delete menuItem.visible;
        menuItems[key] = menuItem;
    }
    return menuItems;
}

/**
 * Restore the jQuery helpers that jQuery 4 removed and that jQuery contextMenu still calls, including the
 * jQuery UI 1.12 position plugin it ships. That plugin replaces jQuery UI's own position, so until these exist
 * every dialog, tooltip and menu that positions itself fails, and dialogs open unsized in the corner.
 */
function installJQueryCompatibility() {
    if (!jQuery.isFunction) {
        jQuery.isFunction = function (obj) {
            return typeof obj === "function";
        };
    }
    if (!jQuery.isWindow) {
        jQuery.isWindow = function (obj) {
            return obj != null && obj === obj.window;
        };
    }
}

/**
 * Give every jQuery UI dialog the shared CAS look. Dialogs are moved to the end of the body, outside the
 * main content that carries the cas-ui class, so the class is added to the dialog itself.
 */
function applySharedDialogStyle() {
    $(document).on("dialogcreate", event => $(event.target).closest(".ui-dialog").addClass("cas-ui palantir-dialog"));
}

/**
 * Keep dialogs and select menus inside the window on small screens. A dialog never grows wider or taller than
 * the window: its content scrolls instead, and once it is open it is moved back into view whenever it grows or the
 * window shrinks. Select menus open above their field when there is no room below, in the window or in the dialog
 * that holds them, which clips what falls outside it, and scroll when they are long.
 */
function keepPalantirPopupsInView() {
    const margin = 16;
    if ($.ui?.selectmenu) {
        $.ui.selectmenu.prototype.options.position = {my: "left top", at: "left bottom", collision: "flip fit"};
    }
    const fit = content => {
        const element = $(content);
        if (!element.hasClass("ui-dialog-content") || !element.dialog("isOpen")) {
            return;
        }
        const widget = element.dialog("widget");
        if (widget.outerWidth() > window.innerWidth - margin * 2) {
            element.dialog("option", "width", window.innerWidth - margin * 2);
        }
        const bounds = widget[0].getBoundingClientRect();
        if (bounds.top < margin || bounds.bottom > window.innerHeight - margin
            || bounds.left < 0 || bounds.right > window.innerWidth) {
            element.dialog("option", "position", {my: "center", at: "center", of: window, collision: "fit"});
        }
    };
    const pending = new Set();
    const observer = typeof ResizeObserver === "function"
        ? new ResizeObserver(entries => {
            entries.forEach(entry => pending.add(entry.target));
            requestAnimationFrame(() => {
                pending.forEach(widget => fit($(widget).find("> .ui-dialog-content")));
                pending.clear();
            });
        })
        : null;
    $(document)
        .off("dialogopen.palantirInView")
        .on("dialogopen.palantirInView", event => {
            const widget = $(event.target).closest(".ui-dialog");
            fit(event.target);
            observer?.observe(widget[0]);
        })
        .off("dialogclose.palantirInView")
        .on("dialogclose.palantirInView", event => observer?.unobserve($(event.target).closest(".ui-dialog")[0]));
    $(document)
        .off("selectmenuopen.palantirInView")
        .on("selectmenuopen.palantirInView", event => {
            const menu = $(event.target).selectmenu("instance");
            if (!menu) {
                return;
            }
            const dialog = menu.menuWrap.closest(".ui-dialog");
            const bounds = dialog.length > 0
                ? dialog[0].getBoundingClientRect()
                : {top: 0, bottom: window.innerHeight};
            const button = menu.button[0].getBoundingClientRect();
            const height = menu.menuWrap.outerHeight();
            const below = Math.min(bounds.bottom, window.innerHeight) - button.bottom;
            const above = button.top - Math.max(bounds.top, 0);
            const upward = height > below && above > below;
            menu.menuWrap.position({
                my: upward ? "left bottom" : "left top",
                at: upward ? "left top" : "left bottom",
                of: menu.button,
                collision: "fit",
                within: dialog.length > 0 ? dialog : window
            });
        });
    $(window)
        .off("resize.palantirInView")
        .on("resize.palantirInView", () => $(".ui-dialog-content").each((index, content) => fit(content)));
}

function initializeContextMenu({selector, callback, items, build, trigger = "right"}) {
    installJQueryCompatibility();
    $.contextMenu("destroy", selector);

    const contextMenuOptions = {
        selector: selector,
        trigger: trigger,
        callback: function (key, options) {
            callback(key, options);
        },
        events: {
            show: function (options) {
                highlightContextMenuRow(options);
            },
            hide: function (options) {
                clearContextMenuRow(options);
            }
        },
        items: typeof items === "function" || !items
            ? items
            : Object.fromEntries(Object.entries(items).map(([key, item]) => [key, restrictContextMenuItem(item)]))
    };

    if (build) {
        delete contextMenuOptions.callback;
        delete contextMenuOptions.items;
        contextMenuOptions.build = function ($trigger, event) {
            const context = build($trigger, event);
            if (!context) {
                return false;
            }
            if (typeof context === "object") {
                context.$trigger ??= $trigger;
                context.$row ??= $trigger.closest("tr");
                context.event ??= event;
            }

            const menuItems = prepareContextMenuItems(items, context);
            if (Object.keys(menuItems).length === 0) {
                return false;
            }

            return {
                callback: function (key, options) {
                    options.context = context;
                    callback(key, options);
                },
                events: {
                    show: function (options) {
                        options.context = context;
                        highlightContextMenuRow(options, context);
                    },
                    hide: function (options) {
                        options.context = context;
                        clearContextMenuRow(options, context);
                    }
                },
                items: menuItems
            };
        };
    }

    $.contextMenu(contextMenuOptions);
}

function initializeDataTableContextMenu({table, selector, callback, items, trigger = "right"}) {
    initializeContextMenu({
        selector: selector,
        trigger: trigger,
        build: ($trigger, event) => {
            const $row = $trigger.closest("tr");
            if ($row.hasClass("dataTables_empty") || $row.hasClass("child") || $row.parent("thead").length > 0) {
                return false;
            }
            const row = table.row($row);
            const rowData = row.data();
            if (!rowData) {
                return false;
            }
            return {
                row: row,
                rowData: rowData,
                $row: $row,
                $trigger: $trigger,
                event: event
            };
        },
        items: items,
        callback: (key, options) => callback(key, options.context)
    });
    makeDataTableRowsKeyboardReachable(table, selector);
}

/**
 * Let keyboard users reach the rows of a table whose rows have actions: each row takes focus with Tab, Enter does
 * what a click does, and Shift+F10 or the context menu key opens the row's actions where a right click would.
 *
 * @param table the DataTables instance
 * @param selector the rows' selector, as given to the context menu
 */
function makeDataTableRowsKeyboardReachable(table, selector) {
    const focusable = () => $(selector).not(".dataTables_empty").filter((index, row) => !$(row).find("td.dataTables_empty").length)
        .attr("tabindex", "0");
    focusable();
    table.off("draw.palantirKeyboard").on("draw.palantirKeyboard", focusable);
    const namespace = `keydown.palantirRows${selector.replace(/[^A-Za-z0-9]/g, "")}`;
    $(document).off(namespace).on(namespace, selector, event => {
        if (event.target !== event.currentTarget) {
            return;
        }
        if (event.key === "ContextMenu" || (event.shiftKey && event.key === "F10")) {
            event.preventDefault();
            const bounds = event.currentTarget.getBoundingClientRect();
            $(event.currentTarget).contextMenu({x: bounds.left + 24, y: bounds.top + bounds.height / 2});
        } else if (event.key === "Enter") {
            event.preventDefault();
            $(event.currentTarget).trigger("click");
        }
    });
}

/**
 * The accessible name of a table: the heading just before it, or the label of the inner tab it sits in, or its
 * id written out in words.
 *
 * @param table the table element
 * @returns {string} the name
 */
function palantirTableName(table) {
    let node = $(table).closest(".dt-container")[0] ?? table;
    for (let depth = 0; depth < 4 && node; depth++) {
        let sibling = node.previousElementSibling;
        while (sibling) {
            const heading = $(sibling).is("h1,h2,h3,h4,h5") ? $(sibling) : $(sibling).find("h1,h2,h3,h4,h5").last();
            if (heading.length > 0 && heading.text().trim()) {
                return heading.text().replace(/\s+/g, " ").trim();
            }
            sibling = sibling.previousElementSibling;
        }
        node = node.parentElement;
        if ($(node).is(".ui-tabs-panel, .ui-dialog-content, .attribute-tab")) {
            break;
        }
    }
    const panel = $(table).closest(".ui-tabs-panel");
    const tab = panel.length > 0 ? $(`.ui-tabs-anchor[href="#${panel.attr("id")}"]`).first().text().trim() : "";
    if (tab) {
        return tab;
    }
    return String(table.id || "Table").replace(/Table$/, "").replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .replace(/^./, character => character.toUpperCase());
}

/**
 * Give buttons and links that show only an icon the name their tooltip already carries.
 *
 * @param root the element to look in
 */
function nameIconOnlyControls(root = document) {
    $(root).find("button, a[href], [role=button]").each((index, control) => {
        if (control.hasAttribute("aria-label") || control.hasAttribute("aria-labelledby") || control.textContent.trim()) {
            return;
        }
        const name = control.getAttribute("title") || control.dataset.originalTitle;
        if (name) {
            control.setAttribute("aria-label", name);
        }
    });
}

/**
 * Accessibility of tables and dialogs across Palantir, applied as they are created rather than at every call site:
 * tables get a name and name their pagination after it, icon-only controls get the name of their tooltip, modal
 * dialogs say they are modal, and jQuery UI select menus take the name of their label (jQuery UI points the label
 * at its button, which a span cannot take a name from).
 */
function initializeTableAndDialogAccessibility() {
    $(document).off("init.dt.palantirA11y").on("init.dt.palantirA11y", (event, settings) => {
        const table = event.target?.tagName === "TABLE" ? event.target : settings && new $.fn.dataTable.Api(settings).table().node();
        if (!table) {
            return;
        }
        if (!table.hasAttribute("aria-label") && !table.hasAttribute("aria-labelledby") && !table.querySelector("caption")) {
            table.setAttribute("aria-label", palantirTableName(table));
        }
        const name = table.getAttribute("aria-label") ?? palantirTableName(table);
        $(table).closest(".dt-container").find("nav[aria-label]").attr("aria-label", `${name}, pages`);
        nameIconOnlyControls($(table).closest(".dt-container"));
    });
    $(document).off("draw.dt.palantirA11y").on("draw.dt.palantirA11y", event => nameIconOnlyControls(event.target));
    $(document).off("dialogcreate.palantirA11y").on("dialogcreate.palantirA11y", event => {
        const content = $(event.target);
        if (content.dialog("option", "modal")) {
            content.closest(".ui-dialog").attr("aria-modal", "true");
        }
    });
    $(document).off("dialogopen.palantirA11y").on("dialogopen.palantirA11y", event => {
        nameIconOnlyControls($(event.target).closest(".ui-dialog"));
    });
    $(document).off("selectmenucreate.palantirA11y").on("selectmenucreate.palantirA11y", event => {
        const select = event.target;
        const button = $(select).selectmenu("widget");
        if (button.attr("aria-label") || button.attr("aria-labelledby")) {
            return;
        }
        const label = select.labels?.[0] ?? $(`label[for="${button.attr("id")}"]`)[0] ?? $(select).closest("label")[0];
        let name = select.getAttribute("aria-label") || select.title || "";
        if (label) {
            const copy = label.cloneNode(true);
            $(copy).find("select, .ui-selectmenu-button, .ui-selectmenu-menu").remove();
            name = copy.textContent.replace(/\s+/g, " ").trim() || name;
        }
        if (name) {
            button.attr("aria-label", name);
        }
    });
    nameIconOnlyControls(document);
}

/**
 * Corrects the ARIA roles jQuery UI puts on accordions.
 *
 * jQuery UI marks accordion headers role="tab" and panels role="tabpanel".
 * Those roles are only meaningful inside a role="tablist", which an accordion
 * is not, and role="tab" on an <h3> overrides its heading semantics, so a
 * screen reader user cannot move through the sections by heading at all.
 *
 * The root loses role="tablist" for the same reason, since it no longer holds tabs.
 *
 * This restores what the ARIA authoring practices describe: headers stay
 * headings and keep aria-expanded and aria-controls, and each panel becomes a
 * region named by its header. jQuery UI's keyboard handling, roving tab index
 * and animation are untouched.
 *
 * @param element the accordion root element to correct
 */
function applyAccordionAccessibility(element) {
    const accordion = $(element);
    accordion.removeAttr("role");
    accordion.children(".ui-accordion-header").removeAttr("role").removeAttr("aria-selected");
    accordion.children(".ui-accordion-content").attr("role", "region");
}

/**
 * Teaches every jQuery UI accordion on the page to correct itself.
 *
 * Extending the widget rather than patching each call site covers accordions
 * created later and the internal refreshes jQuery UI performs when panels are
 * added or options change. Safe to call more than once.
 */
function initializeAccordionAccessibility() {
    if (typeof $.widget !== "function" || !$.ui?.accordion || $.ui.accordion.prototype.palantirAccessible) {
        return;
    }
    $.widget("ui.accordion", $.ui.accordion, {
        palantirAccessible: true,
        _refresh: function () {
            this._super();
            applyAccordionAccessibility(this.element);
        },
        _toggle: function (data) {
            this._super(data);
            applyAccordionAccessibility(this.element);
        }
    });
}

const PALANTIR_SAFE_HTML_TAGS = new Set(["A", "ABBR", "B", "BLOCKQUOTE", "BR", "CODE", "DIV", "EM", "H1", "H2", "H3", "H4",
    "H5", "H6", "HR", "I", "LI", "OL", "P", "PRE", "S", "SMALL", "SPAN", "STRONG", "SUB", "SUP", "U", "UL"]);

const PALANTIR_DROPPED_HTML_TAGS = new Set(["SCRIPT", "STYLE", "TEMPLATE", "IFRAME", "FRAME", "OBJECT", "EMBED", "NOSCRIPT",
    "SVG", "MATH", "LINK", "META", "BASE", "FORM", "INPUT", "BUTTON", "SELECT", "TEXTAREA", "TITLE"]);

/**
 * Turn markup from CAS, such as an interrupt message, into markup that is safe to put on the page.
 *
 * The markup is parsed into an inert document, where nothing runs or loads, and copied node by node into fresh
 * elements: text is kept, formatting tags on an allow list are kept without any of their attributes, links keep only
 * an http, https or mailto address and open in a new window, the content of scripts, styles, frames, forms and
 * embedded documents is dropped, and any other tag is replaced by its content.
 *
 * @param html the markup to clean
 * @returns {string} markup with only the allowed tags and link addresses
 */
function sanitizePalantirHtml(html) {
    const source = new DOMParser().parseFromString(`<!DOCTYPE html><body>${String(html ?? "")}</body>`, "text/html").body;
    const target = document.createElement("div");
    const copy = (from, to) => {
        for (const node of from.childNodes) {
            if (node.nodeType === Node.TEXT_NODE) {
                to.appendChild(document.createTextNode(node.textContent));
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                const tag = node.tagName.toUpperCase();
                if (PALANTIR_DROPPED_HTML_TAGS.has(tag)) {
                    continue;
                }
                if (!PALANTIR_SAFE_HTML_TAGS.has(tag)) {
                    copy(node, to);
                    continue;
                }
                const element = document.createElement(tag.toLowerCase());
                if (tag === "A") {
                    try {
                        const url = new URL(node.getAttribute("href") ?? "", window.location.href);
                        if (["http:", "https:", "mailto:"].includes(url.protocol)) {
                            element.setAttribute("href", url.href);
                            element.setAttribute("target", "_blank");
                            element.setAttribute("rel", "noopener noreferrer");
                        }
                    } catch (error) {
                        console.debug("Palantir dropped a link it could not read", error);
                    }
                }
                copy(node, element);
                to.appendChild(element);
            }
        }
    };
    copy(source, target);
    return target.innerHTML;
}

/**
 * The text of markup from CAS, for places that show plain text such as table cells. The content of scripts,
 * styles and other dropped tags is not part of it.
 *
 * @param html the markup
 * @returns {string} its text
 */
function palantirHtmlText(html) {
    return new DOMParser().parseFromString(`<!DOCTYPE html><body>${sanitizePalantirHtml(html)}</body>`, "text/html").body.textContent.trim();
}

/**
 * Magnify a chart or a diagram in a dialog as large as the window. A button marked
 * <code>data-palantir-magnify="target-id"</code>, or a click on a target marked
 * <code>data-palantir-magnifiable</code>, opens it: a Chart.js canvas
 * is drawn again at the dialog's size, and an SVG diagram, such as a Mermaid flow, can be zoomed with the wheel, the
 * keyboard or the toolbar and moved by dragging.
 */
class PalantirMagnifier {
    static MIN_SCALE = .2;

    static MAX_SCALE = 8;

    static initialized = false;

    /**
     * Listen for magnify buttons and double-clicks on magnifiable targets, once.
     */
    static initialize() {
        if (PalantirMagnifier.initialized) {
            return;
        }
        PalantirMagnifier.initialized = true;
        $(document)
            .on("click.palantirMagnifier", "[data-palantir-magnify]", event => {
                event.preventDefault();
                const button = event.currentTarget;
                PalantirMagnifier.magnify(document.getElementById(button.dataset.palantirMagnify),
                    button.dataset.palantirMagnifyTitle || button.getAttribute("aria-label") || "");
            })
            .on("click.palantirMagnifier", "[data-palantir-magnifiable]", event => {
                if (PalantirMagnifier.opensOnClick(event)) {
                    PalantirMagnifier.magnify(event.currentTarget, event.currentTarget.dataset.palantirMagnifiable || "");
                }
            })
            .on("keydown.palantirMagnifier", "[data-palantir-magnifiable]", event => {
                if ((event.key === "Enter" || event.key === " ") && event.target === event.currentTarget) {
                    event.preventDefault();
                    PalantirMagnifier.magnify(event.currentTarget, event.currentTarget.dataset.palantirMagnifiable || "");
                }
            });
        PalantirMagnifier.markTargets();
    }

    /**
     * Whether a click on a magnifiable target opens it: not when it lands on a control or a link inside it, on a
     * chart's legend, which shows and hides series, or ends a text selection.
     *
     * @param event the click
     * @returns {boolean} true to magnify
     */
    static opensOnClick(event) {
        if ($(event.target).closest("a, button, input, select, textarea, [onclick], .clickable").length > 0
            || String(window.getSelection?.() ?? "").length > 0) {
            return false;
        }
        const canvas = event.currentTarget.tagName === "CANVAS" ? event.currentTarget : null;
        const chart = canvas && typeof Chart !== "undefined" ? Chart.getChart(canvas) : null;
        if (chart && typeof chart.config.options?.onClick === "function"
            && chart.getElementsAtEventForMode(event, "nearest", {intersect: true}, false).length > 0) {
            return false;
        }
        const legend = chart?.legend;
        if (legend) {
            const bounds = canvas.getBoundingClientRect();
            const x = event.clientX - bounds.left;
            const y = event.clientY - bounds.top;
            if (x >= legend.left && x <= legend.right && y >= legend.top && y <= legend.bottom) {
                return false;
            }
        }
        return true;
    }

    /**
     * Let keyboard users open magnifiable targets as they would a link.
     */
    static markTargets() {
        $("[data-palantir-magnifiable]").each((index, target) => {
            target.setAttribute("tabindex", target.getAttribute("tabindex") ?? "0");
            if (!target.getAttribute("title")) {
                target.setAttribute("title", "Click to magnify");
            }
        });
    }

    /**
     * Magnify what a target holds: a chart on a canvas, or an SVG diagram.
     *
     * @param target the canvas, or an element holding an SVG
     * @param title the dialog title
     */
    static magnify(target, title) {
        if (!target) {
            return;
        }
        const canvas = target.tagName === "CANVAS" ? target : target.querySelector("canvas");
        const chart = canvas && typeof Chart !== "undefined" ? Chart.getChart(canvas) : null;
        if (chart) {
            PalantirMagnifier.chart(chart, title);
            return;
        }
        const svg = target.tagName.toLowerCase() === "svg" ? target : target.querySelector("svg");
        if (svg) {
            PalantirMagnifier.svg(svg, title);
        }
    }

    /**
     * Open the magnifier dialog and let a builder fill its stage.
     *
     * @param title the dialog title
     * @param build called with the stage and the toolbar once the dialog is open; returns a function to call on close
     */
    static open(title, build) {
        const dialog = $(`
            <div class="palantir-magnifier">
                <div class="palantir-magnifier__toolbar" role="toolbar" aria-label="Magnifier"></div>
                <div class="palantir-magnifier__stage"></div>
            </div>`).attr("title", title || "Magnified view").appendTo(document.body);
        let cleanup = null;
        dialog.dialog({
            modal: true,
            width: window.innerWidth - 48,
            height: window.innerHeight - 48,
            draggable: false,
            resizable: false,
            position: {my: "center", at: "center", of: window},
            open: () => {
                cleanup = build(dialog.find(".palantir-magnifier__stage")[0], dialog.find(".palantir-magnifier__toolbar"));
            },
            close: () => {
                cleanup?.();
                dialog.dialog("destroy").remove();
            }
        });
    }

    /**
     * Draw a chart again, as large as the dialog, with the data it shows now.
     *
     * @param chart the Chart.js chart
     * @param title the dialog title
     */
    static chart(chart, title) {
        PalantirMagnifier.open(title, (stage, toolbar) => {
            toolbar.remove();
            const canvas = document.createElement("canvas");
            canvas.setAttribute("role", "img");
            canvas.setAttribute("aria-label", chart.canvas.getAttribute("aria-label") ?? title);
            stage.classList.add("is-chart");
            stage.append(canvas);
            const options = chart.config.options ?? {};
            const large = new Chart(canvas.getContext("2d"), {
                type: chart.config.type,
                data: typeof cloneChartValue === "function" ? cloneChartValue(chart.data) : JSON.parse(JSON.stringify(chart.data)),
                options: {...options, responsive: true, maintainAspectRatio: false, animation: false},
                plugins: chart.config.plugins ?? []
            });
            if (typeof followPalantirChart === "function") {
                followPalantirChart(chart, large);
            }
            return () => {
                if (typeof unfollowPalantirChart === "function") {
                    unfollowPalantirChart(chart, large);
                }
                large.destroy();
            };
        });
    }

    /**
     * Show a copy of an SVG diagram that can be zoomed and moved.
     *
     * @param svg the diagram
     * @param title the dialog title
     */
    static svg(svg, title) {
        PalantirMagnifier.open(title, (stage, toolbar) => {
            const copy = svg.cloneNode(true);
            const id = `palantirMagnified${Date.now()}`;
            if (svg.id) {
                copy.id = id;
                copy.querySelectorAll("style").forEach(style => {
                    style.textContent = style.textContent.split(`#${svg.id}`).join(`#${id}`);
                });
            }
            const box = svg.viewBox?.baseVal;
            const width = box?.width || svg.getBoundingClientRect().width || 800;
            const height = box?.height || svg.getBoundingClientRect().height || 600;
            copy.removeAttribute("style");
            copy.setAttribute("width", String(width));
            copy.setAttribute("height", String(height));
            const layer = $("<div>", {class: "palantir-magnifier__layer"}).append(copy)[0];
            stage.classList.add("is-diagram");
            stage.tabIndex = 0;
            stage.append(layer);

            const view = {scale: 1, x: 0, y: 0};
            const apply = () => {
                layer.style.transform = `translate(${view.x}px, ${view.y}px) scale(${view.scale})`;
                toolbar.find("[data-zoom-level]").text(`${Math.round(view.scale * 100)}%`);
            };
            const fit = () => {
                const bounds = stage.getBoundingClientRect();
                view.scale = Math.min(PalantirMagnifier.MAX_SCALE, Math.max(PalantirMagnifier.MIN_SCALE,
                    Math.min((bounds.width - 32) / width, (bounds.height - 32) / height)));
                view.x = (bounds.width - width * view.scale) / 2;
                view.y = (bounds.height - height * view.scale) / 2;
                apply();
            };
            const zoom = (factor, originX, originY) => {
                const bounds = stage.getBoundingClientRect();
                const pointX = originX ?? bounds.width / 2;
                const pointY = originY ?? bounds.height / 2;
                const scale = Math.min(PalantirMagnifier.MAX_SCALE, Math.max(PalantirMagnifier.MIN_SCALE, view.scale * factor));
                view.x = pointX - (pointX - view.x) * scale / view.scale;
                view.y = pointY - (pointY - view.y) * scale / view.scale;
                view.scale = scale;
                apply();
            };

            toolbar.html(`
                <button type="button" class="palantir-icon-button" data-zoom="out" title="Zoom out" aria-label="Zoom out"><i class="mdi mdi-magnify-minus-outline" aria-hidden="true"></i></button>
                <span class="palantir-magnifier__level" data-zoom-level aria-live="polite"></span>
                <button type="button" class="palantir-icon-button" data-zoom="in" title="Zoom in" aria-label="Zoom in"><i class="mdi mdi-magnify-plus-outline" aria-hidden="true"></i></button>
                <button type="button" class="palantir-icon-button" data-zoom="fit" title="Fit to the window" aria-label="Fit to the window"><i class="mdi mdi-fit-to-screen-outline" aria-hidden="true"></i></button>
                <span class="palantir-magnifier__hint">Scroll to zoom, drag to move</span>`);
            toolbar.on("click", "[data-zoom]", event => {
                const action = event.currentTarget.dataset.zoom;
                if (action === "fit") {
                    fit();
                } else {
                    zoom(action === "in" ? 1.25 : .8);
                }
            });
            stage.addEventListener("wheel", event => {
                event.preventDefault();
                const bounds = stage.getBoundingClientRect();
                zoom(Math.exp(-event.deltaY * .0015), event.clientX - bounds.left, event.clientY - bounds.top);
            }, {passive: false});
            let drag = null;
            stage.addEventListener("pointerdown", event => {
                drag = {x: event.clientX - view.x, y: event.clientY - view.y};
                stage.setPointerCapture(event.pointerId);
                stage.classList.add("is-dragging");
            });
            stage.addEventListener("pointermove", event => {
                if (drag) {
                    view.x = event.clientX - drag.x;
                    view.y = event.clientY - drag.y;
                    apply();
                }
            });
            const release = () => {
                drag = null;
                stage.classList.remove("is-dragging");
            };
            stage.addEventListener("pointerup", release);
            stage.addEventListener("pointercancel", release);
            stage.addEventListener("keydown", event => {
                const moves = {ArrowLeft: [40, 0], ArrowRight: [-40, 0], ArrowUp: [0, 40], ArrowDown: [0, -40]};
                if (event.key === "+" || event.key === "=") {
                    zoom(1.25);
                } else if (event.key === "-") {
                    zoom(.8);
                } else if (event.key === "0") {
                    fit();
                } else if (moves[event.key]) {
                    view.x += moves[event.key][0];
                    view.y += moves[event.key][1];
                    apply();
                } else {
                    return;
                }
                event.preventDefault();
            });
            requestAnimationFrame(() => {
                fit();
                stage.focus();
            });
            return null;
        });
    }
}

/**
 * Put the cursor in the first field of a page or inner tab as soon as the operator switches to it, so typing a
 * username, a query or a filter does not need another click. A field marked data-palantir-autofocus wins over the
 * first one; one inside data-palantir-no-autofocus is never chosen, and neither is one that opens a calendar or a list of
 * suggestions when it gets focus, unless it is marked.
 *
 * Focus is only moved when it is nowhere the operator is working: on the page, on the navigation or tab list that was
 * just used, or on something that the switch hid. It stays put while a dialog is open over the page, while the arrow
 * keys move along a tab list, and on touch screens, where focusing a field would pop up the keyboard. Escape in a field
 * of a page lets go of it again, so the single-key shortcuts that typing in a field turns off work at once.
 */
class PalantirPanelFocus {
    static FIELDS = [
        "input:not([type])", "input[type=text]", "input[type=search]", "input[type=email]", "input[type=password]",
        "input[type=number]", "input[type=url]", "input[type=tel]", "textarea"
    ].join(", ");

    static NAVIGATION = ".ui-tabs-nav, #palantirNavigation, nav.sidebar-navigation, #dashboardTabBar";

    static POPUPS = ".hasDatepicker, .ui-autocomplete-input, .ts-wrapper input";

    static arrowingTabs = false;

    static placed = null;

    static initialize() {
        document.addEventListener("pointerdown", () => {
            PalantirPanelFocus.arrowingTabs = false;
        }, true);
        document.addEventListener("keydown", event => {
            const key = typeof event.key === "string" ? event.key : "";
            PalantirPanelFocus.arrowingTabs = /^(Arrow|Home$|End$)/.test(key) && Boolean(event.target.closest?.(".ui-tabs-nav"));
        }, true);
        document.addEventListener("keydown", event => PalantirPanelFocus.release(event));
        document.addEventListener("input", event => {
            if (event.target === PalantirPanelFocus.placed) {
                PalantirPanelFocus.placed = null;
            }
        }, true);
        $(document).off("tabsactivate.palantirPanelFocus").on("tabsactivate.palantirPanelFocus", (_event, ui) => {
            if (ui?.newPanel?.length) {
                PalantirPanelFocus.into(ui.newPanel);
            }
        });
    }

    static usable(field) {
        const element = $(field);
        return !field.disabled && !field.readOnly && field.tabIndex >= 0 && element.is(":visible")
            && element.closest("[data-palantir-no-autofocus], [aria-hidden='true'], [inert], .ace_editor").length === 0;
    }

    static field(panel) {
        const root = $(panel);
        const preferred = root.find("[data-palantir-autofocus]").filter((_, field) => PalantirPanelFocus.usable(field)).first();
        return preferred.length ? preferred : root.find(PalantirPanelFocus.FIELDS)
            .filter((_, field) => PalantirPanelFocus.usable(field) && !$(field).is(PalantirPanelFocus.POPUPS)).first();
    }

    static allowed(panel) {
        if (PalantirPanelFocus.arrowingTabs || window.matchMedia?.("(pointer: coarse)")?.matches) {
            return false;
        }
        if (typeof Swal !== "undefined" && Swal.isVisible()) {
            return false;
        }
        const dialogs = $(".ui-dialog:visible");
        if (dialogs.length && !$(panel).closest(dialogs.last()).length) {
            return false;
        }
        const active = document.activeElement;
        return !active || active === document.body || active === PalantirPanelFocus.placed || !$(active).is(":visible")
            || $(active).closest(PalantirPanelFocus.NAVIGATION).length > 0;
    }

    static into(panel) {
        requestAnimationFrame(() => {
            const root = $(panel);
            if (root.length && root.is(":visible") && PalantirPanelFocus.allowed(root)) {
                PalantirPanelFocus.field(root).each((_, field) => {
                    field.focus({preventScroll: true});
                    PalantirPanelFocus.placed = field;
                });
            }
        });
    }

    static release(event) {
        const field = event.target;
        if (event.key !== "Escape" || event.defaultPrevented || !(field instanceof HTMLElement) || !field.matches(PalantirPanelFocus.FIELDS)
            || $(field).closest(".ui-dialog, .swal2-container, .ace_editor").length > 0 || !$(field).closest(".attribute-tab").length) {
            return;
        }
        field.blur();
    }
}
