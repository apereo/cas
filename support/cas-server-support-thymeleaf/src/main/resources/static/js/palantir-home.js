/**
 * The home page: widgets drawn from one summary of the server, asked for at the refresh interval while the page is
 * shown. The server computes and keeps each part of the summary, so the page costs one request per refresh however
 * many widgets it shows. People can change the order of the widgets; that order is kept in the browser.
 */
class PalantirHome {
    static STORAGE_KEY = "PalantirHomeLayout";
    static MAX_SAMPLES = 30;

    static PAGES = {
        system: () => Tabs.SYSTEM.index,
        applications: () => Tabs.APPLICATIONS.index,
        sessions: () => Tabs.SSO_SESSIONS.index
    };

    static WIDGETS = [
        {id: "status", title: "Server", span: 4, needs: ["server"], render: summary => PalantirHome.renderStatus(summary)},
        {id: "health", title: "System Health", span: 2, needs: ["health"], page: "system", render: summary => PalantirHome.renderHealth(summary)},
        {id: "resources", title: "Resources", span: 2, needs: ["resources"], page: "system", render: summary => PalantirHome.renderResources(summary)},
        {id: "http", title: "HTTP Requests", span: 2, needs: ["traffic"], page: "system", available: summary => Boolean(summary.sections.traffic?.http), render: summary => PalantirHome.renderHttp(summary)},
        {id: "runtime", title: "Runtime", span: 2, needs: ["traffic"], page: "system", render: summary => PalantirHome.renderRuntime(summary)},
        {id: "sessions", title: "SSO Sessions", span: 2, needs: ["tickets"], page: "sessions", render: summary => PalantirHome.renderSessions(summary)},
        {id: "types", title: "Applications", span: 2, needs: ["applications"], page: "applications", render: summary => PalantirHome.renderTypes(summary)},
        {id: "changes", title: "Recent Application Changes", span: 4, needs: ["changes"], page: "applications", render: summary => PalantirHome.renderChanges(summary)}
    ];

    static summary = null;
    static sessions = [];
    static requests = [];
    static lastRequests = null;
    static pending = false;
    static fetched = 0;
    static FRESH_FOR = 5000;
    static PROGRESS_DELAY = 200;
    static timer = null;
    static dragged = null;
    static healthChart = null;

    static layout() {
        const ids = PalantirHome.WIDGETS.map(widget => widget.id);
        let stored;
        try {
            stored = JSON.parse(localStorage.getItem(PalantirHome.STORAGE_KEY));
        } catch (error) {
            stored = null;
        }
        const order = Array.isArray(stored?.order) ? stored.order.filter(id => ids.includes(id)) : [];
        ids.filter(id => !order.includes(id)).forEach(id => order.push(id));
        return {order};
    }

    static saveLayout(layout) {
        try {
            localStorage.setItem(PalantirHome.STORAGE_KEY, JSON.stringify(layout));
        } catch (error) {
            console.debug("The home layout is kept for this visit only", error);
        }
        PalantirHome.render();
    }

    static widget(id) {
        return PalantirHome.WIDGETS.find(widget => widget.id === id);
    }

    /**
     * Whether a widget has what it needs from the summary: every section it reads is there and could be computed.
     *
     * @param widget the widget
     * @returns {boolean} true when it can be drawn
     */
    static available(widget) {
        const sections = PalantirHome.summary?.sections ?? {};
        return widget.needs.every(name => sections[name] !== undefined && !sections[name].unavailable)
            && (typeof widget.available !== "function" || widget.available(PalantirHome.summary));
    }

    static visible() {
        const tab = document.getElementById(`attribute-tab-${Tabs.HOME.index}`);
        return document.visibilityState === "visible" && tab !== null && tab.offsetParent !== null;
    }

    /**
     * Called each time Home is shown: a summary older than a few seconds is fetched again right away, with the
     * progress bar showing, rather than at the next refresh.
     */
    static activated() {
        if (PalantirHome.summary !== null && Date.now() - PalantirHome.fetched > PalantirHome.FRESH_FOR) {
            PalantirHome.refresh({visible: true});
        }
    }

