package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.UserSaveRequest;
import com.classdesign.finance.entity.SysRole;
import com.classdesign.finance.entity.SysUser;
import com.classdesign.finance.entity.SysUserRole;
import com.classdesign.finance.mapper.SysRoleMapper;
import com.classdesign.finance.mapper.SysUserMapper;
import com.classdesign.finance.mapper.SysUserRoleMapper;
import com.classdesign.finance.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserMapper sysUserMapper,
                       SysUserRoleMapper sysUserRoleMapper,
                       SysRoleMapper sysRoleMapper,
                       PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserVO> list() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId));
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Long> userRoleMap = userRoles.stream()
                .collect(Collectors.toMap(SysUserRole::getUserId, SysUserRole::getRoleId, (left, right) -> left));
        Map<Long, SysRole> roleMap = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
        return users.stream()
                .sorted(Comparator.comparing(SysUser::getId))
                .map(user -> {
                    SysRole role = roleMap.get(userRoleMap.get(user.getId()));
                    return new UserVO(
                            user.getId(),
                            user.getUsername(),
                            user.getRealName(),
                            user.getPhone(),
                            role == null ? "" : role.getRoleCode(),
                            role == null ? "" : role.getRoleName(),
                            user.getStatus()
                    );
                })
                .toList();
    }

    @Transactional
    public UserVO create(UserSaveRequest request) {
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException("新增用户时密码不能为空");
        }
        checkUsernameUnique(request.username(), null);
        SysRole role = getRoleByCode(request.roleCode());

        SysUser user = new SysUser();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password().trim()));
        user.setRealName(request.realName().trim());
        user.setPhone(request.phone());
        user.setStatus(request.status());
        user.setCreatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);

        return new UserVO(user.getId(), user.getUsername(), user.getRealName(), user.getPhone(),
                role.getRoleCode(), role.getRoleName(), user.getStatus());
    }

    @Transactional
    public UserVO update(Long id, UserSaveRequest request) {
        SysUser user = getUser(id);
        checkUsernameUnique(request.username(), id);
        SysRole role = getRoleByCode(request.roleCode());

        user.setUsername(request.username().trim());
        user.setRealName(request.realName().trim());
        user.setPhone(request.phone());
        user.setStatus(request.status());
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }
        sysUserMapper.updateById(user);

        SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id)
                .last("limit 1"));
        if (userRole == null) {
            userRole = new SysUserRole();
            userRole.setUserId(id);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        } else {
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.updateById(userRole);
        }

        return new UserVO(user.getId(), user.getUsername(), user.getRealName(), user.getPhone(),
                role.getRoleCode(), role.getRoleName(), user.getStatus());
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = getUser(id);
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    private SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim());
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    private SysRole getRoleByCode(String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode.trim())
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }
}
