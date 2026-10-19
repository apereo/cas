const assert = require("assert");
const cas = require("../../cas.js");

const THROTTLES = "https://localhost:8443/cas/actuator/throttles";

/**
 * Reads what the current variation's backend recorded for the failed logins, straight from it: audit records for the
 * JDBC and MongoDb throttles, the failure set for Redis, and the locked-account attribute the LDAP receiver writes.
 */
async function recordedInBackend() {
    switch (process.env.SCENARIO_VARIATION) {
    case "jdbc": {
        const psql = (sql) => cas.dockerExec("postgres-server", ["psql", "-U", "postgres", "-d", "audit", "-tA", "-c", sql]);
        const table = (await psql("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND lower(table_name) LIKE '%audit%trail%'")).split("\n")[0];
        assert(table.length > 0, "The audit table must exist");
        return Number(await psql(`SELECT COUNT(*) FROM "${table}"`)) > 0;
    }
    case "redis":
        return (await cas.dockerExec("redis_server_master", ["redis-cli", "--scan", "--pattern", "CasThrottledSubmission:*"])).length > 0;
    case "mongo":
        return Number((await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/audit?authSource=admin", "--eval",
            "db.MongoDbCasAuditRepository.countDocuments({actionPerformed: 'AUTHENTICATION_FAILED'})"])).split("\n").pop()) > 0;
    case "ldap": {
        const entry = await cas.dockerExec("ldap-server", ["ldapsearch", "-LLL", "-x", "-D", "cn=Directory Manager",
            "-w", "password", "-H", "ldap://", "-b", "cn=casuser,ou=throttle,dc=example,dc=org", "postalCode"]);
        return entry.split("\n").some((line) => line.trim().toLowerCase() === "postalcode: true");
    }
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} throttle`);
    await cas.doDelete(`${THROTTLES}?clear=true`);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.log("Log in attempt: #1");
        await submitLoginFailure(page);
        await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

        await cas.log("Log in attempt: #2");
        await submitLoginFailure(page);

        await cas.log("Log in attempt: #3");
        await submitLoginFailure(page);
        await cas.assertInnerText(page, "#content h2", "Access Denied");
        await cas.assertInnerTextContains(page, "#content p", "You've been throttled.");
        assert(await recordedInBackend(), "The failed logins must be recorded in the backend");

        if (["redis", "ldap"].includes(process.env.SCENARIO_VARIATION)) {
            await cas.logg("Releasing the throttle lets the user log in again");
            await cas.doRequest(`${THROTTLES}?clear=true`, "DELETE", {"Accept": "application/json"}, 200);
            await cas.gotoLogin(page);
            await cas.loginWith(page);
            await cas.sleep(1000);
            await cas.assertCookie(page);
        }
    } finally {
        await cas.closeBrowser(browser);
    }
})();
