const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "mongouser", "p@SSw0rd");
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page, service);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

    const baseUrl = "https://localhost:8443/cas/actuator";
    const payload = [
        {
            "name": "cas.server.scope",
            "value": "example.com"
        },
        {
            "name": "cas.logout.follow-service-redirects",
            "value": "true"
        }
    ];
    
    const body = JSON.stringify(payload, undefined, 2);
    await cas.log(`Sending ${body}`);
    const json = JSON.parse(await cas.doRequest(`${baseUrl}/casConfig/update`, "POST",
        {
            "Content-Type": "application/json"
        }, 200, body));
    console.dir(json, {depth: null, colors: true});
    await cas.refreshContext();

    await cas.doGet(`${baseUrl}/env/cas.server.scope`,
        async (res) => {
            assert(res.status === 200);
            const property = res.data["property"];
            assert(property["source"] === "bootstrapProperties-MongoDbPropertySource");
            assert(property["value"] === "example.com");
        },
        async (error) => {
            throw error;
        });

    await cas.doGet(`${baseUrl}/env/cas.logout.follow-service-redirects`,
        async (res) => {
            assert(res.status === 200);
            const property = res.data["property"];
            assert(property["source"] === "bootstrapProperties-MongoDbPropertySource");
            assert(property["value"] === "true");
        },
        async (error) => {
            throw error;
        });

    await cas.gotoLogout(page, service);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, service);
    
    await cas.closeBrowser(browser);
})();
