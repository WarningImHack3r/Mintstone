package com.warningimhack3r.mintstonebackend.controllers

import com.tekgator.queryminecraftserver.api.QueryException
import com.tekgator.queryminecraftserver.api.QueryStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/query")
class QueryController {

    @GetMapping
    fun status(@RequestParam("server") serverAddress: String): String {
        try {
            val status = QueryStatus.Builder(serverAddress)
                .build()
            return status.status.toJson()
        } catch (e: QueryException) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message, e)
        }
    }
}
