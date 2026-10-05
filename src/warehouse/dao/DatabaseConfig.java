package warehouse.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseConfig {
    private static final Path CONFIG_FILE = Paths.get("config", "database.properties");
    private static Properties props;

    static {
        props = new Properties();
        try (InputStream in = openConfigStream()) {
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Nuk u gjet config/database.properties. Kopjoni database.properties.example dhe plotësoni kredencialet Supabase.");
        }
    }

    private static InputStream openConfigStream() throws IOException {
        if (Files.exists(CONFIG_FILE)) {
            return Files.newInputStream(CONFIG_FILE);
        }
        InputStream resource = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties");
        if (resource != null) {
            return resource;
        }
        throw new IOException("Mungon skedari " + CONFIG_FILE);
    }

    public static String getUrl() {
        return props.getProperty("db.url");
    }

    public static String getUser() {
        return props.getProperty("db.user");
    }

    public static String getPassword() {
        return props.getProperty("db.password");
    }
}
