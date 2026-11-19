async function initializeCasEventsOperations() {
    if (CasActuatorEndpoints.events()) {
        const casEventsTable = $("#casEventsTable").DataTable({
        deferRender: true,
            pageLength: 10,
            autoWidth: false,
            drawCallback: settings => {
                $("#casEventsTable tr").addClass("mdc-data-table__row");
                $("#casEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        function updateCasEventsTable() {
            if (!isPalantirPollingContextActive(Tabs.LOGGING, "#casEvents-tab")) {
                return;
            }
            $.ajax({
                url: `${CasActuatorEndpoints.events()}`,
                type: "GET",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                success: (response, textStatus, xhr) => {
                    casEventsTable.clear();
                    const jsonEvents = typeof response === "string" ? JSON.parse(response) : response;
                    if (jsonEvents.length > 0) {
                        for (const entry of jsonEvents) {
                            const geoLocation = `${entry?.properties?.geoLatitude ?? ""} ${entry?.properties?.geoLongitude ?? ""} ${entry?.properties?.geoAccuracy ?? ""}`.trim();
                            casEventsTable.row.add({
                                0: `${palantirCode(entry?.creationTime ?? "N/A")}`,
                                1: `${palantirCode(getLastWord(entry?.type) ?? "N/A")}`,
                                2: `${palantirCode(entry?.properties?.eventId ?? "N/A")}`,
                                3: `${palantirCode(entry?.principalId ?? "N/A")}`,
                                4: `${palantirCode(entry?.properties?.clientip ?? "N/A")}`,
                                5: `${palantirCode(entry?.properties?.serverip ?? "N/A")}`,
                                6: `${palantirCode(entry?.properties?.agent ?? "N/A")}`,
                                7: `${palantirCode(entry?.properties?.tenant ?? "N/A")}`,
                                8: `${palantirCode(entry?.properties?.deviceFingerprint ?? "N/A")}`,
                                9: `${palantirCode(geoLocation.length === 0 ? "N/A" : geoLocation)}`
                            });
                        }
                    }
                    casEventsTable.draw();
                },
                error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
            });
        }

        setInterval(updateCasEventsTable, palantirSettings().refreshInterval);
        window.addEventListener(palantirPollingContextEvent, updateCasEventsTable);
        updateCasEventsTable();
    }
}

async function initializeAuditEventsOperations() {
    if (CasActuatorEndpoints.auditLog()) {
        function escapeHtml(str) {
            return String(str).replace(/[&<>"']/g, s => ({
                "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
            }[s]));
        }

        function splitAuditResourceParts(value, separator) {
            const parts = [];
            let depth = 0;
            let start = 0;
            for (let i = 0; i < value.length; i++) {
                const ch = value[i];
                if (ch === "{" || ch === "[" || ch === "(") {
                    depth++;
                }
                if (ch === "}" || ch === "]" || ch === ")") {
                    depth = Math.max(0, depth - 1);
                }
                if (ch === separator && depth === 0) {
                    parts.push(value.substring(start, i).trim());
                    start = i + 1;
                }
            }
            parts.push(value.substring(start).trim());
            return parts.filter(part => part.length > 0);
        }

        function findAuditResourceAssignment(value) {
            let depth = 0;
            for (let i = 0; i < value.length; i++) {
                const ch = value[i];
                if (ch === "{" || ch === "[" || ch === "(") {
                    depth++;
                }
                if (ch === "}" || ch === "]" || ch === ")") {
                    depth = Math.max(0, depth - 1);
                }
                if (ch === "=" && depth === 0) {
                    return i;
                }
            }
            return -1;
        }

        function parseAuditResourceScalar(value) {
            const trimmed = String(value ?? "").trim();
            if (trimmed === "null") {
                return null;
            }
            if (trimmed === "true") {
                return true;
            }
            if (trimmed === "false") {
                return false;
            }
            if (/^-?\d+(?:\.\d+)?$/.test(trimmed)) {
                return Number(trimmed);
            }
            return trimmed;
        }

        function parseAuditResourceMap(value) {
            const trimmed = String(value ?? "").trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}") || !trimmed.includes("=")) {
                return null;
            }
            const body = trimmed.substring(1, trimmed.length - 1).trim();
            if (body.length === 0) {
                return {};
            }
            return splitAuditResourceParts(body, ",").reduce((resource, part) => {
                const index = findAuditResourceAssignment(part);
                if (index <= 0) {
                    return resource;
                }
                const key = part.substring(0, index).trim();
                const rawValue = part.substring(index + 1).trim();
                resource[key] = parseAuditResourceValue(rawValue);
                return resource;
            }, {});
        }

        function parseAuditResourceList(value) {
            const trimmed = String(value ?? "").trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                return null;
            }
            const body = trimmed.substring(1, trimmed.length - 1).trim();
            if (body.length === 0) {
                return [];
            }
            return splitAuditResourceParts(body, ",").map(parseAuditResourceValue);
        }

        function parseAuditResourceValue(value) {
            const asMap = parseAuditResourceMap(value);
            if (asMap !== null) {
                return asMap;
            }
            const asList = parseAuditResourceList(value);
            if (asList !== null) {
                return asList;
            }
            return parseAuditResourceScalar(value);
        }

        function parseAuditResource(value) {
            if (typeof value !== "string") {
                return null;
            }
            const trimmed = value.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                try {
                    return JSON.parse(trimmed);
                } catch (e) {
                    const parsed = parseAuditResourceValue(trimmed);
                    return typeof parsed === "object" ? parsed : null;
                }
            }
            return null;
        }

        function parseJsonAuditResource(value) {
            if (value && typeof value === "object") {
                return value;
            }
            if (typeof value !== "string") {
                return null;
            }
            const trimmed = value.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return null;
            }
            try {
                const parsed = JSON.parse(trimmed);
                return parsed && typeof parsed === "object" ? parsed : null;
            } catch (e) {
                return null;
            }
        }

        function auditActionType(action) {
            const value = String(action ?? "").toLowerCase();
            if (/(fail|failure|failed|deny|denied|error|exception|unauthori[sz]ed|invalid)/i.test(value)) {
                return "failure";
            }
            if (/(success|successful|succeed|succeeded|grant|granted|allow|allowed|valid)/i.test(value)) {
                return "success";
            }
            return "unknown";
        }

        function renderAuditAction(action) {
            const type = auditActionType(action);
            const text = action || "N/A";
            return `<span class="audit-action-tag audit-action-${type}" title="${escapeHtml(text)}">`
                + `${escapeHtml(text).replaceAll("_", "_<wbr>")}</span>`;
        }

        function renderAuditPrincipal(principal) {
            const parts = String(principal).split(/(?<=[:@.\\/])/).map(part => escapeHtml(part));
            return `<span class="audit-principal-value" title="${escapeHtml(principal)}">
                        <i class="mdi mdi-account" aria-hidden="true"></i>
                        <code>${parts.join("<wbr>")}</code>
                    </span>`;
        }

        /**
         * What an audit record was for: an application, by name, that opens its definition, or another target, such
         * as a multifactor provider, as it is recorded.
         *
         * @param {string} target the target, or nothing
         * @returns {string} the cell
         */
        function renderAuditTarget(target) {
            if (!target) {
                return "<span class=\"audit-target-none\" aria-label=\"None\">—</span>";
            }
            if (/^\d+$/.test(target) && CasActuatorEndpoints.registeredServices()) {
                const name = typeof registeredServicesById !== "undefined" ? registeredServicesById.get(target)?.name : null;
                return `<button type="button" class="audit-target-application" data-audit-application="${escapeHtml(target)}"
                            title="Open application ${escapeHtml(target)}">
                            <i class="mdi mdi-application-outline" aria-hidden="true"></i>
                            <span>${escapeHtml(name ?? `Application ${target}`)}</span>
                        </button>`;
            }
            return `<code class="audit-target-value">${escapeHtml(target)}</code>`;
        }

        /**
         * Name the applications of the rows shown that Palantir has not listed yet, reading each once.
         */
        function nameAuditTargetApplications() {
            if (typeof palantirApplicationName !== "function") {
                return;
            }
            $("#auditEventsTable [data-audit-application]").each((index, button) => {
                const id = button.dataset.auditApplication;
                palantirApplicationName(id).then(name => {
                    $(button).find("span").text(name);
                    if (typeof palantirApplicationIsGone === "function" && palantirApplicationIsGone(id)) {
                        $(button).addClass("is-gone").attr("title", "This application no longer exists");
                    }
                });
            });
        }

        function renderAuditResourceSummary(data, row) {
            if (row.resourceDetails && typeof row.resourceDetails === "object") {
                return `
                    <div class="audit-resource-json-summary">
                        ${renderAuditJsonValue(row.resourceDetails)}
                    </div>`;
            }
            return `<code class="audit-resource-value">${escapeHtml(data)}</code>`;
        }

        function normalizeAuditEvent(entry, index) {
            const resource = entry?.auditableResource ?? entry?.resourceOperatedUpon ?? "";
            const jsonResource = parseJsonAuditResource(resource);
            const resourceText = typeof resource === "string" ? resource : JSON.stringify(resource);
            const resourceDetails = jsonResource ?? parseAuditResource(resourceText);
            const action = entry?.actionPerformed ?? "N/A";
            return {
                id: `audit-event-${index}`,
                principal: entry?.principal ?? "N/A",
                resource: resourceText || "N/A",
                resourceDetails,
                action,
                actionType: auditActionType(action),
                target: entry?.target ? String(entry.target) : "",
                date: entry?.whenActionWasPerformed ?? "N/A",
                clientIpAddress: entry?.clientInfo?.clientIpAddress ?? "N/A",
                userAgent: entry?.clientInfo?.userAgent ?? "N/A"
            };
        }

        function renderAuditJsonValue(value) {
            if (value && typeof value === "object") {
                const isArray = Array.isArray(value);
                const entries = isArray
                    ? value.map((entry, index) => [index, entry])
                    : Object.entries(value);
                if (entries.length === 0) {
                    return `<code class="audit-resource-json-value audit-resource-json-value-empty">${isArray ? "[]" : "{}"}</code>`;
                }
                return `<dl class="audit-resource-fields">${entries.map(([key, entry]) => `
                    <div class="audit-resource-field">
                        <dt class="audit-resource-json-key">${escapeHtml(isArray ? `[${key}]` : key)}</dt>
                        <dd class="audit-resource-field-value">${renderAuditJsonValue(entry)}</dd>
                    </div>`).join("")}</dl>`;
            }
            const displayValue = value === null ? "null" : String(value);
            const valueType = value === null ? "null" : typeof value;
            return `<code class="audit-resource-json-value audit-resource-json-value-${valueType}">${escapeHtml(displayValue)}</code>`;
        }

        let auditEventsTable;
        let auditActionsChart;

        function auditActionChartColors(action) {
            const type = auditActionType(action);
            if (type === "success" || type === "failure") {
                const status = type === "success" ? "UP" : "DOWN";
                return {
                    background: palantirStatusColor(status, .45),
                    border: palantirStatusColor(status)
                };
            }
            return {
                background: palantirChartColor(1, .45),
                border: palantirChartColor(1)
            };
        }

        function initializeAuditActionsChart() {
            if (auditActionsChart) {
                return auditActionsChart;
            }
            const canvas = document.getElementById("auditActionsChart");
            if (!canvas || !$("#auditevents-tab").is(":visible")) {
                return null;
            }
            auditActionsChart = new Chart(canvas.getContext("2d"), {
                type: "bar",
                data: {
                    labels: [],
                    datasets: [{
                        label: "Audit Events",
                        data: [],
                        borderSkipped: false,
                        maxBarThickness: 54
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: {
                        duration: 250
                    },
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: context => `${context.raw} ${context.raw === 1 ? "event" : "events"}`
                            }
                        }
                    },
                    scales: {
                        x: {
                            grid: {
                                display: false
                            },
                            ticks: {
                                maxRotation: 35,
                                minRotation: 0
                            }
                        },
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: "Event count"
                            },
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
            return auditActionsChart;
        }

        function updateAuditActionsChart() {
            if (!auditEventsTable) {
                return;
            }
            const chart = initializeAuditActionsChart();
            if (!chart) {
                return;
            }

            const actionCounts = new Map();
            auditEventsTable.rows({search: "applied"}).data().each(entry => {
                const action = String(entry?.action ?? "N/A");
                actionCounts.set(action, (actionCounts.get(action) ?? 0) + 1);
            });
            const groupedActions = [...actionCounts.entries()]
                .sort(([firstAction, firstCount], [secondAction, secondCount]) =>
                    secondCount - firstCount || firstAction.localeCompare(secondAction));
            const colors = groupedActions.map(([action]) => auditActionChartColors(action));
            const eventCount = groupedActions.reduce((total, [, count]) => total + count, 0);
            const actionCount = groupedActions.length;

            chart.data.labels = groupedActions.map(([action]) => action);
            chart.data.datasets[0].data = groupedActions.map(([, count]) => count);
            chart.data.datasets[0].backgroundColor = colors.map(color => color.background);
            chart.data.datasets[0].borderColor = colors.map(color => color.border);
            chart.update();

            $("#auditActionsChartCount").text(
                `${eventCount} ${eventCount === 1 ? "event" : "events"} across `
                + `${actionCount} ${actionCount === 1 ? "action" : "actions"}`
            );
        }

        auditEventsTable = $("#auditEventsTable").DataTable({
        deferRender: true,
            pageLength: 10,
            autoWidth: false,
            columns: [
                {
                    data: "principal",
                    width: "10rem",
                    render: (data, type) => type === "display" ? renderAuditPrincipal(data) : data
                },
                {
                    data: "resource",
                    width: "30rem",
                    render: (data, type, row) => type === "display" ? renderAuditResourceSummary(data, row) : data
                },
                {
                    data: "action",
                    width: "15rem",
                    render: (data, type) => type === "display" ? renderAuditAction(data) : data
                },
                {
                    data: "target",
                    width: "11rem",
                    render: (data, type) => type === "display" ? renderAuditTarget(data) : data
                },
                {
                    data: "date",
                    width: "10rem",
                    render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
                },
                {
                    data: "clientIpAddress",
                    width: "6.5rem",
                    render: (data, type) => type === "display" ? `${palantirCode(data)}` : data
                },
                {
                    data: "userAgent",
                    width: "14rem",
                    render: (data, type) => type === "display" ? `<span class="audit-user-agent">${escapeHtml(data)}</span>` : data
                }
            ],
            drawCallback: settings => {
                $("#auditEventsTable tr").addClass("mdc-data-table__row");
                $("#auditEventsTable td").addClass("mdc-data-table__cell");
                nameAuditTargetApplications();
                updateAuditActionsChart();
            }
        });
        $("#auditEventsTable").off("click.auditTarget").on("click.auditTarget", "[data-audit-application]", event => {
            event.stopPropagation();
            const id = event.currentTarget.dataset.auditApplication;
            if (typeof openTopApplication === "function") {
                openTopApplication(id);
            } else if (typeof navigateToApplication === "function") {
                navigateToApplication(id);
            }
        });
        updateAuditActionsChart();

        let auditEventsRequestInFlight = false;
        let auditEventsRefreshWanted = false;
        let auditEventsUsername = "";
        const auditEventsRefreshButton = $("#refreshAuditEventsButton");

        function auditPrincipalPattern(username) {
            return `^${username.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}$`;
        }

        function showAuditEventsUsername(count) {
            $("#showAllAuditEventsButton").prop("hidden", auditEventsUsername === "");
            $("#auditEventsUsernameHint").text(auditEventsUsername === ""
                ? "The audit records of every username are shown; enter a username to see only that username's records, matched regardless of case."
                : `${count} audit record${count === 1 ? "" : "s"} for ${auditEventsUsername} in the chosen interval and action.`);
        }

        function updateAuditEventsTable() {
            if (!isPalantirPollingContextActive(Tabs.LOGGING, "#auditevents-tab")) {
                return;
            }
            if (auditEventsRequestInFlight) {
                auditEventsRefreshWanted = true;
                return;
            }

            const interval = $("#auditEventsIntervalFilter").val();
            const count = $("#auditEventsCountFilter").val();
            const actionFilter = $("#auditEventsActionFilter").val();
            const username = auditEventsUsername;
            const principal = username === "" ? "" : `&principal=${encodeURIComponent(auditPrincipalPattern(username))}`;

            auditEventsRequestInFlight = true;
            auditEventsRefreshButton.prop("disabled", true);
            $.ajax({
                url: `${CasActuatorEndpoints.auditLog()}?interval=${encodeURIComponent(interval)}&count=${encodeURIComponent(count)}${principal}`,
                type: "GET",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                success: (response, textStatus, xhr) => {
                    let auditEvents;
                    try {
                        const parsedResponse = typeof response === "string" ? JSON.parse(response) : response;
                        auditEvents = Array.isArray(parsedResponse) ? parsedResponse : [];
                    } catch (e) {
                        console.error("Unable to parse audit events:", e);
                        return;
                    }
                    if (username !== auditEventsUsername) {
                        return;
                    }
                    const shown = auditEvents
                        .map(normalizeAuditEvent)
                        .filter(entry => actionFilter === "all" || entry.actionType === actionFilter);
                    auditEventsTable.clear();
                    shown.forEach(entry => auditEventsTable.row.add(entry));
                    auditEventsTable.draw();
                    showAuditEventsUsername(shown.length);
                },
                error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown),
                complete: () => {
                    auditEventsRequestInFlight = false;
                    auditEventsRefreshButton.prop("disabled", false);
                    if (auditEventsRefreshWanted) {
                        auditEventsRefreshWanted = false;
                        updateAuditEventsTable();
                    }
                }
            });
        }

        $("#fmAuditEventsUsername").off("submit").on("submit", event => {
            event.preventDefault();
            auditEventsUsername = String($("#auditEventsUsername").val() ?? "").trim();
            updateAuditEventsTable();
        });
        $("#showAllAuditEventsButton").off("click").on("click", () => {
            $("#auditEventsUsername").val("").trigger("input");
            auditEventsUsername = "";
            updateAuditEventsTable();
            $("#auditEventsUsername").trigger("focus");
        });

        $("#auditEventsIntervalFilter, #auditEventsCountFilter, #auditEventsActionFilter").selectmenu({
            change: updateAuditEventsTable
        });
        auditEventsRefreshButton.off("click").on("click", updateAuditEventsTable);

        const auditEventsRefreshInterval = palantirSettings().refreshInterval;
        setInterval(updateAuditEventsTable, auditEventsRefreshInterval);
        window.addEventListener(palantirPollingContextEvent, updateAuditEventsTable);
        updateAuditEventsTable();
    }
}
