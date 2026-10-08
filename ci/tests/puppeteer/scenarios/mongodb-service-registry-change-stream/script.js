const assert = require("assert");
const {execFileSync} = require("child_process");
const cas = require("../../cas.js");

const MONGO_URI = "mongodb://root:secret@localhost:37017,localhost:37018,localhost:37019/cas?authSource=admin&replicaSet=rs0";
const COLLECTION = "cas-service-registry";

async function runMongoCommand(command) {
    await cas.log(`Running MongoDb command: ${command}`);
    const output = execFileSync("docker", ["exec", "mongodb-server-clustered",
        "mongosh", "--quiet", MONGO_URI, "--eval", command]).toString();
    await cas.log(output);
    return output;
}

async function verifyService(page, service, authorized) {
    for (let attempt = 1; attempt <= 20; attempt++) {
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        const ticketIssued = new URL(page.url()).searchParams.has("ticket");
        if (ticketIssued === authorized) {
            await cas.log(`Service ${service} is ${authorized ? "authorized" : "rejected"} after ${attempt} attempt(s)`);
            if (!authorized) {
                await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
            }
            return;
        }
        await cas.log(`Attempt ${attempt}: service ${service} is not yet ${authorized ? "authorized" : "rejected"}`);
    }
    throw new Error(`Service ${service} was expected to be ${authorized ? "authorized" : "rejected"}`);
}

(async () => {
    const originalService = "https://localhost:9859/anything/original";
    const changedService = "https://localhost:9859/anything/changed";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, originalService);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    await verifyService(page, changedService, false);

    await cas.logg("Changing the service definition directly in MongoDb, bypassing CAS");
    const updated = await runMongoCommand(`db.getCollection('${COLLECTION}')
        .updateOne({_id: 1}, {$set: {serviceId: '^https://localhost:9859/anything/changed.*'}}).modifiedCount`);
    assert(updated.trim() === "1");

    await cas.logg("The scheduled reload is an hour away; only the change stream can pick this up");
    await verifyService(page, changedService, true);
    await verifyService(page, originalService, false);

    await cas.logg("Removing the service definition directly in MongoDb");
    const deleted = await runMongoCommand(`db.getCollection('${COLLECTION}').deleteOne({_id: 1}).deletedCount`);
    assert(deleted.trim() === "1");
    await verifyService(page, changedService, false);

    await cas.closeBrowser(browser);
})();
