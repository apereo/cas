const cas = require("../../cas.js");
const assert = require("assert");

async function verifyAccessTokenIsLimited(context) {
    const page = await cas.newPage(context);

    const redirectUri = "http://localhost:9889/anything/limitedaccesstoken";
    await cas.log(`Trying service ${redirectUri} with limited access tokens`);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client3&scope=${encodeURIComponent("openid profile")}&`
        + `redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client3&client_secret=secret3&redirect_uri=${redirectUri}&code=${code}`;
    for (let i = 0; i < 2; i++) {
        const payload = await cas.doPost(accessTokenUrl, "", {
            "Content-Type": "application/json"
        }, (res) => res.data, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });
        assert(payload.access_token !== undefined);
    }
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        throw `Operation must fail to obtain access token but it succeeded: ${res}`;
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "invalid_grant");
    });
}

async function verifyAccessTokenIsNeverReceived(context) {
    const page = await cas.newPage(context);
    const redirectUri = "http://localhost:9889/anything/noaccesstoken";
    await cas.log(`Trying service ${redirectUri} that would never receive an access token`);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code&client_id=client2&scope=${encodeURIComponent("openid profile")}&`
        + `redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client2&client_secret=secret2&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token === undefined);
    assert(payload.token_type === undefined);
    assert(payload.expires_in === undefined);
    assert(payload.scope === undefined);
    assert(payload.id_token !== undefined);
    assert(payload.refresh_token !== undefined);
    return {redirectUri, url, code, accessTokenUrl, payload};
}

async function verifyAccessTokenAndProfile(context) {
    const page = await cas.newPage(context);
    const redirectUri = "http://localhost:9889/anything/app1";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("openid profile MyCustomScope")}`
        + `&redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3&custom_param=custom_value`
        + "&state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential"
        + "%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D";

    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#userInfoClaims");
    await cas.assertVisibility(page, "#scopes");
    await cas.assertVisibility(page, "#MyCustomScope");
    await cas.assertVisibility(page, "#openid");
    await cas.assertVisibility(page, "#informationUrl");
    await cas.assertVisibility(page, "#privacyUrl");
    await cas.assertVisibility(page, "#name");
    await cas.assertVisibility(page, "#phone_number");

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.logPage(page);
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
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== undefined);
    assert(decoded.client_id !== undefined);
    assert(decoded["preferred_username"] !== undefined);

    assert(decoded["identity-name"] === undefined);
    assert(decoded["common-name"] === undefined);
    assert(decoded["lastname"] === undefined);

    assert(decoded["cn"] !== undefined);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["name"] !== undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
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
    return {redirectUri, url, code, accessTokenUrl, payload};
}

async function verifyAuthorizedScopes(context) {
    const page = await cas.newPage(context);
    const redirectUri = "http://localhost:9889/anything/app1";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("MyCustomScope")}`
        + `&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&scope=${encodeURIComponent("openid profile MyCustomScope")}`
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, () => {
        throw "Operation should have failed to obtain access token";
    },
    (err) => {
        assert(err.response.data.error === "invalid_scope");
        assert(err.response.data.error_description === "Invalid or unauthorized scope");
    });
}

async function verifyMissingTicketGrantingCookie(context) {
    const page = await cas.newPage(context);
    const redirectUri = "http://localhost:9889/anything/app1";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("openid profile MyCustomScope")}`
        + `&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(1000);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(1000);
    await cas.deleteCookies(page, "TGC");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.sleep(1000);
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.assertMissingParameter(page, "code");
    await cas.assertVisibility(page, "#loginForm");
}

async function verifyMissingOpenIdScope(context) {
    const page = await cas.newPage(context);
    const redirectUri = "http://localhost:9889/anything/app1";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("MyCustomScope")}`
        + `&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&scope=${encodeURIComponent("MyCustomScope")}`
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data,
    (err) => {
        throw `Operation failed: ${err}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);
    assert(payload.id_token === undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data["auth_time"] !== undefined);
        assert(res.data["id"] === "casuser");
        assert(res.data["client_id"] === "client");
        assert(res.data["service"] !== undefined);
        assert(res.data.sub === "casuser");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    let context = await browser.createBrowserContext();
    await verifyAccessTokenAndProfile(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyAccessTokenIsNeverReceived(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyAccessTokenIsLimited(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyAuthorizedScopes(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyMissingOpenIdScope(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyMissingTicketGrantingCookie(context);
    await context.close();

    await cas.closeBrowser(browser);
})();
