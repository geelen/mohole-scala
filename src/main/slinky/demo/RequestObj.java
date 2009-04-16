package slinky.demo;

import com.google.appengine.api.users.User;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public final class RequestObj {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String content;

    @Persistent
    private User user;

    public RequestObj(final String content, final User user) {
        this.content = content;
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }
}
