# 🧹 ClearBin - Auto Item Remove System

## 🎯 Core Features
- ✅ Automatically removes all dropped ground items every 10 minutes (configurable)
- ✅ Warning system with countdown (60s, 30s, 10s, 5-4-3-2-1)
- ✅ Player trash GUI
- ✅ Bypass permission for VIP players
- ✅ Fully configurable
- ✅ Optimized for performance
- ✅ Supports 1.8 → Latest
- ✅ No NMS usage

## 📦 Installation
1. Download `ClearBin.jar`
2. Place in your server's `plugins` folder
3. Restart server
4. Configure `config.yml` to your needs

## 📜 Commands
- `/clearbin` - Open Trash GUI
- `/clearbin open` - Open Trash GUI
- `/clearbin clear` - Force clear items
- `/clearbin reload` - Reload config
- `/clearbin status` - Show remaining time

## 🧠 Permissions
- `clearbin.admin` - Full admin access
- `clearbin.reload` - Reload config
- `clearbin.clear` - Force clear items
- `clearbin.status` - Check status
- `clearbin.bypass` - Items won't be cleared
- `clearbin.use` - Use trash GUI

## ⚙ Configuration
```yaml
settings:
  auto-clear: true
  interval-seconds: 600  # 10 minutes (default)
  clear-arrows: false
  clear-xp: false

warnings:
  enabled: true
  times:
    - 60
    - 30
    - 10
    - 5
    - 4
    - 3
    - 2
    - 1

messages:
  prefix: "&8[&bClearBin&8] "
  warning: "&cGround items will be cleared in %time% seconds!"
  cleared: "&aRemoved %items% dropped items!"
```

## 🔧 Building
```bash
mvn clean package
```

## 📝 License
Free to use and modify
