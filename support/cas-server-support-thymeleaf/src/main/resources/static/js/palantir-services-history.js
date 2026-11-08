/**
 * The change history of a registered service: its versions, a side-by-side diff of a version against the
 * current definition or the version before it, and restoring a version.
 *
 * Both sides of a diff are the service definitions written as JSON with their keys sorted, so only real
 * changes show and never the order in which properties happened to be written. The older side is on the
 * left: the selected version against the current definition, or the version before it against the selected one.
 */
class PalantirServiceHistory {
    static CONTEXT_LINES = 3;

    static serviceId = null;

    static versions = [];

    static current = null;

    static selected = 0;

    static rows = [];

    /**
     * Copy a value with the keys of every object sorted, so that two definitions with the same content
     * write the same JSON.
     *
     * @param value the value to copy
     * @returns {*} the copy
     */
    static sortKeys(value) {
        if (Array.isArray(value)) {
            return value.map(entry => PalantirServiceHistory.sortKeys(entry));
        }
        if (value !== null && typeof value === "object") {
            return Object.keys(value).sort().reduce((sorted, key) => {
                sorted[key] = PalantirServiceHistory.sortKeys(value[key]);
                return sorted;
            }, {});
        }
        return value;
    }

    /**
     * Write a definition as JSON with sorted keys, one property per line.
     *
     * @param entity the definition
     * @returns {string} the JSON
     */
    static canonical(entity) {
        return JSON.stringify(PalantirServiceHistory.sortKeys(entity ?? {}), null, 2);
    }

    /**
     * Show the date of a version the way the browser's locale writes it. The server sends the commit date
     * as a local date and time, either as text or as an array of its fields.
     *
     * @param value the commit date
     * @returns {string} the formatted date
     */
    static when(value) {
        if (Array.isArray(value)) {
            const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value;
            return new Date(year, month - 1, day, hour, minute, second).toLocaleString();
        }
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? String(value ?? "") : date.toLocaleString();
    }

