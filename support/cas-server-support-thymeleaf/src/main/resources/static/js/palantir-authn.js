function generateAuthnHandlerConfiguration() {
    const editor = ace.edit("authnHandlerEditor");
    if (!editor) {
        return;
    }
    const currentType = $("#authenticationHandlerType").val();
    if (currentType !== "LDAP" && currentType !== "JDBC") {
        return;
    }

    let prefix;
    const lines = [];

    if (currentType === "LDAP") {
        const subtype = $("#ldapAuthenticationHandlerType").val();
        prefix = $("#ldapAuthenticationHandlerType option:selected").data("prefix");
        lines.push(`${prefix}.type=${subtype}`);
    } else {
        prefix = $("#jdbcAuthenticationHandlerType option:selected").data("prefix");
    }

    $("#addAuthenticationHandlerDialog input[data-param-name]").each(function () {
        const $el = $(this);
        const $container = $el.closest("[id$='FieldContainer'], [id$='SwitchButtonPanel']");
        if ($container.length > 0 && !$container.is(":visible")) {
            return;
        }
        if ($el.attr("type") === "hidden" && $el.attr("data-switch-btn")) {
            return;
        }
        const paramName = $el.attr("data-param-name");
        if (!paramName) {
            return;
        }
        const val = $el.val();
        const defaultVal = $el.data("default-value");
        if (val !== undefined && val !== null && val.toString().trim().length > 0
            && val.toString() !== (defaultVal !== undefined ? defaultVal.toString() : "")) {
            lines.push(`${prefix}.${paramName}=${val}`);
        }
    });

    $("#addAuthenticationHandlerDialog input[data-switch-btn]").each(function () {
        const $el = $(this);
        const $panel = $el.closest("[id$='SwitchButtonPanel']");
        if ($panel.length > 0 && !$panel.is(":visible")) {
            return;
        }
        const paramName = $el.attr("data-param-name");
        if (paramName) {
            const val = $el.val();
            const defaultVal = $el.data("default-value");
            if (val !== undefined && val.toString() !== (defaultVal !== undefined ? defaultVal.toString() : "")) {
                lines.push(`${prefix}.${paramName}=${val}`);
            }
        }
    });

    $("#addAuthenticationHandlerDialog select.jqueryui-selectmenu").each(function () {
        const $el = $(this);
        const elId = $el.attr("id");
        if (elId === "authenticationHandlerType" || elId === "ldapAuthenticationHandlerType" || elId === "jdbcAuthenticationHandlerType") {
            return;
        }
        const $container = $el.closest("[id$='SelectContainer']");
        if ($container.length > 0 && !$container.is(":visible")) {
            return;
        }
        const paramName = $el.attr("data-param-name");
        if (!paramName) {
            return;
        }
        const val = $el.val();
        const defaultVal = $el.data("default-value");
        if (val !== undefined && val !== null && val.toString().trim().length > 0
            && val.toString() !== (defaultVal !== undefined ? defaultVal.toString() : "")) {
            lines.push(`${prefix}.${paramName}=${val}`);
        }
    });

    $("#addAuthenticationHandlerDialog select.jqueryui-multiselectmenu").each(function () {
        const $el = $(this);
        const $container = $el.closest("[id$='SelectContainer']");
        if ($container.length > 0 && !$container.is(":visible")) {
            return;
        }
        const paramName = $el.attr("data-param-name");
        if (!paramName) {
            return;
        }
        const tomSelectInstance = $el[0].tomselect;
        if (tomSelectInstance) {
            const values = tomSelectInstance.getValue();
            if (values && values.length > 0) {
                const valStr = Array.isArray(values) ? values.join(",") : values;
                lines.push(`${prefix}.${paramName}=${valStr}`);
            }
        }
    });

    editor.setValue(lines.join("\n"), -1);
}

function toggleAuthnHandlerEditorVisibility() {
    const showEditor = $("#showAuthnHandlerEditor").val();
    const $editorContainer = $("#authnHandlerEditorContainer");
    const $controlsPanel = $("#authnHandlerControlsPanel");

    localStorage.setItem("showAuthnHandlerEditorPreference", showEditor);

    if (showEditor === "true" || showEditor === true) {
        showElements($editorContainer);
        $controlsPanel.removeClass("w-100").addClass("mmw-45");
        const editor = ace.edit("authnHandlerEditor");
        if (editor) {
            editor.resize();
        }
    } else {
        hideElements($editorContainer);
        $controlsPanel.removeClass("mmw-45").addClass("w-100");
    }
}

