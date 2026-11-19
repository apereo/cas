/**
 * The application templates the JSON editor offers: those that ship with CAS and those an administrator keeps in the
 * shared templates folder.
 */
class PalantirServiceTemplates {
    static PLACEHOLDER = "...";

    static SOURCES = {
        builtin: "Built in",
        shared: "Shared"
    };


    static BASICS = new Set(["@class", "id", "name", "description", "serviceId", "evaluationOrder", "clientId", "clientSecret"]);

    /**
     * Every template, ordered by protocol, then built in before shared, then by title.
     *
     * @returns {Array<{key: string, source: string, type: string, protocol: object, title: string, summary: string, definition: object, highlight: string}>} the templates
     */
    static all() {
        const templates = PalantirDashboardConfiguration.serviceTemplates()
            .map((entry, index) => {
                try {
                    return PalantirServiceTemplates.describe(`${entry.source}-${index}`, entry.source, JSON.parse(entry.definition));
                } catch (error) {
                    console.warn("Palantir skipped an application template it could not parse", error);
                    return null;
                }
            })
            .filter(Boolean);
        const sourceOrder = Object.keys(PalantirServiceTemplates.SOURCES);
        return templates.sort((first, second) =>
            APPLICATION_TYPES.findIndex(type => type.key === first.protocol.key) - APPLICATION_TYPES.findIndex(type => type.key === second.protocol.key)
            || sourceOrder.indexOf(first.source) - sourceOrder.indexOf(second.source)
            || first.title.localeCompare(second.title));
    }

    /**
     * Find a template by its key.
     *
     * @param key the template key
     * @returns {object|undefined} the template
     */
    static find(key) {
        return PalantirServiceTemplates.all().find(template => template.key === key);
    }

    /**
     * Describe a template for a card.
     *
     * @param key the template key
     * @param source where it came from: builtin or shared
     * @param definition the application definition
     * @returns {object} the template
     */
    static describe(key, source, definition) {
        const type = String(definition?.["@class"] ?? "");
        return {
            key: key,
            source: source,
            type: type,
            protocol: applicationProtocolOf(type),
            title: String(definition?.name || "Untitled template"),
            summary: String(definition?.description ?? ""),
            definition: definition,
            highlight: PalantirServiceTemplates.highlight(definition)
        };
    }

    /**
     * The setting that sets a template apart, shown on its card: the first property beyond the basics.
     *
     * @param definition the application definition
     * @returns {string} the property and a short form of its value, or an empty string
     */
    static highlight(definition) {
        const entry = Object.entries(definition ?? {}).find(([key]) => !PalantirServiceTemplates.BASICS.has(key));
        if (!entry) {
            return "";
        }
        const [key, value] = entry;
        return `"${key}": ${PalantirServiceTemplates.shorten(value)}`;
    }

    /**
     * A short, single-line form of a JSON value.
     *
     * @param value the value
     * @returns {string} the short form
     */
    static shorten(value) {
        if (Array.isArray(value)) {
            const items = value.length === 2 && typeof value[0] === "string" && Array.isArray(value[1]) ? value[1] : value;
            return JSON.stringify(items);
        }
        if (value && typeof value === "object") {
            const entries = Object.entries(value).filter(([key]) => key !== "@class");
            if (entries.length === 0) {
                return `{ ${String(value["@class"] ?? "").split(".").pop()} }`;
            }
            const [key, nested] = entries[0];
            return `{ "${key}": ${PalantirServiceTemplates.shorten(nested)}${entries.length > 1 ? ", …" : ""} }`;
        }
        return JSON.stringify(value);
    }

    /**
     * A new definition from a template: without its numeric id, and with a new client id and secret where the
     * template only has placeholders.
     *
     * @param template the template
     * @returns {object} the definition to edit
     */
    static instantiate(template) {
        const definition = structuredClone(template.definition);
        delete definition.id;
        if (Object.hasOwn(definition, "clientId") && [PalantirServiceTemplates.PLACEHOLDER, ""].includes(definition.clientId)) {
            definition.clientId = generateRandom(24);
        }
        if (Object.hasOwn(definition, "clientSecret") && [PalantirServiceTemplates.PLACEHOLDER, ""].includes(definition.clientSecret)) {
            definition.clientSecret = generateRandom(48);
        }
        return definition;
    }
}

/**
 * The JSON editor for application definitions. A new definition opens on a start screen of templates; choosing
 * one, or starting blank, moves to a focused editor with a toolbar, a status bar and a problems strip. An existing
 * application opens straight in the editor.
 */
class PalantirServiceEditor {
    static view = "start";

    static isNew = true;

    static selected = null;

    static protocolFilter = "all";

    static query = "";

    static problemMarker = null;

    static checkTimer = null;

    static initialized = false;

    static instance = null;

    static themeDefined = false;

    static pendingText = null;

