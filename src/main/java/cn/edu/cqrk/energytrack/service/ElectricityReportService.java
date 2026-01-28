package cn.edu.cqrk.energytrack.service;

        import cn.edu.cqrk.energytrack.common.BizException;
        import cn.edu.cqrk.energytrack.entity.dto.*;
        import cn.edu.cqrk.energytrack.entity.pojo.ElectricityReport;
        import cn.edu.cqrk.energytrack.entity.vo.ElectricityReportVo;
        import com.baomidou.mybatisplus.extension.service.IService;

        import java.util.List;

public interface ElectricityReportService extends IService<ElectricityReport> {
    ElectricityReportVo add(ElectricityReportAddDto dto);
    boolean deleteById(Integer reportId);
    ElectricityReportVo update(ElectricityReportUpdateDto dto) throws BizException;
    List<ElectricityReportVo> search(ElectricityReportSearchDto dto);
    boolean detectConsumptionSurge(Integer meterId, String startTime, String endTime);

}