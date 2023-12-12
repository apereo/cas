const cas = require("../../cas.js");

(async () =>
    cas.doPost("https://localhost:8443/cas/login", "", {
        "CustomPrincipal": "casuser",
        "ATTR_NAME": "VAL_CAS",
        "ATTR_LASTNAME": "VAL_Apereo"
    }, (res) => {
        cas.log(res.headers["set-cookie"]);
        const cookies = res.headers["set-cookie"][0].split(",");
        let found = false;
        for (let i = 0; !found && i < cookies.length; i++) {
            const cookie = cookies[i];
            cas.log(cookie);
            if (cookie.match("TGC=.+")) {
                found = true;
            }
        }
        if (!found) {
            throw "Unable to locate ticket-granting cookie";
        }
    }, (error) => {
        throw error;
    }))();
