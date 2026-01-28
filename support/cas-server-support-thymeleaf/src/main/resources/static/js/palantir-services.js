async function initializeServicesOperations() {
    const columnDefinitions = [
        {visible: true, targets: 0},
        {visible: true, targets: 1},
        {visible: true, targets: 2},
        {visible: true, targets: 3},
        {visible: true, targets: 4},
        {visible: false, targets: 5}
    ];

    const applicationsTable = $("#applicationsTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        columnDefs: columnDefinitions,
        drawCallback: settings => {
            $("#applicationsTable tr").addClass("mdc-data-table__row");
            $("#applicationsTable td").addClass("mdc-data-table__cell");
        }
    });
    applicationsTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));

    const saml2MetadataProvidersTable = $("#saml2MetadataProvidersTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        columnDefs: [
            {width: "15%", targets: 0},
            {width: "35%", targets: 1},
            {width: "50%", targets: 2}
        ],
        drawCallback: settings => {
            $("#saml2MetadataProvidersTable tr").addClass("mdc-data-table__row");
            $("#saml2MetadataProvidersTable td").addClass("mdc-data-table__cell");
        }
    });
    saml2MetadataProvidersTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));

    $("#entityHistoryTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "5%", targets: 0},
            {width: "25%", targets: 1},
            {visible: false, targets: 2}
        ],
        drawCallback: settings => {
            $("#entityHistoryTable tr").addClass("mdc-data-table__row");
            $("#entityHistoryTable td").addClass("mdc-data-table__cell");
        }
    });

    await fetchServices();
    await initializeFooterButtons();

    let serviceDefinitionsEntries = "";
    for (let type in serviceDefinitions) {
        serviceDefinitionsEntries += `<h3>${type} Templates</h3>`;
        const entries = serviceDefinitions[type];
        serviceDefinitionsEntries += "<div>";
        for (const entry of entries) {
            const definition = JSON.parse(entry);
            serviceDefinitionsEntries += `<a class="mr-4" name="serviceDefinitionEntry" title="${definition.description}" data-type="${type}" data-id="${definition.id}" href="#">${definition.name}-${definition.id}</a>`;
        }
        serviceDefinitionsEntries += "</div>";
    }
    $("#serviceTemplatesContainer")
        .html(serviceDefinitionsEntries)
        .accordion({
            collapsible: true,
            heightStyle: "content"
        })
        .tooltip();

    $("a[name=serviceDefinitionEntry]").off().on("click", function () {
        let type = $(this).data("type");
        let id = $(this).data("id");

        const entries = serviceDefinitions[type];
        for (const entry of entries) {
            const definition = JSON.parse(entry);
            if (definition.id === id) {
                const serviceEditor = initializeAceEditor("serviceEditor", "json");
                serviceEditor.setReadOnly(false);
                serviceEditor.setValue(JSON.stringify(definition, null, 2));
                serviceEditor.gotoLine(1);
            }
        }
    });

    if (Object.entries(serviceDefinitionsEntries).length > 0) {
        $("#serviceTemplatesContainer").show();
    } else {
        $("#serviceTemplatesContainer").hide();
    }
}

