package dev.morphia.test.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Validation;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@Entity("users")
@Validation("{ age : { $gte : 13 } }")
public final class User {
    public String name;
    public List<String> likes;
    public int age;
    @Id
    private ObjectId id;
    private Date joined;

    private User() {
    }

    public User(final String name, final Date joined, final String... likes) {
        this.name = name;
        this.joined = joined;
        this.likes = asList(likes);
    }

    @Override
    public String toString() {
        return format("User{id=%s, name='%s', joined=%s, likes=%s}", id, name, joined, likes);
    }
}
