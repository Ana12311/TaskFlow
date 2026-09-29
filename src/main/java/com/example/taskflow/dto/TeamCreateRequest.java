package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建团队请求体。
 */
public class TeamCreateRequest {

    @NotBlank(message = "团队名不能为空")
    @Size(min = 1, max = 100, message = "团队名最长 100 字符")
    private String name;

    @Size(max = 255, message = "描述最长 255 字符")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
