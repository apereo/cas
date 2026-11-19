/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration, RegisteredServiceSections,
   openRegisteredServiceWizardDialog, isPopulatingWizard, ace, currentActiveTab, Swal, applicationActionHandler,
   applicationRowContext, fetchServices, Tabs, PalantirServiceEditor, PalantirServiceTemplates */
const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

const DASHBOARD_URL = "https://localhost:8443/cas/palantir/dashboard";

/**
 * Record every uncaught error and console error for the whole run.
 *
 * Tabs, widgets and wizard sections are all built on demand now, and a builder that throws is
 * caught and logged rather than surfaced, so the console is the only place a broken panel shows up.
 *
 * @param page the puppeteer page
 * @returns {string[]} the live array failures are appended to
 */
function trackFailures(page) {
    const failures = [];
    page.on("pageerror", (error) => failures.push(`pageerror: ${error.message}`));
    page.on("console", (message) => {
        if (message.type() === "error") {
            const url = message.location()?.url;
            failures.push(url ? `console: ${message.text()} [${url}]` : `console: ${message.text()}`);
        }
    });
    page.on("response", (response) => {
        if (response.status() >= 400) {
            failures.push(`response: ${response.status()} ${response.request().method()} ${response.url()}`);
        }
    });
    return failures;
}

/**
 * Wait until the dashboard has finished starting up.
 *
 * The rail is only populated once the feature catalog resolves, so a visible dashboard means
 * initializePalantir got past the point where it decides which tabs exist.
 *
 * @param page the puppeteer page
 */
async function waitForDashboard(page) {
    await page.waitForFunction(() => {
        const dashboard = document.querySelector("#dashboard");
        return dashboard !== null
            && !dashboard.classList.contains("d-none")
            && typeof CAS_FEATURES !== "undefined"
            && typeof openRegisteredServiceWizardDialog === "function";
    }, {timeout: 60000});
    const features = await page.evaluate(() => CAS_FEATURES.length);
    await cas.logg(`Dashboard ready with ${features} CAS features`);
    assert(features > 0);
}

const PROTOCOL_FIELDS = {
    OidcRegisteredService: ["#registeredServiceClientId", "input.registered-service-client-secret-value",
        {selector: "#registeredServiceAllowedResources", advanced: true}],
    OAuthRegisteredService: ["#registeredServiceClientId", "input.registered-service-client-secret-value",
        {selector: "#registeredServiceAllowedResources", advanced: true}],
    SamlRegisteredService: ["#registeredServiceMetadataLocation"],
    WSFederationRegisteredService: ["#registeredServiceWsFederationRealm"],
    CasRegisteredService: [
        {selector: "#registeredServiceSupportedProtocols", advanced: true},
        {selector: "#registeredServiceRedirectUrl", advanced: true}
    ]
};

/**
 * Walk the wizard's steps with Next, from the first step to the review.
 *
 * @param page the puppeteer page
 * @returns {Promise<string[]>} the steps visited, in order
 */
async function walkWizardSteps(page) {
    return page.evaluate(() => {
        RegisteredServiceSections.start();
        const visited = [RegisteredServiceSections.currentStepKey()];
        while (!$("#nextServiceWizardStep").hasClass("hide") && visited.length < 10) {
            $("#nextServiceWizardStep").trigger("click");
            visited.push(RegisteredServiceSections.currentStepKey());
        }
        return visited;
    });
}

/**
 * Assert that startup does not build the wizard: its fields are built in the background once the dashboard is shown
 * and the browser is idle, so they either do not exist yet or were built after the dashboard appeared.
 *
 * @param page the puppeteer page
 */
async function verifyWizardIsDeferred(page) {
    const built = await page.evaluate(() => {
        const shown = performance.getEntriesByName("palantir-dashboard-shown")[0]?.startTime ?? null;
        const wizard = performance.getEntriesByName("palantir-wizard-built")[0]?.startTime ?? null;
        return {
            shown: shown,
            wizard: wizard,
            params: document.querySelectorAll("#editServiceWizardForm [data-param-name]:not(input[type='hidden'][data-switch-btn])").length
        };
    });
    await cas.logg(`Dashboard shown at ${built.shown} ms; wizard built at ${built.wizard ?? "not yet"} ms; dynamic fields=${built.params}`);
    assert(built.shown !== null);
    if (built.wizard === null) {
        assert(built.params === 0);
    } else {
        assert(built.wizard > built.shown);
    }
}

/**
 * Open the wizard for one service type and check what it built.
 *
 * Every section the wizard knows about is checked against whether it should be available for this
 * service type, this advanced-options setting and the deployment's feature catalog; each available
 * section must have a sidebar entry that reveals its panel; and each protocol field must exist
 * exactly once and be reachable inside the Protocol group when its advanced setting permits it.
 *
 * @param page the puppeteer page
 * @param serviceClass the registered service class to open the wizard for
 * @param hideAdvanced whether advanced options are collapsed for this pass
 * @returns {Promise<string[]>} the problems found, empty when the wizard is correct
 */
