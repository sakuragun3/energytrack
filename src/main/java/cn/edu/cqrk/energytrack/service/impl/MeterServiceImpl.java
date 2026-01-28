package cn.edu.cqrk.energytrack.service.impl;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.DictMeterStatus;
import cn.edu.cqrk.energytrack.entity.pojo.DictMeterType;
import cn.edu.cqrk.energytrack.entity.pojo.Meter;
import cn.edu.cqrk.energytrack.entity.vo.MeterVo;
import cn.edu.cqrk.energytrack.mapper.DictMeterStatusMapper;
import cn.edu.cqrk.energytrack.mapper.DictMeterTypeMapper;
import cn.edu.cqrk.energytrack.mapper.MeterMapper;
import cn.edu.cqrk.energytrack.service.MeterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 电表服务接口实现类
 *
 */
@Service
public class MeterServiceImpl extends ServiceImpl<MeterMapper, Meter> implements MeterService {

    private final DictMeterTypeMapper dictMeterTypeMapper;
    private final DictMeterStatusMapper dictMeterStatusMapper;

    /**
     * 构造方法，注入Mapper依赖
     *
     * @param dictMeterTypeMapper   电表类型Mapper
     * @param dictMeterStatusMapper 电表状态Mapper
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MeterServiceImpl(DictMeterTypeMapper dictMeterTypeMapper, DictMeterStatusMapper dictMeterStatusMapper) {
        this.dictMeterTypeMapper = dictMeterTypeMapper;
        this.dictMeterStatusMapper = dictMeterStatusMapper;
    }

    /**
     * 新增电表
     *
     * @param dto 电表新增数据传输对象
     * @return {@link MeterVo} 电表视图对象
     * @throws BizException 如果电表类型或状态无效
     */
    @Override
    @CacheEvict(value = "meters", allEntries = true) // 清除所有 meters 分页缓存
    public MeterVo add(MeterAddDto dto) {
        // 验证电表类型是否存在
        LambdaQueryWrapper<DictMeterType> typeQuery = Wrappers.lambdaQuery(DictMeterType.class)
                .eq(DictMeterType::getTypeCode, dto.getMeterType());
        if (dictMeterTypeMapper.selectCount(typeQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_TYPE, "无效的电表类型: " + dto.getMeterType());
        }

        // 验证电表状态是否存在
        LambdaQueryWrapper<DictMeterStatus> statusQuery = Wrappers.lambdaQuery(DictMeterStatus.class)
                .eq(DictMeterStatus::getStatusCode, dto.getMeterStatus());
        if (dictMeterStatusMapper.selectCount(statusQuery) == 0) {
            throw new BizException(BizExceptionCode.INVALID_METER_STATUS, "无效的电表状态: " + dto.getMeterStatus());
        }

        // 创建电表
        Meter meter = new Meter();
        BeanUtils.copyProperties(dto, meter);
        meter.setCreateTime(new Date());
        meter.setUpdateTime(new Date());
        this.save(meter);

        // 返回VO
        MeterVo vo = new MeterVo();
        BeanUtils.copyProperties(meter, vo);
        return vo;
    }


    /**
     * 根据ID删除电表
     *
     * @param meterId 电表ID
     * @return {@code true} 如果删除成功，{@code false} 否则
     */
    @Override
    @CacheEvict(value = "meters", allEntries = true) // 清除所有 meters 分页缓存
    public boolean deleteById(Integer meterId) {
        return this.removeById(meterId);
    }

    /**
     * 更新电表信息
     *
     * @param dto 电表更新数据传输对象
     * @return {@link MeterVo} 更新后的电表视图对象
     * @throws BizException 如果电表不存在
     */
    @Override
    @CacheEvict(value = "meters", allEntries = true) // 清除所有 meters 分页缓存
    public MeterVo update(MeterUpdateDto dto) throws BizException {
        Meter meter = this.getById(dto.getMeterId());
        if (meter == null) {
            throw new BizException(BizExceptionCode.METER_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, meter);
        meter.setUpdateTime(new Date());
        this.updateById(meter);

        MeterVo vo = new MeterVo();
        BeanUtils.copyProperties(meter, vo);
        return vo;
    }

    /**
     * 根据条件搜索电表
     *
     * @param dto 电表搜索数据传输对象
     * @return {@link List} &lt;{@link MeterVo}&gt; 匹配的电表视图对象列表
     * @throws BizException 如果电表类型或状态无效，或者未找到匹配的电表记录
     */
    @Override
    @Cacheable(value = "meters_search", key = "#dto.toString()") // 缓存查询结果
    public List<MeterVo> search(MeterSearchDto dto) {
        // 验证 meterType
        if (dto.getMeterType() != null && !dto.getMeterType().isEmpty()) {
            LambdaQueryWrapper<DictMeterType> typeQuery = Wrappers.lambdaQuery(DictMeterType.class)
                    .eq(DictMeterType::getTypeCode, dto.getMeterType());
            if (dictMeterTypeMapper.selectCount(typeQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_METER_TYPE, "无效的电表类型: " + dto.getMeterType());
            }
        }

        // 验证 meterStatus
        if (dto.getMeterStatus() != null && !dto.getMeterStatus().isEmpty()) {
            LambdaQueryWrapper<DictMeterStatus> statusQuery = Wrappers.lambdaQuery(DictMeterStatus.class)
                    .eq(DictMeterStatus::getStatusCode, dto.getMeterStatus());
            if (dictMeterStatusMapper.selectCount(statusQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_METER_STATUS, "无效的电表状态: " + dto.getMeterStatus());
            }
        }

        // 构建查询条件
        LambdaQueryWrapper<Meter> queryWrapper = Wrappers.lambdaQuery(Meter.class);
        queryWrapper.like(dto.getMeterLocation() != null && !dto.getMeterLocation().isEmpty(), Meter::getMeterLocation, dto.getMeterLocation())
                .eq(dto.getMeterType() != null && !dto.getMeterType().isEmpty(), Meter::getMeterType, dto.getMeterType())
                .eq(dto.getMeterStatus() != null && !dto.getMeterStatus().isEmpty(), Meter::getMeterStatus, dto.getMeterStatus());

        List<Meter> meters = this.list(queryWrapper);
        List<MeterVo> voList = meters.stream()
                .map(meter -> {
                    MeterVo vo = new MeterVo();
                    BeanUtils.copyProperties(meter, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 可选：如果结果为空，抛出异常或返回空列表
        if (voList.isEmpty()) {
            // 方案 1：抛出异常
            throw new BizException(BizExceptionCode.NO_DATA_FOUND, "未找到匹配的电表记录");
            // 方案 2：返回空列表（保持当前行为）
            // return voList;
        }

        return voList;
    }

    /**
     * 检查电表ID是否存在
     *
     * @param meterId 电表ID
     * @return {@code true} 如果存在，{@code false} 否则
     */
    @Override
    @Cacheable(value = "meters", key = "#meterId") // 检查是否存在时缓存单条记录
    public boolean checkMeterId(Integer meterId) {
        // 复用 getById 检查电表是否存在
        Meter meter = this.getById(meterId);
        return meter != null; // 如果 meter 不为 null，则存在
    }

}