package cn.edu.cqrk.energytrack.service.impl;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.ElectricityReport;
import cn.edu.cqrk.energytrack.entity.pojo.Meter;
import cn.edu.cqrk.energytrack.entity.pojo.MeterReading;
import cn.edu.cqrk.energytrack.entity.vo.ElectricityReportVo;
import cn.edu.cqrk.energytrack.entity.vo.MeterReadingVo;
import cn.edu.cqrk.energytrack.mapper.ElectricityReportMapper;
import cn.edu.cqrk.energytrack.mapper.MeterMapper;
import cn.edu.cqrk.energytrack.mapper.MeterReadingMapper;
import cn.edu.cqrk.energytrack.service.ElectricityReportService;
import cn.edu.cqrk.energytrack.service.MeterReadingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * 电表报表服务实现类
 * 实现电表报表相关的业务逻辑，包括报表的增删改查、用电量计算和突增检测等功能
 */
@Service
public class ElectricityReportServiceImpl extends ServiceImpl<ElectricityReportMapper, ElectricityReport> implements ElectricityReportService {

    private final MeterMapper meterMapper;
    private final MeterReadingMapper meterReadingMapper;

    @Autowired
    private MeterReadingService meterReadingService;

    /**
     * 构造函数，注入必要的Mapper
     * @param meterMapper 电表Mapper
     * @param meterReadingMapper 电表读数Mapper
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ElectricityReportServiceImpl(MeterMapper meterMapper, MeterReadingMapper meterReadingMapper) {
        this.meterMapper = meterMapper;
        this.meterReadingMapper = meterReadingMapper;
    }

    /**
     * 计算指定电表在指定时间范围内的总用电量
     * @param meterId 电表ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总用电量
     * @throws BizException 当读数不足或读数值无效时抛出异常
     */
    private BigDecimal calculateTotalConsumption(Integer meterId, Date startTime, Date endTime) {
        // 查询指定时间范围内的读数
        LambdaQueryWrapper<MeterReading> readingQuery = Wrappers.lambdaQuery(MeterReading.class)
                .eq(MeterReading::getMeterId, meterId)
                .ge(MeterReading::getReadingTime, startTime)
                .le(MeterReading::getReadingTime, endTime)
                .orderByAsc(MeterReading::getReadingTime);
        List<MeterReading> readings = meterReadingMapper.selectList(readingQuery);

        // 检查是否有足够的读数
        if (readings.size() < 2) {
            throw new BizException(BizExceptionCode.INSUFFICIENT_READINGS, "时间范围内读数不足，无法计算用电量");
        }

        // 计算总用电量（最后一次读数 - 第一次读数）
        BigDecimal firstReading = readings.get(0).getReadingValue();
        BigDecimal lastReading = readings.get(readings.size() - 1).getReadingValue();
        if (firstReading == null || lastReading == null) {
            throw new BizException(BizExceptionCode.INVALID_READING_VALUE, "读数值缺失，无法计算用电量");
        }

        return lastReading.subtract(firstReading);
    }

    /**
     * 添加电表报表
     * @param dto 包含报表信息的DTO
     * @return 新创建的报表VO对象
     * @throws BizException 当电表ID无效或时间范围无效时抛出异常
     */
    @Override
    @CacheEvict(value = "electricityReports", allEntries = true)
    public ElectricityReportVo add(ElectricityReportAddDto dto) {
        // 验证 meterId
        if (dto.getMeterId() == null) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不能为空");
        }
        LambdaQueryWrapper<Meter> meterQuery = Wrappers.lambdaQuery(Meter.class)
                .eq(Meter::getMeterId, dto.getMeterId());
        if (meterMapper.selectCount(meterQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不存在: " + dto.getMeterId());
        }

        // 验证时间范围
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BizException(BizExceptionCode.INVALID_TIME_RANGE, "开始时间和结束时间不能为空");
        }
        if (dto.getStartTime().after(dto.getEndTime())) {
            throw new BizException(BizExceptionCode.INVALID_TIME_RANGE, "开始时间不能晚于结束时间");
        }

