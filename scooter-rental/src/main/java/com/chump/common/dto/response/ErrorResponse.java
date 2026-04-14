package com.chump.common.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class ErrorResponse {

    private int status;
    private String error; // Короткое имя ошибки
    private String message; // Подробное сообщение

    @Builder.Default
    private String traceId = "not implemented"; // TODO ID операции для отслеживания операции
    private List<String> details; // Подробности (например для валидации)
}
