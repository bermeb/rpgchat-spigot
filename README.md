# RPGChat

A Minecraft Spigot plugin that displays chat messages as floating text above players, creating an immersive roleplay chat experience.

## Features

- **Floating Text**: Chat appears above players as armor stands with custom names
- **Whisper System**: Private messages using `#` prefix with customizable range
- **Multi-Channel Support**: Different chat channels with custom ranges and colors
- **Word Filtering**: Configurable profanity filter with custom blacklists
- **Sound Effects**: Customizable sounds for different message types
- **Multi-Version Support**: Compatible with Minecraft 1.19.2 - 1.21.6

## Building

### Prerequisites
- Java 21+
- Maven 3.6+
- Access to Spigot BuildTools or pre-built Spigot dependencies

### Build Commands

```bash
# Build all supported versions
mvn clean package

# Build specific version
mvn clean package -P v1_21_R5

# Available profiles: v1_19_R1, v1_19_R2, v1_19_R3, v1_20_R1, v1_20_R2, v1_20_R3, v1_20_R4, v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5
```

## Dependencies

### Required
- Spigot/Paper 1.19.2+

### Optional
- PlaceholderAPI - For dynamic placeholders in messages
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
