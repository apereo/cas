const assert = require("assert");
const cas = require("../../cas.js");
const express = require("express");

const SERVICE = "https://localhost:9859/anything/consent";
const ENDPOINT = "https://localhost:8443/cas/actuator/attributeConsent";
const JSON_HEADERS = {"Accept": "application/json"};

/*
 * The rest variation keeps decisions in this map, by id, exactly as CAS posted them. CAS reads a list back
 * with default typing, so lists go out wrapped as ["java.util.ArrayList", [...]].
 */
const restDecisions = new Map();

function restDecisionsOf(principal) {
    return [...restDecisions.values()].filter((raw) => JSON.parse(raw).principal === principal);
}

function sendDecisionList(res, decisions) {
    res.type("application/json").send(`["java.util.ArrayList",[${decisions.join(",")}]]`);
}

/**
 * Starts the REST consent backend the rest variation points CAS at.
 *
 * @returns the running server, or undefined for other variations
 */
async function startRestBackend() {
    if (process.env.SCENARIO_VARIATION !== "rest") {
        return undefined;
    }
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.get("/consent", (req, res) => {
        const {principal, service} = req.query;
        if (principal !== undefined && service !== undefined) {
            const found = restDecisionsOf(principal).find((raw) => JSON.parse(raw).service === service);
            if (found === undefined) {
                res.sendStatus(404);
            } else {
                res.type("application/json").send(found);
            }
        } else if (principal === undefined) {
            sendDecisionList(res, [...restDecisions.values()]);
        } else {
            sendDecisionList(res, restDecisionsOf(principal));
        }
    });
    app.post("/consent", (req, res) => {
        const decision = JSON.parse(req.body);
        cas.log(`Storing consent decision ${decision.id} for ${decision.principal}`);
        restDecisions.set(String(decision.id), req.body);
        res.sendStatus(200);
    });
    app.delete("/consent", (req, res) => {
        const {principal, decisionId} = req.query;
        for (const [id, raw] of restDecisions) {
            const decision = JSON.parse(raw);
            if ((principal === undefined || decision.principal === principal) && (decisionId === undefined || id === decisionId)) {
                restDecisions.delete(id);
            }
        }
        res.sendStatus(200);
    });
    return new Promise((resolve) => {
        const server = app.listen(5611, () => resolve(server));
    });
}

/**
 * Reads the consent decisions of casuser straight from the backend of the current variation.
 */
async function storedDecisions() {
    switch (process.env.SCENARIO_VARIATION) {
    case "ldap": {
        const entry = await cas.dockerExec("ldap-server", ["ldapsearch", "-LLL", "-o", "ldif-wrap=no", "-x", "-D", "cn=Directory Manager",
            "-w", "password", "-H", "ldap://", "-b", "cn=casuser,ou=consent,dc=example,dc=org", "description"]);
        return entry.split("\n").filter((line) => line.startsWith("description")).length;
    }
    case "mongo":
        return Number((await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/consent?authSource=admin", "--eval",
            "db.MongoDbCasConsentRepository.countDocuments({principal: 'casuser'})"])).split("\n").pop());
    case "redis": {
        const keys = await cas.dockerExec("redis_server_master", ["redis-cli", "--scan", "--pattern", "ConsentDecision:*"]);
        return keys.length === 0 ? 0 : keys.split("\n").length;
    }
    case "rest":
        return restDecisionsOf("casuser").length;
    case "dynamodb":
        return (await cas.dynamoDbScan("DynamoDbConsentRecords")).filter((item) => item.principal.S === "casuser").length;
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

async function decisions() {
    return JSON.parse(await cas.doRequest(`${ENDPOINT}/casuser`, "GET", JSON_HEADERS, 200));
}

async function gotoService(page) {
    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(2000);
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} consent repository`);
    const restBackend = await startRestBackend();
    await cas.doRequest(`${ENDPOINT}/casuser`, "DELETE", JSON_HEADERS, 200);
    assert(await storedDecisions() === 0);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page);

        await cas.logg("Consent is asked for and the decision is kept in the backend");
        await gotoService(page);
        await cas.assertTextContent(page, "#content h2", "Attribute Consent");
        await cas.assertTextContent(page, "#first-name-value", "[Apereo]");
        await cas.click(page, "#confirm");
        await cas.waitForNavigation(page);
        await cas.assertTicketParameter(page);

        let found = await decisions();
        assert(found.length === 1);
        assert(found[0].decision.principal === "casuser");
        assert(found[0].decision.service.startsWith(SERVICE));
        assert(await storedDecisions() === 1);

        await cas.logg("The stored decision is honored after logging in again");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await gotoService(page);
        await cas.assertTicketParameter(page);

        await cas.logg("Revoking the decision removes it from the backend and consent is asked for again");
        const revoked = await cas.doRequest(`${ENDPOINT}/casuser/${found[0].decision.id}`, "DELETE", JSON_HEADERS, 200);
        assert(revoked === "true");
        found = await decisions();
        assert(found.length === 0);
        assert(await storedDecisions() === 0);
        await gotoService(page);
        await cas.assertTextContent(page, "#content h2", "Attribute Consent");
        await cas.assertTicketParameter(page, false);
    } finally {
        await cas.closeBrowser(browser);
        restBackend?.close();
    }
})();
