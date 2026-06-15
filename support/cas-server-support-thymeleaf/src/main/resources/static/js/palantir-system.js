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
                        .replace(PalantirDashboardConfiguration.casServerPrefix(), "")
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
        $("#systemLiveMemoryUpdated").text(`Updated ${new Date().toLocaleTimeString()}`);
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

            const nestedCount = allEvents.filter(e => e.startupStep.parentId && e.startupStep.parentId !== 0).length;
            $("#startupTimelineSummary").text(`${allEvents.length} steps · ${nestedCount} nested`);

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

    const storedTabs = localStorage.getItem("ActiveTabs");
    if (storedTabs) {
        try {
            const activeTabs = JSON.parse(storedTabs);
            const startupTabIndex = $("#system-tabs ul li a[href='#casstartup-tab']").parent().index();
            if (activeTabs["system-tabs"] === startupTabIndex) {
                await configureStartupTimeline();
            }
        } catch (e) { /* ignore */ }
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
