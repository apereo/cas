const cas = require("../../cas.js");
const assert = require("assert");

const AUDIT_LOG = "https://localhost:8443/cas/actuator/auditLog";
const EVENTS = "https://localhost:8443/cas/actuator/events";
const JSON_HEADERS = {"Content-Type": "application/json", "Accept": "application/json"};

/**
 * Reads a date the actuator returned, either as an ISO local date-time in UTC or as the array Jackson writes.
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

async function auditLog(query) {
    return JSON.parse(await cas.doRequest(`${AUDIT_LOG}?${query}`, "GET", JSON_HEADERS, 200));
}

(async () => {
    const startedAt = Date.now();
    await cas.doDelete(EVENTS);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        for (let attempt = 1; attempt <= 2; attempt++) {
            await cas.gotoLogin(page);
            await cas.loginWith(page, "casuser", "wrong-password");
            await cas.assertCookie(page, false);
        }
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
    } finally {
        await cas.closeBrowser(browser);
    }

    await cas.logg("Every audit record of the principal is kept in DynamoDb, not only the last one");
    const items = (await cas.dynamoDbScan("DynamoDbCasAuditRecords")).filter((item) => item.principal.S === "casuser");
    const failures = items.filter((item) => item.actionPerformed.S === "AUTHENTICATION_FAILED");
    assert(failures.length >= 2, `Both failed attempts must be kept, found ${failures.length}`);
    assert(items.some((item) => item.actionPerformed.S === "AUTHENTICATION_SUCCESS"));
    assert(items.every((item) => Number(item.whenActionWasPerformed.S) >= startedAt - 60000),
        "Records must be stamped with the time, in milliseconds, they were written");

    await cas.logg("Audit records read back from DynamoDb carry the time they were written");
    const recent = await auditLog("interval=PT10M&principal=casuser&count=100");
    assert(recent.filter((record) => record.actionPerformed === "AUTHENTICATION_FAILED").length >= 2);
    assert(recent.some((record) => record.actionPerformed === "AUTHENTICATION_SUCCESS"));
    for (const record of recent) {
        const performed = parseLocalDateTime(record.whenActionWasPerformed);
        assert(Math.abs(performed - Date.now()) < 10 * 60 * 1000, `Unexpected audit time ${record.whenActionWasPerformed}`);
    }
    const history = await auditLog("principal=casuser&count=100");
    assert(history.length >= recent.length);

    await cas.logg("Authentication events are kept in DynamoDb and exposed through the events endpoint");
    const events = JSON.parse(await cas.doRequest(EVENTS, "GET", JSON_HEADERS, 200));
    assert(events.length >= 3, `Expected at least three events, found ${events.length}`);
    const stored = await cas.dynamoDbScan("DynamoDbCasEvents");
    assert(stored.length >= events.length, `DynamoDb holds ${stored.length} events while CAS reports ${events.length}`);
    assert(stored.some((item) => item.principal.S === "casuser"));

    await cas.doDelete(EVENTS);
    assert((await cas.dynamoDbScan("DynamoDbCasEvents")).length === 0, "Removing all events must empty the table");
    assert(JSON.parse(await cas.doRequest(EVENTS, "GET", JSON_HEADERS, 200)).length === 0);
})();
