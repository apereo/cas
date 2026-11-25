/* The startup timeline and the modules of CAS, as the System page shows them. */

/**
 * The Application Startup page: the steps Spring recorded while CAS started, as a tree or sorted by duration or name,
 * with filters by duration and name.
 *
 * The steps are read once and drawn once, however often the page is opened. A startup can record thousands of steps,
 * so the rows are built as text in batches, each added to the page in one go, with a progress bar saying how far
 * along it is; a row's children are opened and closed through one listener on the list rather than one per row, and
 * rows out of view are not laid out until they are scrolled to.
 */
class PalantirStartupTimeline {
    static BATCH = 800;

    static steps = null;

    static loading = null;

    static drawing = 0;

    static rows = [];

    static filter = "all";

    static search = "";

    static order = "index";

    static searchTimer = null;

    /**
     * Read the steps the first time the page is shown and draw them; later, leave the page as it is.
     *
     * @returns {Promise<void>} resolved once the steps are drawn
     */
    static async show() {
        if (!CasActuatorEndpoints.startup()) {
            return;
        }
        PalantirStartupTimeline.bind();
        PalantirStartupTimeline.loading ??= PalantirStartupTimeline.load();
        await PalantirStartupTimeline.loading;
    }

    static async load() {
        PalantirStartupTimeline.progress("Reading the startup steps…", null);
        try {
            const response = await $.get(CasActuatorEndpoints.startup());
            const events = response?.timeline?.events ?? [];
            PalantirStartupTimeline.steps = events.map(event => {
                const ms = PalantirStartupTimeline.millis(event.duration);
                return {
                    id: event.startupStep.id,
                    parentId: event.startupStep.parentId,
                    name: String(event.startupStep.name ?? ""),
                    search: String(event.startupStep.name ?? "").toLowerCase(),
                    tags: event.startupStep.tags ?? [],
                    ms,
                    speed: PalantirStartupTimeline.speed(ms)
                };
            });
        } catch (xhr) {
            console.error("The startup steps could not be read", xhr);
            PalantirStartupTimeline.loading = null;
            PalantirStartupTimeline.progress(null);
            $("#startupTimelineList").html("<p class=\"text-muted p-3\">Could not load startup timeline data.</p>");
            displayBanner(xhr);
            updateNavigationSidebar();
            return;
        }
        const steps = PalantirStartupTimeline.steps;
        if (steps.length === 0) {
            PalantirStartupTimeline.progress(null);
            $("#startupTimelineList").html("<p class=\"text-muted p-3\">No startup steps available. Ensure <code>BufferingApplicationStartup</code> is configured.</p>");
            updateNavigationSidebar();
            return;
        }
        const counts = {fastest: 0, fast: 0, medium: 0, slow: 0, slowest: 0};
        steps.forEach(step => counts[step.speed]++);
        Object.entries(counts).forEach(([speed, count]) => $(`#count-${speed}`).text(count.toLocaleString()));
        await PalantirStartupTimeline.draw();
        updateNavigationSidebar();
    }

