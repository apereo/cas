
const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUri = "https://localhost:9859/post";
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?state=1001&client_id=client&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent("openid profile")}&response_type=code&nonce=vn4qulthnx`;
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(5000);
    const content = await cas.textContent(page, "body pre");
    const payload = JSON.parse(content);
    // await cas.log(payload);
    assert(payload.form.response !== undefined);

    const response = payload.form.response;
    await cas.log(`Reading keystore from ${process.env.OIDC_KEYSTORE}`);
    const keyContent = JSON.parse(fs.readFileSync(process.env.OIDC_KEYSTORE, "utf8"));
    cas.decryptJwtWithJwk(response, keyContent.keys[1], "RS256").then((verified) => {
        assert(verified.payload.aud === "client");
        assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
        assert(verified.payload.state === "1001");
        assert(verified.payload.nonce === "vn4qulthnx");
        assert(verified.payload.code !== undefined);
    });
    await browser.close();
})();

