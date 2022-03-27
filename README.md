# TrChat

[![Version](https://img.shields.io/github/v/release/FlickerProjects/TrChat?logo=VirusTotal&style=for-the-badge)](https://github.com/FlickerProjects/TrChat/releases)
[![Issues](https://img.shields.io/github/issues/FlickerProjects/TrChat?logo=StackOverflow&style=for-the-badge)](https://github.com/FlickerProjects/TrChat/issues)
[![Last Commit](https://img.shields.io/github/last-commit/FlickerProjects/TrChat?logo=ApacheRocketMQ&style=for-the-badge&color=1e90ff)](https://github.com/FlickerProjects/TrChat/commits/v2)
[![Downloads](https://img.shields.io/github/downloads/FlickerProjects/TrChat/total?style=for-the-badge&logo=docusign)](https://github.com/FlickerProjects/TrChat/releases)

**Advanced chat control for Minecraft servers**

---

### 🔔 What's new in TrChat v2?
- **Optimized performance**
- **New Channel & Format System**
- **Better compatibility with other plugins**
- **Use `Adventure`**

---

### ⛏ API usage: 
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
