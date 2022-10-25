const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?state=1001&client_id=client&redirect_uri=${encodeURIComponent("https://httpbin.org/anything/client")}&scope=${encodeURIComponent("openid profile")}&response_type=code&nonce=vn4qulthnx`;
    await cas.goto(page, url);

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);

    await cas.click(page, "#allow");
    await page.waitForNavigation();
    console.log(`Page url: ${await page.url()}\n`);
    let response = await cas.assertParameter(page, "response");

    let token = await cas.decodeJwt(response, true);
    let kid = await token.header.kid;
    console.log(`Token is signed via key identifier ${kid}`);

    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        res => {
            assert(res.status === 200);
            assert(kid === res.data.keys[0]["kid"]);
            console.log(`Using key identifier ${res.data.keys[0]["kid"]}`);

            cas.verifyJwtWithJwk(response, res.data.keys[0], "RS512").then(verified => {
                // console.log(verified)
                assert(verified.payload.aud === "client");
                assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                assert(verified.payload.state === "1001");
                assert(verified.payload.nonce === "vn4qulthnx");
                assert(verified.payload.code !== undefined);
            });
        },
        error => {
            throw error;
        });

    await browser.close();
})();

