/**
 * The System Metrics page: a board of pinned metrics that refresh on their own, and an explorer that shows one metric
 * with its values in readable units, how it changes while the page is open, and how it breaks down by tag.
 *
 * The metrics endpoint answers the current value of each statistic. Trends are sampled by the page at the dashboard's
 * refresh interval, rates are worked out from successive counts, and breakdowns ask for the metric once per tag value.
 */
class PalantirMetrics {
    static STORAGE_KEY = "PalantirPinnedMetrics";
    static MAX_SAMPLES = 60;
    static MAX_BREAKDOWN_VALUES = 15;

    static DEFAULT_PINS = [
        {name: "http.server.requests", tags: []},
        {name: "process.cpu.usage", tags: []},
        {name: "jvm.memory.used", tags: ["area:heap"]},
        {name: "jvm.threads.live", tags: []},
        {name: "jvm.gc.pause", tags: []},
        {name: "tomcat.sessions.active.current", tags: []},
        {name: "system.cpu.usage", tags: []},
        {name: "hikaricp.connections.active", tags: []}
    ];

    static FAMILIES = {
        cas: "CAS", http: "HTTP", jvm: "JVM", process: "Process & system", system: "Process & system",
        disk: "Process & system", tomcat: "Tomcat", hikaricp: "Connection pools", jdbc: "Connection pools",
        logback: "Logging", log4j2: "Logging", executor: "Executors", application: "Application", spring: "Spring",
        cache: "Caches", hazelcast: "Hazelcast", mongodb: "MongoDB", lettuce: "Redis", tasks: "Scheduled tasks"
    };

    static STATISTICS = {
        COUNT: "Count", TOTAL_TIME: "Total time", TOTAL: "Total", MAX: "Max", VALUE: "Value",
        ACTIVE_TASKS: "Active", DURATION: "Duration", UNKNOWN: "Value"
    };

    static names = [];
    static pins = [];
    static selected = null;
    static filters = [];
    static breakdownTag = null;
    static samples = new Map();
    static details = new Map();
    static chart = null;
    static view = "board";
    static pending = false;
    static again = false;
    static timer = null;
    static initialized = false;

    /**
     * Load the metric names and draw the page, once; later calls only refresh what is shown.
     *
     * @returns {Promise<void>} resolved once the names are loaded
     */
    static async initialize() {
        if (!CasActuatorEndpoints.metrics()) {
            return;
        }
        if (!PalantirMetrics.initialized) {
            PalantirMetrics.initialized = true;
            PalantirMetrics.bind();
            PalantirMetrics.timer = PalantirPolling.every("metrics", () => PalantirMetrics.refresh(), {when: () => PalantirMetrics.visible()});
        }
        try {
            const response = await $.get(CasActuatorEndpoints.metrics());
            PalantirMetrics.names = [...(response.names ?? [])].sort();
        } catch (xhr) {
            console.warn("Metric names are not available at the moment:", xhr?.statusText);
            PalantirMetrics.names = [];
        }
        PalantirMetrics.pins = PalantirMetrics.loadPins();
        PalantirMetrics.renderList();
        PalantirMetrics.renderBoard();
        if (PalantirMetrics.visible()) {
            await PalantirMetrics.refresh();
        }
    }

