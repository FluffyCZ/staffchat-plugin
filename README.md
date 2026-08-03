# StaffChatNightCube

Professional StaffChat solution for Minecraft servers with advanced Discord integration.

## 🌟 Features

- **Cross-Platform Communication**: Seamlessly chat between Minecraft servers and Discord.
- **Immersive Discord Experience**:
  - Messages from Minecraft appear with the player's avatar and nickname via Webhooks.
  - Discord messages appear in-game with a clean format.
- **Multi-Server Support**: Designed to work across a network of servers (Lobby, Survival, Creative, etc.).
- **Smart Message Handling**:
  - Automatic message deduplication.
  - Centralized Discord handling to prevent spam.
- **Customization**: Fully configurable messages and formats.

## 🛠️ Installation

1. **Download**: Place the `StaffChatNightCube.jar` into the `plugins` folder of all your Minecraft servers.
2. **First Run**: Restart the servers to generate the default `config.yml`.
3. **Configuration**: Edit `plugins/StaffChatNightCube/config.yml` (see Configuration section below).
4. **Restart**: Restart the servers or use `/screload` to apply changes.

## ⚙️ Configuration

### Critical Setup (Multi-Server)
To prevent message duplication and ensure proper Discord synchronization, you must configure the `discord-handler` setting correctly:

- **Main Server (e.g., Lobby)**: Set `discord-handler: true`. This server will manage Discord webhooks and message cleanup.
- **Other Servers**: Set `discord-handler: false`. These servers will only send/receive messages but won't manage the Discord channel directly.

### Config.yml Explanation
```yaml
discord:
  token: "YOUR_BOT_TOKEN"       # Your Discord Bot Token
  channel-id: "CHANNEL_ID"      # The Channel ID for StaffChat

server-name: "Lobby"            # Unique name for this server (e.g., Lobby, Survival)
discord-handler: true           # SET TO TRUE ON ONLY ONE SERVER!
```

## 🤖 Discord Bot Setup

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications).
2. Create a new Application and add a Bot.
3. **Privileged Gateway Intents**: Enable **Message Content Intent**.
4. **Bot Permissions**: The bot requires the following permissions in the StaffChat channel:
   - `View Channels`
   - `Send Messages`
   - `Manage Messages` (Required for replacing user messages with Webhooks)
   - `Manage Webhooks`
5. Invite the bot to your server and copy the Token to `config.yml`.

## 📜 Commands & Permissions

| Command | Permission | Description |
|---------|------------|-------------|
| `/sc <message>` | `staffchat.use` | Send a message to the global staff chat. |
| `/setnick <nick>` | `staffchat.setnick` | Set a custom nickname for staff chat. |
| `/screload` | `staffchat.reload` | Reload the plugin configuration. |
| - | `staffchat.see` | Permission to see staff chat messages. |

## 💬 Discord Commands

- **`/setnick <nickname>`**: Users can set their display name for the StaffChat webhook directly from Discord.

---
Developed for NightCube Network.

