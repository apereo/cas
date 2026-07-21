const HEIMDALL_RESOURCES_CLASS = "org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources";
const HEIMDALL_RESOURCE_CLASS = "org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource";
const HEIMDALL_POLICY_PACKAGE = "org.apereo.cas.heimdall.authorizer.resource.policy";
const HEIMDALL_EDITOR_PREFERENCE = "showHeimdallResourceJsonEditorPreference";

const HEIMDALL_POLICY_TYPES = [
    ["GroovyAuthorizationPolicy", "Groovy"],
    ["RequiredGrouperGroupsAuthorizationPolicy", "Grouper Groups"],
    ["RequiredGrouperPermissionsAuthorizationPolicy", "Grouper Permissions"],
    ["RequiredAttributesAuthorizationPolicy", "Required Attributes"],
    ["RejectedAttributesAuthorizationPolicy", "Rejected Attributes"],
    ["RequiredACRAuthorizationPolicy", "Required ACR"],
    ["RequiredAMRAuthorizationPolicy", "Required AMR"],
    ["RequiredAudienceAuthorizationPolicy", "Required Audience"],
    ["RequiredIssuerAuthorizationPolicy", "Required Issuer"],
    ["RequiredScopesAuthorizationPolicy", "Required Scopes"],
    ["RestfulAuthorizationPolicy", "REST API"],
    ["OpenFGAAuthorizationPolicy", "OpenFGA"],
    ["JdbcAuthorizationPolicy", "JDBC"]
];

let heimdallResourcesByNamespace = {};
let reloadHeimdallResourcesTable = null;
let heimdallPolicySequence = 0;
let heimdallAuthorizationResponseEditor = null;
let heimdallAuthorizationSimulationMode = "heimdall";

function escapeHeimdallHtml(value) {
    return $("<div>").text(value ?? "").html();
}

function renderHeimdallHttpMethod(method) {
    const value = String(method ?? "N/A");
    const normalized = value.toLowerCase();
    const knownMethods = ["get", "post", "put", "patch", "delete", "head", "options", "trace", "connect"];
    const methodClass = knownMethods.includes(normalized) ? `method-${normalized}` : "method-any";
    return `<span class="http-exchange-method ${methodClass}">${escapeHeimdallHtml(value)}</span>`;
}

function renderHeimdallPolicyEnforcement(enforced) {
    const icon = enforced ? "mdi-check-circle" : "mdi-checkbox-blank-circle-outline";
    const status = enforced ? "enforced" : "not-enforced";
    const label = enforced ? "All policies are enforced" : "Not all policies are required";
    return `<span class="heimdall-policy-enforcement heimdall-policy-enforcement-${status}" title="${label}">
                <i class="mdi ${icon}" aria-hidden="true"></i><span class="visually-hidden">${label}</span>
            </span>`;
}

function heimdallDeepClone(value) {
    return JSON.parse(JSON.stringify(value));
}

function isHeimdallAuthZenSimulation() {
    return heimdallAuthorizationSimulationMode === "authzen";
}

function getHeimdallAuthorizationSimulationMap(containerId) {
    const result = {};
    $(`#${containerId} [data-mapped-input-row='true']`).each(function () {
        const row = $(this);
        const key = row.find("input[data-mapped-field-role='key']").val()?.trim();
        if (key) {
            result[key] = row.find("input[data-mapped-field-role='value']").val() ?? "";
        }
    });
    return result;
}

function buildHeimdallAuthorizationSimulationPayload() {
    const field = id => $(`#${id}`).val()?.trim() ?? "";
    const context = getHeimdallAuthorizationSimulationMap("heimdallAuthorizationContextContainer");
    if (isHeimdallAuthZenSimulation()) {
        return {
            subject: {
                type: field("heimdallAuthorizationSubjectType"),
                id: field("heimdallAuthorizationSubjectId"),
                properties: getHeimdallAuthorizationSimulationMap("heimdallAuthorizationSubjectPropertiesContainer")
            },
            resource: {
                type: field("heimdallAuthorizationResourceType"),
                id: field("heimdallAuthorizationResourceId"),
                properties: getHeimdallAuthorizationSimulationMap("heimdallAuthorizationResourcePropertiesContainer")
            },
            action: {
                name: field("heimdallAuthorizationActionName")
            },
            context: context
        };
    }
    return {
        method: field("heimdallAuthorizationMethod"),
        uri: field("heimdallAuthorizationUri"),
        namespace: field("heimdallAuthorizationNamespace"),
        context: context
    };
}

function initializeHeimdallAuthorizationSimulationMaps() {
    const maps = [
        {
            containerId: "heimdallAuthorizationSubjectPropertiesContainer",
            keyField: "heimdallAuthorizationSubjectPropertyKey",
            valueField: "heimdallAuthorizationSubjectPropertyValue"
        },
        {
            containerId: "heimdallAuthorizationResourcePropertiesContainer",
            keyField: "heimdallAuthorizationResourcePropertyKey",
            valueField: "heimdallAuthorizationResourcePropertyValue"
        },
        {
            containerId: "heimdallAuthorizationContextContainer",
            keyField: "heimdallAuthorizationContextKey",
            valueField: "heimdallAuthorizationContextValue"
        }
    ];
    maps.forEach(map => {
        createMappedInputField({
            ...map,
            keyLabel: "Key",
            valueLabel: "Value",
            cssClasses: "heimdall-authorization-map",
            onChangeCallback: clearHeimdallAuthorizationSimulationResponse
        });
        cas.attachFields(`#${map.containerId}`);
    });
}

function clearHeimdallAuthorizationSimulationResponse() {
    heimdallAuthorizationResponseEditor?.setValue("", -1);
}

function selectHeimdallAuthorizationSimulationMode(mode) {
    heimdallAuthorizationSimulationMode = mode;
    const authZen = mode === "authzen";
    const standardFields = $("#heimdallAuthorizationStandardFields");
    const authZenFields = $("#heimdallAuthorizationAuthZenFields");
    standardFields.find(":input").prop("disabled", authZen);
    authZenFields.find(":input").prop("disabled", !authZen);
    if (authZen) {
        hideElements(standardFields);
        showElements(authZenFields);
    } else {
        showElements(standardFields);
        hideElements(authZenFields);
    }
    $("#heimdallAuthorizationHeimdallModeButton")
        .toggleClass("mdc-button--raised", !authZen)
        .toggleClass("mdc-button--outlined", authZen)
        .attr("aria-pressed", String(!authZen));
    $("#heimdallAuthorizationAuthZenModeButton")
        .toggleClass("mdc-button--raised", authZen)
        .toggleClass("mdc-button--outlined", !authZen)
        .attr("aria-pressed", String(authZen));
    $("#heimdallAuthorizationSubmitButtonLabel").text(authZen ? "Evaluate" : "Authorize");
    clearHeimdallAuthorizationSimulationResponse();
}