    static bind() {
        $("#systemmetrics-tab [data-metrics-view]").off("click").on("click", event =>
            PalantirMetrics.show(event.currentTarget.dataset.metricsView));
        $("#system-tabs").off("tabsactivate.metrics").on("tabsactivate.metrics", (event, ui) => {
            if (ui.newPanel.is("#systemmetrics-tab")) {
                PalantirMetrics.refresh();
            }
        });
        $("#metricsSearch").off("input").on("input", () => PalantirMetrics.renderList());
        $("#metricsResetBoard").off("click").on("click", () => {
            PalantirMetrics.savePins(PalantirMetrics.defaultPins());
            PalantirMetrics.renderBoard();
            PalantirMetrics.refresh();
        });
        $("#metricsAddTile").off("click").on("click", () => {
            PalantirMetrics.show("explorer");
            $("#metricsSearch").trigger("focus");
        });
        $("#metricsDetailPin").off("click").on("click", () => PalantirMetrics.togglePin());
        $("#metricsList").off("click").on("click", "[data-metric]", event =>
            PalantirMetrics.select(event.currentTarget.dataset.metric, []));
        $("#metricsTiles").off("click keydown")
            .on("click", "[data-metric-remove]", event => {
                event.stopPropagation();
                const index = Number(event.currentTarget.dataset.metricRemove);
                PalantirMetrics.savePins(PalantirMetrics.pins.filter((pin, position) => position !== index));
                PalantirMetrics.renderBoard();
                PalantirMetrics.refresh();
            })
            .on("click", "[data-metric-tile]", event => {
                const pin = PalantirMetrics.pins[Number(event.currentTarget.dataset.metricTile)];
                if (pin) {
                    PalantirMetrics.show("explorer");
                    PalantirMetrics.select(pin.name, pin.tags);
                }
            })
            .on("click", "[data-metric-add]", () => $("#metricsAddTile").trigger("click"));
        $("#metricsDetailFilters").off("click").on("click", "[data-metric-filter-remove]", event => {
            const tag = event.currentTarget.dataset.metricFilterRemove;
            PalantirMetrics.select(PalantirMetrics.selected, PalantirMetrics.filters.filter(filter => filter !== tag));
        });
        $("#metricsBreakdownTags").off("click").on("click", "[data-metric-breakdown]", event => {
            PalantirMetrics.breakdownTag = event.currentTarget.dataset.metricBreakdown;
            PalantirMetrics.renderBreakdownTags(PalantirMetrics.details.get(PalantirMetrics.key(PalantirMetrics.selected, PalantirMetrics.filters)));
            PalantirMetrics.loadBreakdown();
        });
        $("#metricsBreakdown").off("click").on("click", "[data-metric-filter-add]", event => {
            const tag = event.currentTarget.dataset.metricFilterAdd;
            PalantirMetrics.select(PalantirMetrics.selected, [...PalantirMetrics.filters, tag]);
        });
    }

    /**
     * Whether the metrics page is on screen, so that refreshing it is worth the requests.
     *
     * @returns {boolean} true when the page is shown in a visible window
     */
    static visible() {
        const tab = document.getElementById("systemmetrics-tab");
        return document.visibilityState === "visible" && tab !== null && tab.offsetParent !== null;
    }

    static show(view) {
        PalantirMetrics.view = view === "explorer" ? "explorer" : "board";
        $("#systemmetrics-tab [data-metrics-view]").each((index, button) =>
            button.setAttribute("aria-pressed", String(button.dataset.metricsView === PalantirMetrics.view)));
        $("#metricsBoard").prop("hidden", PalantirMetrics.view !== "board");
        $("#metricsExplorer").prop("hidden", PalantirMetrics.view !== "explorer");
        $("#systemmetrics-tab [data-metrics-board-only]").prop("hidden", PalantirMetrics.view !== "board");
        PalantirMetrics.refresh();
    }

    /**
     * Read what is shown again: every tile on the board, or the selected metric in the explorer. A refresh that is
     * still waiting for the server is not started twice.
     *
     * @returns {Promise<void>} resolved once the values are drawn
     */
    static async refresh() {
        if (PalantirMetrics.pending) {
            PalantirMetrics.again = true;
            return;
        }
        PalantirMetrics.pending = true;
        PalantirMetrics.again = false;
        try {
            if (PalantirMetrics.view === "board") {
                await Promise.all(PalantirMetrics.pins.map((pin, index) => PalantirMetrics.refreshTile(pin, index)));
            } else if (PalantirMetrics.selected) {
                await PalantirMetrics.refreshDetail();
            }
            $("#metricsStatus").text(`Refreshed every ${Math.round(palantirSettings().refreshInterval / 1000)} s · ${new Date().toLocaleTimeString()}`);
        } finally {
            PalantirMetrics.pending = false;
        }
        if (PalantirMetrics.again) {
            await PalantirMetrics.refresh();
        }
    }

    static loadPins() {
        let stored;
        try {
            stored = JSON.parse(localStorage.getItem(PalantirMetrics.STORAGE_KEY));
        } catch (error) {
            stored = null;
        }
        const pins = Array.isArray(stored)
            ? stored.filter(pin => typeof pin?.name === "string").map(pin => ({name: pin.name, tags: Array.isArray(pin.tags) ? pin.tags.map(String) : []}))
            : PalantirMetrics.defaultPins();
        return pins.filter(pin => PalantirMetrics.names.length === 0 || PalantirMetrics.names.includes(pin.name));
    }

