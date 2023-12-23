package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.shared.TAB;

@Getter
public class MessageFile extends YamlConfigurationFile {

    private final String announceCommandUsage = getString("announce-command-usage", "Usage: /tab announce <type> <name> <length>\nCurrently supported types: &lbar, scoreboard");
    private final String bossBarNotEnabled = getString("bossbar-feature-not-enabled", "&cThis command requires the bossbar feature to be enabled.");
    private final String bossBarAnnounceCommandUsage = getString("bossbar-announce-command-usage", "Usage: /tab announce bar <bar name> <length>");
    private final String bossBarAlreadyAnnounced = getString("bossbar-already-announced", "&cThis bossbar is already being announced");
    private final String parseCommandUsage = getString("parse-command-usage", "Usage: /tab parse <player> <placeholder>");
    private final String sendCommandUsage = getString("send-command-usage", "Usage: /tab send <type> <player> <bar name> <length>\nCurrently supported types: &lbar");
    private final String sendBarCommandUsage = getString("send-bar-command-usage", "Usage: /tab send bar <player> <bar name> <length>");
    private final String teamFeatureRequired = getString("team-feature-required", "This command requires scoreboard teams feature enabled");
    private final String collisionCommandUsage = getString("collision-command-usage", "Usage: /tab setcollision <player> <true/false>");
    private final String noPermission = getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
    private final String commandOnlyFromGame = getString("command-only-from-game", "&cThis command must be ran from the game");
    private final String unlimitedNametagModeNotEnabled = getString("unlimited-nametag-mode-not-enabled", "&c[TAB] Warning! To make this feature work, you need to enable unlimited-nametag-mode in the config!");
    private final String scoreboardFeatureNotEnabled = getString("scoreboard-feature-not-enabled", "&4This command requires the scoreboard feature to be enabled.");
    private final String scoreboardAnnounceCommandUsage = getString("scoreboard-announce-command-usage", "Usage: /tab scoreboard announce <scoreboard name> <length>");
    private final String nametagPreviewOn = getString("nametag-preview-on", "&7Preview mode &aactivated&7.");
    private final String nametagPreviewOff = getString("nametag-preview-of", "&7Preview mode &3deactivated&7.");
    private final String reloadSuccess = getString("reload-success", "&3[TAB] Successfully reloaded");
    private final String reloadFailBrokenFile = getString("reload-fail-file", "&3[TAB] &4Failed to reload, file %file% has broken syntax. Check console for more info.");
    private final String scoreboardOn = getString("scoreboard-toggle-on", "&2Scoreboard enabled");
    private final String scoreboardOff = getString("scoreboard-toggle-off", "&7Scoreboard disabled");
    private final String bossBarOn = getString("bossbar-toggle-on", "&2Bossbar is now visible");
    private final String bossBarOff = getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
    private final String scoreboardShowUsage = getString("scoreboard-show-usage", "Usage: /tab scoreboard show <scoreboard> [player]");
    private final String bossBarNotMarkedAsAnnouncement = getString("bossbar-not-marked-as-announcement", "&cThis bossbar is not marked as an announcement bar and is therefore " +
            "already displayed permanently (if display condition is met)");
    private final List<String> helpMenu = getStringList("help-menu", Arrays.asList("&m                                                                                "
            ," &8>> &3&l/tab reload"
            ,"    &7Reloads plugin and config"
            ," &8>> &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>"
            ,"    &7Do &8/tab group/player &7to show properties"
            ," &8>> &3&l/tab nametag preview"
            ,"    &7Shows your nametag for yourself, for testing purposes"
            ," &8>> &3&l/tab announce bar &3<name> &9<seconds>"
            ,"    &7Temporarily displays bossbar to all players"
            ," &8>> &3&l/tab parse <player> <placeholder> "
            ,"    &7Test if a placeholder works"
            ," &8>> &3&l/tab debug [player]"
            ,"    &7displays debug information about player"
            ," &8>> &3&l/tab cpu"
            ,"    &7shows CPU usage of the plugin"
            ," &8>> &3&l/tab group/player <name> remove"
            ,"    &7Clears all data about player/group"
            ,"&m                                                                                "));
    private final List<String> mySQLHelpMenu = getStringList("mysql-help-menu", Arrays.asList(
            "/tab mysql upload - uploads data from files to mysql",
            "/tab mysql download - downloads data from mysql to files"
    ));
    private final String mySQLFailNotEnabled = getString("mysql-fail-not-enabled", "&cCannot download/upload data from/to MySQL, because it's disabled.");
    private final String mySQLFailError = getString("mysql-fail-error", "MySQL download failed due to an error. Check console for more info.");
    private final String mySQLDownloadSuccess = getString("mysql-download-success", "&aMySQL data downloaded successfully.");
    private final String mySQLUploadSuccess = getString("mysql-upload-success", "&aMySQL data uploaded successfully.");
    private final List<String> nameTagHelpMenu = getStringList("nametag-help-menu", Arrays.asList(
            "/tab nametag preview [player] - toggles armor stand preview mode",
            "/tab nametag toggle [player] - toggles nametags on all players for command sender"
    ));
    private final String nameTagFeatureNotEnabled = getString("nametag-feature-not-enabled", "&cThis command requires nametag feature to be enabled.");
    private final String nameTagsHidden = getString("nametags-hidden", "&aNametags of all players were hidden to you");
    private final String nameTagsShown = getString("nametags-shown", "&aNametags of all players were shown to you");
    private final String armorStandsDisabledCannotPreview = getString("armorstands-disabled-cannot-use-preview", "&cYour armor stands are disabled, therefore you cannot use preview feature");
    private final List<String> scoreboardHelpMenu = getStringList("scoreboard-help-menu", Arrays.asList(
            "/tab scoreboard [on/off/toggle] [player] [options]",
            "/tab scoreboard show <name> [player]",
            "/tab scoreboard announce <name> <length>"
    ));
    private final List<String> bossbarHelpMenu = getStringList("bossbar-help-menu", Arrays.asList(
            "/tab bossbar [on/off/toggle] [player] [options]",
            "/tab bossbar send <name> [player]",
            "/tab bossbar announce <name> <length>"
    ));

