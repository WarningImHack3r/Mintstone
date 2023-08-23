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

@RestController
@RequestMapping("/rcon")
class RCONController: DisposableBean {

    private val log = LoggerFactory.getLogger(RCONController::class.java)
    private var connections = HashMap<String, MinecraftRconService>()
    private var connectionsEstablishing: MutableList<RconDetails> = mutableListOf()

    // --- Lifecycle functions ---
    override fun destroy() {
        if (connections.isNotEmpty()) {
            log.info("Disconnecting from ${connections.size} RCON ${if (connections.size == 1) "server" else "servers"}")
        }
        connections.forEach { (_, connection) ->
            connection.disconnect()
        }
    }

    // --- Helper functions ---
    private fun getServerParams(serverAddress: String, serverPort: String?, serverPassword: String): RconDetails {
        return RconDetails(
            serverAddress,
            serverPort?.toInt() ?: 25575,
            serverPassword
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
            return if (currentConnection?.isConnected == true) {
                currentConnection
            } else {
                Thread.sleep(200)
                connectOrGetConnection(details)
            }
        }
        if (connectionsEstablishing.contains(details)) {
            Thread.sleep(200)
            return connectOrGetConnection(details)
        }
        connectionsEstablishing.add(details)
        val newConnection = connectToServer(details)
        connectionsEstablishing.remove(details)
        connections[details.hostname] = newConnection
        return newConnection
    }

    private fun stopAndRemoveConnection(details: RconDetails) {
        connections[details.hostname]?.disconnect()
        connections.remove(details.hostname)
    }

    private fun sendCommandFromParams(params: RconDetails, command: ICommand): String {
        val connection = connectOrGetConnection(params)

        val response = connection.minecraftRcon().orElseThrow {
            ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to get RCON connection")
        }.sendSync(command)

        return response.responseStringSanitized
    }

    private fun sendAsyncCommandFromParams(params: RconDetails, command: ICommand, completion: Runnable = Runnable {}) {
        val connection = connectOrGetConnection(params)

        connection.minecraftRcon().orElseThrow {
            ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to get RCON connection")
        }.sendAsync(completion, command)
    }

    private fun wrapInObject(response: () -> String): Any {
        return object {
            val status = "success"
            val response = response()
        }
    }


    // --- General commands ---
    @GetMapping("/ping")
    fun pingServer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String
    ): Any {
        try {
            connectOrGetConnection(getServerParams(serverAddress, serverPort, serverPassword))
            return object {
                val status = "success"
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message, e)
        }
    }

    @PostMapping("/stop")
    fun stopServer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String
    ) = getServerParams(
        serverAddress,
        serverPort,
        serverPassword
    ).let { details ->
        sendAsyncCommandFromParams(details, StopCommand()).also {
            stopAndRemoveConnection(details)
        }.let {
            wrapInObject {
                "Stop command sent"
            }
        }
    }

    @PostMapping("/reload")
    fun reloadServer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), ReloadCommand())
    }

    @GetMapping("/version")
    fun getServerVersion(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String
    ): Any = sendCommandFromParams(getServerParams(
        serverAddress,
        serverPort,
        serverPassword
    )) { "version" }.let { response ->
        if (response.lowercase().contains("checking version")) {
            log.info("Server is still checking version, waiting 100ms and trying again")
            Thread.sleep(100)
            getServerVersion(
                serverAddress,
                serverPort,
                serverPassword
            )
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
    fun checkForUpdates(@RequestBody params: ObjectNode): Any {
        val platform = if (params.has("platform")) params["platform"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: platform")
        val platformVersion = if (params.has("serverVersion")) params["serverVersion"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: platformVersion")
        val gameVersion = if (params.has("gameVersion")) params["gameVersion"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: gameVersion")

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
        data class NewVersion(val version: String, val downloadUrl: String, val changelog: List<Change>)

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
                        build.asJsonObject["build"].asInt > paperVersion
                    } ?: emptyList()
                val changes = newBuilds.map { build ->
                        val buildObject = build.asJsonObject
                        Change(buildObject["build"].asString, buildObject["changes"].asJsonArray.map { change ->
                            change.asJsonObject["message"].asString.trim()
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
            val changelog = info?.changelog
        }
    }


    // --- Dashboard commands ---
    @GetMapping("/playerslist")
    fun getPlayersList(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String
    ) = object {
        val status = "success"
        val players = sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), PlayerListCommand.uuids())
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
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), KickCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        ), params["reason"]?.textValue()))
    }

    @PostMapping("/ban")
    fun banPlayer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), BanCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        ),
            params["reason"]?.textValue()
        ))
    }

    @PostMapping("/ban-ip")
    fun banPlayerIp(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), BanIpCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        ),
            params["reason"]?.textValue()
        ))
    }

    @PostMapping("/pardon")
    fun pardonPlayer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode,
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), PardonCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        )))
    }

    @PostMapping("/kill")
    fun killPlayer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), KillCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        )))
    }

    @PostMapping("/op")
    fun opPlayer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), OpCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        )))
    }

    @PostMapping("/deop")
    fun deopPlayer(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), DeOpCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        )))
    }

    @PostMapping("/whitelist-add")
    fun addToWhitelist(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), WhiteListCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        ), WhiteListMode.Targeted.ADD))
    }

    @PostMapping("/whitelist-remove")
    fun removeFromWhitelist(
        @RequestParam serverAddress: String,
        @RequestParam(required = false) serverPort: String?,
        @RequestParam serverPassword: String,
        @RequestBody params: ObjectNode
    ) = wrapInObject {
        sendCommandFromParams(getServerParams(
            serverAddress,
            serverPort,
            serverPassword
        ), WhiteListCommand(Target.player(
            if (params.has("player")) params["player"].textValue() else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: player")
        ), WhiteListMode.Targeted.REMOVE))
    }
}
