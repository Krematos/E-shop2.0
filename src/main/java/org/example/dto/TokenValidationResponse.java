package org.example.dto;

import java.util.List;

public record TokenValidationResponse(boolean valid, String username, List<String> role) {
}
