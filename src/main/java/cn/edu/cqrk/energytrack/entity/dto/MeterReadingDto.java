package cn.edu.cqrk.energytrack.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class MeterReadingDto implements Serializable {
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    private Integer readingId;

    @NotNull(message = "读数值不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private BigDecimal readingValue;

    @NotNull(message = "读数时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date readingTime;
}