let CAS_CONFIG_METADATA = {}

function overrideConfigPropertyValue(name, value) {
    openNewConfigurationPropertyDialog({
        name: name,
        value: value,
        updateEntry: true
    });
}

function searchForConfigPropertyButton(name) {
    $("#configuration-tabs").tabs("option", "active", $("#casConfigSearch").index());
    $("#configSearchQuery").val(`^${name}$`).focus().select();
    $("#configSearchButton").click();
}

function effectiveConfigPropertyValue(name) {
    $.get(`${CasActuatorEndpoints.env()}/${name}`, response => {
        Swal.fire({
            title: "Effective Property Value",
            html: `
                The effective configuration value for <code>${name}</code> is:<p/>
                <pre><code class="border-rounded language-html">${name}=${response.property.value}</code></pre>
            `,
            icon: "info",
            width: "50%",
            showDenyButton: true,
            confirmButtonText: "OK",
            denyButtonText: "Copy",
            didOpen: () => {
                highlightElements();
                Swal.getDenyButton().addEventListener("click", async () => {
                    const text = `${name}=${response.property.value}`;
                    copyToClipboard(text);
                    setTimeout(() => Swal.resetValidationMessage(), 100);
                });
            }
        });
    })
        .fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
}

function deleteConfigPropertyValue(button, name) {
    if (PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig()) {
        const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
        const currentRow = mutableConfigurationTable.row($(button).closest("tr"));
        const propertySource = $(button).data("source").replace("bootstrapProperties-", "");
        Swal.fire({
            title: `Are you sure you want to delete this entry from ${propertySource}?`,
            text: "Once removed, you may not be able to revert this.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.casConfig()}`,
                        method: "DELETE",
                        contentType: "application/json",
                        data: JSON.stringify(
                            {
                                name: name,
                                propertySource: propertySource
                            }
                        )
                    })
                        .done(function (data, textStatus, jqXHR) {
                            const sources = data.join(",");

                            currentRow.remove().draw(false);
                            refreshCasServerConfiguration(`${sources}: Property ${name} Removed`);
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.error("Error:", textStatus, errorThrown);
                            displayBanner(jqXHR);
                        });
                }
            });
    }
}

function updateConfigPropertyValue(button, name) {
    if (PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig()) {
        const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
        const currentRow = mutableConfigurationTable.row($(button).closest("tr"));
        const propertySource = $(button).data("source").replace("bootstrapProperties-", "");
        const rowData = currentRow.data();

        openNewConfigurationPropertyDialog({
            name: name,
            value: $(rowData[2]).text(),
            propertySource: propertySource,
            updateEntry: true
        });
    }
}

