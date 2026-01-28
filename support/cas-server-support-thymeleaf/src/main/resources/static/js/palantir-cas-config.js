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
    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
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
    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
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

    if (mutablePropertySources.length === 1) {
        deletePropertiesFromSource(mutablePropertySources[0]);
    } else {
        Swal.fire({
            title: "Which property source do you want to clear?",
            input: "select",
            icon: "question",
            inputOptions: mutablePropertySources,
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
        inputOptions: mutablePropertySources,
        html: `
            <p class="text-justify">Properties will be imported and applied to the CAS server configuration. You may import multiple files at once.
            Supported file formats are .properties and .yml/.yaml files. You may choose the target property source to which the properties will be applied.</p>
        `,
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then((result) => {
        if (result.isConfirmed) {
            const propertySource = mutablePropertySources[Number(result.value)];

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
        width: 600,
        height: "auto",
        buttons: {
            OK: async function () {
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

            $("#newConfigPropertyName").val(config.name ?? "");
            $("#newConfigPropertyValue").val(config.value ?? "");
            $("#propertySourcesSelect").val(config.propertySource ?? "");
            if (config.updateEntry) {
                $("#newConfigPropertyName").parent().hide();
                $("#propertySourcesSection").hide();
                $("#newConfigPropertyValue").focus().select();
            } else {
                $("#propertySourcesSection").show();
                $("#newConfigPropertyName").parent().show();
                $("#newConfigPropertyName").focus();
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

                if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
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


                if (mutablePropertySources.some(entry => source.name.endsWith(entry))) {
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
    if (mutablePropertySourcesAvailable) {
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
        $("#configEncryptionResult").addClass("d-none");

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
                $("#configEncryptionResult").removeClass("d-none");
            }).fail((xhr, status, error) => {
                displayBanner(xhr);
                $("#configEncryptionResult").addClass("d-none");
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

    $("#encryptConfigButton").off().on("click", () => encryptOrDecryptConfig("encrypt"));
    $("#decryptConfigButton").off().on("click", () => encryptOrDecryptConfig("decrypt"));

    if (CasActuatorEndpoints.configurationMetadata()) {

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
