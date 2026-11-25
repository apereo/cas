/* The fields the wizard is built from: inputs, selects, multi-selects, mapped values, the description editor, and their validity.
   Other pages build their dialogs from them too, so they are fetched on their own and leave the wizard alone until it is loaded. */

/**
 * Show a field's change in the wizard's definition, once the wizard is loaded; the pages that build their dialogs from
 * the wizard's fields do not load it.
 */
function palantirWizardChanged() {
    if (typeof generateServiceDefinition === "function") {
        generateServiceDefinition();
    }
}

/**
 * Whether the wizard is filling its fields from a definition, so their changes are not edits.
 *
 * @returns {boolean} true while the wizard is being populated
 */
function palantirWizardPopulating() {
    return typeof isPopulatingWizard !== "undefined" && isPopulatingWizard;
}

/**
 * The required and otherwise constrained fields of the wizard, as they apply to the application type being edited:
 * Save stays disabled, with the missing fields named beside it, until every one of them holds a valid value, so an
 * incomplete definition never reaches the server.
 */
class PalantirWizardValidity {
    /**
     * The fields that apply: those in the sections this type offers, outside parts hidden because they belong to
     * other types or choices. Fields of the other sections count even though only one section shows at a time.
     *
     * @returns {HTMLElement[]} the fields
     */
    static fields() {
        return RegisteredServiceSections.sections()
            .filter(RegisteredServiceSections.isAvailable)
            .flatMap(section => $(section.panel).find("input, select, textarea").toArray()
                .filter(field => field.willValidate && !PalantirWizardValidity.hiddenWithin(field, section.panel))
                .map(field => ({field, section})));
    }

    static hiddenWithin(field, panel) {
        return [field, ...$(field).parentsUntil(panel).toArray()]
            .some(node => node.classList.contains("hide") || node.classList.contains("d-none") || node.style.display === "none");
    }

    /**
     * The fields that hold no valid value yet, with the section each is in.
     *
     * @returns {Array<{field: HTMLElement, section: object}>} the invalid fields
     */
    static invalid() {
        return PalantirWizardValidity.fields().filter(entry => !entry.field.validity.valid);
    }

    /**
     * How a field is named on screen: its label, or failing that its placeholder or name.
     *
     * @param field the field
     * @returns {string} the name
     */
    static labelOf(field) {
        const label = field.id ? document.querySelector(`label[for="${CSS.escape(field.id)}"]`) : null;
        const text = (label?.querySelector(".mdc-floating-label")?.textContent ?? label?.textContent ?? field.getAttribute("placeholder") ?? field.name ?? "")
            .replace(/\*/g, "").replace(/\s+/g, " ").trim();
        return text || "A field";
    }

    /**
     * Enable Save only when every field that applies is valid, and name the fields still missing.
     */
    static refresh() {
        const dialog = $("#editServiceWizardDialog");
        if (!dialog.hasClass("ui-dialog-content") || !dialog.dialog("isOpen")) {
            return;
        }
        const invalid = PalantirWizardValidity.invalid();
        const names = [...new Set(invalid.map(entry => PalantirWizardValidity.labelOf(entry.field)))];
        const writable = typeof CasActuatorEndpoints.writable !== "function" || CasActuatorEndpoints.writable("registeredServices");
        $("#saveServiceWizard").prop("disabled", invalid.length > 0 || !writable);
        const hint = $("#saveServiceWizardHint");
        if (names.length === 0) {
            hint.prop("hidden", true).empty();
        } else {
            const shown = names.slice(0, 3).join(", ");
            hint.prop("hidden", false)
                .attr("title", names.join(", "))
                .html(`<i class="mdi mdi-alert-circle-outline" aria-hidden="true"></i><span>Required: ${escapePalantirMarkup(shown)}${names.length > 3 ? ` and ${names.length - 3} more` : ""}</span>`);
        }
    }

    /**
     * Open the section of the first invalid field and show what is wrong with it.
     *
     * @returns {boolean} true when every field is valid
     */
    static report() {
        const first = PalantirWizardValidity.invalid()[0];
        if (!first) {
            return true;
        }
        RegisteredServiceSections.activate(first.section.key);
        RegisteredServiceSections.render?.();
        setTimeout(() => {
            first.field.scrollIntoView({block: "center", behavior: "smooth"});
            first.field.reportValidity();
        }, 0);
        return false;
    }
}

