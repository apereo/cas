import com.yubico.u2f.data.*
import org.apereo.cas.adaptors.u2f.storage.*
import org.apereo.cas.util.*
import org.apereo.cas.util.crypto.CertUtils
import org.springframework.core.io.ClassPathResource

import java.time.*

def read(Object[] args) {
    def logger = args[0]

    def devices = new HashMap<>()
    def cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
    def reg = new DeviceRegistration("keyhandle11", "publickey1", cert, 1)
    devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES,
            [new U2FDeviceRegistration(2000, "casuser", reg.toJsonWithAttestationCert(), LocalDate.now(ZoneId.systemDefault())),
             new U2FDeviceRegistration(1000, "casuser", reg.toJsonWithAttestationCert(), LocalDate.now(ZoneId.systemDefault()))
            ])
    return devices
}

def write(Object[] args) {
    def listOfDevices = args[0]
    def logger = args[1]

    true
}

def removeAll(Object[] args) {
    def logger = args[0]
    true
}
