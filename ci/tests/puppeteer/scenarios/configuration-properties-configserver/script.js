
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {

    await cas.doGet("http://casuser:Mellon@localhost:8888/casconfigserver/cas/dev",
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page, "configserver", "p@SSword");
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await browser.close();
})();
