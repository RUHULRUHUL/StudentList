package com.ruhul.studentlist.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class RegistrationResponse {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("code")
    @Expose
    private long code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private Registration data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Registration getData() {
        return data;
    }

    public void setData(Registration data) {
        this.data = data;
    }

}
