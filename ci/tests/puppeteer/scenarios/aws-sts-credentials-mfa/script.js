const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const params = new URLSearchParams()
    params.append('username', 'casuser')
    params.append('password', 'Mellon')
    params.append('passcode', '352410')

    await cas.doPost("https://localhost:8443/cas/actuator/awsSts?duration=PT15S",
        params, {
            'Content-Type': "application/x-www-form-urlencoded"
        }, function (res) {
            console.log(res.data);
            throw 'Operation must fail to fetch credentials without mfa';
        }, function (error) {
            assert(error.response.status === 401)
            assert(error.response.data.toString().includes("Authentication failed"));
        });

})();
