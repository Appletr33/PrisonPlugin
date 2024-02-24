package org.teameugene.prison.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.teameugene.prison.Prison;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Serialize extends GameObject {
    protected String className;
    protected static ArrayList<org.teameugene.prison.Util.Serialize> instances = new ArrayList<>();
    private static final CustomFile dataFile = new CustomFile("objects.yml");

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Serializable { }

    protected Serialize() {
        className = getClass().getName();
        instances.add(this);
    }

    public static void onLoad() {
        FileConfiguration fc = getData();
        if (fc == null) {
            return;
        }

        // Iterate over each class section in the YAML file
        for (String className : fc.getKeys(false)) {
            // Get the section for the current class
            ConfigurationSection classSection = fc.getConfigurationSection(className);
            if (classSection == null) {
                continue;
            }

            // Iterate over each object in the class section
            for (String objectId : classSection.getKeys(false)) {
                // Get the section for the current object
                ConfigurationSection objectSection = classSection.getConfigurationSection(objectId);
                if (objectSection == null) {
                    continue;
                }

                // Create a new instance of the class
                Serialize object = createObject(className);

                if (object == null) {
                    // Skip deserialization if object creation fails
                    continue;
                }

                // Iterate over each attribute in the object section
                for (String attributeName : objectSection.getKeys(false)) {
                    // Get the section for the current attribute
                    ConfigurationSection attributeSection = objectSection.getConfigurationSection(attributeName);
                    if (attributeSection == null) {
                        continue;
                    }

                    // Get the type and value of the attribute
                    String type = attributeSection.getString("Type");
                    String value = attributeSection.getString("Value");

                    // Set the attribute value based on its type
                    setAttribute(object, attributeName, type, value);
                }
            }
        }
    }

    private static void setAttribute(Serialize object, String attributeName, String type, String value) {
        try {
            Field field = object.getClass().getDeclaredField(attributeName);
            field.setAccessible(true);

            switch (type) {
                case "class java.lang.Boolean":
                    field.set(object, Boolean.parseBoolean(value));
                    break;
                case "class java.lang.Integer":
                    field.set(object, Integer.parseInt(value));
                    break;
                case "class java.lang.Double":
                    field.set(object, Double.parseDouble(value));
                    break;
                case "class java.lang.String":
                    field.set(object, value);
                    break;
                case "class org.bukkit.Location":
                    // Convert the string representation of the Location to a Location object
                    Location location = deserializeLocation(value);
                    field.set(object, location);
                    break;
                // Add cases for other types as needed
                default:
                    throw new IllegalArgumentException("[Deserialization] Unsupported type: " + type + " For class " + object.getClass().toString());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Location deserializeLocation(String locationString) {
        // Parse the string representation of the Location and create a Location object
        // This is just a simple example, you may need to adjust this based on the actual format of the string
        String[] parts = locationString.split(",");
        String worldName = parts[0].split("=")[2];
        worldName = worldName.replace("}", "");

        double x = Double.parseDouble(parts[1].split("=")[1]);
        double y = Double.parseDouble(parts[2].split("=")[1]);
        double z = Double.parseDouble(parts[3].split("=")[1]);
        float yaw = Float.parseFloat(parts[5].split("=")[1].replace("}", ""));
        float pitch = Float.parseFloat(parts[4].split("=")[1]);
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    private static ArrayList<String[]> getAttributes(Serialize obj) {

        ArrayList<String[]> attributes = new ArrayList<>();

        // Get the class of the object
        Class<?> clazz = obj.getClass();

        // Get all declared fields of the class
        Field[] fields = clazz.getDeclaredFields();

        // Iterate through each field
        for (Field field : fields) {
            // Check if the field has the IncludeInPrint annotation
            if (field.isAnnotationPresent(Serializable.class)) {
                // Make the field accessible (in case it's private)
                field.setAccessible(true);
                try {
                    // Get the name of the field
                    String fieldName = field.getName();
                    // Get the value of the field for the given object
                    Object fieldValue = field.get(obj);
                    // Print the name and value of the field
                    attributes.add(new String[]{fieldName, fieldValue.getClass().toString(), fieldValue.toString()});
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return attributes;
    }

    public static void onSave() {
        FileConfiguration fc = getData();
        clearFile(fc);

        for (Serialize object : instances) {
            ArrayList<String[]> attributes = getAttributes(object);

            //Generate Unique hash for object
            StringBuilder concatenatedStringBuilder = new StringBuilder();
            for (String[] array : attributes) {
                concatenatedStringBuilder.append(String.join("", array));
            }
            String concatenatedString = concatenatedStringBuilder.toString();
            String hashId = Utils.getHash(String.join("", concatenatedString));


            saveObject(fc, object.getClassName(), hashId, attributes);
        }
        saveData();
    }

    public static void clearFile(FileConfiguration fc) {
        fc.options().copyDefaults(true);
        for (String key : fc.getKeys(false)) {
            fc.set(key, null);
        }
    }

    private static void saveObject(FileConfiguration fc, String className, String id, ArrayList<String[]> attributes) {
        ConfigurationSection attributesSection;

        className = className.replace('.', '|');

        try {
            attributesSection = fc.createSection(className + "." + id);

        } catch (java.lang.IllegalArgumentException e) {
            ConfigurationSection classSection = fc.createSection(className);
            attributesSection = classSection.createSection(id);
        }

        for (String[] attribute : attributes) {
            ConfigurationSection nameSection = attributesSection.createSection(attribute[0]);
            nameSection.set("Type", attribute[1]);
            nameSection.set("Value", attribute[2]);
        }
    }

    private static FileConfiguration getData() {
        return dataFile.getConfig();
    }

    private static void saveData() {
        dataFile.saveConfig();
    }

    public String getClassName() {
        return className;
    }

    private static Serialize createObject(String className) {
        try {
            className = className.replace('|', '.');

            // Get the Class object for the specified class name
            Class<?> clazz = Class.forName(className);

            // Instantiate the class using its no-argument constructor
            Object object = clazz.getDeclaredConstructor().newInstance();

            // Check if the instantiated object is an instance of Serialize
            if (object instanceof Serialize) {
                // Return the instantiated object as a Serialize instance
                return (Serialize) object;
            } else {
                // Handle the case where the class is not a subclass of Serialize
                throw new IllegalArgumentException("Class " + className + " does not extend Serialize");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            // Handle any exceptions that may occur during instantiation
            e.printStackTrace();
            return null;
        }
    }
}