    static defaultPins() {
        return PalantirMetrics.DEFAULT_PINS
            .filter(pin => PalantirMetrics.names.includes(pin.name))
            .slice(0, 7)
            .map(pin => ({name: pin.name, tags: [...pin.tags]}));
    }

    static savePins(pins) {
        PalantirMetrics.pins = pins;
        try {
            localStorage.setItem(PalantirMetrics.STORAGE_KEY, JSON.stringify(pins));
        } catch (error) {
            console.debug("Pinned metrics are kept for this visit only", error);
        }
    }

    static key(name, tags) {
        return `${name}|${[...tags].sort().join(",")}`;
    }

    static url(name, tags) {
        const query = tags.map(tag => `tag=${encodeURIComponent(tag)}`).join("&");
        return `${CasActuatorEndpoints.metrics()}/${encodeURIComponent(name)}${query ? `?${query}` : ""}`;
    }

    static async read(name, tags) {
        const response = await $.ajax({url: PalantirMetrics.url(name, tags), dataType: "json"});
        PalantirMetrics.details.set(PalantirMetrics.key(name, tags), response);
        return response;
    }

    static statistic(response, name) {
        return response?.measurements?.find(measurement => measurement.statistic === name)?.value;
    }

    /**
     * Record a sample of a metric and answer the series it feeds: the rate per minute for anything with a count,
     * otherwise its value.
     *
     * @param key the metric and its tags
     * @param response the metric as the endpoint answered it
     * @returns {{rate: boolean, points: Array<{time: number, value: number}>}} the samples so far
     */
    static sample(key, response) {
        const count = PalantirMetrics.statistic(response, "COUNT");
        const rate = count !== undefined;
        const series = PalantirMetrics.samples.get(key) ?? {rate, points: [], last: null};
        const now = Date.now();
        if (rate) {
            if (series.last !== null && now > series.last.time && count >= series.last.count) {
                series.points.push({time: now, value: (count - series.last.count) / ((now - series.last.time) / 60000)});
            }
            series.last = {time: now, count};
        } else {
            const value = PalantirMetrics.statistic(response, "VALUE") ?? response?.measurements?.[0]?.value;
            if (Number.isFinite(value)) {
                series.points.push({time: now, value});
            }
        }
        series.points = series.points.slice(-PalantirMetrics.MAX_SAMPLES);
        PalantirMetrics.samples.set(key, series);
        return series;
    }

    static percentage(name, unit) {
        return unit === "percent" || /\.usage$/.test(name);
    }

    /**
     * A value in the unit people read it in: durations in µs, ms, s, minutes or hours; sizes in KB, MB or GB;
     * usage as a percentage; counts with thousands separators.
     *
     * @param value the raw value
     * @param unit the base unit the endpoint names
     * @param name the metric name
     * @returns {string} the value to show
     */
    static format(value, unit, name = "") {
        if (!Number.isFinite(value)) {
            return "—";
        }
        if (PalantirMetrics.percentage(name, unit)) {
            return `${(value * 100).toFixed(value < 0.1 ? 1 : 0)}%`;
        }
        if (unit === "seconds") {
            const absolute = Math.abs(value);
            if (absolute === 0) {
                return "0 s";
            }
            if (absolute < 0.001) {
                return `${(value * 1e6).toFixed(0)} µs`;
            }
            if (absolute < 1) {
                return `${(value * 1000).toFixed(absolute < 0.01 ? 2 : 1)} ms`;
            }
            if (absolute < 60) {
                return `${value.toFixed(2)} s`;
            }
            if (absolute < 3600) {
                return `${Math.floor(value / 60)} m ${Math.round(value % 60)} s`;
            }
            return `${Math.floor(value / 3600)} h ${Math.round((value % 3600) / 60)} m`;
        }
        if (unit === "bytes") {
            const sizes = ["B", "KB", "MB", "GB", "TB"];
            let size = Math.abs(value);
            let index = 0;
            while (size >= 1024 && index < sizes.length - 1) {
                size /= 1024;
                index++;
            }
            return `${(Math.sign(value) * size).toFixed(index === 0 ? 0 : size < 10 ? 2 : 1)} ${sizes[index]}`;
        }
        return new Intl.NumberFormat(undefined, {maximumFractionDigits: Math.abs(value) < 10 ? 2 : 0}).format(value);
    }

