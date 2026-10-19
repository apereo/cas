
const cas = require("../../cas.js");
const assert = require("assert");

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
    }
};

(async () => {
    const backend = BACKENDS[process.env.SCENARIO_VARIATION];
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} trusted device storage`);
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

    const container = await cas.dockerContainer(backend.container);
    await cas.log(`Pausing ${backend.container}...`);
    await container.pause();

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
    await cas.log(`Resuming ${backend.container}...`);
    await container.unpause();
    await cas.closeBrowser(browser);
})();