function renderHeimdallAuthorizationResponse(response) {
    const responseBody = response ?? {};
    const responseContent = typeof responseBody === "string"
        ? responseBody
        : JSON.stringify(responseBody, null, 2);
    heimdallAuthorizationResponseEditor.setValue(responseContent, -1);
}

function extractHeimdallAuthorizationErrorResponse(xhr, error) {
    if (xhr.responseJSON) {
        return xhr.responseJSON;
    }
    const responseText = String(xhr.responseText ?? "").trim();
    if (responseText) {
        try {
            return JSON.parse(responseText);
        } catch (_ignored) {
            return {message: responseText};
        }
    }
    return {message: error || "Unable to contact the CAS server."};
}

function submitHeimdallAuthorizationSimulation() {
    const form = document.getElementById("heimdallAuthorizationSimulationForm");
    if (!form.reportValidity()) {
        return;
    }

    const authZen = isHeimdallAuthZenSimulation();
    const payload = buildHeimdallAuthorizationSimulationPayload();
    const endpoint = `${String(PalantirDashboardConfiguration.casServerPrefix()).replace(/\/$/, "")}`
        + `/heimdall/${authZen ? "authzen" : "authorize"}`;
    const submitButton = $("#heimdallAuthorizationSubmitButton");
    submitButton.prop("disabled", true).attr("aria-busy", "true");
    heimdallAuthorizationResponseEditor.setValue("", -1);

    $.ajax({
        url: endpoint,
        method: "POST",
        contentType: "application/json",
        dataType: "json",
        headers: {
            Authorization: $("#heimdallAuthorizationHeader").val().trim()
        },
        data: JSON.stringify(payload)
    }).done(response => {
        renderHeimdallAuthorizationResponse(response);
    }).fail((xhr, _status, error) => {
        renderHeimdallAuthorizationResponse(extractHeimdallAuthorizationErrorResponse(xhr, error));
    }).always(() => {
        submitButton.prop("disabled", false).removeAttr("aria-busy");
    });
}

function heimdallExtractArray(value) {
    if (Array.isArray(value) && value.length === 2 && typeof value[0] === "string" && Array.isArray(value[1])) {
        return value[1];
    }
    return Array.isArray(value) ? value : [];
}

function heimdallCsv(value) {
    if (!value || value.trim().length === 0) {
        return [];
    }
    return value.split(",").map(item => item.trim()).filter(item => item.length > 0);
}

function heimdallPlainMap(value, unwrapValues = false) {
    if (!value) {
        return {};
    }
    const source = Array.isArray(value) && value.length === 2 && typeof value[1] === "object" ? value[1] : value;
    return Object.fromEntries(Object.entries(source)
        .filter(([key]) => key !== "@class")
        .map(([key, mapValue]) => [key, unwrapValues ? heimdallExtractArray(mapValue) : mapValue]));
}

function heimdallPolicyForStorage(policy) {
    const normalized = heimdallDeepClone(policy);
    const policyType = normalized["@class"]?.split(".").pop();
    const attributes = heimdallPlainMap(normalized.attributes, true);
    const specialAttributePolicies = {
        RequiredACRAuthorizationPolicy: ["acrs", "acr", true],
        RequiredAMRAuthorizationPolicy: ["amrs", "amr", true],
        RequiredAudienceAuthorizationPolicy: ["audience", "aud", true],
        RequiredIssuerAuthorizationPolicy: ["issuer", "iss", false],
        RequiredScopesAuthorizationPolicy: ["scopes", "scope", true]
    };
    if (specialAttributePolicies[policyType]) {
        const [field, attribute, multiple] = specialAttributePolicies[policyType];
        const values = heimdallExtractArray(normalized[field]).length > 0
            ? heimdallExtractArray(normalized[field])
            : attributes[attribute] ?? [];
        normalized[field] = multiple ? values : values[0] ?? normalized[field];
        delete normalized.attributes;
    } else if (["RequiredAttributesAuthorizationPolicy", "RejectedAttributesAuthorizationPolicy"].includes(policyType)) {
        normalized.attributes = attributes;
    }
    if (normalized.groups) {
        normalized.groups = heimdallExtractArray(normalized.groups);
    }
    if (normalized.headers) {
        normalized.headers = heimdallPlainMap(normalized.headers);
    }
    return normalized;
}

function heimdallResourceForStorage(resource) {
    const normalized = {
        "@class": HEIMDALL_RESOURCE_CLASS,
        id: Number(resource.id),
        enforceAllPolicies: resource.enforceAllPolicies === true,
        policies: (resource.policies ?? []).map(heimdallPolicyForStorage)
    };
    if (resource.pattern) {
        normalized.pattern = resource.pattern;
    }
    if (resource.method) {
        normalized.method = resource.method;
    }
    if (resource.properties && Object.keys(resource.properties).length > 0) {
        normalized.properties = heimdallPlainMap(resource.properties);
    }
    return normalized;
}

function heimdallTextField({id, label, type = "text", title = "", required = false, value = ""}) {
    const field = $(
        `<label for="${id}" class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label mb-2 w-100">
            <span class="mdc-notched-outline">
                <span class="mdc-notched-outline__leading"></span>
                <span class="mdc-notched-outline__notch"><span class="mdc-floating-label">${label}</span></span>
                <span class="mdc-notched-outline__trailing"></span>
            </span>
            <input class="mdc-text-field__input form-control" id="${id}" name="${id}"
                   type="${type}" title="${title}" autocomplete="off" tabindex="0" size="50"
                   ${required ? "required" : ""}/>
        </label>`);
    field.find("input").val(value);
    decoratePalantirInputIcons(field[0]);
    return field;
}

function heimdallTextArea({id, label, title = "", rows = 4}) {
    return $(
        `<label for="${id}" class="mdc-text-field mdc-text-field--outlined mdc-text-field--textarea control-label mb-2 w-100">
            <span class="mdc-notched-outline">
                <span class="mdc-notched-outline__leading"></span>
                <span class="mdc-notched-outline__notch"><span class="mdc-floating-label">${label}</span></span>
                <span class="mdc-notched-outline__trailing"></span>
            </span>
            <textarea class="mdc-text-field__input form-control" id="${id}" name="${id}"
                      rows="${rows}" title="${title}"></textarea>
        </label>`);
}