    static unitLabel(name, unit) {
        if (PalantirMetrics.percentage(name, unit)) {
            return "%";
        }
        return unit ?? "";
    }

    static statisticUnit(statistic, unit) {
        return ["COUNT", "ACTIVE_TASKS"].includes(statistic) ? null : unit;
    }

    /**
     * The statistics of a metric to show, with the average added where the metric has a count and a total.
     *
     * @param response the metric as the endpoint answered it
     * @returns {Array<{label: string, value: string, note: string, derived: boolean}>} the statistics
     */
    static stats(response) {
        const name = response?.name ?? "";
        const unit = response?.baseUnit ?? null;
        const stats = (response?.measurements ?? []).map(measurement => ({
            label: PalantirMetrics.STATISTICS[measurement.statistic] ?? camelcaseToTitleCase(String(measurement.statistic).toLowerCase()),
            value: PalantirMetrics.format(measurement.value, PalantirMetrics.statisticUnit(measurement.statistic, unit),
                ["COUNT", "ACTIVE_TASKS"].includes(measurement.statistic) ? "" : name),
            note: measurement.statistic === "COUNT" ? "since CAS started" : measurement.statistic === "MAX" ? "recent maximum" : "",
            derived: false
        }));
        const count = PalantirMetrics.statistic(response, "COUNT");
        const total = PalantirMetrics.statistic(response, "TOTAL_TIME") ?? PalantirMetrics.statistic(response, "TOTAL");
        if (Number.isFinite(count) && count > 0 && Number.isFinite(total)) {
            stats.push({label: "Average", value: PalantirMetrics.format(total / count, unit, name), note: "total ÷ count", derived: true});
        }
        return stats;
    }

    static family(name) {
        const prefix = String(name).split(".")[0];
        return PalantirMetrics.FAMILIES[prefix] ?? camelcaseToTitleCase(prefix);
    }

    static renderList() {
        const filter = String($("#metricsSearch").val() ?? "").trim().toLowerCase();
        const groups = new Map();
        for (const name of PalantirMetrics.names) {
            if (filter && !name.toLowerCase().includes(filter)) {
                continue;
            }
            const family = PalantirMetrics.family(name);
            groups.set(family, [...(groups.get(family) ?? []), name]);
        }
        const list = $("#metricsList").empty();
        if (groups.size === 0) {
            list.append($("<p>", {class: "palantir-metrics__muted", text: PalantirMetrics.names.length === 0
                ? "No metrics are available."
                : `No metric name contains "${filter}".`}));
            return;
        }
        for (const [family, names] of [...groups.entries()].sort((left, right) => left[0].localeCompare(right[0]))) {
            const group = $("<div>", {class: "palantir-metrics__group"});
            group.append($("<div>", {class: "palantir-metrics__group-head"})
                .append($("<span>", {text: family}))
                .append($("<span>", {text: names.length})));
            for (const name of names) {
                group.append($("<button>", {
                    type: "button",
                    class: `palantir-metrics__item${name === PalantirMetrics.selected ? " is-selected" : ""}`,
                    "data-metric": name,
                    "aria-current": name === PalantirMetrics.selected ? "true" : null,
                    title: name,
                    text: name
                }));
            }
            list.append(group);
        }
    }

    static renderBoard() {
        const tiles = $("#metricsTiles").empty();
        PalantirMetrics.pins.forEach((pin, index) => {
            const tile = $("<div>", {class: "palantir-metrics__tile", "data-metric-tile": index});
            const head = $("<div>", {class: "palantir-metrics__tile-head"})
                .append($("<button>", {
                    type: "button",
                    class: "palantir-metrics__tile-name",
                    text: pin.name,
                    title: `Open ${pin.name} in the explorer`
                }))
                .append($("<button>", {
                    type: "button",
                    class: "palantir-icon-button palantir-metrics__tile-remove",
                    "data-metric-remove": index,
                    "aria-label": `Unpin ${pin.name}`,
                    title: "Unpin"
                }).append($("<i>", {class: "mdi mdi-pin-off-outline", "aria-hidden": "true"})));
            tile.append(head);
            if (pin.tags.length > 0) {
                const chips = $("<div>", {class: "palantir-metrics__tile-tags"});
                pin.tags.forEach(tag => chips.append($("<span>", {class: "palantir-metrics__chip", text: tag})));
                tile.append(chips);
            }
            tile.append($("<div>", {class: "palantir-metrics__tile-value"})
                .append($("<strong>", {class: "palantir-metrics__tile-number", text: "…"}))
                .append($("<span>", {class: "palantir-metrics__spark"})));
            tile.append($("<div>", {class: "palantir-metrics__tile-note", text: "Loading..."}));
            tiles.append(tile);
        });
        tiles.append($("<button>", {type: "button", class: "palantir-metrics__tile palantir-metrics__tile--add", "data-metric-add": "true"})
            .append($("<i>", {class: "mdi mdi-pin-outline", "aria-hidden": "true"}))
            .append(document.createTextNode(PalantirMetrics.pins.length === 0 ? " Pin the metrics you watch from the explorer" : " Pin a metric")));
    }

