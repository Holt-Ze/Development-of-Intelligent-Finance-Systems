package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.CategorySaveRequest;
import com.classdesign.finance.entity.ExpenseCategory;
import com.classdesign.finance.entity.IncomeCategory;
import com.classdesign.finance.mapper.ExpenseCategoryMapper;
import com.classdesign.finance.mapper.IncomeCategoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final IncomeCategoryMapper incomeCategoryMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;

    public CategoryService(IncomeCategoryMapper incomeCategoryMapper, ExpenseCategoryMapper expenseCategoryMapper) {
        this.incomeCategoryMapper = incomeCategoryMapper;
        this.expenseCategoryMapper = expenseCategoryMapper;
    }

    public List<IncomeCategory> listIncomeCategories() {
        return incomeCategoryMapper.selectList(new LambdaQueryWrapper<IncomeCategory>().orderByAsc(IncomeCategory::getId));
    }

    public IncomeCategory createIncomeCategory(CategorySaveRequest request) {
        checkIncomeCodeUnique(request.categoryCode(), null);
        IncomeCategory category = new IncomeCategory();
        category.setCategoryCode(request.categoryCode().trim());
        category.setCategoryName(request.categoryName().trim());
        category.setStatus(request.status());
        category.setCreatedAt(LocalDateTime.now());
        incomeCategoryMapper.insert(category);
        return category;
    }

    public IncomeCategory updateIncomeCategory(Long id, CategorySaveRequest request) {
        IncomeCategory category = getIncomeCategory(id);
        checkIncomeCodeUnique(request.categoryCode(), id);
        category.setCategoryCode(request.categoryCode().trim());
        category.setCategoryName(request.categoryName().trim());
        category.setStatus(request.status());
        incomeCategoryMapper.updateById(category);
        return category;
    }

    public void updateIncomeCategoryStatus(Long id, Integer status) {
        IncomeCategory category = getIncomeCategory(id);
        category.setStatus(status);
        incomeCategoryMapper.updateById(category);
    }

    public IncomeCategory getEnabledIncomeCategory(Long id) {
        IncomeCategory category = getIncomeCategory(id);
        if (category.getStatus() == 0) {
            throw new BusinessException("收入类别已停用");
        }
        return category;
    }

    public List<ExpenseCategory> listExpenseCategories() {
        return expenseCategoryMapper.selectList(new LambdaQueryWrapper<ExpenseCategory>().orderByAsc(ExpenseCategory::getId));
    }

    public ExpenseCategory createExpenseCategory(CategorySaveRequest request) {
        checkExpenseCodeUnique(request.categoryCode(), null);
        ExpenseCategory category = new ExpenseCategory();
        category.setCategoryCode(request.categoryCode().trim());
        category.setCategoryName(request.categoryName().trim());
        category.setStatus(request.status());
        category.setCreatedAt(LocalDateTime.now());
        expenseCategoryMapper.insert(category);
        return category;
    }

    public ExpenseCategory updateExpenseCategory(Long id, CategorySaveRequest request) {
        ExpenseCategory category = getExpenseCategory(id);
        checkExpenseCodeUnique(request.categoryCode(), id);
        category.setCategoryCode(request.categoryCode().trim());
        category.setCategoryName(request.categoryName().trim());
        category.setStatus(request.status());
        expenseCategoryMapper.updateById(category);
        return category;
    }

    public void updateExpenseCategoryStatus(Long id, Integer status) {
        ExpenseCategory category = getExpenseCategory(id);
        category.setStatus(status);
        expenseCategoryMapper.updateById(category);
    }

    public ExpenseCategory getEnabledExpenseCategory(Long id) {
        ExpenseCategory category = getExpenseCategory(id);
        if (category.getStatus() == 0) {
            throw new BusinessException("支出类别已停用");
        }
        return category;
    }

    private IncomeCategory getIncomeCategory(Long id) {
        IncomeCategory category = incomeCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("收入类别不存在");
        }
        return category;
    }

    private ExpenseCategory getExpenseCategory(Long id) {
        ExpenseCategory category = expenseCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("支出类别不存在");
        }
        return category;
    }

    private void checkIncomeCodeUnique(String categoryCode, Long excludeId) {
        LambdaQueryWrapper<IncomeCategory> wrapper = new LambdaQueryWrapper<IncomeCategory>()
                .eq(IncomeCategory::getCategoryCode, categoryCode.trim());
        if (excludeId != null) {
            wrapper.ne(IncomeCategory::getId, excludeId);
        }
        if (incomeCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("收入类别编号已存在");
        }
    }

    private void checkExpenseCodeUnique(String categoryCode, Long excludeId) {
        LambdaQueryWrapper<ExpenseCategory> wrapper = new LambdaQueryWrapper<ExpenseCategory>()
                .eq(ExpenseCategory::getCategoryCode, categoryCode.trim());
        if (excludeId != null) {
            wrapper.ne(ExpenseCategory::getId, excludeId);
        }
        if (expenseCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("支出类别编号已存在");
        }
    }
}