    static millis(duration) {
        const match = String(duration ?? "").match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?/);
        if (!match) {
            return 0;
        }
        return ((Number(match[1] || 0) * 60 + Number(match[2] || 0)) * 60 + Number(match[3] || 0)) * 1000;
    }

    static speed(ms) {
        if (ms < 10) {
            return "fastest";
        }
        if (ms < 50) {
            return "fast";
        }
        if (ms < 200) {
            return "medium";
        }
        return ms < 1000 ? "slow" : "slowest";
    }

    static duration(ms) {
        return ms < 1000 ? `${Math.round(ms)} ms` : `${(ms / 1000).toFixed(2)} s`;
    }

    /**
     * Show how far along reading or drawing the steps is, or put the progress bar away.
     *
     * @param message what is happening, or null when done
     * @param percent how far along, or null while it cannot be told
     */
    static progress(message, percent = null) {
        const box = $("#startupProgress");
        if (message === null) {
            box.prop("hidden", true);
            return;
        }
        box.prop("hidden", false);
        const track = box.find(".palantir-progress__track").toggleClass("is-busy", percent === null);
        if (percent === null) {
            track.removeAttr("aria-valuenow").find(".palantir-progress__bar").css("width", "");
        } else {
            track.attr("aria-valuenow", Math.round(percent)).find(".palantir-progress__bar").css("width", `${percent}%`);
        }
        $("#startupProgressDetail").text(message);
    }

    /**
     * The steps to draw at the top of the list: the tree's roots, each with its children, or every step on its own
     * when sorted by duration or name.
     *
     * @returns {object[]} the top rows, each with its children
     */
    static roots() {
        const steps = PalantirStartupTimeline.steps;
        const order = PalantirStartupTimeline.order;
        if (order !== "index") {
            const sorted = [...steps];
            if (order === "duration-desc") {
                sorted.sort((left, right) => right.ms - left.ms);
            } else if (order === "duration-asc") {
                sorted.sort((left, right) => left.ms - right.ms);
            } else {
                sorted.sort((left, right) => left.name.localeCompare(right.name));
            }
            return sorted.map(step => ({step, children: []}));
        }
        const nodes = new Map(steps.map(step => [step.id, {step, children: []}]));
        const roots = [];
        steps.forEach(step => {
            const parent = step.parentId ? nodes.get(step.parentId) : null;
            (parent ? parent.children : roots).push(nodes.get(step.id));
        });
        return roots;
    }

    /**
     * The row of one step, with an empty place for its children, which are added to it as they are drawn.
     *
     * @param node the step and its children
     * @param depth how deep in the tree it is
     * @returns {string} the row
     */
    static row(node, depth) {
        const step = node.step;
        const children = node.children.length;
        const tags = step.tags.map(tag => `<span class="startup-tag">${escapePalantirMarkup(tag.key)}=${escapePalantirMarkup(tag.value)}</span>`).join("");
        return `<div class="startup-step-row depth-${depth}${children > 0 ? "" : " is-leaf"}" data-duration-class="${step.speed}" data-step-name="${escapePalantirMarkup(step.search)}">`
            + `<div class="startup-step-main" style="padding-left:${depth * 28}px">`
            + (children > 0
                ? "<button type=\"button\" class=\"startup-expand-btn\" data-expanded=\"true\" title=\"Collapse\">&#9660;</button>"
                : "<span class=\"startup-expand-spacer\"></span>")
            + `<div class="startup-step-info"><span class="startup-step-name">${escapePalantirMarkup(step.name)}</span>`
            + `<span class="startup-step-index">#${escapePalantirMarkup(step.id)}</span>`
            + (children > 0 ? `<span class="startup-children-count">${children} ${children === 1 ? "child" : "children"}</span>` : "")
            + `</div><span class="startup-duration-badge ${step.speed}">${PalantirStartupTimeline.duration(step.ms)}</span></div>`
            + (tags ? `<div class="startup-step-tags" style="padding-left:${depth * 28 + 36}px">${tags}</div>` : "")
            + (children > 0 ? "<div class=\"startup-children\"></div>" : "")
            + "</div>";
    }

    /**
     * Wait for the page to be drawn before the next batch; a page in a hidden browser tab, which is not drawn, carries
     * on all the same.
     *
     * @returns {Promise<void>} resolved once the batch is on screen or a moment has passed
     */
    static frame() {
        return new Promise(resolve => {
            const timer = setTimeout(resolve, 50);
            requestAnimationFrame(() => {
                clearTimeout(timer);
                resolve();
            });
        });
    }

    /**
     * Draw the steps in batches, saying how many are drawn so far. A step's children are added to its row as further
     * batches, so a step with thousands of steps under it, such as the refresh of the application context, does not
     * make one batch of the whole tree; each level is drawn before the next, so the top of the tree shows first. A
     * newer drawing, as when the order changes, stops an older one.
     *
     * @returns {Promise<void>} resolved once every step is drawn
     */
    static async draw() {
        const drawing = ++PalantirStartupTimeline.drawing;
        const list = document.getElementById("startupTimelineList");
        const total = PalantirStartupTimeline.steps.length;
        list.innerHTML = "";
        PalantirStartupTimeline.rows = [];
        const queue = [{container: list, depth: 0, nodes: PalantirStartupTimeline.roots(), next: 0}];
        let drawn = 0;
        while (queue.length > 0) {
            let budget = PalantirStartupTimeline.BATCH;
            while (queue.length > 0 && budget > 0) {
                const group = queue[0];
                const nodes = group.nodes.slice(group.next, group.next + budget);
                group.next += nodes.length;
                budget -= nodes.length;
                drawn += nodes.length;
                const before = group.container.children.length;
                group.container.insertAdjacentHTML("beforeend", nodes.map(node => PalantirStartupTimeline.row(node, group.depth)).join(""));
                nodes.forEach((node, index) => {
                    if (node.children.length > 0) {
                        const children = group.container.children[before + index].querySelector(":scope > .startup-children");
                        queue.push({container: children, depth: group.depth + 1, nodes: node.children, next: 0});
                    }
                });
                if (group.next >= group.nodes.length) {
                    queue.shift();
                }
            }
            PalantirStartupTimeline.progress(`Showing ${drawn.toLocaleString()} of ${total.toLocaleString()} startup steps…`, drawn / total * 100);
            await PalantirStartupTimeline.frame();
            if (drawing !== PalantirStartupTimeline.drawing) {
                return;
            }
        }
        PalantirStartupTimeline.rows = [...list.querySelectorAll(".startup-step-row")];
        PalantirStartupTimeline.progress(null);
        PalantirStartupTimeline.apply();
    }

    /**
     * Show the rows that match the duration and the name asked for.
     */
    static apply() {
        const filter = PalantirStartupTimeline.filter;
        const search = PalantirStartupTimeline.search;
        PalantirStartupTimeline.rows.forEach(row => {
            const shown = (filter === "all" || row.dataset.durationClass === filter) && (!search || row.dataset.stepName.includes(search));
            row.style.display = shown ? "" : "none";
        });
    }

    static expand(button, expanded) {
        button.dataset.expanded = String(expanded);
        button.innerHTML = expanded ? "&#9660;" : "&#9658;";
        button.title = expanded ? "Collapse" : "Expand";
        const children = button.closest(".startup-step-row")?.querySelector(":scope > .startup-children");
        if (children) {
            children.style.display = expanded ? "" : "none";
        }
    }

    static bind() {
        $("#startupTimelineList").off("click.startup").on("click.startup", ".startup-expand-btn", event => {
            PalantirStartupTimeline.expand(event.currentTarget, event.currentTarget.dataset.expanded !== "true");
        });
        $("#startupStepFilter").off("input").on("input", function () {
            const value = String($(this).val() ?? "").toLowerCase();
            clearTimeout(PalantirStartupTimeline.searchTimer);
            PalantirStartupTimeline.searchTimer = setTimeout(() => {
                PalantirStartupTimeline.search = value;
                PalantirStartupTimeline.apply();
            }, 120);
        });
        $("#startupExpandAll").off("click").on("click", () => {
            document.querySelectorAll("#startupTimelineList .startup-expand-btn").forEach(button => PalantirStartupTimeline.expand(button, true));
        });
        $("#startupCollapseAll").off("click").on("click", () => {
            document.querySelectorAll("#startupTimelineList .startup-expand-btn").forEach(button => PalantirStartupTimeline.expand(button, false));
        });
        $("#startupDurationFilters").off("click", ".startup-duration-filter").on("click", ".startup-duration-filter", function () {
            PalantirStartupTimeline.filter = $(this).data("filter");
            $("#startupDurationFilters .startup-duration-filter").removeClass("active");
            $(this).addClass("active");
            PalantirStartupTimeline.apply();
        });
        $("#startupSortSelect").off("change").on("change", function () {
            PalantirStartupTimeline.order = $(this).val();
            if (PalantirStartupTimeline.steps?.length > 0) {
                PalantirStartupTimeline.draw();
            }
        });
    }
}

