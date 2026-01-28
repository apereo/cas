async function initializeSystemOperations() {
    function configureAuditEventsChart() {
        if (CasActuatorEndpoints.auditEvents()) {
            $.get(CasActuatorEndpoints.auditEvents(), response => {
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
        if (CasActuatorEndpoints.httpExchanges()) {
            $.get(CasActuatorEndpoints.httpExchanges(), response => {
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
                displayBanner(xhr);
            });

            $("#downloadHeapDumpButton").off().on("click", () => {
                $("#downloadHeapDumpButton").prop("disabled", true);
                fetch(CasActuatorEndpoints.heapDump())
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

    async function configureHealthChart() {
        function updateHealthChart(response) {
            if (response.components !== undefined) {
                const payload = {
                    labels: [],
                    data: [],
                    colors: []
                };
                Object.keys(response.components).forEach(key => {
                    payload.labels.push(camelcaseToTitleCase(key));
                    payload.data.push(1);
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
            }
        }

        if (CasActuatorEndpoints.health()) {
            $.ajax({
                url: CasActuatorEndpoints.health(),
                method: "GET",
                timeout: 5000,
                success: function (response) {
                    updateHealthChart(response);
                },
                error: function (xhr, textStatus, errorThrown) {
                    if (xhr.status === 503) {
                        const response = xhr.responseJSON;
                        updateHealthChart(response);
                    } else {
                        console.error("Error fetching health data:", errorThrown);
                        displayBanner(xhr);
                    }
                }
            });
        }
    }

    async function configureStatistics() {
        if (CasActuatorEndpoints.statistics()) {
            $.get(CasActuatorEndpoints.statistics(), response => {

                const expired = response.expiredTickets;
                const valid = response.validTickets;
                statisticsChart.data.datasets[0].data = [valid, expired];
                statisticsChart.update();
            }).fail((xhr, status, error) => console.error("Error fetching data:", error));
        }
    }

    async function fetchSystemData(callback) {
        if (CasActuatorEndpoints.info()) {
            $.get(CasActuatorEndpoints.info(), response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    }

    async function configureSystemMetrics() {
        $("#systemMetricNameFilter").selectmenu({
            change: function () {
                $(this).selectmenu("close");
                const metric = $(this).val();
                systemMetricsTagsTable.clear();
                systemMetricsMeasurementsTable.clear();
                $("#systemMetricNameDescriptionContainer").hide();

                if (metric && metric.length > 0) {
                    $.get(`${CasActuatorEndpoints.metrics()}/${metric}`, response => {
                        let description = `${response.description ?? "No description is available"}. Metric is measured in ${response.baseUnit ?? "unknown units"}.`;
                        $("#systemMetricNameDescription").text(description);
                        $("#systemMetricNameDescriptionContainer").show();

                        response.availableTags.forEach(entry => {
                            systemMetricsTagsTable.row.add({
                                0: `<code>${entry.tag}</code>`,
                                1: `<code>${entry.values.join(",")}</code>`
                            });
                        });
                        systemMetricsTagsTable.draw();
                        response.measurements.forEach(entry => {
                            systemMetricsMeasurementsTable.row.add({
                                0: `<code>${entry.statistic}</code>`,
                                1: `<code>${entry.value}</code>`
                            });
                        });
                        systemMetricsMeasurementsTable.draw();
                    });
                }
            }
        });

        $("#systemMetricNameFilter").empty();
        $("#systemMetricNameFilter").append(
            $("<option>", {
                value: "",
                text: "Select a metric to view details..."
            })
        );

        if (CasActuatorEndpoints.metrics()) {
            $.get(CasActuatorEndpoints.metrics(), response => {
                for (const name of response.names) {
                    $("#systemMetricNameFilter").append(
                        $("<option>", {
                            value: name,
                            text: name
                        })
                    );
                    $("#systemMetricNameFilter").selectmenu("refresh");
                }
            });
        }
    }

    async function configureSystemData() {
        await fetchSystemData(response => {

            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);

            memoryChart.data.datasets[0].data = [maximum, total, free];
            memoryChart.update();
        });

        if (CasActuatorEndpoints.metrics()) {
            $.get(`${CasActuatorEndpoints.metrics()}/http.server.requests`, response => {
                let count = response.measurements[0].value;
                let totalTime = response.measurements[1].value.toFixed(2);
                let maxTime = response.measurements[2].value.toFixed(2);
                $("#httpRequestsCount").text(count);
                $("#httpRequestsTotalTime").text(`${totalTime}s`);
                $("#httpRequestsMaxTime").text(`${maxTime}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
            $.get(`${CasActuatorEndpoints.metrics()}/http.server.requests.active`, response => {
                let active = response.measurements[0].value;
                let duration = response.measurements[1].value.toFixed(2);
                $("#httpRequestsActive").text(active);
                $("#httpRequestsDuration").text(`${duration}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
        configureHttpRequestResponses().then(configureAuditEventsChart());
    }

    const systemTable = $("#systemTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemTable tr").addClass("mdc-data-table__row");
            $("#systemTable td").addClass("mdc-data-table__cell");
        }
    });

    const systemMetricsTagsTable = $("#systemMetricsTagsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemMetricsTagsTable tr").addClass("mdc-data-table__row");
            $("#systemMetricsTagsTable td").addClass("mdc-data-table__cell");
        }
    });

    const systemMetricsMeasurementsTable = $("#systemMetricsMeasurementsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemMetricsMeasurementsTable tr").addClass("mdc-data-table__row");
            $("#systemMetricsMeasurementsTable td").addClass("mdc-data-table__cell");
        }
    });

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));

    async function configureSystemInfo() {
        await fetchSystemData(response => {
            const flattened = flattenJSON(response);
            systemTable.clear();

            for (const [key, value] of Object.entries(flattened)) {
                systemTable.row.add({
                    0: `<code>${key}</code>`,
                    1: `<code>${value}</code>`
                });
            }
            systemTable.draw();

            highlightElements();
            $("#casServerPrefix").text(casServerPrefix);
            $("#casServerHost").text(response.server["host"]);
        });
    }

    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.SYSTEM.index) {
            configureSystemInfo();
        }
    });


    setInterval(() => {
        if (currentActiveTab === Tabs.SYSTEM.index) {
            configureSystemData();
            configureHealthChart();
            configureStatistics();
        }
    }, palantirSettings().refreshInterval);

    await configureSystemData()
        .then(configureStatistics())
        .then(configureHealthChart())
        .then(configureSystemInfo())
        .then(configureSystemMetrics());

    $("button[name=shutdownServerButton]").off().on("click", function () {
        Swal.fire({
            title: "Are you sure you want to shut the server down?",
            text: "Once confirmed, the server will begin shutdown procedures. Note that this operation does not support clustered deployments.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: CasActuatorEndpoints.shutdown(),
                        type: "POST",
                        headers: {"Content-Type": "application/json"},
                        success: response => {
                            Swal.fire("Shutting Down...", "CAS will start to shutdown shortly. You may close this window.", "info");
                        },
                        error: (xhr, status, error) => {
                            console.error("Error deleting resource:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    });

    $("button[name=restartServerButton]").off().on("click", function () {
        Swal.fire({
            title: "Are you sure you want to restart the server?",
            text: "Once confirmed, the server will begin restarting. Note that this operation does not support clustered deployments.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: CasActuatorEndpoints.restart(),
                        type: "POST",
                        headers: {"Content-Type": "application/json"},
                        success: response => {
                            Swal.fire({
                                icon: "info",
                                title: `Restarting CAS`,
                                text: "Please wait while the CAS server is restarting...",
                                allowOutsideClick: false,
                                showConfirmButton: false,
                                didOpen: () => Swal.showLoading()
                            });
                            waitForActuator(CasActuatorEndpoints.info()).then(function () {
                                Swal.close();
                            });
                        },
                        error: (xhr, status, error) => {
                            console.error("Error deleting resource:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    });
}
