# L2Jaln - Lineage II Server Emulator

[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Status](https://img.shields.io/badge/status-Active-green.svg)]()
[![UTF-8](https://img.shields.io/badge/Encoding-UTF8-blue.svg)]()

## Overview

L2Jaln is a Lineage II server emulator built upon:
- Clean ACIS source code (original clean source)
- Selected mods from L2J/Brasil community
- Custom-developed modifications

This combination provides a stable and feature-rich Lineage II server environment.

## Key Features

- **Clean Foundation**: Built upon clean ACIS source code
- **Modern Architecture**: Updated package structure and optimized code organization
- **Community Mods**: Selected improvements from L2J/Brasil community
- **Custom Features**: Personal modifications and implementations
- **Enhanced Performance**: Optimized server performance and resource utilization
- **Comprehensive Documentation**: Detailed setup and configuration guides

## Professional Logging System

- Custom error log handlers
- Item logging system
- Chat and command logging
- GM action auditing
- PvP and event logs
- Advanced debug system

## Technical Requirements

- Java Development Kit (JDK) 17 or higher
- MySQL Server 8.0 or higher
- Minimum 4GB RAM (8GB recommended)
- Windows/Linux operating system
- UTF-8 support for special characters

## Project Structure

```
L2Jaln/
├── java/                 # Core server implementation
│   ├── com.l2jaln/      # Main package
│   │   ├── gameserver/  # Game server components
│   │   ├── loginserver/ # Login server components
│   │   └── util/        # Utility classes
├── sql/                  # Database scripts
├── config/              # Configuration files
└── tools/               # Server management tools
```

## Installation

1. Clone the repository
2. Import the database using the provided SQL scripts
3. Configure the server settings in `config/l2jaln.ini`
4. Start the login server and game server using the provided scripts

## How to Compile and Run

1. Compile the project:
   ```sh
   ant dist-local
   ```

2. Start the servers (in separate terminals):
   ```sh
   cd "Pack L2Jaln/login"
   startLoginServer.bat
   ```
   ```sh
   cd "Pack L2Jaln/game"
   startGameServer.bat
   ```

> **Tip:** Use `chcp 65001` in the terminal for better special character support.

## Configuration

The server can be configured through the following files:
- `config/l2jaln.ini`: Main server configuration
- `config/rates.properties`: Game rates and multipliers
- `config/network.properties`: Network settings

## Community Contributions

This project has been enriched with various community modifications and improvements, including:
- Enhanced PvP system
- Custom events and features
- Optimized performance patches
- Security improvements
- Bug fixes and stability enhancements

## Technologies Used

- Java 17+
- Apache Ant
- UTF-8
- Shell Script & Batch Script
- Custom Logging System

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- ACIS project for providing the clean source code
- L2J/Brasil Community for selected mods
- Special thanks to Anderson Luis do Nascimento (A.L.N) for:
  - Complete development and adaptation
  - Custom modifications and improvements
  - Bug fixes and optimizations
  - System maintenance and updates

> Note: The clean ACIS source code used as the foundation for this project may not be publicly available for download.

## About the Project

Complete, modern, and optimized system for Lineage 2 private servers, developed in Java.
Based on aCis, it offers stability, performance, and advanced features for administrators and players.

## Repository

> **Private** – exclusive use of the author
> GitHub: [ALN2025](https://github.com/ALN2025)

## Signature

```
Work developed and owned by Anderson Luis do Nascimento  
Dev ⩿ A.L.N/⪀
```

---

L2jALN — The best Java technology for Lineage 2 servers! 