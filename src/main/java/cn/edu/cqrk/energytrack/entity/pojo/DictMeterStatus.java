package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("dict_meter_status")
public class DictMeterStatus implements Serializable {

    @TableId(value = "status_code", type = IdType.INPUT)
    private String statusCode; // 状态编码

    @TableField("status_name")
    private String statusName; // 状态名称

    @TableField("status_description")
    private String statusDescription; // 状态描述

    @TableField("is_enabled")
    private Boolean isEnabled; // 是否启用
}