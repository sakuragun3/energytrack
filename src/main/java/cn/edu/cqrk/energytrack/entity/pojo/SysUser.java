package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_name")
    private String username;

    @TableField("user_password")
    private String password;

    @TableField("user_role")
    private String role;

    @TableField("user_email")
    private String email;

    @TableField("user_phone")
    private String phone;

    @TableField("user_status")
    private String status;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}