function heimdallSelectField({id, label, options}) {
    const field = $(
        `<label for="${id}" class="heimdall-select-field mb-2 w-100">
            <span>${label}</span>
            <select class="jqueryui-selectmenu w-100" id="${id}" name="${id}"></select>
        </label>`);
    const select = field.find("select");
    for (const option of options) {
        const value = typeof option === "string" ? option : option.value;
        const text = typeof option === "string" ? option : option.text;
        select.append($("<option>", {value: value, text: text}));
    }
    return field;
}

function heimdallPolicyFieldGroup(policyIndex, policyTypes, fields) {
    const group = $("<div>", {
        class: "heimdall-policy-fields",
        "data-policy-types": policyTypes.join(",")
    });
    for (const field of fields) {
        const id = `heimdallPolicy${policyIndex}${field.key}`;
        const element = field.kind === "map"
            ? $("<div>", {
                id: `${id}MapHost`,
                class: "heimdall-policy-map-host",
                "data-policy-map-field": field.key,
                "data-policy-map-label": field.label,
                "data-policy-map-multiple": field.multiple === true
            })
            : field.kind === "textarea"
            ? heimdallTextArea({id: id, label: field.label, title: field.title, rows: field.rows})
            : field.kind === "select"
                ? heimdallSelectField({id: id, label: field.label, options: field.options})
                : heimdallTextField({
                id: id,
                label: field.label,
                type: field.type,
                title: field.title,
                value: field.value
            });
        element.find("input, textarea, select")
            .attr("data-policy-field", field.key)
            .attr("placeholder", field.placeholder ?? "");
        if (field.value !== undefined) {
            element.find("input, textarea, select").val(field.value);
        }
        group.append(element);
        if (field.groovyEditorTitle) {
            attachPalantirGroovyEditorButton({
                target: element.find("input").first(),
                title: field.groovyEditorTitle
            });
        }
    }
    return group;
}

function showHeimdallPolicyFields(card) {
    const selectedType = card.find("select[data-policy-type]").val();
    card.find(".heimdall-policy-fields").each(function () {
        const fieldGroup = $(this);
        const visible = fieldGroup.data("policy-types").split(",").includes(selectedType);
        fieldGroup.toggle(visible);
        fieldGroup.find("input, textarea, select").prop("disabled", !visible);
        fieldGroup.find(".mdc-text-field")
            .toggleClass("mdc-text-field--disabled", !visible)
            .attr("aria-disabled", String(!visible));
        fieldGroup.find("select.jqueryui-selectmenu").each(function () {
            const dropdown = $(this);
            if (dropdown.selectmenu("instance")) {
                dropdown.selectmenu("option", "disabled", !visible);
            }
        });
    });
}

