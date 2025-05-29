const assert = require("assert");
const cas = require("../../cas.js");

async function exchangeToken(token, fromType, toType) {
    const grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
    let params = `grant_type=${encodeURIComponent(grantType)}&scope=${encodeURIComponent("openid profile address phone")}`;
    params += `&subject_token=${encodeURIComponent(token)}`;
    params += `&resource=${encodeURIComponent("https://localhost:9859/anything/backend")}`;
    const fromTokenType = encodeURIComponent(`urn:ietf:params:oauth:token-type:${fromType}`);
    const requestedTokenType = `urn:ietf:params:oauth:token-type:${toType}`;
    params += `&subject_token_type=${fromTokenType}`;
    params += `&requested_token_type=${encodeURIComponent(requestedTokenType)}`;

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Exchanging token ${token}`);
    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (payload) => {
        switch (toType) {
        case "access_token":
        case "jwt":
            assert(payload.data.access_token !== undefined);
            assert(payload.data.token_type === "Bearer");
            assert(payload.data.expires_in !== undefined);
            assert(payload.data.issued_token_type === requestedTokenType);
            assert(payload.data.scope === "openid profile");
            break;
        case "id_token":
            assert(payload.data.access_token !== undefined);
            assert(payload.data.token_type !== undefined);
            assert(payload.data.expires_in !== undefined);
            assert(payload.data.id_token !== undefined);
            assert(payload.data.issued_token_type === requestedTokenType);

            await cas.doGet("https://localhost:8443/cas/oidc/jwks",
                (keys) => {
                    assert(keys.status === 200);
                    assert(keys.data.keys[0]["kid"] !== undefined);
                    cas.log(`Using key identifier ${keys.data.keys[0]["kid"]}`);
                    cas.verifyJwtWithJwk(payload.data.id_token, keys.data.keys[0], "RS256").then((verified) => {
                        cas.log(verified);
                        assert(verified.payload.sub === "client");
                        assert(verified.payload.aud === "client");
                        assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                        assert(verified.payload.state === undefined);
                        assert(verified.payload.nonce === undefined);
                        assert(verified.payload.iat !== undefined);
                        assert(verified.payload.jti !== undefined);
                        assert(verified.payload.exp !== undefined);
                        assert(verified.payload.preferred_username === "client");
                        assert(verified.payload.name === "CAS");
                        assert(verified.payload.gender === "female");
                    });
                },
                (error) => {
                    throw error;
                });

            break;
        }
        if (toType === "jwt") {
            const jwt = payload.data.access_token;
            const decoded = await cas.decodeJwt(jwt);
            assert(decoded.sub === "client");
            assert(decoded.iss === "https://localhost:8443/cas/oidc");
            assert(decoded.aud === "https://localhost:9859/anything/backend");
            assert(decoded.iat !== undefined);
            assert(decoded.jti !== undefined);
        }
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyTokenExchangeTypes() {
    const params = "grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);

        await exchangeToken(res.data.access_token, "access_token", "access_token");
        await exchangeToken(res.data.access_token, "access_token", "jwt");
        await exchangeToken(res.data.access_token, "access_token", "id_token");

    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyTokenExchangeNativeSso() {

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);

        const redirectUri = "https://localhost:9859/anything/app";
        let url = "https://localhost:8443/cas/oidc/authorize?response_type=code";
        url += `&redirect_uri=${redirectUri}&client_id=client&scope=${encodeURIComponent("openid device_sso")}`;

        await cas.goto(page, url);
        await cas.logPage(page);
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(3000);

        const code = await cas.assertParameter(page, "code");
        await cas.log(`OAuth code ${code}`);
        await context.close();

        let accessTokenParams = "client_id=client&client_secret=secret&grant_type=authorization_code&";
        accessTokenParams += `redirect_uri=${redirectUri}`;

        const baseTokenUrl = "https://localhost:8443/cas/oidc/token";
        const accessTokenUrl = `${baseTokenUrl}?${accessTokenParams}&code=${code}`;
        await cas.log(`Calling ${accessTokenUrl}`);

        const tokens = await cas.doPost(accessTokenUrl, "", {
            "Content-Type": "application/json"
        }, (res) => {
            assert(res.data.access_token !== undefined);
            assert(res.data.id_token !== undefined);
            assert(res.data.device_secret !== undefined);
            return {
                id_token: res.data.id_token,
                access_token: res.data.access_token,
                device_secret: res.data.device_secret
            };
        }, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

        await cas.log("Decoding ID token...");
        const decoded = await cas.decodeJwt(tokens.id_token);
        assert(decoded.sid !== undefined);
        assert(decoded.ds_hash !== undefined);
        assert(decoded.sid_ref !== undefined);

        const grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
        const subjectTokenType = "urn:ietf:params:oauth:token-type:id_token";
        const actorTokenType = "urn:openid:params:token-type:device-secret";

        let params = `grant_type=${encodeURIComponent(grantType)}`;
        params += `&scope=${encodeURIComponent("openid profile address phone")}`;
        params += `&resource=${encodeURIComponent("https://localhost:9859/anything/backend")}`;
        params += `&subject_token=${tokens.id_token}`;
        params += `&subject_token_type=${encodeURIComponent(subjectTokenType)}`;
        params += `&actor_token=${tokens.device_secret}`;
        params += `&actor_token_type=${encodeURIComponent(actorTokenType)}`;

        await cas.doPost(`${baseTokenUrl}?${params}`, "", {
            "Content-Type": "application/json",
            "Authorization": `Basic ${btoa("client:secret")}`
        }, (res) => {
            assert(res.data.access_token !== undefined);
            assert(res.data.expires_in !== undefined);
            assert(res.data.token_type === "Bearer");
            assert(res.data.issued_token_type === "urn:ietf:params:oauth:token-type:access_token");
        }, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

        params = `grant_type=${encodeURIComponent(grantType)}`;
        params += `&scope=${encodeURIComponent("openid profile address phone")}`;
        params += `&resource=${encodeURIComponent("https://localhost:9859/anything/backend")}`;
        params += `&subject_token=${tokens.id_token}`;
        params += `&subject_token_type=${encodeURIComponent(subjectTokenType)}`;
        params += `&actor_token=${tokens.device_secret}`;
        params += `&actor_token_type=${encodeURIComponent(actorTokenType)}`;
        params += `&requested_token_type=${encodeURIComponent("urn:ietf:params:oauth:token-type:id_token")}`;

        await cas.doPost(`${baseTokenUrl}?${params}`, "", {
            "Content-Type": "application/json",
            "Authorization": `Basic ${btoa("client:secret")}`
        }, (res) => {
            assert(res.data.access_token !== undefined);
            assert(res.data.expires_in !== undefined);
            assert(res.data.token_type === "Bearer");
            assert(res.data.id_token !== undefined);
            assert(res.data.issued_token_type === "urn:ietf:params:oauth:token-type:id_token");
        }, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

    } finally {
        await browser.close();
    }

}

(async () => {
    await verifyTokenExchangeTypes();
    await verifyTokenExchangeNativeSso();

})();
