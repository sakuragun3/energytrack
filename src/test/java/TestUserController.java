import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestUserController {

    @Autowired
    private TestUserService testUserService;

    @GetMapping("/test/password/{username}")
    public String getPassword(@PathVariable String username) {
        return testUserService.getPasswordByUsername(username);
    }
}