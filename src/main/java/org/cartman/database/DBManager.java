package org.cartman.database;

// Imports
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.cartman.Application;
import org.cartman.database.collections.*;

public final class DBManager {
    @Getter private static Datastore dataStore;
    @Getter private static final Map<Long, User> users = new HashMap<>();
    @Getter private static final List<String> proxies = new ArrayList<>();
    @Getter private static final List<String> reportedUrls = new ArrayList<>();
    private static final ExecutorService eventExecutor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());
    private static final boolean applyChanges = true;

    /**
     * Starts the mongodb database.
     */
    public static void initializeDatabase() {
        String dbUrl = Application.getConfigParser().getString("mongodb.dburl");
        String dbName = Application.getConfigParser().getString("mongodb.dbname");
        if (dbUrl == null || dbName == null) {
            throw new IllegalStateException("Missing MongoDB URI or database name in the config");
        }

        MapperOptions mapperOptions = MapperOptions.builder().storeEmpties(true).storeNulls(false).build();
        dataStore = Morphia.createDatastore(MongoClients.create(dbUrl), dbName, mapperOptions);
        dataStore.ensureIndexes();
        Application.getLogger().info("Connected to database: {}", dbName);

        List<User> userList = dataStore.find(User.class).iterator().toList();
        for (User user : userList) {
            users.put(user.getUserId(), user);
        }

        Application.getLogger().info("Loaded {} users into memory.", users.size());
    }

    /**
     * Deletes an object into the database.
     * @param object The object to delete.
     */
    public static void deleteInstance(Object object) {
        if(applyChanges) {
            eventExecutor.submit(() -> getDataStore().delete(object));
        }
    }

    /**
     * Saves an object into the database.
     * @param object The object to save.
     */
    public static void saveInstance(Object object) {
        if(applyChanges) {
            eventExecutor.submit(() -> getDataStore().save(object));
        }
    }
}