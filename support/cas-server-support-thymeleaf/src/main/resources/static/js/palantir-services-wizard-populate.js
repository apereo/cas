/* Filling the wizard from an application definition, contacts included. */

// Flag to prevent generateServiceDefinition from running during population
let isPopulatingWizard = false;

/**
 * Populates wizard fields from an existing service definition.
 * Handles all field types: input fields, select fields, switch buttons,
 * multi-select fields, and mapped fields.
 * @param {Object} serviceDefinition - The service definition JSON object
 */
function populateWizardFromServiceDefinition(definition, options = {}) {
    const serviceDefinition = PalantirWizardChains.unpack(definition);
    const $wizardDialog = $("#editServiceWizardDialog").closest(".ui-dialog");
    $wizardDialog.addClass("is-populating").attr("aria-busy", "true");

    // Set flag to prevent generateServiceDefinition from running during population
    isPopulatingWizard = true;

    /**
     * Extracts a nested value from an object using dot notation path
     * @param {Object} obj - The object to extract from
     * @param {string} path - Dot notation path (e.g., "accessStrategy.enabled")
     * @returns {*} The value at the path, or undefined if not found
     */
    function getNestedValue(obj, path) {
        if (!obj || !path) {
            return undefined;
        }
        const parts = path.split(".");
        let current = obj;
        for (const part of parts) {
            if (current === null || current === undefined) {
                return undefined;
            }
            current = current[part];
        }
        return current;
    }

    /**
     * Converts array values to comma-separated strings
     * Handles Java collection wrappers like ["java.util.HashSet", [...]]
     * @param {*} value - The value to convert
     * @returns {string} Comma-separated string or original value as string
     */
    function arrayToString(value) {
        if (Array.isArray(value)) {
            if (value.length === 2 && typeof value[0] === "string" && value[0].startsWith("java.util.") && Array.isArray(value[1])) {
                return value[1].join(",");
            }
            return value.join(",");
        }
        return value !== null && value !== undefined ? String(value) : "";
    }

    /**
     * Properly floats the MDC text field label when a value is set programmatically
     * Also removes the missing-required-field class and clears validation errors
     * @param {jQuery} $input - The input element
     */
    function floatMdcTextFieldLabel($input) {
        const textFieldEl = $input.closest(".mdc-text-field")[0];
        if (textFieldEl) {
            // Add the floating label class
            textFieldEl.classList.add("mdc-text-field--label-floating");

            // Handle the notched outline
            const notchedOutline = textFieldEl.querySelector(".mdc-notched-outline");
            if (notchedOutline) {
                notchedOutline.classList.add("mdc-notched-outline--notched");
            }

            // Re-attach the MDC text field to update its state
            try {
                const mdcTextField = mdc.textField.MDCTextField.attachTo(textFieldEl);
                // Force layout update
                mdcTextField.layout();
            } catch (e) {
                // Component may already be attached, which is fine
            }
        }

        // Remove the missing-required-field class from parent if value is set
        if ($input.val() && $input.val().length > 0) {
            $input.parent().removeClass("missing-required-field");
            // Clear any custom validity
            if ($input[0] && $input[0].setCustomValidity) {
                $input[0].setCustomValidity("");
            }
        }
    }

    /**
     * Updates a select field's value and triggers change handlers
     * @param {jQuery} $select - The select element
     * @param {string} value - The value to set
     */
    function setSelectValue($select, value) {
        if (!$select.length || value === undefined || value === null) {
            return;
        }

        // Convert booleans to strings
        let stringValue = value;
        if (typeof value === "boolean") {
            stringValue = value.toString();
        }

        // Try to find the option with exact match, then uppercase, then lowercase
        let $option = $select.find(`option[value="${stringValue}"]`);
        if ($option.length === 0) {
            $option = $select.find(`option[value="${stringValue.toString().toUpperCase()}"]`);
        }
        if ($option.length === 0) {
            $option = $select.find(`option[value="${stringValue.toString().toLowerCase()}"]`);
        }
        if ($option.length === 0) {
            $option = $select.find("option").filter((index, option) => option.text === String(stringValue));
        }

        if ($option.length > 0) {
            $select.val($option.val());
            try {
                $select.selectmenu("refresh");
            } catch (e) {
                // Selectmenu not initialized yet
            }

            const handlerNames = $select.data("change-handler");
            if (handlerNames) {
                const handlers = handlerNames.split(",");
                for (const handlerName of handlers) {
                    if (handlerName && handlerName.length > 0 && handlerName !== "generateServiceDefinition" && typeof window[handlerName] === "function") {
                        window[handlerName]($select);
                    }
                }
            }
        }
    }

    /**
     * Updates a TomSelect multi-select field
     * @param {jQuery} $select - The select element
     * @param {Array|string} values - The values to set
     */
    function setMultiSelectValue($select, values) {
        if (!$select.length) {
            return;
        }

        const tomSelect = $select[0].tomselect;
        if (tomSelect) {
            tomSelect.clear();
            const valueArray = Array.isArray(values) ? values : (values ? values.split(",") : []);
            for (const val of valueArray) {
                if (val && val.trim().length > 0) {
                    if (!tomSelect.options[val.trim()]) {
                        tomSelect.addOption({value: val.trim(), text: val.trim()});
                    }
                    tomSelect.addItem(val.trim());
                }
            }
        }
    }

    /**
     * Updates a switch button (checkbox) field
     * @param {string} buttonId - The button ID (without Button suffix)
     * @param {boolean} value - The value to set
     */
    function setSwitchButtonValue(buttonId, value) {
        const $input = $(`input#${buttonId}`);
        const $button = $(`button#${buttonId}Button`);

        if (!$input.length) {
            return;
        }

        const newValue = value === true || value === "true" || value === "TRUE";
        if (wizardSwitchSelected($button) !== newValue) {
            $button.click();
        }
        $input.val(String(newValue));
    }

    /**
     * Populates a mapped input field section (key-value pairs)
     * @param {string} containerFieldName - The field name for the container
     * @param {Object} mapData - The map data object
     * @param {string} policyClassName - Optional: the policy class name to find the correct container
     */
    function populateMappedField(containerFieldName, mapData, policyClassName = null) {
        if (!mapData || typeof mapData !== "object") {
            return;
        }

        const entries = Object.entries(mapData).filter(([key]) => key !== "@class");
        if (entries.length === 0) {
            return;
        }

        // Find inputs matching the param name
        let $inputs = $(`form#editServiceWizardForm input[data-param-name="${containerFieldName}"]`);
        if ($inputs.length === 0) {
            return;
        }

        // If a policy class name is provided, find the container that matches
        let $container = null;
        if (policyClassName) {
            const policyShortName = getLastWord(policyClassName);
            // Find a container that has the policy class in its CSS classes
            $inputs.each(function () {
                const $potentialContainer = $(this).closest("[id$='MapContainer']");
                if ($potentialContainer.hasClass(policyShortName) ||
                    $potentialContainer.find(`.${policyShortName}`).length > 0 ||
                    $potentialContainer.closest(`.${policyShortName}`).length > 0) {
                    $container = $potentialContainer;
                    return false; // break
                }
            });
        }

        // Fallback: find a policy-enabled container or the first container
        if (!$container || !$container.length) {
            const $firstKeyInput = $inputs.first();
            $container = $firstKeyInput.closest("[id$='MapContainer']");

            // Inactive sidebar sections may hide an otherwise enabled container
            $inputs.each(function () {
                const $potentialContainer = $(this).closest("[id$='MapContainer']");
                if (!isWizardContainerSwitchedOff($potentialContainer[0])) {
                    $container = $potentialContainer;
                    return false; // break
                }
            });
        }

        if (!$container || !$container.length) {
            return;
        }

        const $addButton = $container.find("button[name$='AddButton']");

        function mapFieldValueToString(value, role) {
            if (typeof value === "object" && value !== null && !Array.isArray(value)) {
                if (role && role !== "value") {
                    return value[role] !== undefined ? arrayToString(value[role]) : "";
                }
                if (value["@class"]) {
                    if (value.pattern) {
                        return value.pattern;
                    }
                    if (value.values) {
                        return arrayToString(value.values);
                    }
                    return JSON.stringify(value);
                }
                return arrayToString(value);
            }
            return arrayToString(value);
        }

        entries.forEach(([key, value], index) => {
            if (index > 0 && $addButton.length) {
                $addButton.click();
            }

            const $row = $container.find("[data-mapped-input-row='true']").eq(index);
            if ($row.length > 0) {

                const $keyInput = $row.find(`input[data-param-name="${containerFieldName}"][data-mapped-field-role='key']`).first();
                if ($keyInput.length > 0) {
                    $keyInput.val(key).trigger("input");
                    floatMdcTextFieldLabel($keyInput);
                }

                $row.find(`input[data-param-name="${containerFieldName}"][data-mapped-field-role]`)
                    .not("[data-mapped-field-role='key']")
                    .each(function () {
                        const $input = $(this);
                        const role = $input.data("mapped-field-role");
                        const structured = role === "value" && $input.attr("data-mapped-plain") === "true"
                            && value !== null && typeof value === "object";
                        const valueStr = structured ? JSON.stringify(value) : mapFieldValueToString(value, role);
                        if (structured) {
                            $input.attr({"data-mapped-value-type": "json", "data-mapped-value-key": key});
                        } else if (role === "value" && (typeof value === "number" || typeof value === "boolean")) {
                            $input.attr({"data-mapped-value-type": typeof value, "data-mapped-value-key": key});
                        } else {
                            $input.removeAttr("data-mapped-value-type data-mapped-value-key");
                        }
                        $input.val(valueStr).trigger("input");
                        floatMdcTextFieldLabel($input);
                    });
            }
        });
    }

    // Populate all input fields
    $("form#editServiceWizardForm input[data-param-name], form#editServiceWizardForm textarea[data-param-name]").each(function () {
        const $input = $(this);
        const paramName = $input.data("param-name");

        if (!paramName) {
            return;
        }
        if (paramName === "clientSecrets" && $input.attr("id") === "registeredServiceClientSecrets") {
            return;
        }
        if (paramName === "contacts") {
            return;
        }

        // Skip if this is part of a mapped field (handled separately)
        if ($input.closest("[id$='MapContainer']").length > 0) {
            return;
        }

        const reader = $input.data("reader");
        const setting = getNestedValue(serviceDefinition, paramName);
        const value = typeof reader === "function" ? reader(setting) : setting;

        if (value !== undefined && value !== null) {
            const stringValue = arrayToString(value);
            $input.val(stringValue);

            // Special handling for description field with WYSIWYG editor
            if (paramName === "description") {
                setDescriptionEditorValue(stringValue);
            }

            // Float the label if we have a value
            if (stringValue.length > 0) {
                floatMdcTextFieldLabel($input);
            }
        }
    });

    populateRegisteredServiceContacts(serviceDefinition, floatMdcTextFieldLabel);

    // Populate select fields
    $("form#editServiceWizardForm select[data-param-name]").each(function () {
        const $select = $(this);
        const paramName = $select.data("param-name");

        if (!paramName) {
            return;
        }

        let value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            // Handle objects with @class property
            if (typeof value === "object" && value["@class"]) {
                value = value["@class"];
            }

            setSelectValue($select, value);
        }
    });

    // Populate multi-select fields (TomSelect)
    $("form#editServiceWizardForm select.jqueryui-multiselectmenu[data-param-name]").each(function () {
        const $select = $(this);
        const paramName = $select.data("param-name");

        if (!paramName) {
            return;
        }

        let value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            const arrayValue = arrayToString(value);
            setMultiSelectValue($select, arrayValue);
        }
    });

    // Populate switch buttons (hidden inputs with corresponding buttons)
    $("form#editServiceWizardForm input[type='hidden'][data-param-name]").each(function () {
        const $input = $(this);
        const paramName = $input.data("param-name");
        const inputId = $input.attr("id");

        if (!paramName || !inputId) {
            return;
        }

        const value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            setSwitchButtonValue(inputId, value);
        }
    });

    setTimeout(() => {
        const mappedFieldContainers = new Set();

        // First, handle attributeReleasePolicy.allowedAttributes specially
        // because it needs to find the right container based on the policy class
        if (serviceDefinition.attributeReleasePolicy && serviceDefinition.attributeReleasePolicy.allowedAttributes) {
            const policyClass = serviceDefinition.attributeReleasePolicy["@class"];
            const policyShortName = getLastWord(policyClass);

            // Find the container with the matching policy class
            $("form#editServiceWizardForm [id$='MapContainer']").each(function () {
                const $container = $(this);
                const $inputs = $container.find("input[data-param-name='attributeReleasePolicy.allowedAttributes']");

                if ($inputs.length > 0 && $container.hasClass(policyShortName)) {
                    const containerKey = `attributeReleasePolicy.allowedAttributes:${$container.attr("id")}`;
                    if (!mappedFieldContainers.has(containerKey)) {
                        mappedFieldContainers.add(containerKey);
                        populateMappedField("attributeReleasePolicy.allowedAttributes",
                            serviceDefinition.attributeReleasePolicy.allowedAttributes, policyClass);
                        return false; // break
                    }
                }
            });
        }

        // Then handle other mapped fields
        $("form#editServiceWizardForm [id$='MapContainer']").each(function () {
            const $container = $(this);

            // Skip disabled policy containers, including when their sidebar section is inactive
            if (isWizardContainerSwitchedOff($container[0])) {
                return;
            }

            const $inputs = $container.find("input[data-param-name]");

            if ($inputs.length > 0) {
                const paramName = $inputs.first().data("param-name");
                // Create a unique key based on paramName and container ID to avoid conflicts
                const containerKey = `${paramName}:${$container.attr("id")}`;
                if (paramName && !mappedFieldContainers.has(containerKey)) {
                    mappedFieldContainers.add(containerKey);
                    const mapData = getNestedValue(serviceDefinition, paramName);
                    if (mapData) {
                        // Pass the attribute release policy class if applicable
                        const policy = paramName.includes(".") ? serviceDefinition[paramName.split(".")[0]] : null;
                        const policyClass = policy !== null && typeof policy === "object" && !Array.isArray(policy)
                            ? policy["@class"] ?? null
                            : null;
                        populateMappedField(paramName, mapData, policyClass);
                    }
                }
            }
        });
    }, 0);

    // Handle special cases for different service types
    const serviceClass = serviceDefinition["@class"];
    if (serviceClass) {
        // OAuth/OIDC specific fields
        if (serviceClass.includes("OAuthRegisteredService") || serviceClass.includes("OidcRegisteredService")) {
            if (typeof populateRegisteredServiceClientSecrets === "function") {
                populateRegisteredServiceClientSecrets(serviceDefinition);
            }
            if (serviceDefinition.supportedGrantTypes) {
                const $grantTypes = $("select#registeredServiceSupportedGrantTypes");
                setMultiSelectValue($grantTypes, arrayToString(serviceDefinition.supportedGrantTypes));
            }
            if (serviceDefinition.supportedResponseTypes) {
                const $responseTypes = $("select#registeredServiceSupportedResponseTypes");
                setMultiSelectValue($responseTypes, arrayToString(serviceDefinition.supportedResponseTypes));
            }
            if (serviceDefinition.scopes) {
                const $scopes = $("select#registeredServiceScopes");
                setMultiSelectValue($scopes, arrayToString(serviceDefinition.scopes));
            }
        }

        // SAML specific fields
        if (serviceClass.includes("SamlRegisteredService")) {
            if (serviceDefinition.attributeNameFormats) {
                populateMappedField("attributeNameFormats", serviceDefinition.attributeNameFormats);
            }
            if (serviceDefinition.attributeFriendlyNames) {
                populateMappedField("attributeFriendlyNames", serviceDefinition.attributeFriendlyNames);
            }
            if (serviceDefinition.attributeValueTypes) {
                populateMappedField("attributeValueTypes", serviceDefinition.attributeValueTypes);
            }
        }
    }

    setTimeout(() => {
        isPopulatingWizard = false;

        // Clean up any missing-required-field classes on fields that have values
        $("form#editServiceWizardForm input[data-param-name]").each(function () {
            const $input = $(this);
            const value = $input.val();
            if (value && value.length > 0) {
                $input.parent().removeClass("missing-required-field");
                if (this.setCustomValidity) {
                    this.setCustomValidity("");
                }
            }
        });

        generateServiceDefinition();
        $wizardDialog.removeClass("is-populating").removeAttr("aria-busy");
        PalantirWizardChains.render(true);
        if (typeof options.done === "function") {
            options.done();
        }
        if (options.focus === false) {
            return;
        }

        // Focus on the first visible input field after dialog is fully ready
        // Use a longer timeout to ensure MDC dialog animation is complete
        setTimeout(() => {
            const $firstInput = $("form#editServiceWizardForm input[data-param-name]:visible").first();
            if ($firstInput.length > 0) {
                $firstInput.trigger("focus");
            }
        }, 300);
    }, 0);
}