    /**
     * The level a value has reached: usage above 75% asks for attention and above 90% is critical.
     *
     * @param ratio the share of the whole, between 0 and 1, or undefined when there is no whole
     * @returns {string|null} "warn", "critical" or null
     */
    static level(ratio) {
        if (!Number.isFinite(ratio)) {
            return null;
        }
        return ratio >= 0.9 ? "critical" : ratio >= 0.75 ? "warn" : null;
    }

    static async refreshTile(pin, index) {
        const tile = $(`#metricsTiles [data-metric-tile="${index}"]`);
        try {
            const response = await PalantirMetrics.read(pin.name, pin.tags);
            const unit = response.baseUnit ?? null;
            const series = PalantirMetrics.sample(PalantirMetrics.key(pin.name, pin.tags), response);
            const count = PalantirMetrics.statistic(response, "COUNT");
            const value = PalantirMetrics.statistic(response, "VALUE") ?? response.measurements?.[0]?.value;
            let number;
            let note;
            let ratio;
            if (series.rate) {
                const latest = series.points.at(-1)?.value;
                number = Number.isFinite(latest) ? `${PalantirMetrics.format(latest, null)}/min` : PalantirMetrics.format(count, null);
                const notes = [`${PalantirMetrics.format(count, null)} total`];
                const total = PalantirMetrics.statistic(response, "TOTAL_TIME");
                if (Number.isFinite(total) && count > 0) {
                    notes.push(`avg ${PalantirMetrics.format(total / count, unit)}`);
                }
                const max = PalantirMetrics.statistic(response, "MAX");
                if (Number.isFinite(max)) {
                    notes.push(`max ${PalantirMetrics.format(max, unit, pin.name)}`);
                }
                note = notes.join(" · ");
            } else {
                number = PalantirMetrics.format(value, unit, pin.name);
                note = response.description ?? PalantirMetrics.unitLabel(pin.name, unit);
                if (PalantirMetrics.percentage(pin.name, unit)) {
                    ratio = value;
                } else if (pin.name === "jvm.memory.used" && PalantirMetrics.names.includes("jvm.memory.max")) {
                    const max = PalantirMetrics.statistic(await PalantirMetrics.read("jvm.memory.max", pin.tags)
                        .catch(() => null), "VALUE");
                    if (Number.isFinite(max) && max > 0) {
                        ratio = value / max;
                        number = `${Math.round(ratio * 100)}%`;
                        note = `${PalantirMetrics.format(value, unit)} of ${PalantirMetrics.format(max, unit)}`;
                    }
                }
            }
            const level = PalantirMetrics.level(ratio);
            tile.removeClass("is-warn is-critical is-unavailable").addClass(level ? `is-${level}` : "");
            tile.find(".palantir-metrics__tile-number").text(number);
            tile.find(".palantir-metrics__tile-note").text(note);
            tile.find(".palantir-metrics__spark").html(PalantirMetrics.sparkline(series.points.map(point => point.value)));
            tile.attr("title", series.rate ? "Rate per minute while this page is open" : "");
        } catch (xhr) {
            tile.removeClass("is-warn is-critical").addClass("is-unavailable");
            tile.find(".palantir-metrics__tile-number").text("—");
            tile.find(".palantir-metrics__tile-note").text(xhr?.status === 404
                ? "This metric is not available on this server."
                : "Could not be read just now; trying again at the next refresh.");
        }
    }

