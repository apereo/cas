const cas = require("../../cas.js");
const assert = require("assert");

async function loginWithAccessToken(page, service, token) {
    await cas.gotoLogout(page);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&token=${token}`);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

(async () => {
    const service = "https://localhost:9859/anything/sample1";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogout(page);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?&response_type=code&client_id=client&scope=${encodeURIComponent("openid profile email")}&redirect_uri=${service}`;

    await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${service}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["given_name"] !== undefined);
    assert(decoded["name"] !== undefined);
    assert(decoded["email"] !== undefined);
    assert(decoded["preferred_username"] !== undefined);
    assert(decoded["client_id"] !== undefined);

    await loginWithAccessToken(page, service, payload.access_token);
    
    await cas.closeBrowser(browser);
})();
