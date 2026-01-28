package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ElectricityReportSearchDto implements Serializable {
    private Integer meterId;
    private Date startTime;
    private Date endTime;
}