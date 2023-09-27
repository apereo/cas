const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?locale=de");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");

    await cas.gotoLogin(page, https://apereo.github.io);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");

    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");

    await cas.goto(page, "https://localhost:8443/cas/login?locale=es&service=https://apereo.github.io");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "INICIAR SESIÓN");
    
    await browser.close();
})();
