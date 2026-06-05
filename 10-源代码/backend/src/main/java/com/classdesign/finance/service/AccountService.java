package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.AccountSaveRequest;
import com.classdesign.finance.entity.AccountInfo;
import com.classdesign.finance.mapper.AccountInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final AccountInfoMapper accountInfoMapper;

    public AccountService(AccountInfoMapper accountInfoMapper) {
        this.accountInfoMapper = accountInfoMapper;
    }

    public List<AccountInfo> list() {
        return accountInfoMapper.selectList(new LambdaQueryWrapper<AccountInfo>().orderByAsc(AccountInfo::getId));
    }

    public AccountInfo create(AccountSaveRequest request) {
        checkCodeUnique(request.accountCode(), null);
        AccountInfo accountInfo = new AccountInfo();
        fillAccount(accountInfo, request);
        accountInfo.setCreatedAt(LocalDateTime.now());
        accountInfoMapper.insert(accountInfo);
        return accountInfo;
    }

    public AccountInfo update(Long id, AccountSaveRequest request) {
        AccountInfo accountInfo = getById(id);
        checkCodeUnique(request.accountCode(), id);
        fillAccount(accountInfo, request);
        accountInfoMapper.updateById(accountInfo);
        return accountInfo;
    }

    public void updateStatus(Long id, Integer status) {
        AccountInfo accountInfo = getById(id);
        accountInfo.setStatus(status);
        accountInfoMapper.updateById(accountInfo);
    }

    public AccountInfo getEnabled(Long id) {
        AccountInfo accountInfo = getById(id);
        if (accountInfo.getStatus() == 0) {
            throw new BusinessException("账户已停用");
        }
        return accountInfo;
    }

    private AccountInfo getById(Long id) {
        AccountInfo accountInfo = accountInfoMapper.selectById(id);
        if (accountInfo == null) {
            throw new BusinessException("账户不存在");
        }
        return accountInfo;
    }

    private void checkCodeUnique(String accountCode, Long excludeId) {
        LambdaQueryWrapper<AccountInfo> wrapper = new LambdaQueryWrapper<AccountInfo>()
                .eq(AccountInfo::getAccountCode, accountCode.trim());
        if (excludeId != null) {
            wrapper.ne(AccountInfo::getId, excludeId);
        }
        if (accountInfoMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("账户编号已存在");
        }
    }

    private void fillAccount(AccountInfo accountInfo, AccountSaveRequest request) {
        accountInfo.setAccountCode(request.accountCode().trim());
        accountInfo.setAccountName(request.accountName().trim());
        accountInfo.setOwnerUnit(request.ownerUnit());
        accountInfo.setDescription(request.description());
        accountInfo.setStatus(request.status());
    }
}
