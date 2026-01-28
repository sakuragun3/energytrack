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
public class MeterReadingVo implements Serializable {
    private Integer readingId; // 读数编号
    private Integer meterId; // 电表编号
    private BigDecimal readingValue; // 当前读数
    private Date readingTime; // 读数时间
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}