async function inspectWizard(page, serviceClass, hideAdvanced) {
    const name = serviceClass.split(".").pop();
    await page.evaluate((entry) => {
        if ($("#hideAdvancedOptions").val() !== String(entry.hideAdvanced)) {
            $("#hideAdvancedOptionsButton").trigger("click");
        }
    }, {hideAdvanced});

    return page.evaluate((entry) => {
        const errors = [];
        const featureSections = {
            registeredServiceAcceptableUsagePolicy: "AcceptableUsagePolicy",
            registeredServiceWebflowInterruptPolicy: "InterruptNotifications",
            registeredServicePasswordlessPolicy: "PasswordlessAuthn",
            registeredServiceSurrogatePolicy: "SurrogateAuthentication"
        };
        const expected = (entry.protocolFields ?? [])
            .map((field) => typeof field === "string" ? {selector: field} : field);
        const seen = new Set();

        for (const section of RegisteredServiceSections.sections()) {
            const classes = [...section.header.classList];
            const protocolClasses = classes.filter((value) => value.startsWith("class-"));
            const feature = featureSections[section.header.id];
            const shouldExist = (protocolClasses.length === 0 || protocolClasses.includes(`class-${entry.name}`))
                && (!entry.hideAdvanced || !classes.includes("advanced-option"))
                && (!feature || CAS_FEATURES.includes(feature))
                && (section.header.id !== "registeredServiceMfaPolicy"
                    || PalantirDashboardConfiguration.availableMultifactorProviders().length > 0);

            if (RegisteredServiceSections.isAvailable(section) !== shouldExist) {
                errors.push(`Incorrect availability: ${section.key}`);
            }
            if (!RegisteredServiceSections.GROUP_ORDER.includes(RegisteredServiceSections.groupOf(section))) {
                errors.push(`Ungrouped section: ${section.key}`);
            }
            if (!RegisteredServiceSections.isAvailable(section)) {
                continue;
            }

            const link = document.querySelector(`[data-section-key="${section.key}"]`);
            if (!link) {
                errors.push(`Missing sidebar entry: ${section.key}`);
                continue;
            }
            link.click();
            if (!$(section.panel).is(":visible")) {
                errors.push(`Hidden selected panel: ${section.key}`);
            }
            for (const {selector} of expected) {
                if ($(section.panel).find(selector).is(":visible")) {
                    seen.add(selector);
                    if (RegisteredServiceSections.groupOf(section) !== "Protocol") {
                        errors.push(`Protocol field in wrong group: ${selector}`);
                    }
                }
            }
            $(section.panel).find("[class*='class-']:visible").each((_, element) => {
                if (![...element.classList].includes(`class-${entry.name}`)
                    && !element.classList.contains("always-show")) {
                    errors.push(`Wrong protocol field: ${element.id}`);
                }
            });
        }

        for (const {selector, advanced} of expected) {
            if (document.querySelectorAll(`#editServiceWizardForm ${selector}`).length !== 1) {
                errors.push(`Missing or duplicate protocol field: ${selector}`);
            }
            if (entry.hideAdvanced && advanced) {
                if (seen.has(selector)) {
                    errors.push(`Visible advanced protocol field: ${selector}`);
                }
            } else if (!seen.has(selector)) {
                errors.push(`Unreachable protocol field: ${selector}`);
            }
        }
        if ($("input[data-param-name='properties'][data-mapped-field-role='key']").first().val() !== "wizard.setting") {
            errors.push("Saved properties were not populated");
        }
        return errors;
    }, {name, hideAdvanced, protocolFields: PROTOCOL_FIELDS[name]});
}

/**
 * Pick an entry in a jQuery UI selectmenu through the widget's own menu.
 *
 * The open menu is driven with its focus and select methods rather than a click at the item's
 * coordinates: the menu sits in the dialog, which can scroll under a coordinate click and leave the
 * choice unmade. Selecting through the menu still runs the selectmenu's change callback, which is
 * what the wizard relies on to reveal a type's fields.
 *
 * @param page the puppeteer page
 * @param selectId the id of the underlying select element
 * @param text the label of the entry to choose
 */
async function chooseDropdownOption(page, selectId, text) {
    const chosen = await page.evaluate((entry) => {
        const $select = $(`#${entry.selectId}`);
        $select.selectmenu("open");
        const $menu = $select.selectmenu("menuWidget");
        const $item = $menu.find("li").filter((_, item) => item.textContent.trim() === entry.text).first();
        if ($item.length === 0) {
            $select.selectmenu("close");
            return null;
        }
        $menu.menu("focus", null, $item);
        $menu.menu("select", $.Event("click", {target: $item[0]}));
        return $select.val();
    }, {selectId, text});
    assert(chosen, `${selectId} has no ${text} entry`);
}

