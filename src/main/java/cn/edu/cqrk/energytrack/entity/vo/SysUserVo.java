package cn.edu.cqrk.energytrack.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserVo implements Serializable {
    private Integer id;
    private String username;
    private String role;
    private String email;
    private String phone;
    private String status;
    private Date createTime;
    private Date updateTime;
}