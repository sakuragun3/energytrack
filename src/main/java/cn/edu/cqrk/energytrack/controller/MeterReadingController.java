package cn.edu.cqrk.energytrack.controller;

import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.common.R;
import cn.edu.cqrk.energytrack.entity.dto.MeterReadingDto;
import cn.edu.cqrk.energytrack.entity.pojo.MeterReading;
import cn.edu.cqrk.energytrack.entity.vo.MeterReadingVo;
import cn.edu.cqrk.energytrack.service.MeterReadingService;
import cn.edu.cqrk.energytrack.service.MeterService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meter/reading")
public class MeterReadingController {

    @Autowired
    private MeterReadingService meterReadingService;

    /**
     * 添加电表读数
     * @param dto 读数提交数据
     * @return 添加后的读数信息封装为VO对象
     */
    @PostMapping("/add")
    public R<MeterReadingVo> add(@RequestBody @Validated MeterReadingDto dto) {
        MeterReadingVo vo = meterReadingService.add(dto);
        return R.success(vo);
    }

    /**
     * 查询指定电表在某个时间段内的读数列表
     * @param meterId 电表ID
     * @param startTime 起始时间（格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间
     * @return 满足条件的读数VO列表
     */
    @GetMapping("/readings")
    public R<List<MeterReadingVo>> getReadingsByMeterId(
            @RequestParam Integer meterId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<MeterReadingVo> voList = meterReadingService.getReadingsByMeterId(meterId, startTime, endTime);
        return R.success(voList);
    }

    /**
     * 更新电表读数
     * @param dto 读数信息 DTO
     * @return 更新后的VO信息
     */
    @PostMapping("/update")
    public R<MeterReadingVo> update(@RequestBody @Validated MeterReadingDto dto) {
        MeterReadingVo vo = meterReadingService.update(dto);
        return R.success(vo);
    }

    /**
     * 删除某条读数记录
     * @param readingId 读数ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{readingId}")
    public R<Void> delete(@PathVariable Integer readingId) {
        boolean success = meterReadingService.deleteById(readingId);
        return success ? R.success(null) : R.fail(BizExceptionCode.FAILED_DELETE);
    }

    /**
     * 分页查询所有读数记录
     * 可后期启用缓存加速
     * @param page 当前页
     * @param limit 每页记录数
     * @return 分页后的VO对象集合
     */
    @GetMapping("/findAll")
    public R<IPage<MeterReadingVo>> findAllReadings(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int limit) {
        IPage<MeterReading> readingPage = new Page<>(page, limit);
        IPage<MeterReading> result = meterReadingService.page(readingPage);

        // 转换为 VO 类型
        IPage<MeterReadingVo> voPage = result.convert(reading -> {
            MeterReadingVo vo = new MeterReadingVo();
            BeanUtils.copyProperties(reading, vo);
            return vo;
        });

        return R.success(voPage);
    }

    // ========================== SQL 执行相关（仅供管理员或测试用） ===========================

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private CacheManager cacheManager;

    /**
     * 执行SQL语句（仅支持 INSERT/UPDATE/DELETE）
     * 可用于手动维护数据，执行后清除缓存
     * @param request 包含 SQL 语句的 Map
     * @return 执行结果
     */
    @PostMapping("/executeSql")
    public R<String> executeSql(@RequestBody Map<String, String> request) {
        String sql = request.get("sql").trim().toUpperCase();
        try {
            jdbcTemplate.batchUpdate(sql.split("\n"));

            // 清理缓存（暂为全量清理，可优化为按key清理）
            Cache cache = cacheManager.getCache("meterReadings");
            if (cache != null && shouldClearCache(sql)) {
                cache.clear();
            }
            return R.success("执行成功");
        } catch (Exception e) {
            return R.fail(500, "执行失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否为会修改 meter_reading 表的操作
     * 用于决定是否清理缓存
     */
    private boolean shouldClearCache(String sql) {
        return sql.startsWith("INSERT INTO METER_READING") ||
                sql.startsWith("UPDATE METER_READING") ||
                sql.startsWith("DELETE FROM METER_READING");
    }

    /**
     * 获取某电表的最大读数值
     * @param meterId 电表ID
     * @return 最大读数（若无数据默认返回200）
     */
    @GetMapping("/maxValue")
    public R<Double> getMaxReadingValue(@RequestParam("meterId") Integer meterId) {
        String sql = "SELECT MAX(reading_value) FROM meter_reading WHERE meter_id = ?";
        Double maxValue = jdbcTemplate.queryForObject(sql, new Object[]{meterId}, Double.class);
        return R.success(maxValue != null ? maxValue : 200.0);
    }
}
