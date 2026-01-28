package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeterSearchDto implements Serializable {
    private String meterLocation; // 安装位置
    private String meterType; // 电表类型
    private String meterStatus; // 电表状态
}