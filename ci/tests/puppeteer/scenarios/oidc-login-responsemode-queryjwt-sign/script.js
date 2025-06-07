
const cas = require("../../cas.js");
const assert = require("assert");

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

    const token = await cas.decodeJwt(response, true);
    const kid = await token.header.kid;
    await cas.log(`Token is signed via key identifier ${kid}`);

    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        (res) => {
            assert(res.status === 200);
            assert(kid === res.data.keys[0]["kid"]);
            cas.log(`Using key identifier ${res.data.keys[0]["kid"]}`);

            cas.verifyJwtWithJwk(response, res.data.keys[0], "RS512").then((verified) => {
                // await cas.log(verified)
                assert(verified.payload.aud === "client");
                assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                assert(verified.payload.state === "1001");
                assert(verified.payload.nonce === "vn4qulthnx");
                assert(verified.payload.code !== undefined);
            });
        },
        (error) => {
            throw error;
        });

    await browser.close();
})();

