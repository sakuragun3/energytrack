package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("meter")
public class Meter implements Serializable {

    @TableId(value = "meter_id", type = IdType.AUTO)
    private Integer meterId; // 电表编号

    @TableField("meter_location")
    private String meterLocation; // 安装位置

    @TableField("meter_type")
    private String meterType; // 电表类型

    @TableField("meter_status")
    private String meterStatus; // 电表状态

    @TableField("create_time")
    private Date createTime; // 创建时间

    @TableField("update_time")
    private Date updateTime; // 更新时间
}