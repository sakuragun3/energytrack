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
@TableName("electricity_report")
public class ElectricityReport implements Serializable {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId; // 报表编号

    @TableField("meter_id")
    private Integer meterId; // 电表编号

    @TableField("start_time")
    private Date startTime; // 报表起始时间

    @TableField("end_time")
    private Date endTime; // 报表结束时间

    @TableField("total_consumption")
    private BigDecimal totalConsumption; // 总用电量

    @TableField("create_time")
    private Date createTime; // 创建时间

    @TableField("update_time")
    private Date updateTime; // 更新时间
}