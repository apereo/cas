let editServiceWizardDialog = undefined;

/**
 * Deferred construction of the service wizard's fields.
 *
 * The wizard is a large form -- a couple of hundred fields across every protocol -- inside a dialog
 * that starts hidden and that most visits never open. Building it while the page parsed cost every
 * load that DOM, the MDC components attached to it, a dozen select widgets, and the discovery
 * profile request its attribute pickers make.
 *
 * The markup therefore registers each section's builder with palantirWizardField instead of calling it, and the
 * queue, palantirWizardFieldBuilders, is drained the first time the dialog is opened. Both are defined inline in the
 * dashboard page, since the markup registers its builders while the page is parsed, before the deferred scripts run.
 * Registration order is document order, which is what decides where each field lands, so the queue is drained in
 * order and awaited: one builder fetches the OpenID Connect discovery document before it can fill its dropdowns.
 */
let palantirWizardFieldsBuilt = null;

/**
 * Build the wizard in the background once the browser is idle, so that opening the first application does not
 * wait for it. Startup does not: this is asked for after the Applications page is set up.
 */
function prepareRegisteredServiceWizardWhenIdle() {
    const prepare = () => buildRegisteredServiceWizardFields();
    if ("requestIdleCallback" in window) {
        requestIdleCallback(prepare, {timeout: 5000});
    } else {
        setTimeout(prepare, 2000);
    }
}

/**
 * Build the wizard's fields, once.
 *
 * A builder that throws is logged and skipped: one section that cannot be built must not leave the
 * rest of the wizard empty. The fields are built after the Applications panel has had its widgets
 * set up, so the form's widgets are initialized here once the builders are done: a dropdown that the
 * wizard later turns into a bare selectmenu would carry no change handler, and picking a type in it
 * would not reveal that type's fields.
 *
 * @returns {Promise<void>} resolved when every registered builder has run
 */
function buildRegisteredServiceWizardFields() {
    if (!palantirWizardFieldsBuilt) {
        palantirWizardFieldsBuilt = (async () => {
            await new Promise(resolve => CasDiscoveryProfile.fetchIfNeeded().always(resolve));
            for (const builder of palantirWizardFieldBuilders) {
                try {
                    await builder();
                } catch (e) {
                    console.error("Unable to build a service wizard section:", e);
                }
            }
            initializePalantirWidgets(document.getElementById("editServiceWizardForm"));
            PalantirWizardSuggestions.attach(document.getElementById("editServiceWizardForm"));
            for (let attempt = 0; attempt < 20 && !$("#registeredServiceDescriptionEditor").data("trumbowyg"); attempt++) {
                await new Promise(resolve => setTimeout(resolve, 50));
            }
            performance.mark("palantir-wizard-built");
        })();
    }
    return palantirWizardFieldsBuilt;
}

