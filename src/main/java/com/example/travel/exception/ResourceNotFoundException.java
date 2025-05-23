package com.example.travel.exception;

/**
 * 资源未找到异常（HTTP 404）
 * 使用场景：当查询用户、景点等数据不存在时抛出
 */
public class ResourceNotFoundException extends BaseException {

    /**
     * 构造函数
     * @param message 异常信息（会返回给前端）
     * 示例：throw new ResourceNotFoundException("用户ID 123不存在");
     */
    public ResourceNotFoundException(String message) {
        super(404, message); // 调用父类构造，固定code为404
    }

    /**
     * 增强版构造函数（带调试详情）
     * @param message 用户可见的提示信息
     * @param detail 调试详情（记录日志但不返回前端）
     * 示例：throw new ResourceNotFoundException("景点不存在", "attraction_id: 999");
     */
    public ResourceNotFoundException(String message, String detail) {
        super(404, message, detail);
    }
}