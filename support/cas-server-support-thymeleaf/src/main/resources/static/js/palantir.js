/**
 * Internal Functions
 */
function fetchServices() {
    $.get(`${casServerPrefix}/actuator/registeredServices`, response => {

        let applicationsTable = $("#applicationsTable").DataTable();
        applicationsTable.clear();
        for (const service of response[1]) {
            let icon = "mdi-web-box";
            const serviceClass = service["@class"];
            if (serviceClass.includes("CasRegisteredService")) {
                icon = "mdi-alpha-c-box-outline";
            } else if (serviceClass.includes("SamlRegisteredService")) {
                icon = "mdi-alpha-s-box-outline";
            } else if (serviceClass.includes("OAuthRegisteredService")) {
                icon = "mdi-alpha-o-circle-outline";
            } else if (serviceClass.includes("OidcRegisteredService")) {
                icon = "mdi-alpha-o-box-outline";
            }

            let serviceDetails = `<span serviceId="${service.id}" title='${service.name}'>${service.name}</span>`;
            serviceDetails += "<p>";
            if (service.informationUrl) {
                serviceDetails += `<a target="_blank" href='${service.informationUrl}'>Information URL</a>`;
            }
            if (service.privacyUrl) {
                serviceDetails += `&nbsp;<a target="_blank" href='${service.privacyUrl}'>Privacy URL</a>`;
            }

            let serviceButtons = `
                                 <button type="button" name="editService" href="#" serviceId='${service.id}'
                                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                                    <i class="mdi mdi-pencil fas fa-eye min-width-32x" aria-hidden="true"></i>
                                </button>
                                <button type="button" name="deleteService" href="#" serviceId='${service.id}'
                                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                                    <i class="mdi mdi-delete fas fa-eye min-width-32x" aria-hidden="true"></i>
                                </button>
                            `;
            applicationsTable.row.add({
                0: `<i title='${serviceClass}' class='mdi ${icon}'></i>`,
                1: `${serviceDetails}`,
                2: `<span serviceId='${service.id}' class="text-wrap">${service.serviceId}</span>`,
                3: `<span serviceId='${service.id}' class="text-wrap">${service.description ?? ""}</span>`,
                4: `<span serviceId='${service.id}'>${serviceButtons.trim()}</span>`
            });
        }
        applicationsTable.draw();
        initializeServiceButtons();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
    });
}

function initializeFooterButtons() {
    let newServiceButton = document.getElementById("newService");
    newServiceButton.addEventListener("click", event => {

        const editServiceDialogElement = document.getElementById("editServiceDialog");
        let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceDialogElement);
        const editor = initializeAceEditor("serviceEditor");
        editor.setValue("");
        editor.gotoLine(1);

        $(editServiceDialogElement).attr("newService", true);
        editServiceDialog["open"]();
        event.preventDefault();
    }, false);

    let importServiceButton = document.getElementById("importService");
    importServiceButton.addEventListener("click", event => {
        $("#serviceFileInput").click();

        $("#serviceFileInput").change(event => {
            const file = event.target.files[0];

            const reader = new FileReader();
            reader.readAsText(file);
            reader.onload = e => {
                const fileContent = e.target.result;
                console.log("File content:", fileContent);

                $.ajax({
                    url: `${casServerPrefix}/actuator/registeredServices`,
                    type: "PUT",
                    contentType: "application/json",
                    data: fileContent,
                    success: response => {
                        console.log("File upload successful:", response);
                        $("#reloadAll").click();
                    },
                    error: (xhr, status, error) => {
                        console.error("File upload failed:", error);
                    }
                });
            };

        });

        event.preventDefault();
    }, false);

    let exportServiceButtons = document.getElementsByName("exportService");
    exportServiceButtons.forEach(exportServiceButton => {
        exportServiceButton.addEventListener("click", event => {
            let serviceId = $(exportServiceButton).attr("serviceId");
            fetch(`${casServerPrefix}/actuator/registeredServices/export/${serviceId}`)
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
            event.preventDefault();
        }, false);
    });

    let exportAllButton = document.getElementById("exportAll");
    exportAllButton.addEventListener("click", event => {
        fetch(`${casServerPrefix}/actuator/registeredServices/export`)
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
        event.preventDefault();
    }, false);
}

/**
 * Initialization Functions
 */
