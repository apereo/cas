const {execFileSync} = require("child_process");
const cas = require("../../cas.js");

const BASE_DN = "ou=services,dc=example,dc=org";
const BIND = ["-x", "-D", "cn=Directory Manager", "-w", "password", "-H", "ldap://"];

(async () => {
    await cas.verifyServiceRegistry({
        insert: async (definition) => {
            const ldif = `dn: uid=${definition.id},${BASE_DN}\nobjectClass: top\nobjectClass: account\n`
                + `uid: ${definition.id}\ndescription: ${JSON.stringify(definition)}\n`;
            await cas.log(`Adding LDAP entry:\n${ldif}`);
            execFileSync("docker", ["exec", "-i", "ldap-server", "ldapadd", ...BIND], {input: ldif});
        },
        contains: async (id) => {
            const output = execFileSync("docker", ["exec", "ldap-server", "ldapsearch", ...BIND,
                "-b", BASE_DN, `(uid=${id})`, "dn"]).toString();
            await cas.log(output);
            return output.includes(`dn: uid=${id},${BASE_DN}`);
        }
    });
})();
