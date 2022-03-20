package me.arasple.mc.trchat;

/**
 * @author wlys
 * @since 2022/3/20 12:54
 */

import taboolib.common.env.RuntimeDependencies;
import taboolib.common.env.RuntimeDependency;


@RuntimeDependency(
        value = "!net.kyori:adventure-api:4.10.0",
        test = "!net.kyori.adventure.Adventure",
        initiative = true
)
@RuntimeDependency(
        value = "!net.kyori:adventure-platform-bungeecord:4.1.0",
        test = "!net.kyori.adventure.platform.bungeecord.BungeeAudiences",
        repository = "https://repo.maven.apache.org/maven2",
        initiative = true
)

public class BungeeEnv {  }
