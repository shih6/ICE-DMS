package com.shih.icedms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName file_history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileHistory implements Serializable {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String matterId;

    /**
     * 
     */
    private String docKey;

    /**
     * 
     */
    private Date created;

    /**
     * 
     */
    private String userId;

    /**
     * 
     */
    private Integer version;

    /**
     * 
     */
    private String serverVersion;

    /**
     * 
     */
    private String changesObjectName;
    /**
     *
     */
    private String objectName;

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
        FileHistory other = (FileHistory) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMatterId() == null ? other.getMatterId() == null : this.getMatterId().equals(other.getMatterId()))
            && (this.getDocKey() == null ? other.getDocKey() == null : this.getDocKey().equals(other.getDocKey()))
            && (this.getCreated() == null ? other.getCreated() == null : this.getCreated().equals(other.getCreated()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getServerVersion() == null ? other.getServerVersion() == null : this.getServerVersion().equals(other.getServerVersion()))
            && (this.getChangesObjectName() == null ? other.getChangesObjectName() == null : this.getChangesObjectName().equals(other.getChangesObjectName()))
            && (this.getObjectName() == null ? other.getObjectName() == null : this.getObjectName().equals(other.getObjectName()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMatterId() == null) ? 0 : getMatterId().hashCode());
        result = prime * result + ((getDocKey() == null) ? 0 : getDocKey().hashCode());
        result = prime * result + ((getCreated() == null) ? 0 : getCreated().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getServerVersion() == null) ? 0 : getServerVersion().hashCode());
        result = prime * result + ((getChangesObjectName() == null) ? 0 : getChangesObjectName().hashCode());
        result = prime * result + ((getObjectName() == null) ? 0 : getObjectName().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileId=").append(matterId);
        sb.append(", docKey=").append(docKey);
        sb.append(", created=").append(created);
        sb.append(", userId=").append(userId);
        sb.append(", version=").append(version);
        sb.append(", serverVersion=").append(serverVersion);
        sb.append(", changesObjectName=").append(changesObjectName);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append(", getObjectName=").append(objectName);
        sb.append("]");
        return sb.toString();
    }
}