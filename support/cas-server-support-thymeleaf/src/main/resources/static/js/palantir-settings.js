class PalantirSettings {
    constructor(data = {}) {
        const parsedInterval = Number(data.refreshInterval);
        this.refreshInterval = Number.isFinite(parsedInterval)
            ? parsedInterval * 1000
            : 10 * 1000;
    }
}

function palantirSettings() {
    try {
        const raw = JSON.parse(localStorage.getItem("PalantirSettings")) ?? {};
        return new PalantirSettings(raw);
    } catch {
        return new PalantirSettings();
    }
}

/**
 * Remove every preference Palantir keeps in this browser, after the operator confirms, and load the dashboard again.
 * Local and session storage of this site are cleared and its IndexedDB databases deleted; the sign-in session is a
 * cookie and is left alone.
 *
 * @returns {Promise<void>} resolved once the operator declined or the page is reloading
 */
async function resetPalantirPreferences() {
    const result = await Swal.fire({
        titleText: "Reset All Preferences?",
        text: "Everything Palantir keeps in this browser is removed: the theme, zen mode, refresh interval, last pages and tabs, "
            + "views and filters, editor and wizard options, and drafts of unsaved edits. You stay signed in, and "
            + "Palantir reloads with its defaults.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Yes",
        cancelButtonText: "Cancel",
        focusCancel: true
    });
    if (!result.isConfirmed) {
        return;
    }
    for (const storage of [window.localStorage, window.sessionStorage]) {
        try {
            storage.clear();
        } catch (error) {
            console.warn("Palantir could not clear the browser storage", error);
        }
    }
    try {
        const databases = typeof indexedDB?.databases === "function" ? await indexedDB.databases() : [];
        databases.filter(database => database.name).forEach(database => indexedDB.deleteDatabase(database.name));
    } catch (error) {
        console.warn("Palantir could not remove the browser databases", error);
    }
    location.reload();
}

document.addEventListener("click", event => {
    if (event.target instanceof Element && event.target.closest("#resetPalantirPreferences")) {
        resetPalantirPreferences();
    }
});

let palantirActuatorsTableBuilt = false;

/**
 * Build the table of resolved actuator endpoints shown in the settings dialog.
 *
 * The dialog is opened on demand, so the table is built the first time it is needed rather than
 * during dashboard startup, and rebuilding is refused because DataTables cannot be initialized
 * twice on the same element.
 *
 * @returns {Promise<void>} resolved once the table exists
 */
async function initializePalantirActuatorsTable() {
    if (palantirActuatorsTableBuilt || $("#palantirActuatorsTable").length === 0) {
        return;
    }
    palantirActuatorsTableBuilt = true;
    const palantirActuatorsTable = $("#palantirActuatorsTable").DataTable({
        pageLength: 5,
        columns: [
            { width: "10%", targets: 0},
            { width: "90%", targets: 1}
        ],
        order: [[1, "asc"]],
        lengthChange: false,
        drawCallback: settings => {
            $("#palantirActuatorsTable tr").addClass("mdc-data-table__row");
            $("#palantirActuatorsTable td").addClass("mdc-data-table__cell");
        }
    });


    CasActuatorEndpoints.all().forEach(endpoint => {
        const endpointAvailable = endpoint.value !== undefined && endpoint.value !== null;
        if (!endpointAvailable) {
            return;
        }
        palantirActuatorsTable.row.add({
            0: "<i class='mdc-tab__icon mdi mdi-check-circle' aria-hidden='true'></i>"
                + "<span class='palantir-sr-only'>Available</span>",
            1: `${palantirCode(endpoint.value)}`
        });
    });
    palantirActuatorsTable.draw();
}

/**
 * Zen mode: the CAS header, the Palantir navigation bar and the footer are put away so the page has the whole window.
 * Shift+Z turns it on and off, and the choice is kept in the browser like the theme. The navigation is moved out of
 * view rather than removed, since the page shortcuts and the search find their destinations through it.
 */
class PalantirZenMode {
    static STORAGE_KEY = "PalantirZenMode";

    static enabled() {
        return document.documentElement.hasAttribute("data-palantir-zen");
    }

    static set(on, announce = true) {
        document.documentElement.toggleAttribute("data-palantir-zen", on);
        try {
            if (on) {
                localStorage.setItem(PalantirZenMode.STORAGE_KEY, "true");
            } else {
                localStorage.removeItem(PalantirZenMode.STORAGE_KEY);
            }
        } catch (error) {
            console.debug("Zen mode is kept for this visit only", error);
        }
        $("#palantirZenExit").prop("hidden", !on);
        if (announce && typeof notyf !== "undefined") {
            notyf.dismissAll();
            notyf.success(on ? "Zen mode is on. Press Shift+Z to bring the navigation back." : "Zen mode is off.");
        }
        $(window).trigger("resize");
    }

    static toggle() {
        PalantirZenMode.set(!PalantirZenMode.enabled());
    }

    static initialize() {
        if ($("#palantirZenExit").length === 0) {
            $("body").append($("<button>", {
                type: "button",
                id: "palantirZenExit",
                class: "palantir-zen-exit",
                title: "Leave zen mode (Shift+Z)",
                "aria-label": "Leave zen mode and show the navigation",
                hidden: !PalantirZenMode.enabled()
            }).append($("<i>", {class: "mdi mdi-arrow-collapse-vertical", "aria-hidden": "true"}))
                .append($("<span>", {text: "Zen"}))
                .on("click", () => PalantirZenMode.set(false)));
        }
        $(document).off("keydown.zen").on("keydown.zen", event => {
            const target = event.target;
            const typing = target instanceof HTMLElement
                && (target.isContentEditable || ["INPUT", "TEXTAREA", "SELECT"].includes(target.tagName));
            if (typing || event.ctrlKey || event.metaKey || event.altKey || !event.shiftKey) {
                return;
            }
            if (event.code === "KeyZ" || event.key === "Z") {
                event.preventDefault();
                PalantirZenMode.toggle();
            }
        });
    }
}