function generateMappedFieldValue(sectionId, config) {
    const {
        multipleValues = false,
        valueFieldRenderer,
        multipleValuesType = "java.util.ArrayList",
        unwrapSingleElement = false,
        groovyEditorTitle = "",
        mapType = "java.util.TreeMap"
    } = config;

    const definition = {};

    const rows = $(`#${sectionId}`).find("[data-mapped-input-row='true']");
    rows.each(function () {
        const $row = $(this);
        const $keyInput = $row.find("input[data-mapped-field-role='key']").first();
        const $valueInput = $row.find("input[data-mapped-field-role='value']").first();
        const key = $keyInput.val();
        const value = $valueInput.val();

        // Skip empty keys
        if (!key || key.trim().length === 0) {
            return;
        }

        if (valueFieldRenderer !== undefined && typeof valueFieldRenderer === "function") {
            const additionalInputs = {};
            $row.find("input[data-mapped-field-role]")
                .not("[data-mapped-field-role='key']")
                .not("[data-mapped-field-role='value']")
                .each(function () {
                    const fieldName = $(this).data("mapped-field-role");
                    additionalInputs[fieldName] = $(this);
                });
            definition[key] = valueFieldRenderer($keyInput, $valueInput, additionalInputs, $row);
        } else if (value && value.trim().length > 0) {
            if (groovyEditorTitle && /^groovy\s*\{[\s\S]*\}\s*$/.test(value.trim())) {
                definition[key] = value.trim();
            } else if (multipleValues) {
                const valueArray = value.split(",").filter(entry => entry.trim().length > 0);
                if (unwrapSingleElement && valueArray.length === 1) {
                    definition[key] = value.trim();
                } else {
                    definition[key] = [multipleValuesType, valueArray];
                }
            } else {
                const given = $valueInput.attr("data-mapped-value-key") === key ? $valueInput.attr("data-mapped-value-type") : undefined;
                definition[key] = mappedFieldValue(value, given);
            }
        }
    });
    return {"@class": mapType, ...definition};
}

/**
 * The value of a mapped field as the definition held it: a number, a boolean, a list or an object the wizard was given
 * goes back as one, so long as the text still reads as one and the row still has the key it was given with; a row
 * cleared and filled in again starts as text.
 *
 * @param text the text of the field
 * @param type the type of the value the field was given: number, boolean or json, when it was not text
 * @returns {string|number|boolean|object|Array} the value
 */
function mappedFieldValue(text, type) {
    const trimmed = text.trim();
    if (type === "json") {
        try {
            const parsed = JSON.parse(trimmed);
            if (parsed !== null && typeof parsed === "object") {
                return parsed;
            }
        } catch (error) {
            return text;
        }
    }
    if (type === "boolean" && /^(true|false)$/i.test(trimmed)) {
        return trimmed.toLowerCase() === "true";
    }
    if (type === "number" && trimmed.length > 0 && Number.isFinite(Number(trimmed))) {
        return Number(trimmed);
    }
    return text;
}

/**
 * Attach the Material text fields under an element, such as a row just added to a mapped field. Attaching every text
 * field of the page instead costs more as the wizard grows and adds another set of listeners to each field every time.
 *
 * @param scope the element to look under
 */
function attachPalantirTextFields(scope) {
    $(scope).find(".mdc-text-field").addBack(".mdc-text-field").each((index, field) => {
        mdc.textField.MDCTextField.attachTo(field);
    });
}

