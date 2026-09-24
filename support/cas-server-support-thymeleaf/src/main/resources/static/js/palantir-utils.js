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

function hideBanner() {
    notyf.dismissAll();
}

function closeAllDialogs() {
    $(".ui-dialog-content:visible").dialog("close");
}

function displayBanner(error) {
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
            message = "Unable to contact the CAS server. Are you sure the server is reachable?";
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
                    const storedTabs = localStorage.getItem("ActiveTabs");
                    const activeTabs = storedTabs ? JSON.parse(storedTabs) : {};
                    activeTabs[tabId] = active;
                    localStorage.setItem("ActiveTabs", JSON.stringify(activeTabs));
                }
                notifyPalantirPollingContextChanged();
            }
        }).off().on("click", () => updateNavigationSidebar());
    });
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

function initializeDatePickers(root) {
    palantirWidgetTargets("input.jquery-datepicker", root).each(function () {
        if ($(this).data("datepicker")) {
            return;
        }
        $(this).datepicker({
            showAnim: "slideDown",
            onSelect: function (date, ins) {
                $(ins).val(date);
                generateServiceDefinition();
                $(`#${$(ins).prop("id")}`).prev().find(".mdc-notched-outline__notch").hide();
            }
        });
    });
}

function initializeTooltips() {
    $(function () {
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

        const menuItem = {...item};
        delete menuItem.visible;
        menuItems[key] = menuItem;
    }
    return menuItems;
}

function initializeContextMenu({selector, callback, items, build, trigger = "right"}) {
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
        items: items
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
}

/**
 * Corrects the ARIA roles jQuery UI puts on accordions.
 *
 * jQuery UI marks accordion headers role="tab" and panels role="tabpanel".
 * Those roles are only meaningful inside a role="tablist", which an accordion
 * is not, and role="tab" on an <h3> overrides its heading semantics, so a
 * screen reader user cannot move through the sections by heading at all.
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