    /**
     * A small trend line, drawn to the scale of its own samples.
     *
     * @param values the samples, oldest first
     * @returns {string} the SVG markup, or an empty string until there are two samples
     */
    static sparkline(values) {
        if (values.length < 2) {
            return "";
        }
        const width = 120;
        const height = 34;
        const max = Math.max(...values);
        const min = Math.min(...values);
        const range = max - min || 1;
        const points = values.map((value, index) => [
            2 + index * (width - 4) / (values.length - 1),
            height - 4 - (value - min) / range * (height - 8)
        ]);
        const line = points.map(([x, y]) => `${x.toFixed(1)},${y.toFixed(1)}`).join(" ");
        const [lastX, lastY] = points.at(-1);
        return `<svg viewBox="0 0 ${width} ${height}" width="${width}" height="${height}" aria-hidden="true" focusable="false">`
            + `<polygon class="palantir-metrics__spark-area" points="2,${height} ${line} ${lastX.toFixed(1)},${height}"/>`
            + `<polyline class="palantir-metrics__spark-line" points="${line}"/>`
            + `<circle class="palantir-metrics__spark-dot" cx="${lastX.toFixed(1)}" cy="${lastY.toFixed(1)}" r="2.6"/></svg>`;
    }

    /**
     * Open a metric in the explorer, narrowed to the given tag values.
     *
     * @param name the metric name
     * @param tags the tag filters, each as key:value
     * @returns {Promise<void>} resolved once it is drawn
     */
    static async select(name, tags) {
        const changed = name !== PalantirMetrics.selected;
        PalantirMetrics.selected = name;
        PalantirMetrics.filters = [...new Set(tags)];
        if (changed) {
            PalantirMetrics.breakdownTag = null;
        }
        PalantirMetrics.renderList();
        $("#metricsDetailEmpty").prop("hidden", true);
        $("#metricsDetailBody").prop("hidden", false);
        $("#metricsDetailName").text(name);
        PalantirMetrics.renderFilters();
        PalantirMetrics.renderPin();
        $("#metricsBreakdown").empty();
        await PalantirMetrics.refreshDetail();
        PalantirMetrics.renderBreakdownTags(PalantirMetrics.details.get(PalantirMetrics.key(name, PalantirMetrics.filters)));
        PalantirMetrics.loadBreakdown();
    }

    static pinIndex() {
        const key = PalantirMetrics.key(PalantirMetrics.selected, PalantirMetrics.filters);
        return PalantirMetrics.pins.findIndex(pin => PalantirMetrics.key(pin.name, pin.tags) === key);
    }

    static renderPin() {
        const pinned = PalantirMetrics.pinIndex() >= 0;
        $("#metricsDetailPin").empty()
            .append($("<i>", {class: `mdi ${pinned ? "mdi-pin-off-outline" : "mdi-pin-outline"}`, "aria-hidden": "true"}))
            .append(document.createTextNode(pinned ? "Unpin from Board" : "Pin to Board"))
            .attr("aria-pressed", String(pinned));
    }

    static togglePin() {
        const index = PalantirMetrics.pinIndex();
        if (index >= 0) {
            PalantirMetrics.savePins(PalantirMetrics.pins.filter((pin, position) => position !== index));
            notyf.success(`${PalantirMetrics.selected} is no longer on the board.`);
        } else {
            PalantirMetrics.savePins([...PalantirMetrics.pins, {name: PalantirMetrics.selected, tags: [...PalantirMetrics.filters]}]);
            notyf.success(`${PalantirMetrics.selected} is pinned to the board.`);
        }
        PalantirMetrics.renderBoard();
        PalantirMetrics.renderPin();
    }

    static renderFilters() {
        const filters = $("#metricsDetailFilters").empty();
        if (PalantirMetrics.filters.length === 0) {
            filters.append($("<span>", {class: "palantir-metrics__muted", text: "All values. Click a bar in the breakdown to narrow it down."}));
            return;
        }
        filters.append($("<span>", {class: "palantir-metrics__muted", text: "Only"}));
        for (const tag of PalantirMetrics.filters) {
            filters.append($("<button>", {
                type: "button",
                class: "palantir-metrics__chip is-active",
                "data-metric-filter-remove": tag,
                "aria-label": `Remove the filter ${tag}`
            }).append(document.createTextNode(tag)).append($("<i>", {class: "mdi mdi-close", "aria-hidden": "true"})));
        }
    }

