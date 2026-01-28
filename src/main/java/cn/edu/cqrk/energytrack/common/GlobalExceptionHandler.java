package cn.edu.cqrk.energytrack.common;

import cn.edu.cqrk.energytrack.common.BizException;
import cn.edu.cqrk.energytrack.common.BizExceptionCode;
import cn.edu.cqrk.energytrack.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException; // 导入 ClientAbortException
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Objects;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常处理
    @ExceptionHandler(BizException.class)
    public R<String> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("业务异常 => URI: {} | 错误码: {} | 消息: {}",
                request.getRequestURI(), e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // Spring Security权限异常
    @ExceptionHandler(AccessDeniedException.class)
    public R<String> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限拒绝 => URI: {} | 消息: {}", request.getRequestURI(), e.getMessage());
        return R.fail(BizExceptionCode.ACCESS_DENIED);
    }

    // 参数校验异常（@Validated）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数校验失败 => URI: {} | 消息: {}", request.getRequestURI(), message);
        return R.fail(BizExceptionCode.PARAM_VALID_ERROR.getCode(), message);
    }

    // 参数绑定异常（@RequestParam）
    @ExceptionHandler(BindException.class)
    public R<String> handleBindException(BindException e, HttpServletRequest request) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数绑定失败 => URI: {} | 消息: {}", request.getRequestURI(), message);
        return R.fail(BizExceptionCode.PARAM_BIND_ERROR.getCode(), message);
    }

    // 参数类型不匹配
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型错误 => URI: {} | 参数: {} | 类型应为: {}",
                request.getRequestURI(), e.getName(), Objects.requireNonNull(e.getRequiredType()).getSimpleName());
        return R.fail(BizExceptionCode.PARAM_TYPE_ERROR);
    }

    // 请求方法不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 => URI: {} | 方法: {}", request.getRequestURI(), e.getMethod());
        return R.fail(BizExceptionCode.METHOD_NOT_ALLOWED);
    }

    // 请求体解析失败
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 => URI: {} | 错误: {}", request.getRequestURI(), e.getMessage());
        return R.fail(BizExceptionCode.BODY_NOT_READABLE);
    }

    // 处理客户端中止连接异常
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex, HttpServletRequest request) {
        log.warn("客户端中止连接 => URI: {}", request.getRequestURI());
        // 注意：对于 ClientAbortException，通常不需要返回任何响应，只需记录日志即可
    }

    // 其他未捕获异常
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 => URI: " + request.getRequestURI(), e);
        return R.fail(BizExceptionCode.SYSTEM_ERROR);
    }
}