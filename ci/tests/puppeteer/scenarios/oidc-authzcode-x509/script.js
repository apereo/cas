
const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const request = require("request");

(async () => {

    const args = process.argv.slice(2);
    const config = JSON.parse(fs.readFileSync(args[0]));

    await cas.log(`Certificate file: ${config.trustStoreCertificateFile}`);
    await cas.log(`Private key file: ${config.trustStorePrivateKeyFile}`);

    const cert = fs.readFileSync(config.trustStoreCertificateFile);
    const key = fs.readFileSync(config.trustStorePrivateKeyFile);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.setRequestInterception(true);
    page.on("request", (interceptedRequest) => {
        if (interceptedRequest.isInterceptResolutionHandled()) {
            return;
        }

        const url = interceptedRequest.url();
        if (!url.startsWith("https://localhost:8443/cas/login")) {
            // cas.logb(`Will NOT intercept the request for ${url}`);
            interceptedRequest.continue();
            return;
        }

        console.log(`Intercepting request for ${url}`);
        const options = {
            uri: interceptedRequest.url(),
            method: interceptedRequest.method(),
            headers: interceptedRequest.headers(),
            body: interceptedRequest.postData(),
            cert: cert,
            key: key
        };

        request(options, (err, resp, body) => {
            if (err) {
                cas.logr(`Unable to call ${options.uri}`, err);
                return interceptedRequest.abort("connectionrefused");
            }

            cas.logb(`Responding with X.509 client certificate ${url}`);
            interceptedRequest.respond({
                status: resp.statusCode,
                contentType: resp.headers["content-type"],
                headers: resp.headers,
                body: body
            });
        });
    });

    const redirectUri = "https://localhost:9859/anything/oidc";
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent("openid profile")}&response_type=code&nonce=vn4qulthnx`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:9859/anything/oidc");
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(/CN=(.+), OU=dev, O=bft, L=mt, C=world/.test(decoded["sub"]));
    assert(decoded["sub"] === decoded["preferred_username"]);
    assert(decoded["txn"] !== undefined);
    assert(decoded["amr"][0] === "X509");

    await browser.close();
})();
