/* HTTP exchanges, traces and request mappings, as the System page shows them. */

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

function formatNanosDuration(value) {
    const nanos = Number(value);
    if (!Number.isFinite(nanos)) {
        return "N/A";
    }
    if (nanos < 1_000) {
        return `${Math.round(nanos)} ns`;
    }
    if (nanos < 1_000_000) {
        return `${(nanos / 1_000).toFixed(1)} us`;
    }
    if (nanos < 1_000_000_000) {
        return `${(nanos / 1_000_000).toFixed(1)} ms`;
    }
    return `${(nanos / 1_000_000_000).toFixed(2)} s`;
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
            && /\/dashboard\/?$/i.test(referrerUrl.pathname);
    } catch (e) {
        return String(referrer).includes("/dashboard");
    }
}

function httpExchangeOriginatedFromPalantir(entry) {
    return httpExchangeOriginatedFromApplication(entry) || httpExchangeOriginatedFromDashboard(entry);
}

function httpExchangeIsWebjarsPath(path) {
    return [...getHttpExchangePathVariants(path)].some(candidate => {
        const value = candidate.toLowerCase();
        return value === "/webjars"
            || value.startsWith("/webjars/");
    });
}

function httpExchangeIsPalantirOrWebjarsPath(path) {
    return [...getHttpExchangePathVariants(path)].some(candidate => {
        const value = candidate.toLowerCase();
        return httpExchangeIsWebjarsPath(candidate)
            || value.includes("palantir");
    });
}

function httpExchangeIsPalantirGeneratedRequest(entry) {
    return httpExchangeIsPalantirOrWebjarsPath(entry.path)
        && httpExchangeOriginatedFromPalantir(entry);
}

function httpExchangeShouldBeDisplayed(entry) {
    return !httpExchangeIsPalantirGeneratedRequest(entry)
        && (!httpExchangeIsActuatorPath(entry.path)
        || (!httpExchangeOriginatedFromApplication(entry) && !httpExchangeOriginatedFromDashboard(entry)));
}

