# TrChat

[![Version](https://img.shields.io/badge/dynamic/json?label=Version&query=%24.tag_name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FFlickerProjects%2FTrChat%2Freleases%2Flatest)](https://github.com/FlickerProjects/TrChat/releases) [![CodeFactor](https://www.codefactor.io/repository/github/flickerprojects/trchat/badge)](https://www.codefactor.io/repository/github/flickerprojects/trchat)

**Advanced chat control for Minecraft servers**

API usage: 
```java
public class Demo implements Listener {
    
    @EventHandler
    private void e(TrChatEvent e) {
        e.getChannel(); // 获取聊天频道
        e.setCanceled(true); // 取消发送聊天
        e.setMessage("..."); // 改变聊天内容
    }   
}
```
