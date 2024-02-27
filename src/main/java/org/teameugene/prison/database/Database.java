package org.teameugene.prison.database;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Util.BytePacking;
import org.teameugene.prison.enums.Difficulty;
import org.teameugene.prison.enums.Ore;
import org.teameugene.prison.mine.Asteroid;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class Database {
    Plugin plugin;
    private Connection connection;

    public boolean isConnected() {
        if (connection == null) return false;
        else return true;
    }

    public Database(Plugin plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    private void setupDatabase() {
        //Load Database creds
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        String port = config.getString("database.port");
        String database = config.getString("database.name");
        String username = config.getString("database.username");
        String password = config.getString("database.password");


        // JDBC connection URL
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";

        try {
            // Register the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the database
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to connect to the database. Disabling plugin.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlayerInDatabase(String uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players WHERE player_uuid = ?")) {
            statement.setString(1, uuid);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if the player is in the database
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createPlayerEntry(String uuid) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players (player_uuid) VALUES (?)")) {
            statement.setString(1, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double x = getHighestXValue();
        if (x != -1.0) {
            x += 500;
            setPlayerShipCoordinates(uuid, x, 70, 0);
        }
        else {
            plugin.getLogger().info("[ERROR]: Getting highest x value from database failed!");
        }
    }

    private void setPlayerShipCoordinates(String uuid, double x, double y, double z) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO ships (player_id, x, y, z) VALUES ((SELECT id FROM players WHERE player_uuid = ?), ?, ?, ?)")) {
            statement.setString(1, uuid);
            statement.setDouble(2, x);
            statement.setDouble(3, y);
            statement.setDouble(4, z);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double[] getPlayerShipCoordinates(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT x, y, z FROM ships WHERE player_id = (SELECT id FROM players WHERE player_uuid = ?)")) {
            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");

                    return new double[]{x, y, z};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[]{0, 0, 0}; // Return 0,0,0 if no coordinates are found
    }

    private double getHighestXValue() {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT MAX(x) AS highest_x FROM ships")) {

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("highest_x");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1.0; // Return a default value if no coordinates are found
    }

    public long getPoints(UUID playerUUID) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            // Prepare SQL statement
            String sql = "SELECT points FROM players WHERE player_uuid = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, playerUUID.toString());

            // Execute query
            resultSet = statement.executeQuery();

            // Check if a result is found
            if (resultSet.next()) {
                // Retrieve and return the points value
                return resultSet.getLong("points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the reverse order of their creation
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Return a default value (e.g., 0) if no points are found
        plugin.getServer().broadcastMessage("NO POINTS FOUND");
        return 0;
    }

    public void updatePoints(UUID playerUUID, long newPoints) {
        if (connection == null) return;

        PreparedStatement statement = null;
        try {
            // Prepare SQL statement to update points for a specific player
            String sql = "UPDATE players SET points = ? WHERE player_uuid = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, newPoints);
            statement.setString(2, playerUUID.toString());

            // Execute the update
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the reverse order of their creation
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void incrementPoints(UUID playerUUID, long pointsToAdd) {
        if (connection == null) return;

        PreparedStatement statement = null;
        try {
            // Prepare SQL statement to increment points for a specific player
            String sql = "UPDATE players SET points = points + ? WHERE player_uuid = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, pointsToAdd);
            statement.setString(2, playerUUID.toString());

            // Execute the update
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the reverse order of their creation
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getLeaderboard(int topN) {
        List<String> leaderboard = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT player_uuid, points FROM players ORDER BY points DESC LIMIT ?")) {
            statement.setInt(1, topN);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID playerUUID = UUID.fromString(resultSet.getString("player_uuid"));
                    long points = resultSet.getLong("points");
                    String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
                    leaderboard.add(playerName + " - credits: " + ChatColor.GOLD + points);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Collections.reverse(leaderboard);

        return leaderboard;
    }

    public float findAsteroidWithLargestPositionX() {
        try {
            // Create a statement
            Statement stmt = connection.createStatement();

            // SQL query to retrieve the asteroid with the largest position_x
            String sql = "SELECT * FROM asteroids ORDER BY position_x DESC LIMIT 1";

            // Execute the query
            ResultSet rs = stmt.executeQuery(sql);

            // Check if any rows were returned
            if (rs.next()) {
                return rs.getFloat("position_x");
            } else {
                return 0f;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void addAsteroid(float positionX, int difficulty, int radius, long oreTypes, UUID playerUUID) {
        try {
            // Get the player_id based on the UUID
            int playerId = getPlayerIdByUUID(connection, playerUUID);

            // Create a prepared statement to insert the asteroid
            String sql = "INSERT INTO asteroids (position_x, difficulty, radius, oretypes, player_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setFloat(1, positionX);
                pstmt.setInt(2, difficulty);
                pstmt.setInt(3, radius);
                pstmt.setLong(4, oreTypes);
                pstmt.setInt(5, playerId);

                // Execute the insert statement
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getPlayerIdByUUID(Connection conn, UUID playerUUID) throws SQLException {
        int playerId = -1;
        String sql = "SELECT id FROM players WHERE player_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    playerId = rs.getInt("id");
                }
            }
        }
        return playerId;
    }

    public ArrayList<Asteroid> getAsteroidsByPlayerUUID(UUID playerUUID) {
        ArrayList<Asteroid> asteroids = new ArrayList<>();
        try {
            int playerId = getPlayerIdByUUID(connection, playerUUID);
            String sql = "SELECT id, position_x, difficulty, radius, oretypes FROM asteroids WHERE player_id = ? ORDER BY id";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, playerId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        float positionX = rs.getFloat("position_x");
                        int difficulty = rs.getInt("difficulty");
                        int radius = rs.getInt("radius");
                        long oreTypes = rs.getLong("oretypes");

                        //Unpack the data
                        Difficulty difficulty1 = Difficulty.values()[difficulty];
                        byte[] oreBytes = BytePacking.unpackBytes(oreTypes);
                        ArrayList<Ore> oreTypes1 = new ArrayList<>();
                        for (int i = 0; i < oreBytes.length; i++) {
                            int ordinal = ((int) oreBytes[i]) - 1;
                            if (ordinal < 0)
                                continue;
                            oreTypes1.add(Ore.values()[ordinal]);
                        }

                        // Create an Asteroid object and add it to the list
                        Asteroid asteroid = new Asteroid(id, positionX, difficulty1, radius, oreTypes1);
                        asteroids.add(asteroid);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return asteroids;
    }

}
