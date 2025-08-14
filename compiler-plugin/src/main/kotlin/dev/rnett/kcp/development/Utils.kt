package dev.rnett.kcp.development

import org.gradle.api.plugins.PluginManager

fun PluginManager.withPlugins(vararg ids: String, block: () -> Unit) {
    withPlugins(ids.toList(), block)
}

tailrec fun PluginManager.withPlugins(ids: List<String>, block: () -> Unit) {
    when {
        ids.isEmpty() -> block()
        ids.size == 1 -> withPlugin(ids[0]) { block() }
        else -> withPlugins(ids.drop(1), block)
    }
}