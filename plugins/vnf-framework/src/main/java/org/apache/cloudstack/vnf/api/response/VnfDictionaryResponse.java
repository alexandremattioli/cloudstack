package org.apache.cloudstack.vnf.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class VnfDictionaryResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "Dictionary ID")
    private String id;

    @SerializedName("vendor")
    @Param(description = "VNF vendor name")
    private String vendor;

    @SerializedName("version")
    @Param(description = "Vendor API version")
    private String version;

    @SerializedName("uploaded")
    @Param(description = "Upload timestamp")
    private String uploaded;

    @SerializedName("size")
    @Param(description = "Dictionary size in bytes")
    private Long size;

    @SerializedName("operations")
    @Param(description = "Number of operations defined")
    private Integer operations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getOperations() {
        return operations;
    }

    public void setOperations(Integer operations) {
        this.operations = operations;
    }
}