function validatePattern(button, targetFieldId = "#registeredServiceId") {
    $("#validatePatternDialog").dialog({
        title: "Validate Pattern",
        modal: true,
        width: 600,
        closeOnEscape: false,
        buttons: {
            "Use Pattern": function () {
                $(targetFieldId).val($("#providedPattern").val()).focus();
                $(this).dialog("close");
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
        open: function () {
            $("#providedPattern").val($(targetFieldId).val());
        },
        close: function () {
            $(this).find("input").val("");
            $(this).dialog("destroy");
        }
    });
}

/**
 * Put the field that says which requests an application answers, its service id, redirect URI or entity id, at the
 * top of the protocol section of the application's type, after the client id and secrets for OAuth and OpenID
 * Connect and after the metadata location for SAML2, since what it holds depends on the protocol and, for SAML2, on
 * where the metadata comes from. A type without a protocol section keeps it in the general
 * section.
 *
 * @param className the short name of the application's type
 */
function placeRegisteredServiceIdField(className) {
    const field = $("#registeredServiceIdFieldContainer");
    const containers = {
        CasRegisteredService: "#editServiceWizardCasContainer",
        OAuthRegisteredService: "#editServiceWizardOAuthOidcContainer",
        OidcRegisteredService: "#editServiceWizardOAuthOidcContainer",
        SamlRegisteredService: "#editServiceWizardSamlContainer",
        WSFederationRegisteredService: "#editServiceWizardWSFederationContainer"
    };
    const container = $(containers[className] ?? "");
    if (container.length === 0) {
        $("#registeredServiceIdHome").before(field);
        return;
    }
    const client = container.children("#registeredServiceClientSecretsContainer, #registeredServiceClientIdFieldContainer, "
        + "#registeredServiceMetadataLocationFieldContainer").last();
    if (client.length > 0) {
        client.after(field);
    } else {
        container.prepend(field);
    }
}

function createRegisteredServiceFields() {
    createInputField({
        labelTitle: "Name",
        name: "registeredServiceName",
        paramName: "name",
        required: true,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a logical name for this application, preferably without spaces or special characters."
    });

    createInputField({
        labelTitle: "Service ID",
        name: "registeredServiceId",
        paramName: "serviceId",
        required: true,
        dataType: "regex",
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a pattern that determines which requests are mapped to this application. This is typically a URL pattern or a regular expression."
    }).after(`
        <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="validatePattern(this)">
            <i class="mdi mdi-check-circle" aria-hidden="true"></i>
            <span class="sr-only">Validate</span>
        </button>
    `);

    $("#registeredServiceIdFieldContainer").after($("<span>", {id: "registeredServiceIdHome", hidden: true}));

    initializeDescriptionEditor("editServiceWizardGeneralContainer");

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Numeric ID",
        name: "registeredServiceIdentifier",
        paramName: "id",
        dataType: "number",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a unique numeric identifier for this application. Depending on your choice of registry, this ID may be auto-generated."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Theme",
        name: "registeredServiceTheme",
        paramName: "theme",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a theme to customize the appearance of the login and other user interface pages for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Locale",
        name: "registeredServiceLocale",
        paramName: "locale",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a locale to customize the language and regional settings for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Information URL",
        name: "registeredServiceInformationUrl",
        paramName: "informationUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL that points to more information about this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Privacy URL",
        name: "registeredServicePrivacyUrl",
        paramName: "privacyUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL that points to the privacy policy for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Template",
        name: "registeredServiceTemplate",
        paramName: "templateName",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a template name to apply predefined settings and configurations for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Evaluation Order",
        name: "registeredServiceEvaluationOrder",
        paramName: "evaluationOrder",
        dataType: "number",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the order in which this application is evaluated relative to other applications. Lower numbers indicate higher priority."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Logo",
        name: "registeredServiceEvaluationLogo",
        paramName: "logo",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL to a logo image that represents this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Logout URL",
        name: "registeredServiceEvaluationLogoutUrl",
        paramName: "logoutUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a URL to redirect users to upon logout from this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Environment(s)",
        name: "registeredServiceEnvironments",
        paramName: "environments",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the environment(s), separated by comma, in which this application operates (e.g., development, staging, production)"
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });
}

/********************************************/

function hideAdvancedRegisteredServiceOptions() {
    const value = $("#hideAdvancedOptions").val();
    const className = getLastWord($("#serviceClassType").text());

    localStorage.setItem("hideAdvancedOptionsPreference", value);

    if (value === "true" || value === true) {
        hideElements($("form#editServiceWizardForm .advanced-option"));
    } else {
        showElements($("form#editServiceWizardForm .advanced-option"));
        hideElements($("form#editServiceWizardForm [class*='class-']").not(`.class-${className}`).not(".always-show"));
    }

    if (PalantirDashboardConfiguration.availableMultifactorProviders().length === 0) {
        hideElements($("#registeredServiceMfaPolicy"));
    }
    if (!CAS_FEATURES.includes("AcceptableUsagePolicy")) {
        hideElements($("#registeredServiceAcceptableUsagePolicy"));
    }
    if (!CAS_FEATURES.includes("InterruptNotifications")) {
        hideElements($("#registeredServiceWebflowInterruptPolicy"));
    }
    if (!CAS_FEATURES.includes("PasswordlessAuthn")) {
        hideElements($("#registeredServicePasswordlessPolicy"));
    }
    if (!CAS_FEATURES.includes("SurrogateAuthentication")) {
        hideElements($("#registeredServiceSurrogatePolicy"));
    }
    if (!CAS_FEATURES.some(feature => feature === "DelegatedAuthentication" || feature.startsWith("DelegatedAuthentication."))) {
        hideElements($("#registeredServiceDelegatedAuthenticationPolicy"));
    }
    RegisteredServiceSections.refresh();
}

function toggleJsonEditorVisibility() {
    const showEditor = $("#showJsonEditor").val();
    const $editorContainer = $("#wizardJsonEditorContainer");
    const $wizardMenu = $("#editServiceWizardMenu");

    localStorage.setItem("showJsonEditorPreference", showEditor);

    if (showEditor === "true" || showEditor === true) {
        $editorContainer.show();
        $wizardMenu.removeClass("w-100").addClass("mmw-45");
        const editor = ace.edit("wizardServiceEditor");
        if (editor) {
            editor.resize();
        }
    } else {
        $editorContainer.hide();
        $wizardMenu.removeClass("mmw-45").addClass("w-100");
    }

    RegisteredServiceSections.refresh();
}

