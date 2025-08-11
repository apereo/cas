const assert = require("assert");
const cas = require("../../cas.js");

const REQUEST_BASKET_AUTHZ_TOKEN = "SV00cPIKRdWjGkN1vkbEbPdhtvV5vIJ0ajygcdnZVBgl";

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const casService = "https://localhost:9859/anything/oidc";
    await cas.logg(`Trying with URL ${casService}`);

    const payload = await getPayload(page, casService, "client", "secret",
        encodeURIComponent("openid profile email"));
    await cas.decodeJwt(payload.id_token);

    await cas.gotoLogout(page);
    await cas.sleep(2000);
    await cas.assertCookie(page, false);
    
    await cas.doGet("http://localhost:56789/api/baskets/cas/requests?max=1",
        async (res) => {
            const logoutRequestBody = res.data.requests[0].body;
            const params = new URLSearchParams(logoutRequestBody);
            const logoutToken = params.get("logout_token");
            assert(logoutToken !== null && logoutToken !== undefined);
            await cas.log(`logout_token ${logoutToken}`);
            const decodedToken = await cas.decodeJwt(logoutToken);
            assert(decodedToken.iss === "https://localhost:8443/cas/oidc");
            assert(decodedToken.sub === "casuser");
            assert(decodedToken.aud === "client");
            assert(decodedToken.events !== undefined);
            assert(decodedToken.events["http://schemas.openid.net/event/backchannel-logout"] !== undefined);
            assert(decodedToken.sid !== undefined);
            assert(decodedToken.exp !== undefined);
            assert(decodedToken.iat !== undefined);
        },
        async (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json",
            "Authorization": REQUEST_BASKET_AUTHZ_TOKEN
        });

    await cas.closeBrowser(browser);
})();

async function getPayload(page, redirectUri, clientId, clientSecret, scopes) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${scopes}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=${clientId}&client_secret=${clientSecret}&redirect_uri=${redirectUri}&code=${code}`;

    return cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}
