import com.yubico.u2f.data.*
import org.apereo.cas.adaptors.u2f.storage.*
import org.apereo.cas.util.*

import java.time.*

def read(Object[] args) {
    def logger = args[0]

    def devices = new HashMap<>()
    def reg = new DeviceRegistration("123456", "bjsdghj3b", "njsdkhjdfjh45", 1, false)
    devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES,
            [new U2FDeviceRegistration(2000, "casuser", reg.toJson(), LocalDate.now()),
             new U2FDeviceRegistration(1000, "casuser", reg.toJson(), LocalDate.now())
            ])
    return devices
}

def write(Object[] args) {
    def listOfDevices = args[0]
    def logger = args[1]

    true
}
