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

    function escapeHtml(str) {
        return String(str).replace(/[&<>"']/g, s => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[s]));
    }

    function normalizeDependency(dependency) {
        const entry = dependency ?? {};
        return {
            groupId: entry.groupId ?? "",
            artifactId: entry.artifactId ?? "",
            version: entry.version ?? "",
            source: entry.source ?? ""
        };
    }

    function renderDependencyVersion(version) {
        return `<span class="cas-dependency-version">${escapeHtml(version)}</span>`;
    }

    function renderVulnerabilityId(id) {
        return `<span class="cas-vulnerability-id">${escapeHtml(id)}</span>`;
    }

    function roundUpCvssScore(value) {
        return Math.ceil(value * 10) / 10;
    }

    function cvssSeverityLabel(score) {
        if (!Number.isFinite(score)) {
            return "Unknown";
        }
        if (score === 0) {
            return "None";
        }
        if (score < 4) {
            return "Low";
        }
        if (score < 7) {
            return "Medium";
        }
        if (score < 9) {
            return "High";
        }
        return "Critical";
    }

    function parseCvssVector(vector) {
        return Object.fromEntries(String(vector ?? "")
            .split("/")
            .filter(part => part.includes(":"))
            .map(part => {
                const [key, value] = part.split(":");
                return [key, value];
            }));
    }

    function calculateCvssV3Score(vector) {
        const metrics = parseCvssVector(vector);

        const metricValues = {
            AV: {N: 0.85, A: 0.62, L: 0.55, P: 0.2},
            AC: {L: 0.77, H: 0.44},
            UI: {N: 0.85, R: 0.62},
            S: {U: "U", C: "C"},
            C: {H: 0.56, L: 0.22, N: 0},
            I: {H: 0.56, L: 0.22, N: 0},
            A: {H: 0.56, L: 0.22, N: 0}
        };
        const scope = metricValues.S[metrics.S];
        const privilegeRequired = {
            U: {N: 0.85, L: 0.62, H: 0.27},
            C: {N: 0.85, L: 0.68, H: 0.5}
        }[scope]?.[metrics.PR];
        const values = {
            attackVector: metricValues.AV[metrics.AV],
            attackComplexity: metricValues.AC[metrics.AC],
            privilegeRequired,
            userInteraction: metricValues.UI[metrics.UI],
            confidentiality: metricValues.C[metrics.C],
            integrity: metricValues.I[metrics.I],
            availability: metricValues.A[metrics.A]
        };
        if (!scope || Object.values(values).some(value => !Number.isFinite(value))) {
            return null;
        }

        const impactSubScore = 1 - ((1 - values.confidentiality) * (1 - values.integrity) * (1 - values.availability));
        const impact = scope === "U"
            ? 6.42 * impactSubScore
            : 7.52 * (impactSubScore - 0.029) - 3.25 * Math.pow(impactSubScore - 0.02, 15);
        const exploitability = 8.22 * values.attackVector * values.attackComplexity
            * values.privilegeRequired * values.userInteraction;
        if (impact <= 0) {
            return 0;
        }
        const baseScore = scope === "U"
            ? Math.min(impact + exploitability, 10)
            : Math.min(1.08 * (impact + exploitability), 10);
        return roundUpCvssScore(baseScore);
    }

    function calculateCvssV4Score(vector) {
        const metrics = parseCvssVector(vector);
        const metricValues = {
            AV: {N: 0.85, A: 0.62, L: 0.55, P: 0.2},
            AC: {L: 0.77, H: 0.44},
            AT: {N: 1, P: 0.7},
            PR: {N: 0.85, L: 0.62, H: 0.27},
            UI: {N: 0.85, P: 0.62, A: 0.45},
            VC: {H: 0.56, L: 0.22, N: 0},
            VI: {H: 0.56, L: 0.22, N: 0},
            VA: {H: 0.56, L: 0.22, N: 0},
            SC: {H: 0.56, L: 0.22, N: 0},
            SI: {S: 0.56, H: 0.56, L: 0.22, N: 0},
            SA: {S: 0.56, H: 0.56, L: 0.22, N: 0}
        };
        const exploitabilityMetrics = {
            attackVector: metricValues.AV[metrics.AV],
            attackComplexity: metricValues.AC[metrics.AC],
            attackRequirements: metricValues.AT[metrics.AT],
            privilegeRequired: metricValues.PR[metrics.PR],
            userInteraction: metricValues.UI[metrics.UI]
        };
        const impactMetrics = {
            vulnerableConfidentiality: metricValues.VC[metrics.VC],
            vulnerableIntegrity: metricValues.VI[metrics.VI],
            vulnerableAvailability: metricValues.VA[metrics.VA],
            subsequentConfidentiality: metricValues.SC[metrics.SC],
            subsequentIntegrity: metricValues.SI[metrics.SI],
            subsequentAvailability: metricValues.SA[metrics.SA]
        };
        if (Object.values(exploitabilityMetrics).some(value => !Number.isFinite(value))
            || Object.values(impactMetrics).some(value => !Number.isFinite(value))) {
            return null;
        }

        const vulnerableImpact = 1 - ((1 - impactMetrics.vulnerableConfidentiality)
            * (1 - impactMetrics.vulnerableIntegrity)
            * (1 - impactMetrics.vulnerableAvailability));
        const subsequentImpact = 1 - ((1 - impactMetrics.subsequentConfidentiality)
            * (1 - impactMetrics.subsequentIntegrity)
            * (1 - impactMetrics.subsequentAvailability));
        const impact = Math.max(vulnerableImpact, subsequentImpact);
        if (impact <= 0) {
            return 0;
        }
        const exploitability = 8.22 * exploitabilityMetrics.attackVector
            * exploitabilityMetrics.attackComplexity
            * exploitabilityMetrics.attackRequirements
            * exploitabilityMetrics.privilegeRequired
            * exploitabilityMetrics.userInteraction;
        return roundUpCvssScore(Math.min((6.42 * impact) + exploitability, 10));
    }

    function calculateCvssScore(type, vector) {
        if (String(type ?? "").toUpperCase() === "CVSS_V4" || String(vector ?? "").startsWith("CVSS:4.0/")) {
            return calculateCvssV4Score(vector);
        }
        return calculateCvssV3Score(vector);
    }

    function normalizeVulnerabilitySeverity(severity) {
        const entries = Array.isArray(severity) ? severity : [];
        const entry = entries[0] ?? {};
        const rawScore = entry.score ?? "";
        const numericScore = Number(rawScore);
        const score = Number.isFinite(numericScore)
            ? numericScore
            : calculateCvssScore(entry.type, rawScore);
        const label = cvssSeverityLabel(score);
        return {
            type: entry.type ?? "",
            score,
            vector: rawScore,
            label,
            level: label.toLowerCase()
        };
    }

    function renderVulnerabilitySeverity(severity) {
        const label = severity?.label ?? "Unknown";
        const score = Number.isFinite(severity?.score) ? severity.score.toFixed(1) : "";
        const title = [severity?.type, severity?.vector].filter(Boolean).join(" ");
        return `
            <span class="cas-vulnerability-severity severity-${escapeHtml(severity?.level ?? "unknown")}"
                  title="${escapeHtml(title)}">
                ${escapeHtml(label)}${score ? ` ${escapeHtml(score)}` : ""}
            </span>`;
    }

    function renderVulnerabilitySeverityBreakdown(vulnerabilities) {
        const levels = ["critical", "high", "medium", "low", "unknown"];
        const labels = {
            critical: "Critical",
            high: "High",
            medium: "Medium",
            low: "Low",
            unknown: "Unknown"
        };
        const counts = Object.fromEntries(levels.map(level => [level, 0]));
        vulnerabilities.forEach(vulnerability => {
            const level = vulnerability.severity?.level;
            counts[levels.includes(level) && level !== "none" ? level : "unknown"]++;
        });
        const maxCount = Math.max(...levels.map(level => counts[level]), 1);
        const rows = levels.map(level => {
            const count = counts[level];
            const width = count > 0 ? Math.max(2, (count / maxCount) * 100) : 0;
            return `
                <div class="cas-vulnerability-breakdown-row">
                    <span class="cas-vulnerability-breakdown-label severity-${level}">${labels[level]}</span>
                    <span class="cas-vulnerability-breakdown-track">
                        <span class="cas-vulnerability-breakdown-bar severity-${level}" style="width:${width}%"></span>
                    </span>
                    <span class="cas-vulnerability-breakdown-count">${count}</span>
                </div>`;
        }).join("");
        $("#casVulnerabilitiesSeverityBreakdown").html(`
            <div class="cas-vulnerability-breakdown-title">Severity breakdown</div>
            <div class="cas-vulnerability-breakdown-body">${rows}</div>`);
        showElements($("#casVulnerabilitiesSeverityBreakdown"));
    }

    function formatDependencyTimestamp(value) {
        if (!value) {
            return "";
        }
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
    }

    function dependenciesVulnerabilitiesEndpoint() {
        const endpoint = CasActuatorEndpoints.dependencies();
        return endpoint ? `${endpoint.replace(/\/$/, "")}/vulnerabilities` : undefined;
    }

    function setSystemJqueryTabAvailable(anchorSelector, panelSelector, available) {
        const tabItem = $(anchorSelector).parent();
        const panel = $(panelSelector);
        const tabs = tabItem.closest(".jqueryui-tabs");
        if (available) {
            showElements(tabItem);
            showElements(panel);
            showElements(tabs);
        } else {
            const tabIndex = tabItem.index();
            hideElements(tabItem);
            hideElements(panel);
            if (tabs.data("ui-tabs")) {
                if (tabs.tabs("option", "active") === tabIndex) {
                    tabs.tabs("option", "active", 0);
                }
                tabs.tabs("refresh");
            }
            if (tabs.find("> ul > li:not(.hide):not(.d-none)").length === 0) {
                hideElements(tabs);
            }
        }
    }

    function hideDependenciesTab() {
        setSystemJqueryTabAvailable("#casdependenciestab", "#casdependencies-tab", false);
    }

    function hideVulnerabilitiesTab() {
        setSystemJqueryTabAvailable("#casvulnerabilitiestab", "#casvulnerabilities-tab", false);
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
        Swal.fire({
            title: "Checking vulnerabilities",
            text: "Scanning dependencies and collecting vulnerability data...",
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });
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

                $("#casVulnerabilitiesSummary").html(`
                    <span class="cas-vulnerability-summary-item">
                        <i class="mdi mdi-package-variant-closed" aria-hidden="true"></i>
                        <strong>${escapeHtml(response.dependencyCount ?? 0)}</strong> dependencies
                    </span>`);
                const errors = Array.isArray(response.errors) ? response.errors : [];
                $("#casVulnerabilitiesStatus").text(errors.length > 0 ? errors.join("; ") : "");
                vulnerabilitiesLoaded = true;
                Swal.close();
                resolve();
            }).fail((xhr, status, error) => {
                console.error("Error fetching dependency vulnerability data:", error);
                hideVulnerabilitiesTab();
                Swal.close();
                resolve();
            });
        });
    }

    function formatMemoryBytes(value) {
        if (!Number.isFinite(value)) {
            return "N/A";
        }
        if (value < 0) {
            return "Unlimited";
        }
        const units = ["B", "KB", "MB", "GB", "TB"];
        let unitIndex = 0;
        let result = value;
        while (result >= 1024 && unitIndex < units.length - 1) {
            result /= 1024;
            unitIndex++;
        }
        const precision = unitIndex === 0 ? 0 : 1;
        return `${result.toFixed(precision)} ${units[unitIndex]}`;
    }

    function formatMemoryPercent(value) {
        if (!Number.isFinite(value)) {
            return "N/A";
        }
        return `${Math.round(value)}%`;
    }

    function memoryUsagePercent(used, max) {
        if (!Number.isFinite(used) || !Number.isFinite(max) || max <= 0) {
            return null;
        }
        return Math.min(100, Math.max(0, (used / max) * 100));
    }

    function memoryMetricUrl(metricName, tags = []) {
        const params = new URLSearchParams();
        tags.forEach(tag => params.append("tag", tag));
        const query = params.toString();
        return `${CasActuatorEndpoints.metrics()}/${metricName}${query ? `?${query}` : ""}`;
    }

    async function getMemoryMetricValue(metricName, tags = []) {
        return new Promise(resolve => {
            $.get(memoryMetricUrl(metricName, tags), response => {
                const measurement = response.measurements?.find(entry => entry.statistic === "VALUE")
                    ?? response.measurements?.[0];
                const value = Number(measurement?.value);
                resolve(Number.isFinite(value) ? value : null);
            }).fail((xhr, status, error) => {
                console.debug(`Unable to fetch ${metricName}`, error);
                resolve(null);
            });
        });
    }

    function getHeaderValue(headers, name) {
        if (!headers) {
            return undefined;
        }
        const headerName = name.toLowerCase();
        const matchingKey = Object.keys(headers).find(key => key.toLowerCase() === headerName);
        const value = matchingKey ? headers[matchingKey] : undefined;
        if (Array.isArray(value)) {
            return value.length > 0 ? value[0] : undefined;
        }
        return value;
    }

    function formatHttpExchangeDuration(value) {
        const millis = parseHttpExchangeDuration(value);
        if (!Number.isFinite(millis)) {
            return "N/A";
        }
        if (millis < 1000) {
            return `${Math.round(millis)} ms`;
        }
        return `${(millis / 1000).toFixed(2)} s`;
    }

    function parseHttpExchangeDuration(value) {
        if (typeof value === "number") {
            return value;
        }
        if (typeof value !== "string" || value.length === 0) {
            return null;
        }
        const match = value.match(/^PT(?:(\d+(?:\.\d+)?)H)?(?:(\d+(?:\.\d+)?)M)?(?:(\d+(?:\.\d+)?)S)?$/);
        if (!match) {
            return null;
        }
        const hours = Number(match[1] ?? 0);
        const minutes = Number(match[2] ?? 0);
        const seconds = Number(match[3] ?? 0);
        return ((hours * 3600) + (minutes * 60) + seconds) * 1000;
    }

    function getHttpExchangePath(uri) {
        if (!uri) {
            return "";
        }
        try {
            const url = new URL(uri, window.location.origin);
            return `${url.pathname}${url.search}`;
        } catch (e) {
            return String(uri)
                .replace(PalantirDashboardConfiguration.casServerPrefix(), "");
        }
    }

    function getHttpExchangeActuatorBasePath() {
        const endpoint = CasActuatorEndpoints.httpExchanges();
        if (!endpoint) {
            return "/actuator";
        }
        try {
            const url = new URL(endpoint, window.location.origin);
            return url.pathname.replace(/\/httpexchanges\/?$/i, "") || "/actuator";
        } catch (e) {
            return String(endpoint)
                .replace(PalantirDashboardConfiguration.casServerPrefix(), "")
                .replace(/\/httpexchanges\/?$/i, "") || "/actuator";
        }
    }

    function getHttpExchangePathVariants(path) {
        const paths = new Set([path]);
        try {
            const casServerPath = new URL(PalantirDashboardConfiguration.casServerPrefix()).pathname;
            if (casServerPath && casServerPath !== "/" && path.startsWith(`${casServerPath}/`)) {
                paths.add(path.substring(casServerPath.length));
            }
        } catch (e) {
            /* ignore */
        }
        return paths;
    }

    function httpExchangeIsActuatorPath(path) {
        const actuatorBasePath = getHttpExchangeActuatorBasePath();
        return [...getHttpExchangePathVariants(path)].some(candidate =>
            candidate === actuatorBasePath || candidate.startsWith(`${actuatorBasePath}/`));
    }

    function normalizeRemoteAddress(remoteAddress) {
        if (!remoteAddress) {
            return "";
        }
        const value = String(remoteAddress)
            .trim()
            .replace(/^::ffff:/i, "")
            .toLowerCase();
        if (value.startsWith("[")) {
            const endIndex = value.indexOf("]");
            return endIndex > 0 ? value.substring(1, endIndex) : value;
        }
        if (/^\d{1,3}(?:\.\d{1,3}){3}:\d+$/.test(value)) {
            return value.replace(/:\d+$/, "");
        }
        if (/^[a-z0-9.-]+:\d+$/.test(value)) {
            return value.replace(/:\d+$/, "");
        }
        return value;
    }

    function getApplicationHostNames() {
        const hostNames = new Set(["localhost", "127.0.0.1", "::1", "0:0:0:0:0:0:0:1"]);
        const casServerHost = $("#casServerHost").text();
        if (casServerHost) {
            hostNames.add(casServerHost.toLowerCase());
        }
        try {
            hostNames.add(new URL(PalantirDashboardConfiguration.casServerPrefix()).hostname.toLowerCase());
        } catch (e) {
            /* ignore */
        }
        if (window.location.hostname) {
            hostNames.add(window.location.hostname.toLowerCase());
        }
        return hostNames;
    }

    function httpExchangeOriginatedFromApplication(entry) {
        const remoteAddress = normalizeRemoteAddress(entry.remoteAddress);
        if (!remoteAddress) {
            return false;
        }
        if (remoteAddress === "127.0.0.1" || remoteAddress === "::1" || remoteAddress === "0:0:0:0:0:0:0:1") {
            return true;
        }
        return getApplicationHostNames().has(remoteAddress);
    }

    function httpExchangeOriginatedFromDashboard(entry) {
        const referrer = getHeaderValue(entry.requestHeaders, "referer")
            ?? getHeaderValue(entry.requestHeaders, "referrer");
        if (!referrer) {
            return false;
        }
        try {
            const referrerUrl = new URL(referrer, window.location.origin);
            return getApplicationHostNames().has(referrerUrl.hostname.toLowerCase())
                && referrerUrl.pathname.endsWith("/dashboard");
        } catch (e) {
            return String(referrer).includes("/dashboard");
        }
    }

    function httpExchangeShouldBeDisplayed(entry) {
        return !httpExchangeIsActuatorPath(entry.path)
            || (!httpExchangeOriginatedFromApplication(entry) && !httpExchangeOriginatedFromDashboard(entry));
    }

    function getHttpExchangeTraceId(exchange) {
        const requestHeaders = exchange.request?.headers ?? {};
        const responseHeaders = exchange.response?.headers ?? {};
        const traceParent = getHeaderValue(requestHeaders, "traceparent")
            ?? getHeaderValue(responseHeaders, "traceparent");
        if (traceParent) {
            const match = String(traceParent).match(/^[\da-f]{2}-([\da-f]{32})-/i);
            if (match) {
                return match[1];
            }
        }
        return exchange.traceId
            ?? getHeaderValue(requestHeaders, "x-b3-traceid")
            ?? getHeaderValue(responseHeaders, "x-b3-traceid")
            ?? getHeaderValue(requestHeaders, "x-trace-id")
            ?? getHeaderValue(responseHeaders, "x-trace-id")
            ?? "";
    }

    function normalizeHttpExchange(exchange, index) {
        const timestamp = new Date(exchange.timestamp);
        const requestHeaders = exchange.request?.headers ?? {};
        const responseHeaders = exchange.response?.headers ?? {};
        const status = Number(exchange.response?.status ?? 0);
        const path = getHttpExchangePath(exchange.request?.uri);
        const traceId = getHttpExchangeTraceId(exchange);
        const duration = parseHttpExchangeDuration(exchange.timeTaken);

        return {
            id: `http-exchange-${index}`,
            raw: exchange,
            timestamp: Number.isNaN(timestamp.getTime()) ? null : timestamp,
            timestampMs: Number.isNaN(timestamp.getTime()) ? 0 : timestamp.getTime(),
            time: Number.isNaN(timestamp.getTime()) ? "N/A" : timestamp.toLocaleString(),
            method: exchange.request?.method ?? "UNKNOWN",
            uri: exchange.request?.uri ?? "",
            path,
            status,
            duration,
            durationLabel: formatHttpExchangeDuration(exchange.timeTaken),
            traceId,
            remoteAddress: exchange.request?.remoteAddress ?? "",
            principal: exchange.principal?.name ?? "",
            sessionId: exchange.session?.id ?? "",
            requestHeaders,
            responseHeaders
        };
    }

    function httpExchangeUrlIsAcceptable(path) {
        return !path.startsWith("/actuator")
            && !path.startsWith("/webjars")
            && !path.endsWith(".js")
            && !path.endsWith(".ico")
            && !path.endsWith(".png")
            && !path.endsWith(".jpg")
            && !path.endsWith(".jpeg")
            && !path.endsWith(".gif")
            && !path.endsWith(".svg")
            && !path.endsWith(".css");
    }

    function updateHttpExchangeCharts(entries) {
        let httpSuccesses = [];
        let httpFailures = [];
        let httpSuccessesPerUrl = [];
        let httpFailuresPerUrl = [];

        let totalHttpSuccessPerUrl = 0;
        let totalHttpSuccess = 0;
        let totalHttpFailurePerUrl = 0;
        let totalHttpFailure = 0;

        for (const entry of entries) {
            const url = entry.path.replaceAll(/\?.+/gi, "");
            if (httpExchangeUrlIsAcceptable(url)) {
                if (entry.status >= 100 && entry.status <= 400) {
                    totalHttpSuccess++;
                    httpSuccesses.push({x: entry.timestamp ? formatDateYearMonthDayHourMinute(entry.timestamp) : entry.time, y: totalHttpSuccess});
                    totalHttpSuccessPerUrl++;
                    httpSuccessesPerUrl.push({x: url, y: totalHttpSuccessPerUrl});
                } else {
                    totalHttpFailure++;
                    httpFailures.push({x: entry.timestamp ? formatDateYearMonthDayHourMinute(entry.timestamp) : entry.time, y: totalHttpFailure});
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
    }

    function hideHttpExchangesTab() {
        hideElements($("#httpExchangesContent"));
        hideElements($("#httprequeststab").parent());
    }

    function setHttpExchangesAvailable() {
        showElements($("#httpExchangesContent"));
        showElements($("#httprequeststab").parent());
        $("#httpExchangesMethodFilter, #httpExchangesStatusFilter").prop("disabled", false);
        if ($("#httpExchangesMethodFilter").data("ui-selectmenu")) {
            $("#httpExchangesMethodFilter, #httpExchangesStatusFilter").selectmenu("enable");
        }
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

        $("#httpExchangesTable tbody")
            .off("click", "button.http-exchange-details-button")
            .on("click", "button.http-exchange-details-button", function () {
                const tableRow = $(this).closest("tr");
                const row = httpExchangesTable.row(tableRow);
                if (row.child.isShown()) {
                    row.child.hide();
                    tableRow.removeClass("shown");
                    $(this).removeClass("http-exchange-details-button-active");
                    $(this).attr("aria-label", "View details");
                    $(this).find(".mdi").removeClass("mdi-chevron-up").addClass("mdi-table-eye");
                } else {
                    row.child(renderHttpExchangeDetails(row.data())).show();
                    tableRow.addClass("shown");
                    tableRow.next("tr").addClass("http-exchange-details-row");
                    $(this).addClass("http-exchange-details-button-active");
                    $(this).attr("aria-label", "Hide details");
                    $(this).find(".mdi").removeClass("mdi-table-eye").addClass("mdi-chevron-up");
                }
            });
    }

    function updateHttpExchangeMethodOptions(entries) {
        const methods = [...new Set(entries.map(entry => entry.method).filter(method => method))].sort();
        const currentValue = $("#httpExchangesMethodFilter").val();
        $("#httpExchangesMethodFilter").empty().append($("<option>", {
            value: "",
            text: "All methods"
        }));
        methods.forEach(method => $("#httpExchangesMethodFilter").append($("<option>", {
            value: method,
            text: method
        })));
        if (currentValue && methods.includes(currentValue)) {
            $("#httpExchangesMethodFilter").val(currentValue);
        }
        $("#httpExchangesMethodFilter").selectmenu("refresh");
    }

    function httpExchangeMatchesFilters(entry) {
        const methodFilter = $("#httpExchangesMethodFilter").val();
        const statusFilter = $("#httpExchangesStatusFilter").val();
        return (!methodFilter || entry.method === methodFilter)
            && (!statusFilter || String(entry.status).startsWith(statusFilter));
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

    function renderHeaderEntries(headers) {
        const entries = Object.entries(headers ?? {});
        if (entries.length === 0) {
            return `<div class="http-exchange-empty">No headers recorded.</div>`;
        }
        return entries.map(([name, value]) => {
            const renderedValue = Array.isArray(value) ? value.join(", ") : value;
            return `
                <div class="http-exchange-header-row">
                    <strong>${escapeHtml(name)}</strong>
                    <code>${escapeHtml(renderedValue)}</code>
                </div>`;
        }).join("");
    }

    function applyMdcDataTableControls(selector) {
        const wrapper = $(selector).closest(".dataTables_wrapper, .dt-container");
        wrapper.find(".dataTables_filter input, .dt-search input")
            .addClass("mdc-text-field__input form-control palantir-datatable-search-input")
            .attr("aria-label", "Search table");
        wrapper.find(".dataTables_length select, .dt-length select")
            .addClass("mdc-select__native-control form-select");
        wrapper.find(".paginate_button, .dt-paging-button")
            .addClass("mdc-button mdc-button--outlined");
    }

    function renderHttpExchangeDetails(entry) {
        const metadata = [
            ["Remote address", entry.remoteAddress],
            ["Principal", entry.principal],
            ["Session", entry.sessionId],
            ["Trace id", entry.traceId]
        ].filter(item => item[1]);

        return `
            <div class="http-exchange-details">
                <h4>Metadata</h4>
                <div class="http-exchange-metadata">
                    ${metadata.length === 0 ? `<div class="http-exchange-empty">No metadata recorded.</div>` : metadata.map(([name, value]) => `
                        <strong>${escapeHtml(name)}</strong>
                        <code>${escapeHtml(value)}</code>
                    `).join("")}
                </div>
                <div class="http-exchange-headers">
                    <section>
                        <h4>Request headers</h4>
                        ${renderHeaderEntries(entry.requestHeaders)}
                    </section>
                    <section>
                        <h4>Response headers</h4>
                        ${renderHeaderEntries(entry.responseHeaders)}
                    </section>
                </div>
            </div>`;
    }

    async function getMemoryPoolNames() {
        return new Promise(resolve => {
            $.get(memoryMetricUrl("jvm.memory.used"), response => {
                const idTag = response.availableTags?.find(entry => entry.tag === "id");
                resolve(idTag?.values ?? []);
            }).fail((xhr, status, error) => {
                console.debug("Unable to fetch memory pool names", error);
                resolve([]);
            });
        });
    }

    let heapDumpAnalysisEntries = [];
    let httpExchangeEntries = [];
    let httpExchangesControlsInitialized = false;
    let dependenciesLoaded = false;
    let vulnerabilitiesLoaded = false;

    function toArray(value) {
        if (value === undefined || value === null || value === "" || typeof value === "function") {
            return [];
        }
        return Array.isArray(value) ? value : [value];
    }

    function ownValue(source, name) {
        return source && Object.prototype.hasOwnProperty.call(source, name) ? source[name] : undefined;
    }

    function getConditionValues(condition, ...names) {
        if (!condition) {
            return [];
        }
        for (const name of names) {
            const value = ownValue(condition, name);
            if (value !== undefined && value !== null) {
                return toArray(value);
            }
        }
        return [];
    }

    function parseHttpRequestMappingPredicate(predicate) {
        const text = String(predicate ?? "");
        const knownMethods = ["GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE"];
        const methods = new Set();
        const patterns = new Set();
        const consumes = new Set();
        const produces = new Set();

        knownMethods.forEach(method => {
            if (new RegExp(`(^|[^A-Z])${method}(?=\\s|\\[|,|\\})`).test(text)) {
                methods.add(method);
            }
        });

        const patternMatch = text.match(/\[([^\]]+)]/);
        if (patternMatch) {
            patternMatch[1].split(/\s*\|\|\s*/).forEach(pattern => {
                const value = pattern.trim();
                if (value && value !== "function values() { [native code] }") {
                    patterns.add(value);
                }
            });
        }

        const consumesMatch = text.match(/consumes\s+\[([^\]]+)]/i);
        if (consumesMatch) {
            consumesMatch[1].split(/\s*,\s*/).filter(Boolean).forEach(value => consumes.add(value.trim()));
        }

        const producesMatch = text.match(/produces\s+\[([^\]]+)]/i);
        if (producesMatch) {
            producesMatch[1].split(/\s*,\s*/).filter(Boolean).forEach(value => produces.add(value.trim()));
        }

        return {
            methods: [...methods],
            patterns: [...patterns],
            consumes: [...consumes],
            produces: [...produces]
        };
    }

    function normalizeHttpRequestMapping(contextId, sourceName, mapping, index) {
        const details = mapping.details ?? {};
        const conditions = details.requestMappingConditions ?? mapping.requestMappingConditions ?? {};
        const predicate = mapping.predicate ?? details.predicate ?? "";
        const parsedPredicate = parseHttpRequestMappingPredicate(predicate);
        const requestMethods = getConditionValues(conditions.methods, "methods", "method", "values");
        const conditionPatterns = [
            ...getConditionValues(conditions.patterns, "patterns", "values"),
            ...getConditionValues(conditions.pathPatterns, "patterns", "values")
        ];
        const methods = parsedPredicate.methods
            .concat(toArray(mapping.methods ?? details.methods ?? requestMethods))
            .filter(Boolean);
        const patterns = parsedPredicate.patterns
            .concat(toArray(mapping.patterns ?? details.patterns))
            .concat(conditionPatterns)
            .filter(Boolean);
        const handler = mapping.handler ?? details.handlerMethod?.name ?? details.handlerMethod?.descriptor ?? "";
        return {
            id: `${contextId}-${sourceName}-${index}`,
            source: sourceName,
            methods: methods.length > 0 ? [...new Set(methods)] : ["ANY"],
            patterns: [...new Set(patterns)],
            handler,
            consumes: parsedPredicate.consumes.concat(getConditionValues(conditions.consumes, "mediaTypes", "consumes")),
            produces: parsedPredicate.produces.concat(getConditionValues(conditions.produces, "mediaTypes", "produces"))
        };
    }

    function getHttpRequestMappingEntries(response) {
        const rows = [];
        Object.entries(response?.contexts ?? {}).forEach(([contextId, context]) => {
            Object.entries(context.mappings ?? {}).forEach(([sourceName, source]) => {
                if (Array.isArray(source)) {
                    source.forEach((mapping, index) =>
                        rows.push(normalizeHttpRequestMapping(contextId, sourceName, mapping, index)));
                } else if (source && typeof source === "object") {
                    Object.entries(source).forEach(([nestedSourceName, mappings]) => {
                        if (Array.isArray(mappings)) {
                            mappings.forEach((mapping, index) =>
                                rows.push(normalizeHttpRequestMapping(contextId, nestedSourceName, mapping, index)));
                        }
                    });
                }
            });
        });
        return rows.filter(row => row.patterns.length > 0 && row.handler);
    }

    function renderHttpMappingMethods(methods) {
        return toArray(methods).map(method => {
            const value = String(method);
            return `<span class="http-exchange-method method-${escapeHtml(value.toLowerCase())}">${escapeHtml(value)}</span>`;
        }).join(" ");
    }

    function renderHttpMappingValues(values, cssClass = "") {
        const entries = toArray(values);
        if (entries.length === 0) {
            return "-";
        }
        return entries.map(value => `<code class="${cssClass}" title="${escapeHtml(value)}">${escapeHtml(value)}</code>`).join(" ");
    }

    function renderHttpRequestMappings(entries) {
        httpRequestMappingsTable.clear();
        entries.forEach(entry => httpRequestMappingsTable.row.add(entry));
        httpRequestMappingsTable.draw();
        httpRequestMappingsTable.columns.adjust();
        $("#httpRequestMappingsStatus").text(`${entries.length.toLocaleString()} mappings loaded.`);
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

    function getHeapDumpAnalysisEntries(response) {
        const entries = response?.classesByRetainedSize
            ?? response?.classesByShallowSize
            ?? response?.classes
            ?? response?.topClasses
            ?? response?.histogram
            ?? response?.entries
            ?? response?.data
            ?? [];
        return Array.isArray(entries) ? entries : [];
    }

    function normalizeHeapDumpEntry(entry) {
        const retainedSize = Number(entry.retainedSizeBytes ?? entry.retainedSize ?? entry.retainedBytes);
        const shallowSize = Number(entry.shallowSizeBytes ?? entry.shallowSize ?? entry.bytes ?? entry.size);
        const instances = Number(entry.instanceCount ?? entry.instances ?? entry.objects ?? entry.count ?? 0);
        const averageShallowSize = Number(entry.averageShallowSizeBytes ?? entry.averageShallowSize ?? entry.avgShallowSize);
        const averageRetainedSize = Number(entry.averageRetainedSizeBytes ?? entry.averageRetainedSize ?? entry.avgRetainedSize);
        const retainedToShallowRatio = Number(entry.retainedToShallowRatio ?? entry.retainedShallowRatio ?? entry.retainedRatio);
        return {
            className: entry.className ?? entry.class ?? entry.name ?? "",
            instances: Number.isFinite(instances) ? instances : 0,
            retained: Number.isFinite(retainedSize) && retainedSize > 0
                ? retainedSize
                : (Number.isFinite(shallowSize) ? shallowSize : 0),
            averageShallowSize: Number.isFinite(averageShallowSize) && averageShallowSize >= 0 ? averageShallowSize : null,
            averageRetainedSize: Number.isFinite(averageRetainedSize) && averageRetainedSize >= 0 ? averageRetainedSize : null,
            retainedToShallowRatio: Number.isFinite(retainedToShallowRatio) && retainedToShallowRatio >= 0 ? retainedToShallowRatio : null
        };
    }

    function formatNumber(value) {
        return Number(value || 0).toLocaleString();
    }

    function formatHeapDumpRatio(value) {
        return Number.isFinite(value) ? `${value.toFixed(2)}x` : "N/A";
    }

    function getHeapDumpTotalObjects(response, entries) {
        const total = Number(response?.totalObjects ?? response?.objects ?? response?.objectCount);
        return Number.isFinite(total)
            ? total
            : entries.reduce((sum, entry) => sum + entry.instances, 0);
    }

    function getHeapDumpTotalBytes(response, entries) {
        const total = Number(response?.totalRetainedSize ?? response?.totalBytes ?? response?.retainedSize ?? response?.bytes);
        return Number.isFinite(total)
            ? total
            : entries.reduce((sum, entry) => sum + entry.retained, 0);
    }

    function renderHeapDumpRetainedCell(entry) {
        const width = Math.max(0, Math.min(100, Number(entry.retainedWidth ?? 0)));
        return `
            <div class="heap-dump-retained-cell">
                <span class="heap-dump-retained-bar">
                    <span style="width:${width}%"></span>
                </span>
                <span>${formatMemoryBytes(entry.retained)}</span>
            </div>`;
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

    async function fetchHeapDumpFile() {
        const response = await fetch(CasActuatorEndpoints.heapDump(), {
            credentials: "include"
        });
        if (!response.ok) {
            throw {status: response.status, message: "Unable to capture heap dump."};
        }
        return response.blob();
    }

    async function analyzeHeapDumpFile(heapDump) {
        const formData = new FormData();
        formData.append("file", heapDump, "heapdump.hprof");
        const endpoint = new URL(CasActuatorEndpoints.heapDumpAnalysis(), window.location.href);
        const response = await fetch(endpoint.toString(), {
            method: "POST",
            credentials: "include",
            headers: {
                Accept: "application/json"
            },
            body: formData
        });
        if (!response.ok) {
            throw {status: response.status, message: "Unable to analyze heap dump."};
        }
        return response.json();
    }

    function showHeapDumpAnalysisProgress(message, progress) {
        Swal.fire({
            title: "Heap Dump Analysis",
            html: `
                <div class="heap-dump-swal-progress">
                    <div id="heapDumpAnalysisProgressMessage" class="heap-dump-swal-progress-message">${message}</div>
                    <div class="heap-dump-swal-progress-track">
                        <span id="heapDumpAnalysisProgressBar" style="width:${progress}%"></span>
                    </div>
                </div>`,
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });
    }

    function updateHeapDumpAnalysisProgress(message, progress) {
        const container = Swal.getHtmlContainer();
        if (!container) {
            return;
        }
        const messageElement = container.querySelector("#heapDumpAnalysisProgressMessage");
        const progressElement = container.querySelector("#heapDumpAnalysisProgressBar");
        if (messageElement) {
            messageElement.textContent = message;
        }
        if (progressElement) {
            progressElement.style.width = `${progress}%`;
        }
    }

    function getHeapDumpAnalysisErrorMessage(error) {
        if (error?.message) {
            return error.message;
        }
        if (error?.status) {
            return `HTTP error: ${error.status}`;
        }
        return "Unable to analyze heap dump.";
    }

    async function refreshHeapDumpAnalysis() {
        if (!CasActuatorEndpoints.heapDump() || !CasActuatorEndpoints.heapDumpAnalysis()) {
            hideElements($("#heapDumpAnalysisContainer"));
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

    function renderLiveMemoryCard(title, icon, colorClass, values, caption) {
        if (!Number.isFinite(values.used) && !Number.isFinite(values.committed)) {
            return "";
        }

        const max = Number.isFinite(values.max) && values.max >= 0 ? values.max : null;
        const barLimit = max ?? values.committed;
        const usage = memoryUsagePercent(values.used, barLimit);
        const width = usage === null ? 0 : usage;
        const footer = max !== null && usage !== null
            ? `${formatMemoryPercent(usage)} of max used`
            : caption;

        return `
            <div class="live-memory-card">
                <div class="live-memory-card-title">
                    <i class="mdi ${icon} ${colorClass}" aria-hidden="true"></i>
                    ${title}
                </div>
                <div class="live-memory-primary">
                    <span>Used</span>
                    <strong>${formatMemoryBytes(values.used)}</strong>
                </div>
                <div class="live-memory-progress">
                    <span class="live-memory-progress-fill ${colorClass}" style="width:${width}%"></span>
                </div>
                <div class="live-memory-stats">
                    <div><span>Used</span><strong>${formatMemoryBytes(values.used)}</strong></div>
                    <div><span>Committed</span><strong>${formatMemoryBytes(values.committed)}</strong></div>
                    <div><span>Max</span><strong>${formatMemoryBytes(values.max)}</strong></div>
                </div>
                <div class="live-memory-caption">${footer}</div>
            </div>`;
    }

    function renderLiveMemoryPoolRow(pool) {
        const usage = memoryUsagePercent(pool.used, pool.max);
        const usageClass = usage !== null && usage >= 80 ? "warning" : "heap";
        const width = usage === null ? 0 : usage;
        const usageLabel = usage === null ? "N/A" : formatMemoryPercent(usage);

        return {
            0: `<code>${escapeHtml(pool.name)}</code>`,
            1: formatMemoryBytes(pool.used),
            2: formatMemoryBytes(pool.committed),
            3: formatMemoryBytes(pool.max),
            4: `
                <div class="live-memory-pool-usage">
                    <span class="live-memory-pool-bar">
                        <span class="live-memory-pool-fill ${usageClass}" style="width:${width}%"></span>
                    </span>
                    <span>${usageLabel}</span>
                </div>`
        };
    }

    async function configureLiveMemory() {
        if (!CasActuatorEndpoints.metrics()) {
            liveMemoryPoolsTable.clear().draw();
            hideElements($("#systemLiveMemoryContainer"));
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
        await configureLiveMemory();
        await configureHeapDumpAnalysis();
        await refreshHttpRequestMappings();
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

    const liveMemoryPoolsTable = $("#systemLiveMemoryPoolsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#systemLiveMemoryPoolsTable tr").addClass("mdc-data-table__row");
            $("#systemLiveMemoryPoolsTable td").addClass("mdc-data-table__cell");
        }
    });

    const httpRequestMappingsTable = $("#httpRequestMappingsTable").DataTable({
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
                    ? `<code class="http-request-mapping-handler" title="${escapeHtml(data)}">${escapeHtml(data)}</code>`
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
        pageLength: 10,
        autoWidth: false,
        order: [[0, "asc"]],
        columns: [
            {data: "rank", width: "3rem", className: "text-end text-muted"},
            {
                data: "className",
                render: (data, type) => type === "display"
                    ? `<code>${escapeHtml(data)}</code>`
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
        pageLength: 10,
        autoWidth: false,
        order: [[0, "desc"]],
        columns: [
            {
                data: "timestampMs",
                className: "text-start",
                width: "10rem",
                render: (data, type, row) => type === "display" ? escapeHtml(row.time) : data
            },
            {
                data: "method",
                width: "6.5rem",
                render: (data, type) => type === "display"
                    ? `<span class="http-exchange-method method-${escapeHtml(String(data).toLowerCase())}">${escapeHtml(data)}</span>`
                    : data
            },
            {
                data: "path",
                width: "65rem",
                render: (data, type) => type === "display"
                    ? `<code class="http-exchange-path">${escapeHtml(data)}</code>`
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
                    return `<span class="http-exchange-status status-${statusClass}">${escapeHtml(data)}</span>`;
                }
            },
            {
                data: "duration",
                width: "7rem",
                render: (data, type, row) => type === "display" ? escapeHtml(row.durationLabel) : data
            },
            {
                data: null,
                orderable: false,
                searchable: false,
                width: "4rem",
                render: (data, type, row) => {
                    if (type !== "display") {
                        return "";
                    }
                    return `
                        <button type="button"
                                class="mdc-button mdc-button--raised http-exchange-details-button"
                                data-http-exchange-id="${escapeHtml(row.id)}"
                                aria-label="View details">
                            <span class="mdc-button__ripple"></span>
                            <span class="mdc-button__label">
                                <i class="mdc-tab__icon mdi mdi-table-eye" aria-hidden="true"></i>
                            </span>
                        </button>`;
                }
            }
        ],
        drawCallback: settings => {
            $("#httpExchangesTable tr").addClass("mdc-data-table__row");
            $("#httpExchangesTable td").addClass("mdc-data-table__cell");
        }
    });

    const casDependenciesTable = $("#casDependenciesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        order: [[1, "asc"]],
        columns: [
            {
                data: "groupId",
                width: "28%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "artifactId",
                width: "26%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "version",
                width: "12%",
                render: (data, type) => type === "display" ? renderDependencyVersion(data) : data
            },
            {
                data: "source",
                width: "34%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
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
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "dependency.artifactId",
                width: "15%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "dependency.version",
                width: "10%",
                render: (data, type) => type === "display" ? renderDependencyVersion(data) : data
            },
            {
                data: "dependency.source",
                width: "22%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "modified",
                width: "12%",
                render: (data, type) => type === "display" ? escapeHtml(formatDependencyTimestamp(data)) : data
            }
        ],
        initComplete: () => applyMdcDataTableControls("#casVulnerabilitiesTable"),
        drawCallback: settings => {
            $("#casVulnerabilitiesTable tr").addClass("mdc-data-table__row");
            $("#casVulnerabilitiesTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#casVulnerabilitiesTable");
        }
    });

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));

    async function configureStartupTimeline() {
        if (!CasActuatorEndpoints.startup()) {
            return;
        }

        let allEvents = [];
        let activeFilter = "all";
        let searchFilter = "";
        let sortOrder = "index";
        let initialized = false;

        function parseDurationMs(durationStr) {
            if (!durationStr) return 0;
            const match = durationStr.match(/PT(?:(\d+)M)?(?:([\d.]+)S)?/);
            if (!match) return 0;
            return (parseFloat(match[1] || 0) * 60 + parseFloat(match[2] || 0)) * 1000;
        }

        function getDurationClass(ms) {
            if (ms < 10) return "fastest";
            if (ms < 50) return "fast";
            if (ms < 200) return "medium";
            if (ms < 1000) return "slow";
            return "slowest";
        }

        function formatDuration(ms) {
            if (ms < 1000) return `${Math.round(ms)} ms`;
            return `${(ms / 1000).toFixed(2)} s`;
        }

        function buildTree(events) {
            const map = {};
            const roots = [];
            events.forEach(e => { map[e.startupStep.id] = {...e, children: []}; });
            events.forEach(e => {
                const node = map[e.startupStep.id];
                const parentId = e.startupStep.parentId;
                if (parentId && parentId !== 0 && map[parentId]) {
                    map[parentId].children.push(node);
                } else {
                    roots.push(node);
                }
            });
            return roots;
        }

        function renderNode(node, depth) {
            const ms = parseDurationMs(node.duration);
            const durClass = getDurationClass(ms);
            const hasChildren = node.children && node.children.length > 0;
            const tags = (node.startupStep.tags || [])
                .map(t => `<span class="startup-tag">${escapeHtml(t.key)}=${escapeHtml(t.value)}</span>`).join("");
            const childLabel = hasChildren
                ? `<span class="startup-children-count">${node.children.length} ${node.children.length === 1 ? "child" : "children"}</span>` : "";
            const expandBtn = hasChildren
                ? `<button class="startup-expand-btn" data-expanded="true" title="Collapse">&#9660;</button>`
                : `<span class="startup-expand-spacer"></span>`;

            const el = document.createElement("div");
            el.className = `startup-step-row depth-${depth}`;
            el.dataset.durationClass = durClass;
            el.dataset.stepName = node.startupStep.name.toLowerCase();
            el.dataset.ms = ms;
            el.innerHTML = `
                <div class="startup-step-main" style="padding-left:${depth * 28}px">
                    ${expandBtn}
                    <div class="startup-step-info">
                        <span class="startup-step-name">${escapeHtml(node.startupStep.name)}</span>
                        <span class="startup-step-index">#${node.startupStep.id}</span>
                        ${childLabel}
                    </div>
                    <span class="startup-duration-badge ${durClass}">${formatDuration(ms)}</span>
                </div>
                ${tags ? `<div class="startup-step-tags" style="padding-left:${depth * 28 + 36}px">${tags}</div>` : ""}
            `;

            if (hasChildren) {
                const childrenEl = document.createElement("div");
                childrenEl.className = "startup-children";
                node.children.forEach(child => childrenEl.appendChild(renderNode(child, depth + 1)));
                el.appendChild(childrenEl);

                el.querySelector(".startup-expand-btn").addEventListener("click", evt => {
                    const btn = evt.currentTarget;
                    const expanded = btn.dataset.expanded === "true";
                    btn.dataset.expanded = String(!expanded);
                    btn.innerHTML = expanded ? "&#9658;" : "&#9660;";
                    btn.title = expanded ? "Expand" : "Collapse";
                    childrenEl.style.display = expanded ? "none" : "";
                });
            }
            return el;
        }

        function countByDuration(events) {
            const counts = {fastest: 0, fast: 0, medium: 0, slow: 0, slowest: 0};
            events.forEach(e => counts[getDurationClass(parseDurationMs(e.duration))]++);
            return counts;
        }

        function applyFilters() {
            document.querySelectorAll("#startupTimelineList .startup-step-row").forEach(row => {
                const matchesDuration = activeFilter === "all" || row.dataset.durationClass === activeFilter;
                const matchesSearch = !searchFilter || row.dataset.stepName.includes(searchFilter);
                row.style.display = matchesDuration && matchesSearch ? "" : "none";
            });
        }

        function renderAll() {
            const list = document.getElementById("startupTimelineList");
            list.innerHTML = "";
            let events = [...allEvents];

            if (sortOrder === "duration-desc") {
                events.sort((a, b) => parseDurationMs(b.duration) - parseDurationMs(a.duration));
                events.forEach(e => list.appendChild(renderNode({...e, children: []}, 0)));
            } else if (sortOrder === "duration-asc") {
                events.sort((a, b) => parseDurationMs(a.duration) - parseDurationMs(b.duration));
                events.forEach(e => list.appendChild(renderNode({...e, children: []}, 0)));
            } else if (sortOrder === "name") {
                events.sort((a, b) => a.startupStep.name.localeCompare(b.startupStep.name));
                events.forEach(e => list.appendChild(renderNode({...e, children: []}, 0)));
            } else {
                buildTree(events).forEach(node => list.appendChild(renderNode(node, 0)));
            }
            applyFilters();
        }

        function attachControls() {
            $("#startupStepFilter").off("input").on("input", function () {
                searchFilter = $(this).val().toLowerCase();
                applyFilters();
            });

            $("#startupExpandAll").off("click").on("click", () => {
                document.querySelectorAll("#startupTimelineList .startup-expand-btn").forEach(btn => {
                    btn.dataset.expanded = "true";
                    btn.innerHTML = "&#9660;";
                    btn.title = "Collapse";
                });
                document.querySelectorAll("#startupTimelineList .startup-children").forEach(c => c.style.display = "");
            });

            $("#startupCollapseAll").off("click").on("click", () => {
                document.querySelectorAll("#startupTimelineList .startup-expand-btn").forEach(btn => {
                    btn.dataset.expanded = "false";
                    btn.innerHTML = "&#9658;";
                    btn.title = "Expand";
                });
                document.querySelectorAll("#startupTimelineList .startup-children").forEach(c => c.style.display = "none");
            });

            $("#startupDurationFilters").off("click", ".startup-duration-filter").on("click", ".startup-duration-filter", function () {
                activeFilter = $(this).data("filter");
                $("#startupDurationFilters .startup-duration-filter").removeClass("active");
                $(this).addClass("active");
                applyFilters();
            });

            $("#startupSortSelect").off("change").on("change", function () {
                sortOrder = $(this).val();
                renderAll();
            });
        }

        if (initialized) {
            attachControls();
            return;
        }

        $.get(CasActuatorEndpoints.startup(), response => {
            initialized = true;
            allEvents = (response.timeline && response.timeline.events) ? response.timeline.events : [];

            if (allEvents.length === 0) {
                $("#startupTimelineList").html('<p class="text-muted p-3">No startup steps available. Ensure <code>BufferingApplicationStartup</code> is configured.</p>');
                return;
            }
            
            const counts = countByDuration(allEvents);
            $("#count-fastest").text(counts.fastest);
            $("#count-fast").text(counts.fast);
            $("#count-medium").text(counts.medium);
            $("#count-slow").text(counts.slow);
            $("#count-slowest").text(counts.slowest);

            renderAll();
            attachControls();
        }).fail((xhr, status, error) => {
            console.error("Error fetching startup data:", error);
            $("#startupTimelineList").html('<p class="text-muted p-3">Could not load startup timeline data.</p>');
            displayBanner(xhr);
        });
    }

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

    $("#system-tabs").on("tabsactivate", function (event, ui) {
        if (ui.newPanel.attr("id") === "casstartup-tab") {
            configureStartupTimeline();
        }
    });

    $("#system-info-tabs").on("tabsactivate", function (event, ui) {
        if (ui.newPanel.attr("id") === "casdependencies-tab") {
            refreshDependencies();
        }
        if (ui.newPanel.attr("id") === "casvulnerabilities-tab") {
            refreshVulnerabilities();
        }
    });

    const storedTabs = localStorage.getItem("ActiveTabs");
    if (storedTabs) {
        try {
            const activeTabs = JSON.parse(storedTabs);
            const startupTabIndex = $("#system-tabs ul li a[href='#casstartup-tab']").parent().index();
            if (activeTabs["system-tabs"] === startupTabIndex) {
                await configureStartupTimeline();
            }
            const systemInfoTabIndex = activeTabs["system-info-tabs"];
            const dependenciesTabIndex = $("#system-info-tabs ul li a[href='#casdependencies-tab']").parent().index();
            if (systemInfoTabIndex === dependenciesTabIndex) {
                await refreshDependencies();
            }
            const vulnerabilitiesTabIndex = $("#system-info-tabs ul li a[href='#casvulnerabilities-tab']").parent().index();
            if (systemInfoTabIndex === vulnerabilitiesTabIndex) {
                await refreshVulnerabilities();
            }
        } catch (e) { /* ignore */ }
    }

    if ($("#system-info-tabs").data("ui-tabs") && $("#system-info-tabs").tabs("option", "active") === 0) {
        await refreshDependencies();
    }

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