/**
 * Whether a wizard field container is switched off for the type chosen in its section.
 *
 * Type handlers reveal their fields either through showElements, which drops the hide class, or
 * through jQuery's show(), which only sets an inline display and leaves the class in place. A
 * container is off when its inline display is none, or when it carries hide with no inline display
 * overriding it. What is on screen is not asked, since the wizard hides every section but the current
 * one, and a value entered in another section still belongs in the definition.
 *
 * @param element the field, select or map container
 * @returns {boolean} true when the container's values must stay out of the definition
 */
function isWizardContainerSwitchedOff(element) {
    const display = element.style.display;
    return display === "none" || (element.classList.contains("hide") && display === "");
}

/**
 * Whether a field holds text even when what is typed looks like a number, such as an OAuth client id, a service id, a
 * name or a secret: turning such a value into a number would change its type and, for long digits, the value itself.
 * Fields that hold numbers are number fields.
 *
 * @param $input the field
 * @param paramName the setting the field writes
 * @returns {boolean} true when the value is kept as text
 */
function isWizardTextSetting($input, paramName) {
    if (String($input.attr("type") ?? "").toLowerCase() === "number") {
        return false;
    }
    const setting = String(paramName ?? "").split(".").pop();
    return setting !== "id" && /(id|ids|name|secret|key|url|uri|location|realm|issuer|audience|pattern)$/i.test(setting);
}

let generateServiceDefinitionTimer = null;

/**
 * Generate the definition from the wizard's fields, shortly after the fields change. Calls made before it runs are
 * served by that one run, which reads the fields as they are then.
 */
function generateServiceDefinition() {
    if (isPopulatingWizard || generateServiceDefinitionTimer !== null) {
        return;
    }

    generateServiceDefinitionTimer = setTimeout(runServiceDefinitionGeneration, 10);
}

/**
 * Generate the definition now if fields changed since it was last generated, so what is read from the preview, such as
 * the definition about to be saved, holds the latest edit of every field.
 */
function flushServiceDefinition() {
    if (generateServiceDefinitionTimer !== null) {
        runServiceDefinitionGeneration();
    }
}

function runServiceDefinitionGeneration() {
    clearTimeout(generateServiceDefinitionTimer);
    generateServiceDefinitionTimer = null;
    if (!document.getElementById("wizardServiceEditor")) {
        return;
    }
    const editor = initializeAceEditor("wizardServiceEditor");
    const serviceDefinition = buildWizardServiceDefinition();
    editor.setValue(serviceDefinition === null ? "" : palantirStringifyJson(serviceDefinition, 4));
    editor.gotoLine(1);
    PalantirWizardValidity.refresh();
    PalantirWizardChains.render();
}

/**
 * The definition the wizard's fields make, with each chain put back together around the member the fields hold.
 *
 * @returns {object|null} the definition, or null when the fields make nothing beyond the type of application
 */
