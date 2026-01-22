package org.example.dto.user;

import java.util.Set;

public record UserInfoResponse (

        Long id,
        String username,
        String email,
        Set<String> roles
) {
}
