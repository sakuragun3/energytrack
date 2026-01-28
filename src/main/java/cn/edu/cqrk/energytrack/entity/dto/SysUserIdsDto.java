package cn.edu.cqrk.energytrack.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysUserIdsDto implements Serializable {
    private List<Integer> userIds;

}
