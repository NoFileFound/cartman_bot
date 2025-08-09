package org.cartman.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.cartman.database.DBManager;

@Entity(value = "users", useDiscriminator = false)
@Getter
public final class User {
    private @Id ObjectId id; // default
    private final long userId;
    private final long guildId;
    private final long registeredDate;
    @Setter private int privilegeId;
    @Setter private int karma;

    /**
     * Creates a new username.
     * @param userId The discord user id.
     * @param guildId The discord guild id.
     */
    public User(final long userId, final long guildId) {
        this.userId = userId;
        this.guildId = guildId;
        this.registeredDate = System.currentTimeMillis();
    }

    /**
     * Deletes the user from the database.
     */
    public void delete() {
        DBManager.deleteInstance(this);
    }

    /**
     * Updates the user in the database.
     */
    public void save() {
        DBManager.saveInstance(this);
    }
}