    /**
     * Bind the start screen, toolbar and footer once.
     */
    static initialize() {
        if (PalantirServiceEditor.initialized) {
            return;
        }
        PalantirServiceEditor.initialized = true;
        PalantirServiceEditor.defineTheme();

        $("#serviceTemplateCards")
            .on("click", "[data-template-key]", event => {
                PalantirServiceEditor.select(String($(event.currentTarget).data("template-key")));
            })
            .on("dblclick", "[data-template-key]", event => {
                PalantirServiceEditor.select(String($(event.currentTarget).data("template-key")));
                PalantirServiceEditor.proceed();
            })
            .on("keydown", "[data-template-key]", event => PalantirServiceEditor.moveSelection(event));
        $("#serviceTemplateProtocols").on("click", "button[data-application-type]", event => {
            PalantirServiceEditor.protocolFilter = String($(event.currentTarget).data("application-type"));
            PalantirServiceEditor.renderTemplates();
        });
        $("#serviceTemplateSearch").on("input", event => {
            PalantirServiceEditor.query = String(event.currentTarget.value ?? "");
            PalantirServiceEditor.renderTemplates();
        });

        $("#continueServiceTemplate").on("click", () => PalantirServiceEditor.proceed());
        $("#pasteServiceDefinition").on("click", () => PalantirServiceEditor.startFrom("", "Paste the application definition into the editor."));
        $("#openServiceDefinitionFile").on("click", () => $("#serviceDefinitionFileInput").val("").trigger("click"));
        $("#serviceDefinitionFileInput").on("change", event => PalantirServiceEditor.openFile(event.currentTarget.files?.[0]));

        $("#formatServiceDefinition").on("click", () => PalantirServiceEditor.format());
        $("#foldServiceDefinition").on("click", () => PalantirServiceEditor.editor().getSession().foldAll(1));
        $("#findInServiceDefinition").on("click", () => PalantirServiceEditor.editor().execCommand("find"));
        $("#chooseServiceTemplate").on("click", () => PalantirServiceEditor.backToTemplates());
        $("#serviceEditorModes").on("click", "button[data-editor-mode]", event => {
            if ($(event.currentTarget).data("editor-mode") === "wizard") {
                PalantirServiceEditor.openInWizard();
            }
        });
        $("#validateService").on("click", () => PalantirServiceEditor.validate());
        $("#copyServiceDefinition").on("click", () => copyToClipboard(PalantirServiceEditor.editor().getValue()));
        $("#serviceEditorProblemShow").on("click", () => PalantirServiceEditor.revealProblem());

        const editor = PalantirServiceEditor.editor();
        editor.session.on("change", () => {
            clearTimeout(PalantirServiceEditor.checkTimer);
            PalantirServiceEditor.checkTimer = setTimeout(() => PalantirServiceEditor.check(), 250);
        });
        editor.selection.on("changeCursor", () => PalantirServiceEditor.updatePosition());
    }

    /**
     * The Ace editor of this dialog, in the Palantir code theme.
     *
     * @returns {object} the Ace editor
     */
    static editor() {
        if (PalantirServiceEditor.instance === null) {
            PalantirServiceEditor.defineTheme();
            const editor = initializeAceEditor("serviceEditor", "json", {useWorker: false});
            editor.setTheme("ace/theme/palantir");
            editor.setOption("fontSize", "14px");
            editor.setOption("animatedScroll", false);
            PalantirServiceEditor.instance = editor;
        }
        return PalantirServiceEditor.instance;
    }

    /**
     * A code theme drawn from the Palantir colors, so the editor follows light and dark like the page around it.
     */
    static defineTheme() {
        if (typeof ace === "undefined" || PalantirServiceEditor.themeDefined) {
            return;
        }
        PalantirServiceEditor.themeDefined = true;
        ace.define("ace/theme/palantir", ["require", "exports", "module"], (require, exports) => {
            exports.isDark = false;
            exports.cssClass = "ace-palantir";
            exports.cssText = "";
        });
    }

    /**
     * The dialog was opened, possibly with a draft restored.
     *
     * @param isNew whether this is a new definition
     * @param restored whether a draft was restored into the editor
     */
    static opened(isNew, restored) {
        PalantirServiceEditor.initialize();
        PalantirServiceEditor.isNew = isNew;
        PalantirServiceEditor.selected = null;
        PalantirServiceEditor.clearProblem();
        const resumed = PalantirServiceEditor.pendingText !== null;
        PalantirServiceEditor.pendingText = null;
        if (isNew && !restored && !resumed) {
            PalantirServiceEditor.showStart();
        } else {
            PalantirServiceEditor.showEditor();
        }
    }

    /**
     * Show the template cards.
     */
    static showStart() {
        PalantirServiceEditor.view = "start";
        PalantirServiceEditor.query = "";
        $("#serviceTemplateSearch").val("");
        PalantirServiceEditor.renderProtocols();
        PalantirServiceEditor.renderTemplates();
        PalantirServiceEditor.applyView();
        $("#editServiceDialog").dialog("option", "title", "New application definition");
        setTimeout(() => $("#serviceTemplateCards [data-template-key]").first().trigger("focus"), 0);
    }

