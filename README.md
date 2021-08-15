# TrChat

***Advanced chat control for Minecraft servers***

API usage: 
```kotlin
@EventHandler
fun e(e: GlobalShoutEvent) {
    // if (...)
    e.isCancelled = true // 取消发送全服喊话
    e.message = "..." // 改变内容
}
```
