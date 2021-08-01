const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    await cas.doPost("https://localhost:8443/cas/v1/users",
        "username=user1+casuser&password=Mellon", {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        function (res) {
            console.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser != null);
            assert(res.data.authentication.attributes.surrogateEnabled != null);
            assert(res.data.authentication.attributes.surrogatePrincipal != null);
        },
        function (error) {
            throw error;
        });

    await cas.doPost("https://localhost:8443/cas/v1/users",
        "username=casuser&password=Mellon", {
            'Accept': 'application/json',
            'X-Surrogate-Principal': 'user1',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        function (res) {
            console.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser != null);
            assert(res.data.authentication.attributes.surrogateEnabled != null);
            assert(res.data.authentication.attributes.surrogatePrincipal != null);
        },
        function (error) {
            throw error;
        });
})();
