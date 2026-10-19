const cas = require("../../cas.js");
const assert = require("assert");

const BUCKET = "security-lake-logs";

async function awslocal(args) {
    return cas.dockerExec("localstack", ["awslocal", "--output", "json", ...args]);
}

/*
 * Reads back every object the delivery stream wrote into its bucket. Firehose concatenates the records it delivers,
 * so each object must still split into one JSON audit record per line.
 */
async function deliveredRecords() {
    const listing = await awslocal(["s3api", "list-objects-v2", "--bucket", BUCKET]);
    const objects = listing.length === 0 ? [] : (JSON.parse(listing).Contents ?? []);
    const records = [];
    for (const object of objects) {
        const content = await cas.dockerExec("localstack", ["awslocal", "s3", "cp", `s3://${BUCKET}/${object.Key}`, "-"]);
        content.split("\n")
            .filter((line) => line.trim().length > 0)
            .forEach((line) => records.push(JSON.parse(line)));
    }
    return records;
}

function findRecord(records, action) {
    return records.find((record) => JSON.stringify(record).includes(action) && JSON.stringify(record).includes("casuser"));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);

    let records = [];
    for (let attempt = 1; attempt <= 15; attempt++) {
        records = await deliveredRecords();
        if (findRecord(records, "AUTHENTICATION_SUCCESS") !== undefined && findRecord(records, "TICKET_GRANTING_TICKET_DESTROYED") !== undefined) {
            break;
        }
        await cas.sleep(2000);
    }
    await cas.log(`Found ${records.length} audit record(s) delivered through Firehose`);
    const login = findRecord(records, "AUTHENTICATION_SUCCESS");
    assert(login !== undefined, "The login must be audited through Firehose");
    await cas.log(login);
    assert(login.principal === "casuser");
    assert(login.applicationCode === "CAS");
    assert(findRecord(records, "TICKET_GRANTING_TICKET_DESTROYED") !== undefined, "The logout must be audited through Firehose");
})();
