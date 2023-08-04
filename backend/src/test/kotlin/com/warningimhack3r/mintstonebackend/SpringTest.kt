package com.warningimhack3r.mintstonebackend

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckHTTPResponse {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun shouldPassIfStringMatches() {
        assertThat(restTemplate.getForObject("/", String::class.java))
            .contains("Mintstone Backend")
    }
}
