package com.warningimhack3r.mintstonebackend.controllers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.warningimhack3r.mintstonebackend.utils.responseStringSanitized
import io.graversen.minecraft.rcon.commands.BanCommand
import io.graversen.minecraft.rcon.commands.KickCommand
import io.graversen.minecraft.rcon.commands.PlayerListCommand
import io.graversen.minecraft.rcon.commands.StopCommand
import io.graversen.minecraft.rcon.commands.base.ICommand
import io.graversen.minecraft.rcon.service.ConnectOptions
import io.graversen.minecraft.rcon.service.MinecraftRconService
import io.graversen.minecraft.rcon.service.RconDetails
import io.graversen.minecraft.rcon.util.Target
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

@RestController
@RequestMapping("/rcon")
class RCONController {

    private var connections = HashMap<String, MinecraftRconService>()

    // --- Helper functions ---
    private fun getServerParams(params: ObjectNode): RconDetails {
        val serverAddress = if (params.has("serverAddress")) params["serverAddress"] else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: serverAddress")
        val serverPassword = if (params.has("serverPassword")) params["serverPassword"] else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: serverPassword")

        return RconDetails(
            serverAddress.textValue(),
            params["serverPort"]?.intValue() ?: 25575,
            serverPassword.textValue()
        )
    }

    private fun connectToServer(details: RconDetails): MinecraftRconService {
        val connection = MinecraftRconService(
            details,
            ConnectOptions.defaults()
        )
        connection.connectBlocking(Duration.ofSeconds(10))

        if (!connection.isConnected) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Connection timed out")
        }
        return connection
    }

    private fun connectOrGetConnection(details: RconDetails): MinecraftRconService {
        if (connections.containsKey(details.hostname)) {
            val currentConnection = connections[details.hostname]
            if (currentConnection?.isConnected == true) {
                return currentConnection
            }
        }
        val newConnection = connectToServer(details)
        connections[details.hostname] = newConnection
        return newConnection
    }

    private fun sendCommandFromParams(params: ObjectNode, command: ICommand): String {
        val serverParams = getServerParams(params)
        val connection = connectOrGetConnection(serverParams)

        val response = connection.minecraftRcon().orElseThrow {
            ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to get RCON connection")
        }.sendSync(command)

        return response.responseStringSanitized
    }

    private fun wrapInObject(response: () -> String): Any {
        return object {
            val status = "success"
            val response = response()
        }
    }


    // --- General commands ---
    @GetMapping("/ping")
    fun pingServer(@RequestBody params: ObjectNode): Any {
        try {
            connectOrGetConnection(getServerParams(params))
            return object {
                val status = "success"
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message, e)
        }
    }

    @PostMapping("/stop")
    fun stopServer(@RequestBody params: ObjectNode) = wrapInObject {
        sendCommandFromParams(params, StopCommand())
    }
}