    static async initialize() {
        PalantirHome.bind();
        if (PalantirHome.timer === null) {
            PalantirHome.timer = setInterval(() => {
                if (PalantirHome.visible()) {
                    PalantirHome.refresh();
                }
            }, palantirSettings().refreshInterval);
        }
        await PalantirHome.refresh({visible: true});
    }

    static bind() {
        $("#homeGrid").off("click dragstart dragover dragleave drop dragend")
            .on("click", "[data-home-action]", event => PalantirHome.act(event.currentTarget))
            .on("click", "[data-home-page]", event => {
                const page = PalantirHome.PAGES[event.currentTarget.dataset.homePage];
                if (page) {
                    $(`nav.sidebar-navigation ul li[data-tab-index=${page()}]`).trigger("click");
                }
            })
            .on("dragstart", ".palantir-home__widget", event => {
                PalantirHome.dragged = event.currentTarget.dataset.homeWidget;
                event.originalEvent.dataTransfer.effectAllowed = "move";
                event.originalEvent.dataTransfer.setData("text/plain", PalantirHome.dragged);
                event.currentTarget.classList.add("is-dragging");
            })
            .on("dragover", ".palantir-home__widget", event => {
                if (PalantirHome.dragged && PalantirHome.dragged !== event.currentTarget.dataset.homeWidget) {
                    event.preventDefault();
                    event.currentTarget.classList.add("is-drop-target");
                }
            })
            .on("dragleave", ".palantir-home__widget", event => event.currentTarget.classList.remove("is-drop-target"))
            .on("drop", ".palantir-home__widget", event => {
                event.preventDefault();
                const target = event.currentTarget.dataset.homeWidget;
                const dragged = PalantirHome.dragged;
                PalantirHome.dragged = null;
                if (dragged && dragged !== target) {
                    const layout = PalantirHome.layout();
                    layout.order = layout.order.filter(id => id !== dragged);
                    layout.order.splice(layout.order.indexOf(target), 0, dragged);
                    PalantirHome.saveLayout(layout);
                }
            })
            .on("dragend", ".palantir-home__widget", () => {
                PalantirHome.dragged = null;
                $("#homeGrid .is-dragging, #homeGrid .is-drop-target").removeClass("is-dragging is-drop-target");
            });
    }

    static act(button) {
        const id = button.dataset.homeFor;
        const layout = PalantirHome.layout();
        const shown = layout.order.filter(entry => PalantirHome.available(PalantirHome.widget(entry)));
        const position = shown.indexOf(id);
        switch (button.dataset.homeAction) {
        case "earlier":
        case "later": {
            const neighbor = shown[position + (button.dataset.homeAction === "earlier" ? -1 : 1)];
            if (!neighbor) {
                return;
            }
            layout.order = layout.order.filter(entry => entry !== id);
            layout.order.splice(layout.order.indexOf(neighbor) + (button.dataset.homeAction === "later" ? 1 : 0), 0, id);
            break;
        }
        default:
            return;
        }
        PalantirHome.saveLayout(layout);
        $(`#homeGrid [data-home-for="${id}"][data-home-action="${button.dataset.homeAction}"]`).trigger("focus");
    }

