const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const jwkToPem = require("jwk-to-pem");

const ISSUER = "https://localhost:8443/cas/oidc";
const ACTUATOR = "https://localhost:8443/cas/actuator/oidcJwks";
const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});

/*
 * Runs a query against the oidc database of the MongoDb container and returns what it prints as JSON.
 */
async function mongo(query) {
    const output = await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
        "mongodb://root:secret@localhost:27017/oidc?authSource=admin", "--eval", `EJSON.stringify(${query})`]);
    return JSON.parse(output.split("\n").pop());
}

/*
 * Reads the key identifiers of the CAS keystore kept in MongoDb, which holds the keystore as one document per issuer.
 */
async function storedKeyIds() {
    const document = await mongo(`db.OidcJwks.findOne({_id: '${ISSUER}'})`);
    assert(document !== null, "The keystore must be kept in MongoDb");
    const keys = JSON.parse(document.data).keys;
    assert(keys.every((entry) => entry.d !== undefined), "The stored keystore must hold the private keys");
    return keys.map((entry) => entry.kid);
}

async function publishedKeyIds() {
    const jwks = JSON.parse(await cas.doRequest(`${ISSUER}/jwks`, "GET", {"Accept": "application/json"}, 200));
    return jwks.keys.map((entry) => entry.kid);
}

async function getAccessToken() {
    const params = `grant_type=client_credentials&scope=${encodeURIComponent("openid client_jwks_registration_scope")}`;
    return cas.doPost(`${ISSUER}/token?${params}`, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => res.data.access_token, (error) => {
        throw `Operation failed: ${error}`;
    }, false);
}

async function registerPublicKey(accessToken) {
    const publicJwk = {kty: key.kty, n: key.n, e: key.e, kid: key.kid, use: key.use, alg: key.alg};
    const proof = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "client",
        "aud": `${ISSUER}/token`
    }, privateKey, "RS256", {header: {jwk: publicJwk}});
    const body = JSON.stringify({"proof": proof});
    return JSON.parse(await cas.doRequest(`${ISSUER}/jwks/clients`, "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
    }, 200, body));
}

async function loginWithJwtBearer(jkt) {
    const assertion = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "client",
        "aud": `${ISSUER}/token`,
        "exp": 185542587100,
        "iat": 1653737633,
        "sub": "casuser",
        "client_id": "client"
    }, privateKey, "RS256", {header: {"kid": jkt}});
    let params = `assertion=${assertion}&`;
    params += `scope=${encodeURIComponent("openid email profile")}&`;
    params += "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";
    return cas.doPost(`${ISSUER}/token?${params}`, "", {"Content-Type": "application/json"},
        async (res) => res.data, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });
}

(async () => {
    await cas.logg("The CAS keystore is generated into MongoDb and published from there");
    const initial = await storedKeyIds();
    const published = await publishedKeyIds();
    assert(published.length > 0);
    assert(published.every((kid) => initial.includes(kid)), `Published keys ${published} must come from MongoDb ${initial}`);

    await cas.logg("Rotating and revoking keys rewrites the keystore in MongoDb");
    await cas.doRequest(`${ACTUATOR}/rotate`, "POST", {}, 200);
    const rotated = await storedKeyIds();
    assert(rotated.some((kid) => !initial.includes(kid)), "Rotation must add a key to the stored keystore");
    await cas.doRequest(`${ACTUATOR}/revoke`, "POST", {}, 200);
    const revoked = await storedKeyIds();
    assert.notDeepStrictEqual(revoked.sort(), rotated.sort());
    assert((await publishedKeyIds()).every((kid) => revoked.includes(kid)));

    await cas.logg("A client key registered through CAS is kept in MongoDb and used for the JWT bearer grant");
    const accessToken = await getAccessToken();
    assert(accessToken !== undefined);
    const registration = await registerPublicKey(accessToken);
    assert(registration.jkt !== undefined);
    const entries = await mongo(`db.getCollection('OidcJwks-ClientJwksStore').find({_id: '${registration.jkt}'}).toArray()`);
    assert(entries.length === 1 && entries[0].clientId === "client", "The client key must be kept in MongoDb");
    const clients = JSON.parse(await cas.doRequest(`${ACTUATOR}/clients`, "GET", {"Accept": "application/json"}, 200));
    assert(clients.some((entry) => entry.jkt === registration.jkt && entry.clientId === "client"));

    const tokens = await loginWithJwtBearer(registration.jkt);
    assert(tokens.access_token !== undefined && tokens.id_token !== undefined);
    const idToken = await cas.decodeJwt(tokens.id_token);
    assert(idToken.sub !== undefined && idToken.client_id === "client");

    await cas.logg("Removing the client key removes it from MongoDb");
    await cas.doRequest(`${ACTUATOR}/clients/client/${registration.jkt}`, "DELETE", {}, 204);
    const remaining = await mongo(`db.getCollection('OidcJwks-ClientJwksStore').find({_id: '${registration.jkt}'}).toArray()`);
    assert(remaining.length === 0);
})();
