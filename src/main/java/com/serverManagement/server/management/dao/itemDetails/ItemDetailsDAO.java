package com.serverManagement.server.management.dao.itemDetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsDTOForCSV;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.service.itemDetails.GetSerialIdOnly;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDetailsDAO
                extends JpaRepository<ItemDetailsEntity, Long>, JpaSpecificationExecutor<ItemDetailsEntity> {

        @Query("SELECT um FROM ItemDetailsEntity um WHERE LOWER(um.serial_No)= (:serial)")
        public ItemDetailsEntity getComponentDetailsBySerialNo(@Param("serial") String serial);

        @Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM ItemDetailsEntity um WHERE LOWER(um.serial_No)= (:serial)")
        public boolean isSerialExist(@Param("serial") String serial);

        @Query("SELECT um.serial_No, um.id FROM ItemDetailsEntity um WHERE LOWER(um.serial_No)= (:serial)")
        public GetSerialIdOnly getSerialAndIdExistBySerial(@Param("serial") String serial);

        @Query("SELECT um FROM ItemDetailsEntity um WHERE LOWER(um.serial_No)= (:serial)")
        public ItemDetailsEntity getItemDetailsBySerialNo(@Param("serial") String serial);

        @Query("SELECT um.itemHistoryUpdatedByAdminEntityList FROM ItemDetailsEntity um WHERE LOWER(um.serial_No)= (:serial)")
        public List<ItemHistoryUpdatedByAdminEntity> getComponentHistorySerialNo(@Param("serial") String serial);

        @Query("SELECT new com.serverManagement.server.management.entity.itemDetails.ItemDetailsDTOForCSV(um.serial_No, um.boxNo, um.partNo,  um.rack_No, um.spare_Location, um.system, um.modelNo, um.moduleFor, um.system_Version, um.itemDescription, um.remark, um.empEmail, um.addedByEmail, um.partyName, um.update_Date, um.adding_Date, is.itemStatus, ia.itemAvailableOption, r.city, k.keywordName, sk.subKeyword) FROM ItemDetailsEntity um LEFT JOIN um.itemStatusId is LEFT JOIN um.availableStatusId ia LEFT JOIN um.region r LEFT JOIN um.keywordEntity k LEFT JOIN um.subKeyWordEntity sk WHERE um.region= (:regionId)")
        public List<ItemDetailsDTOForCSV> getRegionAllItemList(@Param("regionId") RegionEntity regionId);

        @Query("SELECT COUNT(i) FROM ItemDetailsEntity i " + "WHERE i.partNo = :partNo " + "AND i.region = :region "
                        + "AND i.availableStatusId.itemAvailableOption = 'AVAILABLE'")
        long countAvailableByPartNoAndRegion(@Param("partNo") String partNo, @Param("region") RegionEntity region);

        @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END "
                        + "FROM ItemDetailsEntity i WHERE i.partNo = :partNo")
        boolean existsByPartNo(@Param("partNo") String partNo);

        @Query("SELECT COUNT(i) FROM ItemDetailsEntity i")
        long countAllItems();

        @Query("SELECT i.region.city, COUNT(i) FROM ItemDetailsEntity i GROUP BY i.region.city")
        List<Object[]> countByRegion();

        @Query("SELECT COUNT(i) FROM ItemDetailsEntity i WHERE lower(i.region.city) = :city")
        long countByRegionName(@Param("city") String city);

        @Query("select count(i) from ItemDetailsEntity i where lower(i.keywordEntity.keywordName) = :kw")
        long countByKeywordNameLower(@Param("kw") String kw);

        // Get distinct products for RMA form dropdown
        @Query("SELECT DISTINCT i.system, i.modelNo, i.partNo FROM ItemDetailsEntity i WHERE i.system IS NOT NULL ORDER BY i.system")
        List<Object[]> findDistinctProducts();

}
