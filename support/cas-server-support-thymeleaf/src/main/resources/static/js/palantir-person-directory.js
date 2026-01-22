async function initializePersonDirectoryOperations() {
    const personDirectoryTable = $("#personDirectoryTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#personDirectoryTable tr").addClass("mdc-data-table__row");
            $("#personDirectoryTable td").addClass("mdc-data-table__cell");
        }
    });

    const attributeDefinitionsTable = $("#attributeDefinitionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#attributeDefinitionsTable tr").addClass("mdc-data-table__row");
            $("#attributeDefinitionsTable td").addClass("mdc-data-table__cell");
        }
    });

    const attributeRepositoriesTable = $("#attributeRepositoriesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#attributeRepositoriesTable tr").addClass("mdc-data-table__row");
            $("#attributeRepositoriesTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=personDirectoryClearButton]").off().on("click", () => {
        if (CasActuatorEndpoints.personDirectory()) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            Swal.fire({
                title: `Are you sure you want to delete the cache for ${username}?`,
                text: `Once the cached entry is removed, attribute repositories would be forced to fetch attributes for ${username} again`,
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        personDirectoryTable.clear();
                        $.ajax({
                            url: `${CasActuatorEndpoints.personDirectory()}/cache/${username}`,
                            type: "DELETE",
                            contentType: "application/json",
                            success: (response, status, xhr) => {

                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=personDirectoryButton]").off().on("click", () => {
        if (CasActuatorEndpoints.personDirectory()) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            personDirectoryTable.clear();
            $.ajax({
                url: `${CasActuatorEndpoints.personDirectory()}/cache/${username}`,
                type: "GET",
                contentType: "application/json",
                success: (response, status, xhr) => {
                    for (const [key, values] of Object.entries(response.attributes)) {
                        personDirectoryTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `<code>${values}</code>`
                        });
                    }
                    personDirectoryTable.draw();
                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                }
            });
        }
    });

    attributeDefinitionsTable.clear();
    let attributeDefinitions = 0;
    if (CasActuatorEndpoints.attributeDefinitions()) {
        $.get(CasActuatorEndpoints.attributeDefinitions(), response => {
            for (const definition of response) {
                attributeDefinitionsTable.row.add({
                    0: `<code>${definition.key ?? "N/A"}</code>`,
                    1: `<code>${definition.name ?? "N/A"}</code>`,
                    2: `<code>${definition.scoped ?? "false"}</code>`,
                    3: `<code>${definition.encrypted ?? "false"}</code>`,
                    4: `<code>${definition.singleValue ?? "false"}</code>`,
                    5: `<code>${definition.attribute ?? "N/A"}</code>`,
                    6: `<code>${definition.patternFormat ?? "N/A"}</code>`,
                    7: `<code>${definition.canonicalizationMode ?? "N/A"}</code>`,
                    8: `<code>${definition.flattened ?? "false"}</code>`,
                    9: `<code>${definition?.friendlyName ?? "N/A"}</code>`,
                    10: `<code>${definition?.urn ?? "N/A"}</code>`
                });
                attributeDefinitions++;
            }
            if (attributeDefinitions > 0) {
                attributeDefinitionsTable.draw();
                showElements($("#attributeDefinitionsTab").parent());
            } else {
                hideElements($("#attributeDefinitionsTab").parent());
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    } else {
        hideElements($("#attributeDefinitionsTab").parent());
    }

    attributeRepositoriesTable.clear();
    let attributeRepositories = 0;
    if (CasActuatorEndpoints.personDirectory()) {
        $.get(`${CasActuatorEndpoints.personDirectory()}/repositories`, response => {
            for (const definition of response) {
                attributeRepositoriesTable.row.add({
                    0: `<code>${definition.id ?? "N/A"}</code>`,
                    1: `<code>${definition.order ?? "0"}</code>`,
                    2: `<code>${JSON.stringify(definition.tags)}</code>`
                });
                attributeRepositories++;
            }
            attributeRepositoriesTable.draw();
            $("#attributeRepositoriesTab").toggle(attributeRepositories > 0);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}
