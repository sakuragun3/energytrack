package cn.edu.cqrk.energytrack.common;

public enum BizExceptionCode {

    /* 成功码 */
    SUCCESS(200, "成功"),

    /* 权限相关 */
    INVALID_TOKEN(401, "无效的Token"),
    ACCESS_DENIED(402, "无权访问"),
    AUTH_FAILED(403, "认证失败"),

    /* 参数相关 */
    PARAM_VALID_ERROR(4001, "参数校验失败"),
    PARAM_BIND_ERROR(4002, "参数绑定失败"),
    PARAM_TYPE_ERROR(4003, "参数类型错误"),
    BODY_NOT_READABLE(4004, "请求体解析失败"),

    /* 请求相关 */
    METHOD_NOT_ALLOWED(4005, "不支持的请求方法"),

    /* 业务相关 */
    USER_EXIST(5001, "用户已存在"),
    USER_NOT_FOUND(5002, "用户不存在"),
    USER_DISABLED(5003, "用户已被禁用"),
    FAILED_LOGIN(5004, "登录失败"),
    METER_NOT_FOUND(5005,"电表不存在"),
    REPORT_NOT_FOUND(5006,"报表无法获取"),
    FAILED_DELETE(5007,"删除失败"),
    INVALID_STATUS(5008,"无效的状态"),
    INVALID_ROLE(5009,"无效的角色"),
    INVALID_METER_TYPE(5010, "无效的电表类型"),
    INVALID_METER_STATUS(5011, "无效的电表状态"),
    NO_DATA_FOUND(5012, "未找到匹配的记录"),
    INVALID_METER_ID(5013, "无效的电表ID"),
    INVALID_READING_VALUE(5014, "无效的读数值"),
    INVALID_READING_TIME(5015, "无效的读数时间"),
    INVALID_TIME_RANGE(5016, "无效的时间范围"),
    INSUFFICIENT_READINGS(5017, "读数不足"),
    INVALID_REPORT_ID(5018, "无效的报表ID"),
    INVALID_USER_ID(5019, "无效的用户ID"),
    INVALID_PARAM(5020,"时间范围不能为空"),
    OLD_PASSWORD_ERROR(5021,"旧密码错误"),

    /* 系统错误 */
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试");
            ;

    private  Integer code;
    private  String msg;


    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    BizExceptionCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
