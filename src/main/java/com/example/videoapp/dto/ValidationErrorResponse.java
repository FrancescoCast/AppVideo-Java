package com.example.videoapp.dto;

import java.util.List;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, List<String>> validationErrors;

    public ValidationErrorResponse() {
        super();
    }

    public ValidationErrorResponse(int status, String error, String message, String path, Map<String, List<String>> validationErrors) {
        super(status, error, message, path);
        this.validationErrors = validationErrors;
    }

    public Map<String, List<String>> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, List<String>> validationErrors) { this.validationErrors = validationErrors; }
}