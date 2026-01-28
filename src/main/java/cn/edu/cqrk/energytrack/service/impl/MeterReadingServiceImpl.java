package cn.edu.cqrk.energytrack.service.impl;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.entity.dto.MeterReadingDto;
import cn.edu.cqrk.energytrack.entity.pojo.Meter;
import cn.edu.cqrk.energytrack.entity.pojo.MeterReading;
import cn.edu.cqrk.energytrack.entity.vo.MeterReadingVo;
import cn.edu.cqrk.energytrack.mapper.MeterMapper;
import cn.edu.cqrk.energytrack.mapper.MeterReadingMapper;
import cn.edu.cqrk.energytrack.service.MeterReadingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.Timestamp;

/**
 * 电表读数服务实现类
 * 处理电表读数的增删改查、报表生成等业务逻辑
 */
@Service
public class MeterReadingServiceImpl extends ServiceImpl<MeterReadingMapper, MeterReading> implements MeterReadingService {

    private final MeterMapper meterMapper;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MeterReadingMapper meterReadingMapper;

    /**
     * 构造函数，注入电表Mapper
     * @param meterMapper 电表Mapper
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MeterReadingServiceImpl(MeterMapper meterMapper) {
        this.meterMapper = meterMapper;
    }

    /**
     * 添加电表读数记录
     * @param dto 包含读数信息的DTO
     * @return 新创建的读数VO对象
     * @throws BizException 当电表ID无效、读数值无效或读数时间无效时抛出
     */
    @Override
    @CacheEvict(value = "meterReadings", allEntries = true)
    public MeterReadingVo add(MeterReadingDto dto) {
        // 验证 meterId 是否存在
        if (dto.getMeterId() == null) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不能为空");
        }
        LambdaQueryWrapper<Meter> meterQuery = Wrappers.lambdaQuery(Meter.class)
                .eq(Meter::getMeterId, dto.getMeterId());
        if (meterMapper.selectCount(meterQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不存在: " + dto.getMeterId());
        }

        // 验证 readingValue
        if (dto.getReadingValue() == null || dto.getReadingValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(BizExceptionCode.INVALID_READING_VALUE, "读数值必须为非负数");
        }

        // 验证 readingTime
        if (dto.getReadingTime() == null) {
            throw new BizException(BizExceptionCode.INVALID_READING_TIME, "读数时间不能为空");
        }
        if (dto.getReadingTime().after(new Date())) {
            throw new BizException(BizExceptionCode.INVALID_READING_TIME, "读数时间不能晚于当前时间");
        }

        // 创建读数记录
        MeterReading reading = new MeterReading();
        BeanUtils.copyProperties(dto, reading);
        reading.setCreateTime(new Date());
        reading.setUpdateTime(new Date());
        this.save(reading);

        // 返回VO
        MeterReadingVo vo = new MeterReadingVo();
        BeanUtils.copyProperties(reading, vo);
        return vo;
    }

    /**
     * 更新电表读数记录
     * @param dto 包含更新信息的DTO
     * @return 更新后的读数VO对象
     * @throws RuntimeException 当读数记录不存在时抛出
     */
    @Override
    @CacheEvict(value = "meterReadings", allEntries = true)
    public MeterReadingVo update(MeterReadingDto dto) {
        MeterReading reading = meterReadingMapper.selectById(dto.getReadingId());
        if (reading == null) {
            throw new RuntimeException("读数不存在");
        }
        BeanUtils.copyProperties(dto, reading);
        reading.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        meterReadingMapper.updateById(reading);
        MeterReadingVo vo = new MeterReadingVo();
        BeanUtils.copyProperties(reading, vo);
        return vo;
    }

    /**
     * 删除电表读数记录
     * @param readingId 读数记录ID
     * @return 删除是否成功
     */
    @Override
    @CacheEvict(value = "meterReadings", allEntries = true)
    public boolean deleteById(Integer readingId) {
        return meterReadingMapper.deleteById(readingId) > 0;
    }

    /**
     * 根据电表ID和时间范围获取读数记录
     * @param meterId 电表ID
     * @param startTime 开始时间字符串(yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间字符串(yyyy-MM-dd HH:mm:ss)
     * @return 符合条件的读数VO列表
     * @throws BizException 当电表ID无效或时间参数无效时抛出
     */
    @Override
    @Cacheable(value = "meterReadings", key = "#meterId + ':' + #startTime + ':' + #endTime")
    public List<MeterReadingVo> getReadingsByMeterId(Integer meterId, String startTime, String endTime) {
        // 验证 meterId
        if (meterId == null) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不能为空");
        }
        LambdaQueryWrapper<Meter> meterQuery = Wrappers.lambdaQuery(Meter.class)
                .eq(Meter::getMeterId, meterId);
        if (meterMapper.selectCount(meterQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不存在: " + meterId);
        }

        // 验证时间参数
        if (startTime == null || endTime == null) {
            throw new BizException(BizExceptionCode.INVALID_PARAM, "时间范围不能为空");
        }

        // 查询读数
        LambdaQueryWrapper<MeterReading> queryWrapper = Wrappers.lambdaQuery(MeterReading.class)
                .eq(MeterReading::getMeterId, meterId)
                .ge(MeterReading::getReadingTime, startTime)
                .le(MeterReading::getReadingTime, endTime)
                .orderByAsc(MeterReading::getReadingTime);
        List<MeterReading> readings = this.list(queryWrapper);

        // 转换为 VO
        List<MeterReadingVo> voList = readings.stream()
                .map(reading -> {
                    MeterReadingVo vo = new MeterReadingVo();
                    BeanUtils.copyProperties(reading, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return voList;
    }

    /**
     * 生成电表读数报表(PDF格式)
     * @param meterId 电表ID
     * @param startTime 开始时间字符串(yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间字符串(yyyy-MM-dd HH:mm:ss)
     * @return PDF报表的字节数组
     * @throws JRException 当报表生成失败时抛出
     */
    @Override
    public byte[] generateReadingReport(Integer meterId, String startTime, String endTime) throws JRException {
        try {
            // 查询读数数据
            LambdaQueryWrapper<MeterReading> query = Wrappers.lambdaQuery(MeterReading.class)
                    .eq(MeterReading::getMeterId, meterId)
                    .between(MeterReading::getReadingTime, startTime, endTime);
            List<MeterReading> readings = meterReadingMapper.selectList(query);

            // 加载 JRXML 模板文件
            String jrxmlPath = getClass().getClassLoader().getResource("reports/meter_reading_report.jrxml").getPath();
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            // 创建数据源
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(readings);

            // 格式化时间参数
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

            // 转换为 Timestamp
            Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
            Timestamp endTimestamp = Timestamp.valueOf(endDateTime);

            // 设置报表参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("meterId", meterId);
            parameters.put("startTime", startTimestamp);
            parameters.put("endTime", endTimestamp);

            // 填充报表数据
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // 导出为 PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            throw new JRException("生成报表失败: " + e.getMessage(), e);
        }
    }
}