function deleteAllConfigurationProperties(button) {
    function deletePropertiesFromSource(propertySource) {
        Swal.close();

        Swal.fire({
            title: `Delete Configuration Properties`,
            text: `Are you sure you want to delete all configuration properties from ${propertySource}? 
                Once deleted, you may not be able to recover the configuration properties.`,
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.casConfig()}`,
                        method: "DELETE",
                        contentType: "application/json",
                        data: JSON.stringify(
                            {
                                propertySource: propertySource
                            }
                        )
                    })
                        .done(function (data, textStatus, jqXHR) {
                            $.get(CasActuatorEndpoints.env(), res => {
                                reloadConfigurationTable(res);
                                refreshCasServerConfiguration(`${propertySource}: All Properties Removed`);
                            })
                                .fail((xhr) => {
                                    displayBanner(xhr);
                                });
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.error("Error:", textStatus, errorThrown);
                            displayBanner(jqXHR);
                        });
                }
            });

    }

    if (PalantirDashboardConfiguration.mutablePropertySources().length === 1) {
        deletePropertiesFromSource(PalantirDashboardConfiguration.mutablePropertySources()[0]);
    } else {
        Swal.fire({
            title: "Which property source do you want to clear?",
            input: "select",
            icon: "question",
            inputOptions: PalantirDashboardConfiguration.mutablePropertySources(),
            inputPlaceholder: "Choose a property source...",
            showCancelButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                deletePropertiesFromSource(result.value);
            }
        });
    }
}

function importConfigurationProperties(button) {
    function parseProperties(text) {
        const result = {};
        text.split(/\r?\n/).forEach(line => {
            line = line.trim();
            if (!line || line.startsWith("#") || line.startsWith("!")) {
                return;
            }

            const idx = line.indexOf("=");
            if (idx === -1) {
                return;
            }
            const key = line.substring(0, idx).trim();
            result[key] = line.substring(idx + 1).trim();
        });
        return result;
    }

    function importProperties(payload) {
        $.ajax({
            url: `${CasActuatorEndpoints.casConfig()}/update`,
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: response => {
                Swal.close();
                $.get(CasActuatorEndpoints.env(), res => {
                    reloadConfigurationTable(res);
                    refreshCasServerConfiguration(`New Property ${name} Created`);
                })
                    .fail((xhr) => {
                        displayBanner(xhr);
                    });
            },
            error: (xhr, status, error) => {
                Swal.close();
                console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                displayBanner(xhr);
            }
        });
    }

    Swal.fire({
        title: "Are you sure you want to import configuration properties?",
        input: "select",
        inputOptions: PalantirDashboardConfiguration.mutablePropertySources(),
        html: `
            <p class="text-justify">Properties will be imported and applied to the CAS server configuration. You may import multiple files at once.
            Supported file formats are .properties and .yml/.yaml files. You may choose the target property source to which the properties will be applied.</p>
        `,
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then((result) => {
        if (result.isConfirmed) {
            const propertySource = PalantirDashboardConfiguration.mutablePropertySources()[Number(result.value)];

            $("#configurationFilesToImport").click();
            $("#configurationFilesToImport").change(event => {
                const files = Array.from(event.target.files);
                files.forEach(file => {
                    const reader = new FileReader();
                    reader.readAsText(file);
                    reader.onload = e => {
                        const content = e.target.result;
                        const ext = file.name.split(".").pop().toLowerCase();

                        Swal.fire({
                            icon: "info",
                            title: `${propertySource}: Importing Properties`,
                            text: "Please wait while the configuration properties are being imported...",
                            allowOutsideClick: false,
                            showConfirmButton: false,
                            didOpen: () => Swal.showLoading()
                        });
                        if (ext === "properties") {
                            const props = parseProperties(content);
                            const payload = Object.entries(props).map(([key, value]) => ({
                                name: key,
                                value
                            }));
                            importProperties(payload);
                        } else if (ext === "yml" || ext === "yaml") {
                            const props = flattenJSON(jsyaml.load(content));
                            const payload = Object.entries(props).map(([key, value]) => ({
                                name: key,
                                value
                            }));
                            importProperties(payload);
                        }
                    };
                });
                $("#configurationFilesToImport").val("");
            });
        }
    });

}

async function encryptConfigProperty(value) {
    try {
        return await $.ajax({
            method: "POST",
            url: `${CasActuatorEndpoints.casConfig()}/encrypt`,
            data: value,
            contentType: "text/plain; charset=utf-8",
            dataType: "text"
        });
    } catch (xhr) {
        displayBanner(xhr);
        throw xhr;
    }
}

function createNewConfigurationProperty(button) {
    openNewConfigurationPropertyDialog({
        name: "",
        propertySource: "",
        updateEntry: false
    });

}

function openNewConfigurationPropertyDialog(config) {
    $("#newConfigurationDialog").dialog({
        autoOpen: false,
        modal: true,
        position: {
            my: "center top",
            at: "center top+50",
            of: window
        },
        width: 700,
        height: "auto",
        buttons: {
            OK: async function () {
                const nameElem = $("#newConfigPropertyName")[0];
                const selectedValue = nameElem.tomselect.getValue();
                const typedValue = nameElem.tomselect.control_input.value?.trim();
                if (!selectedValue && typedValue) {
                    nameElem.tomselect.setValue(typedValue);
                }

                if (!$("#newConfigurationForm")[0].reportValidity()) {
                    return;
                }

                const name = $("#newConfigPropertyName").val();
                let value = $("#newConfigPropertyValue").val();
                const encrypt = $("#encryptConfigProperty").val();

                if (encrypt && encrypt === "true" && CasActuatorEndpoints.casConfig()) {
                    value = await encryptConfigProperty(value);
                }

                const sources = $("#propertySourcesSelect").val();

                let payload;
                if (sources.length === 0) {
                    payload = [{name: name, value: value}];
                } else {
                    payload = sources.map(entry => ({
                        name: name,
                        value: value,
                        propertySource: entry
                    }));
                }

                $.ajax({
                    url: `${CasActuatorEndpoints.casConfig()}/update`,
                    method: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(payload),
                    success: response => {
                        $(this).dialog("close");
                        $.get(CasActuatorEndpoints.env(), res => {
                            reloadConfigurationTable(res);
                            refreshCasServerConfiguration(`New Property ${name} Created`);
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
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
            open: function () {
                $(this).css("overflow", "visible");

                const tomselect = $("#newConfigPropertyName")[0].tomselect;
                if (config.name) {
                    if (!tomselect.options[config.name]) {
                        tomselect.addOption({
                            id: config.name,
                            name: config.name
                        });
                    }
                    tomselect.setValue(config.name, true);
                } else {
                    tomselect.setValue("", true);
                }
                
                $("#newConfigPropertyValue").val(config.value ?? "");

                if (config.propertySource) {
                    $("#propertySourcesSelect").val(config.propertySource ?? "");
                } else {
                    const ts = $("#propertySourcesSelect")[0].tomselect;
                    const options = Object.keys(ts.options);
                    if (options.length === 1) {
                        ts.setValue(options[0], true);
                    }
                }

                if (config.updateEntry) {
                    $("#newConfigPropertyName").parent().hide();
                    $("#propertySourcesSection").hide();
                    $("#newConfigPropertyValue").focus().select();
                } else {
                    $("#propertySourcesSection").show();
                    $("#newConfigPropertyName").parent().show();
                    $("#newConfigPropertyName")[0].tomselect.focus();
                }
            }
    });
    $("#newConfigurationDialog").dialog("open");
}

function refreshCasServerConfiguration(title) {
    if (CasActuatorEndpoints.refresh()) {
        Swal.fire({
            showConfirmButton: true,
            showCancelButton: true,
            icon: "question",
            title: title,
            width: "35%",
            confirmButtonText: "Refresh",
            html: `Do you want to refresh the CAS runtime context to apply the changes?
            <p class="text-justify"/>CAS will rebind components to external configuration properties <strong>internally without a restart</strong>, 
            allowing components to be refreshed to reflect the changes. In-flight requests and operations keep running normally
            using the existing/old CAS components until they are fully refreshed in the runtime application context.
            <p class="text-justify"/>
            <strong>Note: Not every component is refreshable.</strong> Configuration properties that control
            fundamental aspects of CAS operation <strong>(specially those that are not owned or controlled by CAS)</strong> 
            may not be dynamically reloaded. In such cases, a full restart of the CAS server
            may be required for the changes to take effect.
            `
        })
            .then((reloadResult) => {
                if (reloadResult.isConfirmed) {
                    Swal.close();
                    Swal.fire({
                        icon: "info",
                        title: "Refreshing CAS Server",
                        text: "Please wait while the CAS application context is being refreshed...",
                        allowOutsideClick: false,
                        showConfirmButton: false,
                        didOpen: () => Swal.showLoading()
                    });

                    const endpoint = CasActuatorEndpoints.refresh();
                    $.post(endpoint)
                        .done(() => {
                            reloadAuthenticationHandlersTable();
                            loadExternalIdentityProvidersTable().then(r => {
                                Swal.close();
                                Swal.fire({
                                    title: "Success!",
                                    text: "CAS configuration has been reloaded successfully.",
                                    icon: "success",
                                    timer: 1000,
                                    timerProgressBar: true,
                                    showConfirmButton: true
                                });
                            });
                        })
                        .fail((jqXHR, textStatus, errorThrown) => {
                            console.error("Error:", textStatus, errorThrown);
                            Swal.close();
                            Swal.fire("Error", "Failed to reload CAS configuration.", "error");
                            displayBanner(jqXHR);
                        });
                }
            });
    }
}

function reloadConfigurationTable(response) {
    const configurationTable = $("#configurationTable").DataTable();
    configurationTable.clear();

    const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
    mutableConfigurationTable.clear();

    $("#casPropertySourcesChipset").empty();

    for (const source of response.propertySources) {
        let propertySourceChip = `
            <div class="mdc-chip" role="row">
                <div class="mdc-chip__ripple"></div>
                <span role="gridcell">
                  <span class="mdc-chip__text">${source.name}</span>
                </span>
            </div>
        `.trim();
        $("#casPropertySourcesChipset").append($(propertySourceChip));

        const properties = flattenJSON(source.properties);
        for (const [key, value] of Object.entries(properties)) {
            if (!key.endsWith(".origin")) {
                const propertyName = key.replace(".value", "");
                let buttons = `
                    <button type="button" name="effectiveConfigPropertyValueButton" href="#"
                            title="View Effective Value" 
                            data-key='${propertyName}'
                            onclick="effectiveConfigPropertyValue('${propertyName}')"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                    </button>
                `;

                if (CasActuatorEndpoints.configurationMetadata()) {
                    buttons += `
                            <button type="button" 
                                    name="searchForConfigPropertyButton" href="#" 
                                    title="Configuration Property Help"
                                    data-key='${propertyName}'
                                    data-value="'${value}'"
                                    onclick="searchForConfigPropertyButton('${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-help min-width-32x" aria-hidden="true"></i>
                            </button>
                    `;
                }

                if (PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig()) {
                    buttons += `
                            <button type="button" 
                            name="overrideConfigPropertyValueButton" href="#" 
                                    title="Override Configuration Property Value"
                                    data-key='${propertyName}'
                                    data-value="'${value}'"
                                    onclick="overrideConfigPropertyValue('${propertyName}', '${value}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-arrow-left-circle min-width-32x" aria-hidden="true"></i>
                            </button>
                    `;
                }
                configurationTable.row.add({
                    0: `${camelcaseToTitleCase(source.name)}`,
                    1: `<code>${propertyName}</code>`,
                    2: `<code>${value}</code>`,
                    3: buttons
                });


                if (PalantirDashboardConfiguration.mutablePropertySources().some(entry => source.name.endsWith(entry))) {
                    const propertyName = key.replace(".value", "");
                    let buttons = `
                            <button type="button" name="effectiveConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    title="View Effective Value"
                                    onclick="effectiveConfigPropertyValue('${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="updateConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    title="Update Configuration Property Value"
                                    data-source='${source.name}'
                                    onclick="updateConfigPropertyValue(this, '${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-content-save-edit min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="deleteConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    title="Delete Configuration Property Value"
                                    data-source='${source.name}'
                                    onclick="deleteConfigPropertyValue(this, '${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                            </button>
                        `;

                    if (CasActuatorEndpoints.configurationMetadata()) {
                        buttons += `
                            <button type="button" 
                                    name="searchForConfigPropertyButton" href="#" 
                                    data-key='${propertyName}'
                                    title="Configuration Property Help"
                                    data-value="'${value}'"
                                    onclick="searchForConfigPropertyButton('${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-help min-width-32x" aria-hidden="true"></i>
                            </button>
                        `;
                    }
                    mutableConfigurationTable.row.add({
                        0: `${camelcaseToTitleCase(source.name)}`,
                        1: `<code>${propertyName}</code>`,
                        2: `<code>${value}</code>`,
                        3: buttons
                    });
                }
            }
        }
    }
    configurationTable.search("").draw();
    mutableConfigurationTable.search("").draw();
}

async function loadConfigurationMetadata() {
    if (!CasActuatorEndpoints.configurationMetadata() || CAS_CONFIG_METADATA.length === 0) {
        return;
    }
    const response = await fetch(CasActuatorEndpoints.configurationMetadata());
    if (!response.ok) {
        throw new Error(`Failed to load configuration metadata: ${response.status}`);
    }
    
    CAS_CONFIG_METADATA = Object.values(await response.json());
}

async function populateConfigurationNameSelectOptions() {
    const nameElem = $("#newConfigPropertyName")[0];
    const ts = nameElem.tomselect;
    const currentValue = ts.getValue();
    const entries = [...new Map(
        CAS_CONFIG_METADATA
            .filter(entry => entry.id)
            .map(entry => [
                entry.id,
                {
                    id: entry.id,
                    name: entry.id,
                    type: entry.type,
                    description: entry.description,
                    defaultValue: entry.defaultValue ?? "",
                    deprecated: entry.deprecated ?? false
                }
            ])
    ).values()]
        .sort((a, b) => a.id.localeCompare(b.id));
    ts.clearOptions();
    entries.forEach(entry => {
        if (!ts.options[entry.id]) {
            ts.addOption(entry);
        }
    });
    ts.refreshOptions(false);
    if (currentValue) {
        ts.setValue(currentValue, true);
    }
}

function escapeConfigHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, s => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[s]));
}

function classifySpringBean(type) {
    const beanType = String(type ?? "");
    if (beanType.startsWith("org.springframework")
        || beanType.startsWith("jakarta.")
        || beanType.startsWith("javax.")
        || beanType.startsWith("com.fasterxml.")
        || beanType.startsWith("io.micrometer.")) {
        return "FRAMEWORK";
    }
    if (beanType.startsWith("org.apereo.cas")) {
        return "CAS";
    }
    return "APPLICATION";
}

function normalizeSpringBeans(response) {
    const entries = [];
    Object.entries(response.contexts ?? {}).forEach(([contextName, context]) => {
        Object.entries(context.beans ?? {}).forEach(([name, bean]) => {
            entries.push({
                name,
                context: contextName,
                type: bean.type ?? "",
                scope: bean.scope ?? "singleton",
                classification: classifySpringBean(bean.type),
                dependencies: bean.dependencies ?? []
            });
        });
    });
    return entries.sort((a, b) => a.name.localeCompare(b.name));
}

function setSpringBeansUnavailable(message) {
    $("#springBeansUnavailable span").html(escapeConfigHtml(message));
    showElements($("#springBeansUnavailable"));
    hideElements($("#springBeansContent"));
    hideElements($("#springBeansTabItem"));
}

function setSpringBeansAvailable() {
    hideElements($("#springBeansUnavailable"));
    showElements($("#springBeansContent"));
    showElements($("#springBeansTabItem"));
}

function collectSpringConditionMatches(source, matches, matchType, contextName) {
    const entries = [];
    const addMatch = (match, outcome = matchType) => {
        if (!match) {
            return;
        }
        entries.push({
            source,
            context: contextName,
            matchType: outcome,
            condition: match.condition ?? "",
            message: match.message ?? ""
        });
    };

    if (Array.isArray(matches)) {
        matches.forEach(match => addMatch(match));
        return entries;
    }

    if (matches?.condition || matches?.message) {
        addMatch(matches);
        return entries;
    }

    (matches?.notMatched ?? []).forEach(match => addMatch(match, "negative"));
    (matches?.matched ?? []).forEach(match => addMatch(match, "positive"));
    return entries;
}

function normalizeSpringConditions(response) {
    const entries = {
        positive: [],
        negative: []
    };
    Object.entries(response.contexts ?? {}).forEach(([contextName, context]) => {
        Object.entries(context.positiveMatches ?? {}).forEach(([source, matches]) => {
            entries.positive.push(...collectSpringConditionMatches(source, matches, "positive", contextName));
        });
        Object.entries(context.negativeMatches ?? {}).forEach(([source, matches]) => {
            const collected = collectSpringConditionMatches(source, matches, "negative", contextName);
            entries.negative.push(...collected.filter(entry => entry.matchType === "negative"));
        });
    });
    entries.positive.sort((a, b) => a.source.localeCompare(b.source));
    entries.negative.sort((a, b) => a.source.localeCompare(b.source));
    return entries;
}

function setSpringConditionsUnavailable(message) {
    $("#springConditionsUnavailable span").html(escapeConfigHtml(message));
    showElements($("#springConditionsUnavailable"));
    hideElements($("#springConditionsContent"));
    hideElements($("#springConditionsTabItem"));
}

function setSpringConditionsAvailable() {
    hideElements($("#springConditionsUnavailable"));
    showElements($("#springConditionsContent"));
    showElements($("#springConditionsTabItem"));
}

async function initializeConfigurationOperations() {
    const configurationTable = $("#configurationTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#configurationTable tr").addClass("mdc-data-table__row");
            $("#configurationTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group}</td>
                            </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    const toolbar = document.createElement("div");
    let toolbarEntries = `
            <button type="button" id="reloadConfigurationTableButton" 
                    title="Reload Configuration Table"
                    onclick="$.get(CasActuatorEndpoints.env(), res => { reloadConfigurationTable(res); }).fail((xhr) => { displayBanner(xhr); });"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-database-arrow-down" aria-hidden="true"></i>Reload</span>
            </button>
    `;
    if (PalantirDashboardConfiguration.mutablePropertySourcesAvailable()) {
        toolbarEntries += `
            <button type="button" title="Create a new configuration property" onclick="createNewConfigurationProperty(this);" id="newConfigPropertyButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus" aria-hidden="true"></i>New Property</span>
            </button>
            <button type="button" title="Delete all configuration properties" onclick="deleteAllConfigurationProperties(this);" id="deleteAllConfigurationPropertiesButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-delete" aria-hidden="true"></i>Delete All</span>
            </button>
            <button type="button" title="Import configuration from properties from files" onclick="importConfigurationProperties(this);" id="importConfigurationPropertiesButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-file-import" aria-hidden="true"></i>Import</span>
            </button>
            <button type="button" id="refreshConfigurationButton"
                    title="Refresh CAS Server Configuration"
                    onclick="refreshCasServerConfiguration('Context Refresh');" 
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-refresh" aria-hidden="true"></i>Refresh CAS</span>
            </button>
        `;
    }

    toolbar.innerHTML = toolbarEntries;
    const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        layout: {
            topStart: toolbar
        },
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#mutableConfigurationTable tr").addClass("mdc-data-table__row");
            $("#mutableConfigurationTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group}</td>
                            </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function encryptOrDecryptConfig(op) {
        hideBanner();
        hideElements("#configEncryptionResult");

        const form = document.getElementById("fmConfigEncryption");
        if (!form.reportValidity()) {
            return false;
        }
        const configValue = $("#configValue").val();
        if (CasActuatorEndpoints.casConfig()) {
            $.post({
                url: `${CasActuatorEndpoints.casConfig()}/${op}`,
                data: configValue,
                contentType: "text/plain"
            }, data => {
                $("#configEncryptionResult pre code").text(data);
                highlightElements();
                showElements("#configEncryptionResult");
            }).fail((xhr, status, error) => {
                displayBanner(xhr);
                hideElements("#configEncryptionResult");
            });
        }
    }

    configurationTable.clear();
    mutableConfigurationTable.clear();

    if (CasActuatorEndpoints.env()) {
        $.get(CasActuatorEndpoints.env(), response => {
            reloadConfigurationTable(response);

            $("#casActiveProfiles").empty();
            for (const element of response.activeProfiles) {
                let feature = `
                <div class="mdc-chip" role="row">
                    <div class="mdc-chip__ripple"></div>
                    <span role="gridcell">
                        <i class="mdi mdi-wrench" aria-hidden="true"></i>
                      <span class="mdc-chip__text">${element.trim()}</span>
                    </span>
                </div>
            `.trim();
                $("#casActiveProfiles").append($(feature));
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
    
    const configPropsTable = $("#configPropsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#configPropsTable tr").addClass("mdc-data-table__row");
            $("#configPropsTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });
    configPropsTable.clear();

    if (CasActuatorEndpoints.configProps()) {
        $.get(CasActuatorEndpoints.configProps(), response => {
            const casBeans = response.contexts["cas-1"].beans;
            const bootstrapBeans = response.contexts["bootstrap"].beans;
            for (const [sourceBean, bean] of Object.entries(casBeans)) {
                let flattened = flattenJSON(bean.properties);
                for (const [prop, propValue] of Object.entries(flattened)) {
                    const property = `${bean.prefix}.${prop}`;
                    if (Object.keys(propValue).length > 0) {
                        configPropsTable.row.add({
                            0: `${sourceBean}`,
                            1: `<code>${property}</code>`,
                            2: `<code>${propValue}</code>`
                        });
                    }
                }
            }
            for (const [sourceBean, bean] of Object.entries(bootstrapBeans)) {
                let flattened = flattenJSON(bean.properties);
                for (const [prop, propValue] of Object.entries(flattened)) {
                    const property = `${bean.prefix}.${prop}`;
                    if (Object.keys(propValue).length > 0) {
                        configPropsTable.row.add({
                            0: `${sourceBean}`,
                            1: `<code>${toKebabCase(property)}</code>`,
                            2: `<code>${propValue}</code>`
                        });
                    }
                }
            }
            configPropsTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    let springBeansEntries = [];
    const springBeansTable = $("#springBeansTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        order: [[0, "asc"]],
        columns: [
            {
                data: "name",
                width: "22%",
                render: (data, type) => type === "display"
                    ? `<code class="spring-bean-name">${escapeConfigHtml(data)}</code>`
                    : data
            },
            {
                data: "type",
                width: "34%",
                render: (data, type) => type === "display"
                    ? `<code class="spring-bean-type">${escapeConfigHtml(data)}</code>`
                    : data
            },
            {
                data: "scope",
                width: "10%",
                render: data => escapeConfigHtml(data)
            },
            {
                data: "classification",
                width: "12%",
                render: (data, type) => type === "display"
                    ? `<span class="spring-bean-classification classification-${escapeConfigHtml(String(data).toLowerCase())}">${escapeConfigHtml(data)}</span>`
                    : data
            },
            {
                data: "dependencies",
                render: (data, type) => {
                    if (type !== "display") {
                        return Array.isArray(data) ? data.join(", ") : data;
                    }
                    const dependencies = Array.isArray(data) ? data : [];
                    return dependencies.length > 0
                        ? `<span class="spring-bean-dependencies">${dependencies.map(escapeConfigHtml).join(", ")}</span>`
                        : `<span class="text-muted">&mdash;</span>`;
                }
            }
        ],
        drawCallback: settings => {
            $("#springBeansTable tr").addClass("mdc-data-table__row");
            $("#springBeansTable td").addClass("mdc-data-table__cell");
        }
    });

    function renderSpringBeans() {
        springBeansTable.clear();
        springBeansEntries.forEach(entry => springBeansTable.row.add(entry));
        springBeansTable.draw();
        springBeansTable.columns.adjust();
    }

    $("#springBeansFilter").off("input").on("input", renderSpringBeans);
    if (CasActuatorEndpoints.beans()) {
        $.get(CasActuatorEndpoints.beans(), response => {
            setSpringBeansAvailable();
            springBeansEntries = normalizeSpringBeans(response);
            renderSpringBeans();
        }).fail((xhr, status, error) => {
            console.error("Error fetching Spring beans:", error);
            setSpringBeansUnavailable("The Spring Boot beans actuator endpoint is not available.");
        });
    } else {
        setSpringBeansUnavailable("The Spring Boot beans actuator endpoint is not available.");
    }

    let springConditionsEntries = {positive: [], negative: []};
    const createSpringConditionsTable = (selector, positive) => $(selector).DataTable({
        pageLength: 10,
        autoWidth: false,
        order: [[0, "asc"]],
        columns: [
            {
                data: "source",
                width: "36%",
                render: (data, type) => {
                    if (type !== "display") {
                        return data;
                    }
                    const icon = positive ? "mdi-check-circle-outline" : "mdi-close-circle-outline";
                    const status = positive ? "positive" : "negative";
                    return `
                        <span class="spring-condition-source spring-condition-source-${status}">
                            <i class="mdi ${icon}" aria-hidden="true"></i>
                            <code>${escapeConfigHtml(data)}</code>
                        </span>
                    `.trim();
                }
            },
            {
                data: "condition",
                width: "26%",
                render: (data, type) => type === "display"
                    ? `<code class="spring-condition-name">${escapeConfigHtml(data)}</code>`
                    : data
            },
            {
                data: "message",
                render: (data, type) => type === "display"
                    ? `<span class="spring-condition-message">${escapeConfigHtml(data || "No condition message provided.")}</span>`
                    : data
            }
        ],
        drawCallback: settings => {
            $(`${selector} tr`).addClass("mdc-data-table__row");
            $(`${selector} td`).addClass("mdc-data-table__cell");
        }
    });
    const springConditionsPositiveTable = createSpringConditionsTable("#springConditionsPositiveTable", true);
    const springConditionsNegativeTable = createSpringConditionsTable("#springConditionsNegativeTable", false);

    function renderSpringConditions() {
        springConditionsPositiveTable.clear();
        springConditionsNegativeTable.clear();
        springConditionsEntries.positive.forEach(entry => springConditionsPositiveTable.row.add(entry));
        springConditionsEntries.negative.forEach(entry => springConditionsNegativeTable.row.add(entry));
        springConditionsPositiveTable.draw();
        springConditionsNegativeTable.draw();
        springConditionsPositiveTable.columns.adjust();
        springConditionsNegativeTable.columns.adjust();
    }

    $("#springConditionsResultTabs").tabs({
        activate: () => {
            springConditionsPositiveTable.columns.adjust();
            springConditionsNegativeTable.columns.adjust();
        }
    });
    if (CasActuatorEndpoints.conditions()) {
        $.get(CasActuatorEndpoints.conditions(), response => {
            setSpringConditionsAvailable();
            springConditionsEntries = normalizeSpringConditions(response);
            renderSpringConditions();
        }).fail((xhr, status, error) => {
            console.error("Error fetching Spring conditions:", error);
            setSpringConditionsUnavailable("The Spring Boot conditions actuator endpoint is not available.");
        });
    } else {
        setSpringConditionsUnavailable("The Spring Boot conditions actuator endpoint is not available.");
    }

    $("#encryptConfigButton").off().on("click", () => encryptOrDecryptConfig("encrypt"));
    $("#decryptConfigButton").off().on("click", () => encryptOrDecryptConfig("decrypt"));

    if (CasActuatorEndpoints.configurationMetadata()) {
        await loadConfigurationMetadata();
        await populateConfigurationNameSelectOptions();

        const configSearchResultsTable = $("#configSearchResultsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#configSearchResultsTable tr").addClass("mdc-data-table__row");
                $("#configSearchResultsTable td").addClass("mdc-data-table__cell").addClass("text-wrap");
            }
        });
        configSearchResultsTable.clear();

        $("button[name=configSearchButton]").off().on("click", () => {
            $("#configSearchResults").hide();
            configSearchResultsTable.clear();

            const form = document.getElementById("fmConfigSearch");
            if (!form.reportValidity()) {
                return false;
            }
            const searchQuery = $("#configSearchQuery").val();
            Swal.fire({
                icon: "info",
                title: `Fetching Results`,
                text: "Please wait while configuration metadata repository is consulted to find matches...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });
            $.get(`${CasActuatorEndpoints.configurationMetadata()}/${searchQuery}`, response => {
                for (const entry of response) {
                    configSearchResultsTable.row.add({
                        0: entry.id,
                        1: entry.description
                    });
                }
                $("#configSearchResults").show();
                configSearchResultsTable.draw();
            })
                .fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                })
                .always(() => Swal.close());
        });
    }
}