function createMappedInputField(config) {
    const {
        header = "",
        cssClasses = "",
        keyField,
        keyLabel = "",
        valueField,
        valueFieldType = "text",
        valueLabel = "",
        containerId,
        containerField,
        containerType,
        required = false,
        multipleValues = false,
        multipleValuesType = "java.util.ArrayList",
        valueFieldRenderer,
        unwrapSingleElement = false,
        onChangeCallback,
        groovyEditorTitle = "",
        additionalFields = []
    } = config;

    const changeCallback = onChangeCallback || palantirWizardChanged;

    const sectionContainerId = `registeredService${capitalize(keyField)}MapContainer`;
    const addButtonId = `registeredService${capitalize(keyField)}AddButton`;
    const deleteAllButtonId = `registeredService${capitalize(keyField)}DeleteAllButton`;
    const removeButtonId = `registeredService${capitalize(keyField)}RemoveButton`;
    const mapRowId = `registeredService${capitalize(keyField)}Row`;

    const inputFieldKeyId = `registeredService${capitalize(keyField)}`;
    const inputFieldValueId = `registeredService${capitalize(valueField)}`;
    const additionalFieldElements = additionalFields.map(field => {
        const inputFieldId = `registeredService${capitalize(field.field)}`;
        return `
                <label for="${inputFieldId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses} ${field.cssClasses ?? ""}">
                    <span class="mdc-notched-outline">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${field.label ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input form-control"
                           id="${inputFieldId}"
                           name="${inputFieldId}"
                           size="25"
                           type="${field.type ?? "text"}"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="${field.name}"
                           ${required ? "required" : ""}/>
                </label>
        `;
    }).join("");
    const rowElements = `
            <div class="d-flex justify-content-between pt-2 ${keyField}-map-row ${cssClasses}" id="${mapRowId}" data-mapped-input-row="true">
                <label for="${inputFieldKeyId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses}">
                    <span class="mdc-notched-outline pr-2">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${keyLabel ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input"
                           id="${inputFieldKeyId}"
                           name="${inputFieldKeyId}"
                           type="text"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="key"
                           ${required ? "required" : ""}/>
                </label>

                <label for="${inputFieldValueId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses}">
                    <span class="mdc-notched-outline">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${valueLabel ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input form-control"
                           id="${inputFieldValueId}"
                           name="${inputFieldValueId}"
                           size="25"
                           type="${valueFieldType}"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="value"
                           ${!multipleValues && typeof valueFieldRenderer !== "function" ? "data-mapped-plain=\"true\"" : ""}
                           ${required ? "required" : ""}/>
                </label>

                ${additionalFieldElements}

                <button type="button"
                        id="${removeButtonId}"
                        name="${removeButtonId}"
                        title="Remove Row"
                        class="mdc-button mdc-button--raised btn btn-link mdc-button--inline-row ${cssClasses}">
                    <i class="mdi mdi-minus-thick" aria-hidden="true"></i>
                </button>
            </div>
    `;

    const html =
        `<div id="${sectionContainerId}" class="${cssClasses}">
            <h3 class="mt-2 mb-2 ${header && header.length > 0 ? "" : "hide"} ${cssClasses}">${header}</h3>
            ${rowElements}
            <span id="${sectionContainerId}ToAppend" class="pt-2 ${cssClasses}"></span>
            <div class="d-flex pt-2 ${cssClasses}">
                <button type="button" 
                        name="${addButtonId}"
                        id="${addButtonId}"
                        title="Add Row"
                        class="mdc-button mdc-button--raised mdc-button--round add-row ${cssClasses}">
                    <span class="mdc-button__label">
                        <i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>
                    </span>
                </button>
                <button type="button" 
                        name="${deleteAllButtonId}"
                        id="${deleteAllButtonId}"
                        title="Delete All Rows"
                        class="mdc-button mdc-button--raised mdc-button--round add-row ${cssClasses}">
                    <span class="mdc-button__label">
                        <i class="mdc-tab__icon mdi mdi-trash-can" aria-hidden="true"></i>
                    </span>
                </button>
            </div>
        </div>
        `;

    const section = $(`${html}`);
    decoratePalantirInputIcons(section[0]);
    $(`#${containerId}`).append(section);

    function configureRemoveMapRowEventHandler() {
        $(`button[name=${removeButtonId}]`).off().on("click", function () {
            $(this).closest(`.${keyField}-map-row`).remove();
            changeCallback();
        });
    }

    function configureInputEventHandler() {
        $(`#${sectionContainerId} input`).off().on("input", () => {
            changeCallback();
        });
    }

    function configureInputRenderer() {
        $(`#${sectionContainerId} input`).data("renderer", function () {
            return generateMappedFieldValue(sectionContainerId, config);
        });
    }

    function configureGroovyEditors(scope) {
        const editorFields = [];
        if (groovyEditorTitle) {
            editorFields.push({role: "value", title: groovyEditorTitle});
        }
        additionalFields.filter(field => field.groovyEditorTitle).forEach(field => {
            editorFields.push({role: field.name, title: field.groovyEditorTitle});
        });
        editorFields.forEach(editorField => {
            $(scope).find(`input[data-mapped-field-role='${editorField.role}']`).each(function () {
                attachPalantirGroovyEditorButton({
                    target: this,
                    title: editorField.title
                });
            });
        });
    }

    $(`button[name=${addButtonId}]`).off().on("click", () => {
        let elementsToAdd = $(rowElements).removeClass("hide");
        elementsToAdd.find("*").removeClass("hide");

        decoratePalantirInputIcons(elementsToAdd[0]);
        $(`#${sectionContainerId}ToAppend`).append(elementsToAdd);
        configureGroovyEditors(elementsToAdd);
        configureRemoveMapRowEventHandler();
        attachPalantirTextFields(elementsToAdd);
        configureInputEventHandler();
        configureInputRenderer();
    });
    $(`button[name=${deleteAllButtonId}]`).off().on("click", () => {
        $(`#${sectionContainerId}ToAppend`).empty();
        $(`#${sectionContainerId} input`).val("").removeAttr("data-mapped-value-type data-mapped-value-key");
        configureRemoveMapRowEventHandler();
        configureInputEventHandler();
        configureInputRenderer();
        changeCallback();
    });

    configureRemoveMapRowEventHandler();
    configureInputEventHandler();
    configureInputRenderer();
    configureGroovyEditors($(`#${sectionContainerId}`));

    return $(`#${sectionContainerId}`);
}