/**
 * The CAS modules this server runs, as the casModules endpoint lists them, one card each, with a filter by text and by
 * kind of module. The list does not change while CAS runs, so it is read once.
 */
class PalantirModules {
    static modules = null;
    static loading = null;
    static kind = "all";
    static GROUP = "org.apereo.cas";
    static FORMAT_KEY = "PalantirModuleDependencyFormat";

    static KINDS = [
        {key: "support", label: "Support", icon: "mdi-puzzle-outline"},
        {key: "core", label: "Core", icon: "mdi-cube-outline"},
        {key: "api", label: "API", icon: "mdi-api"},
        {key: "webapp", label: "Web Application", icon: "mdi-web"},
        {key: "other", label: "Other", icon: "mdi-package-variant-closed"}
    ];

    static kindOf(name) {
        const match = String(name ?? "").match(/^cas-server-(support|core|api|webapp)\b/);
        return match ? match[1] : "other";
    }

    static shortName(name) {
        return String(name ?? "").replace(/^cas-server-(support|core|api|webapp)-/, "") || String(name ?? "");
    }

    static load() {
        if (PalantirModules.modules !== null) {
            return Promise.resolve();
        }
        if (PalantirModules.loading === null) {
            PalantirModules.bind();
            PalantirModules.loading = new Promise(resolve => {
                $.get(CasActuatorEndpoints.casModules(), response => {
                    PalantirModules.modules = (Array.isArray(response) ? response : [])
                        .filter(module => module && module.name)
                        .filter((module, index, modules) => modules.findIndex(other => other.name === module.name
                            && other.version === module.version) === index)
                        .sort((first, second) => String(first.name).localeCompare(String(second.name)));
                    PalantirModules.render();
                    resolve();
                }).fail((xhr, status, error) => {
                    console.error("Error fetching the CAS modules:", error);
                    $("#casModulesCards").html("<p class=\"palantir-empty-state\">The modules could not be read just now.</p>");
                    PalantirModules.loading = null;
                    resolve();
                });
            });
        }
        return PalantirModules.loading;
    }

