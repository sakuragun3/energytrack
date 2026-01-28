package cn.edu.cqrk.energytrack.controller;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.R;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.ElectricityReport;
import cn.edu.cqrk.energytrack.entity.vo.ElectricityReportVo;
import cn.edu.cqrk.energytrack.service.ElectricityReportService;
import cn.edu.cqrk.energytrack.service.MeterReadingService;
import cn.edu.cqrk.energytrack.service.MeterService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/electricity-report")
public class ElectricityReportController {

    // 自动注入 ElectricityReportService 用于处理报表业务逻辑
    @Autowired
    private ElectricityReportService electricityReportService;

    // 使用构造函数注入 MeterReadingService，用于生成读表报告
    private final MeterReadingService meterReadingService;
    public ElectricityReportController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /**
     * 添加电量报表
     * @param dto 包含报表数据的请求体
     * @return 添加成功的报表信息
     */
    @PostMapping("/add")
    public R<ElectricityReportVo> add(@RequestBody @Validated ElectricityReportAddDto dto) {
        ElectricityReportVo vo = electricityReportService.add(dto);
        return R.success(vo);
    }

    /**
     * 删除电量报表
     * @param dto 包含报表 ID 的请求体
     * @return 删除成功提示
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestBody @Validated ElectricityReportIdDto dto) {
        electricityReportService.deleteById(dto.getReportId());
        return R.success("删除成功");
    }

    /**
     * 更新电量报表
     * @param dto 报表更新数据
     * @return 更新后的报表信息
     */
    @PostMapping("/update")
    public R<ElectricityReportVo> update(@RequestBody @Validated ElectricityReportUpdateDto dto) throws BizException {
        ElectricityReportVo vo = electricityReportService.update(dto);
        return R.success(vo);
    }

    /**
     * 搜索报表信息（可按条件筛选）
     * @param dto 搜索条件封装类
     * @return 满足条件的报表列表
     */
    @GetMapping("/search")
    public R<List<ElectricityReportVo>> search(@Validated ElectricityReportSearchDto dto) {
        List<ElectricityReportVo> voList = electricityReportService.search(dto);
        return R.success(voList);
    }

    /**
     * 分页获取所有电量报表（带缓存）
     * @param page 页码
     * @param limit 每页数量
     * @return 分页后的报表列表
     */
    @GetMapping("/findAllReports")
    @Cacheable(value = "electricityReports", key = "'page:' + #page + ':limit:' + #limit")
    public R<IPage<ElectricityReportVo>> findAllReports(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int limit) {
        IPage<ElectricityReport> reportPage = new Page<>(page, limit);
        IPage<ElectricityReport> result = electricityReportService.page(reportPage);

        List<ElectricityReportVo> voList = result.getRecords().stream()
                .map(report -> {
                    ElectricityReportVo vo = new ElectricityReportVo();
                    BeanUtils.copyProperties(report, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        IPage<ElectricityReportVo> voPage = new Page<>(page, limit);
        voPage.setRecords(voList);
        voPage.setTotal(result.getTotal());

        return R.success(voPage);
    }

    /**
     * 下载指定时间范围内某个电表的读表 PDF 报告（仅管理员可访问）
     * @param meterId 电表ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return PDF文件流或错误信息
     */
    @GetMapping("/report")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> downloadReadingReport(
            @RequestParam("meterId") Integer meterId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {
        try {
            byte[] reportBytes = meterReadingService.generateReadingReport(meterId, startTime, endTime);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = URLEncoder.encode("meter_reading_report.pdf", StandardCharsets.UTF_8.name());
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (JRException e) {
            // 报告生成失败
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Failed to generate report: " + e.getMessage()));
        } catch (java.io.UnsupportedEncodingException e) {
            // 文件名编码失败
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Failed to encode filename: " + e.getMessage()));
        }
    }

    /**
     * 检测指定电表在时间段内是否存在用电突增
     * @param meterId 电表ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否存在突增（true/false）
     */
    @GetMapping("/surge-detection")
    public R<Boolean> detectConsumptionSurge(@RequestParam("meterId") Integer meterId,
                                             @RequestParam("startTime") String startTime,
                                             @RequestParam("endTime") String endTime) {

        boolean surgeDetected = electricityReportService.detectConsumptionSurge(meterId, startTime, endTime);
        return R.success(surgeDetected);
    }

    // 定义内部类用于统一错误返回结构
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
