package cn.edu.cqrk.energytrack.service.impl;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.DictUserRole;
import cn.edu.cqrk.energytrack.entity.pojo.DictUserStatus;
import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.entity.vo.SysUserVo;
import cn.edu.cqrk.energytrack.mapper.DictUserRoleMapper;
import cn.edu.cqrk.energytrack.mapper.DictUserStatusMapper;
import cn.edu.cqrk.energytrack.mapper.SysUserMapper;
import cn.edu.cqrk.energytrack.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务接口实现类
 *
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final DictUserRoleMapper dictUserRoleMapper;
    private final DictUserStatusMapper dictUserStatusMapper;
    private static final Logger logger = LoggerFactory.getLogger(SysUserServiceImpl.class);


    /**
     * 构造方法，注入所需的依赖
     *
     * @param passwordEncoder      密码编码器
     * @param dictUserRoleMapper   用户角色字典Mapper
     * @param dictUserStatusMapper 用户状态字典Mapper
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SysUserServiceImpl(PasswordEncoder passwordEncoder,
                              DictUserRoleMapper dictUserRoleMapper,
                              DictUserStatusMapper dictUserStatusMapper) {
        this.passwordEncoder = passwordEncoder;
        this.dictUserRoleMapper = dictUserRoleMapper;
        this.dictUserStatusMapper = dictUserStatusMapper;
    }

    /**
     * 添加新用户
     *
     * @param dto 用户添加数据传输对象
     * @return {@link SysUserVo} 新增用户的视图对象
     * @throws BizException 如果用户名已存在，角色或状态无效
     */
    @Override
    public SysUserVo addUser(SysUserAddDto dto) throws BizException {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> query = Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, dto.getUsername());
        if (count(query) > 0) {
            throw new BizException(BizExceptionCode.USER_EXIST, "用户名已存在: " + dto.getUsername());
        }

        // 验证角色是否存在
        if (dto.getRole() != null) {
            LambdaQueryWrapper<DictUserRole> roleQuery = Wrappers.lambdaQuery(DictUserRole.class)
                    .eq(DictUserRole::getRoleCode, dto.getRole());
            if (dictUserRoleMapper.selectCount(roleQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_ROLE, "无效的角色: " + dto.getRole());
            }
        }

        // 验证状态是否存在
        if (dto.getStatus() != null) {
            LambdaQueryWrapper<DictUserStatus> statusQuery = Wrappers.lambdaQuery(DictUserStatus.class)
                    .eq(DictUserStatus::getStatusCode, dto.getStatus());
            if (dictUserStatusMapper.selectCount(statusQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_STATUS, "无效的状态: " + dto.getStatus());
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        save(user);

        // 返回VO
        SysUserVo vo = new SysUserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 用户登录
     *
     * @param dto 用户登录数据传输对象
     * @return {@link SysUserVo} 登录成功的用户视图对象
     * @throws BizException 如果用户名或密码错误，或者用户被禁用
     */
    @Override
    public SysUserVo login(SysUserLoginDto dto) throws BizException {
        SysUser user = getOne(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, dto.getUsername()));

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(BizExceptionCode.FAILED_LOGIN, "用户名或密码错误");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new BizException(BizExceptionCode.USER_DISABLED, "用户已被禁用");
        }

        SysUserVo vo = new SysUserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 更新用户信息
     *
     * @param dto 用户更新数据传输对象
     * @return {@link SysUserVo} 更新后的用户视图对象
     * @throws BizException 如果用户ID为空，用户不存在，用户名已存在（排除当前用户），角色或状态无效
     */
    @Override
    public SysUserVo updateUser(SysUserUpdateDto dto) throws BizException {
        // 验证用户是否存在
        if (dto.getId() == null) {
            throw new BizException(BizExceptionCode.INVALID_USER_ID, "用户ID不能为空");
        }
        SysUser user = getById(dto.getId());
        if (user == null) {
            throw new BizException(BizExceptionCode.USER_NOT_FOUND, "用户不存在: " + dto.getId());
        }

        // 检查用户名是否重复（排除当前用户）
        LambdaQueryWrapper<SysUser> usernameQuery = Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, dto.getId());
        if (count(usernameQuery) > 0) {
            throw new BizException(BizExceptionCode.USER_EXIST, "用户名已存在: " + dto.getUsername());
        }

        // 验证角色是否存在
        if (dto.getRole() != null) {
            LambdaQueryWrapper<DictUserRole> roleQuery = Wrappers.lambdaQuery(DictUserRole.class)
                    .eq(DictUserRole::getRoleCode, dto.getRole());
            if (dictUserRoleMapper.selectCount(roleQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_ROLE, "无效的角色: " + dto.getRole());
            }
        }

        // 验证状态是否存在
        if (dto.getStatus() != null) {
            LambdaQueryWrapper<DictUserStatus> statusQuery = Wrappers.lambdaQuery(DictUserStatus.class)
                    .eq(DictUserStatus::getStatusCode, dto.getStatus());
            if (dictUserStatusMapper.selectCount(statusQuery) == 0) {
                throw new BizException(BizExceptionCode.INVALID_STATUS, "无效的状态: " + dto.getStatus());
            }
        }

        // 更新用户
        BeanUtils.copyProperties(dto, user, "password");
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUpdateTime(new Date());
        updateById(user);

        SysUserVo vo = new SysUserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return {@code true} 如果删除成功，{@code false} 否则
     * @throws BizException 如果用户ID为空或用户不存在
     */
    @Override
    public boolean deleteUser(Integer id) throws BizException {
        if (id == null) {
            throw new BizException(BizExceptionCode.INVALID_USER_ID, "用户ID不能为空");
        }
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(BizExceptionCode.USER_NOT_FOUND, "用户不存在: " + id);
        }
        return removeById(id);
    }

    /**
     * 分页搜索用户
     *
     * @param dto      用户搜索数据传输对象
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return {@link IPage} &lt;{@link SysUserVo}&gt; 包含用户视图对象的分页结果
     * @throws BizException 如果未找到匹配的用户
     */
    @Override
    public IPage<SysUserVo> searchUsers(SysUserSearchDto dto, long pageNum, long pageSize) throws BizException {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> query = Wrappers.lambdaQuery(SysUser.class);
        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            query.like(SysUser::getUsername, dto.getUsername());
        }
        IPage<SysUser> userPage = baseMapper.selectPage(page, query);
        if (userPage.getRecords().isEmpty()) {
            throw new BizException(BizExceptionCode.NO_DATA_FOUND, "未找到匹配的用户");
        }
        return userPage.convert(user -> {
            SysUserVo vo = new SysUserVo();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
    }

    /**
     * 注入SysUserMapper
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 更新用户信息
     *
     * @param user 需要更新的系统用户对象
     * @return {@code true} 如果更新成功，{@code false} 否则
     * @throws BizException 如果用户信息更新失败
     */
    @Override
    public boolean updateUserInfo(SysUser user) {
        int result = sysUserMapper.updateById(user);
        if (result <= 0) {
            throw new BizException(BizExceptionCode.SYSTEM_ERROR, "用户信息更新失败");
        }
        return true;
    }

    /**
     * 修改用户密码
     *
     * @param currentUser 当前登录用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return {@code true} 如果密码修改成功，{@code false} 否则
     * @throws BizException 如果旧密码错误或新密码修改失败
     */
    @Override
    public boolean changePassword(SysUser currentUser, String oldPassword, String newPassword) {
        logger.debug("尝试修改用户 {} 的密码", currentUser.getUsername());
        logger.debug("提供的旧密码：{}", oldPassword);
        logger.debug("数据库中的密码：{}", currentUser.getPassword());

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            logger.warn("用户 {} 提供的旧密码错误", currentUser.getUsername());
            throw new BizException(BizExceptionCode.OLD_PASSWORD_ERROR);
        }
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        SysUser userToUpdate = new SysUser();
        userToUpdate.setId(currentUser.getId());
        userToUpdate.setPassword(encodedNewPassword);
        int result = sysUserMapper.updateById(userToUpdate);
        if (result <= 0) {
            throw new BizException(BizExceptionCode.SYSTEM_ERROR, "新密码修改失败");
        }
        return true; // 返回 true 表示密码修改成功
    }

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return {@link SysUser} 查找到的系统用户对象，如果不存在则返回null
     */
    @Override
    public SysUser findUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }
}