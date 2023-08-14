package com.warningimhack3r.mintstonebackend.controllers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.warningimhack3r.mintstonebackend.utils.responseStringSanitized
import io.graversen.minecraft.rcon.commands.*
import io.graversen.minecraft.rcon.commands.base.ICommand
import io.graversen.minecraft.rcon.service.ConnectOptions
import io.graversen.minecraft.rcon.service.MinecraftRconService
import io.graversen.minecraft.rcon.service.RconDetails
import io.graversen.minecraft.rcon.util.Target
import io.graversen.minecraft.rcon.util.WhiteListMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
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
class RCONController: DisposableBean {

    private val log = LoggerFactory.getLogger(RCONController::class.java)
    private var connections = HashMap<String, MinecraftRconService>()

    override fun destroy() {
        if (connections.isNotEmpty()) {
            log.info("Disconnecting from ${connections.size} RCON ${if (connections.size == 1) "server" else "servers"}")
        }
        connections.forEach { (_, connection) ->
            connection.disconnect()
        }
    }

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

    @GetMapping("/version")
    fun getServerVersion(@RequestBody params: ObjectNode): Any = sendCommandFromParams(params) { "version" }.let { response ->
        if (response.lowercase().contains("checking version")) {
            log.info("Server is still checking version, waiting 100ms and trying again")
            Thread.sleep(100)
            getServerVersion(params)
        } else object {
            val status = "success"
            val platform = with(response) {
                when {
                    contains(" Paper ") -> "Paper"
                    contains("Spigot") -> "Spigot" // TODO: Check if this is correct
                    contains("Bukkit") -> "Bukkit" // TODO: Check if this is correct
                    // TODO: Add more platforms?
                    startsWith("Unknown command") -> "Vanilla"
                    else -> "Unknown"
                }
            }
            val version = when(platform) {
                "Paper" -> response.substringAfter("git-Paper-").substringBefore(" (MC:")
                "Spigot" -> "" // TODO
                "Bukkit" -> "" // TODO
                "Vanilla" -> null
                else -> "Unknown"
            }
        }
    }

    @PostMapping("/check-for-updates")
    fun checkForUpdates(
        @RequestBody params: ObjectNode,
        @RequestParam platform: String,
        @RequestParam("serverVersion") platformVersion: String,
        @RequestParam gameVersion: String
    ): Any {
        fun makeAPICall(url: String): JsonObject? {
            val response = try {
                HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                        .uri(URI(url))
                        .build(), HttpResponse.BodyHandlers.ofString())
                    .body()
            } catch (e: Exception) {
                println("Unable to make API call to $url")
                return null
            }
            return try {
                JsonParser.parseString(response).asJsonObject
            } catch (e: Exception) {
                println("Unable to parse response from $url")
                null
            }
        }

        data class Change(val version: String, val changes: List<String>)
        data class NewVersion(val version: String, val downloadUrl: String, val changes: List<Change>)

        val info = when(platform.lowercase()) {
            "paper" -> {
                val paperVersion = platformVersion.toIntOrNull() ?: run {
                    log.error("Unable to parse Paper version: $platformVersion")
                    return object {
                        val status = "error"
                        val message = "Unable to parse Paper version: $platformVersion"
                    }
                }
                val newBuilds = makeAPICall("https://api.papermc.io/v2/projects/paper/versions/$gameVersion/builds")
                    ?.get("builds")?.asJsonArray?.filter { build ->
                        build.asJsonObject.get("build").asInt > paperVersion
                    } ?: emptyList()
                val changes = newBuilds.map { build ->
                        val buildObject = build.asJsonObject
                        Change(buildObject.get("build").asString, buildObject.get("changes").asJsonArray.map { change ->
                            change.asJsonObject.get("message").asString.trim()
                        })
                    }.reversed()
                if (newBuilds.isEmpty()) null else NewVersion(
                    changes.first().version,
                    "https://api.papermc.io/v2/projects/paper/versions/$gameVersion/builds/${changes.first().version}/downloads/paper-$gameVersion-${changes.first().version}.jar",
                    changes
                )
            }
            // TODO: Add more platforms
            else -> return object {
                val status = "error"
                val message = "Unknown platform: $platform"
            }
        }

        return object {
            val status = "success"
            val updateAvailable = info != null
            val latestVersion = info?.version
            val downloadUrl = info?.downloadUrl
            val changes = info?.changes
        }
    }


    // --- Dashboard commands ---
    @GetMapping("/playerslist")
    fun getPlayersList(@RequestBody params: ObjectNode) = object {
        val status = "success"
        val players = sendCommandFromParams(params, PlayerListCommand.uuids())
            .split(", ")
            .map { playerLine ->
                val filteredLine = playerLine
                                    .removePrefix(playerLine.substringBefore(":") + ":").trim()
                                    .replace(Regex("[()]"), "")
                object {
                    val name = filteredLine.substringBefore(" ")
                    val uuid = filteredLine.substringAfter(" ")
                }
            }
        val playersCount = players.size
    }


    // --- Player commands ---
    @PostMapping("/kick")
    fun kickPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String,
        @RequestParam("reason") kickReason: Optional<String>
    ) = wrapInObject {
        sendCommandFromParams(params, KickCommand(Target.player(playerName), kickReason.orElse(null)))
    }

    @PostMapping("/ban")
    fun banPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String,
        @RequestParam("reason") banReason: Optional<String>
    ) = wrapInObject {
        sendCommandFromParams(params, BanCommand(Target.player(playerName), banReason.orElse(null)))
    }

    @PostMapping("/ban-ip")
    fun banPlayerIp(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String,
        @RequestParam("reason") banReason: Optional<String>
    ) = wrapInObject {
        sendCommandFromParams(params, BanIpCommand(Target.player(playerName), banReason.orElse(null)))
    }

    @PostMapping("/pardon")
    fun pardonPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, PardonCommand(Target.player(playerName)))
    }

    @PostMapping("/kill")
    fun killPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, KillCommand(Target.player(playerName)))
    }

    @PostMapping("/op")
    fun opPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, OpCommand(Target.player(playerName)))
    }

    @PostMapping("/deop")
    fun deopPlayer(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, DeOpCommand(Target.player(playerName)))
    }

    @PostMapping("/whitelist-add")
    fun addToWhitelist(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, WhiteListCommand(Target.player(playerName), WhiteListMode.Targeted.ADD))
    }

    @PostMapping("/whitelist-remove")
    fun removeFromWhitelist(
        @RequestBody params: ObjectNode,
        @RequestParam("player") playerName: String
    ) = wrapInObject {
        sendCommandFromParams(params, WhiteListCommand(Target.player(playerName), WhiteListMode.Targeted.REMOVE))
    }
}