    public MessageFile() throws IOException {
        super(MessageFile.class.getClassLoader().getResourceAsStream("messages.yml"), new File(TAB.getInstance().getDataFolder(), "messages.yml"));
    }

    public @NotNull String getBossBarNotFound(@NotNull String name) {
        return getString("bossbar-not-found", "&cNo bossbar found with the name \"%name%\"").replace("%name%", name);
    }

    public @NotNull String getGroupDataRemoved(@NotNull String group) {
        return getString("group-data-removed", "&3[TAB] All data has been successfully removed from group &e%group%").replace("%group%", group);
    }

    public @NotNull String getGroupValueAssigned(@NotNull String property, @NotNull String value, @NotNull String group) {
        return getString("group-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to group &e%group%")
                .replace("%property%", property).replace("%value%", value).replace("%group%", group);
    }

    public @NotNull String getGroupValueRemoved(@NotNull String property, @NotNull String group) {
        return getString("group-value-removed", "&3[TAB] %property% has been successfully removed from group &e%group%")
                .replace("%property%", property).replace("%group%", group);
    }

    public @NotNull String getPlayerDataRemoved(@NotNull String player) {
        return getString("user-data-removed", "&3[TAB] All data has been successfully removed from player &e%player%").replace("%player%", player);
    }

    public @NotNull String getPlayerValueAssigned(@NotNull String property, @NotNull String value, @NotNull String player) {
        return getString("user-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to player &e%player%")
                .replace("%property%", property).replace("%value%", value).replace("%player%", player);
    }

    public @NotNull String getPlayerValueRemoved(@NotNull String property, @NotNull String player) {
        return getString("user-value-removed", "&3[TAB] %property% has been successfully removed from player &e%player%")
                .replace("%property%", property).replace("%player%", player);
    }

    public @NotNull String getPlayerNotFound(@NotNull String name) {
        return getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
    }

    public @NotNull String getInvalidNumber(@NotNull String input) {
        return getString("invalid-number", "\"%input%\" is not a number!").replace("%input%", input);
    }

    public @NotNull String getScoreboardNotFound(@NotNull String name) {
        return getString("scoreboard-not-found", "&cNo scoreboard found with the name \"%name%\"").replace("%name%", name);
    }

    public @NotNull String getBossBarAnnouncementSuccess(@NotNull String bar, int length) {
        return getString("bossbar-announcement-success", "&aAnnouncing bossbar &6%bossbar% &afor %length% seconds.")
                .replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }

    public @NotNull String getBossBarSendSuccess(@NotNull String player, @NotNull String bar, int length) {
        return getString("bossbar-send-success", "&aSending bossbar &6%bossbar% &ato player &6%player% &afor %length% seconds.")
                .replace("%player%", player).replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }
}