    /**
     * Show the editor.
     */
    static showEditor() {
        PalantirServiceEditor.view = "editor";
        PalantirServiceEditor.applyView();
        PalantirServiceEditor.check();
        PalantirServiceEditor.updatePosition();
        const editor = PalantirServiceEditor.editor();
        setTimeout(() => {
            editor.resize(true);
            editor.focus();
        }, 0);
    }

    static applyView() {
        const start = PalantirServiceEditor.view === "start";
        $("#editServiceDialog").toggleClass("is-start", start).toggleClass("is-editing", !start);
        $("#serviceEditorStart").prop("hidden", !start);
        $("#serviceEditorWork").prop("hidden", start);
        $("#editServiceDialog [data-editor-view]").each(function () {
            $(this).prop("hidden", $(this).data("editor-view") !== PalantirServiceEditor.view);
        });
        $("#chooseServiceTemplate").prop("hidden", !PalantirServiceEditor.isNew);
        $("#continueServiceTemplate").prop("disabled", PalantirServiceEditor.selected === null);
    }

    static renderProtocols() {
        const templates = PalantirServiceTemplates.all();
        const counts = new Map();
        templates.forEach(template => counts.set(template.protocol.key, (counts.get(template.protocol.key) ?? 0) + 1));
        if (PalantirServiceEditor.protocolFilter !== "all" && !counts.has(PalantirServiceEditor.protocolFilter)) {
            PalantirServiceEditor.protocolFilter = "all";
        }
        const choices = [{key: "all", label: "All", count: templates.length}]
            .concat(APPLICATION_TYPES.filter(type => counts.has(type.key)).map(type => ({...type, count: counts.get(type.key)})));
        $("#serviceTemplateProtocols")
            .html(choices.map(choice => `
                <button type="button" data-application-type="${choice.key}" aria-pressed="${choice.key === PalantirServiceEditor.protocolFilter}">
                    ${choice.key === "all" ? "" : `<span class="palantir-type-filter__dot is-${choice.key}" aria-hidden="true"></span>`}
                    ${escapePalantirMarkup(choice.label)}<span class="palantir-type-filter__count">${choice.count}</span>
                </button>`).join(""))
            .prop("hidden", counts.size < 2);
    }

    /**
     * Draw the template cards the protocol filter and search allow, after the Blank card.
     */
    static renderTemplates() {
        const query = PalantirServiceEditor.query.trim().toLowerCase();
        const templates = PalantirServiceTemplates.all().filter(template =>
            (PalantirServiceEditor.protocolFilter === "all" || template.protocol.key === PalantirServiceEditor.protocolFilter)
            && (query === "" || [template.title, template.summary, template.highlight, template.protocol.label, PalantirServiceTemplates.SOURCES[template.source]]
                .some(text => String(text).toLowerCase().includes(query))));
        $("#serviceTemplateProtocols button[data-application-type]").each(function () {
            $(this).attr("aria-pressed", String($(this).data("application-type") === PalantirServiceEditor.protocolFilter));
        });
        const blank = `
            <div class="palantir-template-card is-blank" role="option" tabindex="-1" data-template-key="blank"
                 aria-selected="${PalantirServiceEditor.selected === "blank"}">
                <i class="mdi mdi-plus" aria-hidden="true"></i>
                <span class="palantir-template-card__title">Blank definition</span>
                <span class="palantir-template-card__summary">An empty JSON object to write from scratch.</span>
            </div>`;
        const cards = templates.map(template => `
            <div class="palantir-template-card" role="option" tabindex="-1" data-template-key="${escapePalantirMarkup(template.key)}"
                 aria-selected="${PalantirServiceEditor.selected === template.key}">
                <span class="palantir-template-card__tags">
                    <span class="palantir-protocol-badge is-${template.protocol.key}">${escapePalantirMarkup(template.protocol.short)}</span>
                    ${template.source === "builtin" ? "" : `<span class="palantir-template-card__source is-${template.source}">${PalantirServiceTemplates.SOURCES[template.source]}</span>`}
                </span>
                <span class="palantir-template-card__title">${escapePalantirMarkup(template.title)}</span>
                <span class="palantir-template-card__summary">${escapePalantirMarkup(template.summary)}</span>
                ${template.highlight ? `<code class="palantir-template-card__peek">${escapePalantirMarkup(template.highlight)}</code>` : ""}
            </div>`).join("");
        const empty = templates.length === 0 && query !== ""
            ? `<p class="palantir-template-cards__empty">No template matches “${escapePalantirMarkup(PalantirServiceEditor.query.trim())}”.</p>`
            : "";
        $("#serviceTemplateCards").html(blank + cards + empty);
        const focusable = $("#serviceTemplateCards [aria-selected=true]").first();
        (focusable.length > 0 ? focusable : $("#serviceTemplateCards [data-template-key]").first()).attr("tabindex", "0");
        $("#continueServiceTemplate").prop("disabled", PalantirServiceEditor.selected === null);
    }

