const DEFAULT_INTERVAL = 15000;

/**
 * Internal Functions
 */
const Tabs = {
    APPLICATIONS: 0,
    SYSTEM: 1,
    TICKETS: 2,
    TASKS: 3,
    ACCESS_STRATEGY: 4,
    LOGGING: 5,
    SSO_SESSIONS: 6,
    CONFIGURATION: 7,
    PERSON_DIRECTORY: 8,
    AUTHENTICATION: 9,
    CONSENT: 10,
    PROTOCOLS: 11,
    THROTTLES: 12
};

/**
 * Charts objects.
 */
let servicesChart = null;
let memoryChart = null;
let statisticsChart = null;
let systemHealthChart = null;
let jvmThreadsChart = null;
let httpRequestResponsesChart = null;
let httpRequestsByUrlChart = null;
let auditEventsChart = null;
let threadDumpChart = null;

function fetchServices(callback) {
    if (actuatorEndpoints.registeredservices) {
        $.get(actuatorEndpoints.registeredservices, response => {
            let serviceCountByType = [0, 0, 0, 0, 0];
            let applicationsTable = $("#applicationsTable").DataTable();
            applicationsTable.clear();
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
                    serviceDetails += `<a target="_blank" href='${service.informationUrl}'>Information URL</a>`;
                }
                if (service.privacyUrl) {
                    serviceDetails += `&nbsp;<a target="_blank" href='${service.privacyUrl}'>Privacy URL</a>`;
                }

                let serviceIdDetails = `<span serviceId='${service.id}' class="text-wrap">${service.serviceId}</span>`;
                if (serviceClass.includes("SamlRegisteredService")) {
                    serviceIdDetails += `<br><p><a href='${service.metadataLocation}' target='_blank'>Metadata Location</a>`;
                }

                let serviceButtons = `
                 <button type="button" name="editService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="deleteService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="copyService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                </button>
            `;
                applicationsTable.row.add({
                    0: `<i serviceId='${service.id}' title='${serviceClass}' class='mdi ${icon}'></i>`,
                    1: `${serviceDetails}`,
                    2: `${serviceIdDetails}`,
                    3: `<span serviceId='${service.id}' class="text-wrap">${service.description ?? ""}</span>`,
                    4: `<span serviceId='${service.id}'>${serviceButtons.trim()}</span>`
                });
            }
            applicationsTable.draw();

            servicesChart.data.datasets[0].data = serviceCountByType;
            servicesChart.update();

            if (callback !== undefined) {
                callback(applicationsTable);
            }

            initializeServiceButtons();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }
}

