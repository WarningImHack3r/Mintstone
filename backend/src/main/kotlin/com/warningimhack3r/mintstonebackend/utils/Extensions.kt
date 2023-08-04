package com.warningimhack3r.mintstonebackend.utils

import io.graversen.minecraft.rcon.RconResponse

val RconResponse.responseStringSanitized: String
    get() = responseString.replace(Regex("§."), "")

val RconResponse.responseStringColored: String
    get() = ChatColors.colorize(responseString)