function appendOptionsToDropDown(config) {
    const {
        selectElement,
        options
    } = config;

    if (options) {
        options.forEach(opt => {
            const $opt = $("<option>")
                .attr("value", opt.value)
                .text(opt.text);
            if (opt.data && Object.keys(opt.data).length > 0) {
                Object.entries(opt.data).forEach(([key, value]) => {
                    $opt.data(key, value);
                });
            }
            if (opt.selected) {
                $opt.attr("selected", "selected");
            }

            selectElement.append($opt);
        });
    }
}

function createSelectField(config) {
    const {
        containerId,
        labelTitle,
        paramName,
        paramType = "",
        options,
        serviceClass = "",
        cssClasses = "",
        labelCssClasses = "",
        changeEventHandlers = "",
        id = ""
    } = config;

    let selectId = "";
    if (id !== undefined && id.trim().length > 0) {
        selectId = id.replace(".", "").trim();
    } else {
        selectId = `registeredService${capitalize(paramName.replace(".", ""))}`;
    }

    const $label = $("<label>")
        .addClass(serviceClass ?? "")
        .addClass("pt-2")
        .addClass("mb-2")
        .addClass(cssClasses ?? "")
        .addClass(labelCssClasses ?? "")
        .css("display", "block")
        .attr("for", selectId).text(`${labelTitle} `);

    const $select = $("<select>")
        .attr("id", selectId)
        .attr("data-change-handler", `${`${changeEventHandlers},`}generateServiceDefinition`)
        .attr("data-param-name", paramName)
        .attr("data-param-type", paramType)
        .addClass("jqueryui-selectmenu");

    appendOptionsToDropDown({
        selectElement: $select,
        options: options
    });

    $label.append($select);

    const container = $("<div>", {
        id: `${selectId}SelectContainer`,
        class: `${serviceClass ?? ""} ${cssClasses ?? ""}`
    });
    $(container).append($label);

    if (typeof containerId === "string") {
        $(`#${containerId}`).append(container);
    } else {
        $(containerId).append(container);
    }
    return $select;
}

