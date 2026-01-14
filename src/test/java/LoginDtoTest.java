import org.example.dto.LoginRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
public class LoginDtoTest {

    @Mock
    private LoginRequest loginDto;
    /*
    @BeforeEach
    void setUp() {
        loginDto = new LoginDto();
    }

    @Test
    void testGetUsername() {
        loginDto.setUsername("testUser");
        assertEquals("testUser", loginDto.getUsername());
    }

    @Test
    void testGetPassword() {
        loginDto.setPassword("testPass");
        assertEquals("testPass", loginDto.getPassword());
    }*/


}
