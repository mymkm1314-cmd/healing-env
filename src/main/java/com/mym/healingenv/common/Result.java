package com.mym.healingenv.common;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<T>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<T>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<T>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
