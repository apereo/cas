/**
 * The home page: widgets drawn from one summary of the server, asked for at the refresh interval while the page is
 * shown. The server computes and keeps each part of the summary, so the page costs one request per refresh however
 * many widgets it shows. People can change the order of the widgets, hide the ones they do not want, bring them back
 * or reset the page; that choice is kept in the browser. The summary is asked only for what the shown widgets need, so
 * a hidden widget whose data is slow to compute costs nothing.
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
        {id: "status", title: "Server", span: 4, needs: ["server"], uses: ["health"], render: summary => PalantirHome.renderStatus(summary)},
        {id: "health", title: "System Health", span: 2, needs: ["health"], page: "system", render: summary => PalantirHome.renderHealth(summary)},
        {id: "resources", title: "Resources", span: 2, needs: ["resources"], page: "system", render: summary => PalantirHome.renderResources(summary)},
        {id: "http", title: "HTTP Requests", span: 2, needs: ["traffic"], page: "system", available: summary => Boolean(summary.sections.traffic?.http), render: summary => PalantirHome.renderHttp(summary)},
        {id: "endpoints", title: "Slowest Endpoints", span: 4, needs: ["endpoints"], page: "system", render: summary => PalantirHome.renderEndpoints(summary)},
        {id: "runtime", title: "Runtime", span: 2, needs: ["traffic"], uses: ["resources"], page: "system", render: summary => PalantirHome.renderRuntime(summary)},
        {id: "sessions", title: "SSO Sessions", span: 2, needs: ["tickets"], page: "sessions", render: summary => PalantirHome.renderSessions(summary)},
        {id: "types", title: "Applications", span: 2, needs: ["applications"], page: "applications", render: summary => PalantirHome.renderTypes(summary)},
        {id: "changes", title: "Recent Application Changes", span: 4, needs: ["changes"], page: "applications", render: summary => PalantirHome.renderChanges(summary)}
    ];

    static summary = null;
    static sessions = [];
    static requests = [];
    static lastRequests = null;
    static pending = false;
    static again = false;
    static fetched = 0;
    static FRESH_FOR = 5000;
    static PROGRESS_DELAY = 200;
    static timer = null;
    static dragged = null;
    static healthChart = null;
    static refreshing = new Set();
    static queued = false;
    static reads = 0;
    static readings = {};

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
        const hidden = Array.isArray(stored?.hidden) ? stored.hidden.filter(id => ids.includes(id)) : [];
        return {order, hidden};
    }

    /**
     * The sections of the summary the widgets on the page read, those they need and those they show when present;
     * hidden widgets are left out.
     *
     * @returns {string[]} the section names
     */
    static wantedSections() {
        const layout = PalantirHome.layout();
        return [...new Set(layout.order
            .filter(id => !layout.hidden.includes(id))
            .map(id => PalantirHome.widget(id))
            .flatMap(widget => [...widget.needs, ...(widget.uses ?? [])]))];
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
        $("#homeRefresh").off("click").on("click", () => {
            $("#homeRefresh").addClass("is-refreshing").attr("aria-busy", "true");
            PalantirHome.refresh({visible: true, fresh: true});
        });
        $("#homeCustomize").off("click").on("click", () => PalantirHome.toggleMenu());
        $("#homeCustomizeMenu").off("click keydown")
            .on("click", "[data-home-action]", event => PalantirHome.act(event.currentTarget))
            .on("keydown", event => {
                if (event.key === "Escape") {
                    PalantirHome.toggleMenu(false);
                    $("#homeCustomize").trigger("focus");
                } else if (event.key === "ArrowDown" || event.key === "ArrowUp") {
                    event.preventDefault();
                    const items = $("#homeCustomizeMenu button").get();
                    const position = items.indexOf(document.activeElement);
                    const step = event.key === "ArrowDown" ? 1 : -1;
                    items[(position + step + items.length) % items.length]?.focus();
                }
            });
        $(document).off("click.palantirHomeMenu").on("click.palantirHomeMenu", event => {
            if (!$(event.target).closest(".palantir-home__customize").length) {
                PalantirHome.toggleMenu(false);
            }
        });
        $("#homeGrid").off("click dragstart dragover dragleave drop dragend")
            .on("click", "[data-home-action]", event => PalantirHome.act(event.currentTarget))
            .on("click", "[data-home-application]", event => openTopApplication(event.currentTarget.dataset.homeApplication))
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

    /**
     * Open or close the Customize menu, which lists the hidden widgets to show again and resets the page.
     *
     * @param {boolean} open whether to open it; toggles when not given
     */
    static toggleMenu(open) {
        const menu = $("#homeCustomizeMenu");
        const show = open ?? menu.prop("hidden");
        if (show) {
            PalantirHome.renderMenu();
        }
        menu.prop("hidden", !show);
        $("#homeCustomize").attr("aria-expanded", String(show));
        if (show) {
            menu.find("button").first().trigger("focus");
        }
    }

    static renderMenu() {
        const layout = PalantirHome.layout();
        const menu = $("#homeCustomizeMenu").empty();
        const hidden = layout.order.filter(id => layout.hidden.includes(id));
        menu.append($("<p>", {class: "palantir-home__customize-heading",
            text: hidden.length > 0 ? "Hidden cards" : "No cards are hidden."}));
        hidden.forEach(id => menu.append($("<button>", {
            type: "button",
            role: "menuitem",
            class: "palantir-home__customize-item",
            "data-home-action": "show",
            "data-home-for": id
        }).append($("<i>", {class: "mdi mdi-eye-outline", "aria-hidden": "true"}))
            .append($("<span>", {text: `Show ${PalantirHome.widget(id).title}`}))));
        menu.append($("<button>", {
            type: "button",
            role: "menuitem",
            class: "palantir-home__customize-item is-reset",
            "data-home-action": "reset"
        }).append($("<i>", {class: "mdi mdi-restore", "aria-hidden": "true"}))
            .append($("<span>", {text: "Reset the layout"})));
    }

    static act(button) {
        const id = button.dataset.homeFor;
        const layout = PalantirHome.layout();
        const shown = layout.order.filter(entry => !layout.hidden.includes(entry) && PalantirHome.available(PalantirHome.widget(entry)));
        const position = shown.indexOf(id);
        switch (button.dataset.homeAction) {
        case "refresh":
            PalantirHome.refreshWidget(id);
            return;
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
        case "hide":
            layout.hidden = [...new Set([...layout.hidden, id])];
            PalantirHome.saveLayout(layout);
            PalantirHome.announce(`${PalantirHome.widget(id).title} is hidden; bring it back from Customize.`);
            $("#homeCustomize").trigger("focus");
            return;
        case "show":
            layout.hidden = layout.hidden.filter(entry => entry !== id);
            PalantirHome.toggleMenu(false);
            PalantirHome.saveLayout(layout);
            PalantirHome.refreshIfMissing();
            if (!$(`#homeGrid [data-home-widget="${id}"]`).find("button").first().trigger("focus").length) {
                $("#homeCustomize").trigger("focus");
            }
            return;
        case "reset":
            PalantirHome.toggleMenu(false);
            try {
                localStorage.removeItem(PalantirHome.STORAGE_KEY);
            } catch (error) {
                console.debug("The home layout could not be reset in the browser", error);
            }
            PalantirHome.render();
            PalantirHome.refreshIfMissing();
            PalantirHome.announce("The home page is back to its original layout.");
            $("#homeCustomize").trigger("focus");
            return;
        default:
            return;
        }
        PalantirHome.saveLayout(layout);
        $(`#homeGrid [data-home-for="${id}"][data-home-action="${button.dataset.homeAction}"]`).trigger("focus");
    }

    /**
     * Ask for the summary again when a widget brought back reads a section the last summary did not include.
     */
    static refreshIfMissing() {
        const sections = PalantirHome.summary?.sections ?? {};
        if (PalantirHome.wantedSections().some(name => !(name in sections))) {
            if (PalantirHome.pending) {
                PalantirHome.again = true;
            } else {
                PalantirHome.refresh({visible: true});
            }
        }
    }

    static announce(message) {
        $("#homeUpdated").text(message);
    }

    /**
     * Ask for the summary and draw it. A refresh asked for while one is waiting for the server is skipped, unless the
     * operator asked for it, in which case it runs once the one under way is done; a summary that cannot be read
     * leaves the widgets as they were and says so.
     *
     * @param visible whether to show the progress bar while the summary is read
     * @param fresh whether the operator asked for it, so the server computes every section again
     * @returns {Promise<void>} resolved once the widgets are drawn
     */
    static async refresh({visible = false, fresh = false} = {}) {
        if (PalantirHome.pending) {
            PalantirHome.queued = PalantirHome.queued || fresh;
            return;
        }
        const wanted = PalantirHome.wantedSections();
        if (wanted.length === 0) {
            PalantirHome.summary = PalantirHome.summary ?? {sections: {}};
            $("#homeRefresh").removeClass("is-refreshing").attr("aria-busy", "false");
            PalantirHome.render();
            return;
        }
        PalantirHome.pending = true;
        $("#homeRefresh").toggleClass("is-refreshing", fresh).attr("aria-busy", String(fresh));
        const progress = visible
            ? setTimeout(() => $("#homeProgress").prop("hidden", PalantirHome.summary === null), PalantirHome.PROGRESS_DELAY)
            : null;
        try {
            const read = ++PalantirHome.reads;
            PalantirHome.sample(PalantirHome.apply(await PalantirHome.read(wanted, fresh), read));
            PalantirHome.fetched = Date.now();
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
            if (!PalantirHome.queued) {
                $("#homeRefresh").removeClass("is-refreshing").attr("aria-busy", "false");
            }
            PalantirHome.pending = false;
        }
        if (PalantirHome.queued) {
            PalantirHome.queued = false;
            PalantirHome.again = false;
            await PalantirHome.refresh({visible: true, fresh: true});
        } else if (PalantirHome.again) {
            PalantirHome.again = false;
            PalantirHome.refreshIfMissing();
        }
    }

    /**
     * Read the summary of some sections from the server.
     *
     * @param sections the section names
     * @param fresh whether the server should compute them again rather than use the values it kept
     * @returns {Promise<object>} the summary
     */
    static async read(sections, fresh) {
        const query = new URLSearchParams({sections: sections.join(",")});
        if (fresh) {
            query.set("fresh", "true");
        }
        const response = await fetch(`${retrieveDashboardUrl()}/overview?${query}`, {credentials: "include", headers: {"Accept": "application/json"}});
        if (!response.ok) {
            throw new Error(`${response.status}`);
        }
        return response.json();
    }

    /**
     * Take the sections of a summary that are newer than what the page shows. The page and its cards read sections
     * independently, so an answer to an earlier request, such as the page's own refresh, can arrive after a card was
     * refreshed; a section is only taken from an answer to a request made after the one it was last taken from.
     *
     * @param summary the summary read
     * @param read the number of the request it answers, counted when the request was made
     * @returns {object} the summary of the sections taken
     */
    static apply(summary, read) {
        const taken = Object.fromEntries(Object.entries(summary?.sections ?? {})
            .filter(([name]) => read > (PalantirHome.readings[name] ?? 0)));
        Object.keys(taken).forEach(name => PalantirHome.readings[name] = read);
        PalantirHome.summary = {
            ...(PalantirHome.summary ?? {}),
            ...summary,
            sections: {...(PalantirHome.summary?.sections ?? {}), ...taken}
        };
        return {sections: taken};
    }

    /**
     * Keep the readings the sparklines draw from a summary.
     *
     * @param summary the summary, or part of it
     */
    static sample(summary) {
        const sessions = summary.sections?.tickets?.sessions;
        if (Number.isFinite(sessions)) {
            PalantirHome.sessions = [...PalantirHome.sessions, sessions].slice(-PalantirHome.MAX_SAMPLES);
        }
        PalantirHome.sampleRequests(summary.sections?.traffic?.http?.count);
    }

    /**
     * Read again only the sections one card shows, and redraw the page with them; the other cards keep what they
     * show. The card's refresh button turns while the read is under way.
     *
     * @param id the card
     * @returns {Promise<void>} resolved once the card is drawn
     */
    static async refreshWidget(id) {
        const widget = PalantirHome.widget(id);
        if (!widget || PalantirHome.refreshing.has(id)) {
            return;
        }
        PalantirHome.refreshing.add(id);
        PalantirHome.markRefreshing(id, true);
        try {
            const read = ++PalantirHome.reads;
            PalantirHome.sample(PalantirHome.apply(await PalantirHome.read([...new Set([...widget.needs, ...(widget.uses ?? [])])], true), read));
            PalantirHome.announce(`${widget.title} updated ${new Date().toLocaleTimeString()}`);
        } catch (error) {
            console.warn(`The ${widget.id} card could not be read just now:`, error);
            PalantirHome.announce(`${widget.title} could not be read just now; trying again at the next refresh.`);
        } finally {
            PalantirHome.refreshing.delete(id);
            PalantirHome.render();
            PalantirHome.markRefreshing(id, false);
        }
    }

    static markRefreshing(id, refreshing) {
        $(`#homeGrid [data-home-action="refresh"][data-home-for="${id}"]`)
            .toggleClass("is-refreshing", refreshing)
            .attr("aria-busy", String(refreshing));
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
                : active.dataset.homePage ? `[data-home-widget="${active.closest("[data-home-widget]")?.dataset.homeWidget}"] [data-home-page]`
                    : active.dataset.homeApplication ? `[data-home-application="${CSS.escape(active.dataset.homeApplication)}"]` : null)
            : null;
        const shown = layout.order
            .filter(id => !layout.hidden.includes(id))
            .map(id => PalantirHome.widget(id))
            .filter(widget => PalantirHome.available(widget));
        const cards = grid.children();
        const unchanged = shown.length > 0 && cards.length === shown.length
            && shown.every((widget, index) => cards[index].dataset.homeWidget === widget.id);
        if (unchanged) {
            shown.forEach((widget, index) => PalantirHome.update(cards.eq(index), PalantirHome.frame(widget, index, shown.length)));
        } else {
            grid.empty();
            shown.forEach((widget, index) => grid.append(PalantirHome.frame(widget, index, shown.length)));
            if (layout.order.every(id => layout.hidden.includes(id))) {
                grid.append($("<p>", {class: "palantir-home__muted palantir-home__empty",
                    text: "Every card is hidden. Bring them back from Customize."}));
            }
        }
        if (!$("#homeCustomizeMenu").prop("hidden")) {
            PalantirHome.renderMenu();
        }
        PalantirHome.drawHealthChart();
        if (restore) {
            grid.find(restore).first().trigger("focus");
        }
    }

    /**
     * Bring a card already on the page up to date with a newly drawn one, keeping the card and its header, so a
     * refresh does not move the page or take the focus away. The health chart keeps its canvas, and is updated in
     * place once the page is drawn.
     *
     * @param card the card on the page
     * @param drawn the card drawn from the latest summary
     */
    static update(card, drawn) {
        const body = card.children(".palantir-home__widget-body");
        const next = drawn.children(".palantir-home__widget-body");
        const box = body.find(".palantir-home__health");
        const nextBox = next.find(".palantir-home__health");
        if (box.length === 0 || nextBox.length === 0) {
            body.replaceWith(next);
            return;
        }
        box.attr("style", nextBox.attr("style"));
        box.find("canvas").attr("aria-label", nextBox.find("canvas").attr("aria-label"));
        body.find(".palantir-home__ring").replaceWith(next.find(".palantir-home__ring"));
        body.children().not(".palantir-home__health-layout").remove();
        body.append(next.children().not(".palantir-home__health-layout"));
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
        [["refresh", "mdi-refresh", "Refresh", false],
            ["earlier", "mdi-arrow-left", "Move earlier", index === 0],
            ["later", "mdi-arrow-right", "Move later", index === count - 1],
            ["hide", "mdi-eye-off-outline", "Hide", false]].forEach(([action, icon, label, disabled]) => {
            actions.append($("<button>", {
                type: "button",
                class: "palantir-icon-button palantir-home__action",
                "data-home-action": action,
                "data-home-for": widget.id,
                "aria-label": `${label}: ${widget.title}`,
                title: label,
                disabled
            }).toggleClass("is-refreshing", action === "refresh" && PalantirHome.refreshing.has(widget.id))
                .append($("<i>", {class: `mdi ${icon}`, "aria-hidden": "true"})));
        });
        head.append(actions);
        const body = $("<div>", {class: "palantir-home__widget-body"});
        const sections = PalantirHome.summary?.sections ?? {};
        try {
            if (widget.needs.some(name => sections[name]?.pending)) {
                body.append(PalantirHome.muted("Still being worked out on the server. If this card is often slow, "
                    + "hide it from its header and it will no longer be asked for."));
                return card.append(head).append(body);
            }
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
            const computed = new Date(section.computed);
            return PalantirHome.muted(Number.isNaN(computed.getTime())
                ? "Being counted again; the last count is shown."
                : `Counted ${PalantirHome.ago(computed)}, at ${PalantirHome.time(section.computed)}; being counted again.`);
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

    /**
     * The server at a glance: a beacon in the color of its state, what CAS is and where it runs, and the health checks
     * as one segment each, colored as the health chart colors them, with the checks that are not up named in tags. The
     * host is the one the login page footer shows. CAS that is up while some checks are not reads as up with checks to
     * look at, in amber.
     *
     * @param summary the summary
     * @returns {jQuery} the card body
     */
    static renderStatus(summary) {
        const server = summary.sections.server;
        const health = summary.sections.health;
        const components = Array.isArray(health?.components) ? health.components : [];
        const attention = components.filter(component => component.status !== "UP");
        const state = !health || health.unavailable ? "neutral"
            : health.pending ? "pending"
                : health.status !== "UP" ? "down"
                    : attention.length > 0 ? "attention" : "up";
        const checks = attention.length === 1 ? "1 check" : `${attention.length} checks`;
        const label = {
            neutral: "CAS is running",
            pending: "Checking health...",
            up: "All systems healthy",
            attention: `Up, with ${checks} to look at`,
            down: "Something is down"
        }[state];
        const icon = {neutral: "mdi-server", pending: "mdi-timer-sand", up: "mdi-check-bold", attention: "mdi-exclamation-thick", down: "mdi-close-thick"}[state];
        const band = $("<div>", {class: `palantir-home__status is-${state}`, role: "status"})
            .append($("<span>", {class: "palantir-home__beacon", "aria-hidden": "true"}).append($("<i>", {class: `mdi ${icon}`})));
        const running = $("<p>", {class: "palantir-home__status-where"})
            .append(document.createTextNode("Running for "))
            .append($("<b>", {text: PalantirHome.duration(server.uptimeSeconds)}));
        if (server.host) {
            running.append(document.createTextNode(" on ")).append($("<code>", {text: server.host, title: "The host the login page footer shows"}));
        }
        const facts = $("<div>", {class: "palantir-home__facts"});
        [["CAS", server.version], ["Java", server.java]].filter(([, value]) => value)
            .forEach(([name, value]) => facts.append($("<span>", {class: "palantir-home__fact"})
                .append($("<small>", {text: name})).append(document.createTextNode(value))));
        band.append($("<div>", {class: "palantir-home__status-main"})
            .append($("<b>", {class: "palantir-home__status-title", text: label}))
            .append(running)
            .append(facts));
        if (attention.length > 0) {
            const tags = $("<div>", {class: "palantir-home__check-tags", "aria-label": "Health checks to look at"});
            attention.forEach(component => {
                const status = String(component.status ?? "unknown").toUpperCase();
                const failing = status === "DOWN" || status === "OUT_OF_SERVICE";
                tags.append($("<span>", {
                    class: `palantir-home__check-tag${failing ? " is-down" : ""}`,
                    text: camelcaseToTitleCase(component.name),
                    title: `${camelcaseToTitleCase(component.name)}: ${status.toLowerCase().replaceAll("_", " ")}`
                }));
            });
            band.find(".palantir-home__status-main").append(tags);
        }
        if (components.length > 0 && !health.pending) {
            const up = components.length - attention.length;
            const segments = $("<div>", {class: "palantir-home__segments", role: "img",
                "aria-label": `${up} of ${components.length} health checks up`});
            components.forEach(component => segments.append($("<i>", {
                style: `background: ${palantirHealthColor(component.status)}`,
                title: `${camelcaseToTitleCase(component.name)}: ${String(component.status ?? "unknown").toLowerCase()}`
            })));
            band.append($("<div>", {class: "palantir-home__checks"})
                .append($("<div>", {class: "palantir-home__checks-count"})
                    .append($("<strong>", {text: up})).append($("<span>", {text: `/${components.length}`})))
                .append(segments));
        }
        return band;
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
            .append($("<canvas>", {id: "homeHealthChart", role: "img", tabindex: 0, title: "Click to magnify",
                "data-palantir-magnifiable": "System Health",
                "aria-label": components.map(component => `${camelcaseToTitleCase(component.name)} ${component.status.toLowerCase()}`).join("; ")}));
        const layout = $("<div>", {class: "palantir-home__health-layout"})
            .append(PalantirHome.healthRing(health.status, components))
            .append(box);
        return [layout, PalantirHome.staleNote(health)];
    }

    /**
     * The overall health as a ring with its status inside: green when CAS is up, red when it is down, amber when it is
     * up but a check reports a warning, is unknown or was too slow to answer.
     *
     * @param status the overall status
     * @param components the health checks
     * @returns {jQuery} the ring
     */
    static healthRing(status, components) {
        const attention = components.some(component => component.status !== "UP");
        const tone = status === "UP" ? (attention ? "slow" : "up") : "down";
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

    /**
     * Draw the health chart, or update the one already drawn on the same canvas with the latest checks.
     */
    static drawHealthChart() {
        const canvas = document.getElementById("homeHealthChart");
        const health = PalantirHome.summary?.sections?.health;
        const components = health?.components;
        const usable = canvas && Array.isArray(components) && typeof Chart !== "undefined";
        const chart = PalantirHome.healthChart;
        if (chart && usable && chart.canvas === canvas) {
            const colors = components.map(component => palantirHealthColor(component.status));
            chart.data.labels = components.map(component => camelcaseToTitleCase(component.name));
            chart.data.datasets[0].data = components.map(component => Math.max(1, Number(component.millis) || 1));
            chart.data.datasets[0].backgroundColor = colors;
            chart.data.datasets[0].borderColor = colors;
            chart.update("none");
            return;
        }
        const copies = chart?.$palantirCopies;
        if (chart) {
            chart.destroy();
            PalantirHome.healthChart = null;
        }
        if (!usable) {
            return;
        }
        const colors = components.map(component => palantirHealthColor(component.status));
        PalantirHome.healthChart = new Chart(canvas, {
            type: "bar",
            data: {
                labels: components.map(component => camelcaseToTitleCase(component.name)),
                datasets: [{
                    label: "System Health",
                    data: components.map(component => Math.max(1, Number(component.millis) || 1)),
                    borderWidth: 2,
                    borderRadius: 4,
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
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: context => {
                                const component = PalantirHome.summary?.sections?.health?.components?.[context.dataIndex];
                                return component ? palantirHealthDescription(component.status, component.millis) : "";
                            }
                        }
                    }
                },
                scales: palantirHealthScales(true)
            }
        });
        if (copies?.size > 0) {
            PalantirHome.healthChart.$palantirCopies = copies;
            PalantirHome.healthChart.update("none");
        }
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

    static renderEndpoints(summary) {
        const slowest = summary.sections.endpoints.slowest ?? [];
        if (slowest.length === 0) {
            return PalantirHome.muted("No requests to measure yet, apart from Palantir's own, actuator endpoints and static files.");
        }
        const table = $("<table>", {class: "palantir-home__endpoints"})
            .append($("<thead>").append($("<tr>")
                .append($("<th>", {scope: "col", text: "URL"}))
                .append($("<th>", {scope: "col", text: "Average"}))
                .append($("<th>", {scope: "col", text: "Longest"}))
                .append($("<th>", {scope: "col", text: "Requests"}))
                .append($("<th>", {scope: "col", text: "Errors", title: "Requests that ended with a server error"}))));
        const rows = $("<tbody>");
        slowest.forEach(endpoint => rows.append($("<tr>", {class: endpoint.errors > 0 ? "is-failing" : ""})
            .append($("<td>").append($("<code>", {text: endpoint.uri, title: endpoint.uri})))
            .append($("<td>", {text: PalantirHome.seconds(endpoint.meanSeconds)}))
            .append($("<td>", {text: PalantirHome.seconds(endpoint.maxSeconds)}))
            .append($("<td>", {text: PalantirHome.number(endpoint.count)}))
            .append($("<td>", {text: PalantirHome.number(endpoint.errors)}))));
        return [table.append(rows), PalantirHome.muted("Since CAS started, slowest by average; the longest time is recent. "
            + "Palantir's own requests, actuator endpoints and static files are left out.")];
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
        if (Number.isFinite(traffic.warningsLogged)) {
            stats.append(PalantirHome.stat("Warnings logged", PalantirHome.number(traffic.warningsLogged), "since CAS started"));
        }
        if (Number.isFinite(traffic.gcOverhead)) {
            stats.append(PalantirHome.stat("GC overhead", `${(traffic.gcOverhead * 100).toFixed(1)}%`, "of CPU time, last 5 min",
                traffic.gcOverhead >= 0.1 ? "warning" : ""));
        }
        const share = usage => Number.isFinite(usage?.max) ? usage.used / usage.max : NaN;
        if (Number.isFinite(traffic.liveData?.used)) {
            const live = share(traffic.liveData);
            stats.append(PalantirHome.stat("Heap after GC", PalantirHome.bytes(traffic.liveData.used),
                Number.isFinite(live) ? `${Math.round(live * 100)}% of the old generation` : "live data in the old generation",
                live >= 0.8 ? "warning" : ""));
        }
        if (Number.isFinite(traffic.files?.used)) {
            const files = share(traffic.files);
            stats.append(PalantirHome.stat("Open files", PalantirHome.number(traffic.files.used),
                Number.isFinite(files) ? `of ${PalantirHome.number(traffic.files.max)} allowed` : "file descriptors in use",
                files >= 0.8 ? "warning" : ""));
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
                    .append(PalantirHome.changedApplication(change))
                    .append(document.createTextNode(` ${verbs[change.action] ?? change.action} by `))
                    .append($("<b>", {text: change.author}))
                    .append(change.type ? $("<small>", {text: change.type}) : null))
                .append($("<time>", {datetime: change.when, title: when.toLocaleString(), text: PalantirHome.ago(when)})));
        });
        return [list, PalantirHome.muted(cluster
            ? "Changes made on any CAS server, from the service history."
            : "Changes made on this server since it started.")];
    }

    /**
     * The name of a changed application: a link that opens it in the editor while it still exists and Palantir can
     * read applications, its name alone otherwise.
     *
     * @param change the change
     * @returns {jQuery} the name
     */
    static changedApplication(change) {
        const openable = change.exists !== false && change.action !== "deleted" && /^\d+$/.test(String(change.id ?? ""))
            && Boolean(CasActuatorEndpoints.registeredServices()) && typeof openTopApplication === "function";
        return openable
            ? $("<button>", {type: "button", class: "palantir-home__change-link", "data-home-application": String(change.id),
                text: change.name, title: `Open ${change.name}`})
            : $("<b>", {text: change.name});
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
