package krematos.event;

import krematos.model.User;

public record UserRegisteredEvent(User user) {
    public boolean getUser() {
        return user != null;
    }
}
