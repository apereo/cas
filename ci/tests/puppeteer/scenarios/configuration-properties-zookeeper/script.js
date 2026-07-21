
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page, "zookeeper", "p@SSword");
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);

    await cas.sleep(3000);
    await cas.doGet("https://localhost:8443/cas/actuator/clusterTopology/discovery",
        (res) => {
            cas.log(res.data);
        },
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });
})();
