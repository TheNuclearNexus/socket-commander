# Socket Commander

Socket Commander is a Minecraft mod which allows you to send commands directly to the games integrated server.

**This is not done via RCON.**

When the client loads a world, a socket server is started on port 25566. 
Commands can be sent to it as plain text, delimited by new line characters.

All commands are processed in the order they are received.
