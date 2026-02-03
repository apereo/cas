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

async function initializePalantirActuatorsTable() {
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
        palantirActuatorsTable.row.add({
            0: endpointAvailable
                ? "<i class='mdc-tab__icon mdi mdi-check-circle' aria-hidden='true'></i>"
                : "<i class='mdc-tab__icon mdi mdi-close-octagon' aria-hidden='true'></i>",
            1: `<code>${endpointAvailable ? endpoint.value : `Endpoint ${endpoint.name} is unavailable.`}</code>`
        });
    });
    palantirActuatorsTable.draw();
}