function buildWizardServiceDefinition() {
    let serviceDefinition = {
        "@class": $("#serviceClassType").text().trim()
    };

    // Track which param names have been processed to avoid duplicates from hidden containers
    const processedMappedFields = new Set();

    // Get currently selected attribute release policy type for filtering
    const $attrReleasePolicySelect = $("select#registeredServiceAttributeReleasePolicy");
    const selectedAttrReleasePolicy = $attrReleasePolicySelect.length > 0 ? getLastWord($attrReleasePolicySelect.val()) : "";

    $("form#editServiceWizardForm")
        .find("input,select,textarea")
        .each(function () {
            const $input = $(this);
            const paramName = $input.data("param-name");
            const paramType = $input.data("param-type");
            const skipWhenFalse = $input.data("param-skip-false");
            const skipWhenTrue = $input.data("param-skip-true");

            // Skip inputs in MapContainers (handled specially)
            const $mapContainer = $input.closest("[id$='MapContainer']");
            if ($mapContainer.length > 0) {
                // For mapped fields, only process once per container
                const containerKey = `${paramName}:${$mapContainer.attr("id")}`;
                if (processedMappedFields.has(containerKey)) {
                    return; // Already processed this container
                }
                processedMappedFields.add(containerKey);

                // For attributeReleasePolicy.allowedAttributes, only process the container
                // that matches the currently selected policy type
                if (paramName === "attributeReleasePolicy.allowedAttributes") {
                    // Check if this container has the class matching the selected policy
                    if (selectedAttrReleasePolicy && !$mapContainer.hasClass(selectedAttrReleasePolicy)) {
                        return; // Skip containers for other policy types
                    }
                }

                // Check if this container has any non-empty values before processing
                const $keyInputs = $mapContainer.find("input[data-mapped-field-role='key']");
                const $allInputs = $mapContainer.find("input");
                let hasNonEmptyValue = false;
                if ($keyInputs.length > 0) {
                    for (let i = 0; i < $keyInputs.length; i++) {
                        const key = $($keyInputs[i]).val();
                        if (key && key.trim().length > 0) {
                            hasNonEmptyValue = true;
                            break;
                        }
                    }
                } else {
                    for (let i = 0; i < $allInputs.length; i += 2) {
                        const key = $($allInputs[i]).val();
                        if (key && key.trim().length > 0) {
                            hasNonEmptyValue = true;
                            break;
                        }
                    }
                }
                if (!hasNonEmptyValue) {
                    return; // Skip empty containers
                }
            } else {
                // For regular inputs/selects (not in MapContainer), check if they're in a hidden container
                // This handles fields like ReturnAllowedAttributeReleasePolicy's allowedAttributes
                const $fieldContainer = $input.closest("[id$='FieldContainer'], [id$='SelectContainer']");
                if ($fieldContainer.length > 0 && isWizardContainerSwitchedOff($fieldContainer[0])) {
                    // Don't skip if this is an advanced-option that has a value
                    if (!$fieldContainer.hasClass("advanced-option")) {
                        return; // Skip inputs in hidden field containers (except advanced-option)
                    }
                }

                // For attributeReleasePolicy.allowedAttributes, check if this field matches the selected policy
                if (paramName === "attributeReleasePolicy.allowedAttributes") {
                    if (selectedAttrReleasePolicy && $fieldContainer.length > 0 && !$fieldContainer.hasClass(selectedAttrReleasePolicy)) {
                        return; // Skip fields for other policy types
                    }
                }
            }

            let value = $input.val();

            if (value && Array.isArray(value)) {
                value = value.filter(v => v != null && v !== "").join(",");
            }
            if (paramName === "clientSecrets" && $input.attr("id") === "registeredServiceClientSecrets") {
                value = "clientSecrets";
            }
            if (paramName && paramName.trim().length > 0 && value && String(value).trim().length > 0) {
                if (skipWhenFalse && (value === "false" || value === false)) {
                    console.debug(`Skipping parameter ${paramName} because its value is false`);
                } else if (skipWhenTrue && (value === "true" || value === true)) {
                    console.debug(`Skipping parameter ${paramName} because its value is true`);
                } else {
                    const renderer = $input.data("renderer");

                    if (typeof renderer === "function") {
                        value = renderer(String(value), $input, serviceDefinition);
                    } else if (value === "true") {
                        value = true;
                    } else if (value === "false") {
                        value = false;
                    } else if (isNumeric(value) && !isWizardTextSetting($input, paramName)) {
                        value = /^-?\d+$/.test(String(value).trim()) && !Number.isSafeInteger(Number(value))
                            ? palantirLargeId(String(value).trim())
                            : Number(value);
                    }

                    if (paramName.includes(".")) {
                        const parts = paramName.split(".");
                        let current = serviceDefinition;

                        if (paramType && paramType.length > 0 && parts.length > 0) {
                            if (!current[parts[0]]) {
                                current[parts[0]] = {"@class": paramType};
                            }
                        }

                        for (let i = 0; i < parts.length; i++) {
                            let part = parts[i];
                            const isLastPart = (i === parts.length - 1);

                            if (isLastPart) {
                                // Always set the value at the last part of the path
                                if (typeof value === "object" && Object.keys(value).length > 0) {
                                    current[part] = mergeWizardPolicy(current[part], value);
                                } else if (typeof value !== "object") {
                                    current[part] = value;
                                }
                            } else {
                                // For intermediate parts, create empty object if it doesn't exist
                                if (!current[part]) {
                                    current[part] = {};
                                }
                                current = current[part];
                            }
                        }
                    } else {
                        serviceDefinition[paramName] = mergeWizardPolicy(serviceDefinition[paramName], value);
                    }
                }
            }
        });

    if (Object.keys(serviceDefinition).length > 1) {
        $("form#editServiceWizardForm")
            .find("input,select")
            .each(function () {
                const $input = $(this);
                const beforeGenerate = $input.data("beforeGenerate");
                if (typeof beforeGenerate === "function") {
                    beforeGenerate($input, serviceDefinition);
                }
            });

        const contacts = [...(wizardContactSlots.get(serviceDefinition) ?? new Map()).entries()]
            .sort(([left], [right]) => left - right)
            .map(([, contact]) => contact)
            .concat(wizardExtraContacts.map(contact => structuredClone(contact)));
        if (contacts.length > 0) {
            serviceDefinition.contacts = ["java.util.ArrayList", contacts];
        }
        PalantirWizardChains.pack(serviceDefinition);

        Object.keys(serviceDefinition).forEach(key => {
            if (
                key !== "@class" &&
                typeof serviceDefinition[key] === "object" &&
                serviceDefinition[key] !== null &&
                Object.keys(serviceDefinition[key]).length === 1 &&
                serviceDefinition[key].hasOwnProperty("@class")) {

                const classType = serviceDefinition[key]["@class"];
                const markerClass = $(`option[value='${classType}']`).data("markerClass");
                if (!markerClass) {
                    // console.log("Deleting", serviceDefinition[key]);
                    delete serviceDefinition[key];
                }
            }
        });
        const remainingKeys = Object.keys(serviceDefinition);
        return remainingKeys.length === 1 && remainingKeys[0] === "@class" ? null : serviceDefinition;
    }
    return null;
}