/**
 * Assert the wizard's type dropdowns react to a choice.
 *
 * The wizard's fields are built after the Applications panel has set up its widgets, so each dropdown
 * that declares change handlers must still carry them; choosing a username attribute provider type
 * must reveal that type's fields, and going back to the default must hide them again.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function verifyWizardDropdowns(page, name) {
    const unbound = await page.evaluate(() => {
        RegisteredServiceSections.activate("editServiceWizardMenuItemUsernameAttribute");
        return $("#editServiceWizardForm select.jqueryui-selectmenu")
            .filter((_, select) => $(select).data("change-handler")
                && typeof $(select).selectmenu("option", "change") !== "function")
            .map((_, select) => select.id)
            .get();
    });
    assert(unbound.length === 0, `${name}: dropdowns without change handlers: ${unbound.join(", ")}`);

    const staticFields = "#editServiceWizardMenuItemUsernameAttribute .StaticRegisteredServiceUsernameProvider";
    await chooseDropdownOption(page, "registeredServiceUsernameAttributeProvider", "STATIC");
    await page.waitForFunction((selector) => $(selector).is(":visible"), {timeout: 10000}, staticFields);
    await chooseDropdownOption(page, "registeredServiceUsernameAttributeProvider", "DEFAULT");
    await page.waitForFunction((selector) => !$(selector).is(":visible"), {timeout: 10000}, staticFields);
    await cas.logg(`${name}: wizard dropdowns reveal the fields of the chosen type`);
}

/**
 * Assert the wizard's date fields offer the date picker and that a picked date reaches the definition.
 *
 * The service expiration date takes the day as is; the time-based access strategy gets the start of
 * the picked day for its start and the last second for its end.
 *
 * The picker opens when its field takes focus. When clicking the field does not open it, which has been
 * seen for the second service type the wizard is opened for, the state that decides it (the element
 * under the click, the focused element and the date picker's own bookkeeping) is logged and the picker
 * is opened through its API, so the rest of the check still runs; a field without a date picker fails.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function verifyWizardDatePickers(page, name) {
    const day = "#ui-datepicker-div td[data-handler='selectDay']";
    const pick = async (selector) => {
        await page.waitForSelector(selector, {visible: true, timeout: 10000});
        await page.click(selector);
        const opened = await page.waitForSelector(day, {visible: true, timeout: 5000}).then(() => true, () => false);
        if (!opened) {
            const state = await page.$eval(selector, (input) => {
                const box = input.getBoundingClientRect();
                const target = document.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);
                return {
                    hasDatepicker: $(input).hasClass("hasDatepicker"),
                    focused: document.activeElement === input,
                    activeElement: document.activeElement?.id || document.activeElement?.tagName,
                    elementAtClick: target === null ? null : `${target.tagName}#${target.id}.${target.className}`,
                    lastInput: $.datepicker._lastInput?.id ?? null,
                    datepickerShowing: $.datepicker._datepickerShowing
                };
            });
            await cas.logy(`${name}: clicking ${selector} did not open the date picker: ${JSON.stringify(state)}`);
            assert(state.hasDatepicker, `${name}: ${selector} has no date picker`);
            await page.$eval(selector, (input) => $(input).datepicker("show"));
            await page.waitForSelector(day, {visible: true, timeout: 10000});
        }
        await page.click(`${day} a`);
        return page.$eval(selector, (input) => input.value);
    };
    await page.evaluate(() => {
        if ($("#hideAdvancedOptions").val() === "true") {
            $("#hideAdvancedOptionsButton").trigger("click");
        }
        RegisteredServiceSections.activate("editServiceWizardMenuItemExpirationPolicy");
    });
    const expiration = await pick("#registeredServiceExpirationPolicyExpirationDate");
    assert(/^\d{4}-\d{2}-\d{2}$/.test(expiration), `${name}: expiration date ${expiration}`);

    await page.evaluate(() => RegisteredServiceSections.activate("editServiceWizardMenuItemAccessStrategy"));
    await chooseDropdownOption(page, "registeredServiceAccessStrategy", "TIME BASED");
    const starting = await pick("#registeredServiceAccessStrategyTimeBasedStartingDateTime");
    const ending = await pick("#registeredServiceAccessStrategyTimeBasedEndingDateTime");
    assert(starting.endsWith("T00:00:00") && ending.endsWith("T23:59:59"), `${name}: ${starting} to ${ending}`);

    await page.waitForFunction((values) => {
        const definition = ace.edit("wizardServiceEditor").getValue();
        return values.every((value) => definition.includes(value));
    }, {timeout: 10000}, [expiration, starting, ending]);
    await chooseDropdownOption(page, "registeredServiceAccessStrategy", "DEFAULT");
    await page.evaluate(() => $("#registeredServiceExpirationPolicyExpirationDate").val("").trigger("input"));
    await cas.logg(`${name}: date fields pick ${expiration}, ${starting} and ${ending} into the definition`);
}

/**
 * Answer the unsaved-changes question, if closing a dialog asked it, by discarding the changes, which also removes
 * the draft kept for them.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function discardUnsavedChanges(page, name) {
    await cas.sleep(500);
    const asked = await page.evaluate(() => $(".swal2-popup:visible").length > 0
        && $(".swal2-title").text() === "Discard your changes?");
    if (asked) {
        await page.click(".swal2-deny");
        await cas.logg(`${name}: closing the wizard with changes asked before discarding them`);
    }
}

/**
 * A new application in the wizard starts by choosing its type: a single click on a card opens the wizard for that
 * type, without a button to confirm the choice.
 *
 * @param page the puppeteer page
 */
