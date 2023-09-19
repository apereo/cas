const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://apereo.github.io";
    const request = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsIm9yZy5hcGVyZW8uY2FzLnNlcnZpY2VzLlJlZ2lzdGVyZWRTZXJ2aWNlIjoiMjEzMzI0ODYyMSIsImtpZCI6IjEyMzQ1Njc4OTAifQ.eyJzdWIiOiJjYXN1c2VyIiwic2NvcGUiOiJvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2FzLmV4YW1wbGUub3JnIiwicmVzcG9uc2VfdHlwZSI6ImNvZGUiLCJyZWRpcmVjdF91cmkiOiJodHRwczpcL1wvYXBlcmVvLmdpdGh1Yi5pbyIsImlhdCI6MTY0NjczMTgxOSwianRpIjoiZWVkY2Q5Y2ItNDA1MS00ODAyLWFmYWUtYmFkMzU1NDNiYjU3IiwiY2xpZW50X2lkIjoiY2xpZW50In0.16XuMcIc68QSLeEfOdP6_hegZac-YI46tVfbeEhu6_fiPH5LxB4OOefTNuf0ST18scya18L3DaQLFQhdQkTneKa9dJt4fHl8POQ-IjpagaVWwFMGWM9VyVo_wd0rHd-1pg-OtnvH8PqSZuVoLm--eS0x7vQOX5IKedTXhACIQRZCq3Rxs9s9q1Rhjxv6hvkgWgrG42i5D6IEUxs1y-a9HLySm2_pxvg_7PiaNIps85Le9mWSrOf_F761q1pKHIR5INDoItMAHWKgnDLjQg8R1WPCyeq7XMacKeXDS4dYk0IeJPK1teyKWJrsdRBdzgnLVyM6MaFszHWOLv_U9Uy22g";
    
    const url = `https://localhost:8443/cas/oidc/authorize?request=${request}&client_id=client&redirect_uri=https://unknown.net`;

    await cas.log(`Browsing to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page, "casuser", "Mellon");

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, async res => {
        await cas.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);
        assert(decoded.sub === "casuser");
        assert(decoded.jti.startsWith("TGT-"));
        assert(decoded.aud === "client");
        assert(decoded.jti != null);
        assert(decoded.iat != null);
        assert(decoded.sid != null);
        assert(decoded.iss === "https://localhost:8443/cas/oidc");
        assert(decoded.client_id === "client");
        assert(decoded.auth_time != null);
        assert(decoded.preferred_username === "casuser");
        assert(decoded.amr[0] === "Static Credentials")
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    
    await browser.close();
})();
