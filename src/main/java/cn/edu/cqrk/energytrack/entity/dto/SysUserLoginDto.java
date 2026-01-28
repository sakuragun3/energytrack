package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.*;
import java.io.Serializable;

@Data
public class SysUserLoginDto implements Serializable {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

}