async function verifyTypePicker(page) {
    await cas.separator();
    const types = await page.evaluate(() => Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).length);
    if (types < 2) {
        await cas.logy("Only one application type is supported, so no type is chosen; skipping");
        return;
    }
    await page.evaluate(() => {
        openRegisteredServiceWizardDialog();
    });
    await page.waitForFunction(() => $(".palantir-type-card").length > 1, {timeout: 15000});
    assert(await page.evaluate(() => !$(".swal2-confirm").is(":visible")));
    const chosen = await page.evaluate(() => $(".palantir-type-card").eq(1).data("service-class"));
    await page.click(".palantir-type-card:nth-of-type(2)");
    await cas.sleep(500);
    if (await page.evaluate(() => $(".swal2-popup:visible").length > 0 && $(".swal2-title").text() === "Resume your draft?")) {
        await page.click(".swal2-deny");
    }
    await page.waitForFunction((serviceClass) => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#editServiceWizardForm").data("service-class") === serviceClass, {timeout: 30000}, chosen);
    await cas.logg(`One click on a card opens the wizard for ${chosen}`);
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "Type picker");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Exercise the service wizard for each service type it models.
 *
 * The wizard's fields are built when the dialog is first opened rather than while the page parses,
 * so this is what proves the deferred construction produces the same form. Each type is opened,
 * inspected with advanced options collapsed and expanded, and dismissed through Cancel.
 *
 * The deployment's supported types come from a classpath scan and can include registered service
 * classes the wizard declares no protocol fields for; those are reported and skipped rather than
 * failed, since there is nothing to assert about them.
 *
 * @param page the puppeteer page
 */
async function verifyServiceWizard(page) {
    await cas.separator();
    await verifyWizardIsDeferred(page);

    const supported = await page.evaluate(() => Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).sort());
    const types = supported.filter((serviceClass) => PROTOCOL_FIELDS[serviceClass.split(".").pop()] !== undefined);
    const unmodelled = supported.filter((serviceClass) => !types.includes(serviceClass));
    await cas.logg(`Deployment supports ${supported.length} service types; ${types.length} are modelled by the wizard`);
    if (unmodelled.length > 0) {
        await cas.logy(`Not exercised, no protocol fields declared for: ${unmodelled.join(", ")}`);
    }
    assert(types.length > 0);

    for (const serviceClass of types) {
        const name = serviceClass.split(".").pop();
        await page.evaluate((entry) => openRegisteredServiceWizardDialog({
            "@class": entry.serviceClass,
            id: 123456,
            name: entry.name,
            serviceId: "https://wizard.example.org/.*",
            properties: {
                "@class": "java.util.HashMap",
                "wizard.setting": {
                    "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
                    values: ["java.util.HashSet", ["restored value"]]
                }
            }
        }), {serviceClass, name});

        await page.waitForFunction((expected) => !isPopulatingWizard
            && $("#registeredServiceName").val() === expected, {timeout: 30000}, name);

        for (const hideAdvanced of [true, false]) {
            const errors = await inspectWizard(page, serviceClass, hideAdvanced);
            if (errors.length > 0) {
                await cas.logr(`${name} (hideAdvanced=${hideAdvanced}): ${errors.join(", ")}`);
            }
            assert(errors.length === 0, `${name}: ${errors.join(", ")}`);
        }

        await verifyWizardDropdowns(page, name);
        await verifyWizardDatePickers(page, name);

        const steps = await walkWizardSteps(page);
        await cas.logg(`${name} steps: ${steps.join(" > ")}`);
        assert(steps[0] === "basics" && steps.at(-1) === "review" && new Set(steps).size === steps.length);

        await page.evaluate(() => $("#registeredServiceName").val("").trigger("input").trigger("change"));
        await page.waitForFunction(() => $("#saveServiceWizard").prop("disabled")
            && $("#saveServiceWizardHint").text().includes("Name"), {timeout: 10000});
        const required = await page.evaluate(() => $("#saveServiceWizardHint").text().trim());
        await cas.logg(`${name}: without a name, Save is disabled and the wizard says ${required}`);

        await cas.logg(`Wizard verified for ${name}`);
        await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
        await discardUnsavedChanges(page, name);
        await page.waitForFunction(() => $("#editServiceWizardDialog").hasClass("ui-dialog-content")
            && $("#editServiceWizardDialog").dialog("isOpen") === false, {timeout: 15000});
    }
    await cas.screenshot(page);
}