async function initializeAccessStrategyOperations() {
    const accessStrategyEditor = initializeAceEditor("accessStrategyEditor");
    accessStrategyEditor.setReadOnly(true);
    let accessStrategyButton = document.getElementById("accessStrategyButton");
    accessStrategyButton.addEventListener("click", event => {
        $("#serviceAccessResultDiv").hide();
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
            url: `${casServerPrefix}/actuator/serviceAccess`,
            type: "POST",
            contentType: "application/x-www-form-urlencoded",
            data: $.param(renamedData),
            success: (response, status, xhr) => {
                $("#accessStrategyEditorContainer").removeClass("d-none");
                $("#serviceAccessResultDiv")
                    .show()
                    .addClass("banner-success")
                    .removeClass("banner-danger");
                $("#serviceAccessResultDiv #serviceAccessResult").text(`Status ${xhr.status}: Service is authorized.`);
                accessStrategyEditor.setValue(JSON.stringify(response, null, 2));
                accessStrategyEditor.gotoLine(1);
            },
            error: (xhr, status, error) => {
                $("#serviceAccessResultDiv")
                    .show()
                    .removeClass("banner-success")
                    .addClass("banner-danger");
                $("#accessStrategyEditorContainer").addClass("d-none");
                $("#serviceAccessResultDiv #serviceAccessResult").text(`Status ${xhr.status}: Service is unauthorized.`);
            }
        });
        event.preventDefault();
    }, false);
}

