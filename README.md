# FBR CLIENT v1.21.10

![Build Status](https://github.com/USERNAME/fbrclient/actions/workflows/build-release.yml/badge.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1+-green)
![Fabric](https://img.shields.io/badge/Fabric-Loader-orange)
![Modules](https://img.shields.io/badge/Modules-21-blue)

> Advanced Minecraft Fabric client mod with Krypton-style GUI and 21 powerful modules.

---

## ⬇️ Download

**[→ Latest Release](../../releases/latest)**

Download `fbrclient-1.21.10.jar` and place in `.minecraft/mods/`

---

## ✨ Features

### 🎮 CPVP (8 Modules)
| Module | Description |
|--------|-------------|
| Auto Anchor | Auto-explode anchors • GrimAC v4 bypass |
| Crystal Macro | Place & break crystals at 20 CPS |
| Auto Totem | Auto-equip totems • 2s movement pause bypass |
| Kill Aura | Auto-attack nearby players |
| Velocity | Reduce knockback |
| Auto Clicker | Auto click at set CPS |
| Reach | Extended attack range |
| Criticals | Always land critical hits |

### 🍩 DONUT (11 Modules)
| Module | Description |
|--------|-------------|
| Player ESP | Rainbow highlighting • 1000 block range |
| Shulker Order | Auto-select shulkers over $900 |
| Better Chunk Finder | Scan 1000 blocks for chunk borders |
| Farm Finder | Locate farms within 500 blocks |
| Stash Finder | Find hidden storage and valuables |
| Elytra Finder | Scan for equipped/framed elytras |
| Flight | Creative-style flight |
| Speed | Increased movement speed |
| No Fall | Prevent fall damage |
| Jesus | Walk on water |
| Spider | Climb walls |

### ⚡ PERFORMANCE (2 Modules)
| Module | Description |
|--------|-------------|
| FPS Boost | 100x performance mode - applies ultra-low settings |
| Ultimate Culling | Aggressive entity/block culling |

---

## 🎨 GUI

Press **M** in-game to open the Krypton-style GUI:

- Dark theme with cyan accents
- 3-column horizontal layout (CPVP | DONUT | PERFORMANCE)
- Click to toggle modules
- Right-click Auto Anchor for slot configuration

---

## 📦 Requirements

- Minecraft **1.20.1+**
- Fabric Loader **0.15.3+**
- Fabric API
- Java **17+**

---

## 🛠️ Building From Source

```bash
git clone https://github.com/USERNAME/fbrclient.git
cd fbrclient
./gradlew build
```

Output: `build/libs/fbrclient-1.21.10.jar`

The GitHub Actions workflow will also automatically build and release on every push to `main`.

---

## ⚠️ Disclaimer

For educational and testing purposes only. Use responsibly and respect server rules.

---

## 📜 License

MIT License