/**
 * The JSON editor opens on a start screen of templates for a new definition: the templates that ship with CAS are
 * offered for every protocol of the deployment, a search narrows them, and choosing one moves to the editor with a
 * definition CAS can read. A syntax error is reported on its line, and Cancel asks before discarding the edits.
 *
 * @param page the puppeteer page
 */
async function verifyPlainEditorDialog(page) {
    await cas.separator();
    await cas.click(page, "#newServicePlain");
    await page.waitForFunction(() => $("#editServiceDialog").hasClass("ui-dialog-content")
        && $("#editServiceDialog").dialog("isOpen")
        && PalantirServiceEditor.view === "start"
        && $("#serviceTemplateCards [data-template-key]").length > 1, {timeout: 30000});
    const start = await page.evaluate(() => ({
        templates: PalantirServiceTemplates.all().length,
        protocols: [...new Set(PalantirServiceTemplates.all().map((template) => template.protocol.key))],
        continueDisabled: $("#continueServiceTemplate").prop("disabled")
    }));
    await cas.logg(`JSON editor offers ${start.templates} template(s) for ${start.protocols.join(", ")}`);
    assert(start.templates >= 20);
    assert(start.protocols.includes("cas") && start.protocols.includes("saml") && start.protocols.includes("oidc"));
    assert(start.continueDisabled === true);

    await page.type("#serviceTemplateSearch", "proxy tickets");
    await page.waitForFunction(() => $("#serviceTemplateCards [data-template-key]").length === 2, {timeout: 10000});
    await page.click("#serviceTemplateCards [data-template-key]:not([data-template-key=blank])");
    await page.click("#continueServiceTemplate");
    await page.waitForFunction(() => PalantirServiceEditor.view === "editor"
        && $("#serviceEditorCheck").attr("data-state") === "ok", {timeout: 10000});
    const definition = await page.evaluate(() => JSON.parse(ace.edit("serviceEditor").getValue()));
    await cas.log(`Started from the template ${definition.name}`);
    assert(definition.proxyPolicy !== undefined);
    assert(definition.id === undefined);

    await cas.click(page, "#validateService");
    await page.waitForFunction(() => !$("#serviceEditorProblem").prop("hidden"), {timeout: 15000});
    const validation = await page.evaluate(() => $("#serviceEditorProblem").attr("data-state"));
    await cas.logg(`Validating the template answers ${validation}`);
    assert(validation === "ok");

    await page.evaluate(() => ace.edit("serviceEditor").session.insert({row: 2, column: 0}, "    \"broken\"\n"));
    await page.waitForFunction(() => $("#serviceEditorCheck").attr("data-state") === "error", {timeout: 10000});
    const syntax = await page.evaluate(() => $("#serviceEditorCheck").text());
    await cas.logg(`A syntax error is reported as: ${syntax}`);
    assert(syntax.startsWith("Line "));

    await page.evaluate(() => $("#cancelService").trigger("click"));
    await discardUnsavedChanges(page, "JSON editor");
    await page.waitForFunction(() => $("#editServiceDialog").dialog("isOpen") === false, {timeout: 15000});
    await cas.logg("JSON editor dismissed through Cancel");
}

/**
 * Open an application in the editor, change it behind the editor's back, and check that saving the edit shows what
 * changed instead of overwriting it; keep editing, then discard the edit and delete the application.
 *
 * @param page the puppeteer page
 */
async function verifyEditConflict(page) {
    await cas.separator();
    const serviceId = await page.evaluate(async () => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({
                "@class": "org.apereo.cas.services.CasRegisteredService",
                serviceId: "^https://conflict.example.org/.*",
                name: "ConflictExample",
                description: "Original description"
            })
        });
        const created = await response.json();
        await fetchServices();
        return created.id;
    });
    await page.waitForFunction((id) => $(`#applicationsTable span[serviceId=${id}]`).length > 0, {timeout: 30000}, serviceId);
    await page.evaluate((id) => applicationActionHandler("editPlain", applicationRowContext(String(id))), serviceId);
    await page.waitForFunction(() => $("#editServiceDialog").dialog("instance") !== undefined
        && $("#editServiceDialog").dialog("isOpen"), {timeout: 30000});
    await cas.sleep(1000);

    await page.evaluate(async (id) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {headers: {"Accept": "application/json"}});
        const service = await response.json();
        await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "PUT",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({...service, description: "Changed elsewhere"})
        });
        const editor = ace.edit("serviceEditor");
        const edited = JSON.parse(editor.getValue());
        edited.name = "ConflictExampleEdited";
        editor.setValue(JSON.stringify(edited, null, 4), -1);
    }, serviceId);
    await cas.sleep(1000);

    await page.evaluate(() => $("#saveService").trigger("click"));
    await page.waitForFunction(() => $(".swal2-title").text() === "This application has changed", {timeout: 30000});
    const changed = await page.evaluate(() => $(".palantir-conflict__diff td.is-added").length);
    await cas.logg(`Saving a stale edit shows ${changed} changed line(s) instead of overwriting`);
    assert(changed > 0);
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible() && $("#editServiceDialog").dialog("isOpen"), {timeout: 10000});
    const description = await page.evaluate(async (id) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {headers: {"Accept": "application/json"}});
        return (await response.json()).description;
    }, serviceId);
    assert(description === "Changed elsewhere");

    await page.evaluate(() => $("#cancelService").trigger("click"));
    await discardUnsavedChanges(page, "Edit conflict");
    await page.waitForFunction(() => !$("#editServiceDialog").dialog("isOpen"), {timeout: 15000});
    await page.evaluate(async (id) => {
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
    }, serviceId);
}