/**
 * Whether a switch shows as on. The switch itself is the truth: its hidden field is emptied whenever the wizard opens.
 *
 * @param $button the switch
 * @returns {boolean} true when it is on
 */
function wizardSwitchSelected($button) {
    return $button.attr("aria-checked") === "true" || $button.hasClass("mdc-switch--selected");
}

/**
 * Put every dropdown of the wizard back to the option it starts with and empty every multi-select, then let each
 * dropdown's handlers show and hide the fields that depend on it, so a definition opened after another one starts
 * from the same wizard a new one does.
 */
function resetWizardSelects() {
    $("#editServiceWizardForm select[data-param-name]").each(function () {
        if (this.tomselect) {
            this.tomselect.clear(true);
            return;
        }
        const options = [...this.options];
        const initial = options.find(option => option.defaultSelected) ?? options[0];
        if (initial) {
            this.value = initial.value;
        }
    });
    $("#editServiceWizardForm select.jqueryui-selectmenu[data-param-name]").each(function () {
        const $select = $(this);
        String($select.data("change-handler") ?? "").split(",")
            .filter(name => name.length > 0 && name !== "generateServiceDefinition" && typeof window[name] === "function")
            .forEach(name => window[name]($select));
    });
}

/**
 * Put every switch of the wizard, or of one part of it, back the way the page first drew it, before a definition fills the wizard in, so a
 * switch turned on or off for the previous application does not show for the next one, or for the policy chosen
 * after another. Its hidden field stays empty,
 * so a switch nobody touches adds nothing to the definition. The hidden field's value is its default only until the
 * switch is first used, so the default is noted the first time the wizard opens.
 *
 * @param scope the part of the wizard whose switches are put back
 */
function resetWizardSwitches(scope = "#editServiceWizardForm") {
    $(scope).find("input[type='hidden'][data-switch-btn]").each(function () {
        if (this.dataset.switchDefault === undefined) {
            this.dataset.switchDefault = String(this.getAttribute("value") === "true");
        }
        const $button = $(`#${this.dataset.switchBtn}`);
        if ($button.length > 0 && wizardSwitchSelected($button) !== (this.dataset.switchDefault === "true")) {
            $button.click();
        }
        this.value = "";
    });
}

/**
 * The value a field gives a top-level setting, keeping what other fields already put in it. A policy is chosen by
 * a list whose value names the policy's type, while switches and fields elsewhere in the section set the policy's
 * other settings, such as whether authentication attributes may be released. Whichever is read first, the type
 * and the other settings end up in one policy rather than the type replacing them.
 *
 * @param current what the setting holds so far
 * @param value   the value the field gives it
 * @returns {*} the setting's value
 */
function mergeWizardPolicy(current, value) {
    const isPolicy = candidate => candidate !== null && typeof candidate === "object" && !Array.isArray(candidate);
    if (!isPolicy(current) || !isPolicy(value)) {
        return value;
    }
    const settings = Object.fromEntries(Object.entries(current).filter(([key]) => key !== "@class"));
    return {...value, ...settings};
}

/**
 * Creates the wizard dialog, or hands back the one already created.
 *
 * A jQuery UI dialog rather than an MDC one, matching every other dialog in
 * Palantir. The widget supplies the focus trap, Escape handling, a title bar
 * and role=dialog, which the hand-written MDC markup did not.
 *
 * Called before the Ace editor is attached, because creating the dialog moves
 * its element to the end of the document and Ace does not enjoy being moved
 * after the fact.
 *
 * @returns {{open: function, close: function}} a handle the shared save handler
 *          in palantir-services.js can use the same way as the plain editor
 *          dialog's MDC instance
 */
