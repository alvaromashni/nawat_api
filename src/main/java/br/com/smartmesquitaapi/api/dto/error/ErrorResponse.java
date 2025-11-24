package br.com.smartmesquitaapi.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@lombok.Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private Map<String, String> details;

    public ErrorResponse(String message){
        this.message = message;
        timestamp = LocalDateTime.now();
    }

}
