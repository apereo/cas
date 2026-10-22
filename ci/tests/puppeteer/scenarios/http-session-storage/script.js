
const cas = require("../../cas.js");
const assert = require("assert");

/**
 * Whether the current variation's backend holds the HTTP session, read straight from it.
 */
async function sessionInBackend(sessionId) {
    if (process.env.SCENARIO_VARIATION === "redis") {
        return (await cas.dockerExec("redis_server_master", ["redis-cli", "EXISTS", `spring:session:sessions:${sessionId}`])) === "1";
    }
    return Number((await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
        "mongodb://root:secret@localhost:27017/sessions?authSource=admin", "--eval",
        `db.MongoDbSessionRepository.countDocuments({_id: '${sessionId}'})`])).split("\n").pop()) === 1;
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} session repository`);
    await cas.refreshContext();

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    
    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    const sessionCookie = await cas.containsCookie(page, "SESSION");
    await cas.log(`Found session cookie ${sessionCookie.name}`);
    const cookieValue = await cas.base64Decode(sessionCookie.value);
    await cas.log(`Session cookie value ${cookieValue}`);
    await cas.sleep(1000);
    await cas.doGet(`https://localhost:8443/cas/actuator/sessions/${cookieValue}`,
        (res) => {
            assert(res.data.id !== undefined);
            assert(res.data.creationTime !== undefined);
            assert(res.data.lastAccessedTime !== undefined);
            assert(res.data.attributeNames[0] === "webflowConversationContainer");
        }, (error) => {
            throw error;
        }, { "Content-Type": "application/json" });

    assert(await sessionInBackend(cookieValue), "The HTTP session must be kept in the backend");

    await cas.closeBrowser(browser);
})();
