# TrChat

![Version](https://img.shields.io/badge/dynamic/json?label=Version&query=%24.tag_name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FFlickerProjects%2FTrChat%2Freleases%2Flatest)

**Advanced chat control for Minecraft servers**

已知问题: [#3](https://github.com/FlickerProjects/TrChat/issues/3)
        [#20](https://github.com/FlickerProjects/TrChat/issues/20)
        [#51](https://github.com/FlickerProjects/TrChat/issues/51)

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
