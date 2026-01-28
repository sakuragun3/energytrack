package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("dict_user_role")
public class DictUserRole implements Serializable {

    @TableId(value = "role_code", type = IdType.INPUT)
    private String roleCode; // 角色编码

    @TableField("role_name")
    private String roleName; // 角色名称

    @TableField("role_description")
    private String roleDescription; // 角色描述

    @TableField("is_enabled")
    private Boolean isEnabled; // 是否启用
}