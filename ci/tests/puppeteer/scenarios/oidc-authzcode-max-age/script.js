
const cas = require("../../cas.js");
const assert = require("assert");

async function fetchIdToken(page, maxAge, successHandler) {
    const redirectUrl = "https://localhost:9859/anything/cas";
    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid email profile")}&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;
    if (maxAge !== undefined && maxAge > 0) {
        url += `&max_age=${maxAge}`;
    }

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.loginWith(page);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.screenshot(page);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `client_id=client&client_secret=secret&grant_type=authorization_code&redirect_uri=${redirectUrl}`;
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        const decoded = await cas.decodeJwt(res.data.id_token);
        successHandler(decoded);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let time1 = null;
    let time2 = null;

    await fetchIdToken(page, -1, (idToken) => {
        time1 = idToken.auth_time;
    });
    await cas.sleep(2000);
    await fetchIdToken(page, 1, (idToken) => {
        time2 = idToken.auth_time;
    });

    await cas.log(`Initial attempt; ID token auth_time: ${time1}`);
    await cas.log(`Second attempt with max_age=1; ID token auth_time: ${time2}`);
    assert(time1 !== time2);

    await cas.closeBrowser(browser);
})();
