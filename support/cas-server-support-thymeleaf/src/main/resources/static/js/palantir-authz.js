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

function renderHeimdallHttpMethod(method) {
    const value = String(method ?? "N/A");
    const normalized = value.toLowerCase();
    const knownMethods = ["get", "post", "put", "patch", "delete", "head", "options", "trace", "connect"];
    const methodClass = knownMethods.includes(normalized) ? `method-${normalized}` : "method-any";
    return `<span class="http-exchange-method ${methodClass}">${escapePalantirMarkup(value)}</span>`;
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
        .attr("aria-pressed", String(!authZen));
    $("#heimdallAuthorizationAuthZenModeButton")
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
    if (resource.resourceType) {
        normalized.resourceType = resource.resourceType;
    }
    if (resource.resourceIdPattern) {
        normalized.resourceIdPattern = resource.resourceIdPattern;
    }
    const actions = heimdallExtractArray(resource.actions);
    if (actions.length > 0) {
        normalized.actions = actions;
    }
    if (resource.properties && Object.keys(resource.properties).length > 0) {
        normalized.properties = heimdallPlainMap(resource.properties);
    }
    return normalized;
}

function renderHeimdallAuthZenMapping(resource) {
    if (!resource.resourceType) {
        return "N/A";
    }
    const actions = heimdallExtractArray(resource.actions).map(action => `${palantirCode(action)}`);
    const idPattern = resource.resourceIdPattern
        ? ` <span title="Resource ID pattern">(${palantirCode(resource.resourceIdPattern)})</span>`
        : "";
    return `${palantirCode(resource.resourceType)}${idPattern}: ${actions.length > 0 ? actions.join(", ") : "N/A"}`;
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
            }).data("policy-map-hint", field.hint)
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
            {
                key: "attributes", label: "Attributes", kind: "map", multiple: true,
                hint: "Keys are principal attribute names, or request values such as <code>subject.properties.department</code>, "
                    + "<code>resource.properties.owner</code>, <code>action.properties.method</code> or <code>context.channel</code>."
            }
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
        {key: "query", label: "SQL Query", placeholder: "select authorized from ..."},
        {key: "dataSourceName", label: "Data Source Bean Name", placeholder: "Optional; derived from the URL and username"},
        {key: "queryTimeout", label: "Query Timeout", placeholder: "PT5S"}
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
        const hint = host.data("policy-map-hint");
        if (hint) {
            const hintElement = $("<p>", {class: "text-muted small mt-0 mb-2 heimdall-policy-map-hint"}).html(hint);
            const header = host.find("h3").first();
            if (header.length > 0) {
                header.after(hintElement);
            } else {
                host.prepend(hintElement);
            }
        }
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
        "apiUrl", "storeId", "token", "relation", "userType", "username", "password", "query", "dataSourceName", "queryTimeout"]) {
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
        for (const key of ["url", "username", "password", "query", "dataSourceName", "queryTimeout"]) {
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
    input.val(value).attr("title", value);
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
    const resourceType = $("#heimdallResourceType").val()?.trim();
    const resourceIdPattern = $("#heimdallResourceIdPattern").val()?.trim();
    const actions = heimdallCsv($("#heimdallResourceActions").val());
    if (resourceType) {
        resource.resourceType = resourceType;
    }
    if (resourceIdPattern) {
        resource.resourceIdPattern = resourceIdPattern;
    }
    if (actions.length > 0) {
        resource.actions = actions;
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
    $("#heimdallResourceType").val(resource.resourceType ?? "");
    $("#heimdallResourceIdPattern").val(resource.resourceIdPattern ?? "");
    $("#heimdallResourceActions").val(heimdallExtractArray(resource.actions).join(","));
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
    controls.append(heimdallTextField({
        id: "heimdallResourceType", label: "AuthZEN Resource Type",
        title: "Exact AuthZEN resource type that selects this resource; leave blank if AuthZEN requests should not match."
    }));
    controls.append(heimdallTextField({
        id: "heimdallResourceActions", label: "AuthZEN Actions",
        title: "Comma-separated AuthZEN action names, such as can_read,can_write."
    }));
    controls.append(heimdallTextField({
        id: "heimdallResourceIdPattern", label: "AuthZEN Resource ID Pattern",
        title: "Optional regular expression that must match the entire AuthZEN resource id."
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
            dialog.on("input change", "[data-mapped-input-row='true'] input", function () {
                $(this).attr("title", $(this).val());
            });

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
        title: `Delete Resource <strong>${escapePalantirMarkup(resourceId)}</strong> from <strong>${escapePalantirMarkup(namespace)}</strong>?`,
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
        <button type="button" data-palantir-write="heimdall" id="newHeimdallResourceButton" onclick="newHeimdallResource()"
                title="Create Authorizable Resource" class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>NEW</span>
        </button>`;

    const heimdallResourcesTable = $("#heimdallResourcesTable").DataTable({
        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0, render: palantirTextColumn},
            {visible: false, targets: 1, render: palantirTextColumn}
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
                            palantirTableGroupRow(`Namespace: ${group}`, 4));
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
                delete: {name: "Delete Resource", write: "heimdall", icon: contextMenuIcon("mdi-delete")}
            },
            callback: (key, context) => {
                if (key === "edit") {
                    editHeimdallResource(context.rowData.namespace, context.rowData.resourceId);
                } else if (key === "delete") {
                    deleteHeimdallResource(context.rowData.namespace, context.rowData.resourceId);
                }
            }
        });

        const loadHeimdallResources = () => $.get(`${CasActuatorEndpoints.heimdall()}/resources`, response => {
            heimdallResourcesByNamespace = response;
            heimdallResourcesTable.clear();
            for (const [key, value] of Object.entries(response)) {
                for (const resource of Object.values(value)) {
                    heimdallResourcesTable.row.add({
                        0: key,
                        1: String(resource.id ?? "N/A"),
                        2: `${palantirCode(resource.pattern ?? "N/A")}`,
                        3: renderHeimdallHttpMethod(resource.method),
                        4: renderHeimdallAuthZenMapping(resource),
                        5: renderHeimdallPolicyEnforcement(resource.enforceAllPolicies === true),
                        namespace: key,
                        resourceId: resource.id
                    });
                }
            }
            heimdallResourcesTable.draw(false);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
        const fetchHeimdallResources = palantirOneAtATime(loadHeimdallResources);

        fetchHeimdallResources();
        reloadHeimdallResourcesTable = loadHeimdallResources;

        setInterval(() => {
            if (currentActiveTab === Tabs.ACCESS_STRATEGY.index && document.visibilityState === "visible") {
                fetchHeimdallResources();
            }
        }, palantirSettings().refreshInterval);
    }

}

/**
 * The access check: whether a user may access an application, and why, as the steps CAS goes through at sign-in.
 *
 * The application is chosen by name, URL, client id or entity id from the registered applications, which decides the
 * identifier sent: the client id of an OAuth or OpenID Connect client, the entity id of a SAML2 service provider, or a
 * URL the application answers. The answer is drawn as a verdict, the steps as a row of gates, the reasons behind each
 * step, the user's attributes and the application's definition. The last checks are kept in the browser, without any
 * password.
 */
class PalantirAccessCheck {
    static STORAGE_KEY = "PalantirAccessChecks";

    static MAX_RECENT = 8;

    static WIZARD_SECTION = "editServiceWizardMenuItemAccessStrategy";

    static selected = null;

    static sorted = null;

    static patterns = new Map();

    static definitionRequest = 0;

    static attributesTable = null;

    static editor = null;

    static OUTCOMES = {
        PASSED: {icon: "mdi-check-circle", style: "is-passed", label: "Passed"},
        FAILED: {icon: "mdi-close-circle", style: "is-failed", label: "Failed"},
        SKIPPED: {icon: "mdi-minus-circle", style: "is-skipped", label: "Not Checked"},
        INFO: {icon: "mdi-information", style: "is-info", label: "For Information"}
    };

    /**
     * The registered applications in the order CAS evaluates them: by evaluation order, then name, service id and id.
     *
     * @returns {object[]} the applications
     */
    static applications() {
        const key = `${registeredServicesVersion}:${registeredServicesById.size}`;
        if (PalantirAccessCheck.sorted?.key === key) {
            return PalantirAccessCheck.sorted.list;
        }
        const text = value => String(value ?? "");
        const list = [...registeredServicesById.values()].sort((left, right) =>
            (Number(left.evaluationOrder ?? 0) - Number(right.evaluationOrder ?? 0))
            || text(left.name).localeCompare(text(right.name), undefined, {sensitivity: "accent"})
            || text(left.serviceId).localeCompare(text(right.serviceId))
            || (Number(left.id) - Number(right.id)));
        PalantirAccessCheck.sorted = {key, list};
        return list;
    }

    static identifierOf(service) {
        return service.clientId ? String(service.clientId) : String(service.serviceId ?? "");
    }

    /**
     * Whether a service id names one URL rather than a pattern, once escaped dots are read as dots.
     *
     * @param pattern the service id
     * @returns {boolean} true when it is one URL
     */
    static literal(pattern) {
        return !/[\\^$*+?()[\]{}|]/.test(String(pattern ?? "").replace(/\\\./g, "."));
    }

    /**
     * The start of a URL a pattern matches, to offer as the URL to check: the pattern up to its first special
     * character, with escaped dots read as dots.
     *
     * @param pattern the service id
     * @returns {string} the start of the URL, or nothing when the pattern does not start like one
     */
    static urlStart(pattern) {
        const text = String(pattern ?? "").replace(/^\^/, "");
        let start = "";
        for (let index = 0; index < text.length; index++) {
            const character = text[index];
            if (character === "\\" && index + 1 < text.length) {
                start += text[++index];
            } else if (character === "." && /[*+?{]/.test(text[index + 1] ?? "")) {
                break;
            } else if ("^$*+?()[]{}|".includes(character)) {
                break;
            } else {
                start += character;
            }
        }
        return /^https?:\/\//i.test(start) ? start : "";
    }

    /**
     * The registered application that answers a service URL, as CAS would match it: the first CAS application, in
     * evaluation order, whose service id matches the whole URL. OAuth and OpenID Connect applications are only found by
     * their client id, and SAML2 service providers by their entity id, however broad their service id.
     *
     * @param url the URL
     * @returns {object|null} the application
     */
    static matching(url) {
        return PalantirAccessCheck.applications().find(service => {
            return PalantirAccessCheck.protocolOf(service) === "cas" && Boolean(service.serviceId)
                && (PalantirAccessCheck.pattern(service.serviceId)?.test(url) ?? false);
        }) ?? null;
    }

    static initials(name) {
        const words = String(name ?? "").trim().split(/\s+/).filter(Boolean);
        return (words.length > 1 ? words[0][0] + words[1][0] : String(name ?? "?").slice(0, 2)).toUpperCase();
    }

    static protocolOf(service) {
        return applicationProtocolOf(service?.["@class"]).key;
    }

    /**
     * Whether a registered application answers a value: its client id, its service id, or a URL or an entity id its
     * service id matches.
     *
     * @param service the registered application
     * @param value the value
     * @returns {boolean} true when it answers the value
     */
    static answers(service, value) {
        if (!service || value === "") {
            return false;
        }
        if (String(service.clientId ?? "") === value || String(service.serviceId ?? "") === value) {
            return true;
        }
        return PalantirAccessCheck.pattern(service.serviceId)?.test(value) ?? false;
    }

    /**
     * The pattern of a service id, compiled once: the applications are matched again with every key typed.
     *
     * @param serviceId the service id
     * @returns {RegExp|null} the pattern, or null when the service id is not a valid one
     */
    static pattern(serviceId) {
        const key = String(serviceId ?? "");
        if (!PalantirAccessCheck.patterns.has(key)) {
            try {
                PalantirAccessCheck.patterns.set(key, new RegExp(`^(?:${key})$`));
            } catch (error) {
                PalantirAccessCheck.patterns.set(key, null);
            }
        }
        return PalantirAccessCheck.patterns.get(key);
    }

    /**
     * What the application field names: the request parameter to send and the registered application CAS resolves it
     * to: the client id of an OAuth or OpenID Connect client, the entity id of a SAML2 service provider, and otherwise a
     * service URL, matched the way CAS matches it, in evaluation order. When a SAML2 service provider names what is
     * typed as its one entity id and a CAS application also answers it as a URL, nothing is chosen: the applications
     * that answer are given as choices for the operator to pick from.
     *
     * @param text the text of the field
     * @returns {{parameter: object|null, service: object|null, choices: object[]|undefined}} the parameter, the
     * application, if any, and the applications to choose from when more than one answers
     */
    static resolve(text) {
        const value = String(text ?? "").trim();
        if (value === "") {
            return {parameter: null, service: null};
        }
        const services = PalantirAccessCheck.applications();
        const selected = PalantirAccessCheck.selected;
        const clients = services.filter(service => ["oidc", "oauth"].includes(PalantirAccessCheck.protocolOf(service))
            && String(service.clientId ?? "").toLowerCase() === value.toLowerCase());
        if (selected && PalantirAccessCheck.answers(selected, value)) {
            const protocol = PalantirAccessCheck.protocolOf(selected);
            if (protocol === "cas") {
                return {parameter: {name: "service", value}, service: PalantirAccessCheck.matching(value)};
            }
            if ((protocol === "oidc" || protocol === "oauth") && clients.length > 0) {
                return {parameter: {name: "client_id", value}, service: clients[0]};
            }
        }
        if (clients.length > 0 && !(selected && PalantirAccessCheck.protocolOf(selected) === "saml" && PalantirAccessCheck.answers(selected, value))) {
            return {parameter: {name: "client_id", value}, service: clients[0]};
        }
        const providers = services.filter(service => PalantirAccessCheck.protocolOf(service) === "saml");
        const provider = PalantirAccessCheck.protocolOf(selected) === "saml" && PalantirAccessCheck.answers(selected, value)
            ? providers.find(service => PalantirAccessCheck.answers(service, value))
            : null;
        if (provider) {
            return {parameter: {name: "entityId", value}, service: provider};
        }
        const application = PalantirAccessCheck.matching(value);
        if (!selected) {
            const answering = providers.filter(service => PalantirAccessCheck.answers(service, value));
            const named = answering.find(service => String(service.serviceId ?? "") === value)
                ?? answering.find(service => PalantirAccessCheck.literal(service.serviceId));
            if (named && application) {
                return {parameter: {name: "service", value}, service: null, choices: [named, application]};
            }
            if (named || (answering.length > 0 && !application)) {
                return {parameter: {name: "entityId", value}, service: named ?? answering[0]};
            }
        }
        return {parameter: {name: "service", value}, service: application};
    }

    /**
     * What to put in the application field for a registered application: its client id, its entity id or service URL
     * when it names one, or else the start of a URL its pattern matches, for the operator to complete.
     *
     * @param service the registered application
     * @returns {string} the value
     */
    static exactValueOf(service) {
        const protocol = PalantirAccessCheck.protocolOf(service);
        return ((protocol === "oidc" || protocol === "oauth") && Boolean(service.clientId)) || PalantirAccessCheck.literal(service.serviceId);
    }

    static valueOf(service) {
        const protocol = PalantirAccessCheck.protocolOf(service);
        if ((protocol === "oidc" || protocol === "oauth") && service.clientId) {
            return String(service.clientId);
        }
        return PalantirAccessCheck.literal(service.serviceId)
            ? String(service.serviceId).replace(/\\\./g, ".")
            : PalantirAccessCheck.urlStart(service.serviceId)
                || (/^\^?https?/i.test(String(service.serviceId ?? "")) ? "https://" : "");
    }

    /**
     * Show, under the application field, the registered application the field resolves to, or what is still missing.
     */
    static update() {
        const escape = escapePalantirMarkup;
        const text = String($("#accessApplication").val() ?? "");
        let selected = PalantirAccessCheck.selected;
        const start = selected ? PalantirAccessCheck.valueOf(selected) : "";
        if (selected && start !== "" && !text.trim().startsWith(start) && !PalantirAccessCheck.answers(selected, text.trim())) {
            PalantirAccessCheck.selected = selected = null;
        }
        const {parameter, service, choices} = PalantirAccessCheck.resolve(text);
        $("#accessTargetField .palantir-access__target-clear").prop("hidden", text === "");
        const hint = $("#accessTargetHint").removeClass("is-resolved is-pending is-shadowed");
        const card = (entry, note) => {
            const protocol = applicationProtocolOf(entry["@class"]);
            return `
                <span class="palantir-protocol-badge is-${escape(protocol.key)}" aria-hidden="true">${escape(PalantirAccessCheck.initials(entry.name))}</span>
                <span class="palantir-access__hint-text">
                    <strong title="${escape(entry.name ?? "")}">${escape(entry.name ?? "Application")}</strong>
                    <small>${note}</small>
                </span>`;
        };
        if (choices) {
            hint.addClass("is-pending").html(`<i class="mdi mdi-call-split" aria-hidden="true"></i>
                <span class="palantir-access__hint-text"><span>More than one application answers this. Pick the one to check:</span>
                <span class="palantir-access__hint-choices">${choices.map(entry => `
                    <button type="button" class="palantir-button" data-access-action="pick" data-service-id="${escape(entry.id)}">
                        ${escape(applicationProtocolOf(entry["@class"]).short)} · ${escape(entry.name ?? `#${entry.id}`)}</button>`).join("")}</span></span>`);
        } else if (service && selected && service.id !== selected.id && PalantirAccessCheck.answers(selected, text.trim())) {
            hint.addClass("is-resolved is-shadowed").html(card(service,
                `CAS matches this first, before ${escape(selected.name ?? "the application you picked")}`));
        } else if (selected && service?.id !== selected.id) {
            hint.addClass("is-pending").html(`<i class="mdi mdi-pencil-outline" aria-hidden="true"></i>
                <span class="palantir-access__hint-text"><span>${PalantirAccessCheck.protocolOf(selected) === "saml" ? "Enter an entity id" : "Complete a URL"}
                that <strong>${escape(selected.name ?? "the application")}</strong> answers: it matches
                <code>${escape(selected.serviceId ?? "")}</code></span></span>`);
        } else if (service) {
            const protocol = applicationProtocolOf(service["@class"]);
            const sent = {client_id: "client id", entityId: "entity id", service: "service URL"}[parameter.name];
            hint.addClass("is-resolved").html(card(service, `${escape(protocol.short)} · #${escape(service.id)} · checked by ${escape(sent)}`));
        } else if (text.trim() !== "") {
            hint.html(`<i class="mdi mdi-help-circle-outline" aria-hidden="true"></i>
                <span class="palantir-access__hint-text"><span>No registered application answers this; CAS will say so.</span></span>`);
        } else {
            hint.text("Pick a registered application, or type what it sends to CAS.");
        }
    }

    /**
     * Pick which of the applications that answer what is typed to check, keeping what is typed.
     *
     * @param service the application
     */
    static pick(service) {
        PalantirAccessCheck.selected = service;
        PalantirAccessCheck.problem(null);
        PalantirAccessCheck.update();
        $("#accessUsername").trigger("focus");
    }

    /**
     * Pick a registered application: put what identifies it in the field, ready to check or, for a pattern, to complete.
     *
     * @param service the application, or null to pick none
     * @param focus whether to move to the next field to fill in
     */
    static choose(service, focus = false) {
        PalantirAccessCheck.selected = service;
        const input = $("#accessApplication");
        input.val(service ? PalantirAccessCheck.valueOf(service) : "");
        PalantirAccessCheck.update();
        if (focus) {
            const complete = service && PalantirAccessCheck.exactValueOf(service)
                && PalantirAccessCheck.answers(service, String(input.val()).trim());
            const target = complete ? $("#accessUsername") : input;
            target.trigger("focus");
            if (!complete) {
                const length = String(input.val()).length;
                input[0].setSelectionRange(length, length);
            }
        }
    }

    /**
     * Open the access check for a registered application, as the applications' menu does.
     *
     * @param serviceId the application id
     * @returns {Promise<void>} resolved once the access check shows the application
     */
    static async openFor(serviceId) {
        PalantirPages.open(Tabs.ACCESS_STRATEGY);
        await initializeDashboardTab(Tabs.ACCESS_STRATEGY);
        if ($("#accessstrategy-tabs").tabs("instance")) {
            $("#accessstrategy-tabs").tabs("option", "active", 0);
        }
        await PalantirAccessCheck.loadApplications();
        const service = registeredServicesById.get(String(serviceId));
        if (service) {
            PalantirAccessCheck.choose(service, true);
        }
    }

    static problem(message) {
        $("#accessFormProblem").text(message ?? "").prop("hidden", !message);
    }

    /**
     * The request for the form: the application's parameter, the username and, when asked, the password.
     *
     * @returns {object|null} the request, or null when the form is not complete, with the problem shown
     */
    static request() {
        const typed = String($("#accessApplication").val() ?? "").trim();
        const username = String($("#accessUsername").val() ?? "").trim();
        const password = String($("#accessPassword").val() ?? "");
        const checkPassword = $("#accessCheckPassword").prop("checked") === true;
        if (typed === "") {
            PalantirAccessCheck.problem("Pick an application, or enter a service URL, a client id or an entity id.");
            $("#accessApplication").trigger("focus");
            return null;
        }
        const exact = PalantirAccessCheck.resolve(typed);
        if (!/^https?:\/\//i.test(typed) && exact.parameter.name === "service") {
            const named = PalantirAccessCheck.applications().find(entry => String(entry.name ?? "") === typed);
            if (named) {
                PalantirAccessCheck.choose(named);
            }
        }
        const value = String($("#accessApplication").val() ?? "").trim();
        const {parameter, service, choices} = PalantirAccessCheck.resolve(value);
        if (choices) {
            PalantirAccessCheck.problem(`Both ${choices.map(entry => entry.name ?? `#${entry.id}`).join(" and ")} answer this: pick the one to check.`);
            $("#accessTargetHint [data-access-action='pick']").first().trigger("focus");
            return null;
        }
        const selected = PalantirAccessCheck.selected;
        if (selected && !PalantirAccessCheck.answers(selected, value) && service?.id !== selected.id) {
            const wanted = PalantirAccessCheck.protocolOf(selected) === "saml" ? "an entity id" : "a URL";
            PalantirAccessCheck.problem(`Enter ${wanted} ${selected.name ?? "the application"} answers: it matches ${selected.serviceId}.`);
            $("#accessApplication").trigger("focus");
            return null;
        }
        const label = service?.name ?? parameter.value;
        if (username === "") {
            PalantirAccessCheck.problem("Enter the username to check.");
            $("#accessUsername").trigger("focus");
            return null;
        }
        if (checkPassword && password === "") {
            PalantirAccessCheck.problem("Enter the password to check, or turn off the password check.");
            $("#accessPassword").trigger("focus");
            return null;
        }
        PalantirAccessCheck.problem(null);
        const data = {username, [parameter.name]: parameter.value};
        if (checkPassword) {
            data.password = password;
        }
        return {data, label, username, parameter, applicationId: service ? String(service.id) : null};
    }

    static async check() {
        const request = PalantirAccessCheck.request();
        if (request === null || !CasActuatorEndpoints.serviceAccess()) {
            return;
        }
        hideBanner();
        const button = $("#accessStrategyButton").prop("disabled", true);
        $("#accessResult").html(`<div class="palantir-access__card palantir-access__empty"><i class="mdi mdi-loading mdi-spin" aria-hidden="true"></i>
            <p>Checking whether ${escapePalantirMarkup(request.username)} may access ${escapePalantirMarkup(request.label)}…</p></div>`);
        let answer;
        let status;
        try {
            answer = await $.ajax({
                url: CasActuatorEndpoints.serviceAccess(),
                type: "POST",
                contentType: "application/x-www-form-urlencoded",
                data: $.param(request.data),
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
            status = 200;
        } catch (xhr) {
            status = xhr?.status ?? 0;
            answer = xhr?.responseJSON ?? null;
            if (xhr?.statusText === PALANTIR_READ_ONLY) {
                button.prop("disabled", false);
                return;
            }
        }
        button.prop("disabled", false);
        PalantirAccessCheck.show(request, status, answer);
        if (PalantirAccessCheck.decided(status, answer)) {
            PalantirAccessCheck.remember(request, status, answer);
        }
    }

    /**
     * Whether CAS answered with an explanation; a request it could not take, such as one the endpoint was not
     * reachable for, has none and says nothing about access.
     *
     * @param answer what CAS answered
     * @returns {boolean} true when it is an explanation
     */
    static explained(answer) {
        return answer !== null && typeof answer === "object" && typeof answer.allowed === "boolean";
    }

    /**
     * Whether CAS made a decision: an explanation with an answer it gives at sign-in, allowed, refused or not signed in,
     * rather than a failure to check.
     *
     * @param status the status CAS answered
     * @param answer what CAS answered
     * @returns {boolean} true when it is a decision
     */
    static decided(status, answer) {
        return [200, 401, 403].includes(status) && PalantirAccessCheck.explained(answer);
    }

    static verdict(request, status, answer) {
        if (!PalantirAccessCheck.decided(status, answer)) {
            return {style: "is-unavailable", icon: "mdi-lan-disconnect", title: "Access Could Not Be Checked"};
        }
        if (answer?.allowed === true) {
            return {style: "is-allowed", icon: "mdi-shield-check-outline", title: `${request.username} May Access ${answer.registeredService?.name ?? request.label}`};
        }
        if (status === 401) {
            return {style: "is-unauthenticated", icon: "mdi-account-alert-outline", title: `${request.username} Could Not Sign In`};
        }
        if (answer && !answer.registeredService) {
            return {style: "is-denied", icon: "mdi-help-rhombus-outline", title: "No Application Matches"};
        }
        return {style: "is-denied", icon: "mdi-shield-off-outline", title: `${request.username} Is Denied`};
    }

    static gate(step) {
        const escape = escapePalantirMarkup;
        const outcome = PalantirAccessCheck.OUTCOMES[step.outcome] ?? PalantirAccessCheck.OUTCOMES.INFO;
        return `
            <li class="palantir-access__gate ${outcome.style}" title="${escape(`${step.title}: ${step.detail ?? ""}`)}">
                <i class="mdi ${outcome.icon}" aria-hidden="true"></i>
                <span class="palantir-access__gate-title">${escape(step.title)}</span>
                <span class="palantir-access__gate-outcome">${escape(outcome.label)}</span>
                ${PalantirAccessCheck.members(step)}
            </li>`;
    }

    /**
     * The members of a chained step as a row of marks, one per strategy or condition, each colored by how it turned
     * out, so the gate of a chain shows at a glance which of its members let the user through.
     *
     * @param step the step
     * @returns {string} the marks, or nothing for a step that is not made of others
     */
    static members(step) {
        const inner = Array.isArray(step.steps) ? step.steps : [];
        if (inner.length === 0) {
            return "";
        }
        const escape = escapePalantirMarkup;
        return `<span class="palantir-access__gate-members" aria-label="${escape(`${inner.length} members`)}">${inner.map((member, index) => {
            const outcome = PalantirAccessCheck.OUTCOMES[member.outcome] ?? PalantirAccessCheck.OUTCOMES.INFO;
            return `<span class="palantir-access__gate-member ${outcome.style}" title="${escape(`${index + 1}. ${member.title}: ${outcome.label}`)}"></span>`;
        }).join("")}</span>`;
    }

    static values(values, style) {
        const list = (Array.isArray(values) ? values : [values]).filter(value => value !== null && value !== undefined && value !== "");
        return list.length === 0
            ? `<span class="palantir-access__none">nothing</span>`
            : list.map(value => `<span class="palantir-access__value ${style}">${escapePalantirMarkup(String(value))}</span>`).join("");
    }

    static rule(step, rule) {
        const escape = escapePalantirMarkup;
        const rejected = step.id === "rejected";
        const good = rejected ? !rule.matched : rule.matched;
        return `
            <li class="palantir-access__rule ${good ? "is-passed" : "is-failed"}">
                <i class="mdi ${good ? "mdi-check" : "mdi-close"}" aria-hidden="true"></i>
                <code>${escape(rule.attribute)}</code>
                <span>${rejected ? "is refused when it has" : "needs"}</span>
                ${PalantirAccessCheck.values(rule.values, good ? (rejected ? "" : "is-hit") : "is-miss")}
                <span>· has</span>
                ${PalantirAccessCheck.values(rule.actual, "")}
            </li>`;
    }

    static reason(step) {
        const escape = escapePalantirMarkup;
        const outcome = PalantirAccessCheck.OUTCOMES[step.outcome] ?? PalantirAccessCheck.OUTCOMES.INFO;
        const rules = (step.rules ?? []).map(rule => PalantirAccessCheck.rule(step, rule)).join("");
        const inner = (step.steps ?? []).map(PalantirAccessCheck.reason).join("");
        return `
            <li class="palantir-access__reason ${outcome.style}">
                <i class="mdi ${outcome.icon}" aria-hidden="true"></i>
                <div>
                    <strong>${escape(step.title)}</strong>
                    <span>${escape(step.detail ?? "")}</span>
                    ${rules ? `<ul class="palantir-access__rules">${rules}</ul>` : ""}
                    ${inner ? `<ul class="palantir-access__reasons is-inner">${inner}</ul>` : ""}
                </div>
                <span class="palantir-access__chip ${outcome.style}">${escape(outcome.label)}</span>
            </li>`;
    }

    /**
     * Show the answer: the verdict and its actions, the steps as gates, the reasons behind each step, the user's
     * attributes with those the rules name marked, and the application's definition.
     *
     * @param request the request that was sent
     * @param status the status CAS answered
     * @param answer the explanation CAS answered, if any
     */
    static show(request, status, answer) {
        const escape = escapePalantirMarkup;
        const steps = Array.isArray(answer?.steps) ? answer.steps : [];
        const verdict = PalantirAccessCheck.verdict(request, status, answer);
        const service = answer?.registeredService ?? null;
        const reason = PalantirAccessCheck.explained(answer)
            ? answer.reason
            : status === 0 ? "CAS could not be reached." : `The service access endpoint answered ${status}, without a decision.`;
        $("#accessResult").html(`
            <section class="palantir-access__verdict ${verdict.style}" aria-label="Verdict">
                <span class="palantir-access__verdict-icon" aria-hidden="true"><i class="mdi ${verdict.icon}"></i></span>
                <div class="palantir-access__verdict-text">
                    <h3>${escape(verdict.title)}</h3>
                    <p>${escape(reason)}</p>
                </div>
                ${service?.id !== undefined ? `<div class="palantir-access__verdict-actions">
                    <button type="button" class="palantir-button" data-access-action="open" data-service-id="${escape(service.id)}">
                        <i class="mdi mdi-open-in-new" aria-hidden="true"></i>Open Application</button>
                    <button type="button" class="palantir-button" data-access-action="edit" data-service-id="${escape(service.id)}"
                            data-palantir-write="registeredServices">
                        <i class="mdi mdi-pencil-outline" aria-hidden="true"></i>Edit Access Strategy</button>
                </div>` : ""}
            </section>
            ${steps.length > 0 ? `
            <section class="palantir-access__card" aria-label="Steps">
                <p class="palantir-access__label">How CAS Decided</p>
                <ol class="palantir-access__flow">${steps.map(PalantirAccessCheck.gate).join("")}</ol>
            </section>
            <section class="palantir-access__card" aria-label="Why">
                <p class="palantir-access__label">Why</p>
                <ul class="palantir-access__reasons">${steps.map(PalantirAccessCheck.reason).join("")}</ul>
            </section>` : ""}`);
        applyPalantirEndpointAccess(document.getElementById("accessResult"));
        PalantirAccessCheck.showAttributes(answer, steps);
        PalantirAccessCheck.showDefinition(service);
    }

    static ruleAttributes(steps) {
        return new Set(steps.flatMap(function names(step) {
            return [...(step.rules ?? []).map(rule => rule.attribute), ...(step.steps ?? []).flatMap(names)];
        }));
    }

    static showAttributes(answer, steps) {
        const table = PalantirAccessCheck.attributesTable;
        table.clear();
        const principal = answer?.authentication?.principal;
        const attributes = {...(answer?.authentication?.attributes ?? {}), ...(principal?.attributes ?? {})};
        const named = PalantirAccessCheck.ruleAttributes(steps);
        if (!principal) {
            table.draw();
            $("#accessStrategyAttributesContainer").prop("hidden", true);
            return;
        }
        for (const [name, value] of Object.entries(attributes)) {
            table.row.add({
                0: `${palantirCode(name)}${named.has(name) ? ` <span class="palantir-access__chip is-info">In the Rules</span>` : ""}`,
                1: PalantirAccessCheck.values(value, "")
            });
        }
        table.draw();
        $("#accessStrategyAttributesTitle").text(`${principal.id}'s Attributes · ${Object.keys(attributes).length}`);
        $("#accessStrategyAttributesContainer").prop("hidden", false);
    }

    /**
     * Show the application's definition. The access check names the application without its definition, so the
     * definition comes from the registered applications, as the registered services endpoint gives them.
     *
     * @param service the application the check names
     * @returns {Promise<void>} resolved once the definition is shown or hidden
     */
    static async showDefinition(service) {
        const container = $("#accessStrategyEditorContainer");
        const hide = () => {
            container.prop("hidden", true);
            PalantirAccessCheck.editor.setValue("");
        };
        const shown = ++PalantirAccessCheck.definitionRequest;
        if (service?.id === undefined) {
            hide();
            return;
        }
        let definition = registeredServicesById.get(String(service.id));
        if (!definition && CasActuatorEndpoints.registeredServices()) {
            try {
                definition = await $.get(`${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(service.id)}`);
            } catch (error) {
                definition = null;
            }
        }
        if (shown !== PalantirAccessCheck.definitionRequest) {
            return;
        }
        if (!definition) {
            hide();
            return;
        }
        PalantirAccessCheck.editor.setValue(JSON.stringify(definition, null, 2));
        PalantirAccessCheck.editor.gotoLine(1);
        container.prop("hidden", false);
    }

    static recent() {
        try {
            const stored = JSON.parse(localStorage.getItem(PalantirAccessCheck.STORAGE_KEY) ?? "[]");
            return Array.isArray(stored) ? stored : [];
        } catch (error) {
            return [];
        }
    }

    static remember(request, status, answer) {
        const outcome = answer?.allowed === true ? "allowed" : status === 401 ? "unauthenticated" : answer && !answer.registeredService ? "unmatched" : "denied";
        const entry = {label: answer?.registeredService?.name ?? request.parameter?.value ?? request.label, username: request.username,
            password: request.data.password !== undefined,
            parameter: request.parameter, applicationId: answer?.registeredService?.id !== undefined ? String(answer.registeredService.id) : request.applicationId,
            outcome, at: Date.now()};
        const entries = [entry, ...PalantirAccessCheck.recent().filter(previous =>
            !(previous.username === entry.username && previous.parameter?.value === entry.parameter?.value))]
            .slice(0, PalantirAccessCheck.MAX_RECENT);
        try {
            localStorage.setItem(PalantirAccessCheck.STORAGE_KEY, JSON.stringify(entries));
        } catch (error) {
            console.debug("Palantir could not remember the access check", error);
        }
        PalantirAccessCheck.showRecent();
    }

    static showRecent() {
        const escape = escapePalantirMarkup;
        const entries = PalantirAccessCheck.recent();
        const labels = {allowed: ["is-passed", "Allowed"], denied: ["is-failed", "Denied"],
            unauthenticated: ["is-warn", "Not Signed In"], unmatched: ["is-warn", "No Match"]};
        $("#accessRecentList").html(entries.map((entry, index) => {
            const [style, text] = labels[entry.outcome] ?? labels.denied;
            return `
                <li><button type="button" class="palantir-access__recent-item" data-access-recent="${index}"
                            title="Check ${escape(entry.username)} against ${escape(entry.parameter?.value ?? entry.label)} again">
                    <span><strong>${escape(entry.username)}</strong> · ${escape(entry.label)}</span>
                    <span class="palantir-access__chip ${style}">${escape(text)}</span>
                </button></li>`;
        }).join(""));
        $("#accessRecentChecks").prop("hidden", entries.length === 0);
    }

    /**
     * Fill the form from a recent check and run it again the way it ran: a check that also checked the password turns
     * the password check on and asks for the password, and one that did not turns it off.
     *
     * @param entry the recent check
     */
    static repeat(entry) {
        PalantirAccessCheck.selected = entry.applicationId ? registeredServicesById.get(String(entry.applicationId)) ?? null : null;
        $("#accessApplication").val(entry.parameter?.value ?? entry.label);
        PalantirAccessCheck.update();
        $("#accessUsername").val(entry.username);
        const withPassword = entry.password === true;
        $("#accessCheckPassword").prop("checked", withPassword).trigger("change");
        if (withPassword) {
            PalantirAccessCheck.problem(`Enter ${entry.username}'s password to check it again: passwords are never kept.`);
            $("#accessPassword").val("").trigger("focus");
            return;
        }
        PalantirAccessCheck.check();
    }

    static async editAccessStrategy(serviceId) {
        try {
            const definition = await $.get(`${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(serviceId)}`);
            const opening = PalantirServiceEditor.openApplicationInWizard(definition);
            for (let attempt = 0; attempt < 50 && (typeof isPopulatingWizard === "undefined" || isPopulatingWizard
                || !$("#editServiceWizardDialog").dialog("isOpen")); attempt++) {
                await new Promise(resolve => setTimeout(resolve, 100));
            }
            RegisteredServiceSections.activate(PalantirAccessCheck.WIZARD_SECTION);
            await opening;
        } catch (xhr) {
            displayBanner(xhr);
        }
    }

    /**
     * Read the registered applications to suggest, unless the applications page has read them already. When the
     * applications page reads them meanwhile, its list is the one kept: this answer is dropped rather than mixed into
     * or laid over it.
     *
     * @returns {Promise<void>} resolved once they are read or could not be
     */
    static async loadApplications() {
        if (registeredServicesById.size > 0 || !CasActuatorEndpoints.registeredServices()) {
            return;
        }
        const latest = () => typeof palantirServicesRequest === "undefined" ? 0 : palantirServicesRequest;
        const sequence = latest();
        try {
            const response = await $.ajax({url: CasActuatorEndpoints.registeredServices(), dataType: "json", headers: {"Accept": "application/json"}});
            if (sequence !== latest() || registeredServicesById.size > 0 || !Array.isArray(response?.[1])) {
                return;
            }
            response[1].forEach(service => registeredServicesById.set(String(service.id), service));
            registeredServicesVersion++;
            if (typeof palantirListedApplications === "function") {
                palantirListedApplications();
            }
        } catch (error) {
            console.debug("Palantir could not read the registered applications for the access check", error);
        }
    }

    /**
     * The registered applications to suggest for what is typed: those whose name, ids or description hold it, and, for
     * a URL, those whose service id matches it.
     *
     * @param term what is typed
     * @returns {object[]} the applications
     */
    static suggestions(term) {
        const text = String(term ?? "").trim();
        const url = /^https?:\/\//i.test(text);
        return PalantirAccessCheck.applications().filter(service => PalantirApplicationSuggestions.matches(service, text)
            || (url && PalantirAccessCheck.answers(service, text)));
    }

    static highlight(text, term) {
        const escape = escapePalantirMarkup;
        const value = String(text ?? "");
        const index = term ? value.toLowerCase().indexOf(term.toLowerCase()) : -1;
        return index < 0
            ? escape(value)
            : `${escape(value.slice(0, index))}<mark>${escape(value.slice(index, index + term.length))}</mark>${escape(value.slice(index + term.length))}`;
    }

    static bind() {
        PalantirApplicationSuggestions.attach($("#accessApplication"), {
            field: "#accessTargetField",
            source: term => {
                const {parameter, service, choices} = PalantirAccessCheck.resolve(term);
                if (PalantirAccessCheck.selected || choices || (service && (parameter.name !== "service" || /^https?:\/\//i.test(term)))) {
                    return null;
                }
                return PalantirAccessCheck.suggestions(term);
            },
            select: service => PalantirAccessCheck.choose(service, true)
        });
        $("#accessApplication").on("focus", async function () {
            await PalantirAccessCheck.loadApplications();
            if (!$(this).val() && !PalantirAccessCheck.selected) {
                $(this).autocomplete("search", "");
            }
        }).on("input", () => PalantirAccessCheck.update());

        $("#accessCheckPassword").off("change").on("change", function () {
            $("#accessPasswordField").prop("hidden", !this.checked);
            if (!this.checked) {
                $("#accessPassword").val("");
            }
        });
        $("#fmAccessStrategy").off("submit").on("submit", event => {
            event.preventDefault();
            PalantirAccessCheck.check();
        });
        $("#accessstrategy-tab").off("click.access")
            .on("click.access", "[data-access-action]", event => {
                const button = event.currentTarget;
                const action = button.dataset.accessAction;
                if (action === "clear") {
                    PalantirAccessCheck.choose(null);
                    $("#accessApplication").trigger("focus");
                } else if (action === "pick") {
                    const service = registeredServicesById.get(String(button.dataset.serviceId));
                    if (service) {
                        PalantirAccessCheck.pick(service);
                    }
                } else if (action === "open") {
                    navigateToApplication(button.dataset.serviceId);
                } else if (action === "edit") {
                    PalantirAccessCheck.editAccessStrategy(button.dataset.serviceId);
                }
            })
            .on("click.access", "[data-access-recent]", event => {
                const entry = PalantirAccessCheck.recent()[Number(event.currentTarget.dataset.accessRecent)];
                if (entry) {
                    PalantirAccessCheck.repeat(entry);
                }
            });
        $("#accessStrategyEditorContainer").off("toggle").on("toggle", () => PalantirAccessCheck.editor?.resize());
    }
}

/**
 * Suggestions of registered applications for a text field, as the access check and the attribute release preview of the
 * Users page offer them: a count of the applications that match, then each with its protocol badge, its name and
 * identifier with what is typed marked, and its protocol and id. An application matches by its name, service id,
 * client id, id or description; its text is put together once rather than with every key typed.
 */
class PalantirApplicationSuggestions {
    static LIMIT = 50;

    static searchText = new WeakMap();

    /**
     * Whether an application matches what is typed.
     *
     * @param service the application
     * @param term what is typed
     * @returns {boolean} true when nothing is typed or the application's text holds it
     */
    static matches(service, term) {
        const wanted = String(term ?? "").trim().toLowerCase();
        if (wanted === "") {
            return true;
        }
        if (!PalantirApplicationSuggestions.searchText.has(service)) {
            PalantirApplicationSuggestions.searchText.set(service,
                [service.name, service.serviceId, service.clientId, service.id, service.description]
                    .map(value => String(value ?? "").toLowerCase()).join("\n"));
        }
        return PalantirApplicationSuggestions.searchText.get(service).includes(wanted);
    }

    /**
     * Offer applications as suggestions on a field.
     *
     * @param input the text field
     * @param options the field the menu lines up with, the source of the applications for what is typed (null to
     *        suggest nothing) and what to do with the application picked
     */
    static attach(input, {field, source, select}) {
        const state = {total: 0, term: ""};
        input.autocomplete({
            minLength: 0,
            delay: 0,
            position: {my: "left top+6", at: "left bottom", of: field, collision: "flipfit"},
            source: (request, response) => {
                const term = String(request.term ?? "").trim();
                const found = source(term);
                if (found === null) {
                    response([]);
                    return;
                }
                state.total = found.length;
                state.term = term;
                response(found.slice(0, PalantirApplicationSuggestions.LIMIT).map(service => ({
                    label: service.name ?? PalantirAccessCheck.identifierOf(service),
                    value: service.name ?? PalantirAccessCheck.identifierOf(service),
                    service
                })));
            },
            open: () => input.autocomplete("widget").outerWidth($(field).outerWidth()),
            select: (event, ui) => {
                event.preventDefault();
                select(ui.item.service);
            },
            focus: event => event.preventDefault()
        });
        const widget = input.autocomplete("instance");
        widget._renderMenu = function (list, items) {
            const total = state.total ?? items.length;
            $("<li>").addClass("palantir-access__suggestions-header ui-state-disabled")
                .text(total > items.length ? `${items.length} of ${total} Applications` : `${total} ${total === 1 ? "Application" : "Applications"}`)
                .appendTo(list);
            items.forEach(item => this._renderItemData(list, item));
        };
        widget._renderItem = (list, item) => {
            const escape = escapePalantirMarkup;
            const protocol = applicationProtocolOf(item.service["@class"]);
            return $("<li>").append(`
                <div class="palantir-access__suggestion">
                    <span class="palantir-protocol-badge is-${escape(protocol.key)}" aria-hidden="true">${escape(PalantirAccessCheck.initials(item.service.name))}</span>
                    <span class="palantir-access__suggestion-text">
                        <strong>${PalantirAccessCheck.highlight(item.label, state.term)}</strong>
                        <code>${PalantirAccessCheck.highlight(PalantirAccessCheck.identifierOf(item.service), state.term)}</code>
                    </span>
                    <span class="palantir-access__suggestion-meta">${escape(protocol.short)}<small>#${escape(item.service.id)}</small></span>
                </div>`).appendTo(list);
        };
        widget.widget().addClass("palantir-access__suggestions");
    }
}

async function initializeAccessStrategyOperations() {
    PalantirAccessCheck.editor = initializeAceEditor("accessStrategyEditor");
    PalantirAccessCheck.editor.setReadOnly(true);
    PalantirAccessCheck.attributesTable = $("#accessStrategyAttributesTable").DataTable({
        pageLength: 10,
        drawCallback: () => {
            $("#accessStrategyAttributesTable tr").addClass("mdc-data-table__row");
            $("#accessStrategyAttributesTable td").addClass("mdc-data-table__cell");
        }
    });
    PalantirAccessCheck.bind();
    PalantirAccessCheck.showRecent();
    await PalantirAccessCheck.loadApplications();
}
