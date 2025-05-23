package com.example.travel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName; // 存储在服务器上的文件名
    private String fileDownloadUri; // 文件的可访问URL
    private String fileType;
    private long size;
}