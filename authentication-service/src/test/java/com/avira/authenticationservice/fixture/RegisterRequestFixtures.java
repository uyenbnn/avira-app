package com.avira.authenticationservice.fixture;
import com.avira.authenticationservice.dto.RegisterRequest;
/**
 * Reusable example data for {@link RegisterRequest} across tests.
 */
public final class RegisterRequestFixtures {
    /** A regular buyer/user account. */
    public static final RegisterRequest ALICE = new RegisterRequest(
            "alice",
            "alice@avira.com",
            "StrongPass123!",
            "Alice",
            "Smith"
    );
    /** A seller account. */
    public static final RegisterRequest BOB = new RegisterRequest(
            "bob_seller",
            "bob@avira.com",
            "SecurePass456!",
            "Bob",
            "Jones"
    );
    /** An admin account. */
    public static final RegisterRequest ADMIN = new RegisterRequest(
            "avira-admin",
            "admin@avira.com",
            "AdminPass789!",
            "Default",
            "Admin"
    );
    /** Minimal valid request - useful for boundary / validation tests. */
    public static final RegisterRequest MINIMAL = new RegisterRequest(
            "user1",
            "user1@avira.com",
            "Pass123!",
            "First",
            "Last"
    );
    private RegisterRequestFixtures() {
    }
}
