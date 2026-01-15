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

    function handleLoggerLevelSelectionChange() {
        $("select[name=loggerLevelSelect]").off().on("change", function () {
            if (actuatorEndpoints.loggers) {
                const logger = $(this).data("logger");
                const level = $(this).val();
                const loggerData = {
                    "configuredLevel": level
                };
                $.ajax({
                    url: `${actuatorEndpoints.loggers}/${logger}`,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(loggerData),
                    success: response => $(this).css("background-color", determineLoggerColor(level)),
                    error: (xhr, status, error) => {
                        console.error("Failed", error);
                        displayBanner(xhr);
                    }
                });
            }
        });
    }

    function fetchLoggerData(callback) {
        if (actuatorEndpoints.loggingconfig) {
            $.get(actuatorEndpoints.loggingconfig, response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
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

            loggersTable.clear();
            for (const logger of response.loggers) {
                addLoggerToTable(logger);
            }
            loggersTable.draw();

            $("#newLoggerButton").off().on("click", () =>
                Swal.fire({
                    input: "text",
                    inputAttributes: {
                        autocapitalize: "off"
                    },
                    showConfirmButton: true,
                    showDenyButton: true,
                    icon: "success",
                    title: "What's the name of the new logger?",
                    text: "The new logger will only be effective at runtime and will not be persisted."
                })
                    .then((result) => {
                        if (result.isConfirmed) {
                            addLoggerToTable({name: result.value, level: "INFO"});
                            loggersTable.draw();
                            handleLoggerLevelSelectionChange();
                            loggersTable.search(result.value).draw();
                        }
                    }));
            handleLoggerLevelSelectionChange();
        });
    }

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.LOGGING.index) {
            updateLoggersTable();
        }
    });

    if (currentActiveTab === Tabs.LOGGING.index) {
        updateLoggersTable();
    }

    const logEndpoints = [actuatorEndpoints.cloudwatchlogs, actuatorEndpoints.gcplogs, actuatorEndpoints.loggingconfig, actuatorEndpoints.logfile];
    const hasLogEndpoint = logEndpoints.some(element => element !== undefined);

    if (hasLogEndpoint) {
        const scrollSwitch = new mdc.switchControl.MDCSwitch(document.getElementById("scrollLogsButton"));
        scrollSwitch.selected = true;

        if (actuatorEndpoints.logfile) {
            $("#logFileStream").parent().addClass("w-50");
            $("#logDataStream").parent().addClass("w-50");
        } else {
            $("#logFileStream").parent().addClass("d-none");
            $("#logDataStream").parent().addClass("w-100");
        }

        async function fetchLogsFrom(endpoint) {
            if (endpoint) {
                const logDataStream = $("#logDataStream");
                const level = $("#logLevelFilter").val();
                const count = $("#logCountFilter").val();

                $.ajax({
                    url: `${endpoint}/stream?level=${level}&count=${count}`,
                    type: "GET",
                    dataType: "json",
                    success: logEvents => {
                        logDataStream.empty();
                        logEvents.forEach(log => {
                            let className = `log-${log.level.toLowerCase()}`;
                            const logEntry = `<div>${new Date(log.timestamp).toLocaleString()} - <span class='${className}'>[${log.level}]</span> - ${log.message}</div>`;
                            logDataStream.append($(logEntry));
                        });
                        if (scrollSwitch.selected) {
                            logDataStream.scrollTop(logDataStream.prop("scrollHeight"));
                        }
                    },
                    error: (xhr, status, error) => console.error("Streaming logs failed:", error)
                });
            }
        }

        function startStreamingLogFile() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING.index) {
                    const logFileStream = $("#logFileStream");

                    $.ajax({
                        url: actuatorEndpoints.logfile,
                        type: "GET",
                        success: response => {
                            logFileStream.empty();
                            logFileStream.text(response);
                            if (scrollSwitch.selected) {
                                logFileStream.scrollTop(logFileStream.prop("scrollHeight"));
                            }
                        },
                        error: (xhr, status, error) => console.error("Streaming logs failed:", error)
                    });
                }
            }, $("#logRefreshFilter").val());
        }

        function startStreamingLogData() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING.index) {
                    fetchLogsFrom(actuatorEndpoints.cloudwatchlogs);
                    fetchLogsFrom(actuatorEndpoints.gcplogs);
                    fetchLogsFrom(actuatorEndpoints.loggingconfig);
                }
            }, $("#logRefreshFilter").val());
        }

        let refreshStreamInterval = startStreamingLogData();

        let refreshLogFileInterval = undefined;
        if (actuatorEndpoints.logfile) {
            refreshLogFileInterval = startStreamingLogFile();
        }

        $("#logRefreshFilter").selectmenu({
            change: (event, data) => {
                clearInterval(refreshStreamInterval);
                refreshStreamInterval = startStreamingLogData();

                if (refreshLogFileInterval) {
                    clearInterval(refreshLogFileInterval);
                    refreshLogFileInterval = startStreamingLogFile();
                }
            }
        });

        $("#downloadLogsButton").off().on("click", () => {
            try {
                $("#downloadLogsButton").prop("disabled", true);
                hideBanner();
                const text = $("#logDataStream").text();
                const blob = new Blob([text], {type: "text/plain"});
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "cas.log";
                a.click();
                URL.revokeObjectURL(url);
            } catch (e) {
                console.error("Error downloading log file:", e);
                displayBanner(e);
            } finally {
                $("#downloadLogsButton").prop("disabled", false);
            }
        });

    } else {
        $("#loggingDataStreamOps").parent().addClass("d-none");
    }

}