function toggleAuthnHandlerAdvancedOptions() {
    const showAdvanced = $("#showAuthnHandlerAdvancedOptions").val();
    localStorage.setItem("showAuthnHandlerAdvancedOptionsPreference", showAdvanced);

    const currentType = $("#authenticationHandlerType").val();
    if (!currentType) {
        return;
    }

    let currentSubtype = null;
    if (currentType === "LDAP") {
        currentSubtype = $("#ldapAuthenticationHandlerType").val();
    } else if (currentType === "JDBC") {
        currentSubtype = $("#jdbcAuthenticationHandlerType").val();
    }
    const combo = currentSubtype ? `${currentType}-${currentSubtype}` : null;

    const advancedElements = $("#addAuthenticationHandlerDialog .advanced-option").filter(function () {
        const classes = $(this).attr("class").split(/\s+/);
        const belongsToType = classes.includes(currentType);
        const belongsToCombo = combo && classes.includes(combo);
        const isSubtypeSpecific = classes.some(cls => cls.startsWith(`${currentType}-`));
        return belongsToType || belongsToCombo || (isSubtypeSpecific && !belongsToType);
    });

    if (showAdvanced === "true" || showAdvanced === true) {
        advancedElements.each(function () {
            const classes = $(this).attr("class").split(/\s+/);
            const isSubtypeSpecific = classes.some(cls => cls.startsWith(`${currentType}-`));
            if (!isSubtypeSpecific || (combo && classes.includes(combo))) {
                showElements($(this));
            }
        });
    } else {
        hideElements(advancedElements);
    }
}