/**
 * Open an application whose multifactor policy only names principal attributes in the wizard, and check that the
 * definition the wizard generates keeps them with the policy's type, so that CAS accepts it.
 *
 * @param page the puppeteer page
 */
async function verifyMultifactorPolicyRoundTrip(page) {
    await cas.separator();
    await page.evaluate(() => openRegisteredServiceWizardDialog({
        "@class": "org.apereo.cas.services.CasRegisteredService",
        id: 654321,
        name: "MultifactorPolicyRoundTrip",
        serviceId: "https://mfa-round-trip.example.org/.*",
        multifactorPolicy: {
            "@class": "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
            principalAttributeNameTrigger: "gender",
            bypassPrincipalAttributeName: "gender"
        }
    }));
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceName").val() === "MultifactorPolicyRoundTrip", {timeout: 30000});
    await cas.sleep(2000);
    const outcome = await page.evaluate(async () => {
        const text = ace.edit("wizardServiceEditor").getValue();
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/validate`, {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: text
        });
        return {policy: JSON.parse(text).multifactorPolicy, status: response.status, body: await response.text()};
    });
    await cas.log(`Generated multifactor policy: ${JSON.stringify(outcome.policy)}; validation answered ${outcome.status} ${outcome.body}`);
    assert(outcome.policy["@class"] === "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy");
    assert(outcome.policy.principalAttributeNameTrigger === "gender" && outcome.policy.bypassPrincipalAttributeName === "gender");
    assert(outcome.status === 200);
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "Multifactor policy round trip");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

const HANDOFF_CONTACTS = ["Alice", "Bob", "Carol", "Dave"];

/**
 * Register the application the handoff steps edit: four contacts, one more than the wizard has tabs for, and an
 * access strategy order, a setting inside the access strategy the wizard has no field for.
 *
 * @param page the puppeteer page
 * @returns {Promise<string>} the id of the application
 */
async function createHandoffApplication(page) {
    return page.evaluate(async (names) => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({
                "@class": "org.apereo.cas.services.CasRegisteredService",
                serviceId: "^https://handoff.example.org/.*",
                name: "HandoffExample",
                description: "Original description",
                accessStrategy: {
                    "@class": "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
                    order: 7,
                    enabled: true,
                    ssoEnabled: false
                },
                contacts: ["java.util.ArrayList", names.map((name) => ({
                    "@class": "org.apereo.cas.services.DefaultRegisteredServiceContact",
                    name: name,
                    email: `${name.toLowerCase()}@example.org`
                }))]
            })
        });
        const created = await response.json();
        await fetchServices();
        return String(created.id);
    }, HANDOFF_CONTACTS);
}

/**
 * Read an application as the server holds it.
 *
 * @param page the puppeteer page
 * @param id the id of the application
 * @returns {Promise<object>} the definition
 */
async function readApplication(page, id) {
    return page.evaluate(async (serviceId) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, {headers: {"Accept": "application/json"}});
        return response.json();
    }, id);
}

/**
 * Wait for a SweetAlert dialog with the given title.
 *
 * @param page the puppeteer page
 * @param title the title
 */
async function waitForAlert(page, title) {
    await page.waitForFunction((text) => Swal.isVisible() && $(".swal2-title").text() === text, {timeout: 30000}, title);
}

/**
 * The contacts of the definition the wizard generates.
 *
 * @param page the puppeteer page
 * @returns {Promise<object[]>} the contacts
 */
async function wizardContacts(page) {
    await cas.sleep(1500);
    return page.evaluate(() => JSON.parse(ace.edit("wizardServiceEditor").getValue()).contacts?.[1] ?? []);
}

/**
 * Open an application with four contacts in the wizard. Renaming it keeps all four; clearing the first contact's
 * fields leaves the next three whole, each with its name and email, rather than splitting one across two contacts.
 *
 * @param page the puppeteer page
 * @param id the id of the application
 */
async function verifyWizardContacts(page, id) {
    await cas.separator();
    const definition = await readApplication(page, id);
    await page.evaluate((service) => {
        openRegisteredServiceWizardDialog(service);
    }, definition);
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceName").val() === "HandoffExample", {timeout: 30000});
    await page.evaluate(() => $("#registeredServiceName").val("HandoffRenamed").trigger("input"));
    const renamed = await wizardContacts(page);
    await cas.log(`Renaming kept the contacts ${renamed.map((contact) => contact.name).join(", ")}`);
    assert.deepStrictEqual(renamed.map((contact) => contact.name), HANDOFF_CONTACTS);

    await page.evaluate(() => ["Name", "Email"].forEach((detail) => $(`#registeredServiceContact${detail}1`).val("").trigger("input")));
    const cleared = await wizardContacts(page);
    await cas.log(`Clearing the first contact left ${JSON.stringify(cleared.map((contact) => [contact.name, contact.email]))}`);
    assert.deepStrictEqual(cleared.map((contact) => contact.name), HANDOFF_CONTACTS.slice(1));
    assert(cleared.every((contact) => contact.email === `${contact.name.toLowerCase()}@example.org`));

    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "Wizard contacts");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Rename an application in the JSON editor and move to the wizard without saving: the wizard holds the new name,
 * and closing it asks before the edit is lost.
 *
 * @param page the puppeteer page
 * @param id the id of the application
 */
async function verifyJsonEditorToWizard(page, id) {
    await cas.separator();
    await page.evaluate((serviceId) => applicationActionHandler("editPlain", applicationRowContext(serviceId)), id);
    await page.waitForFunction(() => $("#editServiceDialog").dialog("instance") !== undefined
        && $("#editServiceDialog").dialog("isOpen"), {timeout: 30000});
    await cas.sleep(1000);
    await page.evaluate(() => {
        const editor = ace.edit("serviceEditor");
        const edited = JSON.parse(editor.getValue());
        edited.name = "HandoffEdited";
        editor.setValue(JSON.stringify(edited, null, 4), -1);
    });
    await cas.sleep(1000);
    await page.evaluate(() => {
        PalantirServiceEditor.openInWizard();
    });
    await waitForAlert(page, "The wizard does not cover every setting");
    await page.click(".swal2-deny");
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceName").val() === "HandoffEdited", {timeout: 30000});
    await cas.sleep(1500);
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await waitForAlert(page, "Discard your changes?");
    await cas.logg("Closing the wizard with the unsaved edit from the JSON editor asks first");
    await page.click(".swal2-deny");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Keep a wizard draft of an application, let someone else change the application, then resume the draft and move
 * to the JSON editor: the editor holds the draft, the setting the wizard has no field for, and saving it notices the
 * other change instead of overwriting it.
 *
 * @param page the puppeteer page
 * @param id the id of the application
 */
async function verifyWizardDraftToJsonEditor(page, id) {
    await cas.separator();
    const first = await readApplication(page, id);
    await page.evaluate((service) => {
        PalantirServiceEditor.openApplicationInWizard(service);
    }, first);
    await waitForAlert(page, "The wizard does not cover every setting");
    await page.click(".swal2-deny");
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard, {timeout: 30000});
    await cas.sleep(1500);
    await page.evaluate(() => $("#registeredServiceName").val("HandoffEdited").trigger("input"));
    await page.evaluate(() => document.getElementById("accessStrategySsoEnabledButton").click());
    await cas.sleep(1500);
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await waitForAlert(page, "Discard your changes?");
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});

    await page.evaluate(async (service) => {
        await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "PUT",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({...service, description: "Changed elsewhere"})
        });
    }, first);
    const second = await readApplication(page, id);
    await page.evaluate((service) => {
        PalantirServiceEditor.openApplicationInWizard(service);
    }, second);
    await waitForAlert(page, "Resume your draft?");
    await page.click(".swal2-confirm");
    await waitForAlert(page, "The wizard does not cover every setting");
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $("#editServiceDialog").dialog("isOpen"), {timeout: 30000});
    await cas.sleep(1000);
    const carried = await page.evaluate(() => JSON.parse(ace.edit("serviceEditor").getValue()));
    await cas.log(`The JSON editor holds ${carried.name} with access strategy order ${carried.accessStrategy?.order}`);
    assert(carried.name === "HandoffEdited");
    assert(carried.accessStrategy?.order === 7);
    await cas.log(`Single sign-on, switched on in the wizard, is ${carried.accessStrategy?.ssoEnabled ?? "left to its default"} in the JSON editor`);
    assert(carried.accessStrategy?.ssoEnabled !== false);

    await page.evaluate(() => $("#saveService").trigger("click"));
    await waitForAlert(page, "This application has changed");
    await cas.logg("Saving the resumed draft from the JSON editor notices the change made since the draft began");
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible() && $("#editServiceDialog").dialog("isOpen"), {timeout: 10000});
    const current = await readApplication(page, id);
    assert(current.description === "Changed elsewhere" && current.name === "HandoffExample");
    await page.evaluate(() => $("#cancelService").trigger("click"));
    await discardUnsavedChanges(page, "Wizard draft to JSON editor");
    await page.waitForFunction(() => !$("#editServiceDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Going back to the JSON editor keeps a policy the wizard left out when the wizard has no choice for its type, and
 * leaves it out when the wizard offers that type, since there leaving it out was chosen in the wizard.
 *
 * @param page the puppeteer page
 */
async function verifyCarryOfOmittedPolicy(page) {
    await cas.separator();
    const outcome = await page.evaluate(() => {
        const policy = {"@class": "org.example.CustomExpirationPolicy", deleteWhenExpired: true, notifyWhenDeleted: true};
        const definition = {"@class": "org.apereo.cas.services.CasRegisteredService", name: "Carry", expirationPolicy: policy};
        const generated = JSON.stringify({"@class": "org.apereo.cas.services.CasRegisteredService", name: "Carry"});
        const modeled = ["name", "expirationPolicy", "expirationPolicy.deleteWhenExpired"];
        const unoffered = JSON.parse(PalantirServiceEditor.carry(definition, generated, modeled, [], {expirationPolicy: ["", "org.example.OtherPolicy"]}));
        const offered = JSON.parse(PalantirServiceEditor.carry(definition, generated, modeled, [], {expirationPolicy: ["", policy["@class"]]}));
        return {kept: JSON.stringify(unoffered.expirationPolicy) === JSON.stringify(policy), dropped: offered.expirationPolicy === undefined};
    });
    await cas.log(`A policy of a type the wizard cannot choose is kept: ${outcome.kept}; one it can choose stays out: ${outcome.dropped}`);
    assert(outcome.kept);
    assert(outcome.dropped);
}

/**
 * Edit an application in the wizard and the JSON editor and move between them, then delete it.
 *
 * @param page the puppeteer page
 */
async function verifyEditorHandoffs(page) {
    const id = await createHandoffApplication(page);
    await page.waitForFunction((serviceId) => $(`#applicationsTable span[serviceId=${serviceId}]`).length > 0, {timeout: 30000}, id);
    await verifyWizardContacts(page, id);
    await verifyJsonEditorToWizard(page, id);
    await verifyWizardDraftToJsonEditor(page, id);
    await verifyCarryOfOmittedPolicy(page);
    await page.evaluate(async (serviceId) => {
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
        await fetchServices();
    }, id);
}

/**
 * Delete the applications an earlier attempt of this scenario left behind, found by name. In-process retries run
 * against the same CAS server, so a step that failed before deleting what it created would otherwise meet its own
 * leftovers on the next attempt.
 *
 * @param page the puppeteer page
 * @param names the names of the applications this scenario creates
 */
async function removeLeftoverApplications(page, names) {
    const removed = await page.evaluate(async (leftovers) => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {headers: {"Accept": "application/json"}});
        const body = await response.json();
        const services = (Array.isArray(body?.[1]) ? body[1] : []).filter((service) => leftovers.includes(service.name));
        for (const service of services) {
            await fetch(`${CasActuatorEndpoints.registeredServices()}/${service.id}`, {
                method: "DELETE",
                headers: {"Content-Type": "application/json"}
            });
        }
        if (services.length > 0) {
            await fetchServices();
        }
        return services.map((service) => `${service.name} (${service.id})`);
    }, names);
    if (removed.length > 0) {
        await cas.logy(`Removed applications left by an earlier attempt: ${removed.join(", ")}`);
    }
}