        // 计算总用电量
        BigDecimal totalConsumption = calculateTotalConsumption(dto.getMeterId(), dto.getStartTime(), dto.getEndTime());

        // 创建报表
        ElectricityReport report = new ElectricityReport();
        BeanUtils.copyProperties(dto, report);
        report.setTotalConsumption(totalConsumption);
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        this.save(report);

        ElectricityReportVo vo = new ElectricityReportVo();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    /**
     * 删除电表报表
     * @param reportId 报表ID
     * @return 删除是否成功
     * @throws BizException 当报表ID无效或报表不存在时抛出异常
     */
    @Override
    @CacheEvict(value = "electricityReports", allEntries = true)
    public boolean deleteById(Integer reportId) {
        if (reportId == null) {
            throw new BizException(BizExceptionCode.INVALID_REPORT_ID, "报表ID不能为空");
        }
        ElectricityReport report = this.getById(reportId);
        if (report == null) {
            throw new BizException(BizExceptionCode.REPORT_NOT_FOUND, "报表不存在: " + reportId);
        }
        return this.removeById(reportId);
    }

    /**
     * 更新电表报表
     * @param dto 包含更新信息的DTO
     * @return 更新后的报表VO对象
     * @throws BizException 当报表ID无效、电表ID无效或时间范围无效时抛出异常
     */
    @Override
    @CacheEvict(value = "electricityReports", allEntries = true)
    public ElectricityReportVo update(ElectricityReportUpdateDto dto) throws BizException {
        // 验证 reportId
        if (dto.getReportId() == null) {
            throw new BizException(BizExceptionCode.INVALID_REPORT_ID, "报表ID不能为空");
        }
        ElectricityReport report = this.getById(dto.getReportId());
        if (report == null) {
            throw new BizException(BizExceptionCode.REPORT_NOT_FOUND, "报表不存在: " + dto.getReportId());
        }

        // 验证 meterId
        if (dto.getMeterId() == null) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不能为空");
        }
        LambdaQueryWrapper<Meter> meterQuery = Wrappers.lambdaQuery(Meter.class)
                .eq(Meter::getMeterId, dto.getMeterId());
        if (meterMapper.selectCount(meterQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不存在: " + dto.getMeterId());
        }

        // 验证时间范围
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BizException(BizExceptionCode.INVALID_TIME_RANGE, "开始时间和结束时间不能为空");
        }
        if (dto.getStartTime().after(dto.getEndTime())) {
            throw new BizException(BizExceptionCode.INVALID_TIME_RANGE, "开始时间不能晚于结束时间");
        }

        // 计算总用电量
        BigDecimal totalConsumption = calculateTotalConsumption(dto.getMeterId(), dto.getStartTime(), dto.getEndTime());

        // 更新报表
        BeanUtils.copyProperties(dto, report);
        report.setTotalConsumption(totalConsumption);
        report.setUpdateTime(new Date());
        this.updateById(report);

        ElectricityReportVo vo = new ElectricityReportVo();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    /**
     * 搜索电表报表
     * @param dto 包含搜索条件的DTO
     * @return 符合条件的报表VO列表
     * @throws BizException 当电表ID无效、时间范围无效或未找到数据时抛出异常
     */
    @Override
    @Cacheable(value = "electricity_reports_search", key = "#dto.toString()")
    public List<ElectricityReportVo> search(ElectricityReportSearchDto dto) {
        // 验证 meterId
        if (dto.getMeterId() != null) {
            LambdaQueryWrapper<Meter> meterQuery = Wrappers.lambdaQuery(Meter.class)
                    .eq(Meter::getMeterId, dto.getMeterId());
            if (meterMapper.selectCount(meterQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_METER_ID, "电表ID不存在: " + dto.getMeterId());
            }
        }

        // 验证时间范围
        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getStartTime().after(dto.getEndTime())) {
            throw new BizException(BizExceptionCode.INVALID_TIME_RANGE, "开始时间不能晚于结束时间");
        }