function createRegisteredServiceWizardDialog() {
    const element = $("#editServiceWizardDialog");
    if (!element.hasClass("ui-dialog-content")) {
        element.removeClass("hide").dialog({
            autoOpen: false,
            modal: true,
            closeOnEscape: true,
            draggable: false,
            resizable: false,
            classes: {"ui-dialog": "ui-corner-all registered-service-wizard-dialog"},
            beforeClose: () => PalantirServiceEditGuard.beforeClose("wizard"),
            close: () => {
                $(window).off("resize.registeredServiceWizard");
                PalantirServiceEditGuard.closed("wizard");
            }
        });
    }
    return {
        open: () => {
            element.dialog("open");
            sizeRegisteredServiceWizardDialog();
            $(window)
                .off("resize.registeredServiceWizard")
                .on("resize.registeredServiceWizard", sizeRegisteredServiceWizardDialog);
        },
        close: () => element.dialog("close")
    };
}

/**
 * Sizes the wizard dialog to the window, leaving a small margin.
 *
 * The editor is a working surface rather than a message, so it takes the whole
 * window. Ace is told to re-measure afterwards, since it cannot detect a
 * container resize on its own.
 */
function sizeRegisteredServiceWizardDialog() {
    const element = $("#editServiceWizardDialog");
    if (!element.hasClass("ui-dialog-content") || !element.dialog("isOpen")) {
        return;
    }
    const margin = 24;
    element.dialog("option", {
        width: Math.max(320, $(window).width() - margin * 2),
        height: Math.max(320, $(window).height() - margin * 2)
    });
    element.dialog("option", "position", {my: "center", at: "center", of: window});
    const editor = ace.edit("wizardServiceEditor");
    if (editor) {
        editor.resize(true);
    }
}