function stripHttpExchangeQuery(path) {
    return String(path ?? "").replaceAll(/\?.+/gi, "");
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

function httpExchangeIsStaticAssetPath(path) {
    const value = path.toLowerCase();
    return [".js", ".ico", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".css"]
        .some(extension => value.endsWith(extension));
}

/**
 * Whether the charts count a request: requests to Palantir itself, such as its pages, scripts and styles, to web
 * libraries, to the actuator and for static files are left out, whoever made them.
 *
 * @param entry the request
 * @returns {boolean} true when the charts count it
 */
function httpExchangeUrlIsAcceptable(entry) {
    const path = stripHttpExchangeQuery(entry.path);
    return !httpExchangeIsPalantirOrWebjarsPath(path)
        && !httpExchangeIsActuatorPath(path)
        && !httpExchangeIsStaticAssetPath(path);
}

function updateHttpExchangeCharts(entries) {
    const httpSuccesses = [];
    const httpFailures = [];
    const perUrl = new Map();

    let totalHttpSuccess = 0;
    let totalHttpFailure = 0;

    for (const entry of entries) {
        const url = stripHttpExchangeQuery(entry.path);
        if (httpExchangeUrlIsAcceptable(entry)) {
            const counts = perUrl.get(url) ?? {success: 0, failure: 0};
            perUrl.set(url, counts);
            const time = entry.timestamp ? formatDateYearMonthDayHourMinute(entry.timestamp) : entry.time;
            if (entry.status >= 100 && entry.status <= 400) {
                totalHttpSuccess++;
                httpSuccesses.push({x: time, y: totalHttpSuccess});
                counts.success++;
            } else {
                totalHttpFailure++;
                httpFailures.push({x: time, y: totalHttpFailure});
                counts.failure++;
            }
        }
    }
    const urls = [...perUrl.entries()].sort(([, first], [, second]) => second.success + second.failure - first.success - first.failure);
    const httpSuccessesPerUrl = urls.map(([url, counts]) => ({x: url, y: counts.success}));
    const httpFailuresPerUrl = urls.map(([url, counts]) => ({x: url, y: counts.failure}));
    if (!httpRequestResponsesChart || !httpRequestsByUrlChart) {
        return;
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

function updateHttpExchangeMethodOptions(entries) {
    const methods = [...new Set(entries.map(entry => entry.method).filter(method => method))].sort();
    const currentValue = $("#httpExchangesMethodFilter").val();
    $("#httpExchangesMethodFilter").empty().append($("<option>", {
        value: "",
        text: "All Methods"
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

function renderHeaderEntries(headers) {
    const entries = Object.entries(headers ?? {});
    if (entries.length === 0) {
        return `<div class="http-exchange-empty">No headers recorded.</div>`;
    }
    return entries.map(([name, value]) => {
        const renderedValue = Array.isArray(value) ? value.join(", ") : value;
        return `
            <div class="http-exchange-header-row">
                <strong>${escapePalantirMarkup(name)}</strong>
                ${palantirCode(renderedValue)}
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
                    <strong>${escapePalantirMarkup(name)}</strong>
                    ${palantirCode(value)}
                `).join("")}
            </div>
            <div class="http-exchange-headers">
                <section>
                    <h4>Request Headers</h4>
                    ${renderHeaderEntries(entry.requestHeaders)}
                </section>
                <section>
                    <h4>Response Headers</h4>
                    ${renderHeaderEntries(entry.responseHeaders)}
                </section>
            </div>
        </div>`;
}

function getHttpTraceStatus(summary) {
    return summary.status
        ?? summary.httpStatus
        ?? summary.statusCode
        ?? summary.responseStatus
        ?? summary.httpResponseStatusCode
        ?? null;
}

function httpTraceStatusClass(value, error) {
    const status = Number(value);
    if (error || status >= 500) {
        return "server-error";
    }
    if (status >= 400) {
        return "client-error";
    }
    if (status >= 300) {
        return "redirect";
    }
    if (status >= 200) {
        return "success";
    }
    if (status >= 100) {
        return "informational";
    }
    return error ? "server-error" : "success";
}

function renderHttpTraceStatus(value, error) {
    const status = Number(value);
    const statusClass = httpTraceStatusClass(value, error);
    const label = Number.isFinite(status) && status > 0 ? status : error ? "ERROR" : "OK";
    return `<span class="http-trace-status status-${statusClass}">${escapePalantirMarkup(label)}</span>`;
}

function renderHttpTraceError(error) {
    return error
        ? `<span class="http-trace-error http-trace-error-yes">YES</span>`
        : `<span class="http-trace-error http-trace-error-no">NO</span>`;
}

function renderHttpTraceMethod(method) {
    return method
        ? `<span class="http-exchange-method method-${escapePalantirMarkup(String(method).toLowerCase())}">${escapePalantirMarkup(method)}</span>`
        : "";
}

function renderHttpTraceRootSpan(rootSpan) {
    const value = String(rootSpan ?? "");
    if (value.length === 0) {
        return `<span class="http-trace-root-span">N/A</span>`;
    }
    const match = value.match(/^(http)\s+(get|post|put|patch|delete|options|head)\b(.*)$/i);
    if (!match) {
        return `<span class="http-trace-root-span">${escapePalantirMarkup(value)}</span>`;
    }
    return `
        <span class="http-trace-root-span">
            ${escapePalantirMarkup(match[1].toUpperCase())}
            ${renderHttpTraceMethod(match[2].toUpperCase())}
            ${escapePalantirMarkup(match[3] ?? "")}
        </span>`;
}

function renderHttpTraceServices(services) {
    const values = toArray(services).filter(service => service);
    if (values.length === 0) {
        return `<span class="text-muted">N/A</span>`;
    }
    return values
        .map(service => `<span class="http-trace-service">${escapePalantirMarkup(service)}</span>`)
        .join("");
}

function normalizeHttpTraceSummary(summary, index) {
    const startedAt = new Date(summary.startedAt);
    const status = getHttpTraceStatus(summary);
    const error = summary.error === true || Number(status) >= 400;
    return {
        id: `http-trace-${index}`,
        raw: summary,
        traceId: summary.traceId ?? "",
        startedAt: Number.isNaN(startedAt.getTime()) ? null : startedAt,
        startedAtMs: Number.isNaN(startedAt.getTime()) ? 0 : startedAt.getTime(),
        startedAtLabel: Number.isNaN(startedAt.getTime()) ? "N/A" : startedAt.toLocaleString(),
        rootSpan: summary.rootSpan ?? "",
        method: summary.method ?? "",
        url: summary.url ?? "",
        route: summary.route ?? "",
        services: toArray(summary.services),
        spanCount: Number(summary.spanCount ?? 0),
        durationNanos: Number(summary.durationNanos ?? 0),
        status,
        error
    };
}

function setHttpTracesUnavailable() {
    hideElements($("#httpTracesContent"));
    hideElements($("#httptracestab").parent());
    if ($("#system-tabs").data("ui-tabs")) {
        $("#system-tabs").tabs("refresh");
    }
}

function setHttpTracesAvailable() {
    showElements($("#httpTracesContent"));
    showElements($("#httptracestab").parent());
    if ($("#system-tabs").data("ui-tabs")) {
        $("#system-tabs").tabs("refresh");
    }
}

function httpTraceDetailsUrl(traceId) {
    const endpoint = CasActuatorEndpoints.httpTraces();
    return `${endpoint.replace(/\/$/, "")}/${encodeURIComponent(traceId)}`;
}

function normalizeHttpTraceSpan(span, traceStart) {
    const startEpochNanos = Number(span.startEpochNanos ?? 0);
    const durationNanos = Number(span.durationNanos ?? 0);
    const attributes = span.attributes ?? {};
    const status = span.status
        ?? attributes["http.response.status_code"]
        ?? attributes["http.status_code"]
        ?? attributes["http.response.status"];
    return {
        ...span,
        name: span.name ?? "",
        serviceName: span.serviceName ?? "",
        kind: span.kind ?? "",
        parentSpanId: span.parentSpanId ?? "",
        startEpochNanos,
        relativeStartNanos: Math.max(0, startEpochNanos - traceStart),
        durationNanos,
        attributes,
        status,
        error: span.error === true || Number(status) >= 400
    };
}

function renderHttpTraceAttributes(attributes) {
    const entries = Object.entries(attributes ?? {});
    if (entries.length === 0) {
        return `<div class="http-trace-empty">No attributes recorded.</div>`;
    }
    return entries
        .sort(([first], [second]) => first.localeCompare(second))
        .map(([name, value]) => `
            <div class="http-trace-attribute-row">
                <strong>${escapePalantirMarkup(name)}</strong>
                ${palantirCode(value)}
            </div>`)
        .join("");
}

function renderHttpTraceTimeline(spans, durationNanos) {
    if (spans.length === 0) {
        return `<div class="http-trace-empty">No spans recorded.</div>`;
    }
    const totalDuration = Math.max(Number(durationNanos), ...spans.map(span => span.relativeStartNanos + span.durationNanos), 1);
    return spans.map((span, index) => {
        const left = Math.min(100, Math.max(0, (span.relativeStartNanos / totalDuration) * 100));
        const width = Math.max(.5, Math.min(100 - left, (span.durationNanos / totalDuration) * 100));
        return `
            <div class="http-trace-span-row ${span.error ? "http-trace-span-error" : ""}">
                <div class="http-trace-span-summary">
                    <span class="http-trace-span-service">${escapePalantirMarkup(span.serviceName || "N/A")}</span>
                    <span class="http-trace-span-name">${escapePalantirMarkup(span.name || `Span ${index + 1}`)}</span>
                    <span class="http-trace-span-kind">${escapePalantirMarkup(span.kind || "INTERNAL")}</span>
                </div>
                <div class="http-trace-span-track" aria-label="${escapePalantirMarkup(span.name)} duration">
                    <span class="http-trace-span-bar" style="left:${left}%;width:${width}%"></span>
                </div>
                <div class="http-trace-span-duration">${escapePalantirMarkup(formatNanosDuration(span.durationNanos))}</div>
            </div>`;
    }).join("");
}

function renderHttpTraceSpanTable(spans) {
    if (spans.length === 0) {
        return `<div class="http-trace-empty">No spans recorded.</div>`;
    }
    return `
        <div class="http-trace-spans-table-wrap">
            <table class="mdc-data-table__table table table-striped noborder http-trace-spans-table">
                <thead>
                <tr class="mdc-data-table__header-row">
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Trace ID</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Span ID</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Parent</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Name</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Service</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Kind</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Start Epoch</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Start</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Duration</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Status</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Error</th>
                </tr>
                </thead>
                <tbody class="mdc-data-table__content">
                ${spans.map(span => `
                    <tr class="mdc-data-table__row ${span.error ? "http-trace-row-error" : ""}">
                        <td class="mdc-data-table__cell">${palantirCode(span.traceId ?? "")}</td>
                        <td class="mdc-data-table__cell">${palantirCode(span.spanId ?? "")}</td>
                        <td class="mdc-data-table__cell">${palantirCode(span.parentSpanId || "")}</td>
                        <td class="mdc-data-table__cell">${escapePalantirMarkup(span.name)}</td>
                        <td class="mdc-data-table__cell">${escapePalantirMarkup(span.serviceName)}</td>
                        <td class="mdc-data-table__cell">${escapePalantirMarkup(span.kind)}</td>
                        <td class="mdc-data-table__cell">${palantirCode(span.startEpochNanos)}</td>
                        <td class="mdc-data-table__cell">${escapePalantirMarkup(formatNanosDuration(span.relativeStartNanos))}</td>
                        <td class="mdc-data-table__cell">${escapePalantirMarkup(formatNanosDuration(span.durationNanos))}</td>
                        <td class="mdc-data-table__cell">${renderHttpTraceStatus(span.status, span.error)}</td>
                        <td class="mdc-data-table__cell">${renderHttpTraceError(span.error)}</td>
                    </tr>
                    <tr class="mdc-data-table__row http-trace-attributes-row">
                        <td class="mdc-data-table__cell" colspan="11">
                            ${renderHttpTraceAttributes(span.attributes)}
                        </td>
                    </tr>`).join("")}
                </tbody>
            </table>
        </div>`;
}

function renderHttpTraceDialogContent(detail) {
    const rawSpans = detail.spans ?? [];
    const traceStart = Math.min(...rawSpans.map(entry => Number(entry.startEpochNanos ?? 0)));
    const spans = rawSpans
        .map(span => normalizeHttpTraceSpan(span, Number.isFinite(traceStart) ? traceStart : 0))
        .sort((a, b) => a.startEpochNanos - b.startEpochNanos);
    const services = [...new Set(spans.map(span => span.serviceName).filter(service => service))];
    const traceError = spans.some(span => span.error);
    const durationNanos = Number(detail.durationNanos ?? 0);
    return `
        <div class="http-trace-dialog">
            <div class="http-trace-dialog-summary">
                <div>
                    <span class="http-trace-dialog-label">Trace ID</span>
                    ${palantirCode(detail.traceId ?? "")}
                </div>
                <div>
                    <span class="http-trace-dialog-label">Spans</span>
                    <strong>${escapePalantirMarkup(spans.length)}</strong>
                </div>
                <div>
                    <span class="http-trace-dialog-label">Duration</span>
                    <strong>${escapePalantirMarkup(formatNanosDuration(durationNanos))}</strong>
                </div>
                <div>
                    <span class="http-trace-dialog-label">Status</span>
                    ${renderHttpTraceStatus(null, traceError)}
                </div>
            </div>
            <div class="http-trace-dialog-services">
                ${renderHttpTraceServices(services)}
            </div>
            <div class="http-trace-timeline">
                ${renderHttpTraceTimeline(spans, durationNanos)}
            </div>
            ${renderHttpTraceSpanTable(spans)}
        </div>`;
}

async function openHttpTraceDialog(traceId) {
    $("#httpTraceDialog-title").text(`HTTP Trace ${traceId}`);
    $("#httpTraceDialog-content").html(`<div class="http-trace-empty">Loading trace details...</div>`);
    const dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("httpTraceDialog"));
    dialog.open();
    $.get(httpTraceDetailsUrl(traceId), response => {
        $("#httpTraceDialog-content").html(renderHttpTraceDialogContent(response));
    }).fail((xhr, status, error) => {
        console.error("Error fetching HTTP trace details:", error);
        $("#httpTraceDialog-content").html(`<div class="http-trace-empty">Unable to load trace details.</div>`);
        displayBanner(xhr);
    });
}

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
        return `<span class="http-exchange-method method-${escapePalantirMarkup(value.toLowerCase())}">${escapePalantirMarkup(value)}</span>`;
    }).join(" ");
}

function renderHttpMappingValues(values, cssClass = "") {
    const entries = toArray(values);
    if (entries.length === 0) {
        return "-";
    }
    return entries.map(value => `<code class="${cssClass}" title="${escapePalantirMarkup(value)}">${escapePalantirMarkup(value)}</code>`).join(" ");
}
