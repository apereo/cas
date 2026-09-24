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

// Prometheus charts - dynamic storage
const prometheusCharts = {};
let prometheusAvailableMetrics = [];
let prometheusEndpointUrl = null;
let prometheusRefreshInterval = 10000;
let prometheusRefreshTimer = null;
let prometheusInitializationPromise = null;
let prometheusInitialized = false;

/**
 * The JVM threads chart configuration.
 *
 * Built fresh on every call because Chart.js takes ownership of the object it is handed.
 *
 * @returns {object} a new Chart.js configuration for the JVM threads chart
 */
function jvmThreadsChartConfiguration() {
    return {
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
            responsive: true,
            maintainAspectRatio: false,
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
    };
}

function cloneChartValue(value) {
    const seen = new WeakSet();
    return JSON.parse(JSON.stringify(value, (key, entry) => {
        if (typeof entry === "function") {
            return undefined;
        }
        if (entry && typeof entry === "object" && !Array.isArray(entry)) {
            if (seen.has(entry)) {
                return undefined;
            }
            seen.add(entry);
        }
        return entry;
    }));
}

function showSystemStatusChartDialog(chart, title) {
    if (!chart) {
        return;
    }
    $("#systemStatusChartDialog").remove();

    const dialogHtml = `
        <div id="systemStatusChartDialog" class="system-status-chart-dialog" title="${title}">
            <canvas id="systemStatusChartDialogCanvas"></canvas>
        </div>`;
    $("body").append(dialogHtml);

    $("#systemStatusChartDialog").dialog({
        modal: true,
        width: Math.min($(window).width() * 0.9, 1400),
        height: Math.min($(window).height() * 0.85, 820),
        resizable: true,
        close: function () {
            if (window.systemStatusDialogChart) {
                window.systemStatusDialogChart.destroy();
                window.systemStatusDialogChart = null;
            }
            $(this).dialog("destroy").remove();
        },
        open: function () {
            const canvas = document.getElementById("systemStatusChartDialogCanvas");
            if (!canvas) {
                return;
            }
            const options = cloneChartValue(chart.options ?? {});
            options.responsive = true;
            options.maintainAspectRatio = false;
            options.plugins = options.plugins ?? {};
            options.plugins.title = {
                ...(options.plugins.title ?? {}),
                display: true,
                text: title,
                font: {size: 18}
            };
            window.systemStatusDialogChart = new Chart(canvas.getContext("2d"), {
                type: chart.config.type,
                data: cloneChartValue(chart.data),
                options
            });
        }
    });
}

function makeSystemStatusChartClickable(canvasId, chartAccessor, title) {
    const canvas = document.getElementById(canvasId);
    const cell = canvas?.closest(".mdc-layout-grid__cell");
    if (!cell) {
        return;
    }
    cell.classList.add("system-status-chart-cell");
    cell.setAttribute("role", "button");
    cell.setAttribute("tabindex", "0");
    cell.setAttribute("aria-label", `Open ${title} chart`);
    const open = () => showSystemStatusChartDialog(chartAccessor(), title);
    cell.addEventListener("click", open);
    cell.addEventListener("keydown", event => {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            open();
        }
    });
}

/**
 * Build a chart only when its canvas is present on the page.
 *
 * Tab fragments are rendered conditionally -- a non-admin never receives the system or tasks
 * panels -- and tabs are now initialized on first activation, so a chart's canvas may legitimately
 * be missing. Returning null keeps the caller's assignment harmless instead of throwing on
 * getContext of a null element.
 *
 * @param canvasId the canvas element id backing the chart
 * @param configuration the Chart.js configuration
 * @returns {Chart|null} the chart, or null when the canvas is absent
 */
function createPalantirChart(canvasId, configuration) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) {
        return null;
    }
    const chart = new Chart(canvas.getContext("2d"), configuration);
    chart.update();
    return chart;
}

/**
 * Charts owned by the Tasks tab.
 */
