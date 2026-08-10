# RPGChat

A Minecraft Spigot plugin that displays chat messages as floating text above players, creating an immersive roleplay chat experience.

## Features

- **Floating Text**: Chat appears above players as armor stands with custom names
- **Whisper System**: Private messages using `#` prefix with customizable range
- **Multi-Channel Support**: Different chat channels with custom ranges and colors
- **Word Filtering**: Configurable profanity filter with custom blacklists
- **Sound Effects**: Customizable sounds for different message types
- **Multi-Version Support**: Compatible with Minecraft 1.19.2 - 1.21.11 and 26.1.2 / 26.2

## Building

### Prerequisites
- Java 21+ for the 1.19 - 1.21 profiles, Java 25+ for the 26.x profiles.
  A single JDK 25 builds every profile, so that is the simplest setup.
- Maven 3.6+
- Access to Spigot BuildTools or pre-built Spigot dependencies

### Build Commands

```bash
# Build all supported versions
mvn clean package

# Build specific version
mvn clean package -P v26_2_R1

# Available profiles: v1_19_R1, v1_19_R2, v1_19_R3, v1_20_R1, v1_20_R2, v1_20_R3, v1_20_R4, v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7, v26_1_R1, v26_2_R1
```

Up to 1.21.11 the server is Spigot-mapped, so the build compiles against the
`remapped-mojang` artifact and remaps the plugin back to Spigot names. From 26.1 on
Spigot ships Mojang-mapped and drops the versioned `craftbukkit` package, so the 26.x
profiles use no classifier and skip the remap step entirely.

## Dependencies

### Required
- Spigot/Paper 1.19.2 - 1.21.11, or 26.1.2 / 26.2

### Optional
- ChatControl - Extended chat management integration

## Development

The plugin uses NMS (Net Minecraft Server) code for armor stand manipulation across different Minecraft versions. Each supported version has its own NMS package under `src/main/java/dev/bermeb/rpgchat/nms/`.

### Architecture
- **Display System**: Strategy pattern for different message types
- **Channel Management**: Multi-channel support with range-based visibility
- **Queue System**: Message processing and cleanup
- **NMS Abstraction**: Version-specific entity handling

## License

This project is licensed under CC BY-NC-SA 4.0 - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Test across supported Minecraft versions
4. Submit a pull request

## Support

Report issues on the [GitHub Issues](https://github.com/bermeb/rpgchat-spigot/issues) page.
