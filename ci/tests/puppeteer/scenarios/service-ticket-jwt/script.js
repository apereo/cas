const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/1";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    let ticket = await cas.assertTicketParameter(page);

    const keyPath = path.join(__dirname, "private.key");
    const { payload } = await cas.decryptJwt(ticket, keyPath);
    assert(payload.iss === "https://localhost:8443/cas");
    assert(payload.aud === "https://localhost:9859/anything/1");
    assert(payload.credentialType === "UsernamePasswordCredential");
    assert(payload.sub === "casuser");
    assert(payload.jti.startsWith("ST-"));
    
    await browser.close();
})();
