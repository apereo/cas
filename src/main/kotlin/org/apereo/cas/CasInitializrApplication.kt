package org.apereo.cas

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class CasInitializrApplication

fun main(args: Array<String>) {
    SpringApplication.run(CasInitializrApplication::class.java, *args)
}


