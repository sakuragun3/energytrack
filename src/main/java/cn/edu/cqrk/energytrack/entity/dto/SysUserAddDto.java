package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.io.Serializable;

@Data
public class SysUserAddDto implements Serializable {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "状态不能为空")
    private String status;
}