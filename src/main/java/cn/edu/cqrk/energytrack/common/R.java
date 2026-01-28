package cn.edu.cqrk.energytrack.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {
    private int code;
    private String msg;
    private T data;
    private long timestamp = System.currentTimeMillis();

    // 成功响应（带数据）
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(BizExceptionCode.SUCCESS.getCode());
        r.setMsg(BizExceptionCode.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    // 成功响应（不带数据）
    public static <T> R<T> success() {
        return success(null);
    }

    // 失败响应（异常枚举）
    public static <T> R<T> fail(BizExceptionCode code) {
        R<T> r = new R<>();
        r.setCode(code.getCode());
        r.setMsg(code.getMsg());
        return r;
    }

    // 失败响应（自定义消息）
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    // 失败响应（业务异常）
    public static <T> R<T> fail(BizException e) {
        R<T> r = new R<>();
        r.setCode(e.getCode());
        r.setMsg(e.getMessage());
        return r;
    }
}