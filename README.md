# SF_FNAmplifications

**FN Amplifications for modern Slimefun servers.**

This repository is a community-maintained continuation of **FN-FAL113's FN Amplifications**, updated for the current Slimefun ecosystem while preserving the addon's existing content, item IDs, recipes, and progression wherever possible.

## Compatibility

| Component | Target |
| --- | --- |
| Minecraft | **1.21.11+** |
| Server | **Paper 26.2** primary |
| Paper forks | Supported when they remain Paper API compatible (Purpur and similar forks) |
| Java | **25** |
| Slimefun | **Slimefun Legacy** primary |
| Secondary Slimefun target | Slimefun United compatibility is CI-tested |

> Folia uses a different scheduling model from traditional Paper servers and is not claimed as supported unless a release explicitly says so.

## What FN Amplifications adds

FN Amplifications is a large Slimefun expansion focused on machinery, power, utilities, equipment, PvE/PvP toys, and progression systems.

- **PowerXpansion** generators and solar generators
- **Material Generators** for renewable resources
- **Machinery** including compressors, condensers, recyclers, transformers, and machine downgrading
- **Electric Block Breaker** with configurable behavior
- **20+ staffs** with utility, combat, movement, and area effects
- **11 Mystery Sticks** for high-powered PvE/PvP effects
- **25+ Gems** that add special abilities to weapons, armor, and tools
- **Quivers** and upgraded quivers
- **FN Gear** progression and bonus attributes
- **FN Hoe** area tilling/harvesting and auto-replant support
- **FN Block Rotator**
- **FN Electric Jukebox** variants
- **FN Auto Ladder**
- **FN Orient Pearl**
- **FN Throwable Torch**
- Metal scraps, custom components, multiblocks, and more

## Slimefun Legacy edition

The maintained fork focuses on keeping the original addon usable on modern servers without changing established Slimefun IDs unnecessarily.

Current maintenance goals include:

- Minecraft **1.21.11+** compatibility
- Paper **26.2** as the primary build/runtime target
- Compatibility with API-compatible Paper forks
- Slimefun Legacy as the primary Slimefun implementation
- Slimefun United compile compatibility checks
- Java 25 builds
- Removal of archived BlobBuild/updater infrastructure
- Modern GitHub Actions builds and raw JAR releases
- Safer scheduler usage on modern Paper servers

## Installation

1. Install a supported Paper 26.2 server or compatible Paper fork.
2. Install **Slimefun Legacy**.
3. Download `SF_FNAmplifications4.2.5.jar` from this repository's Releases page.
4. Place the JAR in your server's `plugins` folder.
5. Restart the server.

**Optional:** Vault may be installed for integrations that use it.

## Updating an existing FN Amplifications server

Back up your server before replacing any addon JAR. This fork intentionally keeps existing FN Amplifications Slimefun IDs intact wherever possible so existing player items and placed machines can continue to resolve after an upgrade.

Do not run two FN Amplifications JARs at the same time.

## Configuration

FN Amplifications creates its configuration under:

`plugins/FNAmplifications/`

Power rates, buffers, machine behavior, gear settings, generator settings, staff behavior, and module toggles remain configurable.

## Builds and releases

CI validates the addon against the current **Slimefun Legacy** source and also performs a compatibility compile against **Slimefun United**. Release builds are published as a **raw JAR**, not a ZIP.

Release artifact naming:

`SF_FNAmplifications<version>.jar`

## Credits

- **FN_FAL113** — original FN Amplifications creator and project author
- **Waleks** — SimpleMaterialGenerators-based generator work credited by upstream
- **Minecraft-Heads** — head textures used by the addon
- The Slimefun community and maintainers who kept the ecosystem alive

This fork is maintained independently and is not an official continuation by the original author.

## License

FN Amplifications is distributed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the full license text.

## Issues

For bugs specific to this maintained fork, use this repository's issue tracker:

https://github.com/wickidcow/SF_FNAmplifications/issues
