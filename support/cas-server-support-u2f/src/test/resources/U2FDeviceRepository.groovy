import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.FileUtils
import org.apereo.cas.adaptors.u2f.storage.BaseResourceU2FDeviceRepository
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration

def read(Object[] args) {
    def logger = args[0]
    def mapper = new ObjectMapper().findAndRegisterModules()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    def file = new File(FileUtils.getTempDirectory(), "groovy-u2f.json")
    if (!file.exists()) {
        return Map.of()
    }
    return mapper.readValue(new FileInputStream(file),
            new TypeReference<Map<String, List<U2FDeviceRegistration>>>() {
            })
}

def write(Object[] args) {
    def listOfDevices = args[0]
    def logger = args[1]
    def devices = new HashMap<>()
    devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, listOfDevices)
    def mapper = new ObjectMapper().findAndRegisterModules()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FileUtils.getTempDirectory(),"groovy-u2f.json"), devices)
    true
}

def removeAll(Object[] args) {
    def logger = args[0]
    def devices = new HashMap<>()
    devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, [])
    def mapper = new ObjectMapper().findAndRegisterModules()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FileUtils.getTempDirectory(),"groovy-u2f.json"), devices)
}

def remove(Object[] args) {
    def registration = args[0]
    def logger = args[1]

    def mapper = new ObjectMapper().findAndRegisterModules()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    def devices = mapper.readValue(new FileInputStream(new File(FileUtils.getTempDirectory(),"groovy-u2f.json")),
            new TypeReference<Map<String, List<U2FDeviceRegistration>>>() {
            })
    def list = new ArrayList<>(devices.get(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES))
    if (list.removeIf(d -> d.id == registration.id
            && d.username.equals(registration.username))) {
        devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, list)
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FileUtils.getTempDirectory(),"groovy-u2f.json"), devices)
    }
        
}
