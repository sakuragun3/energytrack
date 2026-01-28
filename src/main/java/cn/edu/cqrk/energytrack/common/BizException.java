package cn.edu.cqrk.energytrack.common;

        import lombok.Data;
        import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)

public class BizException extends RuntimeException {
    private final int code;
    private final String message;

    public BizException(BizExceptionCode code) {
        this.code = code.getCode();
        this.message = code.getMsg();
    }

    public BizException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // 支持动态消息
    public BizException(BizExceptionCode code, String dynamicMessage) {
        this.code = code.getCode();
        this.message = dynamicMessage;
    }
}