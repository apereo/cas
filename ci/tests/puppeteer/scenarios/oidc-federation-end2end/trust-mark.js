const fs = require("fs");
const jose = require("jose");

const TRUST_ANCHOR = "https://localhost:8443/cas/oidc";
const RELYING_PARTY_TRUST_MARK = `${TRUST_ANCHOR}/trust-marks/relying-party`;

/**
 * A relying party trust mark issued by the scenario's trust anchor with its federation key (OpenID Federation 1.0,
 * section 7): the OP requires it of every relying party it registers automatically, and accepts the trust anchor as its
 * issuer because the trust anchor lists itself in trust_mark_issuers.
 */
async function issueRelyingPartyTrustMark(trustAnchorJwksFile, subject) {
    const trustAnchorJwk = JSON.parse(fs.readFileSync(trustAnchorJwksFile, "utf8")).keys[0];
    const issuedAt = Math.floor(Date.now() / 1000);
    return new jose.SignJWT({iss: TRUST_ANCHOR, sub: subject, trust_mark_type: RELYING_PARTY_TRUST_MARK, iat: issuedAt, exp: issuedAt + 86400})
        .setProtectedHeader({alg: "RS256", kid: trustAnchorJwk.kid, typ: "trust-mark+jwt"})
        .sign(await jose.importJWK(trustAnchorJwk, "RS256"));
}

module.exports = {RELYING_PARTY_TRUST_MARK, issueRelyingPartyTrustMark};

if (require.main === module) {
    issueRelyingPartyTrustMark(process.argv[2], process.argv[3])
        .then((trustMark) => process.stdout.write(trustMark))
        .catch((error) => {
            console.error(error);
            process.exit(1);
        });
}
