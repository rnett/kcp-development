package dev.rnett.kcp.development.annotations

@RequiresOptIn("This API is delicate and should only be used by the KCP-development library, unless you really know what you're doing.  Likely to be an easy footgun.", RequiresOptIn.Level.ERROR)
public annotation class DelicateKcpDevApi()