/**
 * The contacts the wizard's three contact tabs hold while a definition is generated, by tab, so the details of one
 * tab make one contact whichever tabs before it are empty.
 */
const wizardContactSlots = new WeakMap();

/**
 * The contacts after the third of the definition the wizard was filled from. The wizard has fields for three contacts;
 * the others are kept as they are and added after the wizard's when it generates the definition.
 */
let wizardExtraContacts = [];

/**
 * Fill the contact fields of the wizard from the contacts of a definition, which are a Java list of contact objects:
 * the first three, one per contact tab. The others are kept to be saved as they are. Every contact field is emptied
 * first, so filling a wizard that is already open, as from metadata, leaves nothing of the contacts it held.
 *
 * @param serviceDefinition the definition
 * @param floatLabel floats the label of a filled text field
 */
function populateRegisteredServiceContacts(serviceDefinition, floatLabel) {
    const contacts = serviceDefinition?.contacts;
    let list = [];
    if (Array.isArray(contacts)) {
        list = contacts.length === 2 && typeof contacts[0] === "string" && Array.isArray(contacts[1]) ? contacts[1] : contacts;
    }
    wizardExtraContacts = list.slice(3).filter(contact => contact !== null && typeof contact === "object");
    [1, 2, 3].forEach(position => ["name", "email", "phone", "department", "type"].forEach(detail => {
        const $input = $(`#registeredServiceContact${capitalize(detail)}${position}`);
        if ($input.length > 0 && $input.val() !== "") {
            $input.val("");
            const field = $input.closest(".mdc-text-field")[0];
            field?.classList.remove("mdc-text-field--label-floating");
            field?.querySelector(".mdc-notched-outline")?.classList.remove("mdc-notched-outline--notched");
        }
    }));
    list.slice(0, 3).forEach((contact, position) => {
        ["name", "email", "phone", "department", "type"].forEach(detail => {
            const $input = $(`#registeredServiceContact${capitalize(detail)}${position + 1}`);
            const value = contact?.[detail];
            if ($input.length > 0 && value !== undefined && value !== null && value !== "") {
                $input.val(String(value));
                floatLabel($input);
            }
        });
    });
}
