
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const service = "https://localhost:9859/anything/sample1";
    const token = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiY3R5IjoiSldUIn0..6q-WnspGZkzHAd_4LDbRnA.Crc5YCWpZ26j9iJ61RR0t7L_A1NtEbTe-IaqqBrEZBlLFxGIcchMGC6sPXOasewh3GwkukrHGyg5pfXU8OvPHWPvC8riG5T1rEYkfgxVoG2rUOIFrLYHGpLCRdsXRP3EYnE4nnIePlDTmKLVC80ovtUoonow2YVwMK6mv1mqWE-RN-XSv_gav4aroaNvY4AA19Q_ppBYc_VVUJmz1t0pr1BTIRao8Hm56LQU8_kSBDfEuB96wTPpuTafZ3zMshgkLM--Ga-FGLL2Tyx_toMdaQ.QtUB4sH8R0BQ0YsB8QmC-ccAtgwGhLZu";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogout(page);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?token=${token}&response_type=code&client_id=client1&scope=${encodeURIComponent("openid profile ssotoken email")}&redirect_uri=${service}`;

    await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.screenshot(page);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client1&client_secret=secret1&redirect_uri=${service}&code=${code}`;
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
    assert(decoded["token"] !== undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token }`;
    await cas.log(`Calling user profile ${profileUrl}`);

    const ssoToken = await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.sub === "casuser");
        assert(res.data.attributes["email"] === "cas@example.org");
        assert(res.data.attributes["given_name"] === "CAS");
        return res.data.attributes["token"];
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const response = await cas.doRequest(`https://localhost:8443/cas/actuator/tokenAuth/${ssoToken}?service=1`,
        "GET", {
            "Content-Type": "application/json",
            "Accept": "application/json"
        });
    const body = JSON.parse(response);
    console.dir(body, {depth: null, colors: true});
    assert(body.registeredService.id === 1);
    assert(body.registeredService.properties === undefined);
    assert(body.principal.id === "casuser");

    await cas.closeBrowser(browser);
})();
