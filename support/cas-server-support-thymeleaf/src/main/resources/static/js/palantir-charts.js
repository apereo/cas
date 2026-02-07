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
                    text: "Audit Events"
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
                    text: "HTTP Requests/Responses (Date)"
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
                    text: "HTTP Requests/Responses (URL)"
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

/**
 * Initialize Prometheus charts by discovering all available metrics and rendering them.
 * Since Spring Boot's /actuator/prometheus doesn't have a query API, we manually
 * fetch and parse the metrics text format.
 */
async function initializePrometheusCharts() {
    prometheusEndpointUrl = CasActuatorEndpoints.prometheus();

    if (!prometheusEndpointUrl) {
        return;
    }

    // Check if the prometheus endpoint is accessible and fetch metrics
    let metricsText;
    try {
        const response = await fetch(prometheusEndpointUrl);
        if (!response.ok) {
            displayBanner(response);
            return;
        }
        metricsText = await response.text();
    } catch (error) {
        displayBanner(error);
        return;
    }

    prometheusRefreshInterval = palantirSettings().refreshInterval;

    // Parse metrics and render all charts
    const parsedMetrics = parsePrometheusMetrics(metricsText);
    prometheusAvailableMetrics = Object.keys(parsedMetrics).sort();

    // Create charts for all metrics
    for (const metricName of prometheusAvailableMetrics) {
        createPrometheusChart(metricName, parsedMetrics[metricName]);
    }

    // Start periodic refresh
    startPrometheusRefresh();
}

/**
 * Start periodic refresh of all Prometheus charts
 */
function startPrometheusRefresh() {
    if (prometheusRefreshTimer) {
        clearInterval(prometheusRefreshTimer);
    }

    // Set up periodic refresh
    prometheusRefreshTimer = setInterval(fetchAndUpdateAllCharts, prometheusRefreshInterval);
}

/**
 * Fetch metrics and update all charts
 */
async function fetchAndUpdateAllCharts() {
    if (Object.keys(prometheusCharts).length === 0) {
        return;
    }

    try {
        const response = await fetch(prometheusEndpointUrl);
        if (!response.ok) {
            return;
        }

        const metricsText = await response.text();
        const parsedMetrics = parsePrometheusMetrics(metricsText);

        // Update each chart with new data
        for (const metricName of Object.keys(prometheusCharts)) {
            updatePrometheusChart(metricName, parsedMetrics[metricName]);
        }
    } catch (error) {
        // Silently fail on refresh errors to avoid spamming the user
    }
}

/**
 * Parse Prometheus text format into metric values
 * @param {string} text - The Prometheus format text
 * @returns {Object} Parsed metrics with their values
 */
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

/**
 * Create a Prometheus chart for the specified metric
 * @param {string} metricName - The metric name
 * @param {Array} metricData - Array of {labels, value} objects
 */
function createPrometheusChart(metricName, metricData) {
    const chartsRow = document.getElementById("prometheusChartsRow");
    if (!chartsRow || !metricData || metricData.length === 0) return;

    // Create a unique ID for this chart
    const chartId = `prometheusChart_${metricName.replace(/[^a-zA-Z0-9]/g, "_")}`;

    // Create the chart container
    const chartCell = document.createElement("div");
    chartCell.className = "mdc-layout-grid__cell mdc-layout-grid__cell--span-4";
    chartCell.id = `${chartId}_container`;
    chartCell.style.cursor = "pointer";
    chartCell.title = "Click to enlarge";

    // Create header with title
    const header = document.createElement("div");
    header.className = "mb-1";
    header.innerHTML = `<h6 class="mb-0" title="${metricName}" style="font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${metricName}</h6>`;

    // Create canvas
    const canvas = document.createElement("canvas");
    canvas.id = chartId;
    canvas.style.maxHeight = "150px";

    chartCell.appendChild(header);
    chartCell.appendChild(canvas);
    chartsRow.appendChild(chartCell);

    // Prepare data for the chart
    const labels = metricData.map(item => formatSeriesLabel(item.labels));
    const values = metricData.map(item => item.value);
    const colors = generateColors(metricData.length);

    // Store chart data for dialog display
    const chartConfig = {
        labels: labels,
        values: values,
        colors: colors,
        metricName: metricName
    };

    // Create the chart
    const chart = new Chart(canvas.getContext("2d"), {
        type: "line",
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

    // Add click handler to show maximized chart
    chartCell.addEventListener("click", () => {
        showMaximizedChart(chartConfig);
    });

    prometheusCharts[metricName] = chart;
}

/**
 * Show a maximized view of a Prometheus chart in a jQuery UI dialog
 * @param {Object} chartConfig - The chart configuration with labels, values, colors, and metricName
 */
function showMaximizedChart(chartConfig) {
    // Remove existing dialog if present
    $("#prometheusChartDialog").remove();

    // Create dialog container
    const dialogHtml = `
        <div id="prometheusChartDialog" title="${chartConfig.metricName}">
            <canvas id="prometheusChartDialogCanvas" style="width: 100%; height: 100%;"></canvas>
        </div>
    `;
    $("body").append(dialogHtml);

    // Initialize jQuery UI dialog
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
            // Create the maximized chart after dialog opens
            const canvas = document.getElementById("prometheusChartDialogCanvas");
            if (canvas) {
                window.prometheusDialogChart = new Chart(canvas.getContext("2d"), {
                    type: "line",
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

/**
 * Update a Prometheus chart with new data
 * @param {string} metricName - The metric name
 * @param {Array} metricData - Array of {labels, value} objects
 */
function updatePrometheusChart(metricName, metricData) {
    const chart = prometheusCharts[metricName];
    if (!chart || !metricData) return;

    const labels = metricData.map(item => formatSeriesLabel(item.labels));
    const values = metricData.map(item => item.value);

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;
    chart.update("none");
}

/**
 * Format a series label (from Prometheus labels) for display
 */
function formatSeriesLabel(labelsString) {
    if (!labelsString || labelsString === "") {
        return "value";
    }

    // Extract key parts from labels like {area="heap",id="G1 Eden Space"}
    const parts = [];
    const matches = labelsString.matchAll(/(\w+)="([^"]+)"/g);
    for (const match of matches) {
        parts.push(match[2]);
    }

    if (parts.length === 0) {
        return labelsString;
    }

    const label = parts.join("/");
    return label.length > 20 ? label.substring(0, 17) + "..." : label;
}

/**
 * Generate colors for chart bars
 */
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