        // 查询
        LambdaQueryWrapper<ElectricityReport> queryWrapper = Wrappers.lambdaQuery(ElectricityReport.class);
        queryWrapper.eq(dto.getMeterId() != null, ElectricityReport::getMeterId, dto.getMeterId())
                .ge(dto.getStartTime() != null, ElectricityReport::getStartTime, dto.getStartTime())
                .le(dto.getEndTime() != null, ElectricityReport::getEndTime, dto.getEndTime());

        List<ElectricityReport> reports = this.list(queryWrapper);
        List<ElectricityReportVo> voList = reports.stream()
                .map(report -> {
                    ElectricityReportVo vo = new ElectricityReportVo();
                    BeanUtils.copyProperties(report, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        if (voList.isEmpty()) {
            throw new BizException(BizExceptionCode.NO_DATA_FOUND, "未找到匹配的用电报表记录");
        }

        return voList;
    }

    /**
     * 检测用电量突增
     * @param meterId 电表ID
     * @param startTime 开始时间字符串
     * @param endTime 结束时间字符串
     * @return 是否检测到用电量突增
     */
    @Override
    public boolean detectConsumptionSurge(Integer meterId, String startTime, String endTime) {
        // 获取指定电表在指定时间范围内的抄表数据
        List<MeterReadingVo> readingVos = meterReadingService.getReadingsByMeterId(meterId, startTime, endTime);

        // 如果抄表数据为空或少于两条，则无法进行比较，直接返回 false
        if (readingVos == null || readingVos.size() < 2) {
            return false;
        }

        // 将 MeterReadingVo 转换为包含 LocalDateTime 时间戳的临时对象以便排序
        List<TempReading> readings = readingVos.stream().map(vo -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime readingTime = LocalDateTime.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vo.getReadingTime()), formatter);
            return new TempReading(readingTime, vo.getReadingValue().doubleValue());
        }).collect(Collectors.toList());

        // 按照时间戳对抄表数据进行排序
        readings.sort(Comparator.comparing(TempReading::getTimestamp));

        // 定义短时间窗口，这里设置为 1 小时
        long shortTimeWindowHours = 1;

        // 遍历排序后的抄表数据，从第二条数据开始与之前的记录进行比较
        for (int i = 1; i < readings.size(); i++) {
            TempReading currentReading = readings.get(i);
            LocalDateTime currentTime = currentReading.getTimestamp();
            double currentConsumption = currentReading.getConsumption();

            // 遍历当前记录之前的记录，查找在短时间窗口内的前一条记录
            for (int j = i - 1; j >= 0; j--) {
                TempReading previousReading = readings.get(j);
                LocalDateTime previousTime = previousReading.getTimestamp();
                double previousConsumption = previousReading.getConsumption();

                // 判断前一条记录是否在短时间窗口内
                if (currentTime.minusHours(shortTimeWindowHours).isBefore(previousTime)) {
                    // 判断当前用电量是否超过前一条记录的 3 倍 (即增长超过 200%)，并确保前一条记录的用电量不为负数
                    if (currentConsumption > 3 * previousConsumption && previousConsumption >= 0) {
                        return true; // 检测到用电量突增，返回 true
                    }
                } else {
                    // 如果前一条记录的时间已经超出了短时间窗口，则更早的记录也不需要再比较了，直接跳出内层循环
                    break;
                }
            }
        }

        // 遍历结束后未检测到用电量突增，返回 false
        return false;
    }

    /**
     * 临时内部类，用于存储和处理抄表数据
     * 方便进行时间排序和用电量比较
     */
    private static class TempReading {
        private LocalDateTime timestamp;
        private double consumption;

        public TempReading(LocalDateTime timestamp, double consumption) {
            this.timestamp = timestamp;
            this.consumption = consumption;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public double getConsumption() {
            return consumption;
        }
    }
}