    /**
     * How a module is declared as a dependency, so it can be looked for in a Gradle or Maven build. CAS modules share
     * one group, and the version is the one this server runs.
     *
     * @param {object} module the module
     * @param {string} format gradle or maven
     * @returns {string} the declaration
     */
    static declaration(module, format) {
        const version = module.version || "${casVersion}";
        return format === "maven"
            ? `<dependency>\n    <groupId>${PalantirModules.GROUP}</groupId>\n    <artifactId>${module.name}</artifactId>\n    <version>${version}</version>\n</dependency>`
            : `implementation "${PalantirModules.GROUP}:${module.name}:${version}"`;
    }

    static format() {
        try {
            return localStorage.getItem(PalantirModules.FORMAT_KEY) === "maven" ? "maven" : "gradle";
        } catch (error) {
            return "gradle";
        }
    }

    static showDependency(card, format) {
        const module = (PalantirModules.modules ?? []).find(entry => entry.name === card.dataset.module);
        if (!module) {
            return;
        }
        const panel = $(card).find(".palantir-modules__dependency");
        panel.find("[data-module-format]").each((index, button) => button.setAttribute("aria-pressed", String(button.dataset.moduleFormat === format)));
        panel.find("code").text(PalantirModules.declaration(module, format));
    }

    static bind() {
        $("#casModulesCatalog").off("click.palantirModules").on("click.palantirModules", () => {
            const tabs = $("#system-tabs");
            tabs.tabs("option", "active", tabs.find("ul li a[href='#casfeaturestab-tab']").parent().index());
        });
        $("#casModulesCards").off("click.palantirModules")
            .on("click.palantirModules", "[data-module-action='dependency']", event => {
                const button = event.currentTarget;
                const card = button.closest("[data-module]");
                const panel = card.querySelector(".palantir-modules__dependency");
                const open = panel.hidden;
                panel.hidden = !open;
                card.classList.toggle("is-expanded", open);
                button.setAttribute("aria-expanded", String(open));
                if (open) {
                    PalantirModules.showDependency(card, PalantirModules.format());
                    card.scrollIntoView({block: "nearest", behavior: "smooth"});
                }
            })
            .on("click.palantirModules", "[data-module-format]", event => {
                const format = event.currentTarget.dataset.moduleFormat;
                try {
                    localStorage.setItem(PalantirModules.FORMAT_KEY, format);
                } catch (error) {
                    console.debug("The dependency format is not remembered:", error);
                }
                PalantirModules.showDependency(event.currentTarget.closest("[data-module]"), format);
            })
            .on("click.palantirModules", "[data-module-action='copy']", event => {
                copyToClipboard(event.currentTarget.closest(".palantir-modules__dependency").querySelector("code").textContent);
                const button = $(event.currentTarget);
                button.find("span").text("Copied");
                setTimeout(() => button.find("span").text("Copy"), 1500);
            });
        $("#casModulesFilter").off("input.palantirModules").on("input.palantirModules", () => PalantirModules.render());
        $("#casModulesKinds").off("click.palantirModules").on("click.palantirModules", "[data-module-kind]", event => {
            PalantirModules.kind = event.currentTarget.dataset.moduleKind;
            PalantirModules.render();
        });
    }

