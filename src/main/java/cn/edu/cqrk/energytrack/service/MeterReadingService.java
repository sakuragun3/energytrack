package cn.edu.cqrk.energytrack.service;

import cn.edu.cqrk.energytrack.entity.dto.MeterReadingDto;
import cn.edu.cqrk.energytrack.entity.pojo.MeterReading;
import cn.edu.cqrk.energytrack.entity.vo.MeterReadingVo;
import com.baomidou.mybatisplus.extension.service.IService;
import net.sf.jasperreports.engine.JRException;

import java.sql.SQLException;
import java.util.List;

public interface MeterReadingService extends IService<MeterReading> {
    MeterReadingVo add(MeterReadingDto dto);
    List<MeterReadingVo> getReadingsByMeterId(Integer meterId, String startTime, String endTime);
    byte[] generateReadingReport(Integer meterId, String startTime, String endTime) throws JRException;
    MeterReadingVo update(MeterReadingDto dto);
    boolean deleteById(Integer readingId);
}