    /**
     * Ask for the summary and draw it. A refresh asked for while one is waiting for the server is skipped; a summary
     * that cannot be read leaves the widgets as they were and says so.
     *
     * @returns {Promise<void>} resolved once the widgets are drawn
     */
    static async refresh({visible = false} = {}) {
        if (PalantirHome.pending) {
            return;
        }
        PalantirHome.pending = true;
        const progress = visible
            ? setTimeout(() => $("#homeProgress").prop("hidden", PalantirHome.summary === null), PalantirHome.PROGRESS_DELAY)
            : null;
        try {
            const response = await fetch(`${retrieveDashboardUrl()}/overview`, {credentials: "include", headers: {"Accept": "application/json"}});
            if (!response.ok) {
                throw new Error(`${response.status}`);
            }
            PalantirHome.summary = await response.json();
            PalantirHome.fetched = Date.now();
            const sessions = PalantirHome.summary.sections?.tickets?.sessions;
            if (Number.isFinite(sessions)) {
                PalantirHome.sessions = [...PalantirHome.sessions, sessions].slice(-PalantirHome.MAX_SAMPLES);
            }
            PalantirHome.sampleRequests(PalantirHome.summary.sections.traffic?.http?.count);
            $("#homeUpdated").text(`Updated ${new Date().toLocaleTimeString()} · every ${Math.round(palantirSettings().refreshInterval / 1000)} s`);
            if (currentActiveTab === Tabs.HOME.index) {
                $("#palantirPageTitle").text(palantirGreeting());
            }
            PalantirHome.render();
        } catch (error) {
            console.warn("The overview could not be read just now:", error);
            $("#homeUpdated").text("The overview could not be read just now; trying again at the next refresh.");
            $("#homeLoading p").text("The overview could not be read just now; trying again at the next refresh.");
        } finally {
            clearTimeout(progress);
            $("#homeProgress").prop("hidden", true);
            PalantirHome.pending = false;
        }
    }

    static render() {
        const grid = $("#homeGrid");
        if (PalantirHome.summary === null) {
            return;
        }
        if (PalantirHome.dragged) {
            return;
        }
        grid.attr("aria-busy", "false");
        const layout = PalantirHome.layout();
        const active = document.activeElement;
        const restore = active && grid[0].contains(active)
            ? (active.dataset.homeAction
                ? `[data-home-action="${active.dataset.homeAction}"][data-home-for="${active.dataset.homeFor}"]`
                : active.dataset.homePage ? `[data-home-widget="${active.closest("[data-home-widget]")?.dataset.homeWidget}"] [data-home-page]` : null)
            : null;
        grid.empty();
        layout.order
            .map(id => PalantirHome.widget(id))
            .filter(widget => PalantirHome.available(widget))
            .forEach((widget, index, shown) => grid.append(PalantirHome.frame(widget, index, shown.length)));
        PalantirHome.drawHealthChart();
        if (restore) {
            grid.find(restore).first().trigger("focus");
        }
    }

    static frame(widget, index, count) {
        const card = $("<section>", {
            class: `palantir-home__widget palantir-home__widget--span-${widget.span}`,
            "data-home-widget": widget.id,
            "aria-label": widget.title
        }).attr("draggable", "true");
        const head = $("<header>", {class: "palantir-home__widget-head"})
            .append($("<i>", {class: "mdi mdi-drag palantir-home__grip", "aria-hidden": "true", title: "Drag to move"}))
            .append($("<h3>").append(widget.page
                ? $("<button>", {type: "button", class: "palantir-home__title-link", "data-home-page": widget.page, text: widget.title, title: `Go to ${widget.title}`})
                : document.createTextNode(widget.title)));
        const actions = $("<span>", {class: "palantir-home__widget-actions"});
        [["earlier", "mdi-arrow-left", "Move earlier", index === 0],
            ["later", "mdi-arrow-right", "Move later", index === count - 1]].forEach(([action, icon, label, disabled]) => {
            actions.append($("<button>", {
                type: "button",
                class: "palantir-icon-button palantir-home__action",
                "data-home-action": action,
                "data-home-for": widget.id,
                "aria-label": `${label}: ${widget.title}`,
                title: label,
                disabled
            }).append($("<i>", {class: `mdi ${icon}`, "aria-hidden": "true"})));
        });
        head.append(actions);
        const body = $("<div>", {class: "palantir-home__widget-body"});
        try {
            body.append(widget.render(PalantirHome.summary));
        } catch (error) {
            console.warn(`The ${widget.id} widget could not be drawn`, error);
            body.append(PalantirHome.muted("This could not be shown just now."));
        }
        return card.append(head).append(body);
    }

    static muted(text) {
        return $("<p>", {class: "palantir-home__muted", text});
    }

