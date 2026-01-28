package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("dict_meter_type")
public class DictMeterType implements Serializable {

    @TableId(value = "type_code", type = IdType.INPUT)
    private String typeCode; // 类型编码

    @TableField("type_name")
    private String typeName; // 类型名称

    @TableField("type_description")
    private String typeDescription; // 类型描述

    @TableField("is_enabled")
    private Boolean isEnabled; // 是否启用
}