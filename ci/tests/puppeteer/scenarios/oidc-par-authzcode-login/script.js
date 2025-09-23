
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.setRequestInterception(true);

    const redirectUrl = "https://localhost:9859/anything/cas";
    page.once("request", (interceptedRequest) =>
        interceptedRequest.continue({
            "method": "POST",
            "postData": "response_type=code&"
                + `client_id=client&scope=${encodeURIComponent("openid profile MyCustomScope")}&`
                + `redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&`
                + "state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential"
                + "%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D&"
                + "client_secret=secret",
            headers: {
                ...interceptedRequest.headers(),
                "Content-Type": "application/x-www-form-urlencoded"
            }
        }));

    const response = await page.goto("https://localhost:8443/cas/oidc/oidcPushAuthorize");
    const responseBody = await response.text();
    const data = JSON.parse(responseBody);
    const requestUri = data.request_uri;
    await cas.sleep(3000);

    page.setRequestInterception(false);

    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code&client_id=client&request_uri=${requestUri}`;

    await cas.goto(page, url);
    await cas.sleep(3000);
    await cas.loginWith(page);

    await cas.sleep(3000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["sub"] === "casuser");
    assert(decoded["client_id"] === "client");
    assert(decoded["preferred_username"] === "casuser");

    assert(decoded["identity-name"] === undefined);
    assert(decoded["common-name"] === undefined);
    assert(decoded["lastname"] === undefined);
    
    assert(decoded["cn"] !== undefined);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["name"] !== undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token }`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(decoded["common-name"] === undefined);
        assert(decoded["lastname"] === undefined);

        assert(res.data["cn"] !== undefined);
        assert(res.data["name"] !== undefined);
        assert(res.data["family_name"] !== undefined);
        assert(res.data.sub !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.closeBrowser(browser);
})();
