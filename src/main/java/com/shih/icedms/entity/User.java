package com.shih.icedms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户表
 * @TableName users
 */
@Data
public class User implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String username;

    /**
     * 
     */
    private String actualName;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    @JsonIgnore
    private String passwordSalt;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    @JsonIgnore
    private String phone;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private String dingtalkId;

    /**
     *
     */
    private String avatar;

    /**
     *
     */
    private Integer isAdmin;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        User other = (User) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getActualName() == null ? other.getActualName() == null : this.getActualName().equals(other.getActualName()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getPasswordSalt() == null ? other.getPasswordSalt() == null : this.getPasswordSalt().equals(other.getPasswordSalt()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getDingtalkId() == null ? other.getDingtalkId() == null : this.getDingtalkId().equals(other.getDingtalkId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getActualName() == null) ? 0 : getActualName().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getPasswordSalt() == null) ? 0 : getPasswordSalt().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getDingtalkId() == null) ? 0 : getDingtalkId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", username=").append(username);
        sb.append(", actualName=").append(actualName);
        sb.append(", password=").append(password);
        sb.append(", passwordSalt=").append(passwordSalt);
        sb.append(", email=").append(email);
        sb.append(", phone=").append(phone);
        sb.append(", status=").append(status);
        sb.append(", dingtalkId=").append(dingtalkId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}