package com.shih.icecms.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName matter_permissions
 */
@Data
public class MatterPermissions implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String matterId;

    /**
     * 
     */
    private String roleId;

    /**
     * 
     */
    private Integer roleType;

    /**
     * 
     */
    private Integer action;

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
        MatterPermissions other = (MatterPermissions) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMatterId() == null ? other.getMatterId() == null : this.getMatterId().equals(other.getMatterId()))
            && (this.getRoleId() == null ? other.getRoleId() == null : this.getRoleId().equals(other.getRoleId()))
            && (this.getRoleType() == null ? other.getRoleType() == null : this.getRoleType().equals(other.getRoleType()))
            && (this.getAction() == null ? other.getAction() == null : this.getAction().equals(other.getAction()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMatterId() == null) ? 0 : getMatterId().hashCode());
        result = prime * result + ((getRoleId() == null) ? 0 : getRoleId().hashCode());
        result = prime * result + ((getRoleType() == null) ? 0 : getRoleType().hashCode());
        result = prime * result + ((getAction() == null) ? 0 : getAction().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", matterId=").append(matterId);
        sb.append(", roleId=").append(roleId);
        sb.append(", roleType=").append(roleType);
        sb.append(", action=").append(action);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}