const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?locale=de");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    // Assert that HTML root node has attribute `lang="de"`
    let node = await page.$('html');
    assert("de" === await node.evaluate(el => el.getAttribute("lang")));

    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");
    // Assert that HTML root node has attribute `lang="fr"`
    node = await page.$('html');
    assert("fr" === await node.evaluate(el => el.getAttribute("lang")));

    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "SE CONNECTER");

    await cas.goto(page, "https://localhost:8443/cas/login?locale=es&service=https://apereo.github.io");
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "INICIAR SESIÓN");
    // Assert that HTML root node has attribute `lang="es"`
    node = await page.$('html');
    assert("es" === await node.evaluate(el => el.getAttribute("lang")));

    await browser.close();
})();
