package org.example.event;

import org.example.model.User;

public record UserRegisteredEvent(User user) {
}
