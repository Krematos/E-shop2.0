package krematos.event;

import krematos.model.User;

public record UserRegisteredEvent(User user) {
}
