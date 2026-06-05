package com.classdesign.finance.dto;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull Integer status) {
}
