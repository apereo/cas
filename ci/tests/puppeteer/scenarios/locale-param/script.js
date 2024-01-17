const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLoginWithLocale(page, undefined, "de");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    await cas.attributeValue(page, "html", "lang", "de");

    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");
    await cas.attributeValue(page, "html", "lang", "fr");

    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");

    await cas.gotoLoginWithLocale(page, "https://apereo.github.io", "es");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "INICIAR SESIÓN");
    await cas.attributeValue(page, "html", "lang", "es");

    await browser.close();
})();
