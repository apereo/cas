
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/1");
    await cas.sleep(1000);
    await cas.click(page, "#warnButton");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await cas.assertVisibility(page, "#ignorewarnButton");
    await cas.sleep(1000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#ignorewarnButton");
    await cas.click(page, "#ignorewarnButton");
    await cas.sleep(2000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/3");
    await cas.sleep(1000);
    await cas.assertInvisibility(page, "#ignorewarnButton");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page, false, "CASPRIVACY");
    
    await cas.closeBrowser(browser);
})();
