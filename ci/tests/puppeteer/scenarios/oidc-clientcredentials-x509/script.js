
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

        cas.logb(`Intercepting request for ${url}`);
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

    const params = "grant_type=client_credentials&scope=openid&client_id=client";
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    const response = await cas.goto(page, url);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    const tokenResponse = JSON.parse(await cas.innerText(page, "body pre"));
    await cas.logb(tokenResponse);
    assert(tokenResponse.access_token !== undefined);
    assert(tokenResponse.id_token !== undefined);
    assert(tokenResponse.refresh_token !== undefined);
    assert(tokenResponse.token_type === "Bearer");
    assert(tokenResponse.scope === "openid");
    assert(tokenResponse.expires_in === 28800);
    const decoded = await cas.decodeJwt(tokenResponse.id_token);
    assert(decoded["sub"] === "mmoayyed");
    assert(decoded["sub"] === decoded["preferred_username"]);
    assert(decoded["txn"] !== undefined);
    assert(decoded["amr"][0] === "X.509");
    
    await cas.closeBrowser(browser);
})();
