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
    SSO_SESSIONS: 6
};

/**
 * Charts objects.
 */
let servicesChart = null;
let memoryChart = null;
let statisticsChart = null;
let systemHealthChart = null;
let jvmThreadsChart = null;

function fetchServices(callback) {
    $.get(`${casServerPrefix}/actuator/registeredServices`, response => {
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
                2: `<span serviceId='${service.id}' class="text-wrap">${service.serviceId}</span>`,
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
    });
}

function initializeFooterButtons() {

    $("button[name=newService]").off().on("click", () => {
        const editServiceDialogElement = document.getElementById("editServiceDialog");
        let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceDialogElement);
        const editor = initializeAceEditor("serviceEditor");
        editor.setValue("");
        editor.gotoLine(1);

        $(editServiceDialogElement).attr("newService", true);
        editServiceDialog["open"]();
    });

    $("button[name=importService]").off().on("click", () => {
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
    });

    $("button[name=exportService]").off().on("click", () => {
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
    });

    $("button[name=exportAll]").off().on("click", () => {
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
    });
}


/**
 * Initialization Functions
 */
async function initializeAccessStrategyOperations() {
    const accessStrategyEditor = initializeAceEditor("accessStrategyEditor");
    accessStrategyEditor.setReadOnly(true);

    $("button[name=accessStrategyButton]").off().on("click", () => {
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
    });
}

function initializeJvmMetrics() {
    function fetchJvmThreadMetric(metricName) {
        return new Promise((resolve, reject) => {
            $.get(`${casServerPrefix}/actuator/metrics/${metricName}`, response => {
                resolve(response.measurements[0].value);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                reject(error);
            });
        });
    }

    async function fetchJvmMetrics() {
        try {
            const promises = [
                "jvm.threads.daemon",
                "jvm.threads.live",
                "jvm.threads.peak",
                "jvm.threads.started",
                "jvm.threads.states"
            ].map(metric => fetchJvmThreadMetric(metric));
            const results = await Promise.all(promises);
            const numbersArray = results.map(result => Number(result));
            console.log("Fetched values:", numbersArray);
            return numbersArray;
        } catch (error) {
            console.error("An error occurred:", error);
        }
    }

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    const index = tabs.foundation.adapter.getFocusedTabIndex();
    if (index === 3) {
        fetchJvmMetrics().then(payload => {
            jvmThreadsChart.data.datasets[0].data = payload;
            jvmThreadsChart.update();
        });
    }
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

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.TASKS) {
            initializeJvmMetrics();
        }
    });
    setInterval(() => {
        initializeJvmMetrics();
    }, 4000);
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
            $.get(`${casServerPrefix}/actuator/ticketRegistry/query?type=${type}&id=${ticketId}&decode=${decode}`,
                response => {
                    ticketEditor.setValue(JSON.stringify(response, null, 2));
                    ticketEditor.gotoLine(1);
                })
                .fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                });
        }
    });

    $("button#cleanTicketsButton").off().on("click", () => {
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
    });


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

    $.get(`${casServerPrefix}/actuator/ticketExpirationPolicies`, response => {
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
    });
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

    $('#ssoSessionUsername').on('keypress', e => {
        if (e.which === 13) {
            $('#ssoSessionButton').click();
        }
    });

    $("#removeSsoSessionButton").off().on("click", () => {
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
                        url: `${casServerPrefix}/actuator/ssoSessions/users/${username}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: (response, status, xhr) => {
                            ssoSessionsEditor.setValue(JSON.stringify(response, null, 2));
                            ssoSessionsEditor.gotoLine(1);
                            ssoSessionsTable.clear().draw();
                        },
                        error: (xhr, status, error) => {
                            console.error("Error fetching data:", error);
                        }
                    });
                }
            });
    });

    $("button[name=ssoSessionButton]").off().on("click", () => {
        const form = document.getElementById("fmSsoSessions");
        if (!form.reportValidity()) {
            return false;
        }

        ssoSessionsEditor.setValue("");
        const username = $("#ssoSessionUsername").val();
        $.ajax({
            url: `${casServerPrefix}/actuator/ssoSessions/users/${username}`,
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
                    for(const [key, value] of Object.entries(attributes.principal)) {
                        ssoSessionDetailsTable.row.add({
                            0: `<code>Principal</code>`,
                            1: `<code>${key}</code>`,
                            2: `<code>${value}</code>`
                        });
                    }
                    for(const [key, value] of Object.entries(attributes.authentication)) {
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
                                    url: `${casServerPrefix}/actuator/ssoSessions/${ticket}`,
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
                                    }
                                });
                            }
                        });
                });
            },
            error: (xhr, status, error) => {
                console.error("Error fetching data:", error);
            }
        });
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
        $.get(`${casServerPrefix}/actuator/loggingConfig`, response => callback(response)).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });
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
                    const logger = $(this).data("logger");
                    const level = $(this).val();
                    console.log("Logger:", logger, "Level:", level);
                    const loggerData = {
                        "configuredLevel": level
                    };
                    $.ajax({
                        url: `${casServerPrefix}/actuator/loggers/${logger}`,
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(loggerData),
                        success: response => {
                            console.log("Update successful:", response);
                            $(this).css("background-color", determineLoggerColor(level));
                        },
                        error: (xhr, status, error) => {
                            console.error("Failed", error);
                        }
                    });
                });
            }

            console.log(response);

            loggersTable.clear();
            for (const logger of response.loggers) {
                addLoggerToTable(logger);
            }
            loggersTable.draw();

            $("#newLoggerButton").off().on("click", () => {
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
                    });
            });
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
    function configureHealthChart() {
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

    function configureStatistics() {
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

    function configureSystemData() {
        fetchSystemData(response => {
            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);
            console.log("Memory: ", maximum, total, free);
            memoryChart.data.datasets[0].data = [maximum, total, free];
            memoryChart.update();
        });
        $.get(`${casServerPrefix}/actuator/metrics/http.server.requests`, response => {
            let count = response.measurements[0].value;
            let totalTime = response.measurements[1].value.toFixed(2);
            let maxTime = response.measurements[2].value.toFixed(2);
            $("#httpRequestsCount").text(count);
            $("#httpRequestsTotalTime").text(`${totalTime}s`);
            $("#httpRequestsMaxTime").text(`${maxTime}s`);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });
        $.get(`${casServerPrefix}/actuator/metrics/http.server.requests.active`, response => {
            let active = response.measurements[0].value;
            let duration = response.measurements[1].value.toFixed(2);
            $("#httpRequestsActive").text(active);
            $("#httpRequestsDuration").text(`${duration}s`);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
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
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureSystemData();
        }
    }, 5000);

    setInterval(() => {
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureStatistics();
        }
    }, 5000);

    setInterval(() => {
        const index = tabs.foundation.adapter.getFocusedTabIndex();
        if (index === 1) {
            configureHealthChart();
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
                                  <span class="mdc-chip__text">${element.trim().replace("CasFeatureModule.", "")}</span>
                                </span>
                            </div>
                        `.trim();
            $("#casFeaturesChipset").append($(feature));
        }
    });

    configureSystemData();
    configureStatistics();
    configureHealthChart();
}

