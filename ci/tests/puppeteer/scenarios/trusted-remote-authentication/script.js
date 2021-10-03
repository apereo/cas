const cas = require('../../cas.js');

(async () => {
    await cas.doPost("https://localhost:8443/cas/login", "", {
        'CustomPrincipal': "casuser"
    }, function (res) {
        console.log(res.headers['set-cookie']);
        let cookies = res.headers['set-cookie'][0].split(",");
        let found = false;
        for (let i = 0; !found && i < cookies.length; i++) {
            let cookie = cookies[i];
            console.log(cookie);
            if (cookie.match("TGC=.+")) {
                found = true;
            }
        }
        if (!found) {
            throw "Unable to locate ticket-granting cookie";
        }
    }, function (error) {
        throw error;
    })
})();
