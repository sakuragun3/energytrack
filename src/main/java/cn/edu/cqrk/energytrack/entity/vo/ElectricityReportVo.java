package cn.edu.cqrk.energytrack.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityReportVo implements Serializable {
    private Integer reportId; // 报表编号
    private Integer meterId; // 电表编号
    private Date startTime; // 报表起始时间
    private Date endTime; // 报表结束时间
    private BigDecimal totalConsumption; // 总用电量
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}