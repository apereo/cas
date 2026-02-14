const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#imageQRCode");
    await cas.assertVisibility(page, "#confirm");

    const qrCode = await cas.parseQRCode(page, "#imageQRCode");
    await cas.logb("Decoded QR Code:", qrCode.data);
    await cas.closeBrowser(browser);
})();
