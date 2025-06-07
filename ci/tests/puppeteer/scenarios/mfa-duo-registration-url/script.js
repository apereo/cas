
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const secret = process.env.DUO_REGISTRATION_SIGNING_KEY;
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-duo");
    await cas.loginWith(page, "unknown", "Mellon");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:9859/anything/1");

    const content = await cas.textContent(page, "body pre");
    const payload = JSON.parse(content);
    await cas.log(payload);
    // remove the last character encoded as a "?"
    const principal = payload.args.principal.slice(0, -1);
    await cas.log(`Using principal ${principal}`);
    const decoded = await cas.verifyJwt(principal, secret, {
        algorithms: ["HS512"],
        complete: false
    });
    assert(decoded.sub === "unknown");
    assert(decoded.aud === "localhost");
    assert(decoded.iss === "https://localhost:8443");
    await browser.close();

})();
