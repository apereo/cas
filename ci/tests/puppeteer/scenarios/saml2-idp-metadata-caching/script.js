const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const endpoint = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache";
    await cas.doDelete(endpoint);

    const GOOD_SP = 1;
    const BAD_SP = 2;
    const SERVICES = [GOOD_SP, BAD_SP];
    
    const sendMetadataRequest = async (id) => {
        try {
            const url = `${endpoint}?serviceId=${id}&includeMetadata=false`;
            return await cas.doGet(url, async (res) => {
                if (id === GOOD_SP) {
                    assert(res.status === 200);
                } else {
                    throw `Expected metadata resolution to pass for service id ${id}`;
                }
            }, async (err) => {
                if (id !== BAD_SP) {
                    await cas.logr(`Expected metadata resolution to pass for service id ${id}: ${err}`);
                    throw err;
                }
            }, {
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "application/json"
            });
        } catch (err) {
            if (id === GOOD_SP) {
                await cas.logr(`Request for service id ${id} failed: ${err.message}`);
                throw err;
            }
        }
        return null;
    };

    const sendConcurrentRequests = async () => {
        const promises = [];
        for (let i = 1; i <= 150; i++) {
            const randomIndex = Math.floor(Math.random() * SERVICES.length);
            const serviceId = SERVICES[randomIndex];
            await cas.logb(`Sending metadata request for service id ${serviceId}`);
            promises.push(sendMetadataRequest(serviceId));
        }
        await Promise.all(promises);
    };
    await sendConcurrentRequests();
})();