function initializeFooterButtons() {

    $("button[name=newService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceDialogElement);
            const editor = initializeAceEditor("serviceEditor");
            editor.setValue("");
            editor.gotoLine(1);

            $(editServiceDialogElement).attr("newService", true);
            editServiceDialog["open"]();
        }
    });

    $("button[name=importService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            $("#serviceFileInput").click();
            $("#serviceFileInput").change(event =>
                swal({
                    title: "Are you sure you want to import this entry?",
                    text: "Once imported, the entry should take immediate effect.",
                    icon: "warning",
                    buttons: true,
                    dangerMode: true
                })
                    .then((willImport) => {
                        if (willImport) {
                            const file = event.target.files[0];
                            const reader = new FileReader();
                            reader.readAsText(file);
                            reader.onload = e => {
                                const fileContent = e.target.result;
                                console.log("File content:", fileContent);

                                $.ajax({
                                    url: `${actuatorEndpoints.registeredservices}`,
                                    type: "PUT",
                                    contentType: "application/json",
                                    data: fileContent,
                                    success: response => {
                                        console.log("File upload successful:", response);
                                        $("#reloadAll").click();
                                    },
                                    error: (xhr, status, error) => {
                                        console.error("File upload failed:", error);
                                        displayErrorInBanner(xhr);
                                    }
                                });
                            };
                        }
                    }));
        }
    });

    $("button[name=exportService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            let serviceId = $(exportServiceButton).attr("serviceId");
            fetch(`${actuatorEndpoints.registeredservices}/export/${serviceId}`)
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
        if (actuatorEndpoints.registeredservices) {
            fetch(`${actuatorEndpoints.registeredservices}/export`)
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


/**
 * Initialization Functions
 */
async function initializeAccessStrategyOperations() {
    const accessStrategyEditor = initializeAceEditor("accessStrategyEditor");
    accessStrategyEditor.setReadOnly(true);

    $("button[name=accessStrategyButton]").off().on("click", () => {
        if (actuatorEndpoints.serviceaccess) {
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
                url: actuatorEndpoints.serviceaccess,
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
        }
    });
}

function hideErrorInBanner() {
    const snackbar = new mdc.snackbar.MDCSnackbar(document.getElementById("errorBanner"));
    snackbar.close();
}

function displayErrorInBanner(error) {
    let message = "";
    switch (error.status) {
    case 401:
        message = "You are not authorized to access this resource. Are you sure you are authenticated?";
        break;
    case 403:
        message = "You are forbidden from accessing this resource. Are you sure you have the necessary permissions and the entry is correctly regstered with CAS?";
        break;
    case 400:
    case 500:
        message = "Unable to process or accept the request. Check CAS server logs for details.";
        break;
    case 0:
        message = "Unable to contact the CAS server. Are you sure the server is reachable?";
        break;
    default:
        message = `HTTP error: ${error.status}. `;
        break;
    }
    if (error.hasOwnProperty("path")) {
        message += `Unable to make an API call to ${error.path}. Is the endpoint enabled and available?`;
    }
    const snackbar = new mdc.snackbar.MDCSnackbar(document.getElementById("errorBanner"));
    snackbar.labelText = message;
    snackbar.open();
}

function initializeJvmMetrics() {
    function fetchJvmThreadMetric(metricName) {
        return new Promise((resolve, reject) =>
            $.get(`${actuatorEndpoints.metrics}/${metricName}`, response => resolve(response.measurements[0].value)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
                reject(error);
            })
        );
    }

    async function fetchJvmMetrics() {
        const promises = [
            "jvm.threads.daemon",
            "jvm.threads.live",
            "jvm.threads.peak",
            "jvm.threads.started",
            "jvm.threads.states"
        ].map(metric => fetchJvmThreadMetric(metric));
        const results = await Promise.all(promises);
        const numbersArray = results.map(result => Number(result));
        console.log("Fetched JVM metrics:", numbersArray);
        return numbersArray;
    }

    async function fetchThreadDump() {
        return new Promise((resolve, reject) =>
            $.get(actuatorEndpoints.threaddump, response => {
                console.log("Thread dump", response);
                let threadData = {};
                for (const thread of response.threads) {
                    if (!threadData[thread.threadState]) {
                        threadData[thread.threadState] = 0;
                    }
                    threadData[thread.threadState] += 1;
                }
                resolve(threadData);
            }).fail((xhr, status, error) => {
                console.error("Error thread dump:", error);
                displayErrorInBanner(xhr);
                reject(error);
            }));

    }

    if (actuatorEndpoints.metrics) {
        fetchJvmMetrics()
            .then(payload => {
                jvmThreadsChart.data.datasets[0].data = payload;
                jvmThreadsChart.update();
            });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump().then(payload => {
            threadDumpChart.data.labels = Object.keys(payload);
            const values = Object.values(payload);
            threadDumpChart.data.datasets[0] = {
                label: `${values.reduce((sum, value) => sum + value, 0)} Threads`,
                data: values
            };
            threadDumpChart.update();
        });
    }
}

async function initializeScheduledTasksOperations() {

    const threadDumpTable = $("#threadDumpTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "5%", targets: 0},
            {width: "35%", targets: 1},
            {width: "30%", targets: 2},
            {width: "10%", targets: 3},
            {width: "10%", targets: 4},
            {width: "10%", targets: 5},
        ],
        drawCallback: settings => {
            $("#threadDumpTable tr").addClass("mdc-data-table__row");
            $("#threadDumpTable td").addClass("mdc-data-table__cell");
        }
    });


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

    if (actuatorEndpoints.scheduledtasks) {
        $.get(actuatorEndpoints.scheduledtasks, response => {
            console.log(response);
            scheduledtasks.clear();
            for (const group of Object.keys(response)) {
                addScheduledTaskCategory(group, response[group]);
            }
            scheduledtasks.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }

    if (actuatorEndpoints.metrics) {
        initializeJvmMetrics();
        setInterval(() => initializeJvmMetrics(), DEFAULT_INTERVAL);
    }

    function fetchThreadDump() {
        $.get(actuatorEndpoints.threaddump, response => {
            console.log(response);
            threadDumpTable.clear();
            for (const thread of response.threads) {
                threadDumpTable.row.add({
                    0: `<code>${thread.threadId}</code>`,
                    1: `<code>${thread.threadName}</code>`,
                    2: `<code>${thread.threadState}</code>`,
                    3: `<code>${thread.priority}</code>`,
                    4: `<code>${thread.daemon}</code>`,
                    5: `<code>${thread.suspended}</code>`,
                });
            }
            threadDumpTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump();
        setInterval(() => fetchThreadDump(), DEFAULT_INTERVAL);
    }
}

async function initializeTicketsOperations() {
    const ticketEditor = initializeAceEditor("ticketEditor");
    ticketEditor.setReadOnly(true);

    $("button#searchTicketButton").off().on("click", () => {
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
            if (actuatorEndpoints.ticketregistry) {
                $.get(`${actuatorEndpoints.ticketregistry}/query?type=${type}&id=${ticketId}&decode=${decode}`,
                    response => {
                        ticketEditor.setValue(JSON.stringify(response, null, 2));
                        ticketEditor.gotoLine(1);
                    })
                    .fail((xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayErrorInBanner(xhr);
                    });
            }
        }
    });

    $("button#cleanTicketsButton").off().on("click", () => {
        hideErrorInBanner();
        if (actuatorEndpoints.ticketregistry) {
            $.ajax({
                url: `${actuatorEndpoints.ticketregistry}/clean`,
                type: "DELETE",
                success: response => {
                    ticketEditor.setValue(JSON.stringify(response, null, 2));
                    ticketEditor.gotoLine(1);
                },
                error: (xhr, status, error) => {
                    console.log(`Error: ${status} / ${error} / ${xhr.responseText}`);
                    displayErrorInBanner(xhr);
                }
            });
        }

    });

    const ticketCatalogTable = $("#ticketCatalogTable").DataTable({

        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0}
        ],

        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketCatalogTable tr").addClass("mdc-data-table__row");
            $("#ticketCatalogTable td").addClass("mdc-data-table__cell");

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

    if (actuatorEndpoints.ticketregistry) {
        $.get(`${actuatorEndpoints.ticketregistry}/ticketCatalog`, response => {
            console.log(response);
            response.forEach(entry => {
                let item = `
                            <li class="mdc-list-item" data-value='${entry.prefix}' role="option">
                                <span class="mdc-list-item__ripple"></span>
                                <span class="mdc-list-item__text">${entry.apiClass}</span>
                            </li>
                        `;
                $("#ticketDefinitions").append($(item.trim()));

                const flattened = flattenJSON(entry);
                for (const [key, value] of Object.entries(flattened)) {
                    ticketCatalogTable.row.add({
                        0: `<code>${entry.prefix}</code>`,
                        1: `<code>${key}</code>`,
                        2: `<code>${value}</code>`
                    });
                }
                ticketCatalogTable.draw();
            });
            const ticketDefinitions = new mdc.select.MDCSelect(document.getElementById("ticketDefinitionsSelect"));
            ticketDefinitions.selectedIndex = 0;
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }

    const ticketExpirationPoliciesTable = $("#ticketExpirationPoliciesTable").DataTable({
        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0}
        ],

        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketExpirationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#ticketExpirationPoliciesTable td").addClass("mdc-data-table__cell");

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

    if (actuatorEndpoints.ticketExpirationPolicies) {
        $.get(actuatorEndpoints.ticketExpirationPolicies, response => {
            console.log(response);
            ticketExpirationPoliciesTable.clear();
            for (const key of Object.keys(response)) {
                const policy = response[key];
                delete policy.name;

                const flattened = flattenJSON(policy);
                for (const [k, v] of Object.entries(flattened)) {
                    ticketExpirationPoliciesTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${k}</code>`,
                        2: `<code>${v}</code>`
                    });
                }
            }
            ticketExpirationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }
}


async function initializeSsoSessionOperations() {
    const ssoSessionsEditor = initializeAceEditor("ssoSessionsEditor");
    ssoSessionsEditor.setReadOnly(true);

    const ssoSessionDetailsTable = $("#ssoSessionDetailsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "10%", targets: 0},
            {width: "20%", targets: 1},
            {width: "70%", targets: 2}
        ],
        drawCallback: settings => {
            $("#ssoSessionDetailsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionDetailsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionsTable = $("#ssoSessionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "13%", targets: 0},
            {width: "37%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "5%", targets: 4},
            {width: "8%", targets: 5},
            {width: "12%", targets: 6},
            {visible: false, target: 7}
        ],
        drawCallback: settings => {
            $("#ssoSessionsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionsTable td").addClass("mdc-data-table__cell");
        }
    });

    $("#ssoSessionUsername").on("keypress", e => {
        if (e.which === 13) {
            $("#ssoSessionButton").click();
        }
    });

    $("#removeSsoSessionButton").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }

            swal({
                title: "Are you sure you want to delete all sessions for the user?",
                text: "Once deleted, you may not be able to recover this entry.",
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willDelete) => {
                    if (willDelete) {
                        const username = $("#ssoSessionUsername").val();

                        $.ajax({
                            url: `${actuatorEndpoints.ssosessions}/users/${username}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => {
                                ssoSessionsEditor.setValue(JSON.stringify(response, null, 2));
                                ssoSessionsEditor.gotoLine(1);
                                ssoSessionsTable.clear().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayErrorInBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=ssoSessionButton]").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }
            ssoSessionsEditor.setValue("");
            const username = $("#ssoSessionUsername").val();
            $.ajax({
                url: `${actuatorEndpoints.ssosessions}/users/${username}`,
                type: "GET",
                contentType: "application/x-www-form-urlencoded",
                success: (response, status, xhr) => {
                    $("#ssoSessionsEditorContainer").removeClass("d-none");
                    ssoSessionsEditor.setValue(JSON.stringify(response, null, 2));
                    ssoSessionsEditor.gotoLine(1);

                    for (const session of response.activeSsoSessions) {
                        const attributes = {
                            principal: session["principal_attributes"],
                            authentication: session["authentication_attributes"]
                        };

                        let serviceButtons = `
                         <button type="button" name="removeSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                class="mdc-button mdc-button--raised min-width-32x">
                            <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                        </button>
                        <button type="button" name="viewSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                class="mdc-button mdc-button--raised min-width-32x">
                            <i class="mdi mdi-account-eye min-width-32x" aria-hidden="true"></i>
                            <span id="sessionAttributes" class="d-none">${JSON.stringify(attributes)}</span>
                        </button>
                    `;

                        ssoSessionsTable.row.add({
                            0: `<code>${session["authentication_date"]}</code>`,
                            1: `<code>${session["ticket_granting_ticket"]}</code>`,
                            2: `<code>${session["authentication_attributes"]?.clientIpAddress?.[0]}</code>`,
                            3: `<code>${session["authentication_attributes"]?.userAgent?.[0]}</code>`,
                            4: `<code>${session["number_of_uses"]}</code>`,
                            5: `<code>${session["remember_me"]}</code>`,
                            6: `${serviceButtons}`,
                            7: `${JSON.stringify(attributes)}`
                        });
                    }
                    ssoSessionsTable.draw();

                    $("button[name=viewSsoSession]").off().on("click", function () {
                        const attributes = JSON.parse($(this).children("span").first().text());
                        for (const [key, value] of Object.entries(attributes.principal)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Principal</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        for (const [key, value] of Object.entries(attributes.authentication)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Authentication</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        ssoSessionDetailsTable.draw();

                        let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("ssoSession-dialog"));
                        dialog["open"]();
                    });

                    $("button[name=removeSsoSession]").off().on("click", function () {
                        const ticket = $(this).data("ticketgrantingticket");
                        console.log("Removing ticket-granting ticket:", ticket);

                        swal({
                            title: "Are you sure you want to delete this session?",
                            text: "Once deleted, you may not be able to recover this entry.",
                            icon: "warning",
                            buttons: true,
                            dangerMode: true
                        })
                            .then((willDelete) => {
                                if (willDelete) {
                                    $.ajax({
                                        url: `${actuatorEndpoints.ssosessions}/${ticket}`,
                                        type: "DELETE",
                                        contentType: "application/x-www-form-urlencoded",
                                        success: (response, status, xhr) => {
                                            ssoSessionsEditor.setValue(JSON.stringify(response, null, 2));
                                            ssoSessionsEditor.gotoLine(1);
                                            let nearestTr = $(this).closest("tr");
                                            ssoSessionsTable.row(nearestTr).remove().draw();
                                        },
                                        error: (xhr, status, error) => {
                                            console.error("Error fetching data:", error);
                                            displayErrorInBanner(xhr);
                                        }
                                    });
                                }
                            });
                    });
                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayErrorInBanner(xhr);
                }
            });
        }
    });

}

async function initializeLoggingOperations() {
    const toolbar = document.createElement("div");
    toolbar.innerHTML = `
        <button type="button" id="newLoggerButton" class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus" aria-hidden="true"></i>New</span>
        </button>
    `;
    const loggersTable = $("#loggersTable").DataTable({
        layout: {
            topStart: toolbar
        },
        pageLength: 25,
        autoWidth: false,
        columnDefs: [
            {width: "90%", targets: 0},
            {width: "10%", targets: 1}
        ],
        drawCallback: settings => {
            $("#loggersTable tr").addClass("mdc-data-table__row");
            $("#loggersTable td").addClass("mdc-data-table__cell");
        }
    });

    function determineLoggerColor(level) {
        let background = "darkgray";
        switch (level) {
        case "DEBUG":
            background = "cornflowerblue";
            break;
        case "INFO":
            background = "mediumseagreen";
            break;
        case "WARN":
            background = "darkorange";
            break;
        case "OFF":
            background = "black";
            break;
        case "ERROR":
            background = "red";
            break;
        }
        return background;
    }

    function fetchLoggerData(callback) {
        if (actuatorEndpoints.loggingConfig) {
            $.get(actuatorEndpoints.loggingConfig, response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
        }
    }

    function updateLoggersTable() {
        fetchLoggerData(response => {
            function addLoggerToTable(logger) {
                const background = determineLoggerColor(logger.level);
                const loggerLevel = `
                    <select data-logger='${logger.name}' name='loggerLevelSelect' class="palantir" style="color: whitesmoke; background-color: ${background}">
                        <option ${logger.level === "TRACE" ? "selected" : ""} value="TRACE">TRACE</option>
                        <option ${logger.level === "DEBUG" ? "selected" : ""} value="DEBUG">DEBUG</option>
                        <option ${logger.level === "INFO" ? "selected" : ""} value="INFO">INFO</option>
                        <option ${logger.level === "WARN" ? "selected" : ""} value="WARN">WARN</option>
                        <option ${logger.level === "ERROR" ? "selected" : ""} value="ERROR">ERROR</option>
                        <option ${logger.level === "OFF" ? "selected" : ""} value="OFF">OFF</option>
                    </select>
                    `;

                loggersTable.row.add({
                    0: `<code>${logger.name}</code>`,
                    1: `${loggerLevel.trim()}`
                });
            }

            function handleLoggerLevelSelectionChange() {
                $("select[name=loggerLevelSelect]").off().on("change", function () {
                    if (actuatorEndpoints.loggers) {
                        const logger = $(this).data("logger");
                        const level = $(this).val();
                        console.log("Logger:", logger, "Level:", level);
                        const loggerData = {
                            "configuredLevel": level
                        };
                        $.ajax({
                            url: `${actuatorEndpoints.loggers}/${logger}`,
                            type: "POST",
                            contentType: "application/json",
                            data: JSON.stringify(loggerData),
                            success: response => {
                                console.log("Update successful:", response);
                                $(this).css("background-color", determineLoggerColor(level));
                            },
                            error: (xhr, status, error) => {
                                console.error("Failed", error);
                                displayErrorInBanner(xhr);
                            }
                        });
                    }
                });
            }

            console.log(response);

            loggersTable.clear();
            for (const logger of response.loggers) {
                addLoggerToTable(logger);
            }
            loggersTable.draw();

            $("#newLoggerButton").off().on("click", () =>
                swal({
                    content: "input",
                    buttons: true,
                    icon: "success",
                    title: "What's the name of the new logger?",
                    text: "The new logger will only be effective at runtime and will not be persisted."
                })
                    .then((value) => {
                        addLoggerToTable({name: value, level: "WARN"});
                        loggersTable.draw();
                        handleLoggerLevelSelectionChange();
                        loggersTable.search(value).draw();
                    }));
            handleLoggerLevelSelectionChange();
        });
    }

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.LOGGING) {
            updateLoggersTable();
        }
    });
}

async function initializeSystemOperations() {
    function configureAuditEventsChart() {
        if (actuatorEndpoints.auditevents) {
            $.get(actuatorEndpoints.auditevents, response => {
                let auditData = [];
                const results = response.events.reduce((accumulator, event) => {
                    let timestamp = formatDateYearMonthDay(event.timestamp);
                    const type = event.type;

                    if (!accumulator[timestamp]) {
                        accumulator[timestamp] = {};
                    }

                    if (!accumulator[timestamp][type]) {
                        accumulator[timestamp][type] = 0;
                    }
                    accumulator[timestamp][type]++;
                    return accumulator;
                }, {});

                for (const [key, value] of Object.entries(results)) {
                    let auditEntry = Object.assign({timestamp: key}, value);
                    auditData.push(auditEntry);
                }

                auditEventsChart.data.labels = auditData.map(d => d.timestamp);
                let datasets = [];
                for (const entry of auditData) {
                    for (const type of Object.keys(auditData[0])) {
                        if (type !== "timestamp" && type !== "AUTHORIZATION_FAILURE") {
                            datasets.push({
                                borderWidth: 2,
                                data: auditData,
                                parsing: {
                                    xAxisKey: "timestamp",
                                    yAxisKey: type
                                },
                                label: type
                            });
                        }
                    }
                }
                auditEventsChart.data.datasets = datasets;
                auditEventsChart.update();
            });
        }
    }

    async function configureHttpRequestResponses() {
        if (actuatorEndpoints.httpexchanges) {
            $.get(actuatorEndpoints.httpexchanges, response => {
                function urlIsAcceptable(url) {
                    return !url.startsWith("/actuator")
                        && !url.startsWith("/webjars")
                        && !url.endsWith(".js")
                        && !url.endsWith(".ico")
                        && !url.endsWith(".png")
                        && !url.endsWith(".jpg")
                        && !url.endsWith(".jpeg")
                        && !url.endsWith(".gif")
                        && !url.endsWith(".svg")
                        && !url.endsWith(".css");
                }

                console.log(response);
                let httpSuccesses = [];
                let httpFailures = [];
                let httpSuccessesPerUrl = [];
                let httpFailuresPerUrl = [];

                let totalHttpSuccessPerUrl = 0;
                let totalHttpSuccess = 0;
                let totalHttpFailurePerUrl = 0;
                let totalHttpFailure = 0;

                for (const exchange of response.exchanges) {
                    let timestamp = formatDateYearMonthDayHourMinute(exchange.timestamp);
                    let url = exchange.request.uri
                        .replace(casServerPrefix, "")
                        .replaceAll(/\?.+/gi, "");

                    if (urlIsAcceptable(url)) {
                        if (exchange.response.status >= 100 && exchange.response.status <= 400) {
                            totalHttpSuccess++;
                            httpSuccesses.push({x: timestamp, y: totalHttpSuccess});
                            totalHttpSuccessPerUrl++;
                            httpSuccessesPerUrl.push({x: url, y: totalHttpSuccessPerUrl});
                        } else {
                            totalHttpFailure++;
                            httpFailures.push({x: timestamp, y: totalHttpFailure});
                            totalHttpFailurePerUrl++;
                            httpFailuresPerUrl.push({x: url, y: totalHttpFailurePerUrl});
                        }
                    }
                }
                httpRequestResponsesChart.data.datasets[0].data = httpSuccesses;
                httpRequestResponsesChart.data.datasets[0].label = "Success";
                httpRequestResponsesChart.data.datasets[1].data = httpFailures;
                httpRequestResponsesChart.data.datasets[1].label = "Failure";
                httpRequestResponsesChart.update();

                httpRequestsByUrlChart.data.datasets[0].data = httpSuccessesPerUrl;
                httpRequestsByUrlChart.data.datasets[0].label = "Success";
                httpRequestsByUrlChart.data.datasets[1].data = httpFailuresPerUrl;
                httpRequestsByUrlChart.data.datasets[1].label = "Failure";
                httpRequestsByUrlChart.update();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });

            $("#downloadHeapDumpButton").off().on("click", () => {
                $("#downloadHeapDumpButton").prop("disabled", true);
                fetch(actuatorEndpoints.heapdump)
                    .then(response =>
                        response.blob().then(blob => {
                            const link = document.createElement("a");
                            link.href = window.URL.createObjectURL(blob);
                            link.download = "heapdump";
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                            $("#downloadHeapDumpButton").prop("disabled", false);
                        }))
                    .catch(error => {
                        console.error("Error fetching file:", error);
                        $("#downloadHeapDumpButton").prop("disabled", false);
                    });
            });
        }
    }

    function configureHealthChart() {
        if (actuatorEndpoints.health) {
            $.get(actuatorEndpoints.health, response => {
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
                displayErrorInBanner(xhr);
            });
        }
    }

    function configureStatistics() {
        if (actuatorEndpoints.statistics) {
            $.get(actuatorEndpoints.statistics, response => {
                const expired = response.expiredTickets;
                const valid = response.validTickets;
                statisticsChart.data.datasets[0].data = [valid, expired];
                statisticsChart.update();
            }).fail((xhr, status, error) => console.error("Error fetching data:", error));
        }
    }

    function fetchSystemData(callback) {
        if (actuatorEndpoints.info) {
            $.get(actuatorEndpoints.info, response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
        }
    }

    function configureSystemData() {
        fetchSystemData(response => {
            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);
            console.log("Memory: ", maximum, total, free);
            memoryChart.data.datasets[0].data = [maximum, total, free];
            memoryChart.update();
        });

        if (actuatorEndpoints.metrics) {
            $.get(`${actuatorEndpoints.metrics}/http.server.requests`, response => {
                let count = response.measurements[0].value;
                let totalTime = response.measurements[1].value.toFixed(2);
                let maxTime = response.measurements[2].value.toFixed(2);
                $("#httpRequestsCount").text(count);
                $("#httpRequestsTotalTime").text(`${totalTime}s`);
                $("#httpRequestsMaxTime").text(`${maxTime}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
            $.get(`${actuatorEndpoints.metrics}/http.server.requests.active`, response => {
                let active = response.measurements[0].value;
                let duration = response.measurements[1].value.toFixed(2);
                $("#httpRequestsActive").text(active);
                $("#httpRequestsDuration").text(`${duration}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
        }
        configureHttpRequestResponses().then(configureAuditEventsChart());
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
        if (index === Tabs.SYSTEM) {
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

    setInterval(() => {
        configureSystemData();
        configureHealthChart();
        configureStatistics();
    }, DEFAULT_INTERVAL);

    if (actuatorEndpoints.casFeatures) {
        $.get(actuatorEndpoints.casFeatures, response => {
            console.log(response);
            $("#casFeaturesChipset").empty();
            for (const element of response) {
                let feature = `
                            <div class="mdc-chip" role="row">
                                <div class="mdc-chip__ripple"></div>
                                <span role="gridcell">
                                  <span class="mdc-chip__text">${element.trim().replace("CasFeatureModule.", "")}</span>
                                </span>
                            </div>
                        `.trim();
                $("#casFeaturesChipset").append($(feature));
            }
        });
    }

    configureSystemData();
    configureStatistics();
    configureHealthChart();
}

function initializeServiceButtons() {
    const editor = initializeAceEditor("serviceEditor");
    let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("editServiceDialog"));

    $("button[name=deleteService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (actuatorEndpoints.registeredservices) {
            swal({
                title: "Are you sure you want to delete this entry?",
                text: "Once deleted, you may not be able to recover this entry.",
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willDelete) => {
                    if (willDelete) {
                        $.ajax({
                            url: `${actuatorEndpoints.registeredservices}/${serviceId}`,
                            type: "DELETE",
                            success: response => {
                                console.log("Resource deleted successfully:", response);
                                let nearestTr = $(this).closest("tr");

                                let applicationsTable = $("#applicationsTable").DataTable();
                                applicationsTable.row(nearestTr).remove().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error deleting resource:", error);
                                displayErrorInBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=editService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (actuatorEndpoints.registeredservices) {
            $.get(`${actuatorEndpoints.registeredservices}/${serviceId}`, response => {
                const value = JSON.stringify(response, null, 4);
                editor.setValue(value, -1);
                editor.gotoLine(1);
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", false);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
        }
    });

    $("button[name=saveService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            swal({
                title: "Are you sure you want to update this entry?",
                text: "Once updated, you may not be able to revert this entry.",
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willUpdate) => {
                    if (willUpdate) {
                        const value = editor.getValue();

                        const editServiceDialogElement = document.getElementById("editServiceDialog");
                        const isNewService = $(editServiceDialogElement).attr("newService") === "true";

                        $.ajax({
                            url: `${actuatorEndpoints.registeredservices}`,
                            type: isNewService ? "POST" : "PUT",
                            contentType: "application/json",
                            data: value,
                            success: response => {
                                console.log("Update successful:", response);
                                editServiceDialog["close"]();
                                fetchServices(() => {
                                    let newServiceId = response.id;
                                    $("#applicationsTable tr").removeClass("selected");
                                    console.log("New Service Id", newServiceId);
                                    $(`#applicationsTable tr td span[serviceId=${newServiceId}]`).each(function () {
                                        $(this).closest("tr").addClass("selected");
                                    });
                                });
                            },
                            error: (xhr, status, error) => {
                                console.error("Update failed:", error);
                                displayErrorInBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=copyService]").off().on("click", function () {
        if (actuatorEndpoints.registeredservices) {
            let serviceId = $(this).parent().attr("serviceId");
            $.get(`${actuatorEndpoints.registeredservices}/${serviceId}`, response => {
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
                editor.setValue(value, -1);
                editor.gotoLine(1);
                editor.findAll("...", {regExp: false});

                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", true);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayErrorInBanner(xhr);
            });
        }
    });
}

async function initializeServicesOperations() {
    const applicationsTable = $("#applicationsTable").DataTable({
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

    applicationsTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));

    fetchServices();
    initializeFooterButtons();
}

async function initializeAllCharts() {
    threadDumpChart = new Chart(document.getElementById("threadDumpChart").getContext("2d"), {
        type: "bar",
        options: {
            responsive: true,
            fill: true,
            borderWidth: 2,
            plugins: {
                legend: {
                    position: "top"
                },
                title: {
                    display: true
                }
            }
        }
    });
    threadDumpChart.update();

    servicesChart = new Chart(document.getElementById("servicesChart").getContext("2d"), {
        type: "pie",
        data: {
            labels: [
                "CAS",
                "SAML2",
                "OAuth",
                "OpenID Connect",
                "Ws-Federation"
            ],
            datasets: [{
                label: "Registered Services",
                data: [0, 0, 0, 0, 0],
                backgroundColor: [
                    "deepskyblue",
                    "indianred",
                    "mediumpurple",
                    "limegreen",
                    "slategrey"
                ],
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "top"
                },
                title: {
                    display: true
                }
            }
        }
    });
    servicesChart.update();

    memoryChart = new Chart(document.getElementById("memoryChart").getContext("2d"), {
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

    statisticsChart = new Chart(document.getElementById("statisticsChart").getContext("2d"), {
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
    auditEventsChart = new Chart(document.getElementById("auditEventsChart").getContext("2d"), {
        type: "bar",
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'Audit Events'
                }
            }
        }
    });
    auditEventsChart.update();

    httpRequestResponsesChart = new Chart(document.getElementById("httpRequestResponsesChart").getContext("2d"), {
        type: "bar",
        data: {
            datasets: [
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Success",
                    borderWidth: 2
                },
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Failure",
                    borderWidth: 2
                }
            ]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'HTTP Request/Responses (Date)'
                }
            }
        }
    });
    httpRequestResponsesChart.update();

    httpRequestsByUrlChart = new Chart(document.getElementById("httpRequestsByUrlChart").getContext("2d"), {
        type: "bar",
        data: {
            datasets: [
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Success",
                    borderWidth: 2
                },
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Failure",
                    borderWidth: 2
                }
            ]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'HTTP Request/Responses (URL)'
                }
            }
        }
    });
    httpRequestsByUrlChart.update();

    systemHealthChart = new Chart(document.getElementById("systemHealthChart").getContext("2d"), {
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

    jvmThreadsChart = new Chart(document.getElementById("jvmThreadsChart").getContext("2d"), {
        type: "bar",
        data: {
            labels: ["Daemon", "Live", "Peak", "Started", "States"],
            datasets: [{
                label: "JVM Thread Types",
                data: [0, 0, 0, 0, 0],
                fill: true,
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

}

function updateNavigationSidebar() {
    setTimeout(() => $("nav.sidebar-navigation").css("height", $("#dashboard .mdc-card").css("height")), 100);
}

async function initializePersonDirectoryOperations() {
    const personDirectoryTable = $("#personDirectoryTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#personDirectoryTable tr").addClass("mdc-data-table__row");
            $("#personDirectoryTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=personDirectoryClearButton]").off().on("click", () => {
        if (actuatorEndpoints.persondirectory) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            swal({
                title: `Are you sure you want to delete the cache for ${username}?`,
                text: `Once the cached entry is removed, attribute repositories would be forced to fetch attributes for ${username} again`,
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willClear) => {
                    if (willClear) {
                        personDirectoryTable.clear();
                        $.ajax({
                            url: `${actuatorEndpoints.persondirectory}/cache/${username}`,
                            type: "DELETE",
                            contentType: "application/json",
                            success: (response, status, xhr) => {
                                console.log("Cache is now cleared");
                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayErrorInBanner(xhr);
                            }
                        });
                    }
                });
        }
    });


    $("button[name=personDirectoryButton]").off().on("click", () => {
        if (actuatorEndpoints.persondirectory) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            personDirectoryTable.clear();
            $.ajax({
                url: `${actuatorEndpoints.persondirectory}/cache/${username}`,
                type: "GET",
                contentType: "application/json",
                success: (response, status, xhr) => {
                    console.log(response);

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
                    displayErrorInBanner(xhr);
                }
            });
        }
    });

}

async function initializeAuthenticationOperations() {
    const authenticationHandlersTable = $("#authenticationHandlersTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0},
            {width: "80%", targets: 1},
            {width: "10%", targets: 2},
            {width: "10%", targets: 3}
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

    authenticationHandlersTable.clear();
    if (actuatorEndpoints.authenticationHandlers) {
        $.get(actuatorEndpoints.authenticationHandlers, response => {
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
            displayErrorInBanner(xhr);
        });
    }

    const authenticationPoliciesTable = $("#authenticationPoliciesTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        columnDefs: [
            {width: "80%", targets: 0},
            {width: "20%", targets: 1}
        ],
        drawCallback: settings => {
            $("#authenticationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#authenticationPoliciesTable td").addClass("mdc-data-table__cell");
        }
    });

    authenticationPoliciesTable.clear();
    if (actuatorEndpoints.authenticationPolicies) {
        $.get(actuatorEndpoints.authenticationPolicies, response => {
            for (const handler of response) {
                authenticationPoliciesTable.row.add({
                    0: `${handler.name}`,
                    1: `<code>${handler.order}</code>`
                });
            }
            authenticationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }

    const delegatedClientsTable = $("#delegatedClientsTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0},
            {width: "40%", targets: 1},
            {width: "60%", targets: 2},
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
                        if (rows.data()[i][3] === "saml2") {
                            samlButtons = `<span class="px-2">
                                <button type="button" title="Service Provider Metadata" name="saml2ClientSpMetadata" href="#" clientName='${group}'
                                        class="mdc-button mdc-button--raised toolbar">
                                    <i class="mdi mdi-text-box min-width-32x" aria-hidden="true"></i>
                                    Service Provider Metadata
                                </button>
                                <button type="button" title="Identity Provider Metadata" name="saml2ClientIdpMetadata" href="#" clientName='${group}'
                                        class="mdc-button mdc-button--raised toolbar">
                                    <i class="mdi mdi-file-xml-box min-width-32x" aria-hidden="true"></i>
                                    Identity Provider Metadata
                                </button>
                        </span>
                            `;
                        }
                        
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group} ${samlButtons.trim()} </td>
                            </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    delegatedClientsTable.clear();
    if (actuatorEndpoints.delegatedClients) {
        const saml2Editor = initializeAceEditor("delegatedClientsSaml2Editor", "xml");
        saml2Editor.setReadOnly(true);
        
        $.get(actuatorEndpoints.delegatedClients, response => {
            function showSamlMetadata(payload) {
                saml2Editor.setValue(new XMLSerializer().serializeToString(payload));
                saml2Editor.gotoLine(1);

                const beautify = ace.require("ace/ext/beautify");
                beautify.beautify(saml2Editor.session);

                const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("delegatedClientsSaml2Dialog"));
                dialog["open"]();
            }

            console.log(response);
            for (const [key, idp] of Object.entries(response)) {
                const details = flattenJSON(idp);
                for (const [k, v] of Object.entries(details)) {
                    if (Object.keys(v).length > 0 && k !== "type") {
                        let serviceButtons = null;
                        delegatedClientsTable.row.add({
                            0: `${key}`,
                            1: `<code>${toKebabCase(k)}</code>`,
                            2: `<code>${v}</code>`,
                            3: `${idp.type}`
                        });
                    }
                }
            }
            delegatedClientsTable.draw();
            $("#delegatedClientsContainer").removeClass("d-none");

            $("button[name=saml2ClientSpMetadata]").off().on("click", function () {
                $(this).prop("disabled", true);
                const url = `${casServerPrefix}/sp/${$(this).attr("clientName")}/metadata`;
                $.get(url, payload => showSamlMetadata(payload))
                    .always(() => $(this).prop("disabled", false));

            });
            $("button[name=saml2ClientIdpMetadata]").off().on("click", function () {
                $(this).prop("disabled", true);
                const url = `${casServerPrefix}/sp/${$(this).attr("clientName")}/idp/metadata`;
                $.get(url, payload => showSamlMetadata(payload))
                    .always(() => $(this).prop("disabled", false));
            });

        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            $("#delegatedClientsContainer").addClass("d-none");
        });
    } else {
        $("#delegatedClientsContainer").addClass("d-none");
    }
}

async function initializeConsentOperations() {
    if (actuatorEndpoints.attributeconsent) {
        const consentAttributesTable = $("#consentAttributesTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            columnDefs: [
                {width: "40%", targets: 0},
                {width: "60%", targets: 1}
            ],
            drawCallback: settings => {
                $("#consentAttributesTable tr").addClass("mdc-data-table__row");
                $("#consentAttributesTable td").addClass("mdc-data-table__cell");
            }
        });
        
        const consentTable = $("#consentTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            columnDefs: [
                {visible: false, targets: 0},
                {width: "12%", targets: 1},
                {width: "20%", targets: 2},
                {visible: false, targets: 3},
                {width: "15%", targets: 4},
                {width: "8%", targets: 5},
                {width: "8%", targets: 6},
                {width: "12%", targets: 7},
            ],
            drawCallback: settings => {
                $("#consentTable tr").addClass("mdc-data-table__row");
                $("#consentTable td").addClass("mdc-data-table__cell");
            }
        });
        consentTable.clear();
        $.get(actuatorEndpoints.attributeconsent, response => {
            for (const source of response) {

                let consentButtons = `
                 <button type="button" name="viewConsentAttributes" href="#" consentId='${source.decision.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                    <span class="d-none">${JSON.stringify(source.attributes)}</span>
                </button>
                <button type="button" name="deleteConsent" href="#" 
                        principal='${source.decision.principal}'
                        consentId='${source.decision.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                </button>
                `;

                consentTable.row.add({
                    0: `<code>${source.decision.id}</code>`,
                    1: `<code>${source.decision.principal}</code>`,
                    2: `<code>${source.decision.service}</code>`,
                    3: `<code></code>`,
                    4: `<code>${source.decision.createdDate}</code>`,
                    5: `<code>${source.decision.options}</code>`,
                    6: `<code>${source.decision.reminder} ${source.decision.reminderTimeUnit}</code>`,
                    7: `${consentButtons}`,
                });
            }
            consentTable.draw();

            $("button[name=viewConsentAttributes]").off().on("click", function () {
                const attributes = JSON.parse($(this).children("span").first().text());
                for (const [key, value] of Object.entries(attributes)) {
                    consentAttributesTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${value}</code>`
                    });
                }
                consentAttributesTable.draw();

                let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("consentAttributes-dialog"));
                dialog["open"]();
            });

            $("button[name=deleteConsent]").off().on("click", function () {
                const id = $(this).attr("consentId");
                const principal = $(this).attr("principal");
                swal({
                    title: `Are you sure you want to delete this entry for ${principal}?`,
                    text: "Once deleted, you may not be able to recover this entry.",
                    icon: "warning",
                    buttons: true,
                    dangerMode: true
                })
                    .then((willDelete) => {
                        if (willDelete) {
                            $.ajax({
                                url: `${actuatorEndpoints.attributeconsent}/${principal}/${id}`,
                                type: "DELETE",
                                contentType: "application/x-www-form-urlencoded",
                                success: (response, status, xhr) => {
                                    let nearestTr = $(this).closest("tr");
                                    consentTable.row(nearestTr).remove().draw();
                                },
                                error: (xhr, status, error) => {
                                    console.error("Error fetching data:", error);
                                    displayErrorInBanner(xhr);
                                }
                            });
                        }
                    });
            });
            
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });



        $("button[name=exportAllConsent]").off().on("click", () => {
            if (actuatorEndpoints.attributeconsent) {
                fetch(`${actuatorEndpoints.attributeconsent}/export`)
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
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function encryptOrDecryptConfig(op) {
        hideErrorInBanner();
        $("#configEncryptionResult").addClass("d-none");
        
        const form = document.getElementById("fmConfigEncryption");
        if (!form.reportValidity()) {
            return false;
        }
        const configValue = $("#configValue").val();
        if (actuatorEndpoints.casconfig) {
            $.post({
                url: `${actuatorEndpoints.casconfig}/${op}`,
                data: configValue,
                contentType: "text/plain"
            }, data => {
                $("#configEncryptionResult pre code").text(data);
                hljs.highlightAll();
                $("#configEncryptionResult").removeClass("d-none");
            }).fail((xhr, status, error) => {
                displayErrorInBanner(xhr);
                $("#configEncryptionResult").addClass("d-none");
            });
        }
    }

    configurationTable.clear();
    if (actuatorEndpoints.env) {
        $.get(actuatorEndpoints.env, response => {
            for (const source of response.propertySources) {
                const properties = flattenJSON(source.properties);
                for (const [key, value] of Object.entries(properties)) {
                    if (!key.endsWith(".origin")) {
                        configurationTable.row.add({
                            0: `${camelcaseToTitleCase(source.name)}`,
                            1: `<code>${key.replace(".value", "")}</code>`,
                            2: `<code>${value}</code>`
                        });
                    }
                }
            }
            configurationTable.draw();

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
            displayErrorInBanner(xhr);
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

    if (actuatorEndpoints.configprops) {
        $.get(actuatorEndpoints.configprops, response => {
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
            displayErrorInBanner(xhr);
        });
    }
    
    $("#encryptConfigButton").off().on("click", () => encryptOrDecryptConfig("encrypt"));
    $("#decryptConfigButton").off().on("click", () => encryptOrDecryptConfig("decrypt"));
}

async function initializeCasProtocolOperations() {
    function buildCasProtocolPayload(endpoint,format) {
        const form = document.getElementById("fmCasProtocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#casProtocolUsername").val();
        const password = $("#casProtocolPassword").val();
        const service = $("#casProtocolService").val();

        $.post(`${actuatorEndpoints.casvalidate}/${endpoint}`, {
            username: username,
            password: password,
            service: service
        }, data => {
            const editor = initializeAceEditor("casProtocolEditor", format);
            editor.setReadOnly(true);
            editor.setValue(data);
            editor.gotoLine(1);
            $("#casProtocolEditorContainer").removeClass("d-none");
        }).fail((xhr, status, error) => {
            displayErrorInBanner(xhr);
            $("#casProtocolEditorContainer").addClass("d-none");
        });
    }

    $("button[name=casProtocolV1Button]").off().on("click", () => buildCasProtocolPayload("validate", "text"));
    $("button[name=casProtocolV2Button]").off().on("click", () => buildCasProtocolPayload("serviceValidate", "xml"));
    $("button[name=casProtocolV3Button]").off().on("click", () => buildCasProtocolPayload("p3/serviceValidate", "xml"));
}

async function initializeThrottlesOperations() {
    const throttlesTable = $("#throttlesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "15%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "10%", targets: 4},
            {width: "20%", targets: 5},
            {width: "10%", targets: 6},
        ],
        drawCallback: settings => {
            $("#throttlesTable tr").addClass("mdc-data-table__row");
            $("#throttlesTable td").addClass("mdc-data-table__cell");
        }
    });

    function fetchThrottledAttempts() {
        throttlesTable.clear();
        $.get(actuatorEndpoints.throttles, response => {
            for (const record of response) {

                let buttons = `
                     <button type="button" name="removeThrottledAttempt" href="#" 
                            data-key='${record.key}'
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                    </button>
                `;
                
                throttlesTable.row.add({
                    0: `<code>${record.key}</code>`,
                    1: `<code>${record.id}</code>`,
                    2: `<code>${record.value}</code>`,
                    3: `<code>${record.username}</code>`,
                    4: `<code>${record.clientIpAddress}</code>`,
                    5: `<code>${record.expiration}</code>`,
                    6: `${buttons}`,
                });
            }
            throttlesTable.draw();
            
            $("button[name=removeThrottledAttempt]").off().on("click", function () {
                const key = $(this).data("key");
                swal({
                    title: "Are you sure you want to delete this entry?",
                    text: "Once deleted, you may not be able to recover this entry.",
                    icon: "warning",
                    buttons: true,
                    dangerMode: true
                })
                    .then((willDelete) => {
                        if (willDelete) {
                            $.ajax({
                                url: `${actuatorEndpoints.throttles}/${key}`,
                                type: "DELETE",
                                contentType: "application/x-www-form-urlencoded",
                                success: (response, status, xhr) => {
                                    let nearestTr = $(this).closest("tr");
                                    throttlesTable.row(nearestTr).remove().draw();
                                },
                                error: (xhr, status, error) => {
                                    console.error("Error fetching data:", error);
                                    displayErrorInBanner(xhr);
                                }
                            });
                        }
                    });
            });
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    }
    
    if (actuatorEndpoints.throttles) {
        fetchThrottledAttempts();

        $("button[name=releaseThrottlesButton]").off().on("click", () => {
            swal({
                title: "Are you sure you want to release throttled entries?",
                text: "Released entries, when eligible, will be removed from the authentication throttling store.",
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willDelete) => {
                    if (willDelete) {
                        $.ajax({
                            url: `${actuatorEndpoints.throttles}`,
                            type: 'DELETE',
                            data: {
                                clear: false
                            },
                            success: (response, textStatus, jqXHR) => {
                                fetchThrottledAttempts();
                            },
                            error: (jqXHR, textStatus, errorThrown) => {
                                displayErrorInBanner(jqXHR);
                            }
                        });
                    }
                });
        });

        $("button[name=clearThrottlesButton]").off().on("click", () => {
            swal({
                title: "Are you sure you want to clear throttled entries?",
                text: "All entries will be removed from the authentication throttling store.",
                icon: "warning",
                buttons: true,
                dangerMode: true
            })
                .then((willDelete) => {
                    if (willDelete) {
                        $.ajax({
                            url: `${actuatorEndpoints.throttles}`,
                            type: 'DELETE',
                            data: {
                                clear: true
                            },
                            success: (response, textStatus, jqXHR) => {
                                fetchThrottledAttempts();
                            },
                            error: (jqXHR, textStatus, errorThrown) => {
                                displayErrorInBanner(jqXHR);
                            }
                        });
                    }
                });
        });
    }
}

async function initializeSAML1ProtocolOperations() {
    $("button[name=saml1ProtocolButton]").off().on("click", () => {
        const form = document.getElementById("fmSaml1Protocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#saml1ProtocolUsername").val();
        const password = $("#saml1ProtocolPassword").val();
        const service = $("#saml1ProtocolService").val();

        $.post(`${actuatorEndpoints.samlvalidate}`, {
            username: username,
            password: password,
            service: service
        }, data => {
            $("#saml1ProtocolEditorContainer").removeClass("d-none");
            const editor = initializeAceEditor("saml1ProtocolEditor", "xml");
            editor.setReadOnly(true);
            editor.setValue(data.assertion);
            editor.gotoLine(1);

            const serviceEditor = initializeAceEditor("saml1ProtocolServiceEditor", "json");
            serviceEditor.setReadOnly(true);
            serviceEditor.setValue(JSON.stringify(data.registeredService, null, 2));
            serviceEditor.gotoLine(1);
        }).fail((xhr, status, error) => {
            displayErrorInBanner(xhr);
            $("#saml2ProtocolEditorContainer").addClass("d-none");
        });
    });

}

function showOidcJwks() {
    $.get(`${casServerPrefix}/oidc/jwks`, response => {
        let oidcOpConfigurationDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("oidcOpConfigurationDialog"));
        const editor = initializeAceEditor("oidcOpConfigurationDialogEditor", "json");
        editor.setValue(JSON.stringify(response, null, 2));
        editor.gotoLine(1);
        editor.setReadOnly(true);
        oidcOpConfigurationDialog["open"]();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayErrorInBanner(xhr);
    });
}

async function initializeOidcProtocolOperations() {
    $("button[name=oidcOpConfigurationButton]").off().on("click", () => {
        hideErrorInBanner();
        $.get(`${casServerPrefix}/oidc/.well-known/openid-configuration`, response => {
            let oidcOpConfigurationDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("oidcOpConfigurationDialog"));
            const editor = initializeAceEditor("oidcOpConfigurationDialogEditor", "json");
            editor.setValue(JSON.stringify(response, null, 2));
            editor.gotoLine(1);
            editor.setReadOnly(true);
            oidcOpConfigurationDialog["open"]();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayErrorInBanner(xhr);
        });
    });

    $("button[name=oidcKeyRotationButton]").off().on("click", () => {
        hideErrorInBanner();
        swal({
            title: "Are you sure you want to rotate keys?",
            text: "Once rotated, the change will take effect immediately.",
            icon: "warning",
            buttons: true,
            dangerMode: true
        })
            .then((willDo) => {
                if (willDo) {
                    $("#oidcKeyRotationButton").prop("disabled", true);
                    $.get(`${actuatorEndpoints.oidcjwks}/rotate`, response => {
                        swal({
                            title: "Done!",
                            text: "Keys in the OpenID Connect keystore are successfully rotated.",
                            buttons: false,
                            icon: "success",
                            timer: 1000
                        });
                        $("#oidcKeyRotationButton").prop("disabled", false);
                    }).fail((xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayErrorInBanner(xhr);
                        $("#oidcKeyRotationButton").prop("disabled", false);
                    });
                }
            });
    });

    $("button[name=oidcKeyRevocationButton]").off().on("click", () => {
        hideErrorInBanner();
        swal({
            title: "Are you sure you want to revoke keys?",
            text: "Once revoked, the change will take effect immediately.",
            icon: "warning",
            buttons: true,
            dangerMode: true
        })
            .then((willDo) => {
                if (willDo) {
                    $("#oidcKeyRevocationButton").prop("disabled", true);
                    $.get(`${actuatorEndpoints.oidcjwks}/revoke`, response => {
                        swal({
                            title: "Done!",
                            text: "Keys in the OpenID Connect keystore are successfully revoked.",
                            buttons: false,
                            icon: "success",
                            timer: 1000
                        });
                        $("#oidcKeyRevocationButton").prop("disabled", false);
                    }).fail((xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayErrorInBanner(xhr);
                        $("#oidcKeyRevocationButton").prop("disabled", false);
                    });
                }
            });
    });

}

async function initializeSAML2ProtocolOperations() {
    $("button[name=saml2ProtocolPostButton]").off().on("click", () => {
        const form = document.getElementById("fmSaml2Protocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#saml2ProtocolUsername").val();
        const password = $("#saml2ProtocolPassword").val();
        const entityId = $("#saml2ProtocolEntityId").val();

        $.post(`${actuatorEndpoints.samlpostprofileresponse}`, {
            username: username,
            password: password,
            entityId: entityId
        }, data => {
            const editor = initializeAceEditor("saml2ProtocolEditor", "xml");
            editor.setReadOnly(true);
            editor.setValue(new XMLSerializer().serializeToString(data));
            editor.gotoLine(1);
            $("#saml2ProtocolEditorContainer").removeClass("d-none");
            $("#saml2ProtocolLogoutEditor").addClass("d-none");
        }).fail((xhr, status, error) => {
            displayErrorInBanner(xhr);
            $("#saml2ProtocolEditorContainer").addClass("d-none");
        });
    });
    
    $("button[name=saml2ProtocolLogoutButton]").off().on("click", () => {
        const entityId = document.getElementById("saml2ProtocolEntityId");
        if (!entityId.checkValidity()) {
            entityId.reportValidity();
            return false;
        }
        $.ajax({
            url: `${actuatorEndpoints.samlpostprofileresponse}/logout/post`,
            type: 'POST',
            data: {
                entityId: $("#saml2ProtocolEntityId").val()
            },
            success: (response, textStatus, jqXHR) => {
                const saml2ProtocolEditor = initializeAceEditor("saml2ProtocolEditor", "html");
                saml2ProtocolEditor.setReadOnly(true);
                saml2ProtocolEditor.setValue(response);
                saml2ProtocolEditor.gotoLine(1);

                const logoutRequest = atob(jqXHR.getResponseHeader("LogoutRequest"));
                const saml2ProtocolLogoutEditor = initializeAceEditor("saml2ProtocolLogoutEditor", "xml");
                saml2ProtocolLogoutEditor.setReadOnly(true);
                saml2ProtocolLogoutEditor.setValue(logoutRequest);
                saml2ProtocolLogoutEditor.gotoLine(1);
                
                $("#saml2ProtocolEditorContainer").removeClass("d-none");
                $("#saml2ProtocolLogoutEditor").removeClass("d-none");
            },
            error: (jqXHR, textStatus, errorThrown) => {
                displayErrorInBanner(xhr);
                $("#saml2ProtocolEditorContainer").addClass("d-none");
                $("#saml2ProtocolLogoutEditor").addClass("d-none");
            }
        });
    });


    $("button[name=saml2MetadataCacheInvalidateButton]").off().on("click", () => {
        hideErrorInBanner();
        $("#saml2MetadataCacheEditorContainer").addClass("d-none");
        
        swal({
            title: "Are you sure you want to invalidate the cache entry?",
            text: "Once deleted, the change will take effect immediately.",
            icon: "warning",
            buttons: true,
            dangerMode: true
        })
            .then((willDelete) => {
                if (willDelete) {
                    $.ajax({
                        url: `${actuatorEndpoints.samlidpregisteredservicemetadatacache}`,
                        type: 'DELETE',
                        data: {
                            entityId: $("#saml2MetadataCacheEntityId").val(),
                            serviceId: $("#saml2MetadataCacheService").val()
                        },
                        success: (response, textStatus, jqXHR) => {
                            swal({
                                title: "Cached metadata record(s) removed.",
                                text: "Cached metadata entry has been removed successfully.",
                                buttons: false,
                                icon: "success",
                                timer: 1000
                            });
                        },
                        error: (jqXHR, textStatus, errorThrown) => {
                            displayErrorInBanner(jqXHR);
                        }
                    });
                }
            });
    });

    $("button[name=saml2MetadataCacheFetchButton]").off().on("click", function() {
        hideErrorInBanner();

        $(this).prop("disabled", true);
        $.ajax({
            url: `${actuatorEndpoints.samlidpregisteredservicemetadatacache}`,
            type: 'GET',
            data: {
                entityId: $("#saml2MetadataCacheEntityId").val(),
                serviceId: $("#saml2MetadataCacheService").val()
            },
            success: (response, textStatus, jqXHR) => {
                console.log(response);
                
                const editor = initializeAceEditor("saml2MetadataCacheEditor", "xml");
                editor.setReadOnly(true);
                for (const [entityId, entry] of Object.entries(response)) {
                    editor.setValue(entry.metadata);
                    $("#saml2MetadataCacheDetails").html(`<i class="mdc-tab__icon mdi mdi-clock" aria-hidden="true"></i> Cache Instant: <code>${entry.cachedInstant}</code>`);
                }
                editor.gotoLine(1);
                $("#saml2MetadataCacheEditorContainer").removeClass("d-none");
                $("#saml2MetadataCacheDetails").removeClass("d-none");
                $(this).prop("disabled", false)
            },
            error: (jqXHR, textStatus, errorThrown) => {
                displayErrorInBanner(jqXHR);
                $("#saml2MetadataCacheEditorContainer").addClass("d-none");
                $("#saml2MetadataCacheDetails").addClass("d-none");
                $(this).prop("disabled", false)
            }
        });
        
    });
}

async function initializePalantir() {
    try {
        await Promise.all([
            initializeAllCharts(),
            initializeScheduledTasksOperations(),
            initializeServicesOperations(),
            initializeAccessStrategyOperations(),
            initializeTicketsOperations(),
            initializeSystemOperations(),
            initializeLoggingOperations(),
            initializeSsoSessionOperations(),
            initializeConfigurationOperations(),
            initializePersonDirectoryOperations(),
            initializeAuthenticationOperations(),
            initializeConsentOperations(),
            initializeCasProtocolOperations(),
            initializeSAML2ProtocolOperations(),
            initializeSAML1ProtocolOperations(),
            initializeOidcProtocolOperations(),
            initializeThrottlesOperations()
        ]);
        setTimeout(() => {
            if (!actuatorEndpoints.registeredservices) {
                $("#applicationsTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.APPLICATIONS}`).addClass("d-none");
            }
            if (!actuatorEndpoints.metrics || !actuatorEndpoints.httpexchanges || !actuatorEndpoints.auditevents
                || !actuatorEndpoints.heapdump || !actuatorEndpoints.health || !actuatorEndpoints.statistics) {
                $("#systemTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.SYSTEM}`).addClass("d-none");
            }
            if (!actuatorEndpoints.ticketregistry) {
                $("#ticketsTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.TICKETS}`).addClass("d-none");
            }
            if (!actuatorEndpoints.ticketregistry) {
                $("#tasksTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.TASKS}`).addClass("d-none");
            }
            if (!actuatorEndpoints.persondirectory) {
                $("#personDirectoryTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.PERSON_DIRECTORY}`).addClass("d-none");
            }
            if (!actuatorEndpoints.authenticationHandlers || !actuatorEndpoints.authenticationPolicies) {
                $("#authenticationTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.AUTHENTICATION}`).addClass("d-none");
            }
            if (!actuatorEndpoints.serviceaccess) {
                $("#accessStrategyTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.ACCESS_STRATEGY}`).addClass("d-none");
            }
            if (!actuatorEndpoints.ssosessions) {
                $("#ssoSessionsTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.SSO_SESSIONS}`).addClass("d-none");
            }
            if (!actuatorEndpoints.loggingConfig || !actuatorEndpoints.loggers) {
                $("#loggingTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.LOGGING}`).addClass("d-none");
            }
            if (!actuatorEndpoints.env || !actuatorEndpoints.configprops) {
                $("#configurationTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.CONFIGURATION}`).addClass("d-none");
            }
            if (!actuatorEndpoints.attributeconsent) {
                $("#consentTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.CONSENT}`).addClass("d-none");
            }
            if (!actuatorEndpoints.casvalidate) {
                $("#casprotocol").parent().remove();
                $("#casProtocolContainer").addClass("d-none");
            }
            if (!actuatorEndpoints.samlpostprofileresponse) {
                $("#saml2protocol").parent().remove();
                $("#saml2ProtocolContainer").addClass("d-none");
            }
            if (!actuatorEndpoints.samlvalidate) {
                $("#saml1ProtocolContainer").addClass("d-none");
                $("#saml1protocol").parent().remove();
            }
            if (!actuatorEndpoints.casconfig) {
                $("#config-encryption-tab").addClass("d-none");
                $("#casConfigSecurity").parent().remove();
            }
            
            if (!actuatorEndpoints.samlvalidate && !actuatorEndpoints.casvalidate
                && !actuatorEndpoints.samlpostprofileresponse) {
                $("#protocolsTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.PROTOCOLS}`).addClass("d-none");
            }

            if (!actuatorEndpoints.throttles) {
                $("#throttlesTabButton").addClass("d-none");
                $(`#attribute-tab-${Tabs.THROTTLES}`).addClass("d-none");
            }

            let visibleCount = $("nav.sidebar-navigation ul li:visible").length;
            console.log("Number of visible list items:", visibleCount);

            if (visibleCount === 0) {
                $("#dashboard").hide();
                swal({
                    title: "Palantir is unavailable!",
                    text: `Palantir requires a number of actuator endpoints to be enabled and exposed, and your CAS deployment fails to do so.`,
                    icon: "warning",
                    buttons: false
                });
            } else {

                let selectedTab = window.localStorage.getItem("PalantirSelectedTab");
                console.log("Selected tab", selectedTab);
                if (!$(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).is(":visible")) {
                    selectedTab = Tabs.APPLICATIONS;
                }
                console.log("Activating Tab", selectedTab);
                $(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).click();
                activateDashboardTab(selectedTab);
            }
        }, 2);
        $("#dashboard").removeClass("d-none");
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

function activateDashboardTab(idx) {
    try {
        let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
        tabs.activateTab(Number(idx));
        updateNavigationSidebar();
    } catch (e) {
        console.error("An error occurred while activating tab:", e);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    $(".jqueryui-tabs").tabs();

    $("nav.sidebar-navigation ul li").off().on("click", function () {
        hideErrorInBanner();
        $("li").removeClass("active");
        $(this).addClass("active");
        const index = $(this).data("tab-index");
        console.log("Tracking tab", index);
        window.localStorage.setItem("PalantirSelectedTab", index);
        activateDashboardTab(index);
    });
    swal({
        icon: "info",
        title: "Initializing Palantir",
        text: "Please wait while Palantir is being initialized...",
        allowOutsideClick: false,
        buttons: false
    });
    initializePalantir().then(r => {
        console.log("Palantir ready!");
        swal({
            title: "Palantir is ready!",
            text: "Palantir has been successfully initialized and is ready for use.",
            buttons: false,
            icon: "success",
            timer: 1000
        });
    });
});
