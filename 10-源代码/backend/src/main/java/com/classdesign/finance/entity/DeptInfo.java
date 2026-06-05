package com.classdesign.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dept_info")
public class DeptInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deptCode;
    private String deptName;
    private String leaderName;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
}
