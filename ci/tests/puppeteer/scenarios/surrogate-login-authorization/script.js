const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "user1+casuser", "Mellon");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user1, have successfully logged into the Central Authentication Service");

    const service = "https://localhost:9859/anything/everything";
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.assertTicketParameter(page, false);
    await browser.close();
})();
