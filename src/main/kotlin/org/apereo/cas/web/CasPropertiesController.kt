package org.apereo.cas.web

import com.github.wnameless.json.flattener.JsonFlattener
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URL


@RestController
open class CasPropertiesController {
    @Value(value = "\${casServerConfigPropertiesEndpoint}")
    lateinit var casServerConfigPropertiesEndpoint: String

    @GetMapping("/properties.json")
    open fun properties(): Map<*, *> {
        return hashMapOf("dependencies" to getAllProperties())
    }

    open fun getAllProperties(): List<CasProperty> {
        val jsonUrl = URL(casServerConfigPropertiesEndpoint)

        val propertiesList = mutableListOf<CasProperty>()

        val casProperties = jsonUrl.readText()
        val map = JsonFlattener.flattenAsMap(casProperties)
        map
                .filterKeys { it -> it.contains("].prefix") }
                .onEach { key ->
                    val prefix = key.value as String
                    val childKeys = map
                            .filterKeys {
                                val childKey = key.key.replace(".prefix", ".properties")
                                it.contains(childKey)
                            }
                            .map {
                                val propertyKey = it.key.replace(Regex(".+\\.properties"), prefix)
                                val propertyValue = if (it.value != null) it.value.toString() else ""
                                val group = key.key.removePrefix("[\\\"").removeSuffix("\\\"].prefix")

                                val valueDescription = if (propertyValue.isEmpty()) "blank" else propertyValue
                                val description = "The default value is <kbd>$valueDescription</kbd>."
                                CasProperty(propertyKey, description,
                                        propertyKey, group,
                                        if (propertyKey.startsWith("cas.")) Integer.MAX_VALUE else 0)
                            }
                    propertiesList.addAll(childKeys)
                }
        return propertiesList
    }
}

data class CasProperty(var name: String, var description: String, var id: String, var group: String, var weight: Int)