    static async refreshDetail() {
        const name = PalantirMetrics.selected;
        const tags = PalantirMetrics.filters;
        const key = PalantirMetrics.key(name, tags);
        let response;
        try {
            response = await PalantirMetrics.read(name, tags);
        } catch (xhr) {
            $("#metricsDetailDescription").text(xhr?.status === 404
                ? "Nothing is recorded for this metric with these filters yet."
                : "This metric could not be read just now; trying again at the next refresh.");
            $("#metricsDetailStats").empty();
            return;
        }
        if (name !== PalantirMetrics.selected || key !== PalantirMetrics.key(PalantirMetrics.selected, PalantirMetrics.filters)) {
            return;
        }
        const unit = response.baseUnit ?? null;
        const label = PalantirMetrics.unitLabel(name, unit);
        $("#metricsDetailUnit").text(label).prop("hidden", label === "");
        $("#metricsDetailDescription").text(response.description ?? "No description is available for this metric.");
        const stats = $("#metricsDetailStats").empty();
        for (const stat of PalantirMetrics.stats(response)) {
            stats.append($("<div>", {class: `palantir-metrics__stat${stat.derived ? " is-derived" : ""}`})
                .append($("<small>", {text: stat.label}))
                .append($("<strong>", {text: stat.value}))
                .append($("<em>", {text: stat.note})));
        }
        const series = PalantirMetrics.sample(key, response);
        $("#metricsDetailLive").text(`Live · every ${Math.round(palantirSettings().refreshInterval / 1000)} s`);
        $("#metricsTrendTitle").text(series.rate ? "Per minute, while this page is open" : "Value, while this page is open");
        PalantirMetrics.renderTrend(series, unit, name);
    }

