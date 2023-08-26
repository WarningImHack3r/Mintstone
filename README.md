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

#### Docker (recommended)

1. Make sure you have [Docker](https://www.docker.com) installed.
2. Create the container with the following command:
    ```sh
    docker run -d \
        --name mintstone \
        -p 8080:8080 \
        -p 3000:3000 \
        ghcr.io/warningimhack3r/mintstone
    ```
3. Open `http://localhost:3000` in your browser.

#### Manual (Docker Compose)

1. Make sure you have [Docker](https://www.docker.com) and [Docker Compose](https://docs.docker.com/compose) installed.
2. Clone this repository.
3. Run `docker-compose up` in the root directory.
4. Open `http://localhost:3000` in your browser.

#### Manual

1. Make sure you have [Node.js](https://nodejs.org) (v16+), [PNPM](https://pnpm.io), and [Java](https://www.java.com) installed.
2. Clone this repository.
3. Run `./gradlew bootRun` (or `gradlew.bat bootRun` on Windows) in the `backend` directory.
4. Run `pnpm install` in the `frontend` directory.
5. Run `pnpm build` in the `frontend` directory.
6. Run `pnpm start` in the `frontend` directory.
7. Open `http://localhost:3000` in your browser.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