async function openRegisteredServiceWizardDialog(existingService = null, options = {}) {
    await buildRegisteredServiceWizardFields();

    async function openWizardDialog(serviceClass, serviceData = null) {
        wizardExtraContacts = [];
        const isNewService = serviceData === null || options.duplicate === true || options.asNew === true;
        const draftKey = PalantirServiceEditGuard.key("wizard", isNewService ? null : serviceData.id, serviceClass);
        if (!isNewService) {
            PalantirServiceEditGuard.remember(draftKey, serviceData);
        }
        const draft = await PalantirServiceEditGuard.offerDraft(draftKey,
            serviceData === null || isNewService ? null : palantirStringifyJson(serviceData));
        let draftDefinition = null;
        if (draft !== null) {
            try {
                draftDefinition = palantirParseJson(draft);
            } catch (error) {
                PalantirServiceEditGuard.removeDraft(draftKey);
            }
        }

        editServiceWizardDialog = createRegisteredServiceWizardDialog();
        PalantirWizardChains.reset();
        if (typeof PalantirSamlMetadataSources !== "undefined") {
            PalantirSamlMetadataSources.asked = "";
        }
        PalantirWizardSuggestions.reset();
        PalantirWizardChains.bind();
        $("#editServiceWizardGeneralContainer").find("input").val("");
        $(".jqueryui-multiselectmenu").each(function () {
            this.tomselect?.clear();
        });

        // Reset the Trumbowyg description editor
        setDescriptionEditorValue("");

        const editor = initializeAceEditor("wizardServiceEditor");
        editor.setReadOnly(true);
        editor.setValue("");
        editor.gotoLine(1);

        const className = getLastWord(serviceClass);
        $("#serviceClassType").text(serviceClass);
        $("#editServiceWizardForm").data("service-class", serviceClass).data("service-class-name", className);

        RegisteredServiceSections.initialize();

        showElements($("#editServiceWizardForm").find(`.class-${className}`));
        hideElements($("#editServiceWizardForm [class*='class-']").not(`.class-${className}`).not(".always-show"));
        placeRegisteredServiceIdField(className);

        RegisteredServiceSections.refresh();

        const editServiceWizardDialogElement = document.getElementById("editServiceWizardDialog");

        $(editServiceWizardDialogElement).attr("newService", isNewService);
        $(editServiceWizardDialogElement).attr("serviceClass", serviceClass);

        $("#editServiceWizardForm input[type=hidden]")
            .each(function () {
                const id = this.id;

                const data = $(`input#${id}`).data("attrs");
                if (data && data.length > 0) {
                    const attributes = Object.fromEntries(
                        data.split(",").map(pair => {
                            const [key, val] = pair.split("=");
                            return [key, val === "true" ? true : val === "false" ? false : val];
                        })
                    );

                    Object.entries(attributes).forEach(([key, value]) => {
                        $(`input#${id}`).data(key, value);
                    });
                }
                $(`button#${id}Button`).off("click.wizardGenerate").on("click.wizardGenerate", function (e) {
                    setTimeout(() => generateServiceDefinition(), 0);
                });
            });

        resetWizardSwitches();
        $("#editServiceWizardForm input").val("");
        if (typeof resetRegisteredServiceClientSecrets === "function") {
            resetRegisteredServiceClientSecrets();
        }
        $("#editServiceWizardForm option").filter(function () {
            const clazz = $(this).data("serviceClass");
            return clazz !== undefined && clazz !== null;
        }).remove();
        resetWizardSelects();

        const dropdown = $("#editServiceWizardForm select.jqueryui-selectmenu");
        try {
            dropdown.selectmenu("refresh");
        } catch (e) {
            dropdown.selectmenu();
        }
        dropdown.selectmenu("option", "appendTo", $(editServiceWizardDialogElement).dialog("widget"));

        const definition = draftDefinition ?? serviceData;
        if (definition !== null) {
            isPopulatingWizard = true;
            setTimeout(() => {
                populateWizardFromServiceDefinition(definition);
            }, 0);
        } else {
            generateServiceDefinition();
        }

        editServiceWizardDialog["open"]();
        PalantirServiceEditGuard.watch("wizard", draftKey, draftDefinition !== null || options.unsaved === true);
        PalantirServiceEditGuard.state.wizard.fromDraft = draftDefinition !== null;

        const savedAdvancedOptionsPref = localStorage.getItem("hideAdvancedOptionsPreference") ?? "true";
        const currentAdvancedOptionsValue = $("#hideAdvancedOptions").val();
        if ((savedAdvancedOptionsPref === "true") !== (currentAdvancedOptionsValue === "true")) {
            $("#hideAdvancedOptionsButton").click();
        }
        hideAdvancedRegisteredServiceOptions();

        // Restore the JSON editor visibility preference from localStorage
        const savedJsonEditorPref = localStorage.getItem("showJsonEditorPreference");
        const currentJsonEditorValue = $("#showJsonEditor").val();
        if (savedJsonEditorPref !== null) {
            // If saved preference differs from current state, toggle it
            if ((savedJsonEditorPref === "true") !== (currentJsonEditorValue === "true")) {
                $("#showJsonEditorButton").click();
            }
        }
        toggleJsonEditorVisibility();

        $("#registeredServiceIdLabel span.mdc-floating-label").text(({
            SamlRegisteredService: "Entity ID",
            OAuthRegisteredService: "Redirect URI",
            OidcRegisteredService: "Redirect URI"
        })[className] ?? "Service ID");
        switch (className) {
        case "SamlRegisteredService":
            createSamlRegisteredServiceAttributeReleasePolicy();
            break;
        case "OAuthRegisteredService":
            $(`h3.class-${className}`).each(function () {
                const original = $(this).text();
                const updated = original.replace(/^(?:OAuth \/ OpenID Connect|OpenID Connect) /, "OAuth ");
                $(this).text(updated);
            });
            RegisteredServiceSections.refresh();
            break;
        case "OidcRegisteredService":
            $(`h3.class-${className}`).each(function () {
                const original = $(this).text();
                const updated = original.replace(/^(?:OAuth \/ OpenID Connect|OAuth) /, "OpenID Connect ");
                $(this).text(updated);
            });
            RegisteredServiceSections.refresh();
            createOidcRegisteredServiceFields();
            break;
        }
        RegisteredServiceSections.start();
        if (className === "SamlRegisteredService") {
            showElements($("#fillServiceWizardFromMetadata"));
        } else {
            hideElements($("#fillServiceWizardFromMetadata"));
        }
        const protocol = applicationProtocolOf(serviceClass);
        let title = isNewService ? `New ${protocol.label}` : `Edit ${serviceData.name ?? protocol.label}`;
        if (options.duplicate === true) {
            title = `Duplicate ${options.original ?? protocol.label}`;
        }
        $(editServiceWizardDialogElement).dialog("option", "title", title);
        setTimeout(function () {
            $("#editServiceWizardForm input[data-param-name]:visible:enabled").first().focus();
        }, 200);
    }

    if (existingService !== null) {
        const serviceClass = existingService["@class"];
        await openWizardDialog(serviceClass, existingService);
    } else if (Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).length === 1) {
        openWizardDialog(Object.keys(PalantirDashboardConfiguration.supportedServiceTypes())[0]);
    } else {
        const sortedServiceTypes = Object.keys(PalantirDashboardConfiguration.supportedServiceTypes())
            .sort()
            .reduce((acc, key) => {
                acc[key] = PalantirDashboardConfiguration.supportedServiceTypes()[key];
                return acc;
            }, {});
        chooseRegisteredServiceType(sortedServiceTypes).then(serviceClass => {
            if (serviceClass) {
                openWizardDialog(serviceClass);
            }
        });
    }
}

