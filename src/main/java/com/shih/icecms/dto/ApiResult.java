package com.shih.icecms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.NoArgsConstructor;

@ApiModel()
@NoArgsConstructor
public class ApiResult<T> {
    @ApiModelProperty("返回码")
    private int code;
    @ApiModelProperty("信息")
    private String msg;
    @ApiModelProperty("数据")
    private T data;
    public static ApiResult<?> SUCCESS(){
        return new ApiResult<>(200,"success",null);
    }
    public static <T> ApiResult<T> SUCCESS(T data){
        return new ApiResult<>(200, "success", data);
    }
    public ApiResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static ApiResult<?>  ERROR(String message){
        return new ApiResult<>(400, message, null);
    }
    public static <T> ApiResult<T> ERROR(String message,T data){
        return new ApiResult<>(200, message, data);
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
