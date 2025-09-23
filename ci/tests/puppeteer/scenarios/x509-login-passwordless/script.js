
const cas = require("../../cas.js");
const fs = require("fs");
const request = require("request");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.setRequestInterception(true);
    const args = process.argv.slice(2);
    const config = JSON.parse(fs.readFileSync(args[0]));

    await cas.log(`Certificate file: ${config.trustStoreCertificateFile}`);
    await cas.log(`Private key file: ${config.trustStorePrivateKeyFile}`);

    const cert = fs.readFileSync(config.trustStoreCertificateFile);
    const key = fs.readFileSync(config.trustStorePrivateKeyFile);

    page.on("request", (interceptedRequest) => {
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

            interceptedRequest.respond({
                status: resp.statusCode,
                contentType: resp.headers["content-type"],
                headers: resp.headers,
                body: body
            });
        });

    });

    await cas.gotoLogin(page);
    await cas.sleep(5000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextContains(page, "#content div p", "CN=mmoayyed, OU=dev, O=bft, L=mt, C=world");

    await cas.closeBrowser(browser);
})();