function openNewAuthenticationHandlerDialog() {
    const dialogContainer = $("<div>", {
        id: "addAuthenticationHandlerDialog"
    });

    const flexContainer = $("<div>", {
        id: "authnHandlerDialogContainer",
        class: "d-flex"
    });

    const controlsPanel = $("<div>", {
        id: "authnHandlerControlsPanel",
        class: "mr-2 mmw-45"
    });

    const editorContainer = $("<div>", {
        id: "authnHandlerEditorContainer",
        class: "flex-grow-1"
    });

    editorContainer.append(`<pre class="ace-editor ace-relative w-100 h-100 mb-0 mt-0 ace-absolute" id="authnHandlerEditor"></pre>`);

    flexContainer.append(controlsPanel, editorContainer);
    dialogContainer.append(flexContainer);

    const availableHandlers = [];
    if (CAS_FEATURES.includes("LDAP.authentication")) {
        availableHandlers.push({value: "LDAP", text: "LDAP & ACTIVE DIRECTORY"});
    }
    if (CAS_FEATURES.includes("Authentication.jdbc")) {
        availableHandlers.push({value: "JDBC", text: "RELATIONAL (SQL) DATABASE"});
    }

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Authentication Source:",
        id: "authenticationHandlerType",
        options: availableHandlers,
        cssClasses: "always-show",
        labelCssClasses: "display-flex"
    });

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Authentication Type:",
        id: "ldapAuthenticationHandlerType",
        options: [
            {value: "AUTHENTICATED", text: "AUTHENTICATED", data: {prefix: "cas.authn.ldap[]"}},
            {value: "AD", text: "ACTIVE DIRECTORY", data: {prefix: "cas.authn.ldap[]"}},
            {value: "DIRECT", text: "DIRECT", data: {prefix: "cas.authn.ldap[]"}}
        ],
        cssClasses: "LDAP",
        labelCssClasses: "display-flex"
    });

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Authentication Type:",
        id: "jdbcAuthenticationHandlerType",
        options: [
            {value: "BIND", text: "BIND", data: {prefix: "cas.authn.jdbc.bind[]"}},
            {value: "QUERY", text: "QUERY", data: {prefix: "cas.authn.jdbc.query[]"}},
            {value: "QUERY_ENCODE", text: "QUERY & ENCODE", data: {prefix: "cas.authn.jdbc.encode[]"}},
            {value: "SEARCH", text: "SEARCH", data: {prefix: "cas.authn.jdbc.search[]"}},
            {value: "STORED_PROCEDURE", text: "STORED PROCEDURE", data: {prefix: "cas.authn.jdbc.procedure[]"}}
        ],
        cssClasses: "JDBC",
        labelCssClasses: "display-flex"
    });

    createInputField({
        labelTitle: "Name",
        name: "authnHandlerName",
        required: true,
        containerId: controlsPanel,
        title: "Define a unique name for this authentication handler.",
        cssClasses: "LDAP JDBC hide",
        paramName: "name"
    }).val(randomWord().replace(/_/g, "-"));

    createInputField({
        labelTitle: "LDAP URL",                                        
        name: "authnHandlerLdapUrl",
        required: true,
        containerId: controlsPanel,
        title: "Define the LDAP URL to connect to.",
        cssClasses: "LDAP hide",
        paramName: "ldap-url"
    });
    createInputField({
        labelTitle: "Base DN",
        name: "authnHandlerBaseDn",
        required: true,
        containerId: controlsPanel,
        title: "Define the base DN to use for LDAP searches.",
        cssClasses: "LDAP-AD LDAP-AUTHENTICATED hide",
        paramName: "base-dn"
    });
    createInputField({
        labelTitle: "Bind DN",
        name: "authnHandlerBindDn",
        required: true,
        containerId: controlsPanel,
        title: "Define the bind DN used to connect to LDAP.",
        cssClasses: "LDAP-AUTHENTICATED hide",
        paramName: "bind-dn"
    });
    createInputField({
        labelTitle: "Bind Credential",
        name: "authnHandlerBindCredential",
        required: true,
        dataType: "password",
        containerId: controlsPanel,
        title: "Define the bind credential/password used to connect to LDAP.",
        cssClasses: "LDAP-AUTHENTICATED hide",
        paramName: "bind-credential"
    });
    createInputField({
        labelTitle: "Search Filter",
        name: "authnHandlerSearchFilter",
        required: true,
        containerId: controlsPanel,
        title: "Define the search filter for LDAP authentication.",
        cssClasses: "LDAP-AD LDAP-AUTHENTICATED hide",
        paramName: "search-filter"
    });
    createInputField({
        labelTitle: "DN Format",
        name: "authnHandlerDnFormat",
        required: true,
        containerId: controlsPanel,
        title: "Define the DN format for LDAP authentication.",
        cssClasses: "LDAP-AD LDAP-DIRECT hide",
        paramName: "dn-format"
    });
    createInputField({
        labelTitle: "Credential Criteria",
        name: "authnHandlerCredentialCriteria",
        required: false,
        containerId: controlsPanel,
        title: "Define the credential criteria pattern for authentication.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "credential-criteria"
    });
    createInputField({
        labelTitle: "Principal Attribute ID",
        name: "authnHandlerPrincipalAttributeId",
        required: false,
        containerId: controlsPanel,
        title: "Define the attribute to use as the principal identifier.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "principal-attribute-id"
    });
    createInputField({
        labelTitle: "Principal DN Attribute Name",
        name: "authnHandlerPrincipalDnAttributeName",
        required: false,
        containerId: controlsPanel,
        title: "Define the LDAP DN attribute name to use for the principal.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "principal-dn-attribute-name"
    }).val("principalLdapDn");

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Deref Aliases:",
        id: "authnHandlerDerefAliases",
        paramName: "deref-aliases",
        options: [
            {value: "NEVER", text: "NEVER"},
            {value: "SEARCHING", text: "SEARCHING"},
            {value: "FINDING", text: "FINDING"},
            {value: "ALWAYS", text: "ALWAYS"}
        ],
        cssClasses: "LDAP advanced-option",
        labelCssClasses: "display-flex"
    });

    createInputField({
        labelTitle: "Connection Timeout",
        name: "authnHandlerLdapConnectionTimeout",
        required: false,
        containerId: controlsPanel,
        title: "Define the LDAP connection timeout.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "connection-timeout"
    }).val("PT5S");
    createInputField({
        labelTitle: "Response Timeout",
        name: "authnHandlerLdapResponseTimeout",
        required: false,
        containerId: controlsPanel,
        title: "Define the LDAP response timeout.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "response-timeout"
    }).val("PT5S");
    createInputField({
        labelTitle: "Idle Time",
        name: "authnHandlerLdapIdleTime",
        required: false,
        containerId: controlsPanel,
        title: "Define the LDAP connection idle time.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "idle-time"
    }).val("PT10M");
    createInputField({
        labelTitle: "Min Pool Size",
        name: "authnHandlerLdapMinPoolSize",
        required: false,
        dataType: "number",
        containerId: controlsPanel,
        title: "Define the minimum LDAP connection pool size.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "min-pool-size"
    }).val("3");
    createInputField({
        labelTitle: "Max Pool Size",
        name: "authnHandlerLdapMaxPoolSize",
        required: false,
        dataType: "number",
        containerId: controlsPanel,
        title: "Define the maximum LDAP connection pool size.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "max-pool-size"
    }).val("10");

    createInputField({
        labelTitle: "Validate Timeout",
        name: "authnHandlerLdapValidateTimeout",
        required: false,
        containerId: controlsPanel,
        title: "Define the timeout for connection validation.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "validate-timeout"
    }).val("PT5S");
    createInputField({
        labelTitle: "Validate Period",
        name: "authnHandlerLdapValidatePeriod",
        required: false,
        containerId: controlsPanel,
        title: "Define the period for periodic connection validation.",
        cssClasses: "LDAP hide advanced-option",
        paramName: "validate-period"
    }).val("PT5M");
    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Pool Passivator:",
        id: "authnHandlerLdapPoolPassivator",
        paramName: "pool-passivator",
        options: [
            {value: "BIND", text: "BIND"},
            {value: "NONE", text: "NONE"}
        ],
        cssClasses: "LDAP advanced-option",
        labelCssClasses: "display-flex"
    });
    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Hostname Verifier:",
        id: "authnHandlerLdapHostnameVerifier",
        paramName: "hostname-verifier",
        options: [
            {value: "DEFAULT", text: "DEFAULT"},
            {value: "ANY", text: "ANY"}
        ],
        cssClasses: "LDAP advanced-option",
        labelCssClasses: "display-flex"
    });

    createInputField({
        labelTitle: "URL",
        name: "authnHandlerJdbcUrl",
        required: true,
        containerId: controlsPanel,
        title: "Define the JDBC connection URL.",
        cssClasses: "JDBC hide",
        paramName: "url"
    });
    createSelectField({
        containerId: controlsPanel,
        labelTitle: "State:",
        id: "authnHandlerJdbcState",
        options: [
            {value: "ACTIVE", text: "ACTIVE"},
            {value: "STANDBY", text: "STANDBY"}
        ],
        cssClasses: "JDBC advanced-option",
        labelCssClasses: "display-flex"
    });
    createInputField({
        labelTitle: "Default Catalog",
        name: "authnHandlerJdbcDefaultCatalog",
        required: false,
        containerId: controlsPanel,
        title: "Define the default catalog for the JDBC connection.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "default-catalog"
    });
    createInputField({
        labelTitle: "Default Schema",
        name: "authnHandlerJdbcDefaultSchema",
        required: false,
        containerId: controlsPanel,
        title: "Define the default schema for the JDBC connection.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "default-schema"
    });
    createInputField({
        labelTitle: "Health Query",
        name: "authnHandlerJdbcHealthQuery",
        required: false,
        containerId: controlsPanel,
        title: "Define the SQL query used to validate the health of the connection.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "health-query"
    });
    createInputField({
        labelTitle: "Connection Timeout",
        name: "authnHandlerJdbcConnectionTimeout",
        required: false,
        containerId: controlsPanel,
        title: "Define the connection timeout in milliseconds.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "connection-timeout"
    });
    createInputField({
        labelTitle: "Leak Threshold",
        name: "authnHandlerJdbcLeakThreshold",
        required: false,
        containerId: controlsPanel,
        title: "Define the connection leak detection threshold in milliseconds.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "leak-threshold"
    }).val("PT6S");
    createInputField({
        labelTitle: "SQL",
        name: "authnHandlerJdbcSql",
        required: true,
        containerId: controlsPanel,
        title: "Define the SQL query for authentication.",
        cssClasses: "JDBC-QUERY JDBC-QUERY_ENCODE hide",
        paramName: "sql"
    });
    createInputField({
        labelTitle: "User",
        name: "authnHandlerJdbcUser",
        required: true,
        containerId: controlsPanel,
        title: "Define the database user for the JDBC connection.",
        cssClasses: "JDBC-BIND JDBC-QUERY JDBC-QUERY_ENCODE JDBC-STORED_PROCEDURE hide",
        paramName: "user"
    });
    createInputField({
        labelTitle: "Password",
        name: "authnHandlerJdbcPassword",
        required: true,
        dataType: "password",
        containerId: controlsPanel,
        title: "Define the database password for the JDBC connection.",
        cssClasses: "JDBC-BIND JDBC-QUERY JDBC-QUERY_ENCODE JDBC-STORED_PROCEDURE hide",
        paramName: "password"
    });
    createInputField({
        labelTitle: "Field Password",
        name: "authnHandlerJdbcFieldPassword",
        required: true,
        containerId: controlsPanel,
        title: "Define the database column name that holds the password.",
        cssClasses: "JDBC-QUERY JDBC-SEARCH hide",
        paramName: "field-password"
    });
    createInputField({
        labelTitle: "Field User",
        name: "authnHandlerJdbcFieldUser",
        required: true,
        containerId: controlsPanel,
        title: "Define the database column name that holds the username.",
        cssClasses: "JDBC-SEARCH hide",
        paramName: "field-user"
    });
    createInputField({
        labelTitle: "Table Users",
        name: "authnHandlerJdbcTableUsers",
        required: true,
        containerId: controlsPanel,
        title: "Define the database table name that holds user accounts.",
        cssClasses: "JDBC-SEARCH hide",
        paramName: "table-users"
    });
    createInputField({
        labelTitle: "Password Field Name",
        name: "authnHandlerJdbcPasswordFieldName",
        required: true,
        containerId: controlsPanel,
        title: "Define the column name that stores the encoded password.",
        cssClasses: "JDBC-QUERY_ENCODE hide",
        paramName: "password-field-name"
    });
    createInputField({
        labelTitle: "Algorithm Name",
        name: "authnHandlerJdbcAlgorithmName",
        required: false,
        containerId: controlsPanel,
        title: "Define the encoding algorithm name (e.g. MD5, SHA-256).",
        cssClasses: "JDBC-QUERY_ENCODE hide advanced-option",
        paramName: "algorithm-name"
    });
    createInputField({
        labelTitle: "Number of Iterations",
        name: "authnHandlerJdbcNumIterations",
        required: false,
        dataType: "number",
        containerId: controlsPanel,
        title: "Define the number of hashing iterations.",
        cssClasses: "JDBC-QUERY_ENCODE hide advanced-option",
        paramName: "number-of-iterations"
    });
    createInputField({
        labelTitle: "Static Salt",
        name: "authnHandlerJdbcStaticSalt",
        required: false,
        containerId: controlsPanel,
        title: "Define the static salt value used for encoding.",
        cssClasses: "JDBC-QUERY_ENCODE hide advanced-option",
        paramName: "static-salt"
    });
    createInputField({
        labelTitle: "Expired Field Name",
        name: "authnHandlerJdbcExpiredFieldName",
        required: false,
        containerId: controlsPanel,
        title: "Define the column name that indicates an expired account.",
        cssClasses: "JDBC-QUERY_ENCODE hide advanced-option",
        paramName: "expired-field-name"
    });
    createInputField({
        labelTitle: "Disabled Field Name",
        name: "authnHandlerJdbcDisabledFieldName",
        required: false,
        containerId: controlsPanel,
        title: "Define the column name that indicates a disabled account.",
        cssClasses: "JDBC-QUERY_ENCODE hide advanced-option",
        paramName: "disabled-field-name"
    });
    createInputField({
        labelTitle: "Procedure Name",
        name: "authnHandlerJdbcProcedureName",
        required: true,
        containerId: controlsPanel,
        title: "Define the stored procedure name for authentication.",
        cssClasses: "JDBC-STORED_PROCEDURE hide",
        paramName: "procedure-name"
    });
    createInputField({
        labelTitle: "Credential Criteria",
        name: "authnHandlerJdbcCredentialCriteria",
        required: false,
        containerId: controlsPanel,
        title: "Define the credential criteria pattern for authentication.",
        cssClasses: "JDBC hide advanced-option",
        paramName: "credential-criteria"
    });


    const savedEditorPref = localStorage.getItem("showAuthnHandlerEditorPreference");
    if (savedEditorPref !== null) {
        const $editorInput = $("#addAuthenticationHandlerDialogFields #showAuthnHandlerEditor");
        $editorInput.val(savedEditorPref);
        const $editorBtn = $("#addAuthenticationHandlerDialogFields #showAuthnHandlerEditorButton");
        if (savedEditorPref === "true") {
            $editorBtn.removeClass("mdc-switch--unselected").addClass("mdc-switch--selected");
            $editorBtn.attr("aria-checked", "true");
        } else {
            $editorBtn.removeClass("mdc-switch--selected").addClass("mdc-switch--unselected");
            $editorBtn.attr("aria-checked", "false");
        }
    }

    const savedAdvancedPref = localStorage.getItem("showAuthnHandlerAdvancedOptionsPreference");
    if (savedAdvancedPref !== null) {
        const $advancedInput = $("#addAuthenticationHandlerDialogFields #showAuthnHandlerAdvancedOptions");
        $advancedInput.val(savedAdvancedPref);
        const $advancedBtn = $("#addAuthenticationHandlerDialogFields #showAuthnHandlerAdvancedOptionsButton");
        if (savedAdvancedPref === "true") {
            $advancedBtn.removeClass("mdc-switch--unselected").addClass("mdc-switch--selected");
            $advancedBtn.attr("aria-checked", "true");
        } else {
            $advancedBtn.removeClass("mdc-switch--selected").addClass("mdc-switch--unselected");
            $advancedBtn.attr("aria-checked", "false");
        }
    }

    const alwaysShowClones = [];
    $("#addAuthenticationHandlerDialogFields").children().each(function () {
        const $clone = $(this).clone(true);
        hideElements($clone.find("[id$='checkBoxPanel']"));
        $clone.find("[id$='checkBoxPanel']").empty();
        if ($clone.find(".always-show").length > 0 || $clone.hasClass("always-show")) {
            showElements($clone.find("[id$='SwitchButtonPanel']"));
            alwaysShowClones.push($clone);
        } else {
            controlsPanel.append($clone);
        }
    });
    const $detachedFields = $("#addAuthenticationHandlerDialogFields").detach();

    dialogContainer.appendTo("body");

    CasDiscoveryProfile.fetchIfNeeded()
        .done(() => {
            const attrOptions = CasDiscoveryProfile.availableAttributes().map(attr => ({
                value: attr,
                text: attr
            }));

            const driverOptions = CasDiscoveryProfile.jdbcDrivers().map(driver => ({
                value: driver,
                text: driver
            }));
            const dialectOptions = CasDiscoveryProfile.jdbcDialects().map(dialect => ({
                value: dialect,
                text: dialect
            }));

            createMultiSelectField({
                id: "authnHandlerJdbcDriver",
                containerId: "authnHandlerControlsPanel",
                labelTitle: "Driver:",
                paramName: "driver",
                options: driverOptions,
                allowCreateOption: true,
                singleSelect: true,
                cssClasses: "JDBC hide"
            });

            createMultiSelectField({
                id: "authnHandlerJdbcDialect",
                containerId: "authnHandlerControlsPanel",
                labelTitle: "Dialect:",
                paramName: "dialect",
                options: dialectOptions,
                allowCreateOption: true,
                singleSelect: true,
                cssClasses: "JDBC hide"
            });

            createMultiSelectField({
                id: "authnHandlerPrincipalAttributeList",
                containerId: "authnHandlerControlsPanel",
                labelTitle: "Principal Attribute List:",
                paramName: "principal-attribute-list",
                options: attrOptions,
                allowCreateOption: true,
                cssClasses: "LDAP JDBC hide"
            });

            createMultiSelectField({
                id: "authnHandlerAdditionalAttributes",
                containerId: "authnHandlerControlsPanel",
                labelTitle: "Additional Attributes:",
                paramName: "additional-attributes",
                options: attrOptions,
                allowCreateOption: true,
                cssClasses: "LDAP hide advanced-option"
            });

            hideElements($("#authnHandlerPrincipalAttributeListSelectContainer"));
            hideElements($("#authnHandlerAdditionalAttributesSelectContainer"));
            hideElements($("#authnHandlerJdbcDriverSelectContainer"));
            hideElements($("#authnHandlerJdbcDialectSelectContainer"));

            ["authnHandlerPrincipalAttributeList", "authnHandlerAdditionalAttributes",
             "authnHandlerJdbcDriver", "authnHandlerJdbcDialect"].forEach(selectId => {
                const el = document.getElementById(selectId);
                if (el && el.tomselect) {
                    el.tomselect.on("item_add", () => generateAuthnHandlerConfiguration());
                    el.tomselect.on("item_remove", () => generateAuthnHandlerConfiguration());
                }
            });

            const currentType = $("#authenticationHandlerType").val();
            if (currentType === "LDAP" || currentType === "JDBC") {
                showElements($("#authnHandlerPrincipalAttributeListSelectContainer"));
            }
            if (currentType === "LDAP") {
                showElements($("#authnHandlerAdditionalAttributesSelectContainer"));
            }
            if (currentType === "JDBC") {
                showElements($("#authnHandlerJdbcDriverSelectContainer"));
                showElements($("#authnHandlerJdbcDialectSelectContainer"));
            }
        });

    function handleAuthenticationHandlerSubtypeChange(handlerType, subtype) {
        const combo = `${handlerType}-${subtype}`;
        hideElements($("#addAuthenticationHandlerDialog [id$='FieldContainer'], #addAuthenticationHandlerDialog [id$='SwitchButtonPanel']")
            .not(`.${handlerType}`)
            .not(`.${combo}`)
            .not(".always-show"));
        showElements($(`#addAuthenticationHandlerDialog .${combo}`));
        showElements($(`#addAuthenticationHandlerDialog [id$='FieldContainer'].${handlerType}`));
        showElements($(`#addAuthenticationHandlerDialog [id$='SwitchButtonPanel'].${handlerType}`));
        showElements($(`#addAuthenticationHandlerDialog [id$='SelectContainer'].${handlerType}`));
        toggleAuthnHandlerAdvancedOptions();
        generateAuthnHandlerConfiguration();
    }

    function handleAuthenticationHandlerTypeChange(type) {
        showElements($(`#addAuthenticationHandlerDialog .${type}`));
        hideElements($("#addAuthenticationHandlerDialog [id$='SelectContainer']")
            .not(`.${type}`)
            .not(".always-show"));
        hideElements($("#addAuthenticationHandlerDialog [id$='FieldContainer']"));
        hideElements($("#addAuthenticationHandlerDialog [id$='SwitchButtonPanel']")
            .not(".always-show"));

        if (type === "LDAP") {
            showElements($(`#addAuthenticationHandlerDialog [id$='FieldContainer'].${type}`));
            showElements($(`#addAuthenticationHandlerDialog [id$='SwitchButtonPanel'].${type}`));
            showElements($(`#addAuthenticationHandlerDialog [id$='SelectContainer'].${type}`));
            const subtype = $("#ldapAuthenticationHandlerType").val();
            if (subtype) {
                handleAuthenticationHandlerSubtypeChange(type, subtype);
            }
        } else if (type === "JDBC") {
            showElements($(`#addAuthenticationHandlerDialog [id$='FieldContainer'].${type}`));
            showElements($(`#addAuthenticationHandlerDialog [id$='SwitchButtonPanel'].${type}`));
            showElements($(`#addAuthenticationHandlerDialog [id$='SelectContainer'].${type}`));
            const subtype = $("#jdbcAuthenticationHandlerType").val();
            if (subtype) {
                handleAuthenticationHandlerSubtypeChange(type, subtype);
            }
        }
        toggleAuthnHandlerAdvancedOptions();
        generateAuthnHandlerConfiguration();
    }

    dialogContainer.dialog({
        position: {
            my: "center top",
            at: "center top+100",
            of: window
        },
        autoOpen: false,
        modal: true,
        width: 1500,
        height: "auto",
        title: "New Authentication Handler",
        buttons: {
            "Add Authentication Handler": function () {
                const $dialog = $(this);
                const editor = ace.edit("authnHandlerEditor");
                const editorContent = editor ? editor.getValue().trim() : "";
                function submitAuthenticationHandler(propertySource) {
                    const payload = [];
                    editorContent.split(/\r?\n/).forEach(line => {
                        line = line.trim();
                        if (!line || line.startsWith("#") || line.startsWith("!")) {
                            return;
                        }
                        const idx = line.indexOf("=");
                        if (idx === -1) {
                            return;
                        }
                        const entry = {
                            name: line.substring(0, idx).trim(),
                            value: line.substring(idx + 1).trim()
                        };
                        if (propertySource) {
                            entry.propertySource = propertySource;
                        }
                        payload.push(entry);
                    });

                    $.ajax({
                        url: `${CasActuatorEndpoints.casConfig()}/update`,
                        method: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(payload),
                        success: response => {
                            $dialog.dialog("close");
                            $.get(CasActuatorEndpoints.env(), async res => {
                                reloadConfigurationTable(res);
                                refreshCasServerConfiguration(`New Authentication Handler Created`);
                            })
                                .fail((xhr) => {
                                    displayBanner(xhr);
                                });
                        },
                        error: (xhr, status, error) => {
                            console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                            displayBanner(xhr);
                        }
                    });
                }

                if (mutablePropertySources.length === 1) {
                    submitAuthenticationHandler(mutablePropertySources[0]);
                } else {
                    Swal.fire({
                        title: "Which property source should receive the configuration?",
                        input: "select",
                        icon: "question",
                        inputOptions: mutablePropertySources,
                        inputPlaceholder: "Choose a property source...",
                        showCancelButton: true
                    }).then((result) => {
                        if (result.isConfirmed) {
                            submitAuthenticationHandler(mutablePropertySources[Number(result.value)]);
                        }
                    });
                }
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
        open: function () {
            const maxHeight = $(window).height() - 250;
            $(this).css({
                "max-height": maxHeight + "px",
                "overflow-y": "auto",
                "overflow-x": "visible"
            });
            $(this).closest(".ui-dialog").css("overflow", "visible");

            const $buttonPane = $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane");
            $buttonPane.css({"display": "flex", "align-items": "center"});
            const $togglesContainer = $("<div>", {
                css: {"display": "flex", "align-items": "center", "margin-right": "auto"}
            });
            for (const $clone of alwaysShowClones) {
                $togglesContainer.append($clone);
            }
            $buttonPane.prepend($togglesContainer);

            $("#addAuthenticationHandlerDialog .jqueryui-selectmenu").selectmenu({
                width: "70%",
                change: function (event, ui) {
                    if ($(this).attr("id") === "authenticationHandlerType") {
                        handleAuthenticationHandlerTypeChange(ui.item.value);
                    } else if ($(this).attr("id") === "ldapAuthenticationHandlerType") {
                        const handlerType = $("#authenticationHandlerType").val();
                        handleAuthenticationHandlerSubtypeChange(handlerType, ui.item.value);
                    } else if ($(this).attr("id") === "jdbcAuthenticationHandlerType") {
                        const handlerType = $("#authenticationHandlerType").val();
                        handleAuthenticationHandlerSubtypeChange(handlerType, ui.item.value);
                    }
                    generateAuthnHandlerConfiguration();
                }
            });
            cas.init("#addAuthenticationHandlerDialog");
            cas.init(".ui-dialog-buttonpane");
            $("#addAuthenticationHandlerDialog .jqueryui-selectmenu").each(function () {
                $(this).closest("label").css({
                    "display": "flex",
                    "align-items": "center",
                    "white-space": "nowrap"
                }).contents().filter(function () {
                    return this.nodeType === 3;
                }).wrap("<span style='min-width: 200px; width: 200px; display: inline-block;'></span>");
            });
            $("#addAuthenticationHandlerDialog .jqueryui-multiselectmenu").each(function () {
                $(this).closest("[id$='SelectContainer']").css({
                    "display": "flex",
                    "align-items": "center"
                });
                $(this).closest("[id$='SelectContainer']").find("label").css({
                    "min-width": "200px",
                    "width": "200px"
                });
                $(this).next(".ts-wrapper").css("flex", "1");
            });
            const currentType = $("#authenticationHandlerType").val();
            handleAuthenticationHandlerTypeChange(currentType);

            const authnEditor = initializeAceEditor("authnHandlerEditor", "properties");
            authnEditor.setReadOnly(true);

            $("#addAuthenticationHandlerDialog").on("input", "input[data-param-name]", function () {
                generateAuthnHandlerConfiguration();
            });
            $("#addAuthenticationHandlerDialog").on("change", "input[data-switch-btn]", function () {
                generateAuthnHandlerConfiguration();
            });

            $("#addAuthenticationHandlerDialog input[data-param-name]").each(function () {
                $(this).data("default-value", $(this).val() || "");
            });
            $("#addAuthenticationHandlerDialog select.jqueryui-selectmenu").each(function () {
                $(this).data("default-value", $(this).val() || "");
            });

            toggleAuthnHandlerEditorVisibility();
            toggleAuthnHandlerAdvancedOptions();
            generateAuthnHandlerConfiguration();
        },
        close: function () {
            $("#authenticationhandlers-tab").append($detachedFields);
            $(this).dialog("destroy").remove();
        }
    });
    dialogContainer.dialog("open");
}

function reloadAuthenticationHandlersTable() {
    const authenticationHandlersTable = $("#authenticationHandlersTable").DataTable();
    authenticationHandlersTable.clear();
    if (CasActuatorEndpoints.authenticationHandlers()) {
        $.get(CasActuatorEndpoints.authenticationHandlers(), response => {
            for (const handler of response) {
                authenticationHandlersTable.row.add({
                    0: `${handler.name}`,
                    1: `<code>${handler.type}</code>`,
                    2: `<code>${handler.state}</code>`,
                    3: `<code>${handler.order}</code>`
                });
            }
            authenticationHandlersTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}

async function initializeAuthenticationOperations() {
    const authnHandlersToolbar = document.createElement("div");
    let authnHandlersToolbarEntries = "";
    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
        authnHandlersToolbarEntries += `
            <button type="button" id="newAuthenticationHandlerButton"
                    onclick="openNewAuthenticationHandlerDialog()"
                    title="Add a new authentication handler"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
            </button>
        `;
    }
    authnHandlersToolbar.innerHTML = authnHandlersToolbarEntries;
    const authenticationHandlersTable = $("#authenticationHandlersTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        layout: {
            topStart: authnHandlersToolbar
        },
        columnDefs: [
            {visible: false, targets: 0},
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#authenticationHandlersTable tr").addClass("mdc-data-table__row");
            $("#authenticationHandlersTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="3">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    reloadAuthenticationHandlersTable();

    const authenticationPoliciesTable = $("#authenticationPoliciesTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        drawCallback: settings => {
            $("#authenticationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#authenticationPoliciesTable td").addClass("mdc-data-table__cell");
        }
    });

    authenticationPoliciesTable.clear();
    if (CasActuatorEndpoints.authenticationPolicies()) {
        $.get(CasActuatorEndpoints.authenticationPolicies(), response => {
            for (const handler of response) {
                authenticationPoliciesTable.row.add({
                    0: `${handler.name}`,
                    1: `<code>${handler.order}</code>`
                });
            }
            authenticationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    const toolbar = document.createElement("div");
    let toolbarEntries = `
        <button type="button" id="loadExternalIdentityProvidersTableButton"
                onclick="loadExternalIdentityProvidersTable()"
                title="Reload external identity providers from sources"
                class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-refresh" aria-hidden="true"></i>Reload</span>
        </button>
    `;

    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
        toolbarEntries += `
            <button type="button" id="newExternalIdentityProvider"
                    onclick="newExternalIdentityProvider()"
                    title="Create a new external identity provider"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
            </button>
        `;
    }

    toolbar.innerHTML = toolbarEntries;
    $("#delegatedClientsTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        layout: {
            topStart: toolbar
        },
        columnDefs: [
            {visible: false, targets: 0},
            {visible: false, targets: 3}
        ],
        drawCallback: settings => {
            $("#delegatedClientsTable tr").addClass("mdc-data-table__row");
            $("#delegatedClientsTable td").addClass("mdc-data-table__cell");
            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        let samlButtons = "";
                        let toolbarButtons = "";

                        rows.data().each(entry => {
                            if (entry[0] === group) {
                                if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
                                    toolbarButtons = `
                                        <span class="px-2" style="float: right;">
                                            <button type="button" 
                                                    name="removeIdentityProvider" 
                                                    href="#"
                                                    title="Remove Identity Provider"
                                                    onclick="removeIdentityProvider('${group}', '${entry[3]}')" 
                                                    data-client-name='${group}'
                                                    data-type='${entry[3]}'
                                                    class="mdc-button mdc-button--raised toolbar">
                                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                                            </button>
                                        </span>
                                    `.trim();
                                }

                                if (entry[3] === "saml2") {
                                    samlButtons = `
                                    <span class="px-2"  style="float: right;">
                                            <button type="button" title="Service Provider Metadata" 
                                                    title="View Service Provider Metadata"
                                                    name="saml2ClientSpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar pr-2">
                                                <i class="mdi mdi-text-box min-width-32x" aria-hidden="true"></i>
                                                Service Provider Metadata
                                            </button>
                                            <button type="button" title="Identity Provider Metadata" 
                                                    title="View Identity Provider Metadata"
                                                    name="saml2ClientIdpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar pr-2">
                                                <i class="mdi mdi-file-xml-box min-width-32x" aria-hidden="true"></i>
                                                Identity Provider Metadata
                                            </button>
                                    </span>
                                    `.trim();
                                }
                            }
                        });
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="2"><span class="idp-group">${group}</span>${toolbarButtons.trim()} ${samlButtons.trim()}</td>
                            </tr>`.trim()
                        );
                        configureSaml2ClientMetadataButtons();
                        last = group;
                    }
                });
        }
    });


    await loadExternalIdentityProvidersTable();
}
