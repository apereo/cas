const cas = require('../../cas.js');
const assert = require('assert');
const fs = require('fs');

(async () => {
    let kid = (Math.random() + 1).toString(36).substring(4);
    console.log(`Generated kid ${kid}`)
    let configFilePath = "/tmp/keystore.jwks";
    let config = JSON.parse(fs.readFileSync(configFilePath));
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        function (res) {
            assert(res.status === 200)
            assert(res.data.keys[0]["kid"] !== kid)
        },
        function (error) {
            throw error;
        })

    config.keys[0]["kid"] = kid;
    console.log(`Updated configuration:\n${JSON.stringify(config)}`);
    await fs.writeFileSync(configFilePath, JSON.stringify(config));
    await cas.sleep(1000)
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        function (res) {
            assert(res.status === 200)
            assert(res.data.keys[0]["kid"] === kid)
        },
        function (error) {
            throw error;
        })

})();
