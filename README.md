# TrChat

[![Version](https://img.shields.io/badge/dynamic/json?label=Version&query=%24.tag_name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FFlickerProjects%2FTrChat%2Freleases%2Flatest)](https://github.com/FlickerProjects/TrChat/releases) [![CodeFactor](https://www.codefactor.io/repository/github/flickerprojects/trchat/badge)](https://www.codefactor.io/repository/github/flickerprojects/trchat)

**Advanced chat control for Minecraft servers**

API usage: 
```kotlin
@EventHandler
fun e(e: TrChatEvent) {
    // if (e.chatType = ...)
    e.isCancelled = true // 取消发送全服喊话
    e.message = "..." // 改变内容
}
```
