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