    static render() {
        const modules = PalantirModules.modules ?? [];
        const query = String($("#casModulesFilter").val() ?? "").trim().toLowerCase();
        $("#casModulesCatalog").toggleClass("hide", !CasActuatorEndpoints.casFeatures()
            || $("#casfeaturestab").parent().is(".hide, .d-none"));
        const counts = {all: modules.length};
        modules.forEach(module => {
            const kind = PalantirModules.kindOf(module.name);
            counts[kind] = (counts[kind] ?? 0) + 1;
        });
        if (PalantirModules.kind !== "all" && !counts[PalantirModules.kind]) {
            PalantirModules.kind = "all";
        }
        const escape = escapePalantirMarkup;
        $("#casModulesKinds").html([{key: "all", label: "All"}, ...PalantirModules.KINDS]
            .filter(kind => counts[kind.key])
            .map(kind => `
                <button type="button" class="palantir-modules__kind" data-module-kind="${kind.key}"
                        aria-pressed="${PalantirModules.kind === kind.key}">${escape(kind.label)} <span>${counts[kind.key]}</span></button>`)
            .join(""));
        const shown = modules.filter(module => (PalantirModules.kind === "all" || PalantirModules.kindOf(module.name) === PalantirModules.kind)
            && (query === "" || [module.name, module.version, module.description]
                .some(value => String(value ?? "").toLowerCase().includes(query))));
        $("#casModulesCount").text(shown.length === modules.length
            ? `${modules.length} ${modules.length === 1 ? "module" : "modules"}`
            : `${shown.length} of ${modules.length} modules`);
        if (modules.length === 0) {
            $("#casModulesCards").html("<p class=\"palantir-empty-state\">This server reports no CAS modules.</p>");
            return;
        }
        if (shown.length === 0) {
            $("#casModulesCards").html("<p class=\"palantir-empty-state\">No module matches that filter.</p>");
            return;
        }
        $("#casModulesCards").html(shown.map(module => PalantirModules.card(module)).join(""));
    }

    static card(module) {
        const escape = escapePalantirMarkup;
        const kind = PalantirModules.KINDS.find(entry => entry.key === PalantirModules.kindOf(module.name));
        const description = String(module.description ?? "").trim();
        return `
            <article class="palantir-record-card palantir-modules__card is-${kind.key}" aria-label="${escape(module.name)}"
                     data-module="${escape(module.name)}">
                <header class="palantir-record-card__head">
                    <span class="palantir-protocol-badge is-module-${kind.key}" title="${escape(kind.label)}"><i class="mdi ${kind.icon}" aria-hidden="true"></i></span>
                    <div class="palantir-record-card__title">
                        <h4 title="${escape(module.name)}">${escape(PalantirModules.shortName(module.name))}</h4>
                        <p title="${escape(module.name)}">${escape(module.name)}</p>
                    </div>
                </header>
                ${description !== "" && description !== module.name
        ? `<p class="palantir-modules__description">${escape(description)}</p>` : ""}
                <footer class="palantir-modules__footer">
                    ${module.version ? `<span class="palantir-modules__version"><i class="mdi mdi-tag-outline" aria-hidden="true"></i>${escape(module.version)}</span>` : "<span></span>"}
                    <button type="button" class="palantir-modules__toggle" data-module-action="dependency" aria-expanded="false"
                            aria-label="Show how ${escape(module.name)} is declared in Gradle or Maven">
                        <i class="mdi mdi-code-braces" aria-hidden="true"></i>Dependency
                    </button>
                </footer>
                <div class="palantir-modules__dependency" hidden>
                    <div class="palantir-modules__formats" role="group" aria-label="Build tool">
                        <button type="button" data-module-format="gradle" aria-pressed="false">Gradle</button>
                        <button type="button" data-module-format="maven" aria-pressed="false">Maven</button>
                        <button type="button" class="palantir-modules__copy" data-module-action="copy" title="Copy">
                            <i class="mdi mdi-content-copy" aria-hidden="true"></i><span>Copy</span>
                        </button>
                    </div>
                    <pre><code></code></pre>
                </div>
            </article>`;
    }
}