async function initializeScheduledTasksOperations() {
    const groupColumn = 0;
    const scheduledtasks = $("#scheduledTasksTable").DataTable({
        pageLength: 25,
        columnDefs: [{visible: false, targets: groupColumn}],
        order: [groupColumn, "asc"],
        drawCallback: settings => {
            $("#scheduledTasksTable tr").addClass("mdc-data-table__row");
            $("#scheduledTasksTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(groupColumn, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function addScheduledTaskCategory(groupName, items) {
        if (items !== undefined && Array.isArray(items)) {
            for (const group of items) {
                const flattened = flattenJSON(group);
                for (const [key, value] of Object.entries(flattened)) {
                    const target = flattened["runnable.target"];
                    if (target !== value) {
                        scheduledtasks.row.add({
                            0: `<code>${camelcaseToTitleCase(groupName)} / ${getLastTwoWords(target)}</code>`,
                            1: `<code>${key}</code>`,
                            2: `<code>${value}</code>`
                        });
                    }
                }
            }
        }
    }

    $.get(`${casServerPrefix}/actuator/scheduledtasks`, response => {
        console.log(response);
        scheduledtasks.clear();
        for (const group of Object.keys(response)) {
            addScheduledTaskCategory(group, response[group]);
        }
        scheduledtasks.draw();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
    });
}

async function initializeTicketsOperations() {
    const ticketEditor = initializeAceEditor("ticketEditor");
    ticketEditor.setReadOnly(true);

    let searchTicketButton = document.getElementById("searchTicketButton");
    searchTicketButton.addEventListener("click", event => {
        const ticket = document.getElementById("ticket");
        if (!ticket.checkValidity()) {
            ticket.reportValidity();
            return false;
        }
        const ticketId = $(ticket).val();
        const type = $("#ticketDefinitions .mdc-list-item--selected").attr("data-value").trim();
        if (ticket && type) {
            const decode = new mdc.switchControl.MDCSwitch(document.getElementById("decodeTicketButton")).selected;
            ticketEditor.setValue("");
            $.get(`${casServerPrefix}/actuator/ticketRegistry/query?type=${type}&id=${ticketId}&decode=${decode}`,
                response => {
                    ticketEditor.setValue(JSON.stringify(response, null, 2));
                    ticketEditor.gotoLine(1);
                })
                .fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                });
        }
        event.preventDefault();
    }, false);

    let cleanTicketsButton = document.getElementById("cleanTicketsButton");
    cleanTicketsButton.addEventListener("click", event => {
        $.ajax({
            url: `${casServerPrefix}/actuator/ticketRegistry/clean`,
            type: "DELETE",
            success: response => {
                ticketEditor.setValue(JSON.stringify(response, null, 2));
                ticketEditor.gotoLine(1);
            },
            error: (xhr, status, error) => {
                console.log(`Error: ${status} / ${error} / ${xhr.responseText}`);
            }
        });
        event.preventDefault();
    }, false);


    $.get(`${casServerPrefix}/actuator/ticketRegistry/ticketCatalog`, response => {
        console.log(response);
        response.forEach(entry => {
            let item = `
                            <li class="mdc-list-item" data-value='${entry.prefix}' role="option">
                                <span class="mdc-list-item__ripple"></span>
                                <span class="mdc-list-item__text">${entry.apiClass}</span>
                            </li>
                        `;
            $("#ticketDefinitions").append($(item.trim()));
        });
        const ticketDefinitions = new mdc.select.MDCSelect(document.getElementById("ticketDefinitionsSelect"));
        ticketDefinitions.selectedIndex = 0;
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
    });

    const groupColumn = 0;
    const ticketCatalogTable = $("#ticketCatalogTable").DataTable({
        pageLength: 25,
        columnDefs: [
            {visible: false, targets: groupColumn}
        ],

        order: [groupColumn, "desc"],
        drawCallback: settings => {
            $("#ticketCatalogTable tr").addClass("mdc-data-table__row");
            $("#ticketCatalogTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(groupColumn, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    $.get(`${casServerPrefix}/actuator/ticketRegistry/ticketCatalog`, response => {
        console.log(response);
        ticketCatalogTable.clear();
        for (const definition of response) {
            const flattened = flattenJSON(definition);
            for (const [key, value] of Object.entries(flattened)) {
                ticketCatalogTable.row.add({
                    0: `<code>${definition.prefix}</code>`,
                    1: `<code>${key}</code>`,
                    2: `<code>${value}</code>`
                });
            }
        }
        ticketCatalogTable.draw();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
    });
}

async function initializeSystemOperations() {
    function configureHealthChart(systemHealthChart) {
        $.get(`${casServerPrefix}/actuator/health`, response => {
            console.log(response);
            const payload = {
                labels: [],
                data: [],
                colors: []
            };
            Object.keys(response.components).forEach(key => {
                payload.labels.push(key.charAt(0).toUpperCase() + key.slice(1).toLowerCase());
                payload.data.push(response.components[key].status === "UP" ? 1 : 0);
                payload.colors.push(response.components[key].status === "UP" ? "rgb(5, 166, 31)" : "rgba(166, 45, 15)");
            });
            systemHealthChart.data.labels = payload.labels;
            systemHealthChart.data.datasets[0].data = payload.data;
            systemHealthChart.data.datasets[0].backgroundColor = payload.colors;
            systemHealthChart.data.datasets[0].borderColor = payload.colors;
            systemHealthChart.options.plugins.legend.labels.generateLabels = (chart => {
                const originalLabels = Chart.defaults.plugins.legend.labels.generateLabels(chart);
                originalLabels.forEach(label => {
                    label.fillStyle = response.status === "UP" ? "rgb(5, 166, 31)" : "rgba(166, 45, 15)";
                    label.lineWidth = 0;
                });
                return originalLabels;
            });

            systemHealthChart.update();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });
    }

    function configureStatistics(statisticsChart) {
        $.get(`${casServerPrefix}/actuator/statistics`, response => {
            const expired = response.expiredTickets;
            const valid = response.validTickets;
            statisticsChart.data.datasets[0].data = [valid, expired];
            statisticsChart.update();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });
    }

    function fetchSystemData(callback) {
        $.get(`${casServerPrefix}/actuator/info`, response => callback(response)).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });
    }

    function configureSystemData(memoryChart) {
        fetchSystemData(response => {
            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);
            console.log("Memory: ", maximum, total, free);
            memoryChart.data.datasets[0].data = [maximum, total, free];
            memoryChart.update();
        });
    }

    const systemTable = $("#systemTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#systemTable tr").addClass("mdc-data-table__row");
            $("#systemTable td").addClass("mdc-data-table__cell");
        }
    });

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === 1) {
            fetchSystemData(response => {
                const flattened = flattenJSON(response);
                systemTable.clear();

                for (const [key, value] of Object.entries(flattened)) {
                    systemTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${value}</code>`
                    });
                }
                systemTable.draw();
            });
        }
    });

    const memoryChartCtx = document.getElementById("memoryChart").getContext("2d");
    const memoryChart = new Chart(memoryChartCtx, {
        type: "bar",
        data: {
            labels: ["Total Memory", "Free Memory", "Used Memory"],
            datasets: [{
                label: "Memory (GB)",
                data: [0, 0, 0],
                fill: true,
                backgroundColor: ["rgba(54, 162, 235, 0.2)", "rgba(75, 192, 192, 0.2)", "rgba(255, 99, 132, 0.2)"],
                borderColor: ["rgba(54, 162, 235, 1)", "rgba(75, 192, 192, 1)", "rgba(255, 99, 132, 1)"],
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: true
                },
                x: {
                    stacked: true,
                    grid: {
                        offset: true
                    }
                }
            }
        }
    });

    const statisticsChartCtx = document.getElementById("statisticsChart").getContext("2d");
    const statisticsChart = new Chart(statisticsChartCtx, {
        type: "bar",
        data: {
            labels: ["Current Tickets", "Expired (Removed) Tickets"],
            datasets: [{
                label: "Ticket Registry",
                data: [0, 0],
                fill: true,
                backgroundColor: ["rgba(75, 192, 192, 0.2)", "rgba(255, 99, 132, 0.2)"],
                borderColor: ["rgba(75, 192, 192, 1)", "rgba(255, 99, 132, 1)"],
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    stacked: true,
                    grid: {
                        offset: true
                    }
                }
            }
        }
    });

    const systemHealthCtx = document.getElementById("systemHealthChart").getContext("2d");
    const systemHealthChart = new Chart(systemHealthCtx, {
        type: "bar",
        data: {
            datasets: [{
                label: "System Health",
                fill: true,
                borderWidth: 2
            }]
        },
        options: {
            indexAxis: "y",
            scales: {
                x: {
                    ticks: {
                        display: false
                    }
                }
            }
        }
    });

    setInterval(() => {
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureSystemData(memoryChart);
        }
    }, 2000);

    setInterval(() => {
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureStatistics(statisticsChart);
        }
    }, 5000);

    setInterval(() => {
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureHealthChart(systemHealthChart);
        }
    }, 5000);

    $.get(`${casServerPrefix}/actuator/casFeatures`, response => {
        console.log(response);
        $("#casFeaturesChipset").empty();
        for (const element of response) {
            let feature = `
                            <div class="mdc-chip" role="row">
                                <div class="mdc-chip__ripple"></div>
                                <span role="gridcell">
                                  <span class="mdc-chip__text">${element.trim()}</span>
                                </span>
                            </div>
                        `.trim();
            $("#casFeaturesChipset").append($(feature));
        }
    });

    configureSystemData(memoryChart);
    configureStatistics(statisticsChart);
    configureHealthChart(systemHealthChart);
}

async function initializeServiceButtons() {
    let deleteServiceButtons = document.getElementsByName("deleteService");
    deleteServiceButtons.forEach(deleteServiceButton => {
        deleteServiceButton.addEventListener("click", event => {
            let caller = event.target || event.srcElement;
            let serviceId = $(caller.parentElement).attr("serviceId");

            let result = confirm("Are you sure you want to delete this entry?");
            if (result) {
                $.ajax({
                    url: `${casServerPrefix}/actuator/registeredServices/${serviceId}`,
                    type: "DELETE",
                    success: response => {
                        console.log("Resource deleted successfully:", response);
                        let nearestTr = $(caller).closest("tr");

                        let applicationsTable = $("#applicationsTable").DataTable();
                        applicationsTable.row(nearestTr).remove().draw();
                    },
                    error: (xhr, status, error) => {
                        console.error("Error deleting resource:", error);
                    }
                });
            }
            event.preventDefault();
        }, false);
    });

    let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("editServiceDialog"));
    let editServiceButtons = document.getElementsByName("editService");
    const editor = initializeAceEditor("serviceEditor");
    editServiceButtons.forEach(editServiceButton => {
        editServiceButton.addEventListener("click", event => {
            let caller = event.target || event.srcElement;

            let serviceId = $(caller.parentElement).attr("serviceId");
            $.get(`${casServerPrefix}/actuator/registeredServices/${serviceId}`, response => {
                const value = JSON.stringify(response, null, 4);
                editor.setValue(value, -1);
                editor.gotoLine(1);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
            });

            const editServiceDialogElement = document.getElementById("editServiceDialog");
            $(editServiceDialogElement).attr("newService", false);
            editServiceDialog["open"]();
            event.preventDefault();

        }, false);
    });

    let saveServiceButton = document.getElementById("saveService");
    saveServiceButton.addEventListener("click", event => {
        let result = confirm("Are you sure you want to update this entry?");
        if (result) {
            const value = editor.getValue();

            const editServiceDialogElement = document.getElementById("editServiceDialog");
            const isNewService = $(editServiceDialogElement).attr("newService") === "true";

            $.ajax({
                url: `${casServerPrefix}/actuator/registeredServices`,
                type: isNewService ? "POST" : "PUT",
                contentType: "application/json",
                data: value,
                success: response => {
                    console.log("Update successful:", response);
                    editServiceDialog["close"]();
                    $("#reloadAll").click();
                },
                error: (xhr, status, error) => {
                    console.error("Update failed:", error);
                }
            });
        }
        event.preventDefault();
    }, false);
}

async function initializeServicesOperations() {
    $("#applicationsTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        columnDefs: [
            {width: "3%", targets: 0},
            {width: "12%", targets: 1},
            {width: "17%", targets: 2},
            {width: "59%", targets: 3},
            {width: "9%", targets: 4}
        ],
        drawCallback: settings => {
            $("#applicationsTable tr").addClass("mdc-data-table__row");
            $("#applicationsTable td").addClass("mdc-data-table__cell");
        }
    });
    fetchServices();
    initializeFooterButtons();
}

async function initializePalantir() {
    try {
        await Promise.all([
            initializeScheduledTasksOperations(),
            initializeServicesOperations(),
            initializeAccessStrategyOperations(),
            initializeTicketsOperations(),
            initializeSystemOperations()
        ]);
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializePalantir().then(r => console.log("Palantir ready!"));
});