    static number(value) {
        return Number.isFinite(value) ? new Intl.NumberFormat().format(value) : "—";
    }

    static duration(seconds) {
        const days = Math.floor(seconds / 86400);
        const hours = Math.floor(seconds % 86400 / 3600);
        const minutes = Math.floor(seconds % 3600 / 60);
        return days > 0 ? `${days} d ${hours} h` : hours > 0 ? `${hours} h ${minutes} min` : `${minutes} min`;
    }

    static time(value) {
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? "" : date.toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
    }

    static staleNote(section) {
        if (section?.pending) {
            return PalantirHome.muted("Still being worked out; it shows at the next refresh.");
        }
        if (section?.stale) {
            return PalantirHome.muted(`As of ${PalantirHome.time(section.computed)}; the source is slow to answer.`);
        }
        return null;
    }

    static kpi(value, note, spark) {
        const box = $("<div>", {class: "palantir-home__kpi"});
        const row = $("<div>", {class: "palantir-home__kpi-row"}).append($("<strong>", {text: value}));
        if (spark) {
            row.append($("<span>", {class: "palantir-home__spark"}).html(spark));
        }
        return box.append(row).append($("<span>", {class: "palantir-home__kpi-note", text: note}));
    }

    static renderStatus(summary) {
        const server = summary.sections.server;
        const health = summary.sections.health;
        const state = !health || health.unavailable ? "neutral" : health.pending ? "pending" : health.status === "UP" ? "up" : "down";
        const label = {neutral: "CAS is running", pending: "Checking health...", up: "All systems up", down: "Something is down"}[state];
        const band = $("<div>", {class: `palantir-home__status is-${state}`, role: "status"})
            .append($("<span>", {class: "palantir-home__pulse", "aria-hidden": "true"}))
            .append($("<b>", {text: label}));
        const facts = $("<span>", {class: "palantir-home__facts"});
        [`CAS ${server.version}`, `up ${PalantirHome.duration(server.uptimeSeconds)}`, server.host ? `node ${server.host}` : null,
            `Java ${server.java}`].filter(Boolean).forEach(fact => facts.append($("<span>", {text: fact})));
        if (health?.components) {
            const down = health.components.filter(component => component.status !== "UP");
            facts.append($("<span>", {text: down.length === 0
                ? `${health.components.length} health checks up`
                : `${down.length} of ${health.components.length} health checks need attention`}));
        }
        return band.append(facts);
    }

    static renderSessions(summary) {
        const tickets = summary.sections.tickets;
        return [PalantirHome.kpi(PalantirHome.number(tickets.sessions), "signed in now",
            typeof PalantirMetrics !== "undefined" ? PalantirMetrics.sparkline(PalantirHome.sessions) : ""), PalantirHome.staleNote(tickets)];
    }

    /**
     * The health checks as the system health chart shows them: one bar per check, named as on the system page, green
     * when it is up and red when it is not, with the overall status in the legend. A check that was too slow to answer
     * is amber. The time each check took is in its tooltip.
     *
     * @param summary the summary
     * @returns {Array<jQuery>} the chart container and a note when the numbers are stale
     */
    static renderHealth(summary) {
        const health = summary.sections.health;
        if (health.pending) {
            return [PalantirHome.muted("Health checks are still running; they show at the next refresh.")];
        }
        const components = health.components ?? [];
        if (components.length === 0) {
            return [PalantirHome.muted("No health checks are registered.")];
        }
        const box = $("<div>", {class: "palantir-home__health", style: `height: ${Math.max(150, components.length * 24 + 60)}px`})
            .append($("<canvas>", {id: "homeHealthChart", role: "img",
                "aria-label": components.map(component => `${camelcaseToTitleCase(component.name)} ${component.status.toLowerCase()}`).join("; ")}));
        const layout = $("<div>", {class: "palantir-home__health-layout"})
            .append(PalantirHome.healthRing(health.status, components))
            .append(box);
        return [layout, PalantirHome.staleNote(health)];
    }

