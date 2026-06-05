package com.classdesign.finance.vo;

import java.util.List;

public record LoginResponse(String token, String username, String realName, List<String> roles) {
}
