package org.cartman.database;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import org.cartman.database.collections.User;

public final class DBUtils {
    /**
     * Creates a new username in the system.
     * @param userId The discord user id.
     * @param guildId The discord guild id.
     * @param guildIdOwner The discord guild owner's id.
     */
    public static void addUserObject(long userId, long guildId, long guildIdOwner) {
        User userObj = new User(userId, guildId);
        userObj.setPrivilegeId(0);
        userObj.save();
        DBManager.getUsers().put(userId, userObj);
    }

    /**
     * Searches for user instance by given userId.
     * @param userId The given userId.
     * @return The user instance if exist or else null.
     */
    public static User findUserObjectById(long userId) {
        return DBManager.getDataStore().find(User.class).filter(eq("userId", userId)).first();
    }
}