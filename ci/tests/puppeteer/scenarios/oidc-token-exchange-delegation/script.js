const assert = require("assert");
const cas = require("../../cas.js");

async function exchangeToken(subjectToken, subjectTokenType, toType, actorToken, actorTokenType) {
    const grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
    const fromTokenType = encodeURIComponent(`urn:ietf:params:oauth:token-type:${subjectTokenType}`);
    const requestedTokenType = `urn:ietf:params:oauth:token-type:${toType}`;
    const requestedActorTokenType = `urn:ietf:params:oauth:token-type:${actorTokenType}`;

    let params = `grant_type=${encodeURIComponent(grantType)}`;
    params += `&scope=${encodeURIComponent("openid")}`;
    params += `&subject_token=${encodeURIComponent(subjectToken)}`;
    params += `&subject_token_type=${fromTokenType}`;
    params += `&requested_token_type=${encodeURIComponent(requestedTokenType)}`;
    params += `&actor_token=${actorToken}`;
    params += `&actor_token_type=${encodeURIComponent(requestedActorTokenType)}`;

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Exchanging subject token ${subjectToken} for ${toType}`);
    
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
            assert(payload.data.scope === "openid");
            break;
        }
        if (toType === "jwt") {
            const jwt = payload.data.access_token;
            const decoded = await cas.decodeJwt(jwt);
            assert(decoded.sub === "client");
            assert(decoded.iss === "https://localhost:8443/cas/oidc");
            assert(decoded.act.sub === "client");
        }
        if (toType === "access_token") {
            const profileUrl = `https://localhost:8443/cas/oidc/profile?token=${payload.data.access_token}`;
            await cas.log(`Calling user profile ${profileUrl}`);

            await cas.doPost(profileUrl, "", {
                "Content-Type": "application/json"
            }, (res) => {
                cas.log(res.data);
                assert(res.data.sub !== undefined);
                assert(res.data.act.sub === "client");
            }, (error) => {
                throw `Operation failed: ${error}`;
            });
        }
        if (toType === "id_token") {
            const decoded = await cas.decodeJwt(payload.data.id_token);
            assert(decoded.act.sub === "client");
        }
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    const params = "grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Obtaining access and id tokens from ${url}`);
    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.id_token !== undefined);

        // await exchangeToken(res.data.access_token, "access_token", "jwt", res.data.id_token, "id_token");
        // await exchangeToken(res.data.access_token, "access_token", "access_token", res.data.id_token, "id_token");
        await exchangeToken(res.data.access_token, "access_token", "id_token", res.data.access_token, "access_token");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
})();
