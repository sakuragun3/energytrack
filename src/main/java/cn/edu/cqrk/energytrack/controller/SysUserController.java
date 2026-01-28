package cn.edu.cqrk.energytrack.controller;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.common.JwtUtil;
import cn.edu.cqrk.energytrack.common.R;
import cn.edu.cqrk.energytrack.entity.dto.*;
import cn.edu.cqrk.energytrack.entity.pojo.SysUser;
import cn.edu.cqrk.energytrack.entity.vo.SysUserVo;
import cn.edu.cqrk.energytrack.service.SysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户管理控制器
 * 负责用户的注册、登录、增删改查、权限验证等操作
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     * @param dto 注册数据
     * @return 注册成功的用户信息
     * @throws BizException 业务异常
     */
    @PostMapping("/register")
    public R<SysUserVo> register(@Validated @RequestBody SysUserAddDto dto) throws BizException {
        return R.success(sysUserService.addUser(dto));
    }

    /**
     * 管理员添加用户
     * @param dto 用户信息
     * @return 添加成功的用户信息
     * @throws BizException 业务异常
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public R<SysUserVo> add(@Validated @RequestBody SysUserAddDto dto) throws BizException {
        return R.success(sysUserService.addUser(dto));
    }

    /**
     * 用户登录，返回用户信息与 Token
     * @param dto 登录请求参数
     * @return 包含 token 和用户信息的 Map
     * @throws BizException 登录失败异常
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Validated @RequestBody SysUserLoginDto dto) throws BizException {
        SysUserVo vo = sysUserService.login(dto);
        String token = jwtUtil.generateToken(vo);
        Map<String, Object> result = new HashMap<>();
        result.put("user", vo);
        result.put("token", token);
        return R.success(result);
    }

    /**
     * 更新用户信息（管理员权限）
     * @param dto 更新信息
     * @return 更新后的用户信息
     * @throws BizException 业务异常
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public R<SysUserVo> update(@Validated @RequestBody SysUserUpdateDto dto) throws BizException {
        return R.success(sysUserService.updateUser(dto));
    }

    /**
     * 删除用户（管理员权限）
     * @param id 用户ID
     * @return 删除结果
     * @throws BizException 业务异常
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public R<String> delete(@PathVariable Integer id) throws BizException {
        sysUserService.deleteUser(id);
        return R.success("删除成功");
    }

    /**
     * 条件搜索用户（管理员权限）
     * @param dto 搜索条件（用户名等）
     * @param page 当前页码
     * @param limit 每页数量
     * @return 分页后的用户数据
     * @throws BizException 业务异常
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public R<IPage<SysUserVo>> search(@Validated SysUserSearchDto dto,
                                      @RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long limit) throws BizException {
        return R.success(sysUserService.searchUsers(dto, page, limit));
    }

    /**
     * 查询所有用户（分页，管理员权限）
     * @param page 当前页码
     * @param limit 每页数量
     * @return 分页用户信息
     */
    @GetMapping("/findSysUserAll")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public R<IPage<SysUserVo>> findSysUserAll(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long limit) {
        IPage<SysUser> userPage = new Page<>(page, limit);
        IPage<SysUser> result = sysUserService.page(userPage);
        IPage<SysUserVo> voPage = result.convert(user -> {
            SysUserVo vo = new SysUserVo();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
        return R.success(voPage);
    }

    /**
     * 获取当前登录用户信息
     * @return 当前用户的 VO 信息
     */
    @GetMapping("/info")
    public R<SysUserVo> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = (SysUser) authentication.getPrincipal();
        SysUserVo userVo = new SysUserVo();
        BeanUtils.copyProperties(currentUser, userVo);
        return R.success(userVo);
    }

    /**
     * 修改当前登录用户的基本信息
     * @param updatedUser 修改后的用户数据
     * @return 修改结果
     */
    @PostMapping("/updateInfo")
    public R<Void> updateInfo(@RequestBody SysUser updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = (SysUser) authentication.getPrincipal();
        updatedUser.setId(currentUser.getId()); // 确保只能更新自己的信息
        boolean success = sysUserService.updateUserInfo(updatedUser);
        if (success) {
            return R.success();
        } else {
            return R.fail(BizExceptionCode.SYSTEM_ERROR);
        }
    }

    /**
     * 修改当前用户密码
     * @param passwordMap 包含 oldPassword 与 newPassword
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public R<Void> changePassword(@RequestBody Map<String, String> passwordMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = (SysUser) authentication.getPrincipal();
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        try {
            sysUserService.changePassword(currentUser, oldPassword, newPassword);
            return R.success();
        } catch (Exception e) {
            if (e instanceof BizException) {
                return R.fail((BizException) e);
            } else {
                return R.fail(BizExceptionCode.SYSTEM_ERROR);
            }
        }
    }
}
