package com.serverManagement.server.management.dao.itemDetails;

import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

public class ItemDetailsDTO {

    private String serial_No;
    private String rack_No;
    private ItemStatusOptionEntity itemStatusId;
    private String system;
    private String system_Version;
    private String moduleFor;
    private String itemDescription;
    private String partyName;
    private String update_Date;
    private RegionEntity region;
    private String remark;

    public ItemDetailsDTO(String serial_No, String rack_No, ItemStatusOptionEntity itemStatusId, String system,
                          String system_Version, String moduleFor, String itemDescription, String partyName, String update_Date,
                          RegionEntity region, String remark) {
        super();
        this.serial_No = serial_No;
        this.rack_No = rack_No;
        this.itemStatusId = itemStatusId;
        this.system = system;
        this.system_Version = system_Version;
        this.moduleFor = moduleFor;
        this.itemDescription = itemDescription;
        this.partyName = partyName;
        this.update_Date = update_Date;
        this.region = region;
        this.remark = remark;
    }

    public ItemDetailsDTO() {
        super();
    }

    public String getSerial_No() {
        return serial_No;
    }

    public void setSerial_No(String serial_No) {
        this.serial_No = serial_No;
    }

    public String getRack_No() {
        return rack_No;
    }

    public void setRack_No(String rack_No) {
        this.rack_No = rack_No;
    }

    public ItemStatusOptionEntity getItemStatusId() {
        return itemStatusId;
    }

    public void setItemStatusId(ItemStatusOptionEntity itemStatusId) {
        this.itemStatusId = itemStatusId;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getSystem_Version() {
        return system_Version;
    }

    public void setSystem_Version(String system_Version) {
        this.system_Version = system_Version;
    }

    public String getModuleFor() {
        return moduleFor;
    }

    public void setModuleFor(String moduleFor) {
        this.moduleFor = moduleFor;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getUpdate_Date() {
        return update_Date;
    }

    public void setUpdate_Date(String update_Date) {
        this.update_Date = update_Date;
    }

    public RegionEntity getRegion() {
        return region;
    }

    public void setRegion(RegionEntity region) {
        this.region = region;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ItemDetailsDTO [serial_No=" + serial_No + ", rack_No=" + rack_No + ", itemStatusId=" + itemStatusId
                + ", system=" + system + ", system_Version=" + system_Version + ", moduleFor=" + moduleFor
                + ", itemDescription=" + itemDescription + ", partyName=" + partyName + ", update_Date=" + update_Date
                + ", region=" + region + ", remark=" + remark + "]";
    }


}
