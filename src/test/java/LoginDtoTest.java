import org.example.dto.LoginDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
public class LoginDtoTest {

    @Mock
    private LoginDto loginDto;

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
    }


}