function createMultiSelectField(config) {
    const {
        containerId,
        paramName,
        paramType = "",
        options,
        serviceClass = "",
        cssClasses = "",
        changeEventHandlers = "",
        labelTitle,
        allowCreateOption = false,
        singleSelect = false,
        inclusion = "append",
        id
    } = config;

    let selectId = "";
    if (id !== undefined && id.trim().length > 0) {
        selectId = id.replace(".", "").trim();
    } else {
        selectId = `registeredService${capitalize(paramName.replace(".", ""))}`;
    }

    const $select = $(`<select ${singleSelect ? "" : "multiple"}>`)
        .attr("id", selectId)
        .attr("data-change-handler", `${`${changeEventHandlers},`}generateServiceDefinition`)
        .attr("data-param-name", paramName)
        .attr("data-param-type", paramType)
        .addClass("jqueryui-multiselectmenu")
        .addClass(serviceClass ?? "")
        .addClass(cssClasses ?? "");

    appendOptionsToDropDown({
        selectElement: $select,
        options: options
    });
    const container = $("<span>", {
        id: `${selectId}SelectContainer`,
        class: `d-flex mb-2 mt-2 ${serviceClass ?? ""} ${cssClasses ?? ""}`
    });

    const $label = $("<label>")
        .addClass(serviceClass ?? "")
        .addClass("pt-2")
        .addClass("pr-2")
        .addClass("mb-2")
        .addClass(cssClasses ?? "")
        .css("min-width", "max-content")
        .attr("for", selectId).text(`${labelTitle} `);

    container.append($label, $select);

    if (inclusion === undefined || inclusion === "" || inclusion === "append") {
        $(`#${containerId}`).append(container);
    } else if (inclusion === "prepend") {
        $(`#${containerId}`).prepend(container);
    } else if (inclusion === "before") {
        $(`#${containerId}`).before(container);
    } else if (inclusion === "after") {
        $(`#${containerId}`).after(container);
    }

    let settings = {
        dropdownParent: $select.closest("#editServiceWizardDialog").length > 0 ? null : "body",
        plugins: ["remove_button"],
        onItemAdd(value, item) {
            palantirWizardChanged();
        },
        onItemRemove(value) {
            palantirWizardChanged();
        }
    };
    if (allowCreateOption) {
        settings.create = true;
        settings.persist = false;
    }
    new TomSelect(`#${selectId}`, settings);

    return $select;
}

function initializeDescriptionEditor(containerId) {
    setTimeout(function () {
        const editorContainer = $("<div>", {
            id: "registeredServiceDescriptionEditorContainer",
            class: "mb-2"
        });
        
        const editorTextarea = $("<textarea>", {
            id: "registeredServiceDescriptionEditor",
            name: "registeredServiceDescriptionEditor",
            "data-param-name": "description",
            title: "Provide a brief description of the application."
        });

        editorContainer.append(editorTextarea);
        $(`#${containerId}`).append(editorContainer);

        if (PalantirDashboardConfiguration.trumbowygIconsPath()) {
            $.trumbowyg.svgPath = PalantirDashboardConfiguration.trumbowygIconsPath();
        }

        $("#registeredServiceDescriptionEditor").trumbowyg({
            btns: [["bold", "italic", "underline", "removeformat", "viewHTML"]],
            removeformatPasted: true,
            semantic: false,
            autogrow: true,
            defaultLinkTarget: "_blank",
            autogrowOnEnter: true
        }).on("tbwchange tbwblur", function () {
            palantirWizardChanged();
        });
        $("#registeredServiceDescriptionEditor").attr("aria-label", "Description").data("renderer", unwrapDescriptionParagraph);
        $("#registeredServiceDescriptionEditorContainer .trumbowyg-editor")
            .attr({"role": "textbox", "aria-multiline": "true", "aria-label": "Description"});
    }, 150);
}

/**
 * The editor keeps every line in a paragraph; a description that is a single paragraph is kept without that wrapper,
 * so a plain description stays plain text, while several paragraphs keep their markup.
 *
 * @param value the HTML of the editor
 * @returns {string} the description
 */
function unwrapDescriptionParagraph(value) {
    const body = new DOMParser().parseFromString(String(value ?? ""), "text/html").body;
    const nodes = [...body.childNodes].filter(node => node.nodeType !== Node.TEXT_NODE || node.textContent.trim().length > 0);
    if (nodes.length === 1 && nodes[0].nodeName === "P" && nodes[0].attributes.length === 0) {
        const inner = nodes[0].innerHTML.trim();
        return inner.length > 0 && inner !== "<br>" ? inner : String(value);
    }
    return String(value);
}

