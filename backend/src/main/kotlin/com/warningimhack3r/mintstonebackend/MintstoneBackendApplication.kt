package com.warningimhack3r.mintstonebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
class MintstoneBackendApplication

fun main(args: Array<String>) {
    runApplication<MintstoneBackendApplication>(*args)
}

@RestController
class RootController {
    @RequestMapping(method = [RequestMethod.GET, RequestMethod.HEAD])
    fun root(): String {
        return "Mintstone Backend running"
    }
}

@Configuration
@EnableWebMvc
class Configurer: WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
    }
}
