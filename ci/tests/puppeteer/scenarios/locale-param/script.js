
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLoginWithLocale(page, undefined, "de");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    await cas.attributeValue(page, "html", "lang", "de");

    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");
    await cas.attributeValue(page, "html", "lang", "fr");

    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");

    await cas.gotoLoginWithLocale(page, service, "es");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "INICIAR SESIÓN");
    await cas.attributeValue(page, "html", "lang", "es");

    await cas.closeBrowser(browser);
})();
