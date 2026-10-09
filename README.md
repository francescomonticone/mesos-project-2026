# MESOS

> **Note:** this public copy ships **without the copyrighted graphic assets** (`src/main/resources/img/` — card art, backgrounds, icons) for copyright reasons. The GUI start screen opens, and **music + emoji are kept** in the built app, but only the **TUI is fully playable**.

<img width="592" height="420" alt="image" src="https://github.com/user-attachments/assets/40f69c4a-c8de-4cf4-92fd-2467130b80c9" />


**Course:** Software Engineering Project A.Y. 2025-2026  
**Project:** PSP2 - MESOS

## Group Members
Francesco Monticone , Samuel Matta , Mattia Mareghello , Gabriele Maiolo

---

## Implemented Features

- ✅ **Complete rules** of the Mesos game implemented
- ✅ **CLI (TUI)** – Text User Interface with support for Linux, macOS, Windows
- ✅ **GUI (JavaFX)** – Full graphical interface featuring
- ✅ **Socket** - Socket protocol over TCP/IP
- ✅ **RMI** - Remote Method Invocation for remote communication

## Advanced Features 

- ✅ **DB leaderboard**
- ✅ **Disconnection Handling**
- ✅ **Multiple games**

---

## How to Run the Project

### Prerequisites

| Component | Version | Notes |
|---|---|---|
| **Java** | 25.0.2 | Java Runtime Environment (JRE) required |
| **MySQL** | 8.0+ | Database server for persistence |
| **Network Permissions** | – | Required to open network connections |

**Check your installed Java version:**
```bash
java -version
```

**Install Java 25** (if not present):
- Download from: https://www.oracle.com/java/technologies/downloads/

---

### Database Setup

#### 1. Install MySQL Community Server

Download the installer from https://dev.mysql.com/downloads/mysql/

#### 2. Install MySQL Workbench and Import the Database Schema
 
Download and install **MySQL Workbench** from https://dev.mysql.com/downloads/workbench/ — this is the graphical tool you will use to manage the database.
 
Once installed, follow these steps:
 
**a) Create a new MySQL connection**
 
Open MySQL Workbench and create a new connection by clicking the **+** button next to "MySQL Connections". Set a connection name, leave the hostname as `127.0.0.1` and the port as `3306`, then set a password for the `root` user. Save and open the connection.
 
**b) Open the Data Import panel**
 
In the left sidebar, click on the **Administration** tab, then select **Data Import / Restore**.
 
**c) Select the dump file**
 
Choose **Import from Self-Contained File** and browse to locate the dump file, found in the `deliverables/final/dump/` directory of the project.
 
**d) Set the target schema**
 
Under **Default Target Schema**, click **New** and type `mesos` as the schema name. Confirm.
 
**e) Select import options**
 
You can select **Dump Structure and Data** if you want but only the structure is provided.
 
**f) Start the import**
 
Click **Start Import** and wait for the process to complete (you may have to select the **import progress** tab in order to start the import). You should see a success message in the log panel.
 
**g) Verify the import**
 
Go back to the **Schemas** panel (left sidebar), click the **refresh icon** (🔄), and you should now see the `mesos` database with its three tables listed underneath.
 
#### 3. Configure Database Credentials
 
Edit the `config.properties.example` in `deliverables/final/dump` directory:
Save it as `config.properties` with your MySQL credentials:
```properties
# Mesos Database Configuration
db.url=jdbc:mysql://127.0.0.1:3306/mesos
db.user=root
db.password=YOUR_PASSWORD_HERE
```
 
**Save this file in the same directory of your Server JAR file!!!**
 
---
 
### Starting the Server

The server must be running before any client connects. 
By default, it runs locally on standard ports, but it can be fully configured via command-line arguments. 

**Basic Start (Localhost):**
```bash
java -jar server.jar
```

java -jar server.jar [RMI_IP] [Socket_Port] [RMI_Port]

| Parameter     | Default     | Description                                                                                                   |
|---------------|-------------|---------------------------------------------------------------------------------------------------------------|
| `RMI_IP`      | `127.0.0.1` | IP address broadcasted to RMI clients. Set to your machine's LAN IP (e.g. `192.168.1.50`) for remote clients. |
| `Socket_Port` | `1234`      | Custom port used for Socket connections.                                                                      |
| `RMI_Port`    | `1099`      | Custom port used for the RMI Registry.                                                                        |

**LAN example:**
```bash
java -jar server.jar 192.168.1.50
```


**Expected output:**
```
RMI Server listening on port ...
Socket Server listening on port ...
```
 
**⚠️ Note:** The server listens on both protocols (Socket and RMI) simultaneously. The terminal will continue displaying client connection logs.
 
---

### Starting the Client

#### Launcher Mode (interactive UI selection)
```bash
java -jar client.jar
```

Important: If you are playing over a LAN or the internet using the RMI protocol, you must specify your client's IP address. RMI requires the server to send callbacks to the client, and this property ensures the server knows exactly where to reach you.

for Mac/Linux:
```bash
java -Djava.rmi.server.hostname=<YOUR_CLIENT_IP> -jar client.jar
```

for Windows:
```bash
java "-Djava.rmi.server.hostname=<YOUR_CLIENT_IP>" -jar client.jar
```

Select your preferred interface when prompted:
```
Choose interface:
  1. TUI (Text User Interface - CLI)
  2. GUI (Graphical User Interface - JavaFX)
```

**The GUI will open in a separate window.** Connect to the server by following the instructions on the connection screen.

---

## Troubleshooting

### Error: "config.properties not found"
**Solution:** Create the `config.properties` file in the working directory:
```bash
cp config.properties.example config.properties
# Edit the file with the correct MySQL credentials
```

### Error: "Connection refused" (Server unreachable)
**Checklist:**
1. Is the server running? (Check for "listening on port..." output)
2. Correct IP/Port? (default: 127.0.0.1:1234 for Socket, :1099 for RMI)
3. Does the firewall allow the connection?
4. Is MySQL running?

---

### Tested Operating Systems
- macOS 12+ (Apple Silicon)
- Windows 11 (x86_64)
---

For bug reports or issues:
1. Check the console logs for error messages
2. Verify that all prerequisites are installed
3. Refer to the Troubleshooting section above
