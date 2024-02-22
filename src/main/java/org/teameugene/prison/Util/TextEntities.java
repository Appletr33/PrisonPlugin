package org.teameugene.prison.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.teameugene.prison.Prison;

import java.util.*;

import static org.teameugene.prison.Util.Utils.getKey;
import static org.teameugene.prison.Util.Utils.getWorldByName;

public class TextEntities {
    public static Map<String, ArrayList<ArmorStand>> textObjects = new HashMap<>();

    static String leaderboardIdentifier = "leaderboard";
    static Location leaderBoardCoords = new Location(getWorldByName(Prison.moonWorldName), -1778, 25, 811);
    static float textHeightOffset = 0.25f;

    public static void initialize() {
        textObjects.put(leaderboardIdentifier, getTextEntities(getWorldByName(Prison.moonWorldName), leaderboardIdentifier));
    }

    public static ArmorStand spawnTextEntity(Location location, String text, String identifier ) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(text);
        armorStand.setGravity(false);
        armorStand.setAI(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.getPersistentDataContainer().set(
                // Use your own unique key, here it's "custom_metadata_key"
                getKey("text"),
                // Use your own data type, here it's PersistentDataType.STRING
                PersistentDataType.STRING,
                // Set your custom data value, here it's "your_custom_data"
                identifier
        );
        return armorStand;
    }

    private static void destroyTextEntities(World world, String identifier) {
        for (Entity entity : world.getEntities()) {
            // Check if the entity is an ArmorStand
            if (entity instanceof ArmorStand armorStand) {
                if (isTextEntityOfType(armorStand, identifier)) {
                    armorStand.remove();
                }
            }
        }
    }

    private static ArrayList<ArmorStand> getTextEntities(World world, String identifier) {
        ArrayList<ArmorStand> armorStands = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            // Check if the entity is an ArmorStand
            if (entity instanceof ArmorStand armorStand) {
                if (isTextEntityOfType(armorStand, identifier)) {
                    armorStands.add(armorStand);
                }
            }
        }
        return armorStands;
    }

    private static boolean isTextEntityOfType(ArmorStand armorStand, String identifier) {
        if (armorStand.getPersistentDataContainer().has(
                // Use the same key used when adding the metadata
                getKey("text"),
                // Use the same data type used when adding the metadata
                PersistentDataType.STRING
        )) {
            String customData = armorStand.getPersistentDataContainer().get(
                    // Use the same key used when adding the metadata
                    getKey("text"),
                    // Use the same data type used when adding the metadata
                    PersistentDataType.STRING
            );
            if (customData != null)
                return customData.equals(identifier);
        }
        return false;
    }

    private static void setTextEntityName(ArmorStand armorStand, String text) {
        armorStand.setCustomName(text);
    }

    private static void generateLeaderBoard(int topN, List<String> leaderboard, Location displayLocation) {
        // THIS MADE MY HEAD MELT I HAVE NO IDEA WHATS GOING ON HERE HELP
        ArrayList<ArmorStand> spawnedArmorStands = new ArrayList<>();

        int leaderboardSize = leaderboard.size() - 1;
        int rank = 1;
        for (int i = topN; i > 0; i--) {
            String text;
            if (i == topN) {
                text = ChatColor.BOLD + "" + ChatColor.GREEN + "----- LEADER BOARD ------";
            }
            else if (leaderboard.size() > rank ) {
                text = rank + ".    " + leaderboard.get(leaderboardSize - 1) + "    ";
                rank++;
                leaderboardSize--;
            } else {
                text = rank + ".    "  + "----- -----";
                rank++;
                leaderboardSize--;
            }

            spawnedArmorStands.add(spawnTextEntity(displayLocation.clone().add(0, i * textHeightOffset, 0), text, leaderboardIdentifier));
        }
        textObjects.put(leaderboardIdentifier, spawnedArmorStands);
    }

    private static boolean updateTextEntities(List<String> names, ArrayList<ArmorStand> armorStandsToUpdate) { //must be sent in order of updating and must be same length
        if (names.size() != armorStandsToUpdate.size())
            return false;
        for (int i = 0; i < armorStandsToUpdate.size(); i++) {
            setTextEntityName(armorStandsToUpdate.get(i), names.get(i));
        }
        return true;
    }

    public static void displayLeaderboard() {
        int topN = 11; // only get top 10 players + 1 for title
        List<String> leaderboard = Prison.database.getLeaderboard(topN);
        leaderboard.add(ChatColor.BOLD + "" + ChatColor.GREEN + "----- LEADER BOARD ------");
        Location displayLocation = leaderBoardCoords;

        ArrayList<ArmorStand> leaderboardTexts = textObjects.get(leaderboardIdentifier);
        destroyTextEntities(getWorldByName(Prison.moonWorldName), leaderboardIdentifier);
        for (Iterator<ArmorStand> iterator = leaderboardTexts.iterator(); iterator.hasNext();) {
            ArmorStand armorStand = iterator.next();
            armorStand.remove();
            iterator.remove(); // Safe removal during iteration
        }
        generateLeaderBoard(topN, leaderboard, displayLocation);
    }
}
