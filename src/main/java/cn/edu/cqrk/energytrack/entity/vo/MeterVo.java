package cn.edu.cqrk.energytrack.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterVo implements Serializable {
    private Integer meterId; // 电表编号
    private String meterLocation; // 安装位置
    private String meterType; // 电表类型
    private String meterStatus; // 电表状态
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}