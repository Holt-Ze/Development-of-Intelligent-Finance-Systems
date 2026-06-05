package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.DepartmentSaveRequest;
import com.classdesign.finance.entity.DeptInfo;
import com.classdesign.finance.mapper.DeptInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {

    private final DeptInfoMapper deptInfoMapper;

    public DepartmentService(DeptInfoMapper deptInfoMapper) {
        this.deptInfoMapper = deptInfoMapper;
    }

    public List<DeptInfo> list() {
        return deptInfoMapper.selectList(new LambdaQueryWrapper<DeptInfo>().orderByAsc(DeptInfo::getId));
    }

    public DeptInfo create(DepartmentSaveRequest request) {
        checkCodeUnique(request.deptCode(), null);
        DeptInfo deptInfo = new DeptInfo();
        fillDepartment(deptInfo, request);
        deptInfo.setCreatedAt(LocalDateTime.now());
        deptInfoMapper.insert(deptInfo);
        return deptInfo;
    }

    public DeptInfo update(Long id, DepartmentSaveRequest request) {
        DeptInfo deptInfo = getById(id);
        checkCodeUnique(request.deptCode(), id);
        fillDepartment(deptInfo, request);
        deptInfoMapper.updateById(deptInfo);
        return deptInfo;
    }

    public void updateStatus(Long id, Integer status) {
        DeptInfo deptInfo = getById(id);
        deptInfo.setStatus(status);
        deptInfoMapper.updateById(deptInfo);
    }

    public DeptInfo getEnabled(Long id) {
        DeptInfo deptInfo = getById(id);
        if (deptInfo.getStatus() == 0) {
            throw new BusinessException("部门已停用");
        }
        return deptInfo;
    }

    private DeptInfo getById(Long id) {
        DeptInfo deptInfo = deptInfoMapper.selectById(id);
        if (deptInfo == null) {
            throw new BusinessException("部门不存在");
        }
        return deptInfo;
    }

    private void checkCodeUnique(String deptCode, Long excludeId) {
        LambdaQueryWrapper<DeptInfo> wrapper = new LambdaQueryWrapper<DeptInfo>()
                .eq(DeptInfo::getDeptCode, deptCode.trim());
        if (excludeId != null) {
            wrapper.ne(DeptInfo::getId, excludeId);
        }
        if (deptInfoMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("部门编号已存在");
        }
    }

    private void fillDepartment(DeptInfo deptInfo, DepartmentSaveRequest request) {
        deptInfo.setDeptCode(request.deptCode().trim());
        deptInfo.setDeptName(request.deptName().trim());
        deptInfo.setLeaderName(request.leaderName());
        deptInfo.setPhone(request.phone());
        deptInfo.setStatus(request.status());
    }
}
