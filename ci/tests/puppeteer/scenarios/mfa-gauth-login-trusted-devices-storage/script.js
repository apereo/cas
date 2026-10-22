
const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

/*
 * The rest variation keeps trusted devices here, by id, exactly as CAS posted them. CAS looks records up with
 * GET {url}/{principal|id|date}, lists them with GET {url}/, and removes them with DELETE {url}/{key}.
 */
const restRecords = new Map();

function parseDate(value) {
    return Date.parse(String(value).replace(/\[.*]$/, ""));
}

function matchesKey(record, key) {
    return String(record.id) === key || record.principal === key || record.recordKey === key;
}

/**
 * Starts the REST trusted device backend the rest variation points CAS at.
 *
 * @returns the running server
 */
function startRestBackend() {
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.get("/trusted{/:key}", (req, res) => {
        const key = req.params.key;
        const records = [...restRecords.values()].map((raw) => JSON.parse(raw));
        let found = records;
        if (key !== undefined && key.length > 0) {
            const since = parseDate(key);
            found = Number.isNaN(since) || /^\d+$/.test(key)
                ? records.filter((record) => matchesKey(record, key))
                : records.filter((record) => parseDate(record.recordDate) >= since);
        }
        res.status(200).type("application/json").send(JSON.stringify(found));
    });
    app.post("/trusted{/:key}", (req, res) => {
        const record = JSON.parse(req.body);
        cas.log(`Storing trusted device ${record.id} for ${record.principal}`);
        restRecords.set(String(record.id), req.body);
        res.sendStatus(200);
    });
    app.delete("/trusted{/:key}", (req, res) => {
        const key = req.params.key;
        for (const [id, raw] of restRecords) {
            const record = JSON.parse(raw);
            const expired = key === undefined && req.body.length > 0 && Number(record.expirationDate) <= parseDate(JSON.parse(req.body));
            if (expired || (key !== undefined && matchesKey(record, key))) {
                restRecords.delete(id);
            }
        }
        res.sendStatus(200);
    });
    return new Promise((resolve) => {
        const server = app.listen(5612, () => resolve(server));
    });
}

const BACKENDS = {
    jdbc: {
        container: "postgres-server",
        read: async () => {
            const psql = (sql) => cas.dockerExec("postgres-server", ["psql", "-U", "postgres", "-d", "mfa", "-tA", "-c", sql]);
            const table = (await psql("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND lower(table_name) LIKE '%trust%'")).split("\n")[0];
            assert(table.length > 0, "The trusted device table must exist");
            const count = await psql(`SELECT COUNT(*) FROM "${table}" WHERE principal='casuser'`);
            return Number(count) > 0 ? count : "";
        }
    },
    redis: {
        container: "redis_server_master",
        read: () => cas.dockerExec("redis_server_master", ["redis-cli", "--scan", "--pattern", "RedisMultifactorAuthenticationTrustStorage:*"])
    },
    dynamodb: {
        container: "dynamodb-server",
        read: async () => {
            const items = await cas.dynamoDbScan("DynamoDbCasMfaTrustRecords");
            const stored = items.filter((item) => item.principal.S === "casuser");
            assert(stored.every((item) => item.multifactorAuthenticationProvider?.S === "mfa-gauth"),
                "The trusted device must remember the multifactor provider it was trusted for");
            return stored.length > 0 ? JSON.stringify(stored) : "";
        }
    },
    rest: {
        read: async () => [...restRecords.values()].filter((raw) => JSON.parse(raw).principal === "casuser").join("\n")
    }
};

(async () => {
    const backend = BACKENDS[process.env.SCENARIO_VARIATION];
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} trusted device storage`);
    const restBackend = process.env.SCENARIO_VARIATION === "rest" ? await startRestBackend() : undefined;
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.log("Using scratch code to login...");
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.type(page,"#token", scratch);
    await cas.submitForm(page, "#fm1");

    await cas.innerText(page, "#deviceName");
    await cas.type(page, "#deviceName", "My Trusted Device");
    await cas.sleep(1000);

    await cas.assertInvisibility(page, "#expiration");
    await cas.assertVisibility(page, "#timeUnit");
    await cas.submitForm(page, "#registerform");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(1000);
    await cas.gotoLogout(page);
    await cas.sleep(2000);

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    const response = await cas.doRequest(baseUrl);
    const record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id !== undefined);
    assert(record.name === "My Trusted Device");
    assert((await backend.read()).length > 0, "The trusted device must be kept in the backend");

    await cas.log("A trusted device skips multifactor authentication on the next login");
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.assertInvisibility(page, "#token");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await cas.sleep(1000);

    if (restBackend !== undefined) {
        await cas.log("Revoking the device through the actuator removes it from the REST backend");
        await cas.doRequest(`${baseUrl}/${record.recordKey}`, "DELETE");
        assert((await backend.read()).length === 0, "The trusted device must be removed from the backend");
        await cas.closeBrowser(browser);
        restBackend.close();
        return;
    }

    const container = await cas.dockerContainer(backend.container);
    await cas.log(`Pausing ${backend.container}...`);
    await container.pause();
    try {
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.log("Using scratch code to login...");
        scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page,"#token", scratch);
        await cas.submitForm(page, "#fm1");
        await cas.sleep(3000);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
    } finally {
        await cas.log(`Resuming ${backend.container}...`);
        await container.unpause();
        await cas.closeBrowser(browser);
    }
})();