/**
 * Ask which type of application to create, as a set of described cards. While the cards are shown, the digit on each
 * card chooses it; the keys are read before the page's own shortcuts and only for as long as the cards are shown.
 *
 * @param serviceTypes the supported registered service classes, mapped to their friendly names
 * @returns {Promise<string|null>} the chosen class, or null when the operator cancels
 */
async function chooseRegisteredServiceType(serviceTypes) {
    const descriptions = {
        cas: "Web applications that validate service tickets over the CAS protocol.",
        saml: "Service providers that consume SAML2 assertions, configured from their metadata.",
        oidc: "Applications that sign users in with ID tokens, scopes and claims.",
        oauth: "Clients that obtain access tokens for APIs, without an OpenID Connect sign-in.",
        wsfed: "Relying parties that use passive WS-Federation requests."
    };
    const needs = {
        cas: "the service URL pattern",
        saml: "a metadata location and the entity ID",
        oidc: "a redirect URI, client ID and secret",
        oauth: "a redirect URI, client ID and grant types",
        wsfed: "the realm and reply URL"
    };
    const cards = Object.entries(serviceTypes).map(([serviceClass, friendlyName], index) => {
        const protocol = applicationProtocolOf(serviceClass);
        const shortcut = index < 9 ? String(index + 1) : "";
        return `
            <button type="button" class="palantir-type-card" role="radio" aria-checked="false"
                    data-service-class="${escapePalantirMarkup(serviceClass)}"${shortcut ? ` aria-keyshortcuts="${shortcut}"` : ""}>
                ${shortcut ? `<kbd class="palantir-type-card-key" title="Press ${shortcut} to choose">${shortcut}</kbd>` : ""}
                <span class="palantir-type-card-head">
                    <span class="palantir-protocol-badge is-${protocol.key}" aria-hidden="true">${escapePalantirMarkup(protocol.short)}</span>
                    <span class="palantir-type-card-name">
                        <span>${escapePalantirMarkup(friendlyName || protocol.label)}</span>
                        ${palantirCode(serviceClass.split(".").pop())}
                    </span>
                </span>
                <span class="palantir-type-card-description">${escapePalantirMarkup(descriptions[protocol.key] ?? "Applications of this registered service type.")}</span>
                ${needs[protocol.key] ? `<span class="palantir-type-card-needs">You will need ${needs[protocol.key]}.</span>` : ""}
            </button>`;
    }).join("");
    let chosen = null;
    let shortcuts = () => {};
    const result = await Swal.fire({
        title: "What type of application do you want to add?",
        html: `
            <p class="palantir-type-picker-intro">The type decides which protocol settings the wizard shows. Press the number on a card to choose it.</p>
            <div class="palantir-type-picker" role="radiogroup" aria-label="Application type">${cards}</div>`,
        width: 1100,
        showConfirmButton: false,
        showCancelButton: true,
        customClass: {popup: "palantir-type-picker-popup"},
        didOpen: popup => {
            const choose = card => {
                chosen = card.dataset.serviceClass;
                popup.querySelectorAll(".palantir-type-card").forEach(entry => {
                    entry.setAttribute("aria-checked", String(entry === card));
                });
                Swal.resetValidationMessage();
            };
            const cards = [...popup.querySelectorAll(".palantir-type-card")];
            cards.forEach((card, index) => {
                card.addEventListener("click", () => {
                    choose(card);
                    Swal.clickConfirm();
                });
                card.addEventListener("keydown", event => {
                    const step = {ArrowRight: 1, ArrowDown: 1, ArrowLeft: -1, ArrowUp: -1}[event.key];
                    if (step) {
                        event.preventDefault();
                        const next = cards[(index + step + cards.length) % cards.length];
                        choose(next);
                        next.focus();
                    }
                });
            });
            cards[0]?.focus();
            shortcuts = event => {
                if (event.ctrlKey || event.metaKey || event.altKey || !/^[1-9]$/.test(event.key)) {
                    return;
                }
                const card = cards[Number(event.key) - 1];
                if (card) {
                    event.preventDefault();
                    event.stopImmediatePropagation();
                    choose(card);
                    Swal.clickConfirm();
                }
            };
            window.addEventListener("keydown", shortcuts, true);
        },
        willClose: () => window.removeEventListener("keydown", shortcuts, true),
        preConfirm: () => {
            if (!chosen) {
                Swal.showValidationMessage("Choose the type of application to continue.");
                return false;
            }
            return chosen;
        }
    });
    return result.isConfirmed ? result.value : null;
}