    /**
     * The overall health as a ring with its status inside: green when CAS is up, red when it is down, amber when it is
     * up but a check was too slow to answer.
     *
     * @param status the overall status
     * @param components the health checks
     * @returns {jQuery} the ring
     */
    static healthRing(status, components) {
        const slow = components.some(component => component.status === "SLOW");
        const tone = status === "UP" ? (slow ? "slow" : "up") : "down";
        const up = components.filter(component => component.status === "UP").length;
        const label = `Overall health ${status}; ${up} of ${components.length} checks up`;
        return $("<div>", {class: `palantir-home__ring is-${tone}`, role: "img", "aria-label": label, title: label})
            .append($(`<svg viewBox="0 0 120 120" aria-hidden="true" focusable="false">
                <circle class="palantir-home__ring-track" cx="60" cy="60" r="48"/>
                <circle class="palantir-home__ring-value" cx="60" cy="60" r="48"/>
                <text x="60" y="60" class="palantir-home__ring-status">${escapePalantirMarkup(status)}</text>
                <text x="60" y="82" class="palantir-home__ring-note">${up}/${components.length} up</text>
            </svg>`));
    }

    static drawHealthChart() {
        if (PalantirHome.healthChart) {
            PalantirHome.healthChart.destroy();
            PalantirHome.healthChart = null;
        }
        const canvas = document.getElementById("homeHealthChart");
        const health = PalantirHome.summary?.sections?.health;
        const components = health?.components;
        if (!canvas || !Array.isArray(components) || typeof Chart === "undefined") {
            return;
        }
        const up = "rgb(5, 166, 31)";
        const down = "rgba(166, 45, 15)";
        const slow = "rgb(201, 154, 6)";
        const colors = components.map(component => component.status === "UP" ? up : component.status === "SLOW" ? slow : down);
        PalantirHome.healthChart = new Chart(canvas, {
            type: "bar",
            data: {
                labels: components.map(component => camelcaseToTitleCase(component.name)),
                datasets: [{
                    label: "System Health",
                    data: components.map(() => 1),
                    fill: true,
                    borderWidth: 2,
                    backgroundColor: colors,
                    borderColor: colors
                }]
            },
            options: {
                indexAxis: "y",
                responsive: true,
                maintainAspectRatio: false,
                animation: false,
                plugins: {
                    legend: {
                        labels: {
                            generateLabels: chart => Chart.defaults.plugins.legend.labels.generateLabels(chart).map(label => ({
                                ...label,
                                fillStyle: health.status === "UP" ? up : down,
                                lineWidth: 0
                            }))
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: context => {
                                const component = components[context.dataIndex];
                                return `${component.status.toLowerCase().replace(/_/g, " ")} · ${component.millis} ms`;
                            }
                        }
                    }
                },
                scales: {
                    x: {ticks: {display: false}, max: 1}
                }
            }
        });
    }

    /**
     * Heap, processor and threads as meters, each turning amber at three quarters and red at nine tenths.
     *
     * @param summary the summary
     * @returns {jQuery} the meters
     */
    static renderResources(summary) {
        const resources = summary.sections.resources;
        const meters = $("<div>", {class: "palantir-home__meters"});
        const meter = (label, share, detail) => {
            const level = share >= 0.9 ? " is-critical" : share >= 0.75 ? " is-warning" : "";
            meters.append($("<div>", {class: `palantir-home__meter${level}`})
                .append($("<div>", {class: "palantir-home__meter-head"})
                    .append($("<span>", {text: label}))
                    .append($("<strong>", {text: `${Math.round(share * 100)}%`})))
                .append($("<span>", {class: "palantir-home__meter-track", role: "meter", "aria-label": label,
                    "aria-valuemin": "0", "aria-valuemax": "100", "aria-valuenow": String(Math.round(share * 100))})
                    .append($("<i>", {style: `width: ${Math.max(1, Math.min(100, share * 100))}%`})))
                .append($("<small>", {text: detail})));
        };
        if (resources.heapMax > 0) {
            meter("Heap Memory", resources.heapUsed / resources.heapMax,
                `${PalantirHome.bytes(resources.heapUsed)} of ${PalantirHome.bytes(resources.heapMax)}`);
        }
        if (Number.isFinite(resources.cpu)) {
            meter("Processor", resources.cpu, `${resources.processors} processor${resources.processors === 1 ? "" : "s"} available`);
        }
        meters.append($("<p>", {class: "palantir-home__muted", text: `${PalantirHome.number(resources.threads)} threads running`}));
        return meters;
    }

