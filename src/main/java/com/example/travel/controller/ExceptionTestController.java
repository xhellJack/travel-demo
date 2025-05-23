package com.example.travel.controller;

import com.example.travel.exception.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/exceptions")
public class ExceptionTestController {

    @GetMapping("/not-found")
    public void triggerNotFound() {
        throw new ResourceNotFoundException("测试资源不存在异常");
    }

    @GetMapping("/conflict")
    public void triggerConflict() {
        throw new ConflictException("测试冲突异常");
    }

    @PostMapping("/validation")
    public void triggerValidation(@RequestParam String param) {
        if (param.length() < 3) {
            throw new BusinessValidationException("参数长度必须≥3");
        }
    }
}