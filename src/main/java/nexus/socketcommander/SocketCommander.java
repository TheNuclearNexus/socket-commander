package nexus.socketcommander;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static nexus.socketcommander.SocketCommander.LOGGER;

public class SocketCommander implements ModInitializer {
    private SocketServerThread serverThread;

    public static final String MOD_ID = "socket-commander";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicated())
                return;

            serverThread = new SocketServerThread(server.getCommandManager(), server.getCommandSource());
            serverThread.start();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (serverThread == null || server.isDedicated())
                return;

            serverThread.kill();
            LOGGER.debug("Server stopped");
        });
    }
}


class SocketServerThread extends Thread {
    private final CommandManager commandManager;
    private final ServerCommandSource commandSource;
    ServerSocket serverSocket;
    boolean running = true;

    public SocketServerThread(CommandManager commandManager, ServerCommandSource commandSource) {
        this.commandManager = commandManager;
        this.commandSource = commandSource;
    }

    public void run() {
        try {
            if (serverSocket != null)
                serverSocket.setReuseAddress(true);
            serverSocket = new ServerSocket(25566);
            loop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void loop() throws IOException {
        LOGGER.debug("Server started");
        while (running) {
            Socket client = serverSocket.accept();
            LOGGER.debug("Client connected");
            Scanner input = new Scanner(client.getInputStream());

            while (input.hasNextLine()) {
                String command = input.nextLine().trim();
                LOGGER.debug("Received Command: {}", command);
                commandManager.executeWithPrefix(commandSource, command);
            }

            client.close();
        }
        serverSocket.close();
    }

    public void kill() {
        running = false;
    }
}