    static renderTrend(series, unit, name) {
        const points = series.points;
        $("#metricsTrendSpan").text(points.length < 2
            ? "collecting samples..."
            : `${Math.max(1, Math.round((points.at(-1).time - points[0].time) / 60000))} min`);
        const canvas = document.getElementById("metricsTrendChart");
        if (!canvas || typeof Chart === "undefined") {
            return;
        }
        const styles = getComputedStyle(document.documentElement);
        const accent = styles.getPropertyValue("--palantir-accent").trim() || "#018077";
        const fill = styles.getPropertyValue("--palantir-accent-soft").trim() || "rgba(1, 128, 119, .15)";
        const labels = points.map(point => new Date(point.time).toLocaleTimeString());
        const data = points.map(point => point.value);
        const tickUnit = series.rate ? null : unit;
        if (!PalantirMetrics.chart) {
            PalantirMetrics.chart = new Chart(canvas, {
                type: "line",
                data: {labels, datasets: [{data, borderColor: accent, backgroundColor: fill, fill: true, tension: 0.3, pointRadius: 0, borderWidth: 2}]},
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    plugins: {legend: {display: false}},
                    scales: {
                        x: {ticks: {maxTicksLimit: 6}, grid: {display: false}},
                        y: {beginAtZero: true, ticks: {maxTicksLimit: 5}}
                    }
                }
            });
        }
        const chart = PalantirMetrics.chart;
        chart.data.labels = labels;
        chart.data.datasets[0].data = data;
        chart.data.datasets[0].borderColor = accent;
        chart.data.datasets[0].backgroundColor = fill;
        chart.options.scales.y.ticks.callback = value => PalantirMetrics.format(value, tickUnit, series.rate ? "" : name);
        chart.options.plugins.tooltip = {callbacks: {label: context =>
            `${PalantirMetrics.format(context.parsed.y, tickUnit, series.rate ? "" : name)}${series.rate ? " per minute" : ""}`}};
        chart.update();
    }

    static renderBreakdownTags(response) {
        const used = new Set(PalantirMetrics.filters.map(filter => filter.split(":")[0]));
        const tags = (response?.availableTags ?? []).filter(tag => !used.has(tag.tag));
        if (!PalantirMetrics.breakdownTag || !tags.some(tag => tag.tag === PalantirMetrics.breakdownTag)) {
            PalantirMetrics.breakdownTag = tags[0]?.tag ?? null;
        }
        const group = $("#metricsBreakdownTags").empty();
        if (tags.length === 0) {
            group.append($("<span>", {class: "palantir-metrics__muted", text: "This metric has no tags to break it down by."}));
            return;
        }
        for (const tag of tags) {
            group.append($("<button>", {
                type: "button",
                class: `palantir-metrics__chip${tag.tag === PalantirMetrics.breakdownTag ? " is-active" : ""}`,
                "data-metric-breakdown": tag.tag,
                "aria-pressed": String(tag.tag === PalantirMetrics.breakdownTag),
                text: `${tag.tag} · ${tag.values.length}`
            }));
        }
    }

    /**
     * Ask for the selected metric once per value of the chosen tag and draw a bar for each, largest first. A value
     * that cannot be read shows as unavailable, after the others and out of the ranking, with a way to read it again.
     *
     * @returns {Promise<void>} resolved once the bars are drawn
     */
    static async loadBreakdown() {
        const name = PalantirMetrics.selected;
        const filters = [...PalantirMetrics.filters];
        const tagName = PalantirMetrics.breakdownTag;
        const response = PalantirMetrics.details.get(PalantirMetrics.key(name, filters));
        const tag = response?.availableTags?.find(entry => entry.tag === tagName);
        const bars = $("#metricsBreakdown").empty();
        if (!tag) {
            return;
        }
        const values = tag.values.slice(0, PalantirMetrics.MAX_BREAKDOWN_VALUES);
        const stale = () => name !== PalantirMetrics.selected || tagName !== PalantirMetrics.breakdownTag
            || filters.join() !== PalantirMetrics.filters.join();
        const read = async value => {
            try {
                const answer = await $.ajax({url: PalantirMetrics.url(name, [...filters, `${tagName}:${value}`]), dataType: "json"});
                const amount = PalantirMetrics.statistic(answer, "COUNT") ?? PalantirMetrics.statistic(answer, "VALUE")
                    ?? answer.measurements?.[0]?.value;
                return {value, amount: Number.isFinite(amount) ? amount : 0, unit: PalantirMetrics.statistic(answer, "COUNT") === undefined ? answer.baseUnit : null};
            } catch (xhr) {
                return {value, amount: null, unit: null};
            }
        };
        bars.append($("<p>", {class: "palantir-metrics__muted", text: "Loading..."}));
        const results = await Promise.all(values.map(read));
        if (stale()) {
            return;
        }
        const draw = () => {
            bars.empty();
            const measured = results.filter(result => result.amount !== null).sort((left, right) => right.amount - left.amount);
            const max = Math.max(...measured.map(result => result.amount), 0);
            measured.forEach(result => {
                const share = max > 0 ? result.amount / max : 0;
                const failure = tagName === "outcome" && /ERROR/.test(result.value) || tagName === "status" && /^5/.test(result.value);
                bars.append($("<button>", {
                    type: "button",
                    class: `palantir-metrics__bar${failure ? " is-failure" : ""}`,
                    "data-metric-filter-add": `${tagName}:${result.value}`,
                    title: `Show only ${tagName} ${result.value}`
                })
                    .append($("<span>", {class: "palantir-metrics__bar-label", text: result.value}))
                    .append($("<span>", {class: "palantir-metrics__bar-track"}).append($("<i>", {style: `width: ${Math.max(share * 100, result.amount > 0 ? 1 : 0)}%`})))
                    .append($("<span>", {class: "palantir-metrics__bar-value", text: PalantirMetrics.format(result.amount, result.unit, result.unit ? name : "")})));
            });
            results.filter(result => result.amount === null).forEach(result => {
                const retry = $("<button>", {
                    type: "button",
                    class: "palantir-metrics__bar-retry",
                    title: `Read ${tagName} ${result.value} again`,
                    text: "Retry"
                }).on("click", async () => {
                    retry.prop("disabled", true);
                    const again = await read(result.value);
                    if (!stale()) {
                        results[results.indexOf(result)] = again;
                        draw();
                    }
                });
                bars.append($("<div>", {class: "palantir-metrics__bar is-unavailable"})
                    .append($("<span>", {class: "palantir-metrics__bar-label", text: result.value}))
                    .append($("<span>", {class: "palantir-metrics__bar-unavailable", text: "Unavailable"}))
                    .append(retry));
            });
            if (tag.values.length > values.length) {
                bars.append($("<p>", {class: "palantir-metrics__muted", text: `Showing the first ${values.length} of ${tag.values.length} values.`}));
            }
        };
        draw();
    }
}
