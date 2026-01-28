package cn.edu.cqrk.energytrack.controller;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.common.R;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.Meter;
import cn.edu.cqrk.energytrack.entity.vo.MeterVo;
import cn.edu.cqrk.energytrack.service.MeterService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meter")
public class MeterController {

    @Autowired
    private MeterService meterService;

    /**
     * 添加电表信息
     * @param dto 电表添加数据传输对象
     * @return 添加后的电表信息封装成的VO
     */
    @PostMapping("/add")
    public R<MeterVo> add(@RequestBody MeterAddDto dto) {
        MeterVo vo = meterService.add(dto);
        return R.success(vo);
    }

    /**
     * 根据电表ID删除电表
     * @param dto 封装了电表ID的DTO
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestBody MeterIdDto dto) {
        boolean isDeleted = meterService.deleteById(dto.getMeterId());
        return isDeleted ? R.success("删除成功") : R.fail(BizExceptionCode.FAILED_DELETE);
    }

    /**
     * 更新电表信息
     * @param dto 包含待更新信息的DTO
     * @return 更新后的电表VO对象
     * @throws BizException 自定义业务异常，可能用于处理数据不合法等场景
     */
    @PostMapping("/update")
    public R<MeterVo> update(@RequestBody MeterUpdateDto dto) throws BizException {
        MeterVo vo = meterService.update(dto);
        return R.success(vo);
    }

    /**
     * 多条件查询电表信息
     * @param dto 查询条件封装DTO
     * @return 满足条件的电表信息列表
     */
    @GetMapping("/search")
    public R<List<MeterVo>> search(MeterSearchDto dto) {
        List<MeterVo> voList = meterService.search(dto);
        return R.success(voList);
    }

    /**
     * 分页查询所有电表
     * 使用缓存提升性能，避免频繁数据库查询
     * @param page 当前页码
     * @param limit 每页大小
     * @return 分页后的电表VO列表
     */
    @GetMapping("/findAll")
    @Cacheable(value = "meters", key = "'page:' + #page + ':limit:' + #limit")
    public R<IPage<MeterVo>> findAllMeters(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long limit) {
        IPage<Meter> meterPage = new Page<>(page, limit);
        IPage<Meter> result = meterService.page(meterPage);

        // 将实体类 Meter 转换为 VO 对象 MeterVo
        IPage<MeterVo> voPage = result.convert(meter -> {
            MeterVo vo = new MeterVo();
            BeanUtils.copyProperties(meter, vo);
            return vo;
        });

        return R.success(voPage);
    }

    /**
     * 校验指定ID的电表是否存在
     * 可用于前端校验或业务逻辑判断
     * @param meterId 电表ID
     * @return 存在返回“电表存在”，否则抛出业务异常码
     */
    @GetMapping("/check/{meterId}")
    public R<String> checkMeterId(@PathVariable("meterId") Integer meterId) {
        boolean exists = meterService.checkMeterId(meterId);
        return exists ? R.success("电表存在") : R.fail(BizExceptionCode.METER_NOT_FOUND);
    }

}
