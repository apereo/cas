async function initializeCasEventsOperations() {
    if (CasActuatorEndpoints.events()) {
        const casEventsTable = $("#casEventsTable").DataTable({
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
                                0: `<code>${entry?.creationTime ?? "N/A"}</code>`,
                                1: `<code>${getLastWord(entry?.type) ?? "N/A"}</code>`,
                                2: `<code>${entry?.properties?.eventId ?? "N/A"}</code>`,
                                3: `<code>${entry?.principalId ?? "N/A"}</code>`,
                                4: `<code>${entry?.properties?.clientip ?? "N/A"}</code>`,
                                5: `<code>${entry?.properties?.serverip ?? "N/A"}</code>`,
                                6: `<code>${entry?.properties?.agent ?? "N/A"}</code>`,
                                7: `<code>${entry?.properties?.tenant ?? "N/A"}</code>`,
                                8: `<code>${entry?.properties?.deviceFingerprint ?? "N/A"}</code>`,
                                9: `<code>${geoLocation.length === 0 ? "N/A" : geoLocation}</code>`
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
            return `<span class="audit-action-tag audit-action-${type}">${escapeHtml(action || "N/A")}</span>`;
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

        const auditEventsTable = $("#auditEventsTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            columns: [
                {
                    data: "principal",
                    width: "8rem",
                    render: (data, type) => type === "display" ? `
                        <span class="audit-principal-value">
                            <i class="mdi mdi-account" aria-hidden="true"></i>
                            <code>${escapeHtml(data)}</code>
                        </span>` : data
                },
                {
                    data: "resource",
                    width: "22rem",
                    render: (data, type, row) => type === "display" ? renderAuditResourceSummary(data, row) : data
                },
                {
                    data: "action",
                    width: "15rem",
                    render: (data, type) => type === "display" ? renderAuditAction(data) : data
                },
                {
                    data: "date",
                    width: "10rem",
                    render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
                },
                {
                    data: "clientIpAddress",
                    width: "6.5rem",
                    render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
                },
                {
                    data: "userAgent",
                    width: "22rem",
                    render: (data, type) => type === "display" ? `<span class="audit-user-agent">${escapeHtml(data)}</span>` : data
                }
            ],
            drawCallback: settings => {
                $("#auditEventsTable tr").addClass("mdc-data-table__row");
                $("#auditEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        let auditEventsRequestInFlight = false;

        function updateAuditEventsTable() {
            if (auditEventsRequestInFlight
                || !isPalantirPollingContextActive(Tabs.LOGGING, "#auditevents-tab")) {
                return;
            }

            const interval = $("#auditEventsIntervalFilter").val();
            const count = $("#auditEventsCountFilter").val();
            const actionFilter = $("#auditEventsActionFilter").val();

            auditEventsRequestInFlight = true;
            $.ajax({
                url: `${CasActuatorEndpoints.auditLog()}?interval=${interval}&count=${count}`,
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
                    auditEventsTable.clear();
                    auditEvents
                        .map(normalizeAuditEvent)
                        .filter(entry => actionFilter === "all" || entry.actionType === actionFilter)
                        .forEach(entry => auditEventsTable.row.add(entry));
                    auditEventsTable.draw();
                },
                error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown),
                complete: () => {
                    auditEventsRequestInFlight = false;
                }
            });
        }

        $("#auditEventsIntervalFilter, #auditEventsCountFilter, #auditEventsActionFilter").selectmenu({
            change: updateAuditEventsTable
        });

        const auditEventsRefreshInterval = palantirSettings().refreshInterval;
        setInterval(updateAuditEventsTable, auditEventsRefreshInterval);
        window.addEventListener(palantirPollingContextEvent, updateAuditEventsTable);
        updateAuditEventsTable();
    }
}
