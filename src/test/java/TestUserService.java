import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TestUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public String getPasswordByUsername(String username) {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user != null) {
            return user.getPassword();
        }
        return null;
    }
}