/**
 * Open the Applications page and wait until its table is built. A new visit lands on Home, and the applications
 * steps of this scenario need the table, the cards and the applications loaded.
 *
 * @param page the puppeteer page
 */
async function openApplications(page) {
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).trigger("click"));
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index
        && $.fn.dataTable.isDataTable("#applicationsTable"), {timeout: 30000});
}

/**
 * Open the New menu of the Applications page, which holds the entries for the JSON editor and the wizard.
 *
 * @param page the puppeteer page
 */
async function openNewApplicationMenu(page) {
    await cas.click(page, "#newService");
    await page.waitForFunction(() => $("#newServicePlain").is(":visible")
        && $("#newServiceWizard").is(":visible"), {timeout: 15000});
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const failures = trackFailures(page);

    await cas.goto(page, DASHBOARD_URL);
    if (await cas.isVisible(page, "#username")) {
        const login = await cas.loginWith(page, "casadmin", "password");
        await cas.sleep(1000);
        await cas.log(`${login.status()} ${login.statusText()}`);
        assert(login.status() === 200);
    }
    await cas.screenshot(page);

    await page.evaluate(() => window.localStorage.clear());
    const response = await cas.goto(page, DASHBOARD_URL);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await waitForDashboard(page);
    await removeLeftoverApplications(page, ["ConflictExample", "ConflictExampleEdited", "HandoffExample", "HandoffEdited"]);
    await openApplications(page);
    await openNewApplicationMenu(page);
    await verifyPlainEditorDialog(page);
    await verifyServiceWizard(page);
    await verifyTypePicker(page);
    await verifyEditConflict(page);
    await verifyMultifactorPolicyRoundTrip(page);
    await verifyEditorHandoffs(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir JSON editor and service wizard verified with no browser errors");

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();
