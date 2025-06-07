
const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?state=1001&client_id=client&redirect_uri=${encodeURIComponent("https://localhost:9859/anything/client")}&scope=${encodeURIComponent("openid profile")}&response_type=code&nonce=vn4qulthnx`;
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.logPage(page);
    const response = await cas.assertParameter(page, "response");

    const configFilePath = path.join(__dirname, "services/Sample-1.jwks");
    await cas.log(`Reading keystore from ${configFilePath}`);
    const keyContent = JSON.parse(fs.readFileSync(configFilePath, "utf8"));

    cas.decryptJwtWithJwk(response, keyContent.keys[1], "RSA-OAEP-256").then((verified) => {
        assert(verified.payload.aud === "client");
        assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
        assert(verified.payload.state === "1001");
        assert(verified.payload.nonce === "vn4qulthnx");
        assert(verified.payload.code !== undefined);
    });
    await browser.close();
})();

