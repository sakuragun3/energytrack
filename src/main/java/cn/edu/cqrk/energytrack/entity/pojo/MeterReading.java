package cn.edu.cqrk.energytrack.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("meter_reading")
public class MeterReading implements Serializable {

    @TableId(value = "reading_id", type = IdType.AUTO)
    private Integer readingId; // 读数编号

    @TableField("meter_id")
    private Integer meterId; // 电表编号

    @TableField("reading_value")
    private BigDecimal readingValue; // 当前读数

    @TableField("reading_time")
    private Date readingTime; // 读数时间

    @TableField("create_time")
    private Date createTime; // 创建时间

    @TableField("update_time")
    private Date updateTime; // 更新时间
}