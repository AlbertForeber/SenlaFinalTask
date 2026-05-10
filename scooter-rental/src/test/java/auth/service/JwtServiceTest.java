package auth.service;

import com.chump.auth.service.JwtService;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import com.chump.user.model.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Service testing")
public class JwtServiceTest {

    private JwtService service;
    private final static String PRIVATE_KEY = "12345678123456781234567812345678";

    @BeforeEach
    public void init() {
        service = new JwtService();

        ReflectionTestUtils.setField(service, "secretKey", PRIVATE_KEY);
        ReflectionTestUtils.setField(service, "expirationTime", 1000000);
    }

    @Test
    @Tag("unit")
    @DisplayName("User ID get method must return ID, if token is valid")
    public void getUserIdShouldReturnIdWhenValid() {
        User user = User.builder()
                .id(1)
                .role(new Role(
                        1,
                        "test",
                        Collections.emptyList()
                ))
                .build();

        String token = service.generateToken(user);
        assertEquals(1, service.getUserId(token));
    }

    @Test
    @Tag("unit")
    @DisplayName("User ID get method must throw an exception, if token is not valid")
    public void getUserIdShouldThrowWhenNotValid() {
        String token = "random_combination";
        assertThrows(JwtException.class, () -> service.getUserId(token));
    }

    @Test
    @Tag("unit")
    @DisplayName("Scopes get method must return scopes, if token is valid")
    public void getScopesShouldReturnScopesWhenValid() {
        List<Scope> scopes = List.of(new Scope(1, "test:scope"));
        User user = User.builder()
                .id(1)
                .role(new Role(
                        1,
                        "test",
                        scopes
                )).build();

        String token = service.generateToken(user);
        Collection<? extends GrantedAuthority> actual = service.getScopes(token);

        assertAll("Returned scopes validation",
                () -> assertEquals(1, actual.size(),
                        "Returning scope list must contain the only element"),
                () -> assertEquals("SCOPE_test:scope", actual.stream().findFirst().isPresent() ?
                        actual.stream().findFirst().get().getAuthority() : "",
                        "The only element in returned list must be given scope")
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Scopes get method must throw an exception, if token is not valid")
    public void getScopesShouldThrowWhenNotValid() {
        String token = "random_combination";
        assertThrows(JwtException.class, () -> service.getScopes(token));
    }
}