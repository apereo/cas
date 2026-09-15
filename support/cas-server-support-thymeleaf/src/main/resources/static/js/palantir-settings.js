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
            1: `<code>${endpoint.value}</code>`
        });
    });
    palantirActuatorsTable.draw();
}
