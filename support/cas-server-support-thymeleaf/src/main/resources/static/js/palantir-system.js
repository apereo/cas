
async function initializeSystemOperations() {
    function configureAuditEventsChart() {
        if (CasActuatorEndpoints.auditEvents()) {
            $.get(CasActuatorEndpoints.auditEvents(), response => {
                const results = response.events.reduce((accumulator, event) => {
                    const timestamp = formatDateYearMonthDay(event.timestamp);
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

                const auditData = Object.entries(results)
                    .sort(([firstTimestamp], [secondTimestamp]) => firstTimestamp.localeCompare(secondTimestamp))
                    .map(([timestamp, eventCounts]) => ({timestamp, ...eventCounts}));
                const eventTypes = [...new Set(auditData.flatMap(entry => Object.keys(entry)))]
                    .filter(type => type !== "timestamp" && type !== "AUTHORIZATION_FAILURE")
                    .sort();

                if (!auditEventsChart) {
                    return;
                }
                auditEventsChart.data.labels = auditData.map(d => d.timestamp);
                auditEventsChart.data.datasets = eventTypes.map(type => ({
                    borderWidth: 2,
                    data: auditData.map(entry => entry[type] ?? 0),
                    label: type
                }));
                auditEventsChart.update();
            });
        }
    }

    async function configureHttpRequestResponses() {
        configureHttpExchangeControls();
        if (!CasActuatorEndpoints.httpExchanges()) {
            hideHttpExchangesTab();
            return;
        }
        await refreshHttpExchanges();

        $("#downloadHeapDumpButton").off().on("click", () => {
            $("#downloadHeapDumpButton").prop("disabled", true);
            fetch(CasActuatorEndpoints.heapDump(), {
                credentials: 'include'
            })
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

    async function configureHttpTraces() {
        configureHttpTraceControls();
        await refreshHttpTraces();
    }

    let healthRequestPending = false;

    /**
     * Read the health checks from the summary behind Palantir's home page, which runs each check on its own, waits a
     * bounded time for it, shows a check still running as slow, and keeps the answer for a while. The answer is
     * shaped like the health endpoint's, with a check still running reported as SLOW. Older servers without the
     * summary answer null, and the health endpoint is read instead.
     *
     * @returns {Promise<object|null>} the health, or null when the summary cannot be read
     */
    async function fetchHealthFromOverview() {
        try {
            const response = await fetch(`${retrieveDashboardUrl()}/overview?sections=health`, {credentials: "include", headers: {"Accept": "application/json"}});
            if (!response.ok) {
                return null;
            }
            const health = (await response.json()).sections?.health;
            if (!health || !Array.isArray(health.components)) {
                return null;
            }
            return {
                status: health.status,
                components: Object.fromEntries(health.components.map(component => [component.name,
                    {status: component.status, millis: component.millis}]))
            };
        } catch (error) {
            console.debug("The health checks are read from the health endpoint:", error);
            return null;
        }
    }

    async function configureHealthChart() {
        function updateHealthChart(response) {
            if (response.components !== undefined && systemHealthChart) {
                const payload = {
                    labels: [],
                    data: [],
                    colors: [],
                    descriptions: []
                };
                const timed = Object.values(response.components).some(component => Number.isFinite(Number(component.millis)));
                Object.keys(response.components).forEach(key => {
                    const component = response.components[key];
                    payload.labels.push(camelcaseToTitleCase(key));
                    payload.data.push(timed ? Math.max(1, Number(component.millis) || 1) : 1);
                    payload.colors.push(palantirHealthColor(component.status));
                    payload.descriptions.push(palantirHealthDescription(component.status, component.millis));
                });
                systemHealthChart.options.scales = palantirHealthScales(timed);
                systemHealthChart.options.plugins.title.text = timed ? "System Health · time each check took" : "System Health";
                systemHealthChart.data.labels = payload.labels;
                systemHealthChart.data.datasets[0].data = payload.data;
                systemHealthChart.data.datasets[0].backgroundColor = [...payload.colors];
                systemHealthChart.data.datasets[0].borderColor = [...payload.colors];
                systemHealthChart.options.plugins.legend.labels.generateLabels = (chart => {
                    const originalLabels = Chart.defaults.plugins.legend.labels.generateLabels(chart);
                    originalLabels.forEach(label => {
                        label.fillStyle = palantirHealthColor(response.status);
                        label.lineWidth = 0;
                    });
                    return originalLabels;
                });
                systemHealthChart.options.plugins.tooltip = {
                    ...systemHealthChart.options.plugins.tooltip,
                    callbacks: {label: context => payload.descriptions[context.dataIndex]}
                };
                systemHealthChart.update();
            }
        }

        if (!CasActuatorEndpoints.health() || healthRequestPending) {
            return;
        }
        healthRequestPending = true;
        return fetchHealthFromOverview().then(overview => {
            if (overview !== null) {
                updateHealthChart(overview);
                healthRequestPending = false;
                return;
            }
            $.ajax({
                url: CasActuatorEndpoints.health(),
                method: "GET",
                success: function (response) {
                    updateHealthChart(response);
                },
                error: function (xhr, textStatus, errorThrown) {
                    if (xhr.status === 503) {
                        const response = xhr.responseJSON;
                        updateHealthChart(response);
                    } else if (xhr.status === 0) {
                        console.warn("Health data is not available at the moment:", textStatus);
                    } else {
                        console.error("Error fetching health data:", errorThrown);
                        displayBanner(xhr);
                    }
                },
                complete: function () {
                    healthRequestPending = false;
                }
            });
        });
    }

    let statisticsRequestPending = false;
    let statisticsCounted = 0;
    const STATISTICS_REFRESH = 60000;

    /**
     * Read the ticket counts for the Ticket Registry chart: all tickets, then a bar for every kind of ticket the
     * registry holds, largest first, named the way CAS names it. Counting can take a moment in a registry that holds
     * many tickets, so the chart says so until the first answer, refreshes once a minute, and a refresh waits for the
     * one before it.
     */
    async function configureStatistics() {
        if (!CasActuatorEndpoints.statistics()) {
            $("#statisticsChartStatus").addClass("hide");
            return;
        }
        if (statisticsRequestPending) {
            return;
        }
        statisticsRequestPending = true;
        $.get(CasActuatorEndpoints.statistics(), response => {
            statisticsCounted = Date.now();
            const total = Number(response.tickets);
            const types = (Array.isArray(response.ticketsByType) ? response.ticketsByType : [])
                .filter(type => Number(type.count) > 0);
            const unknown = !Number.isFinite(total) || total < 0;
            $("#statisticsChartStatus").text(unknown
                ? "This ticket registry could not count its tickets just now."
                : `Counted at ${new Date(response.counted ?? Date.now()).toLocaleTimeString()} · every minute`);
            if (statisticsChart) {
                statisticsChart.data.labels = ["All Tickets", ...types.map(type => type.name)];
                statisticsChart.data.ticketTypes = ["All Tickets", ...types.map(type => `${type.name} (${type.prefix})`)];
                statisticsChart.data.datasets[0].data = [unknown ? 0 : total, ...types.map(type => Number(type.count))];
                statisticsChart.update();
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            $("#statisticsChartStatus").text("The ticket counts could not be read just now; trying again in a minute.");
        }).always(() => statisticsRequestPending = false);
    }

    let slowestRequestPending = false;
    let slowestMeasured = 0;

    /**
     * Read the slowest URLs for the Slowest Endpoints chart from the summary behind Palantir's home page, which the
     * server builds from the HTTP request timers it already keeps: one request, refreshed once a minute. The chart is
     * hidden on servers whose summary has no such section.
     */
    async function configureSlowestEndpoints() {
        if (!slowestEndpointsChart || slowestRequestPending) {
            return;
        }
        slowestRequestPending = true;
        $.get(`${retrieveDashboardUrl()}/overview`, {sections: "endpoints"}, response => {
            slowestMeasured = Date.now();
            const endpoints = response?.sections?.endpoints;
            if (!endpoints || endpoints.unavailable) {
                $("#slowestEndpointsChartCell").addClass("hide");
                return;
            }
            $("#slowestEndpointsChartCell").removeClass("hide");
            if (endpoints.pending) {
                $("#slowestEndpointsChartStatus").text("Measuring the slowest endpoints…");
                return;
            }
            const slowest = endpoints.slowest ?? [];
            $("#slowestEndpointsChartStatus").text(slowest.length === 0
                ? "No requests to measure yet, apart from Palantir's own, actuator endpoints and static files."
                : `Slowest ${slowest.length} of ${endpoints.measured} URLs by average, since CAS started · every minute`);
            slowestEndpointsChart.data.labels = slowest.map(endpoint => endpoint.uri);
            slowestEndpointsChart.data.endpoints = slowest;
            slowestEndpointsChart.data.datasets[0].data = slowest.map(endpoint => endpoint.meanSeconds * 1000);
            slowestEndpointsChart.data.datasets[1].data = slowest.map(endpoint => endpoint.maxSeconds * 1000);
            slowestEndpointsChart.update();
        }).fail(() => {
            slowestMeasured = Date.now();
            $("#slowestEndpointsChartStatus").text("The slowest endpoints could not be read just now; trying again in a minute.");
        }).always(() => slowestRequestPending = false);
    }

    let providersRequestPending = false;
    let providersCounted = 0;

    /**
     * Read multifactor authentication per provider over the last two hours, counted by the audit storage from the
     * provider each triggered event names, refreshed once a minute. An audit storage that cannot count hides the chart;
     * the sign-ins chart says why.
     */
    async function configureMultifactorProviders() {
        if (!CasActuatorEndpoints.auditLog() || !multifactorProvidersChart) {
            $("#multifactorProvidersChartCell").addClass("hide");
            return;
        }
        if (providersRequestPending) {
            return;
        }
        providersRequestPending = true;
        $.get(`${CasActuatorEndpoints.auditLog()}/summary`, {
            interval: "PT2H",
            actions: "AUTHENTICATION_EVENT_TRIGGERED",
            groupBy: "target"
        }, response => {
            providersCounted = Date.now();
            if (!response?.supported) {
                $("#multifactorProvidersChartCell").addClass("hide");
                return;
            }
            $("#multifactorProvidersChartCell").removeClass("hide");
            const counts = (response.counts ?? []).filter(count => count.target && count.action === "AUTHENTICATION_EVENT_TRIGGERED");
            const top = drawPalantirCountsByTarget(multifactorProvidersChart, counts);
            $("#multifactorProvidersChartStatus").text(top.length === 0
                ? "No multifactor authentication in the last 2 hours."
                : `Top ${top.length} of ${top.measured} · every minute`);
        }).fail(() => {
            providersCounted = Date.now();
            $("#multifactorProvidersChartStatus").text("This could not be counted just now; trying again in a minute.");
        }).always(() => providersRequestPending = false);
    }

    let signInsRequestPending = false;
    let signInsCounted = 0;
    const SIGN_IN_SERIES = ["AUTHENTICATION_SUCCESS", "AUTHENTICATION_FAILED", "THROTTLED_LOGIN_ATTEMPT", "AUTHENTICATION_EVENT_TRIGGERED"];
    const HOUR = 3600000;

    function auditHourOf(value) {
        if (Array.isArray(value)) {
            return Date.UTC(value[0], value[1] - 1, value[2], value[3] ?? 0, value[4] ?? 0);
        }
        return Date.parse(`${String(value).replace(/Z$/, "")}Z`);
    }

    function generatedTime(value) {
        if (typeof value === "number") {
            return value < 1e11 ? value * 1000 : value;
        }
        const parsed = Date.parse(value);
        return Number.isFinite(parsed) ? parsed : Date.now();
    }

    /**
     * Read sign-ins per hour over the last two hours for the Sign-ins chart, as the audit storage counts them across the
     * cluster: succeeded, failed and throttled sign-ins stacked, and multifactor authentication triggered as a line.
     * Refreshed once a minute, which is as long as the server keeps a count. An audit storage that cannot count its
     * records says so in place of the chart.
     */
    async function configureSignIns() {
        if (!CasActuatorEndpoints.auditLog() || !signInsChart) {
            $("#signInsChartCell").addClass("hide");
            return;
        }
        if (signInsRequestPending) {
            return;
        }
        signInsRequestPending = true;
        $.get(`${CasActuatorEndpoints.auditLog()}/summary`, {interval: "PT2H", actions: SIGN_IN_SERIES.join(",")}, response => {
            signInsCounted = Date.now();
            const chartArea = $("#signInsChart").closest(".palantir-sign-ins-chart");
            if (!response?.supported) {
                chartArea.addClass("hide");
                $("#signInsChartStatus").text("The audit storage of this server cannot count sign-ins per hour. "
                    + "Audit records kept in a database, MongoDB, Redis, DynamoDB or a REST service can be counted.");
                return;
            }
            chartArea.removeClass("hide");
            const first = auditHourOf(response.query.from);
            const hours = Array.from({length: Math.max(0, Math.round((auditHourOf(response.query.to) - first) / HOUR))},
                (value, index) => first + index * HOUR);
            const series = SIGN_IN_SERIES.map(() => hours.map(() => 0));
            (response.counts ?? []).forEach(count => {
                const index = Math.round((auditHourOf(count.hour) - first) / HOUR);
                const position = SIGN_IN_SERIES.indexOf(count.action);
                if (position >= 0 && index >= 0 && index < hours.length) {
                    series[position][index] += Number(count.count) || 0;
                }
            });
            const signIns = series.slice(0, 3).flat().reduce((total, value) => total + value, 0);
            $("#signInsChartStatus").text(`${signIns.toLocaleString()} sign-ins · counted at `
                + `${new Date(generatedTime(response.generated)).toLocaleTimeString()} · every minute`);
            signInsChart.data.labels = hours.map(hour => new Date(hour).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"}));
            signInsChart.data.hours = hours.map(hour => new Date(hour).toLocaleString([], {
                weekday: "short", hour: "2-digit", minute: "2-digit"
            }));
            series.forEach((values, position) => signInsChart.data.datasets[position].data = values);
            signInsChart.update();
        }).fail(() => {
            $("#signInsChartStatus").text("Sign-ins could not be counted just now; trying again in a minute.");
        }).always(() => signInsRequestPending = false);
    }

    let systemDataRequest = null;

    /**
     * Read the info actuator, coalescing concurrent readers onto one request.
     *
     * The memory chart and the system information table are refreshed together and both need this
     * payload; without coalescing each refresh issued the same request twice.
     *
     * @param callback invoked with the info payload when it arrives
     * @returns {Promise<void>} resolved once the callback has run, or immediately when the
     *     endpoint is unavailable
     */
    async function fetchSystemData(callback) {
        if (!CasActuatorEndpoints.info()) {
            return;
        }
        if (!systemDataRequest) {
            systemDataRequest = $.get(CasActuatorEndpoints.info())
                .fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                })
                .always(() => queueMicrotask(() => systemDataRequest = null));
        }
        try {
            callback(await systemDataRequest);
        } catch (e) {
            console.debug("Unable to read system data", e);
        }
    }

    if (!CasActuatorEndpoints.casModules()) {
        setSystemJqueryTabAvailable("#casmodulestab", "#casmodules-tab", false);
    }

    /**
     * Load what the shown tab under System Info lists: the modules, the dependencies or the vulnerabilities.
     *
     * @returns {Promise<void>} resolved once it is loaded
     */
    async function refreshActiveSystemInfoTab() {
        const tabs = $("#system-info-tabs");
        if (!tabs.data("ui-tabs")) {
            return;
        }
        const panel = tabs.find("> ul > li").eq(tabs.tabs("option", "active")).find("a").attr("href");
        if (panel === "#casmodules-tab") {
            await PalantirModules.load();
        } else if (panel === "#casdependencies-tab") {
            await refreshDependencies();
        } else if (panel === "#casvulnerabilities-tab") {
            await refreshVulnerabilities();
        }
    }

    async function refreshDependencies() {
        if (dependenciesLoaded) {
            return;
        }
        if (!CasActuatorEndpoints.dependencies()) {
            hideDependenciesTab();
            return;
        }
        return new Promise(resolve => {
            $.get(CasActuatorEndpoints.dependencies(), response => {
                setSystemJqueryTabAvailable("#casdependenciestab", "#casdependencies-tab", true);
                const dependencies = Array.isArray(response) ? response : [];
                casDependenciesTable.clear();
                dependencies
                    .map(normalizeDependency)
                    .forEach(dependency => casDependenciesTable.row.add(dependency));
                casDependenciesTable.draw();
                casDependenciesTable.columns.adjust();
                dependenciesLoaded = true;
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching dependency data:", error);
                hideDependenciesTab();
                resolve();
            });
        });
    }

    async function refreshVulnerabilities() {
        if (vulnerabilitiesLoaded) {
            return;
        }
        const endpoint = dependenciesVulnerabilitiesEndpoint();
        if (!endpoint) {
            hideVulnerabilitiesTab();
            return;
        }
        $("#casVulnerabilitiesStatus").empty();
        $("#casvulnerabilities-tab").addClass("is-loading");
        $("#casVulnerabilitiesTable").closest(".dt-container, table").attr("aria-busy", "true");
        return new Promise(resolve => {
            $.get(endpoint, response => {
                setSystemJqueryTabAvailable("#casvulnerabilitiestab", "#casvulnerabilities-tab", true);
                const vulnerabilities = Array.isArray(response.vulnerabilities) ? response.vulnerabilities : [];
                casVulnerabilitiesTable.clear();
                const vulnerabilityRows = [];
                vulnerabilities.forEach(vulnerability => {
                    const details = vulnerability.details ?? {};
                    const row = {
                        id: details.id ?? vulnerability.id ?? "",
                        modified: details.modified ?? vulnerability.modified ?? "",
                        severity: normalizeVulnerabilitySeverity(details.severity ?? vulnerability.severity),
                        dependency: normalizeDependency(vulnerability.dependency)
                    };
                    vulnerabilityRows.push(row);
                    casVulnerabilitiesTable.row.add(row);
                });
                casVulnerabilitiesTable.draw();
                casVulnerabilitiesTable.columns.adjust();
                renderVulnerabilitySeverityBreakdown(vulnerabilityRows);
                const errors = Array.isArray(response.errors) ? response.errors : [];
                $("#casVulnerabilitiesStatus").text(errors.length > 0 ? errors.join("; ") : "");
                vulnerabilitiesLoaded = true;
                $("#casvulnerabilities-tab").removeClass("is-loading");
                casVulnerabilitiesTable.columns.adjust();
                $("#casVulnerabilitiesTable").closest(".dt-container, table").removeAttr("aria-busy");
                updateNavigationSidebar();
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching dependency vulnerability data:", error);
                $("#casVulnerabilitiesStatus").empty();
                $("#casvulnerabilities-tab").removeClass("is-loading");
                $("#casVulnerabilitiesTable").closest(".dt-container, table").removeAttr("aria-busy");
                hideVulnerabilitiesTab();
                updateNavigationSidebar();
                resolve();
            });
        });
    }

    function configureHttpExchangeControls() {
        if (httpExchangesControlsInitialized) {
            return;
        }
        httpExchangesControlsInitialized = true;

            $("#httpExchangesMethodFilter, #httpExchangesStatusFilter").selectmenu({
                width: "100%",
                change: function () {
                    $(this).selectmenu("close");
                    renderHttpExchanges();
                }
            });
        }

    function renderHttpExchanges() {
        const filtered = httpExchangeEntries.filter(httpExchangeMatchesFilters);
        httpExchangesTable.clear();
        filtered.forEach(entry => httpExchangesTable.row.add(entry));
        httpExchangesTable.draw();
        httpExchangesTable.columns.adjust();
    }

    async function refreshHttpExchanges() {
        if ($("#httpExchangesTable tbody tr.shown").length > 0) {
            return;
        }
        return new Promise(resolve => {
            $.get(CasActuatorEndpoints.httpExchanges(), response => {
                setHttpExchangesAvailable();
                httpExchangeEntries = (response.exchanges ?? [])
                    .map(normalizeHttpExchange)
                    .filter(httpExchangeShouldBeDisplayed)
                    .sort((a, b) => b.timestampMs - a.timestampMs);
                updateHttpExchangeMethodOptions(httpExchangeEntries);
                renderHttpExchanges();
                updateHttpExchangeCharts(httpExchangeEntries);
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching HTTP exchanges data:", error);
                if (xhr.status === 404 || xhr.status === 405) {
                    hideHttpExchangesTab();
                } else {
                    $("#httpExchangesStatus").text("Unable to load HTTP exchanges.");
                    displayBanner(xhr);
                }
                resolve();
            });
        });
    }

    function renderHttpTraces() {
        const entries = $("#httpTracesErrorsOnly").val() === "true"
            ? httpTraceEntries.filter(entry => entry.error)
            : httpTraceEntries;
        httpTracesTable.clear();
        entries.forEach(entry => httpTracesTable.row.add(entry));
        httpTracesTable.draw();
        httpTracesTable.columns.adjust();
    }

    async function refreshHttpTraces() {
        if (!CasActuatorEndpoints.httpTraces()) {
            setHttpTracesUnavailable();
            return;
        }
        return new Promise(resolve => {
            $.get(CasActuatorEndpoints.httpTraces(), response => {
                setHttpTracesAvailable();
                httpTraceEntries = (Array.isArray(response) ? response : response.traces ?? response.summaries ?? [])
                    .map(normalizeHttpTraceSummary)
                    .sort((a, b) => b.startedAtMs - a.startedAtMs);
                renderHttpTraces();
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching HTTP traces data:", error);
                if (xhr.status === 404 || xhr.status === 405) {
                    setHttpTracesUnavailable();
                } else {
                    displayBanner(xhr);
                }
                resolve();
            });
        });
    }

    function configureHttpTraceControls() {
        if (httpTracesControlsInitialized) {
            return;
        }
        httpTracesControlsInitialized = true;
        $("#refreshHttpTracesButton").off("click").on("click", () => refreshHttpTraces());
            $("#httpTracesErrorsOnly").off("change").on("change", () => renderHttpTraces());
        }

    let heapDumpAnalysisEntries = [];
    let httpExchangeEntries = [];
    let httpTraceEntries = [];
    let httpExchangesControlsInitialized = false;
    let httpTracesControlsInitialized = false;
    let dependenciesLoaded = false;
    let vulnerabilitiesLoaded = false;

    function renderHttpRequestMappings(entries) {
        httpRequestMappingsTable.clear();
        entries.forEach(entry => httpRequestMappingsTable.row.add(entry));
        httpRequestMappingsTable.draw();
        httpRequestMappingsTable.columns.adjust();
    }

    async function refreshHttpRequestMappings() {
        if (!CasActuatorEndpoints.mappings()) {
            hideElements($("#httprequestsmappingstab").parent());
            return;
        }
        return new Promise(resolve => {
            $.get(CasActuatorEndpoints.mappings(), response => {
                const entries = getHttpRequestMappingEntries(response);
                renderHttpRequestMappings(entries);
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching HTTP request mappings:", error);
                hideElements($("#httprequestsmappingstab").parent());
                if (xhr.status !== 404 && xhr.status !== 405) {
                    displayBanner(xhr);
                }
                resolve();
            });
        });
    }

    function renderHeapDumpClasses(entries) {
        const filtered = entries.slice(0, 50);
        const maxRetained = Math.max(...filtered.map(entry => entry.retained), 0);

        heapDumpClassesTable.clear();
        filtered.forEach((entry, index) => {
            const width = maxRetained > 0 ? Math.max(2, (entry.retained / maxRetained) * 100) : 0;
            heapDumpClassesTable.row.add({
                rank: index + 1,
                className: entry.className,
                instances: entry.instances,
                retained: entry.retained,
                retainedWidth: width,
                averageShallowSize: entry.averageShallowSize,
                averageRetainedSize: entry.averageRetainedSize,
                retainedToShallowRatio: entry.retainedToShallowRatio
            });
        });
        heapDumpClassesTable.draw();
        heapDumpClassesTable.columns.adjust();
    }

    async function refreshHeapDumpAnalysis() {
        if (!CasActuatorEndpoints.heapDump() || !CasActuatorEndpoints.heapDumpAnalysis()) {
            hideElements($("#heapDumpAnalysisContainer"));
            return;
        }
        if (!CasActuatorEndpoints.writable("heapDumpAnalysis")) {
            notyf.dismissAll();
            notyf.error(palantirReadOnlyMessage("heapDumpAnalysis"));
            return;
        }
        heapDumpClassesTable.clear();
        const refreshButton = $("#refreshHeapDumpAnalysisButton");
        refreshButton.prop("disabled", true);
        showHeapDumpAnalysisProgress("Capturing heap dump...", 20);

        try {
            const heapDump = await fetchHeapDumpFile();
            updateHeapDumpAnalysisProgress("Uploading heap dump for analysis...", 55);
            const response = await analyzeHeapDumpFile(heapDump);
            updateHeapDumpAnalysisProgress("Rendering heap dump analysis...", 85);
            heapDumpAnalysisEntries = getHeapDumpAnalysisEntries(response)
                .map(normalizeHeapDumpEntry)
                .filter(entry => entry.className && entry.retained > 0)
                .sort((a, b) => b.retained - a.retained);

            if (heapDumpAnalysisEntries.length === 0) {
                $("#heapDumpAnalysisStatus").text("Heap dump analysis returned no class histogram data.");
                renderHeapDumpClasses(heapDumpAnalysisEntries);
                Swal.fire("No Results", "Heap dump analysis returned no class histogram data.", "info");
                return;
            }

            const totalObjects = getHeapDumpTotalObjects(response, heapDumpAnalysisEntries);
            const totalBytes = getHeapDumpTotalBytes(response, heapDumpAnalysisEntries);
            $("#heapDumpClassesSummary").text(`${formatNumber(totalObjects)} objects · ${formatMemoryBytes(totalBytes)}`);
            response.bigObjects === true
                ? showElements($("#heapDumpBigObjectsBadge"))
                : hideElements($("#heapDumpBigObjectsBadge"));
            response.collectionBloat === true
                ? showElements($("#heapDumpCollectionBloatBadge"))
                : hideElements($("#heapDumpCollectionBloatBadge"));
            renderHeapDumpClasses(heapDumpAnalysisEntries);
            $("#heapDumpAnalysisStatus").text(`Updated ${new Date().toLocaleTimeString()}`);
            showElements($("#heapDumpAnalysisContainer"));
            updateHeapDumpAnalysisProgress("Done.", 100);
            Swal.close();
        } catch (error) {
            console.debug("Unable to fetch heap dump analysis data", error);
            $("#heapDumpAnalysisStatus").text("Unable to analyze heap dump.");
            Swal.fire("Error", getHeapDumpAnalysisErrorMessage(error), "error");
        } finally {
            refreshButton.prop("disabled", false);
        }
    }

    async function configureHeapDumpAnalysis() {
        if (!CasActuatorEndpoints.heapDump() || !CasActuatorEndpoints.heapDumpAnalysis()) {
            hideElements($("#heapDumpAnalysisContainer"));
            return;
        }

        showElements($("#heapDumpAnalysisContainer"));
        $("#refreshHeapDumpAnalysisButton").off("click").on("click", refreshHeapDumpAnalysis);
        window.refreshHeapDumpAnalysis = refreshHeapDumpAnalysis;

        if (heapDumpAnalysisEntries.length === 0) {
            heapDumpClassesTable.clear().draw();
        }
    }

    async function configureLiveMemory() {
        if (!CasActuatorEndpoints.metrics()) {
            liveMemoryPoolsTable.clear().draw();
            hideElements($("#systemLiveMemoryContainer"));
            return;
        }
        if (!$("#systemLiveMemoryContainer").is(":visible") && liveMemoryPoolsTable.data().count() > 0) {
            return;
        }

        const [heapUsed, heapCommitted, heapMax, nonHeapUsed, nonHeapCommitted, nonHeapMax, poolNames] = await Promise.all([
            getMemoryMetricValue("jvm.memory.used", ["area:heap"]),
            getMemoryMetricValue("jvm.memory.committed", ["area:heap"]),
            getMemoryMetricValue("jvm.memory.max", ["area:heap"]),
            getMemoryMetricValue("jvm.memory.used", ["area:nonheap"]),
            getMemoryMetricValue("jvm.memory.committed", ["area:nonheap"]),
            getMemoryMetricValue("jvm.memory.max", ["area:nonheap"]),
            getMemoryPoolNames()
        ]);

        const summaryCards = [
            renderLiveMemoryCard("Heap Memory", "mdi-database", "heap", {
                used: heapUsed,
                committed: heapCommitted,
                max: heapMax
            }, "Heap usage"),
            renderLiveMemoryCard("Non-Heap Memory", "mdi-memory", "nonheap", {
                used: nonHeapUsed,
                committed: nonHeapCommitted,
                max: nonHeapMax
            }, "Metaspace, code cache, and JIT buffers")
        ].filter(card => card.length > 0);

        const poolMetrics = await Promise.all(poolNames.map(async name => {
            const [used, committed, max] = await Promise.all([
                getMemoryMetricValue("jvm.memory.used", [`id:${name}`]),
                getMemoryMetricValue("jvm.memory.committed", [`id:${name}`]),
                getMemoryMetricValue("jvm.memory.max", [`id:${name}`])
            ]);
            return {name, used, committed, max};
        }));
        const pools = poolMetrics.filter(pool => Number.isFinite(pool.used));

        if (summaryCards.length === 0 && pools.length === 0) {
            liveMemoryPoolsTable.clear().draw();
            hideElements($("#systemLiveMemoryContainer"));
            return;
        }

        $("#systemLiveMemorySummary").html(summaryCards.join(""));
        showElements($("#systemLiveMemoryContainer"));
        if (pools.length > 0) {
            showElements($("#systemLiveMemoryPoolsSection"));
            liveMemoryPoolsTable.clear();
            pools.forEach(pool => liveMemoryPoolsTable.row.add(renderLiveMemoryPoolRow(pool)));
            liveMemoryPoolsTable.draw();
            liveMemoryPoolsTable.columns.adjust();
        } else {
            liveMemoryPoolsTable.clear().draw();
            hideElements($("#systemLiveMemoryPoolsSection"));
        }
    }

    async function configureSystemMetrics() {
        PalantirMetrics.initialize();
    }

    async function fetchAvailableMetricNames() {
        if (!CasActuatorEndpoints.metrics()) {
            return new Set();
        }
        return new Promise(resolve => {
            $.get(CasActuatorEndpoints.metrics(), response => resolve(new Set(response.names ?? [])))
                .fail((xhr, status, error) => {
                    console.debug("Unable to fetch metric names", error);
                    resolve(new Set());
                });
        });
    }

    async function configureSystemData() {
        await fetchSystemData(response => {

            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);

            const used = Math.max(0, total - free);
            $("#memoryChartStatus").text(Number.isFinite(maximum) && maximum > 0
                ? `${used.toFixed(1)} GB in use of ${maximum.toFixed(1)} GB · ${Math.round(used / maximum * 100)}%`
                : `${used.toFixed(1)} GB in use`);
            if (!memoryChart) {
                return;
            }
            memoryChart.data.datasets[0].data = [used, total, maximum];
            memoryChart.update();
        });

        const availableMetrics = await fetchAvailableMetricNames();
        if (availableMetrics.has("http.server.requests")) {
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
        }
        if (availableMetrics.has("http.server.requests.active")) {
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
        await configureLiveMemory();
        await configureHttpRequestResponses();
        configureAuditEventsChart();
    }

    const systemTable = $("#systemTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemTable tr").addClass("mdc-data-table__row");
            $("#systemTable td").addClass("mdc-data-table__cell");
        }
    });

    const liveMemoryPoolsTable = $("#systemLiveMemoryPoolsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemLiveMemoryPoolsTable tr").addClass("mdc-data-table__row");
            $("#systemLiveMemoryPoolsTable td").addClass("mdc-data-table__cell");
        }
    });

    const httpRequestMappingsTable = $("#httpRequestMappingsTable").DataTable({
        deferRender: true,
        pageLength: 25,
        autoWidth: false,
        order: [[1, "asc"]],
        columns: [
            {
                data: "methods",
                width: "9rem",
                render: (data, type) => type === "display" ? renderHttpMappingMethods(data) : toArray(data).join(" ")
            },
            {
                data: "patterns",
                width: "24rem",
                render: (data, type) => type === "display"
                    ? renderHttpMappingValues(data, "http-request-mapping-pattern")
                    : toArray(data).join(" ")
            },
            {
                data: "handler",
                width: "34rem",
                render: (data, type) => type === "display"
                    ? `<code class="http-request-mapping-handler" title="${escapePalantirMarkup(data)}">${escapePalantirMarkup(data)}</code>`
                    : data
            },
            {
                data: "consumes",
                width: "12rem",
                render: (data, type) => type === "display" ? renderHttpMappingValues(data) : toArray(data).join(" ")
            },
            {
                data: "produces",
                width: "12rem",
                render: (data, type) => type === "display" ? renderHttpMappingValues(data) : toArray(data).join(" ")
            }
        ],
        initComplete: () => applyMdcDataTableControls("#httpRequestMappingsTable"),
        drawCallback: settings => {
            $("#httpRequestMappingsTable tr").addClass("mdc-data-table__row");
            $("#httpRequestMappingsTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#httpRequestMappingsTable");
        }
    });

    const heapDumpClassesTable = $("#heapDumpClassesTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        order: [[0, "asc"]],
        columns: [
            {data: "rank", width: "3rem", className: "text-end text-muted"},
            {
                data: "className",
                render: (data, type) => type === "display"
                    ? `${palantirCode(data)}`
                    : data
            },
            {
                data: "instances",
                className: "text-end",
                render: (data, type) => type === "display"
                    ? formatNumber(data)
                    : data
            },
            {
                data: "retained",
                width: "22rem",
                render: (data, type, row) => type === "display"
                    ? renderHeapDumpRetainedCell(row)
                    : data
            },
            {
                data: "averageShallowSize",
                className: "text-end",
                render: (data, type) => type === "display"
                    ? formatMemoryBytes(data)
                    : data
            },
            {
                data: "averageRetainedSize",
                className: "text-end",
                render: (data, type) => type === "display"
                    ? formatMemoryBytes(data)
                    : data
            },
            {
                data: "retainedToShallowRatio",
                className: "text-end",
                render: (data, type) => type === "display"
                    ? formatHeapDumpRatio(data)
                    : data
            }
        ],
        drawCallback: settings => {
            $("#heapDumpClassesTable tr").addClass("mdc-data-table__row");
            $("#heapDumpClassesTable td").addClass("mdc-data-table__cell");
        }
    });

    const httpExchangesTable = $("#httpExchangesTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        order: [[0, "desc"]],
        columns: [
            {
                data: "timestampMs",
                className: "text-start",
                width: "10rem",
                render: (data, type, row) => type === "display" ? escapePalantirMarkup(row.time) : data
            },
            {
                data: "method",
                width: "6.5rem",
                render: (data, type) => type === "display"
                    ? `<span class="http-exchange-method method-${escapePalantirMarkup(String(data).toLowerCase())}">${escapePalantirMarkup(data)}</span>`
                    : data
            },
            {
                data: "path",
                width: "65rem",
                render: (data, type) => type === "display"
                    ? `<code class="http-exchange-path">${escapePalantirMarkup(data)}</code>`
                    : data
            },
            {
                data: "status",
                width: "6rem",
                render: (data, type) => {
                    if (type !== "display") {
                        return data;
                    }
                    const statusClass = data >= 500 ? "server-error"
                        : data >= 400 ? "client-error"
                            : data >= 300 ? "redirect"
                                : data >= 200 ? "success"
                                    : "informational";
                    return `<span class="http-exchange-status status-${statusClass}">${escapePalantirMarkup(data)}</span>`;
                }
            },
            {
                data: "duration",
                width: "7rem",
                render: (data, type, row) => type === "display" ? escapePalantirMarkup(row.durationLabel) : data
            }
        ],
        drawCallback: settings => {
            $("#httpExchangesTable tr").addClass("mdc-data-table__row");
            $("#httpExchangesTable td").addClass("mdc-data-table__cell");
        }
    });

    initializeDataTableContextMenu({
        table: httpExchangesTable,
        selector: "#httpExchangesTable tbody tr",
        items: {
            details: {name: "Toggle Details", icon: contextMenuIcon("mdi-table-eye")}
        },
        callback: (key, context) => {
            if (key === "details") {
                if (context.row.child.isShown()) {
                    context.row.child.hide();
                    context.$row.removeClass("shown");
                } else {
                    context.row.child(renderHttpExchangeDetails(context.row.data())).show();
                    context.$row.addClass("shown");
                    context.$row.next("tr").addClass("http-exchange-details-row");
                }
            }
        }
    });

    const httpTracesTable = $("#httpTracesTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        order: [[0, "desc"]],
        columns: [
            {
                data: "startedAtMs",
                width: "13%",
                className: "dt-left",
                render: (data, type, row) => type === "display" ? escapePalantirMarkup(row.startedAtLabel) : data
            },
            {
                data: "traceId",
                width: "19%",
                render: (data, type) => type === "display"
                    ? `<code class="http-trace-id">${escapePalantirMarkup(data)}</code>`
                    : data
            },
            {
                data: "rootSpan",
                width: "18%",
                render: (data, type) => type === "display"
                    ? renderHttpTraceRootSpan(data)
                    : data
            },
            {
                data: "method",
                width: "7%",
                render: (data, type) => type === "display"
                    ? renderHttpTraceMethod(data)
                    : data
            },
            {
                data: "url",
                width: "21%",
                render: (data, type) => type === "display"
                    ? `<code class="http-trace-url">${escapePalantirMarkup(data || "N/A")}</code>`
                    : data
            },
            {
                data: "spanCount",
                width: "5%",
                className: "text-end"
            },
            {
                data: "durationNanos",
                width: "8%",
                className: "text-end",
                render: (data, type) => type === "display" ? escapePalantirMarkup(formatNanosDuration(data)) : data
            },
            {
                data: "status",
                width: "9%",
                render: (data, type, row) => type === "display" ? renderHttpTraceStatus(data, row.error) : data
            }
        ],
        createdRow: (row, data) => {
            if (data.error) {
                $(row).addClass("http-trace-row-error");
            }
        },
        initComplete: () => applyMdcDataTableControls("#httpTracesTable"),
        drawCallback: settings => {
            $("#httpTracesTable tr").addClass("mdc-data-table__row");
            $("#httpTracesTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#httpTracesTable");
        }
    });

    initializeDataTableContextMenu({
        table: httpTracesTable,
        selector: "#httpTracesTable tbody tr",
        items: {
            details: {name: "Open Trace Details", icon: contextMenuIcon("mdi-open-in-new")}
        },
        callback: (key, context) => {
            if (key === "details") {
                openHttpTraceDialog(context.rowData.traceId);
            }
        }
    });

    if (!CasActuatorEndpoints.httpTraces()) {
        setHttpTracesUnavailable();
    }

    const casDependenciesTable = $("#casDependenciesTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        order: [[1, "asc"]],
        columns: [
            {
                data: "groupId",
                width: "28%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            },
            {
                data: "artifactId",
                width: "26%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            },
            {
                data: "version",
                width: "12%",
                render: (data, type) => type === "display" ? renderDependencyVersion(data) : data
            },
            {
                data: "source",
                width: "34%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            }
        ],
        initComplete: () => applyMdcDataTableControls("#casDependenciesTable"),
        drawCallback: settings => {
            $("#casDependenciesTable tr").addClass("mdc-data-table__row");
            $("#casDependenciesTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#casDependenciesTable");
        }
    });

    const casVulnerabilitiesTable = $("#casVulnerabilitiesTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        order: [[6, "desc"]],
        columns: [
            {
                data: "id",
                width: "14%",
                render: (data, type) => type === "display" ? renderVulnerabilityId(data) : data
            },
            {
                data: "severity",
                width: "12%",
                className: "text-start cas-vulnerability-severity-column",
                render: (data, type) => {
                    if (type === "display") {
                        return renderVulnerabilitySeverity(data);
                    }
                    return Number.isFinite(data?.score) ? data.score : data?.label ?? "";
                }
            },
            {
                data: "dependency.groupId",
                width: "15%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            },
            {
                data: "dependency.artifactId",
                width: "15%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            },
            {
                data: "dependency.version",
                width: "10%",
                render: (data, type) => type === "display" ? renderDependencyVersion(data) : data
            },
            {
                data: "dependency.source",
                width: "22%",
                render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
            },
            {
                data: "modified",
                width: "12%",
                render: (data, type) => type === "display" ? escapePalantirMarkup(formatDependencyTimestamp(data)) : data
            }
        ],
        initComplete: () => applyMdcDataTableControls("#casVulnerabilitiesTable"),
        drawCallback: settings => {
            $("#casVulnerabilitiesTable tr").addClass("mdc-data-table__row");
            $("#casVulnerabilitiesTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#casVulnerabilitiesTable");
        }
    });

    const tabs = dashboardTabBar();

    async function configureStartupTimeline() {
        await PalantirStartupTimeline.show();
    }

    async function configureSystemInfo() {
        await fetchSystemData(response => {
            const shown = {...response};
            if (CasActuatorEndpoints.casModules()) {
                delete shown.casModules;
            }
            const flattened = flattenJSON(shown);
            systemTable.clear();

            for (const [key, value] of Object.entries(flattened)) {
                systemTable.row.add({
                    0: `${palantirCode(key)}`,
                    1: `${palantirCode(value)}`
                });
            }
            systemTable.draw();

            highlightElements();
            $("#casServerPrefix").text(PalantirDashboardConfiguration.casServerPrefix());
            $("#casServerHost").text(response.server["host"]);
        });
    }

    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.SYSTEM.index) {
            configureSystemInfo();
        }
    });

    let httpRequestMappingsLoaded = false;

    /**
     * Load the web endpoint mappings the first time that panel is shown.
     *
     * The mappings actuator returns every handler registered in the application, which is one of
     * the largest payloads the dashboard can ask for. It used to be fetched on every System refresh
     * whether or not anyone was looking at it.
     *
     * @returns {Promise<void>} resolved once the mappings have been rendered
     */
    async function loadHttpRequestMappings() {
        if (httpRequestMappingsLoaded) {
            return;
        }
        httpRequestMappingsLoaded = true;
        await refreshHttpRequestMappings();
    }

    $("#system-tabs").on("tabsactivate", function (event, ui) {
        if (ui.newPanel.attr("id") === "casstartup-tab") {
            configureStartupTimeline();
        }
        if (ui.newPanel.attr("id") === "httptraces-tab") {
            configureHttpTraces();
        }
        if (ui.newPanel.attr("id") === "httprequestmappings-tab") {
            loadHttpRequestMappings();
        }
        if (ui.newPanel.attr("id") === "systemstatus-tab") {
            configureLiveMemory();
        }
        if (ui.newPanel.attr("id") === "systeminfo-tab") {
            refreshActiveSystemInfoTab();
        }
    });

    $("#system-info-tabs").on("tabsactivate", function (event, ui) {
        if (ui.newPanel.attr("id") === "casmodules-tab") {
            PalantirModules.load();
        }
        if (ui.newPanel.attr("id") === "casdependencies-tab") {
            refreshDependencies();
        }
        if (ui.newPanel.attr("id") === "casvulnerabilities-tab") {
            refreshVulnerabilities();
        }
    });

    let rememberedSystemTab = null;
    try {
        const remembered = JSON.parse(localStorage.getItem("ActiveTabs") ?? "{}")?.["system-tabs"];
        const index = remembered === undefined ? 0 : palantirInnerTabIndex($("#system-tabs"), remembered);
        rememberedSystemTab = index >= 0 ? $("#system-tabs > ul > li").eq(index).attr("aria-controls") ?? null : null;
    } catch (error) {
        console.debug("The System page shown last is not known", error);
    }
    if (rememberedSystemTab === "casstartup-tab") {
        await configureStartupTimeline();
    }
    if (rememberedSystemTab === "httptraces-tab") {
        await configureHttpTraces();
    }
    if (rememberedSystemTab === "httprequestmappings-tab") {
        await loadHttpRequestMappings();
    }
    if (rememberedSystemTab === "systeminfo-tab") {
        refreshActiveSystemInfoTab();
    }

    PalantirPolling.every("system", () => {
        const work = [];
        if (isPalantirPollingContextActive(Tabs.SYSTEM, "#systemstatus-tab, #httprequests-tab")) {
            work.push(configureSystemData());
        }
        if (isPalantirPollingContextActive(Tabs.SYSTEM, "#systemstatus-tab")) {
            work.push(configureHealthChart());
            if (Date.now() - statisticsCounted >= STATISTICS_REFRESH) {
                work.push(configureStatistics());
            }
            if (Date.now() - signInsCounted >= STATISTICS_REFRESH) {
                work.push(configureSignIns());
            }
            if (Date.now() - slowestMeasured >= STATISTICS_REFRESH) {
                work.push(configureSlowestEndpoints());
            }
            if (Date.now() - providersCounted >= STATISTICS_REFRESH) {
                work.push(configureMultifactorProviders());
            }
        }
        return work.length > 0 ? Promise.all(work) : null;
    }, {tab: Tabs.SYSTEM});

    if ($("#systeminfo-tab").is(":visible")) {
        await refreshActiveSystemInfoTab();
    }

    await configureHeapDumpAnalysis();
    await Promise.all([
        configureSystemData(),
        configureStatistics(),
        configureSignIns(),
        configureSlowestEndpoints(),
        configureMultifactorProviders(),
        configureHealthChart(),
        configureSystemInfo(),
        configureSystemMetrics()
    ]);

    $("button[name=shutdownServerButton]").off().on("click", function () {
        PalantirDialogs.confirm({
            title: "Are you sure you want to shut the server down?",
            text: "Once confirmed, the server will begin shutdown procedures. Note that this operation does not support clustered deployments."
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
        PalantirDialogs.confirm({
            title: "Are you sure you want to restart the server?",
            text: "Once confirmed, the server will begin restarting. Note that this operation does not support clustered deployments."
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: CasActuatorEndpoints.restart(),
                        type: "POST",
                        headers: {"Content-Type": "application/json"},
                        success: response => {
                            PalantirDialogs.wait(`Restarting CAS`, "Please wait while the CAS server is restarting...");
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