    static bytes(value) {
        const units = ["B", "KB", "MB", "GB", "TB"];
        let size = value;
        let index = 0;
        while (size >= 1024 && index < units.length - 1) {
            size /= 1024;
            index++;
        }
        return `${size.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
    }

    /**
     * Keep the request count of each refresh, to work out how many requests a minute CAS answers.
     *
     * @param count the requests answered so far
     */
    static sampleRequests(count) {
        if (!Number.isFinite(count)) {
            return;
        }
        const now = Date.now();
        const last = PalantirHome.lastRequests;
        if (last && now > last.time && count >= last.count) {
            const rate = (count - last.count) / ((now - last.time) / 60000);
            PalantirHome.requests = [...PalantirHome.requests, rate].slice(-PalantirHome.MAX_SAMPLES);
        }
        PalantirHome.lastRequests = {time: now, count};
    }

    static seconds(value) {
        if (!Number.isFinite(value)) {
            return "—";
        }
        if (value < 1) {
            return `${(value * 1000).toFixed(value < 0.01 ? 1 : 0)} ms`;
        }
        return value < 60 ? `${value.toFixed(2)} s` : `${Math.floor(value / 60)} m ${Math.round(value % 60)} s`;
    }

    static stat(label, value, detail, tone = "") {
        return $("<div>", {class: `palantir-home__stat${tone ? ` is-${tone}` : ""}`})
            .append($("<small>", {text: label}))
            .append($("<strong>", {text: value}))
            .append($("<span>", {text: detail}));
    }

    static renderHttp(summary) {
        const http = summary.sections.traffic.http;
        const rate = PalantirHome.requests.at(-1);
        const spark = typeof PalantirMetrics !== "undefined" ? PalantirMetrics.sparkline(PalantirHome.requests) : "";
        const share = count => http.count > 0 ? `${(count * 100 / http.count).toFixed(count * 100 / http.count < 1 ? 2 : 1)}% of requests` : "";
        const stats = $("<div>", {class: "palantir-home__stats"})
            .append(PalantirHome.stat("Answered", PalantirHome.number(http.count), "since CAS started"))
            .append(PalantirHome.stat("Average", PalantirHome.seconds(http.count > 0 ? http.totalSeconds / http.count : NaN), "per request"))
            .append(PalantirHome.stat("Slowest", PalantirHome.seconds(http.maxSeconds), "recently"))
            .append(PalantirHome.stat("Server errors", PalantirHome.number(http.serverErrors),
                `${share(http.serverErrors)} · ${PalantirHome.number(http.clientErrors)} client errors`, http.serverErrors > 0 ? "critical" : ""));
        return [PalantirHome.kpi(Number.isFinite(rate) ? `${PalantirHome.number(Math.round(rate))}/min` : "—",
            Number.isFinite(rate) ? "requests a minute, while this page is open" : "requests a minute; measured from the next refresh", spark), stats];
    }

    static renderRuntime(summary) {
        const traffic = summary.sections.traffic;
        const stats = $("<div>", {class: "palantir-home__stats"});
        if (traffic.gc) {
            stats.append(PalantirHome.stat("GC pauses", PalantirHome.number(traffic.gc.count),
                `avg ${PalantirHome.seconds(traffic.gc.count > 0 ? traffic.gc.totalSeconds / traffic.gc.count : NaN)}`))
                .append(PalantirHome.stat("Longest pause", PalantirHome.seconds(traffic.gc.maxSeconds), "recently",
                    traffic.gc.maxSeconds >= 1 ? "warning" : ""));
        }
        if (Number.isFinite(traffic.errorsLogged)) {
            stats.append(PalantirHome.stat("Errors logged", PalantirHome.number(traffic.errorsLogged), "since CAS started",
                traffic.errorsLogged > 0 ? "warning" : ""));
        }
        if (Number.isFinite(traffic.loadAverage)) {
            const processors = summary.sections.resources?.processors;
            stats.append(PalantirHome.stat("System load", traffic.loadAverage.toFixed(2),
                processors ? `last minute, ${processors} processors` : "last minute",
                processors && traffic.loadAverage > processors ? "warning" : ""));
        }
        return stats.children().length > 0 ? stats : PalantirHome.muted("This server records no runtime metrics.");
    }

    static renderChanges(summary) {
        const changes = summary.sections.changes;
        const recent = changes.recent ?? [];
        const cluster = changes.scope === "cluster";
        if (recent.length === 0) {
            return PalantirHome.muted(cluster
                ? "No application changes are recorded in the service history yet."
                : `No application was added, changed or deleted on this server since ${new Date(changes.since).toLocaleString()}.`);
        }
        const verbs = {created: "created", updated: "updated", saved: "saved", deleted: "deleted"};
        const list = $("<ul>", {class: "palantir-home__changes"});
        recent.slice(0, 8).forEach(change => {
            const when = new Date(change.when);
            list.append($("<li>", {class: `palantir-home__change is-${change.action}`})
                .append($("<span>", {class: "palantir-home__change-icon", "aria-hidden": "true"})
                    .append($("<i>", {class: `mdi ${{created: "mdi-plus-circle-outline", deleted: "mdi-delete-outline"}[change.action] ?? "mdi-content-save-outline"}`})))
                .append($("<span>", {class: "palantir-home__change-text"})
                    .append($("<b>", {text: change.name}))
                    .append(document.createTextNode(` ${verbs[change.action] ?? change.action} by `))
                    .append($("<b>", {text: change.author}))
                    .append(change.type ? $("<small>", {text: change.type}) : null))
                .append($("<time>", {datetime: change.when, title: when.toLocaleString(), text: PalantirHome.ago(when)})));
        });
        return [list, PalantirHome.muted(cluster
            ? "Changes made on any CAS server, from the service history."
            : "Changes made on this server since it started.")];
    }

    static ago(date) {
        const seconds = Math.max(0, Math.round((Date.now() - date.getTime()) / 1000));
        if (seconds < 60) {
            return "just now";
        }
        if (seconds < 3600) {
            return `${Math.floor(seconds / 60)} min ago`;
        }
        if (seconds < 86400) {
            return `${Math.floor(seconds / 3600)} h ago`;
        }
        return date.toLocaleDateString();
    }

    static renderTypes(summary) {
        const applications = summary.sections.applications;
        const entries = Object.entries(applications.byType ?? {}).sort((left, right) => right[1] - left[1]);
        if (entries.length === 0) {
            return PalantirHome.muted("No applications are registered.");
        }
        const max = Math.max(...entries.map(entry => entry[1]));
        const bars = $("<div>", {class: "palantir-home__bars"});
        entries.forEach(([type, count]) => bars.append($("<div>", {class: "palantir-home__type"})
            .append($("<span>", {text: type, title: type}))
            .append($("<span>", {class: "palantir-home__track"}).append($("<i>", {style: `width: ${Math.max(2, count * 100 / max)}%`})))
            .append($("<span>", {class: "palantir-home__count", text: PalantirHome.number(count)}))));
        const expiring = applications.expiring ?? [];
        const result = [bars];
        if (expiring.length > 0) {
            result.push($("<p>", {class: "palantir-home__muted", text: `Expiring within two weeks: ${expiring.map(entry => entry.name).join(", ")}`}));
        }
        return result;
    }
}

/**
 * Draw the home page and keep it current while it is shown.
 *
 * @returns {Promise<void>} resolved once the first summary is drawn
 */
async function initializePalantirHome() {
    await PalantirHome.initialize();
}
