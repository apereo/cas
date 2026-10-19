const assert = require("assert");
const cas = require("../../cas.js");

const SERVICE = "https://localhost:9859/anything/aup";

/**
 * Whether the backend of the current variation records that casuser accepted the policy, read straight from it.
 */
async function acceptedInBackend() {
    switch (process.env.SCENARIO_VARIATION) {
    case "jdbc":
        return (await cas.dockerExec("postgres-server", ["psql", "-U", "postgres", "-tA", "-c",
            "SELECT aupAccepted FROM aup_users WHERE username='casuser'"])) === "t";
    case "ldap": {
        const entry = await cas.dockerExec("ldap-server", ["ldapsearch", "-LLL", "-x", "-D", "cn=Directory Manager",
            "-w", "password", "-H", "ldap://", "-b", "cn=casuser,ou=aup,dc=example,dc=org", "carLicense"]);
        return entry.split("\n").some((line) => line.trim().toUpperCase() === "CARLICENSE: TRUE");
    }
    case "mongo":
        return Number((await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/aup?authSource=admin", "--eval",
            "db.MongoDbCasAUPRepository.countDocuments({username: 'casuser', aupAccepted: true})"])).split("\n").pop()) === 1;
    case "redis":
        return (await cas.dockerExec("redis_server_master", ["redis-cli", "EXISTS",
            "RedisAcceptableUsagePolicyRepository:casuser:aupAccepted"])) === "1";
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} acceptable usage policy repository`);
    assert(!(await acceptedInBackend()), "The policy must not be accepted yet");

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.logg("The policy is asked for and the acceptance is kept in the backend");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page);
        await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");
        await cas.click(page, "#aupSubmit");
        await cas.waitForNavigation(page);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(SERVICE, ticket);
        assert(json.serviceResponse.authenticationSuccess.user === "casuser");
        assert(await acceptedInBackend(), "The acceptance must be kept in the backend");

        await cas.logg("The stored acceptance is honored at the next login");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertTicketParameter(page);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
