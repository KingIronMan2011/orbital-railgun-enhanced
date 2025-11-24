package io.github.kingironman2011.orbital_railgun_enhanced.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandRegistry {
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("""
                Available commands:
                /orsounds radius <value> - Set the sound radius value
                /orsounds debug <true|false> - Toggle debug mode
                /orsounds help - List all available commands
                """), false);
        return 1;
    }

    public static void registerCommands() {
        ServerConfig.INSTANCE.loadConfig();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("orsounds")
                .executes(CommandRegistry::showHelp)
                .then(CommandManager.literal("debug")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> toggleDebugMode(context, BoolArgumentType.getBool(context, "enabled")))))
                .then(CommandManager.literal("radius")
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                .executes(context -> setRadiusValue(context, DoubleArgumentType.getDouble(context, "value")))))
                .then(CommandManager.literal("help")
                        .executes(CommandRegistry::showHelp))));
    }

    private static int toggleDebugMode(CommandContext<ServerCommandSource> context, boolean enabled) {
        ServerConfig.INSTANCE.setDebugMode(enabled);
        context.getSource().sendFeedback(() -> Text.literal("Debug mode set to: " + enabled), false);
        return 1;
    }

    private static int setRadiusValue(CommandContext<ServerCommandSource> context, double radius) {
        ServerConfig.INSTANCE.setSoundRange(radius);
        context.getSource().sendFeedback(() -> Text.literal("Radius set to: " + radius), false);
        return 1;
    }
}
