package cn.edu.cqrk.energytrack.service;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.Meter;
import cn.edu.cqrk.energytrack.entity.vo.MeterVo;
import com.baomidou.mybatisplus.extension.service.IService;
import net.sf.jasperreports.engine.JRException;

import java.sql.SQLException;
import java.util.List;

public interface MeterService extends IService<Meter> {
    MeterVo add(MeterAddDto dto);
    boolean deleteById(Integer meterId);
    MeterVo update(MeterUpdateDto dto) throws BizException;
    List<MeterVo> search(MeterSearchDto dto);
    boolean checkMeterId(Integer meterId);
    }