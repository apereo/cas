const assert = require("assert");
const cas = require("../../cas.js");

const ISSUER = "https://localhost:8443/cas/oidc";
const FRONT_CHANNEL_URL = "https://localhost:9859/anything/frontchannel";

/**
 * Logs in through the authorization code flow and returns the ID token.
 *
 * @param page the page
 * @param redirectUri the redirect URI of the client
 * @returns the decoded ID token
 */
async function login(page, redirectUri) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid profile")}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");
    const tokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(tokenUrl, "", {"Content-Type": "application/json"},
        (res) => res.data, (error) => {
            throw `Operation failed to obtain tokens: ${error}`;
        });
    return cas.decodeJwt(payload.id_token);
}

(async () => {
    await cas.logg("Discovery advertises front-channel logout with the session id");
    const discovery = await cas.doRequest(`${ISSUER}/.well-known/openid-configuration`);
    const configuration = JSON.parse(discovery);
    assert(configuration.frontchannel_logout_supported === true);
    assert(configuration.frontchannel_logout_session_supported === true);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const idToken = await login(page, "https://localhost:9859/anything/oidc");
    assert(idToken.sid !== undefined, "The ID token must carry a session id");

    const notifications = [];
    page.on("request", (request) => {
        if (request.url().startsWith(FRONT_CHANNEL_URL)) {
            notifications.push(new URL(request.url()));
        }
    });

    await cas.logg("Logging out notifies the client in the browser with the issuer and the session id of its ID token");
    await cas.gotoLogout(page);
    await cas.sleep(3000);
    await cas.assertCookie(page, false);
    assert(notifications.length > 0, "The front-channel logout URL of the client must be called");
    const notification = notifications[0];
    await cas.log(`Front-channel logout request ${notification}`);
    assert.equal(notification.searchParams.get("iss"), ISSUER);
    assert.equal(notification.searchParams.get("sid"), idToken.sid);

    await cas.closeBrowser(browser);
})();
