package com.avira.commonlib.messaging.user;

public record UserRegisteredEvent(
        String userId,
        String username,
        String email,
        String firstName,
        String lastName
) {
}
