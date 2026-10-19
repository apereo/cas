const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

const AUDIT_LOG = "https://localhost:8443/cas/actuator/auditLog";

/**
 * Reads a date CAS sent, either as an ISO local date-time in UTC or as the array Jackson writes for timestamps.
 *
 * @param value the date
 * @returns milliseconds since the epoch
 */
function parseLocalDateTime(value) {
    if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0, second = 0, nanos = 0] = value;
        return Date.UTC(year, month - 1, day, hour, minute, second, Math.floor(nanos / 1000000));
    }
    return Date.parse(String(value).endsWith("Z") ? value : `${value}Z`);
}

(async () => {
    const records = [];
    const userAgents = [];
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.post("/audit", (req, res) => {
        const record = JSON.parse(req.body);
        cas.log(`Audit record: ${record.principal} ${record.actionPerformed} ${record.auditableResource}`);
        records.push(record);
        userAgents.push(req.get("userAgent"));
        res.sendStatus(200);
    });
    app.get("/audit", (req, res) => {
        const since = parseLocalDateTime(req.query.date);
        const count = req.query.count === undefined ? records.length : Number(req.query.count);
        const found = records
            .filter((record) => parseLocalDateTime(record.whenActionWasPerformed) >= since)
            .filter((record) => req.query.principal === undefined || record.principal === req.query.principal)
            .slice(-count);
        cas.log(`Returning ${found.length} audit record(s) since ${req.query.date}`);
        res.status(200).json(found);
    });
    app.delete("/audit", (req, res) => {
        records.length = 0;
        res.sendStatus(200);
    });
    const server = await new Promise((resolve) => {
        const started = app.listen(5614, () => resolve(started));
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page, "unknown", "Mellon");
        await cas.assertCookie(page, false);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);

        await cas.logg("Every audit record reaches the REST backend as it happens");
        const actions = records.map((record) => record.actionPerformed);
        await cas.log(actions);
        assert(records.some((record) => record.principal === "unknown" && record.actionPerformed === "AUTHENTICATION_FAILED"));
        assert(records.some((record) => record.principal === "casuser" && record.actionPerformed === "AUTHENTICATION_SUCCESS"));
        assert(records.some((record) => record.principal === "casuser" && record.actionPerformed === "TICKET_GRANTING_TICKET_CREATED"));
        assert(userAgents.every((agent) => agent !== undefined && agent.length > 0), "Each record carries the user agent header");

        await cas.logg("The auditLog endpoint reads the records back from the REST backend");
        const log = JSON.parse(await cas.doRequest(`${AUDIT_LOG}?count=100`, "GET",
            {"Accept": "application/json", "Content-Type": "application/json"}, 200));
        assert(log.some((record) => record.principal === "casuser" && record.actionPerformed === "AUTHENTICATION_SUCCESS"));
        assert(log.some((record) => record.principal === "unknown" && record.actionPerformed === "AUTHENTICATION_FAILED"));

        const filtered = JSON.parse(await cas.doRequest(`${AUDIT_LOG}?count=100&principal=casuser&actionPerformed=TICKET_GRANTING_TICKET_CREATED`, "GET",
            {"Accept": "application/json", "Content-Type": "application/json"}, 200));
        assert(filtered.length > 0);
        assert(filtered.every((record) => record.principal === "casuser" && record.actionPerformed === "TICKET_GRANTING_TICKET_CREATED"));
    } finally {
        await cas.closeBrowser(browser);
        server.close();
    }
})();