    /**
     * Mark a card as the starting point.
     *
     * @param key the template key, or blank
     */
    static select(key) {
        PalantirServiceEditor.selected = key;
        $("#serviceTemplateCards [data-template-key]").each(function () {
            const chosen = String($(this).data("template-key")) === key;
            $(this).attr("aria-selected", String(chosen)).attr("tabindex", chosen ? "0" : "-1");
        });
        $("#continueServiceTemplate").prop("disabled", false);
    }

    static moveSelection(event) {
        const cards = $("#serviceTemplateCards [data-template-key]");
        const index = cards.index(event.currentTarget);
        const columns = Math.max(1, Math.round($("#serviceTemplateCards").width() / Math.max(1, $(event.currentTarget).outerWidth(true))));
        const moves = {ArrowRight: 1, ArrowLeft: -1, ArrowDown: columns, ArrowUp: -columns, Home: -index, End: cards.length - 1 - index};
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            PalantirServiceEditor.select(String($(event.currentTarget).data("template-key")));
            if (event.key === "Enter") {
                PalantirServiceEditor.proceed();
            }
            return;
        }
        if (!Object.hasOwn(moves, event.key)) {
            return;
        }
        event.preventDefault();
        const target = cards.eq(Math.min(cards.length - 1, Math.max(0, index + moves[event.key])));
        PalantirServiceEditor.select(String(target.data("template-key")));
        target.trigger("focus");
    }

    /**
     * Move from the start screen to the editor with the chosen template.
     */
    static proceed() {
        const key = PalantirServiceEditor.selected;
        if (key === null) {
            return;
        }
        if (key === "blank") {
            PalantirServiceEditor.startFrom("{\n}", null, {row: 0, column: 1});
            return;
        }
        const template = PalantirServiceTemplates.find(key);
        if (template) {
            PalantirServiceEditor.startFrom(JSON.stringify(PalantirServiceTemplates.instantiate(template), null, 4));
        }
    }

    /**
     * Fill the editor and show it.
     *
     * @param text the definition
     * @param message a note for the status bar, if any
     * @param cursor where to put the cursor
     */
    static startFrom(text, message = null, cursor = {row: 0, column: 0}) {
        const editor = PalantirServiceEditor.editor();
        editor.setValue(text, -1);
        editor.moveCursorTo(cursor.row, cursor.column);
        PalantirServiceEditor.showEditor();
        if (message) {
            $("#serviceEditorCheck").text(message).attr("data-state", "note");
        }
    }

    /**
     * Read a JSON file chosen by the operator into the editor, without saving it.
     *
     * @param file the chosen file
     */
    static openFile(file) {
        if (!file) {
            return;
        }
        file.text()
            .then(text => {
                try {
                    PalantirServiceEditor.startFrom(JSON.stringify(JSON.parse(text), null, 4));
                } catch (error) {
                    PalantirServiceEditor.startFrom(text);
                }
            })
            .catch(error => Swal.fire({icon: "error", titleText: `${file.name} could not be read`, text: String(error?.message ?? error)}));
    }

    /**
     * Go back to the template cards, asking first when the editor holds something.
     */
    static async backToTemplates() {
        const text = PalantirServiceEditor.editor().getValue().trim();
        if (text !== "" && text !== "{\n}" && text !== "{}") {
            const result = await Swal.fire({
                title: "Start from a template?",
                text: "The definition in the editor is replaced by the template you choose.",
                icon: "question",
                showDenyButton: true,
                confirmButtonText: "Choose a template",
                denyButtonText: "Keep editing"
            });
            if (!result.isConfirmed) {
                return;
            }
        }
        PalantirServiceEditor.selected = null;
        PalantirServiceEditor.clearProblem();
        PalantirServiceEditor.showStart();
    }

    /**
     * Pretty-print the definition when it is valid JSON.
     */
    static format() {
        const editor = PalantirServiceEditor.editor();
        try {
            const cursor = editor.getCursorPosition();
            editor.setValue(JSON.stringify(JSON.parse(editor.getValue()), null, 4), -1);
            editor.moveCursorTo(cursor.row, cursor.column);
        } catch (error) {
            PalantirServiceEditor.check();
            PalantirServiceEditor.revealProblem();
        }
    }

    /**
     * Check the editor holds JSON, and update the identity, the status bar and the problem marker.
     */
    static check() {
        const editor = PalantirServiceEditor.editor();
        const text = editor.getValue();
        const status = $("#serviceEditorCheck");
        $("#serviceEditorLines").text(`${editor.session.getLength()} ${editor.session.getLength() === 1 ? "line" : "lines"}`);
        PalantirServiceEditor.updateDirty();
        if (text.trim() === "") {
            status.text("Empty").attr("data-state", "note");
            PalantirServiceEditor.updateIdentity(null);
            PalantirServiceEditor.clearProblem();
            return;
        }
        try {
            const definition = JSON.parse(text);
            status.text("Valid JSON").attr("data-state", "ok");
            PalantirServiceEditor.updateIdentity(definition);
            if ($("#serviceEditorProblem").data("kind") === "syntax") {
                PalantirServiceEditor.clearProblem();
            }
        } catch (error) {
            const position = PalantirServiceEditor.toPosition(error, text);
            const message = String(error.message).replace(/\s*\(line \d+ column \d+\)\s*$/, "").replace(/ in JSON at position \d+$/, "");
            status.text(`Line ${position.row + 1}: ${message}`).attr("data-state", "error");
            PalantirServiceEditor.showProblem("Not valid JSON", `${message} on line ${position.row + 1}.`, position.row, "syntax");
        }
    }

    /**
     * Where a JSON parse error points, from the browser's message.
     *
     * @param error the parse error
     * @param text the text that was parsed
     * @returns {{row: number, column: number}} the zero-based position
     */
    static toPosition(error, text) {
        const message = String(error?.message ?? "");
        const lineColumn = message.match(/line (\d+) column (\d+)/);
        if (lineColumn) {
            return {row: Number(lineColumn[1]) - 1, column: Number(lineColumn[2]) - 1};
        }
        const offset = message.match(/position (\d+)/);
        if (offset) {
            const before = text.slice(0, Number(offset[1])).split("\n");
            return {row: before.length - 1, column: before.at(-1).length};
        }
        return {row: Math.max(0, text.split("\n").length - 1), column: 0};
    }

    /**
     * Show the protocol, name and id of the definition in the toolbar and the dialog title.
     *
     * @param definition the parsed definition, or null
     */
    static updateIdentity(definition) {
        const type = definition?.["@class"];
        const protocol = type ? applicationProtocolOf(type) : null;
        $("#serviceEditorBadge")
            .attr("class", `palantir-protocol-badge is-${protocol?.key ?? "other"}`)
            .text(protocol?.short ?? "JSON")
            .attr("title", protocol?.label ?? "Application type not set");
        const name = definition?.name ? String(definition.name) : PalantirServiceEditor.isNew ? "New application" : "Application";
        $("#serviceEditorName").text(name);
        const id = definition?.id !== undefined && !PalantirServiceEditor.isNew ? `id ${definition.id}` : "";
        $("#serviceEditorId").text(id).prop("hidden", id === "");
        if (PalantirServiceEditor.view === "editor") {
            $("#editServiceDialog").dialog("option", "title", PalantirServiceEditor.isNew ? "New application definition" : `Edit ${name}`);
        }
    }

    static updatePosition() {
        const position = PalantirServiceEditor.editor().getCursorPosition();
        $("#serviceEditorPosition").text(`Ln ${position.row + 1}, Col ${position.column + 1}`);
    }

    static updateDirty() {
        const dirty = PalantirServiceEditGuard.state?.editor ? PalantirServiceEditGuard.isDirty("editor") : false;
        $("#serviceEditorDirty").prop("hidden", !dirty);
    }

    /**
     * Show a problem under the editor and mark its line.
     *
     * @param title a short title
     * @param message what is wrong
     * @param row the zero-based line, or null when no line is known
     * @param kind syntax for JSON errors, server for what CAS reported
     */
    static showProblem(title, message, row = null, kind = "server") {
        const editor = PalantirServiceEditor.editor();
        PalantirServiceEditor.clearMarkers();
        const strip = $("#serviceEditorProblem").data("kind", kind).data("row", row).attr("data-state", "error").prop("hidden", false);
        strip.find(".palantir-json-editor__problem-icon").attr("class", "mdi mdi-alert-circle-outline palantir-json-editor__problem-icon");
        strip.find(".palantir-json-editor__problem-title").text(title);
        strip.find(".palantir-json-editor__problem-text").text(message);
        $("#serviceEditorProblemShow").prop("hidden", row === null);
        if (row !== null) {
            const Range = ace.require("ace/range").Range;
            PalantirServiceEditor.problemMarker = editor.session.addMarker(new Range(row, 0, row, 1), "palantir-json-editor__problem-line", "fullLine");
            editor.session.setAnnotations([{row: row, column: 0, text: message, type: "error"}]);
        }
        editor.resize();
    }

    /**
     * Show that CAS can read the definition.
     *
     * @param message what to say
     */
    static showSuccess(message) {
        PalantirServiceEditor.clearMarkers();
        const strip = $("#serviceEditorProblem").data("kind", "success").data("row", null).attr("data-state", "ok").prop("hidden", false);
        strip.find(".palantir-json-editor__problem-icon").attr("class", "mdi mdi-check-circle-outline palantir-json-editor__problem-icon");
        strip.find(".palantir-json-editor__problem-title").text("Valid");
        strip.find(".palantir-json-editor__problem-text").text(message);
        $("#serviceEditorProblemShow").prop("hidden", true);
        PalantirServiceEditor.editor().resize();
    }

    static clearMarkers() {
        const editor = PalantirServiceEditor.editor();
        if (PalantirServiceEditor.problemMarker !== null) {
            editor.session.removeMarker(PalantirServiceEditor.problemMarker);
            PalantirServiceEditor.problemMarker = null;
        }
        editor.session.clearAnnotations();
    }

    static clearProblem() {
        PalantirServiceEditor.clearMarkers();
        $("#serviceEditorProblem").data("kind", null).data("row", null).prop("hidden", true);
        PalantirServiceEditor.editor().resize();
    }

    static revealProblem() {
        const row = $("#serviceEditorProblem").data("row");
        if (row === null || row === undefined) {
            return;
        }
        const editor = PalantirServiceEditor.editor();
        editor.scrollToLine(row, true, true);
        editor.gotoLine(row + 1, 0, true);
        editor.focus();
    }

    /**
     * The line of a property named in a message from CAS, so the problem can be marked where it is.
     *
     * @param reason what CAS reported
     * @returns {number|null} the zero-based line, or null
     */
    static rowOf(reason) {
        const text = String(reason ?? "");
        const lineColumn = text.match(/line:\s*(\d+)/i);
        if (lineColumn) {
            return Number(lineColumn[1]) - 1;
        }
        const lines = PalantirServiceEditor.editor().getValue().split("\n");
        const names = [...text.matchAll(/["'“]([A-Za-z@][\w@.]*)["'”]/g)].map(match => match[1]);
        for (const name of names) {
            const key = lines.findIndex(line => line.includes(`"${name.split(".").pop()}"`) && /"\s*:/.test(line));
            if (key >= 0) {
                return key;
            }
        }
        for (const name of names) {
            const value = lines.findIndex(line => line.includes(`"${name}"`));
            if (value >= 0) {
                return value;
            }
        }
        return null;
    }

    /**
     * Ask CAS whether it can read the definition, and show the answer under the editor.
     */
    static validate() {
        PalantirServiceEditor.check();
        if ($("#serviceEditorProblem").data("kind") === "syntax") {
            PalantirServiceEditor.revealProblem();
            return;
        }
        if (!CasActuatorEndpoints.registeredServices()) {
            return;
        }
        $.ajax({
            url: `${CasActuatorEndpoints.registeredServices()}/validate`,
            type: "POST",
            contentType: "application/json",
            data: PalantirServiceEditor.editor().getValue(),
            success: response => {
                if (response === false || response === "false") {
                    PalantirServiceEditor.showProblem("Not valid", "CAS cannot read this definition.");
                    return;
                }
                PalantirServiceEditor.showSuccess("CAS can read this definition. That does not prove it behaves as intended.");
            },
            error: xhr => {
                const reason = xhr?.status === 400 ? xhr.responseJSON?.error : null;
                if (reason) {
                    PalantirServiceEditor.showProblem("Not valid", `CAS cannot read this definition: ${reason}`, PalantirServiceEditor.rowOf(reason));
                    PalantirServiceEditor.revealProblem();
                } else {
                    displayBanner(xhr);
                }
            }
        });
    }

    /**
     * Continue in the wizard with what the editor holds. The wizard does not have a field for every setting, so
     * once it has filled itself in, any setting it left out is listed, with a way back to the JSON editor.
     */
    static async openInWizard() {
        const editor = PalantirServiceEditor.editor();
        const text = editor.getValue();
        let definition;
        try {
            definition = JSON.parse(text);
        } catch (error) {
            PalantirServiceEditor.check();
            PalantirServiceEditor.revealProblem();
            return;
        }
        if (!definition?.["@class"]) {
            PalantirServiceEditor.showProblem("The application type is missing", "Add an \"@class\" property, or start from a template, to open the definition in the wizard.", 0);
            return;
        }
        const isNew = PalantirServiceEditor.isNew;
        const editorKey = PalantirServiceEditGuard.key("editor", isNew ? null : definition.id);
        const original = isNew ? null : PalantirServiceEditGuard.originals.get(editorKey) ?? null;
        const unsaved = PalantirServiceEditGuard.isDirty("editor");
        const editorState = PalantirServiceEditGuard.handOver("editor");
        getEditServiceDialogInstance().close();
        await openRegisteredServiceWizardDialog(definition, {...(isNew ? {asNew: true} : {}), unsaved: unsaved});
        if (editorState) {
            PalantirServiceEditGuard.removeDraft(editorState.key);
        }
        const wizardState = PalantirServiceEditGuard.state.wizard;
        if (original !== null && wizardState && !wizardState.fromDraft) {
            PalantirServiceEditGuard.originals.set(wizardState.key, original);
        }
        const dropped = await PalantirServiceEditor.droppedByWizard(definition);
        await PalantirServiceEditor.offerJsonEditorWhenDropped(dropped, "Back to the JSON editor", handed => {
            PalantirServiceEditor.reopen(PalantirServiceEditor.carry(definition, handed.text, handed.modeled, handed.maps, handed.offered), isNew);
            const kept = handed.original ?? original;
            if (!isNew && kept !== null) {
                PalantirServiceEditGuard.originals.set(editorKey, kept);
            }
        });
    }

    /**
     * Open an application straight in the wizard, as from the applications table. The wizard does not have a field
     * for every setting, so once it has filled itself in, any setting it left out is listed, with a way to continue
     * in the JSON editor instead.
     *
     * @param definition the application as read from the server, or a copy of it to save as a new one
     * @param options the wizard's options, such as duplicate for a copy
     */
    static async openApplicationInWizard(definition, options = {}) {
        const isNew = options.duplicate === true || options.asNew === true;
        await openRegisteredServiceWizardDialog(definition, options);
        const dropped = await PalantirServiceEditor.droppedByWizard(definition);
        await PalantirServiceEditor.offerJsonEditorWhenDropped(dropped, "Continue in the JSON editor", handed => {
            PalantirServiceEditor.reopen(PalantirServiceEditor.carry(definition, handed.text, handed.modeled, handed.maps, handed.offered), isNew);
            if (!isNew) {
                const editorKey = PalantirServiceEditGuard.key("editor", definition.id);
                if (handed.original === null) {
                    PalantirServiceEditGuard.remember(editorKey, definition);
                } else {
                    PalantirServiceEditGuard.originals.set(editorKey, handed.original);
                }
            }
        });
    }

    /**
     * List the settings the wizard left out, and offer to close the wizard and edit the JSON instead.
     *
     * What the wizard holds, a resumed draft included, goes to the JSON editor with the saved definition the edit
     * started from, so saving it still notices a change someone else made since; the wizard's draft is removed only
     * once the JSON editor has it, and the JSON editor then asks before closing without saving it.
     *
     * @param dropped the dotted paths of the settings the wizard left out
     * @param confirmText the text of the button that goes to the JSON editor
     * @param openJsonEditor opens the JSON editor once the wizard is closed, given the definition the wizard generated
     * as text, the settings the wizard has fields for, and the saved definition the edit started from, if known
     * @returns {Promise<void>} resolved once the operator chose
     */
    static async offerJsonEditorWhenDropped(dropped, confirmText, openJsonEditor) {
        if (dropped.length === 0) {
            return;
        }
        const shown = dropped.slice(0, 12);
        const result = await Swal.fire({
            icon: "warning",
            title: "The wizard does not cover every setting",
            html: `
                <p class="palantir-json-editor__note">These settings have no place in the wizard, and saving from the wizard leaves them out:</p>
                <ul class="palantir-json-editor__dropped">${shown.map(path => `<li>${palantirCode(path)}</li>`).join("")}</ul>
                ${dropped.length > shown.length ? `<p class="palantir-json-editor__note">And ${dropped.length - shown.length} more.</p>` : ""}`,
            showDenyButton: true,
            confirmButtonText: confirmText,
            denyButtonText: "Continue in the wizard"
        });
        if (result.isConfirmed) {
            const handed = {
                text: PalantirServiceEditGuard.value("wizard"),
                modeled: PalantirServiceEditor.wizardSettings(),
                maps: PalantirServiceEditor.wizardMaps(),
                offered: PalantirServiceEditor.wizardChoices(),
                original: null
            };
            const state = PalantirServiceEditGuard.handOver("wizard");
            if (state) {
                handed.original = PalantirServiceEditGuard.originals.get(state.key) ?? null;
            }
            editServiceWizardDialog?.close();
            openJsonEditor(handed);
            if (state) {
                PalantirServiceEditGuard.removeDraft(state.key);
            }
        }
    }

    /**
     * The settings the wizard has fields for, for the type of application it holds, as the dotted paths its fields
     * write to, such as <code>accessStrategy.enabled</code>.
     *
     * @returns {string[]} the paths
     */
    static wizardSettings() {
        const className = $("#editServiceWizardForm").data("service-class-name");
        return [...new Set($("#editServiceWizardForm [data-param-name]").toArray()
            .filter(element => {
                const scoped = $(element).closest("[class*='class-']");
                return scoped.length === 0 || scoped.hasClass(`class-${className}`) || scoped.hasClass("always-show");
            })
            .map(PalantirServiceEditor.wizardPath)
            .filter(Boolean))];
    }

    /**
     * The dotted path a wizard field writes to. A switch names its path among its other settings rather than on its
     * own, and the wizard reads it from there once it opens, so the path is taken the way the wizard itself reads it.
     *
     * @param element the field
     * @returns {string} the path, or an empty text when the field writes nowhere
     */
    static wizardPath(element) {
        return String($(element).data("param-name") ?? "");
    }

    /**
     * The types the wizard offers for each part it chooses from a list, such as the kinds of access strategy, by the
     * dotted path of that part.
     *
     * @returns {Object<string, string[]>} the values of the choices, by path
     */
    static wizardChoices() {
        return $("#editServiceWizardForm select[data-param-name]").toArray()
            .reduce((choices, element) => {
                const path = PalantirServiceEditor.wizardPath(element);
                if (path.length > 0) {
                    choices[path] = [...(choices[path] ?? []), ...Array.from(element.options, option => option.value)];
                }
                return choices;
            }, {});
    }

    /**
     * The settings the wizard edits as a whole map, such as properties or required attributes: every entry of such a
     * map comes from the wizard, so an entry removed there stays removed.
     *
     * @returns {string[]} the dotted paths of the maps
     */
    static wizardMaps() {
        return [...new Set($("#editServiceWizardForm [id$='MapContainer'] [data-param-name]").toArray()
            .map(PalantirServiceEditor.wizardPath)
            .filter(Boolean))];
    }

    /**
     * The definition to continue with in the JSON editor: what the wizard generated, in its order, with what it removed
     * left out, plus the settings of the definition it was opened with that the wizard has no field for. A value the
     * wizard has a field for is taken from the wizard alone, and so is a map the wizard edits as a whole and a part
     * whose type the wizard changed, such as another kind of access strategy. A part whose type the wizard only
     * chooses, such as the access strategy, keeps the settings inside it the wizard has no field for. A part the wizard
     * leaves out altogether stays as it was, unless its type is one the wizard offers, in which case leaving it out
     * was a choice made in the wizard.
     *
     * @param definition the definition the wizard was opened with
     * @param wizardText the definition the wizard generated
     * @param modeled the dotted paths the wizard's fields write to
     * @param maps the dotted paths of the maps the wizard edits as a whole
     * @param offered the types the wizard offers for each part it chooses from a list, by dotted path
     * @returns {string} the definition as JSON
     */
    static carry(definition, wizardText, modeled = [], maps = [], offered = {}) {
        let generated;
        try {
            generated = JSON.parse(wizardText);
        } catch (error) {
            return JSON.stringify(definition, null, 4);
        }
        const plain = value => value !== null && typeof value === "object" && !Array.isArray(value);
        const owned = path => modeled.includes(path);
        const within = path => modeled.some(name => name.startsWith(`${path}.`));
        const whole = path => maps.includes(path);
        const merge = (over, base, path) => {
            if (!plain(base) || !plain(over) || (base["@class"] ?? null) !== (over["@class"] ?? null)) {
                return over;
            }
            const merged = {...over};
            Object.entries(over).forEach(([key, value]) => {
                const nested = path === "" ? key : `${path}.${key}`;
                if (plain(value) && plain(base[key]) && !whole(nested)) {
                    merged[key] = merge(value, base[key], nested);
                }
            });
            Object.entries(base).forEach(([key, value]) => {
                const nested = path === "" ? key : `${path}.${key}`;
                if (key in over || whole(nested)) {
                    return;
                }
                if (owned(nested)) {
                    if (plain(value) && !(offered[nested] ?? []).includes(value["@class"])) {
                        merged[key] = value;
                    }
                    return;
                }
                if (!within(nested)) {
                    merged[key] = value;
                } else if (plain(value)) {
                    const kept = merge(value["@class"] === undefined ? {} : {"@class": value["@class"]}, value, nested);
                    if (Object.keys(kept).some(name => name !== "@class")) {
                        merged[key] = kept;
                    }
                }
            });
            return merged;
        };
        return JSON.stringify(merge(generated, definition, ""), null, 4);
    }

    /**
     * The settings of a definition the wizard left out once it has filled itself in.
     *
     * @param definition the definition handed to the wizard
     * @returns {Promise<string[]>} the dotted paths of the settings left out
     */
    static async droppedByWizard(definition) {
        const settle = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));
        await settle(300);
        for (let attempt = 0; attempt < 100 && typeof isPopulatingWizard !== "undefined" && isPopulatingWizard; attempt++) {
            await settle(100);
        }
        await settle(400);
        let generated;
        try {
            generated = JSON.parse(ace.edit("wizardServiceEditor").getValue());
        } catch (error) {
            return [];
        }
        const kept = new Set(PalantirServiceEditor.paths(generated));
        return PalantirServiceEditor.paths(definition).filter(path => !kept.has(path));
    }

    /**
     * The dotted paths of every setting in a definition, without type markers or empty values. Lists are followed
     * item by item, such as <code>contacts[3].email</code>, so a list the wizard shortens shows what it left out; the
     * type CAS writes before the items of a collection is skipped.
     *
     * @param value a definition or part of one
     * @param prefix the path so far
     * @returns {string[]} the paths
     */
    static paths(value, prefix = "") {
        if (value === null || value === undefined || value === "") {
            return [];
        }
        if (Array.isArray(value)) {
            const typed = value.length === 2 && typeof value[0] === "string" && /^(java|org)\./.test(value[0]) && Array.isArray(value[1]);
            const items = typed ? value[1] : value;
            if (items.length === 0) {
                return prefix === "" ? [] : [prefix];
            }
            return items.flatMap((item, index) => PalantirServiceEditor.paths(item, `${prefix}[${index}]`));
        }
        if (typeof value !== "object") {
            return prefix === "" ? [] : [prefix];
        }
        const entries = Object.entries(value).filter(([key]) => key !== "@class");
        if (entries.length === 0) {
            return prefix === "" ? [] : [prefix];
        }
        return entries.flatMap(([key, nested]) => PalantirServiceEditor.paths(nested, prefix === "" ? key : `${prefix}.${key}`));
    }

    /**
     * Open the editor again on a definition, straight in the editor view.
     *
     * @param text the definition
     * @param isNew whether it is a new definition
     */
    static reopen(text, isNew) {
        PalantirServiceEditor.pendingText = text;
        $("#editServiceDialog").attr("newService", String(isNew));
        PalantirServiceEditor.editor().setValue(text, -1);
        getEditServiceDialogInstance().open();
    }
}