    /**
     * Read one endpoint as JSON.
     *
     * @param url the endpoint
     * @returns {Promise<any>} the parsed body
     */
    static get(url) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: url,
                type: "GET",
                headers: {"Accept": "application/json"},
                success: response => resolve(response),
                error: xhr => reject(xhr)
            });
        });
    }

    /**
     * Build the dialog once: a full-window jQuery UI dialog like the service editor, with the versions on
     * the left, the differences on the right, and Restore this version and Close in its footer.
     *
     * @returns {jQuery} the dialog element
     */
    static dialog() {
        const element = $("#viewEntityHistoryDialog");
        if (!element.hasClass("ui-dialog-content")) {
            element.removeClass("hide").dialog({
                autoOpen: false,
                modal: true,
                closeOnEscape: true,
                draggable: false,
                resizable: false,
                classes: {"ui-dialog": "ui-corner-all palantir-history-dialog"},
                buttons: [
                    {
                        text: "Restore this version",
                        "class": "palantir-history-restore",
                        click: () => {
                            const version = PalantirServiceHistory.versions[PalantirServiceHistory.selected];
                            if (version) {
                                PalantirServiceHistory.restore(PalantirServiceHistory.serviceId, version.id);
                            }
                        }
                    },
                    {
                        text: "Close",
                        click: () => element.dialog("close")
                    }
                ],
                close: () => $(window).off("resize.serviceHistory")
            });
            const compare = $("#entityHistoryCompare");
            if (!compare.data("ui-selectmenu")) {
                compare.selectmenu();
            }
            compare.off("change selectmenuchange").on("change selectmenuchange", () => PalantirServiceHistory.render());
            $("#entityHistoryTable tbody").off("click.history").on("click.history", "tr", event => {
                const data = $("#entityHistoryTable").DataTable().row(event.currentTarget).data();
                if (data) {
                    PalantirServiceHistory.select(data.index);
                }
            });
            $("#entityHistoryDiff").off("click.history").on("click.history", "[data-diff-fold]", event => {
                PalantirServiceHistory.unfold(event.currentTarget);
            });
        }
        return element;
    }

    /**
     * Size the dialog to the window, leaving a small margin, like the service editor beside it.
     */
    static size() {
        const element = $("#viewEntityHistoryDialog");
        if (!element.hasClass("ui-dialog-content") || !element.dialog("isOpen")) {
            return;
        }
        const margin = 24;
        element.dialog("option", {
            width: Math.max(320, $(window).width() - margin * 2),
            height: Math.max(320, $(window).height() - margin * 2)
        });
        element.dialog("option", "position", {my: "center", at: "center", of: window});
    }

    /**
     * Open the history of a service: read its versions and its current definition, list the versions,
     * and select the latest version.
     *
     * @param serviceId the numeric id of the service
     * @returns {Promise<void>} resolved once the dialog is open
     */
    static async open(serviceId) {
        const historyUrl = `${CasActuatorEndpoints.entityHistory()}/registeredServices/${encodeURIComponent(serviceId)}`;
        const currentUrl = CasActuatorEndpoints.registeredServices()
            ? `${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(serviceId)}`
            : null;
        await Promise.all([
            PalantirServiceHistory.get(historyUrl),
            currentUrl ? PalantirServiceHistory.get(currentUrl) : Promise.resolve(null)
        ])
            .then(([history, current]) => PalantirServiceHistory.show(serviceId, history, current))
            .catch(xhr => {
                console.error("Error fetching the service history:", xhr);
                displayBanner(xhr);
            });
    }

    /**
     * List the versions of a service and open the dialog on the latest version, compared with the version
     * before it.
     *
     * @param serviceId the numeric id of the service
     * @param history the versions, latest first
     * @param current the current definition, if it could be read
     */
    static show(serviceId, history, current) {
        if (!Array.isArray(history) || history.length === 0) {
            Swal.fire("No History!", "There are no changes recorded for this application definition.", "info");
            return;
        }
        PalantirServiceHistory.serviceId = serviceId;
        PalantirServiceHistory.versions = history.map(item => ({
            id: String(item.id),
            date: PalantirServiceHistory.when(item.date),
            text: PalantirServiceHistory.canonical(item.entity)
        }));
        PalantirServiceHistory.current = current ? PalantirServiceHistory.canonical(current) : null;

        const table = $("#entityHistoryTable").DataTable();
        table.clear();
        PalantirServiceHistory.versions.forEach((version, index) => {
            const tags = [];
            if (index === 0) {
                tags.push("<span class=\"palantir-tag\">Latest</span>");
            }
            if (version.text === PalantirServiceHistory.current) {
                tags.push("<span class=\"palantir-tag is-current\">Current</span>");
            }
            table.row.add({
                0: `<code>${escapePalantirMarkup(version.id)}</code> ${tags.join(" ")}`,
                1: escapePalantirMarkup(version.date),
                2: index,
                serviceId: serviceId,
                revisionId: version.id,
                index: index
            });
        });
        table.draw();

        const element = PalantirServiceHistory.dialog();
        element.dialog("option", "title", `Change history of service ${serviceId}`);
        element.dialog("open");
        PalantirServiceHistory.size();
        $(window).off("resize.serviceHistory").on("resize.serviceHistory", () => PalantirServiceHistory.size());
        table.columns.adjust();
        PalantirServiceHistory.select(0);
        $("#entityHistoryCompare-button").trigger("focus");
    }

    /**
     * Select a version, mark its row, and draw its differences.
     *
     * @param index the position of the version, the latest being 0
     */
    static select(index) {
        PalantirServiceHistory.selected = index;
        const table = $("#entityHistoryTable").DataTable();
        table.rows().indexes().each(position => {
            const selected = table.row(position).data().index === index;
            $(table.row(position).node()).toggleClass("is-selected", selected).attr("aria-selected", String(selected));
        });
        PalantirServiceHistory.render();
    }

    /**
     * Draw the differences of the selected version, older side on the left: the version before it against
     * the selected version, or the selected version against the current definition.
     */
    static render() {
        const version = PalantirServiceHistory.versions[PalantirServiceHistory.selected];
        if (!version) {
            return;
        }
        const againstCurrent = $("#entityHistoryCompare").val() === "current";
        const previous = PalantirServiceHistory.versions[PalantirServiceHistory.selected + 1];
        const selected = {label: `Version ${version.id} · ${version.date}`, text: version.text};
        const [left, right] = againstCurrent
            ? [selected, {label: "Current definition", text: PalantirServiceHistory.current}]
            : [previous ? {label: `Version ${previous.id} · ${previous.date}`, text: previous.text} : null, selected];
        const restore = $(".palantir-history-restore");
        if (restore.data("ui-button")) {
            restore.button("option", "disabled", version.text === PalantirServiceHistory.current);
        }

        if (!left || left.text === null || right.text === null) {
            $("#entityHistorySummary").text(againstCurrent
                ? "The current definition could not be read."
                : "This is the oldest recorded version; there is nothing before it to compare with.");
            $("#entityHistoryDiff").html(PalantirServiceHistory.table(left?.label ?? "", right.label,
                PalantirServiceHistory.compare("", right.text ?? "")));
            return;
        }
        const rows = PalantirServiceHistory.compare(left.text, right.text);
        const added = rows.filter(row => row.right?.changed).length;
        const removed = rows.filter(row => row.left?.changed).length;
        const counts = `${added} ${added === 1 ? "line" : "lines"} added, ${removed} ${removed === 1 ? "line" : "lines"} removed`;
        const noChanges = againstCurrent
            ? "No differences: this version matches the current definition."
            : "No differences from the previous version.";
        $("#entityHistorySummary").text(added + removed === 0
            ? noChanges
            : `${counts} ${againstCurrent ? "since this version." : "in this version."}`);
        $("#entityHistoryDiff").html(PalantirServiceHistory.table(left.label, right.label, rows));
    }

    /**
     * Pair the lines of two texts into side-by-side rows. Unchanged lines sit on both sides; a block of
     * removed lines followed by added lines is paired line by line, and whatever is left over faces an
     * empty cell.
     *
     * @param left the older text
     * @param right the newer text
     * @returns {Array<object>} the rows, each with a left and a right line or null
     */
    static compare(left, right) {
        const rows = [];
        const parts = Diff.diffLines(left, right);
        let leftNumber = 1;
        let rightNumber = 1;
        const lines = part => part.value.replace(/\n$/, "").split("\n");
        for (let index = 0; index < parts.length; index++) {
            const part = parts[index];
            if (!part.added && !part.removed) {
                for (const text of lines(part)) {
                    rows.push({
                        left: {number: leftNumber++, text, changed: false},
                        right: {number: rightNumber++, text, changed: false}
                    });
                }
                continue;
            }
            const removedLines = part.removed ? lines(part) : [];
            const next = parts[index + 1];
            const addedLines = part.added ? lines(part) : next?.added ? lines(next) : [];
            if (part.removed && next?.added) {
                index++;
            }
            const count = Math.max(removedLines.length, addedLines.length);
            for (let line = 0; line < count; line++) {
                rows.push({
                    left: line < removedLines.length ? {number: leftNumber++, text: removedLines[line], changed: true} : null,
                    right: line < addedLines.length ? {number: rightNumber++, text: addedLines[line], changed: true} : null
                });
            }
        }
        return rows;
    }

    /**
     * Write one side of a changed line, marking the words that differ from the line it faces.
     *
     * @param line the line on this side
     * @param other the line it faces
     * @param side removed for the left side, added for the right
     * @returns {string} the markup
     */
    static words(line, other, side) {
        if (!other) {
            return escapePalantirMarkup(line.text);
        }
        const [before, after] = side === "added" ? [other.text, line.text] : [line.text, other.text];
        return Diff.diffWordsWithSpace(before, after)
            .filter(part => side === "added" ? !part.removed : !part.added)
            .map(part => {
                const text = escapePalantirMarkup(part.value);
                const changed = side === "added" ? part.added : part.removed;
                return changed ? `<mark class="palantir-diff__word">${text}</mark>` : text;
            })
            .join("");
    }

    /**
     * Write a row of the diff.
     *
     * @param row the row
     * @returns {string} the markup
     */
    static row(row) {
        const cell = (line, other, side) => line
            ? `<td class="palantir-diff__number${line.changed ? ` is-${side}` : ""}">${line.number}</td>
               <td class="palantir-diff__line${line.changed ? ` is-${side}` : ""}"><span class="palantir-diff__text">${line.changed
                ? PalantirServiceHistory.words(line, other, side === "added" ? "added" : "removed")
                : escapePalantirMarkup(line.text)}</span></td>`
            : "<td class=\"palantir-diff__number\"></td><td class=\"palantir-diff__line is-empty\"></td>";
        return `<tr>${cell(row.left, row.right, "removed")}${cell(row.right, row.left, "added")}</tr>`;
    }

    /**
     * Write the diff table. Runs of unchanged lines away from any change are folded into one row that
     * unfolds when clicked.
     *
     * @param leftLabel the heading of the left side
     * @param rightLabel the heading of the right side
     * @param rows the rows
     * @returns {string} the markup
     */
    static table(leftLabel, rightLabel, rows) {
        PalantirServiceHistory.rows = rows;
        const context = PalantirServiceHistory.CONTEXT_LINES;
        const near = rows.map(() => false);
        rows.forEach((row, index) => {
            if (row.left?.changed || row.right?.changed || !row.left || !row.right) {
                for (let other = Math.max(0, index - context); other <= Math.min(rows.length - 1, index + context); other++) {
                    near[other] = true;
                }
            }
        });
        const hasChanges = near.some(Boolean);
        const body = [];
        let index = 0;
        while (index < rows.length) {
            if (!hasChanges || near[index]) {
                body.push(PalantirServiceHistory.row(rows[index]));
                index++;
                continue;
            }
            let end = index;
            while (end < rows.length && !near[end]) {
                end++;
            }
            const count = end - index;
            body.push(`<tr class="palantir-diff__fold"><td colspan="4">
                <button type="button" class="palantir-diff__unfold" data-diff-fold="${index}:${end}">
                    <i class="mdi mdi-unfold-more-horizontal" aria-hidden="true"></i>${count} unchanged ${count === 1 ? "line" : "lines"}
                </button></td></tr>`);
            index = end;
        }
        return `
            <table class="palantir-diff__table">
                <colgroup><col class="palantir-diff__number-column"><col><col class="palantir-diff__number-column"><col></colgroup>
                <thead><tr>
                    <th scope="col" colspan="2">${escapePalantirMarkup(leftLabel)}</th>
                    <th scope="col" colspan="2">${escapePalantirMarkup(rightLabel)}</th>
                </tr></thead>
                <tbody>${body.join("")}</tbody>
            </table>`;
    }

    /**
     * Replace a folded run of unchanged lines with the lines themselves.
     *
     * @param button the fold's button
     */
    static unfold(button) {
        const [start, end] = button.dataset.diffFold.split(":").map(Number);
        const markup = PalantirServiceHistory.rows.slice(start, end).map(row => PalantirServiceHistory.row(row)).join("");
        $(button).closest("tr").replaceWith(markup);
    }

    /**
     * Restore a version after the operator confirms it, then reload the applications.
     *
     * @param serviceId the numeric id of the service
     * @param revisionId the version to restore
     */
    static restore(serviceId, revisionId) {
        Swal.fire({
            title: "Restore this version?",
            text: `Version ${revisionId} will replace the current definition of service ${serviceId}.`,
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Restore",
            showLoaderOnConfirm: true,
            allowOutsideClick: () => !Swal.isLoading(),
            preConfirm: () => $.ajax({
                url: `${CasActuatorEndpoints.entityHistory()}/registeredServices/${encodeURIComponent(serviceId)}/restore/${encodeURIComponent(revisionId)}`,
                type: "POST",
                headers: {"Accept": "application/json"}
            }).catch(xhr => {
                console.error("Error restoring service:", xhr);
                Swal.showValidationMessage("Unable to restore this version. Please try again or check your access to service history.");
            })
        }).then(result => {
            if (result.isConfirmed) {
                const element = $("#viewEntityHistoryDialog");
                if (element.hasClass("ui-dialog-content")) {
                    element.dialog("close");
                }
                fetchServices(() => updateNavigationSidebar());
                Swal.fire("Service Restored", `Version ${revisionId} is now the current definition of service ${serviceId}.`, "success");
            }
        });
    }
}
