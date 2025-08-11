
const cas = require("../../cas.js");
const request = require("request");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&"
        + `redirect_uri=${redirectUrl}&scope=openid&state=U7yWide2Ak&nonce=8xiyRZUiYP&`
        + "response_type=code";
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertParameter(page, "code");
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await page.setRequestInterception(true);
    page.on("request", (interceptedRequest) => {
        const options = {
            uri: interceptedRequest.url(),
            method: interceptedRequest.method(),
            headers: {
                ...interceptedRequest.headers(),
                "X-Forwarded-For": "1.2.3.4"
            },
            body: interceptedRequest.postData()
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
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.assertCookie(page, false);
    await cas.assertVisibility(page, "#username");
    await cas.closeBrowser(browser);
})();
