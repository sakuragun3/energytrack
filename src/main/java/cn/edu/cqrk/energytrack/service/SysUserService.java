package cn.edu.cqrk.energytrack.service;


import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.entity.dto.SysUserAddDto;
import cn.edu.cqrk.energytrack.entity.dto.SysUserLoginDto;
import cn.edu.cqrk.energytrack.entity.dto.SysUserSearchDto;
import cn.edu.cqrk.energytrack.entity.dto.SysUserUpdateDto;
import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.entity.vo.SysUserVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    SysUserVo addUser(SysUserAddDto dto) throws BizException;
    SysUserVo login(SysUserLoginDto dto) throws BizException;
    SysUserVo updateUser(SysUserUpdateDto dto) throws BizException;
    boolean deleteUser(Integer id);
    IPage<SysUserVo> searchUsers(SysUserSearchDto dto, long pageNum, long pageSize) throws BizException;
    boolean updateUserInfo(SysUser user);
    boolean changePassword(SysUser currentUser, String oldPassword, String newPassword);
    SysUser findUserByUsername(String username);

}