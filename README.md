<img src="frontend/static/Mintstone.png" alt="Mintstone Logo" width="100" />

# Mintstone

Comprehensive web admin panel for self-hosted Minecraft servers. Supports all servers since Minecraft [Beta 1.9-pre4](https://minecraft.fandom.com/wiki/Java_Edition_Beta_1.9_Prerelease_4) (2011/10).

## Features

- Server management
- Support for multiple servers
- Server updates monitoring and notifications
- ...and more!

## Planned Features

- Authentication
- Server monitoring
- Plugin monitoring
- Support for all server types (Vanilla, Spigot, Paper, etc.)

You can see the full list of planned features [in the Projects tab](https://github.com/WarningImHack3r/Mintstone/projects).

## Setup

### Configuring your Minecraft server

_Upcoming_

### Installing Mintstone

#### Manual

1. Make sure you have [Node.js](https://nodejs.org) (v16+), [PNPM](https://pnpm.io), and [Java](https://www.java.com) installed.
2. Clone this repository.
3. Run `./gradlew bootRun` (or `gradlew.bat bootRun` on Windows) in the `backend` directory.
4. Run `pnpm install` in the `frontend` directory.
5. Run `pnpm build` in the `frontend` directory.
6. Run `pnpm start` in the `frontend` directory.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