function initializeServiceButtons() {
    const editor = initializeAceEditor("serviceEditor");
    let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("editServiceDialog"));

    $("button[name=deleteService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
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
                        url: `${casServerPrefix}/actuator/registeredServices/${serviceId}`,
                        type: "DELETE",
                        success: response => {
                            console.log("Resource deleted successfully:", response);
                            let nearestTr = $(this).closest("tr");

                            let applicationsTable = $("#applicationsTable").DataTable();
                            applicationsTable.row(nearestTr).remove().draw();
                        },
                        error: (xhr, status, error) => {
                            console.error("Error deleting resource:", error);
                        }
                    });
                }
            });

    });

    $("button[name=editService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        $.get(`${casServerPrefix}/actuator/registeredServices/${serviceId}`, response => {
            const value = JSON.stringify(response, null, 4);
            editor.setValue(value, -1);
            editor.gotoLine(1);
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            $(editServiceDialogElement).attr("newService", false);
            editServiceDialog["open"]();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
        });

    });

    $("button[name=saveService]").off().on("click", () => {

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
                        url: `${casServerPrefix}/actuator/registeredServices`,
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
                        }
                    });
                }
            });

    });

    $("button[name=copyService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        $.get(`${casServerPrefix}/actuator/registeredServices/${serviceId}`, response => {
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
        });
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

    applicationsTable.on("click", "tbody tr", e => {
        e.currentTarget.classList.remove("selected");
    });

    fetchServices();
    initializeFooterButtons();
}

async function initializeAllCharts() {
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
                label: "JVM Threads",
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
    setTimeout(() => {
        $("nav.sidebar-navigation").css("height", $("#dashboard .mdc-card").css("height"));
    }, 100);
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
            initializeSsoSessionOperations()
        ]);
    } catch (error) {
        console.error("An error occurred:", error);
    }

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    setTimeout(() => {
        const selectedTab = window.localStorage.getItem("PalantirSelectedTab");
        if (selectedTab) {
            tabs.activateTab(Number(selectedTab));
        }
    }, 5);

    tabs.listen("MDCTabBar:activated", ev => {
        const idx = ev.detail.index;
        console.log("Tracking tab", idx);
        window.localStorage.setItem("PalantirSelectedTab", idx);

        let matchingElement = $(`nav.sidebar-navigation ul li[data-tab-index=${idx}]`);
        $("nav.sidebar-navigation ul li").removeClass("active");
        $(matchingElement).addClass("active");
        updateNavigationSidebar();
    });
}

function activateDashboardTab(idx) {
    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.activateTab(Number(idx));
    updateNavigationSidebar();
}

document.addEventListener("DOMContentLoaded", () => {
    initializePalantir().then(r => console.log("Palantir ready!"));
    $("nav.sidebar-navigation ul li").off().on("click", function () {
        $("li").removeClass("active");
        $(this).addClass("active");
        const index = $(this).data("tab-index");
        activateDashboardTab(index);
    });
});