async function initializeServiceButtons() {
    const serviceEditor = initializeAceEditor("serviceEditor");
    let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("editServiceDialog"));

    if (CasActuatorEndpoints.registeredServices()) {
        const entityHistoryTable = $("#entityHistoryTable").DataTable();
        $("button[name=viewEntityHistory]").off().on("click", function () {
            let serviceId = $(this).parent().attr("serviceId");
            $.get(`${CasActuatorEndpoints.entityHistory()}/registeredServices/${serviceId}`, response => {
                entityHistoryTable.clear();

                const editor = initializeAceEditor("entityHistoryEditor", "json");
                editor.setValue("");
                editor.setReadOnly(true);

                for (const item of response) {
                    entityHistoryTable.row.add({
                        0: `<code>${item.id}</code>`,
                        1: `<code>${item.date}</code>`,
                        2: `${JSON.stringify(item.entity, null, 4)}`
                    });
                }

                entityHistoryTable.draw();
                entityHistoryTable.on("click", "tbody tr", function () {
                    let data = entityHistoryTable.row(this).data();
                    editor.setValue(data[2]);
                    editor.gotoLine(1);
                    editor.setReadOnly(true);
                });

                if (response.length > 0) {
                    const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewEntityHistoryDialog"));
                    dialog["open"]();
                } else {
                    Swal.fire("No History!", "There are no changes recorded for this application definition.", "info");
                }

            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });

        });

    }

    $("button[name=viewEntityChangelog]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");

        $.get(`${CasActuatorEndpoints.entityHistory()}/registeredServices/${serviceId}/changelog`, response => {
            const editor = initializeAceEditor("entityChangelogEditor", "text");
            editor.setValue(response);
            editor.setReadOnly(true);
            editor.gotoLine(1);
            const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewEntityChangelogDialog"));
            dialog["open"]();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    });

    $("button[name=deleteService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (CasActuatorEndpoints.registeredServices()) {
            Swal.fire({
                title: "Are you sure you want to delete this entry?",
                text: "Once deleted, you may not be able to recover this entry.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.registeredServices()}/${serviceId}`,
                            type: "DELETE",
                            headers: {"Content-Type": "application/json"},
                            success: response => {
                                let nearestTr = $(this).closest("tr");
                                let applicationsTable = $("#applicationsTable").DataTable();
                                applicationsTable.row(nearestTr).remove().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error deleting resource:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=editService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (CasActuatorEndpoints.registeredServices()) {
            $.get(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, response => {
                const value = JSON.stringify(response, null, 4);
                serviceEditor.setValue(value, -1);
                serviceEditor.gotoLine(1);
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", false);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    });

    $("button[name=saveService],button[name=saveServiceWizard]").off().on("click", function () {
        if (CasActuatorEndpoints.registeredServices()) {
            let isNewService = false;
            let value = "";

            const saveButton = $(this);
            switch (saveButton.attr("name")) {
            case "saveServiceWizard":
                isNewService = true;
                const wizardEditor = initializeAceEditor("wizardServiceEditor");
                value = wizardEditor.getValue();
                break;
            case "saveService":
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                isNewService = $(editServiceDialogElement).attr("newService") === "true";
                value = serviceEditor.getValue();
                break;
            }

            Swal.fire({
                title: `Are you sure you want to ${isNewService ? "create" : "update"} this entry?`,
                text: "Once updated, you may not be able to revert this entry.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.registeredServices()}`,
                            type: isNewService ? "POST" : "PUT",
                            contentType: "application/json",
                            data: value,
                            success: async response => {
                                editServiceDialog["close"]();
                                await fetchServices(() => {
                                    let newServiceId = response.id;
                                    $("#applicationsTable tr").removeClass("selected");

                                    $(`#applicationsTable tr td span[serviceId=${newServiceId}]`).each(function () {
                                        $(this).closest("tr").addClass("selected");
                                    });
                                    updateNavigationSidebar();
                                });
                            },
                            error: (xhr, status, error) => {
                                console.error("Update failed:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=copyService]").off().on("click", function () {
        if (CasActuatorEndpoints.registeredServices()) {
            let serviceId = $(this).parent().attr("serviceId");
            $.get(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, response => {
                let clone = {...response};
                clone.serviceId = "...";
                clone.name = `...`;
                delete clone.id;

                ["clientId", "clientSecret", "metadataLocation"].forEach(entry => {
                    if (Object.hasOwn(clone, entry)) {
                        clone[entry] = `...`;
                    }
                });
                const value = JSON.stringify(clone, null, 4);
                serviceEditor.setValue(value, -1);
                serviceEditor.gotoLine(1);
                serviceEditor.findAll("...", {regExp: false});

                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", true);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    });
}

async function fetchServices(callback) {
    if (CasActuatorEndpoints.registeredServices()) {
        $.get(CasActuatorEndpoints.registeredServices(), response => {
            let serviceCountByType = [0, 0, 0, 0, 0];
            let applicationsTable = $("#applicationsTable").DataTable();
            applicationsTable.clear();

            let saml2MetadataProvidersTable = $("#saml2MetadataProvidersTable").DataTable();
            saml2MetadataProvidersTable.clear();

            for (const service of response[1]) {
                let icon = "mdi-web-box";
                const serviceClass = service["@class"];
                if (serviceClass.includes("CasRegisteredService")) {
                    icon = "mdi-alpha-c-box-outline";
                    serviceCountByType[0]++;
                } else if (serviceClass.includes("SamlRegisteredService")) {
                    icon = "mdi-alpha-s-box-outline";
                    serviceCountByType[1]++;
                } else if (serviceClass.includes("OAuthRegisteredService")) {
                    icon = "mdi-alpha-o-circle-outline";
                    serviceCountByType[2]++;
                } else if (serviceClass.includes("OidcRegisteredService")) {
                    icon = "mdi-alpha-o-box-outline";
                    serviceCountByType[3]++;
                } else if (serviceClass.includes("WSFederationRegisteredService")) {
                    icon = "mdi-alpha-w-box-outline";
                    serviceCountByType[4]++;
                }

                let serviceDetails = `<span serviceId="${service.id}" title='${service.name}'>${service.name}</span>`;
                serviceDetails += "<p>";
                if (service.informationUrl) {
                    serviceDetails += `<a target="_blank" rel="noopener" href='${service.informationUrl}'>Information URL</a>`;
                }
                if (service.privacyUrl) {
                    serviceDetails += `&nbsp;<a target="_blank" rel="noopener" href='${service.privacyUrl}'>Privacy URL</a>`;
                }

                let serviceIdDetails = `<span serviceId='${service.id}' class="text-wrap">${service.serviceId}</span>`;
                if (serviceClass.includes("SamlRegisteredService")) {
                    serviceIdDetails += `<br><p><a href='${service.metadataLocation}' target='_blank'>Metadata Location</a>`;
                }

                let serviceButtons = `
                 <button type="button" name="editService" href="#" serviceId='${service.id}'
                        title="Edit Service Definition"
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="deleteService" href="#" serviceId='${service.id}'
                        title="Delete Service Definition"
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="copyService" href="#" serviceId='${service.id}'
                        title="Copy Service Definition"
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                </button>
                `;
                if (CasActuatorEndpoints.entityHistory()) {
                    serviceButtons += `
                    <button type="button" name="viewEntityHistory" href="#" serviceId='${service.id}'
                            title="View Change History"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-history min-width-32x" aria-hidden="true"></i>
                    </button>
                    <button type="button" name="viewEntityChangelog" href="#" serviceId='${service.id}'
                            title="View Change Log"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-delta min-width-32x" aria-hidden="true"></i>
                    </button>
                    `;
                }

                applicationsTable.row.add({
                    0: `<i serviceId='${service.id}' title='${serviceClass}' class='mdi ${icon}'></i>`,
                    1: `${serviceDetails}`,
                    2: `${serviceIdDetails}`,
                    3: `<span serviceId='${service.id}' class="text-wrap">${service.description ?? ""}</span>`,
                    4: `<span serviceId='${service.id}'>${serviceButtons.trim()}</span>`,
                    5: `${service.id}`
                });

                let metadataSourcesCount = 0;
                if (serviceClass.includes("SamlRegisteredService")) {
                    const metadataLocation = service.metadataLocation;
                    saml2MetadataProvidersTable.row.add({
                        0: `<span serviceId='${service.id}' class="text-wrap">${service.id ?? ""}</span>`,
                        1: `<span serviceId='${service.id}' class="text-wrap">${service.name ?? ""}</span>`,
                        2: metadataLocation
                    });
                    metadataSourcesCount++;
                }
                $("#saml2metadataproviders").toggle(metadataSourcesCount > 0);
            }

            applicationsTable.on("draw", async () => {
                await initializeServiceButtons();
            });

            applicationsTable.search("").draw();
            saml2MetadataProvidersTable.search("").draw();

            servicesChart.data.datasets[0].data = serviceCountByType;
            servicesChart.update();

            if (callback !== undefined) {
                callback(applicationsTable);
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}

async function initializeFooterButtons() {
    $("button[name=copyServiceDefinitionWizard]").off().on("click", () => {
        const editor = initializeAceEditor("wizardServiceEditor");
        copyToClipboard(editor.getValue());
    });

    $("button[name=validateServiceWizard]").off().on("click", () => {
        const $accordion = $("#editServiceWizardMenu");
        let valid = true;
        const originalIndex = $("#editServiceWizardMenu").accordion("option", "active");
        $("#editServiceWizardMenu .ui-accordion-header:visible").each(function () {
            const $header = $(this);
            const index = $accordion
                .find(".ui-accordion-header")
                .index($header);
            $accordion.accordion("option", "active", index);

            $("#editServiceWizardForm .ui-accordion-content:visible input:visible").each(function () {
                const input = $(this);
                valid = input.get(0).checkValidity();
                if (!valid) {
                    input.get(0).reportValidity();
                    return false;
                }
            });
            if (!valid) {
                return false;
            }
        });
        if (valid) {
            const currentIndex = $("#editServiceWizardMenu").accordion("option", "active");
            if (originalIndex !== currentIndex) {
                $("#editServiceWizardMenu").accordion("option", "active", originalIndex);
            }
            if (CasActuatorEndpoints.registeredServices()) {
                const editor = initializeAceEditor("wizardServiceEditor");
                $.ajax({
                    url: `${CasActuatorEndpoints.registeredServices()}/validate`,
                    type: "POST",
                    contentType: "application/json",
                    data: editor.getValue(),
                    success: response => {
                        const message = `
                            The given application definition is valid and can be parsed by CAS. Please note that validity
                            does not imply behavioral correctness. It only indicates that the generated
                            application definition adheres to the expected schema and structure required by CAS
                            and can be stored successfully.
                        `;
                        Swal.fire("Success", message, "info");
                    },
                    error: (xhr, status, error) => {
                        console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                        displayBanner(xhr);
                    }
                });
            }
        }
    });

    $("button[name=newServiceWizard]").off().on("click", () => {
        openRegisteredServiceWizardDialog();
    });

    $("button[name=newServicePlain]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceDialogElement);
            const editor = initializeAceEditor("serviceEditor", "json");
            editor.setValue("");
            editor.gotoLine(1);

            $(editServiceDialogElement).attr("newService", true);
            editServiceDialog["open"]();
        }
    });

    $("button[name=importService]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            $("#serviceFileInput").click();
            $("#serviceFileInput").change(event =>
                Swal.fire({
                    title: "Are you sure you want to import this entry?",
                    text: "Once imported, the entry should take immediate effect.",
                    icon: "question",
                    showConfirmButton: true,
                    showDenyButton: true
                })
                    .then((result) => {
                        if (result.isConfirmed) {
                            const file = event.target.files[0];
                            const reader = new FileReader();
                            reader.readAsText(file);
                            reader.onload = e => {
                                const fileContent = e.target.result;

                                $.ajax({
                                    url: `${CasActuatorEndpoints.registeredServices()}`,
                                    type: "PUT",
                                    contentType: "application/json",
                                    data: fileContent,
                                    success: response => $("#reloadAll").click(),
                                    error: (xhr, status, error) => {
                                        console.error("File upload failed:", error);
                                        displayBanner(xhr);
                                    }
                                });
                            };
                        }
                    }));
        }
    });

    $("button[name=exportService]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            let serviceId = $(exportServiceButton).attr("serviceId");
            fetch(`${CasActuatorEndpoints.registeredServices()}/export/${serviceId}`)
                .then(response => {
                    const filename = response.headers.get("filename");
                    response.blob().then(blob => {
                        const link = document.createElement("a");
                        link.href = window.URL.createObjectURL(blob);
                        link.download = filename;
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                    });

                })
                .catch(error => console.error("Error fetching file:", error));
        }
    });

    $("button[name=exportAll]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            fetch(`${CasActuatorEndpoints.registeredServices()}/export`)
                .then(response => {
                    const filename = response.headers.get("filename");
                    response.blob().then(blob => {
                        const link = document.createElement("a");
                        link.href = window.URL.createObjectURL(blob);
                        link.download = filename;
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                    });

                })
                .catch(error => console.error("Error fetching file:", error));
        }
    });
}