function addHeimdallPolicy(policy = null) {
    const policyIndex = ++heimdallPolicySequence;
    const policyType = policy?.["@class"]?.split(".").pop();
    if (policy && !HEIMDALL_POLICY_TYPES.some(([type]) => type === policyType)) {
        return null;
    }
    const selectedType = policyType ?? HEIMDALL_POLICY_TYPES[0][0];
    const policyBodyId = `heimdallPolicyCardBody${policyIndex}`;
    const card = $("<section>", {
        id: `heimdallPolicyCard${policyIndex}`,
        class: "heimdall-policy-card mb-3 p-3",
        "data-policy-index": policyIndex
    });
    const header = $(
        `<div class="heimdall-policy-card-header mb-3">
            <label class="heimdall-policy-type-label mb-0">
                <span>Policy Type</span>
                <select class="jqueryui-selectmenu w-100" data-policy-type aria-label="Policy type"></select>
            </label>
            <div class="heimdall-policy-actions">
                <button type="button" class="mdc-button mdc-button--outlined heimdall-policy-action heimdall-policy-drag-handle" title="Drag to reorder policy">
                    <span class="mdc-button__label"><i class="mdi mdi-drag" aria-hidden="true"></i><span class="visually-hidden">Drag to reorder</span></span>
                </button>
                <button type="button" class="mdc-button mdc-button--outlined heimdall-policy-action heimdall-policy-collapse"
                        title="Collapse policy" aria-controls="${policyBodyId}" aria-expanded="true">
                    <span class="mdc-button__label"><i class="mdi mdi-chevron-up" aria-hidden="true"></i><span class="visually-hidden">Collapse policy</span></span>
                </button>
                <button type="button" class="mdc-button mdc-button--outlined heimdall-policy-action heimdall-policy-up" title="Move policy up">
                    <span class="mdc-button__label"><i class="mdi mdi-arrow-up" aria-hidden="true"></i><span class="visually-hidden">Move up</span></span>
                </button>
                <button type="button" class="mdc-button mdc-button--outlined heimdall-policy-action heimdall-policy-down" title="Move policy down">
                    <span class="mdc-button__label"><i class="mdi mdi-arrow-down" aria-hidden="true"></i><span class="visually-hidden">Move down</span></span>
                </button>
                <button type="button" class="mdc-button mdc-button--outlined heimdall-policy-action heimdall-policy-remove" title="Remove policy">
                    <span class="mdc-button__label"><i class="mdi mdi-delete" aria-hidden="true"></i><span class="visually-hidden">Remove</span></span>
                </button>
            </div>
        </div>`);
    const select = header.find("select");
    for (const [type, label] of HEIMDALL_POLICY_TYPES) {
        select.append($("<option>", {value: type, text: label}));
    }
    select.val(selectedType);
    card.append(header);

    card.append(heimdallPolicyFieldGroup(policyIndex, ["GroovyAuthorizationPolicy"], [
        {key: "script", label: "Groovy Script", title: "Inline Groovy or a script resource location.",
            groovyEditorTitle: "Heimdall Authorization Policy Groovy Script"}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredGrouperGroupsAuthorizationPolicy"], [
        {key: "groupField", label: "Grouper Group Field", kind: "select",
            options: ["NAME", "EXTENSION", "DISPLAY_NAME", "DISPLAY_EXTENSION"]},
        {key: "groups", label: "Required Groups", placeholder: "a:b:c, a:b:d", title: "Comma-separated Grouper group names."}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredGrouperPermissionsAuthorizationPolicy"], [
        {key: "attributeDefinition", label: "Attribute Definition"},
        {key: "roleName", label: "Role Name"}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex,
        ["RequiredAttributesAuthorizationPolicy", "RejectedAttributesAuthorizationPolicy"], [
            {key: "attributes", label: "Attributes", kind: "map", multiple: true}
        ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredACRAuthorizationPolicy"], [
        {key: "acrs", label: "Required ACR Values", placeholder: "mfa, .*", title: "Comma-separated regular expressions."}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredAMRAuthorizationPolicy"], [
        {key: "amrs", label: "Required AMR Values", placeholder: "pwd, mfa", title: "Comma-separated regular expressions."}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredAudienceAuthorizationPolicy"], [
        {key: "audience", label: "Required Audience Values", placeholder: "https://api.example.org", title: "Comma-separated regular expressions."}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredIssuerAuthorizationPolicy"], [
        {key: "issuer", label: "Required Issuer", placeholder: "^https://issuer\\.example\\.org$"}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RequiredScopesAuthorizationPolicy"], [
        {key: "scopes", label: "Required Scopes", placeholder: "openid, profile", title: "Comma-separated scopes."}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["RestfulAuthorizationPolicy"], [
        {key: "url", label: "REST URL", placeholder: "https://api.example.org/authorize"},
        {key: "method", label: "HTTP Method", kind: "select", value: "POST",
            options: ["GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS", "TRACE"]},
        {key: "headers", label: "Headers", kind: "map"}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["OpenFGAAuthorizationPolicy"], [
        {key: "apiUrl", label: "OpenFGA API URL"},
        {key: "storeId", label: "Store ID"},
        {key: "token", label: "Bearer Token", type: "password"},
        {key: "relation", label: "Relation", value: "owner"},
        {key: "userType", label: "User Type", value: "user"}
    ]));
    card.append(heimdallPolicyFieldGroup(policyIndex, ["JdbcAuthorizationPolicy"], [
        {key: "url", label: "JDBC URL", placeholder: "jdbc:postgresql://localhost/cas"},
        {key: "username", label: "Username"},
        {key: "password", label: "Password", type: "password"},
        {key: "query", label: "SQL Query", placeholder: "select authorized from ..."}
    ]));
    const policyBody = $("<div>", {id: policyBodyId, class: "heimdall-policy-card-body"});
    card.children(".heimdall-policy-fields").appendTo(policyBody);
    card.append(policyBody);
    $("#heimdallPolicies").append(card);
    card.find("[data-policy-map-field]").each(function () {
        const host = $(this);
        const mapField = host.attr("data-policy-map-field");
        createMappedInputField({
            header: host.attr("data-policy-map-label"),
            containerId: host.attr("id"),
            keyField: `heimdallPolicy${policyIndex}${mapField}Key`,
            keyLabel: "Key",
            valueField: `heimdallPolicy${policyIndex}${mapField}Value`,
            valueLabel: host.attr("data-policy-map-multiple") === "true" ? "Values" : "Value",
            cssClasses: "heimdall-policy-map",
            onChangeCallback: generateHeimdallResourcePayload
        });
    });
    showHeimdallPolicyFields(card);

    card.find(".heimdall-policy-collapse").on("click", function () {
        const button = $(this);
        const collapsed = !card.hasClass("heimdall-policy-card-collapsed");
        card.toggleClass("heimdall-policy-card-collapsed", collapsed);
        button.attr("aria-expanded", String(!collapsed))
            .attr("title", collapsed ? "Expand policy" : "Collapse policy");
        button.find(".mdi")
            .toggleClass("mdi-chevron-up", !collapsed)
            .toggleClass("mdi-chevron-down", collapsed);
        button.find(".visually-hidden").text(collapsed ? "Expand policy" : "Collapse policy");
        policyBody.stop(true, true).slideToggle(150);
    });
    card.find(".heimdall-policy-remove").on("click", () => {
        card.remove();
        refreshHeimdallPolicySorting();
        generateHeimdallResourcePayload();
    });
    card.find(".heimdall-policy-up").on("click", () => {
        const previous = card.prev(".heimdall-policy-card");
        if (previous.length) {
            card.insertBefore(previous);
            refreshHeimdallPolicySorting();
            generateHeimdallResourcePayload();
        }
    });
    card.find(".heimdall-policy-down").on("click", () => {
        const next = card.next(".heimdall-policy-card");
        if (next.length) {
            card.insertAfter(next);
            refreshHeimdallPolicySorting();
            generateHeimdallResourcePayload();
        }
    });

    if (policy) {
        prefillHeimdallPolicy(card, policy, selectedType);
    }
    card.find("select.jqueryui-selectmenu").each(function () {
        const dropdown = $(this);
        dropdown.selectmenu({
            width: "100%",
            change: () => {
                if (dropdown.is("[data-policy-type]")) {
                    showHeimdallPolicyFields(card);
                }
                generateHeimdallResourcePayload();
            }
        });
    });
    return card;
}

function refreshHeimdallPolicySorting() {
    const policies = $("#heimdallPolicies");
    if (typeof policies.sortable !== "function") {
        return;
    }
    if (policies.sortable("instance")) {
        policies.sortable("refresh");
    }
}

function initializeHeimdallPolicySorting() {
    const policies = $("#heimdallPolicies");
    if (typeof policies.sortable !== "function") {
        return;
    }
    if (policies.sortable("instance")) {
        policies.sortable("destroy");
    }
    policies.sortable({
        items: "> .heimdall-policy-card",
        handle: ".heimdall-policy-drag-handle",
        cancel: "input, textarea, select, option, .ui-selectmenu-button",
        placeholder: "heimdall-policy-sort-placeholder",
        forcePlaceholderSize: true,
        tolerance: "pointer",
        start: (_event, ui) => ui.item.addClass("heimdall-policy-card-dragging"),
        stop: (_event, ui) => ui.item.removeClass("heimdall-policy-card-dragging"),
        update: generateHeimdallResourcePayload
    });
}

function setHeimdallPolicyField(card, key, value) {
    const selectedType = card.find("select[data-policy-type]").val();
    const activeFields = card.find(".heimdall-policy-fields").filter(function () {
        return $(this).data("policy-types").split(",").includes(selectedType);
    });
    const field = activeFields.find(`[data-policy-field="${key}"]`);
    if (field.length && value !== undefined && value !== null) {
        field.val(value);
        if (String(value).length > 0) {
            const textField = field.closest(".mdc-text-field");
            textField.addClass("mdc-text-field--label-floating");
            textField.find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
            textField.find(".mdc-notched-outline").addClass("mdc-notched-outline--notched");
        }
    }
}

function prefillHeimdallMappedRows(container, entries, valueRenderer = value => value) {
    if (entries.length === 0) {
        return;
    }
    const addButton = container.find("button.add-row").first();
    for (let index = 1; index < entries.length; index++) {
        addButton.trigger("click");
    }
    const rows = container.find("[data-mapped-input-row='true']");
    entries.forEach(([key, value], index) => {
        const row = rows.eq(index);
        setHeimdallMappedFieldValue(row.find("input[data-mapped-field-role='key']"), key);
        setHeimdallMappedFieldValue(row.find("input[data-mapped-field-role='value']"), valueRenderer(value));
    });
}

function prefillHeimdallPolicyMap(card, key, value, multipleValues = false) {
    const host = card.find(`[data-policy-map-field="${key}"]`);
    const entries = Object.entries(heimdallPlainMap(value, multipleValues));
    prefillHeimdallMappedRows(host, entries, mapValue => multipleValues
        ? heimdallExtractArray(mapValue).join(", ")
        : String(mapValue ?? ""));
}

function prefillHeimdallPolicy(card, policy, policyType) {
    for (const key of ["script", "groupField", "attributeDefinition", "roleName", "issuer", "url", "method",
        "apiUrl", "storeId", "token", "relation", "userType", "username", "password", "query"]) {
        setHeimdallPolicyField(card, key, policy[key]);
    }
    for (const key of ["groups", "acrs", "amrs", "audience", "scopes"]) {
        setHeimdallPolicyField(card, key, heimdallExtractArray(policy[key]).join(", "));
    }
    if (policy.attributes) {
        const attributes = heimdallPlainMap(policy.attributes, true);
        prefillHeimdallPolicyMap(card, "attributes", attributes, true);
        const aliases = {
            RequiredACRAuthorizationPolicy: ["acrs", "acr", true],
            RequiredAMRAuthorizationPolicy: ["amrs", "amr", true],
            RequiredAudienceAuthorizationPolicy: ["audience", "aud", true],
            RequiredIssuerAuthorizationPolicy: ["issuer", "iss", false],
            RequiredScopesAuthorizationPolicy: ["scopes", "scope", true]
        };
        if (aliases[policyType]) {
            const [field, attribute, multiple] = aliases[policyType];
            const values = attributes[attribute] ?? [];
            setHeimdallPolicyField(card, field, multiple ? values.join(", ") : values[0]);
        }
    }
    if (policy.headers) {
        prefillHeimdallPolicyMap(card, "headers", policy.headers);
    }
}

function getHeimdallPolicyMap(card, key, multipleValues = false) {
    const result = {};
    card.find(`[data-policy-map-field="${key}"] [data-mapped-input-row='true']`).each(function () {
        const row = $(this);
        const mapKey = row.find("input[data-mapped-field-role='key']").val()?.trim();
        if (mapKey) {
            const value = row.find("input[data-mapped-field-role='value']").val() ?? "";
            result[mapKey] = multipleValues ? heimdallCsv(value) : value;
        }
    });
    return result;
}

function buildHeimdallPolicy(card) {
    const type = card.find("select[data-policy-type]").val();
    const activeFields = card.find(".heimdall-policy-fields").filter(function () {
        return $(this).data("policy-types").split(",").includes(type);
    });
    const field = key => activeFields.find(`[data-policy-field="${key}"]`).val()?.trim() ?? "";
    const policy = {"@class": `${HEIMDALL_POLICY_PACKAGE}.${type}`};
    const copy = key => {
        if (field(key)) {
            policy[key] = field(key);
        }
    };
    const copyCsv = key => {
        const values = heimdallCsv(field(key));
        if (values.length > 0) {
            policy[key] = values;
        }
    };

    if (type === "GroovyAuthorizationPolicy") {
        copy("script");
    } else if (type === "RequiredGrouperGroupsAuthorizationPolicy") {
        copy("groupField");
        copyCsv("groups");
    } else if (type === "RequiredGrouperPermissionsAuthorizationPolicy") {
        copy("attributeDefinition");
        copy("roleName");
    } else if (["RequiredAttributesAuthorizationPolicy", "RejectedAttributesAuthorizationPolicy"].includes(type)) {
        policy.attributes = getHeimdallPolicyMap(card, "attributes", true);
    } else if (type === "RequiredACRAuthorizationPolicy") {
        copyCsv("acrs");
    } else if (type === "RequiredAMRAuthorizationPolicy") {
        copyCsv("amrs");
    } else if (type === "RequiredAudienceAuthorizationPolicy") {
        copyCsv("audience");
    } else if (type === "RequiredIssuerAuthorizationPolicy") {
        copy("issuer");
    } else if (type === "RequiredScopesAuthorizationPolicy") {
        copyCsv("scopes");
    } else if (type === "RestfulAuthorizationPolicy") {
        copy("url");
        copy("method");
        policy.headers = getHeimdallPolicyMap(card, "headers");
    } else if (type === "OpenFGAAuthorizationPolicy") {
        for (const key of ["token", "apiUrl", "storeId", "relation", "userType"]) {
            copy(key);
        }
    } else if (type === "JdbcAuthorizationPolicy") {
        for (const key of ["url", "username", "password", "query"]) {
            copy(key);
        }
    }
    return policy;
}

function getHeimdallResourceProperties() {
    const properties = {};
    $("#registeredServiceHeimdallResourcePropertyKeyMapContainer [data-mapped-input-row='true']").each(function () {
        const row = $(this);
        const key = row.find("input[data-mapped-field-role='key']").val()?.trim();
        if (key) {
            properties[key] = row.find("input[data-mapped-field-role='value']").val() ?? "";
        }
    });
    return properties;
}

function setHeimdallMappedFieldValue(input, value) {
    input.val(value);
    if (String(value).length > 0) {
        const textField = input.closest(".mdc-text-field");
        textField.addClass("mdc-text-field--label-floating");
        textField.find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
        textField.find(".mdc-notched-outline").addClass("mdc-notched-outline--notched");
    }
}

function prefillHeimdallResourceProperties(properties) {
    const entries = Object.entries(heimdallPlainMap(properties));
    const container = $("#registeredServiceHeimdallResourcePropertyKeyMapContainer");
    prefillHeimdallMappedRows(container, entries, value => typeof value === "string" ? value : JSON.stringify(value));
}

function setHeimdallSwitchState(id, selected) {
    const input = $(`#${id}`);
    const button = $(`#${id}Button`);
    input.val(String(selected));
    button.toggleClass("mdc-switch--selected", selected)
        .toggleClass("mdc-switch--unselected", !selected)
        .attr("aria-checked", String(selected));
}

function buildHeimdallResourcePayload() {
    const dialog = $("#newHeimdallResourceDialog");
    const namespace = $("#heimdallResourceNamespace").val()?.trim();
    const idValue = $("#heimdallResourceId").val();
    if (!namespace || idValue === "") {
        return null;
    }

    const resource = {
        "@class": HEIMDALL_RESOURCE_CLASS,
        id: Number(idValue),
        enforceAllPolicies: $("#heimdallEnforceAllPolicies").val() === "true"
    };
    const pattern = $("#heimdallResourcePattern").val()?.trim();
    const method = $("#heimdallResourceMethod").val()?.trim();
    if (pattern) {
        resource.pattern = pattern;
    }
    if (method) {
        resource.method = method;
    }

    const properties = getHeimdallResourceProperties();
    if (Object.keys(properties).length > 0) {
        resource.properties = properties;
    }
    const policies = $("#heimdallPolicies .heimdall-policy-card").toArray()
        .map(policyCard => buildHeimdallPolicy($(policyCard)));
    const preservedPolicies = dialog.data("preserved-policies") ?? [];
    for (const preserved of preservedPolicies.slice().sort((first, second) => first.index - second.index)) {
        policies.splice(Math.min(preserved.index, policies.length), 0, heimdallPolicyForStorage(preserved.policy));
    }
    resource.policies = policies;

    const catalog = dialog.data("resource-catalog") ?? {};
    const originalId = dialog.data("original-id");
    const editMode = dialog.data("edit-mode") === true;
    const resources = heimdallDeepClone(catalog[namespace] ?? []).map(heimdallResourceForStorage);
    const existingIndex = editMode ? resources.findIndex(item => Number(item.id) === Number(originalId)) : -1;
    if (existingIndex >= 0) {
        resources[existingIndex] = resource;
    } else {
        resources.push(resource);
    }
    return {
        "@class": HEIMDALL_RESOURCES_CLASS,
        resources: resources,
        namespace: namespace
    };
}

function generateHeimdallResourcePayload() {
    const editorElement = document.getElementById("heimdallResourceEditor");
    if (!editorElement || typeof ace === "undefined") {
        return null;
    }
    const editor = ace.edit("heimdallResourceEditor");
    try {
        const payload = buildHeimdallResourcePayload();
        editor.session.clearAnnotations();
        editor.setValue(payload ? JSON.stringify(payload, null, 2) : "", -1);
        return payload;
    } catch (error) {
        editor.setValue("", -1);
        editor.session.setAnnotations([{row: 0, column: 0, text: error.message, type: "error"}]);
        return null;
    }
}

function toggleHeimdallResourceEditorVisibility() {
    const showEditor = $("#showHeimdallResourceJsonEditor").val() === "true";
    localStorage.setItem(HEIMDALL_EDITOR_PREFERENCE, String(showEditor));
    $("#heimdallResourceEditorContainer").toggle(showEditor);
    $("#heimdallResourceControls").toggleClass("heimdall-resource-controls-expanded", !showEditor);
    if (showEditor && typeof ace !== "undefined") {
        setTimeout(() => ace.edit("heimdallResourceEditor").resize(true), 50);
    }
}

function validateHeimdallResourceIdentity(payload) {
    const dialog = $("#newHeimdallResourceDialog");
    const editMode = dialog.data("edit-mode") === true;
    const originalId = dialog.data("original-id");
    const catalog = dialog.data("resource-catalog") ?? {};
    const id = Number($("#heimdallResourceId").val());
    const duplicate = (catalog[payload.namespace] ?? []).some(resource => Number(resource.id) === Number(id)
        && (!editMode || Number(resource.id) !== Number(originalId)));
    if (duplicate) {
        throw new Error(`Resource ID ${id} already exists in namespace ${payload.namespace}.`);
    }
}

function storeHeimdallResources(payload) {
    return $.ajax({
        url: `${CasActuatorEndpoints.heimdall()}/resources`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    });
}

function prefillHeimdallResourceDialog(resource) {
    $("#heimdallResourceId").val(resource.id);
    $("#heimdallResourcePattern").val(resource.pattern ?? "");
    $("#heimdallResourceMethod").val(resource.method ?? "");
    setHeimdallSwitchState("heimdallEnforceAllPolicies", resource.enforceAllPolicies === true);
    prefillHeimdallResourceProperties(resource.properties);
    const preservedPolicies = [];
    (resource.policies ?? []).forEach((policy, index) => {
        const policyType = policy?.["@class"]?.split(".").pop();
        if (HEIMDALL_POLICY_TYPES.some(([type]) => type === policyType)) {
            addHeimdallPolicy(policy);
        } else {
            preservedPolicies.push({index: index, policy: policy});
        }
    });
    $("#newHeimdallResourceDialog").data("preserved-policies", preservedPolicies);
}

function newHeimdallResource(prefillData = null, options = {}) {
    const editMode = options.editMode === true;
    const dialog = $("<div>", {id: "newHeimdallResourceDialog"});
    dialog.data("edit-mode", editMode);
    dialog.data("original-id", prefillData?.id);
    dialog.data("resource-catalog", heimdallDeepClone(heimdallResourcesByNamespace));

    const layout = $("<div>", {class: "heimdall-resource-dialog-layout"});
    const controls = $("<form>", {id: "heimdallResourceControls", class: "heimdall-resource-controls"})
        .on("submit", event => event.preventDefault());
    const editorContainer = $(
        `<div id="heimdallResourceEditorContainer" class="heimdall-resource-editor-container">
            <pre class="ace-editor ace-relative w-100 h-100 m-0" id="heimdallResourceEditor"></pre>
        </div>`);

    const originalToggleFields = $("#newHeimdallResourceDialogFields").detach();
    const showEditorToggle = originalToggleFields.find("#showHeimdallResourceJsonEditorSwitchButtonPanel").clone(true);
    const enforceAllToggle = originalToggleFields.find("#heimdallEnforceAllPoliciesSwitchButtonPanel").clone(true);
    showElements(showEditorToggle);
    showElements(enforceAllToggle);

    controls.append(heimdallTextField({
        id: "heimdallResourceNamespace", label: "Namespace", required: true,
        title: "Logical owner of the resource.", value: options.namespace ?? ""
    }));
    controls.append(heimdallTextField({
        id: "heimdallResourceId", label: "Resource ID", type: "number", required: true,
        title: "Unique numeric identifier within this namespace.", value: editMode ? prefillData.id : Date.now()
    }));
    controls.append(heimdallTextField({
        id: "heimdallResourcePattern", label: "URI Pattern", title: "Regular expression for the protected URI; optional for AuthZEN."
    }));
    controls.append(heimdallTextField({
        id: "heimdallResourceMethod", label: "HTTP Method Pattern", title: "HTTP method regular expression, or * for all; optional for AuthZEN."
    }));
    controls.append(enforceAllToggle);
    controls.append('<div id="heimdallResourcePropertiesContainer"></div>');
    controls.append(
        `<div class="d-flex align-items-center justify-content-between mt-2 mb-2">
            <h3 class="m-0">Authorization Policies</h3>
            <button type="button" id="addHeimdallPolicyButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdi mdi-plus-thick"></i>Add Policy</span>
            </button>
        </div>
        <div id="heimdallPolicies"></div>`);

    layout.append(controls, editorContainer);
    dialog.append(layout);
    $("body").append(dialog);

    createMappedInputField({
        header: "Resource Properties",
        containerId: "heimdallResourcePropertiesContainer",
        keyField: "heimdallResourcePropertyKey",
        keyLabel: "Key",
        valueField: "heimdallResourcePropertyValue",
        valueLabel: "Value",
        cssClasses: "heimdall-resource-property",
        onChangeCallback: generateHeimdallResourcePayload
    });

    dialog.dialog({
        title: editMode ? "Edit Authorizable Resource" : "New Authorizable Resource",
        modal: true,
        width: Math.min(1700, $(window).width() - 40),
        autoOpen: false,
        position: {my: "center top", at: "center top+40", of: window},
        buttons: {
            Save: function () {
                const form = document.getElementById("heimdallResourceControls");
                if (!form.reportValidity()) {
                    return;
                }
                try {
                    const payload = buildHeimdallResourcePayload();
                    validateHeimdallResourceIdentity(payload);
                    storeHeimdallResources(payload)
                        .done(() => {
                            dialog.dialog("close");
                            reloadHeimdallResourcesTable?.();
                        })
                        .fail((xhr, status, error) => {
                            console.error("Error storing Heimdall resource:", status, error);
                            displayBanner(xhr);
                        });
                } catch (error) {
                    displayBanner(error);
                }
            },
            Cancel: function () {
                dialog.dialog("close");
            }
        },
        open: function () {
            const dialogShell = dialog.closest(".ui-dialog").addClass("heimdall-resource-dialog");
            const maxHeight = Math.max(440, Math.min(900, $(window).height() - 110));
            dialog.css({height: `${maxHeight}px`, "max-height": `${maxHeight}px`, overflow: "hidden"});
            editorContainer.css("height", "100%");

            const buttonPane = dialogShell.find(".ui-dialog-buttonpane");
            buttonPane.css({display: "flex", "align-items": "center"});
            const footerToggles = $("<div>", {class: "heimdall-dialog-footer-toggles"});
            footerToggles.append(showEditorToggle);
            buttonPane.prepend(footerToggles);

            const savedPreference = localStorage.getItem(HEIMDALL_EDITOR_PREFERENCE);
            setHeimdallSwitchState("showHeimdallResourceJsonEditor", savedPreference !== "false");

            if (editMode) {
                $("#heimdallResourceNamespace").val(options.namespace).prop("readonly", true);
                prefillHeimdallResourceDialog(prefillData);
            }
            if (!editMode && !$("#heimdallPolicies .heimdall-policy-card").length) {
                addHeimdallPolicy();
            }
            initializeHeimdallPolicySorting();
            $("#addHeimdallPolicyButton").on("click", () => {
                const policyCard = addHeimdallPolicy();
                cas.init(`#${policyCard.attr("id")}`);
                refreshHeimdallPolicySorting();
                generateHeimdallResourcePayload();
            });
            dialog.on("input change", "input:not([type='hidden']), textarea", generateHeimdallResourcePayload);

            cas.init("#newHeimdallResourceDialog");
            cas.init(".heimdall-resource-dialog .ui-dialog-buttonpane");

            const editor = initializeAceEditor("heimdallResourceEditor", "json");
            editor.setReadOnly(true);
            editor.setOptions({showPrintMargin: false});

            toggleHeimdallResourceEditorVisibility();
            generateHeimdallResourcePayload();
            setTimeout(() => editor.resize(true), 50);
        },
        close: function () {
            const policies = $("#heimdallPolicies");
            if (typeof policies.sortable === "function" && policies.sortable("instance")) {
                policies.sortable("destroy");
            }
            $("#heimdall-tab > p").after(originalToggleFields);
            dialog.dialog("destroy").remove();
        }
    });
    dialog.dialog("open");
}

function editHeimdallResource(namespace, resourceId) {
    $.get(`${CasActuatorEndpoints.heimdall()}/resources/${encodeURIComponent(namespace)}`, resources => {
        heimdallResourcesByNamespace[namespace] = resources;
        const resource = resources.find(entry => Number(entry.id) === Number(resourceId));
        if (!resource) {
            displayBanner(`Unable to locate resource ${resourceId} in namespace ${namespace}.`);
            return;
        }
        newHeimdallResource(resource, {editMode: true, namespace: namespace});
    }).fail((xhr, status, error) => {
        console.error("Error fetching Heimdall resource:", status, error);
        displayBanner(xhr);
    });
}

function deleteHeimdallResource(namespace, resourceId) {
    Swal.fire({
        title: `Delete resource <strong>${escapeHeimdallHtml(resourceId)}</strong> from <strong>${escapeHeimdallHtml(namespace)}</strong>?`,
        text: "This change is written to the namespace's authorization resource file.",
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then(result => {
        if (!result.isConfirmed) {
            return;
        }
        $.get(`${CasActuatorEndpoints.heimdall()}/resources/${encodeURIComponent(namespace)}`, resources => {
            const payload = {
                "@class": HEIMDALL_RESOURCES_CLASS,
                resources: resources
                    .filter(resource => Number(resource.id) !== Number(resourceId))
                    .map(heimdallResourceForStorage),
                namespace: namespace
            };
            storeHeimdallResources(payload)
                .done(() => reloadHeimdallResourcesTable?.())
                .fail((xhr, status, error) => {
                    console.error("Error deleting Heimdall resource:", status, error);
                    displayBanner(xhr);
                });
        }).fail((xhr, status, error) => {
            console.error("Error fetching Heimdall resources:", status, error);
            displayBanner(xhr);
        });
    });
}

async function initializeHeimdallOperations() {

    if (!CAS_FEATURES.includes("Authorization")) {
        hideElements("#heimdall");
        return;
    }

    heimdallAuthorizationResponseEditor = initializeAceEditor("heimdallAuthorizationResponseEditor", "json");
    heimdallAuthorizationResponseEditor.setReadOnly(true);
    initializeHeimdallAuthorizationSimulationMaps();
    selectHeimdallAuthorizationSimulationMode("heimdall");

    $("#heimdallAuthorizationSimulationForm").on("submit", event => {
        event.preventDefault();
        submitHeimdallAuthorizationSimulation();
    });
    $("#heimdallAuthorizationHeimdallModeButton").on("click", () => {
        selectHeimdallAuthorizationSimulationMode("heimdall");
    });
    $("#heimdallAuthorizationAuthZenModeButton").on("click", () => {
        selectHeimdallAuthorizationSimulationMode("authzen");
    });
    $("#heimdallAuthorizationSimulationForm").on("input", "input", clearHeimdallAuthorizationSimulationResponse);
    $("#accessstrategy-tabs").on("tabsactivate.heimdallAuthorization", (_event, ui) => {
        if (ui.newPanel.attr("id") === "heimdall-tab") {
            setTimeout(() => {
                heimdallAuthorizationResponseEditor.resize(true);
            }, 50);
        }
    });
    $("#heimdallAuthorizationHeaderReveal").on("click", function () {
        const input = $("#heimdallAuthorizationHeader");
        const showHeader = input.attr("type") === "password";
        input.attr("type", showHeader ? "text" : "password");
        $(this)
            .attr("aria-label", `${showHeader ? "Hide" : "Show"} authorization header`)
            .attr("aria-pressed", String(showHeader))
            .find(".mdi")
            .toggleClass("mdi-eye", !showHeader)
            .toggleClass("mdi-eye-off", showHeader);
    });

    const heimdallToolbar = document.createElement("div");
    heimdallToolbar.innerHTML = `
        <button type="button" id="newHeimdallResourceButton" onclick="newHeimdallResource()"
                title="Create Authorizable Resource" class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>NEW</span>
        </button>`;

    const heimdallResourcesTable = $("#heimdallResourcesTable").DataTable({
        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0},
            {visible: false, targets: 1}
        ],
        autoWidth: false,
        layout: {topStart: heimdallToolbar},
        drawCallback: settings => {
            $("#heimdallResourcesTable tr").addClass("mdc-data-table__row");
            $("#heimdallResourcesTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="3">Namespace: ${escapeHeimdallHtml(group)}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (CasActuatorEndpoints.heimdall()) {
        initializeDataTableContextMenu({
            table: heimdallResourcesTable,
            selector: "#heimdallResourcesTable tbody tr",
            items: {
                edit: {name: "Edit Resource", icon: contextMenuIcon("mdi-pencil")},
                delete: {name: "Delete Resource", icon: contextMenuIcon("mdi-delete")}
            },
            callback: (key, context) => {
                if (key === "edit") {
                    editHeimdallResource(context.rowData.namespace, context.rowData.resourceId);
                } else if (key === "delete") {
                    deleteHeimdallResource(context.rowData.namespace, context.rowData.resourceId);
                }
            }
        });

        function fetchHeimdallResources() {
            $.get(`${CasActuatorEndpoints.heimdall()}/resources`, response => {
                heimdallResourcesByNamespace = response;
                heimdallResourcesTable.clear();
                for (const [key, value] of Object.entries(response)) {
                    for (const resource of Object.values(value)) {
                        heimdallResourcesTable.row.add({
                            0: key,
                            1: `${resource.id ?? "N/A"}`,
                            2: `<code>${escapeHeimdallHtml(resource.pattern ?? "N/A")}</code>`,
                            3: renderHeimdallHttpMethod(resource.method),
                            4: renderHeimdallPolicyEnforcement(resource.enforceAllPolicies === true),
                            namespace: key,
                            resourceId: resource.id
                        });
                    }
                }
                heimdallResourcesTable.draw();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }

        fetchHeimdallResources();
        reloadHeimdallResourcesTable = fetchHeimdallResources;

        setInterval(() => {
            if (currentActiveTab === Tabs.ACCESS_STRATEGY.index) {
                fetchHeimdallResources();
            }
        }, palantirSettings().refreshInterval);
    }

}

async function initializeAccessStrategyOperations() {

    if (!CAS_FEATURES.includes("SAMLIdentityProvider")) {
        hideElements("#accessEntityIdLabel");
    }
    if (!CAS_FEATURES.includes("OpenIDConnect") && !CAS_FEATURES.includes("OAuth")) {
        hideElements("#accessClientIdLabel");
    }

    const accessStrategyEditor = initializeAceEditor("accessStrategyEditor");
    accessStrategyEditor.setReadOnly(true);

    const accessStrategyAttributesTable = $("#accessStrategyAttributesTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#accessStrategyAttributesTable tr").addClass("mdc-data-table__row");
            $("#accessStrategyAttributesTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=accessStrategyButton]").off().on("click", () => {
        if (CasActuatorEndpoints.serviceAccess()) {
            hideBanner();
            accessStrategyAttributesTable.clear();

            const form = document.getElementById("fmAccessStrategy");
            if (!form.reportValidity()) {
                return false;
            }

            accessStrategyEditor.setValue("");
            const formData = $(form).serializeArray();
            const renamedData = formData.filter(item => item.value !== "").map(item => {
                const newName = $(`[name="${item.name}"]`).data("param-name") || item.name;
                return {name: newName, value: item.value};
            });

            $.ajax({
                url: CasActuatorEndpoints.serviceAccess(),
                type: "POST",
                contentType: "application/x-www-form-urlencoded",
                data: $.param(renamedData),
                success: (response, status, xhr) => {
                    showElements("#accessStrategyEditorContainer");
                    showElements("#accessStrategyAttributesContainer");

                    accessStrategyEditor.setValue(JSON.stringify(response.registeredService, null, 2));
                    accessStrategyEditor.gotoLine(1);

                    for (const [key, value] of Object.entries(response.authentication.principal.attributes)) {
                        accessStrategyAttributesTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `<code>${value}</code>`
                        });
                    }
                    accessStrategyAttributesTable.draw();
                    updateNavigationSidebar();
                    $("#authorizedServiceNavigation").off().on("click", () => navigateToApplication(response.registeredService.id));
                },
                error: (xhr, status, error) => {
                    hideElements("#accessStrategyEditorContainer");
                    hideElements("#accessStrategyAttributesContainer");
                    displayBanner(`Status ${xhr.status}: Service is unauthorized.`);
                }
            });
        }
    });
}
