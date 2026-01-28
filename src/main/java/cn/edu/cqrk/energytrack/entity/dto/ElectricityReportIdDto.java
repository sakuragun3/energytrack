package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ElectricityReportIdDto implements Serializable {
    @NotNull(message = "报表ID不能为空")
    private Integer reportId;
}