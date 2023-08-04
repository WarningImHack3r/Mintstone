package com.warningimhack3r.mintstonebackend.utils

object ChatColors {

    enum class Codes(val code: String, val ansiCode: String) {
        BLACK("§0", "\u001B[30m"),
        DARK_BLUE("§1", "\u001B[34m"),
        DARK_GREEN("§2", "\u001B[32m"),
        DARK_AQUA("§3", "\u001B[36m"),
        DARK_RED("§4", "\u001B[31m"),
        DARK_PURPLE("§5", "\u001B[35m"),
        GOLD("§6", "\u001B[33m"),
        GRAY("§7", "\u001B[37m"),
        DARK_GRAY("§8", "\u001B[90m"),
        BLUE("§9", "\u001B[94m"),
        GREEN("§a", "\u001B[92m"),
        AQUA("§b", "\u001B[96m"),
        RED("§c", "\u001B[91m"),
        LIGHT_PURPLE("§d", "\u001B[95m"),
        YELLOW("§e", "\u001B[93m"),
        WHITE("§f", "\u001B[97m"),
        OBFUSCATED("§k", "\u001B[8m"),
        BOLD("§l", "\u001B[1m"),
        STRIKETHROUGH("§m", "\u001B[9m"),
        UNDERLINE("§n", "\u001B[4m"),
        ITALIC("§o", "\u001B[3m"),
        RESET("§r", "\u001B[0m")
    }

    fun colorize(text: String): String {
        var result = text
        for (code in Codes.entries) {
            result = result.replace(code.code, code.ansiCode)
        }
        return result.replace("\n", "${Codes.RESET.ansiCode}\n")
    }
}
