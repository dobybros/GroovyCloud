package chat.utils;

import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

public class PropertiesContainer {
    private static PropertiesContainer instance;
    private static Properties properties;

    public static synchronized PropertiesContainer getInstance() {
        if (instance == null) {
            instance = new PropertiesContainer();
            ClassPathResource configResource = new ClassPathResource("container.properties");
            if (properties == null)
                properties = new Properties();
            try {
                properties.load(configResource.getInputStream());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                try {
                    configResource.getInputStream().close();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return instance;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}
