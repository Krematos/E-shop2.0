import org.example.dto.user.UserRegistrationRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RegisterDtoTest {

    @Mock
    private UserRegistrationRequest registerDto;
    /*
    @BeforeEach
    void setUp() {
        registerDto = new RegisterDto();
    }

    @Test
    void testGetUsername() {
        registerDto.setUsername("testUser");
        assertEquals("testUser", registerDto.getUsername());
    }

    @Test
    void testGetPassword() {
        registerDto.setPassword("testPass");
        assertEquals("testPass", registerDto.getPassword());
    }*/
}
