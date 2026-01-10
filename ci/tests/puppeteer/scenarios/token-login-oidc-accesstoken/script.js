const cas = require("../../cas.js");
const assert = require("assert");

const service = "https://localhost:9859/anything/sample1";

async function loginWithAccessToken(page, service, token) {
    await cas.gotoLogout(page);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&token=${token}`);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success.attributes.credentialType[0] === "TokenCredential");
    assert(success.attributes.successfulAuthenticationHandlers[0] === "OidcTokenAuthenticationHandler");
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
}

async function loginWithOidc(page, params = []) {
    await cas.gotoLogout(page);

    const queryParameters = new URLSearchParams(
        params.map((p) => [p.name, p.value])
    ).toString();

    const scopes = encodeURIComponent("openid profile email");
    const requestParams = `response_type=code&client_id=client&scope=${scopes}&redirect_uri=${service}&${queryParameters}`;
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?${requestParams}`;

    await cas.goto(page, url);
    await cas.sleep(2000);

    await cas.logPage(page);

    const hasToken = params.some((p) => p.name === "token");
    if (!hasToken) {
        await cas.loginWith(page);
    }
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
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
    return payload;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const payload = await loginWithOidc(page);
    await loginWithAccessToken(page, service, payload.access_token);
    await loginWithOidc(page, [{name: "token", value: payload.access_token}]);
    await loginWithOidc(page, [{name: "token", value: payload.id_token}]);
    await cas.closeBrowser(browser);
})();
