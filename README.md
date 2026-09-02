# SF_FNAmplifications

<p align="center">
  <strong>FN Amplifications — modernized for Slimefun Legacy.</strong><br>
  Machines, power generation, gems, staffs, gear progression, utilities, multiblocks, and more for modern Slimefun servers.
</p>

<p align="center">
  <a href="https://github.com/wickidcow/SF_FNAmplifications/actions/workflows/build-with-test.yml"><img alt="Build" src="https://github.com/wickidcow/SF_FNAmplifications/actions/workflows/build-with-test.yml/badge.svg"></a>
  <a href="https://github.com/wickidcow/SF_FNAmplifications/releases/latest"><img alt="Latest Release" src="https://img.shields.io/github/v/release/wickidcow/SF_FNAmplifications?display_name=release&sort=semver"></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/license-GPL--3.0-blue"></a>
</p>

> [!IMPORTANT]
> This is a community-maintained continuation of **FN-FAL113's FN Amplifications**. The goal is to keep the original addon working on current Minecraft and Slimefun servers while preserving established Slimefun item IDs, recipes, progression, and existing worlds wherever possible.

## Compatibility

| Component | Support |
| --- | --- |
| Minecraft | **1.21.11+** |
| Server | **Paper 26.2** primary target |
| Paper forks | **Supported when Paper API compatible**, including Purpur-style forks |
| Java | **25** |
| Slimefun | **Slimefun Legacy** primary target |
| Slimefun United | Compatibility compile validated in CI |
| Folia | **Not currently claimed as supported** |

Folia uses a different scheduler/threading model from traditional Paper servers. A dedicated Folia compatibility pass is required before this project will advertise Folia support.

## Download

### Latest release: 4.2.6

**Raw JAR:** [`SF_FNAmplifications4.2.6.jar`](https://github.com/wickidcow/SF_FNAmplifications/releases/download/v4.2.6/SF_FNAmplifications4.2.6.jar)

All releases are available on the [GitHub Releases page](https://github.com/wickidcow/SF_FNAmplifications/releases).

Release files follow the Slimefun Legacy addon naming style:

```text
SF_FNAmplifications<version>.jar
```

## What FN Amplifications adds

FN Amplifications is a large Slimefun expansion with content spanning power, machinery, combat, utilities, progression, and automation.

### Power & automation

- **PowerXpansion** generators
- Multiple **solar generators**
- **Material Generators** for renewable resources
- Electric compressors, condensers, recyclers, transformers, and other machinery
- **Electric Machine Downgrader** and metal scrap progression
- **Electric Block Breaker** with configurable behavior
- Custom multiblocks and production systems

### Equipment & progression

- **25+ Gems** that socket into weapons, armor, and tools
- **FN Gear** with upgrade progression, attributes, and enchantments
- Normal and upgraded **Quivers**
- **11 Mystery Sticks** aimed at high-powered PvE/PvP gameplay
- **20+ Staffs** with combat, movement, utility, and area effects

### Tools & utilities

- **FN Hoe** with area tilling, harvesting, and auto-replanting
- **FN Block Rotator**
- **FN Electric Jukebox** variants
- **FN Auto Ladder**
- **FN Orient Pearl**
- **FN Throwable Torch**
- Custom components, materials, machines, and more

## Slimefun Legacy edition

This maintained fork focuses on compatibility and continuity rather than redesigning the addon.

The current maintenance baseline includes:

- Minecraft **1.21.11+**
- Paper **26.2** as the primary build/runtime target
- Java **25**
- Slimefun Legacy as the primary Slimefun implementation
- Slimefun United compatibility compilation in CI
- Modern Paper API fixes
- Safer Bukkit/Paper scheduler usage
- Removal of the archived BlobBuild/Dough updater path
- Modern GitHub Actions builds
- Automated raw-JAR GitHub releases
- Preservation of existing FN Amplifications Slimefun IDs wherever practical

## Installation

1. Run **Paper 26.2** or an API-compatible Paper fork on **Java 25**.
2. Install **Slimefun Legacy**.
3. Download the latest `SF_FNAmplifications*.jar` from [Releases](https://github.com/wickidcow/SF_FNAmplifications/releases/latest).
4. Place the JAR in your server's `plugins` folder.
5. Start or fully restart the server.

**Optional dependency:** Vault may be used by integrations that require it.

> [!WARNING]
> Do not install two different FN Amplifications JARs at the same time.

## Updating an existing server

Before replacing an older FN Amplifications build:

1. Stop the server.
2. Back up the world and plugin data.
3. Remove the previous FN Amplifications JAR.
4. Add the new `SF_FNAmplifications<version>.jar`.
5. Start the server and review the startup log.

This fork intentionally preserves established Slimefun IDs wherever possible so existing player items and placed machines continue to resolve normally after an upgrade.

## 4.2.6 startup hotfix

Paper 26.2 uses version strings such as `26.2.build.121-stable`. Older FN Amplifications compatibility code attempted to parse the word `build` as a number while loading `VersionedMaterial`, which prevented the plugin from enabling. Version 4.2.6 removes that obsolete parser and directly uses the modern material names guaranteed by the 1.21.11+ support baseline.

## Configuration

Configuration is stored under:

```text
plugins/FNAmplifications/
```

The addon retains configuration for areas such as power generation, buffers, machines, generators, gear, staffs, and module behavior.

## Building from source

FN Amplifications is built with Maven and Java 25.

```bash
mvn clean verify
```

The maintained CI workflow additionally builds against the current **Slimefun Legacy** source and performs a compatibility compile against **Slimefun United**. Maven verification also includes a regression test for compatibility-material class initialization.

Successful release builds publish a raw JAR rather than wrapping it in a ZIP archive.

## Reporting issues

Please report bugs that occur with this maintained fork through the repository issue tracker:

**[Open an issue](https://github.com/wickidcow/SF_FNAmplifications/issues)**

When reporting a server problem, include:

- Minecraft/server version
- Java version
- Slimefun implementation and version
- FN Amplifications version
- Relevant startup or error log
- Steps to reproduce the issue when known

## Credits

- **FN_FAL113** — original FN Amplifications creator and project author
- **Waleks** — SimpleMaterialGenerators-based generator work credited by the upstream project
- **Minecraft-Heads** — head textures used by the addon
- The Slimefun community and maintainers who continue supporting the ecosystem

This fork is independently maintained and is not an official continuation by the original author.

## License

FN Amplifications is distributed under the **GNU General Public License v3.0**.

See [`LICENSE`](LICENSE) for the full license text.