async function initializeTasksCharts() {
    threadDumpChart = createPalantirChart("threadDumpChart", {
        type: "bar",
        options: {
            responsive: true,
            maintainAspectRatio: false,
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

    jvmThreadsChart = createPalantirChart("jvmThreadsChart", jvmThreadsChartConfiguration());
}

/**
 * Charts owned by the Applications tab.
 */
async function initializeApplicationsCharts() {
    servicesChart = createPalantirChart("servicesChart", {
        type: "bar",
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
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
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
}

/**
 * Charts owned by the System tab.
 */
async function initializeSystemCharts() {
    memoryChart = createPalantirChart("memoryChart", {
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

    statisticsChart = createPalantirChart("statisticsChart", {
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
    auditEventsChart = createPalantirChart("auditEventsChart", {
        type: "bar",
        options: {
            plugins: {
                title: {
                    display: true,
                    text: "Audit Events"
                }
            }
        }
    });

    httpRequestResponsesChart = createPalantirChart("httpRequestResponsesChart", {
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
                    text: "HTTP Requests/Responses (Date)"
                }
            }
        }
    });

    httpRequestsByUrlChart = createPalantirChart("httpRequestsByUrlChart", {
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
                    text: "HTTP Requests/Responses (URL)"
                }
            }
        }
    });

    systemHealthChart = createPalantirChart("systemHealthChart", {
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

    makeSystemStatusChartClickable("memoryChart", () => memoryChart, "Memory");
    makeSystemStatusChartClickable("statisticsChart", () => statisticsChart, "Ticket Registry");
    makeSystemStatusChartClickable("systemHealthChart", () => systemHealthChart, "System Health");
    makeSystemStatusChartClickable("httpRequestResponsesChart", () => httpRequestResponsesChart, "HTTP Requests/Responses (Date)");
    makeSystemStatusChartClickable("httpRequestsByUrlChart", () => httpRequestsByUrlChart, "HTTP Requests/Responses (URL)");
    makeSystemStatusChartClickable("auditEventsChart", () => auditEventsChart, "Audit Events");

}

async function initializePrometheusCharts() {
    prometheusEndpointUrl = CasActuatorEndpoints.prometheus();

    if (!prometheusEndpointUrl) {
        return;
    }
    prometheusRefreshInterval = palantirSettings().refreshInterval;
    window.addEventListener(palantirPollingContextEvent, refreshPrometheusCharts);
    startPrometheusRefresh();
    await refreshPrometheusCharts();
}

async function initializePrometheusChartData() {
    if (prometheusInitialized) {
        return;
    }
    if (prometheusInitializationPromise) {
        return prometheusInitializationPromise;
    }

    prometheusInitializationPromise = (async () => {
        try {
            const response = await fetch(prometheusEndpointUrl);
            if (!response.ok) {
                displayBanner(response);
                return;
            }
            const metricsText = await response.text();
            const parsedMetrics = parsePrometheusMetrics(metricsText);
            prometheusAvailableMetrics = Object.keys(parsedMetrics).sort();

            for (const metricName of prometheusAvailableMetrics) {
                createPrometheusChart(metricName, parsedMetrics[metricName]);
            }
            prometheusInitialized = true;
        } catch (error) {
            displayBanner(error);
        }
    })();
    try {
        await prometheusInitializationPromise;
    } finally {
        prometheusInitializationPromise = null;
    }
}

function startPrometheusRefresh() {
    if (prometheusRefreshTimer) {
        clearInterval(prometheusRefreshTimer);
    }

    prometheusRefreshTimer = setInterval(refreshPrometheusCharts, prometheusRefreshInterval);
}

async function refreshPrometheusCharts() {
    if (!isPalantirPollingContextActive(Tabs.SYSTEM, "#prometheusmetrics-tab")) {
        return;
    }
    if (!prometheusInitialized) {
        await initializePrometheusChartData();
        return;
    }
    await fetchAndUpdateAllCharts();
}

async function fetchAndUpdateAllCharts() {
    if (!isPalantirPollingContextActive(Tabs.SYSTEM, "#prometheusmetrics-tab")
        || Object.keys(prometheusCharts).length === 0) {
        return;
    }

    try {
        const response = await fetch(prometheusEndpointUrl);
        if (!response.ok) {
            return;
        }

        const metricsText = await response.text();
        const parsedMetrics = parsePrometheusMetrics(metricsText);

        for (const metricName of Object.keys(prometheusCharts)) {
            updatePrometheusChart(metricName, parsedMetrics[metricName]);
        }
    } catch (error) {
        console.error(error);
    }
    await updateNavigationSidebar();
}

function parsePrometheusMetrics(text) {
    const metrics = {};
    const lines = text.split("\n");

    for (const line of lines) {
        if (line.startsWith("#") || line.trim() === "") {
            continue;
        }

        // Parse: metric_name{labels} value
        const match = line.match(/^([a-zA-Z_:][a-zA-Z0-9_:]*)(\{[^}]*\})?\s+([0-9.eE+-]+|NaN|Inf|-Inf)$/);
        if (match) {
            const metricName = match[1];
            const labels = match[2] || "";
            const value = parseFloat(match[3]);

            if (!metrics[metricName]) {
                metrics[metricName] = [];
            }
            metrics[metricName].push({ labels, value });
        }
    }

    return metrics;
}

function determinePrometheusChartType(metricName) {
    const name = metricName.toLowerCase();
    if (name.endsWith("_count") || name.endsWith("_total") || name.endsWith("_sum") || name.endsWith("_max")) {
        return "bar";
    }
    return "line";
}

function createPrometheusChart(metricName, metricData) {
    const chartsRow = document.getElementById("prometheusChartsRow");
    if (!chartsRow || !metricData || metricData.length === 0) return;

    const chartId = `prometheusChart_${metricName.replace(/[^a-zA-Z0-9]/g, "_")}`;

    const chartCell = document.createElement("div");
    chartCell.className = "mdc-layout-grid__cell mdc-layout-grid__cell--span-4";
    chartCell.id = `${chartId}_container`;
    chartCell.style.cursor = "pointer";
    chartCell.title = "Click to enlarge";

    const header = document.createElement("div");
    header.className = "mb-1";
    header.innerHTML = `<h6 class="mb-0" title="${metricName}" style="font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${metricName}</h6>`;

    const canvas = document.createElement("canvas");
    canvas.id = chartId;
    canvas.style.maxHeight = "150px";

    chartCell.appendChild(header);
    chartCell.appendChild(canvas);
    chartsRow.appendChild(chartCell);

    const labels = metricData.map(item => formatSeriesLabel(item.labels));
    const values = metricData.map(item => item.value);
    const colors = generateColors(metricData.length);

    const chartType = determinePrometheusChartType(metricName);
    const chartConfig = {
        labels: labels,
        values: values,
        colors: colors,
        metricName: metricName,
        chartType: chartType
    };

    const chart = new Chart(canvas.getContext("2d"), {
        type: chartType,
        data: {
            labels: labels,
            datasets: [{
                label: metricName,
                data: values,
                backgroundColor: colors.map(c => c.replace("1)", "0.2)")),
                borderColor: colors[0],
                borderWidth: 2,
                fill: true,
                tension: 0.3,
                pointRadius: 2,
                pointHoverRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: false
                }
            },
            scales: {
                x: {
                    display: metricData.length <= 5,
                    ticks: {
                        font: { size: 8 },
                        maxRotation: 45
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        font: { size: 9 }
                    }
                }
            }
        }
    });

    chartCell.addEventListener("click", () => {
        showMaximizedChart(chartConfig);
    });

    prometheusCharts[metricName] = chart;
}

function showMaximizedChart(chartConfig) {
    $("#prometheusChartDialog").remove();

    const dialogHtml = `
        <div id="prometheusChartDialog" title="${chartConfig.metricName}">
            <canvas id="prometheusChartDialogCanvas" style="width: 100%; height: 100%;"></canvas>
        </div>
    `;
    $("body").append(dialogHtml);

    $("#prometheusChartDialog").dialog({
        modal: true,
        width: Math.min($(window).width() * 0.85, 1200),
        height: Math.min($(window).height() * 0.8, 700),
        resizable: true,
        close: function() {
            // Destroy chart and remove dialog on close
            if (window.prometheusDialogChart) {
                window.prometheusDialogChart.destroy();
                window.prometheusDialogChart = null;
            }
            $(this).dialog("destroy").remove();
        },
        open: function() {
            const canvas = document.getElementById("prometheusChartDialogCanvas");
            if (canvas) {
                window.prometheusDialogChart = new Chart(canvas.getContext("2d"), {
                    type: chartConfig.chartType,
                    data: {
                        labels: chartConfig.labels,
                        datasets: [{
                            label: chartConfig.metricName,
                            data: chartConfig.values,
                            backgroundColor: chartConfig.colors.map(c => c.replace("1)", "0.2)")),
                            borderColor: chartConfig.colors[0],
                            borderWidth: 2,
                            fill: true,
                            tension: 0.3,
                            pointRadius: 4,
                            pointHoverRadius: 6
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: true,
                                position: "top"
                            },
                            title: {
                                display: true,
                                text: chartConfig.metricName,
                                font: { size: 16 }
                            }
                        },
                        scales: {
                            x: {
                                display: true,
                                ticks: {
                                    font: { size: 11 },
                                    maxRotation: 45
                                }
                            },
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    font: { size: 11 }
                                }
                            }
                        }
                    }
                });
            }
        }
    });
}

function updatePrometheusChart(metricName, metricData) {
    const chart = prometheusCharts[metricName];
    if (!chart || !metricData) return;

    const labels = metricData.map(item => formatSeriesLabel(item.labels));
    const values = metricData.map(item => item.value);

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;
    chart.update("none");
}

function formatSeriesLabel(labelsString) {
    if (!labelsString || labelsString === "") {
        return "value";
    }

    const parts = [];
    const matches = labelsString.matchAll(/(\w+)="([^"]+)"/g);
    for (const match of matches) {
        parts.push(match[2]);
    }

    if (parts.length === 0) {
        return labelsString;
    }

    const label = parts.join("/");
    return label.length > 20 ? `${label.substring(0, 17)}...` : label;
}

function generateColors(count) {
    const baseColors = [
        "rgba(54, 162, 235, 1)",
        "rgba(255, 99, 132, 1)",
        "rgba(75, 192, 192, 1)",
        "rgba(255, 206, 86, 1)",
        "rgba(153, 102, 255, 1)",
        "rgba(255, 159, 64, 1)",
        "rgba(199, 199, 199, 1)",
        "rgba(83, 102, 255, 1)",
        "rgba(255, 99, 255, 1)",
        "rgba(99, 255, 132, 1)"
    ];

    const colors = [];
    for (let i = 0; i < count; i++) {
        colors.push(baseColors[i % baseColors.length]);
    }
    return colors;
}

/**
 * Builds a sentence describing what a chart currently shows.
 *
 * Chart.js paints into a canvas, which assistive technology can only report as
 * an unlabelled image. This reads the chart's own configuration and data, so
 * the description always matches what is on screen, including after a refresh.
 *
 * @param chart the Chart.js instance to describe
 * @param maxPoints how many data points to name per dataset before summarising
 * @returns {string} a description suitable for use as the canvas aria-label
 */
/**
 * The canvas label as the template authored it.
 *
 * The description this module writes ends up in aria-label, so reading aria-label back as the
 * heading on the next refresh would prepend the previous description to the new one and grow the
 * attribute without bound. The template's own label is captured once and reused instead.
 *
 * The cache is tested for presence rather than truthiness: a canvas built at runtime has no
 * aria-label, and an empty cached value read back as falsy would re-capture the description this
 * module had meanwhile written into the attribute.
 *
 * @param canvas the chart's canvas element
 * @returns {string} the original aria-label, or an empty string when the canvas had none
 */
function chartOriginalLabel(canvas) {
    if (!("chartOriginalLabel" in canvas.dataset)) {
        canvas.dataset.chartOriginalLabel = canvas.getAttribute("aria-label") ?? "";
    }
    return canvas.dataset.chartOriginalLabel;
}

function buildChartAccessibleDescription(chart, maxPoints = 8) {
    const canvas = chart?.canvas;
    if (!canvas) {
        return "";
    }
    const title = chart.options?.plugins?.title?.text
        || canvas.getAttribute("data-chart-title")
        || chartOriginalLabel(canvas)
        || "Chart";
    const heading = Array.isArray(title) ? title.join(" ") : String(title);
    const labels = chart.data?.labels ?? [];
    const datasets = chart.data?.datasets ?? [];
    if (datasets.length === 0) {
        return `${heading}. No data.`;
    }
    const parts = datasets.map(dataset => {
        const values = (dataset.data ?? []).map((value, index) => {
            const name = labels[index] ?? `point ${index + 1}`;
            const raw = value && typeof value === "object"
                ? (value.y ?? value.v ?? JSON.stringify(value))
                : value;
            return `${name} ${raw}`;
        });
        const shown = values.slice(0, maxPoints).join(", ");
        const rest = values.length > maxPoints ? `, and ${values.length - maxPoints} more` : "";
        const name = dataset.label ? `${dataset.label}: ` : "";
        return `${name}${shown}${rest}`;
    });
    return `${heading}. ${parts.join(". ")}.`;
}

/**
 * Keeps every canvas chart described for assistive technology.
 *
 * Registered once as a Chart.js plugin so it covers charts created anywhere,
 * including the Prometheus charts built at runtime and the maximised dialog
 * copies, without any call site having to remember to do it.
 *
 * @param chart the Chart.js instance that has just rendered or updated
 */
function applyChartAccessibleDescription(chart) {
    const canvas = chart?.canvas;
    if (!canvas) {
        return;
    }
    const description = buildChartAccessibleDescription(chart);
    if (description && canvas.getAttribute("aria-label") !== description) {
        canvas.setAttribute("role", "img");
        canvas.setAttribute("aria-label", description);
    }
}

if (typeof Chart !== "undefined" && typeof Chart.register === "function") {
    Chart.register({
        id: "palantirChartAccessibility",
        afterUpdate: applyChartAccessibleDescription,
        afterRender: applyChartAccessibleDescription
    });
}
