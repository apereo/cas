import com.yubico.u2f.data.DeviceRegistration
import org.apereo.cas.adaptors.u2f.storage.BaseResourceU2FDeviceRepository
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration
import org.apereo.cas.util.crypto.CertUtils
import org.springframework.core.io.ClassPathResource

def read(Object[] args) {
    def logger = args[0]

    def devices = new HashMap<>()
    def cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
    def reg1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1)
    def reg2 = new DeviceRegistration("keyhandle22", "publickey1", cert, 1)
    devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES,
            [
                    U2FDeviceRegistration.builder()
                            .id(2000)
                            .username("casuser")
                            .record(reg2.toJsonWithAttestationCert())
                            .build(),
                    U2FDeviceRegistration.builder()
                            .id(1000)
                            .username("casuser")
                            .record(reg1.toJsonWithAttestationCert())
                            .build()
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