let descriptionEditorValue = "";

let descriptionEditorTimer = null;

/**
 * Put a description in the description editor. Until the editor is ready, only the latest description is kept and
 * applied once it is, and the definition is generated again then, since it was generated without it.
 *
 * @param value the description
 */
function setDescriptionEditorValue(value) {
    descriptionEditorValue = value || "";
    const $editor = $("#registeredServiceDescriptionEditor");
    if ($editor.length > 0 && $editor.data("trumbowyg")) {
        clearTimeout(descriptionEditorTimer);
        descriptionEditorTimer = null;
        $editor.trumbowyg("html", descriptionEditorValue);
        return;
    }
    if (descriptionEditorTimer === null) {
        descriptionEditorTimer = setTimeout(() => {
            descriptionEditorTimer = null;
            setDescriptionEditorValue(descriptionEditorValue);
            if (!palantirWizardPopulating()) {
                palantirWizardChanged();
            }
        }, 150);
    }
}

function createInputField(config) {
    const {
        labelTitle,
        name,
        paramName,
        paramType = "",
        required,
        dataType = "text",
        containerId,
        inclusion = "append",
        title,
        serviceClass = "",
        cssClasses = "",
        data = {}
    } = config;

    const label = $("<label>", {
        for: name,
        id: `${name}Label`,
        class: `${serviceClass ?? ""} mdc-text-field mdc-text-field--outlined control-label mdc-text-field--with-trailing-icon mb-2 ${cssClasses ?? ""}`
    });

    const outline = $("<span>", {class: "mdc-notched-outline"});
    outline.append($("<span>", {class: "mdc-notched-outline__leading"}));

    const notch = $("<span>", {class: "mdc-notched-outline__notch"});
    notch.append($("<span>", {id: `${name}LabelText`, class: "mdc-floating-label", html: labelTitle}));

    outline.append(notch);
    outline.append($("<span>", {class: "mdc-notched-outline__trailing"}));

    const input = $("<input>", {
        class: `${serviceClass ?? ""} mdc-text-field__input form-control ${cssClasses ?? ""}`,
        type: dataType === "regex" || dataType === "text" ? "text" : dataType,
        id: name,
        name: name,
        "data-param-name": paramName,
        "data-param-type": paramType,
        tabindex: 0,
        size: 50,
        title: title,
        required: required
    }).data("tooltip-html", title).on("input", function () {
        const value = $(this).val();
        if (dataType === "date") {
            if (value.length === 0) {
                $(`${name}LabelText`).text(title);
            } else {
                $(`${name}LabelText`).text("");
            }
            palantirWizardChanged();
        } else if (value.length > 0 && dataType === "regex" && !isValidRegex(value)) {
            this.setCustomValidity("Value must be a valid regular expression.");
        } else if (required && value.length <= 0) {
            this.setCustomValidity("Field value is required");
        } else {
            this.setCustomValidity("");
            palantirWizardChanged();
        }
    })
        .on("blur", function () {
            // Skip validation styling during wizard population
            if (palantirWizardPopulating()) {
                return;
            }
            if (!this.checkValidity()) {
                $(this).parent().addClass("missing-required-field");
            } else {
                $(this).parent().removeClass("missing-required-field");
            }
        });

    Object.entries(data).forEach(([key, value]) => {
        input.data(key, value);
    });
    label.append(outline, input);
    decoratePalantirInputIcons(label[0]);

    const container = $("<span>", {
        id: `${name}FieldContainer`,
        class: `${serviceClass ?? ""} ${cssClasses ?? ""}`
    });

    $(container).append(label);
    if (inclusion === undefined || inclusion === "" || inclusion === "append") {
        if (typeof containerId === "string") {
            $(`#${containerId}`).append(container);
        } else {
            $(containerId).append(container);
        }
    } else if (inclusion === "prepend") {
        if (typeof containerId === "string") {
            $(`#${containerId}`).prepend(container);
        } else {
            $(containerId).prepend(container);
        }
    }
    return input;
}
