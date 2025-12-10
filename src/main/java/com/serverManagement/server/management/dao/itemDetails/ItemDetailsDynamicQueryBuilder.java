package com.serverManagement.server.management.dao.itemDetails;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.criteria.Predicate;

public class ItemDetailsDynamicQueryBuilder {
	
	public static Specification<ItemDetailsEntity> getItemDetailsQuery(RegionEntity region, KeywordEntity keyword, SubKeywordEntity subKeyword, ItemAvailableStatusOptionEntity itemAvailEntity, ItemStatusOptionEntity itemStatusOptionEntity, String partNo, String systemName){
		return (root,query,criteriaBuilder)->{
			List<Predicate> predicates=new ArrayList<Predicate>();

	        // Select only specific columns (moved before the if conditions)
			query.multiselect(
	                root.get("serial_No"),
	                root.get("rack_No"),
	                root.get("itemStatusId"),
	                root.get("system"),
	                root.get("system_Version"),
	                root.get("moduleFor"),
	                root.get("itemDescription"),
	                root.get("partyName"),
	                root.get("update_Date"),
	                root.get("region"),
	                root.get("remark")
	        );
	        
	        if(region==null) {
	            throw new IllegalArgumentException("Region is required.");
	        }else {
	           predicates.add(criteriaBuilder.equal(root.get("region"), region));
	        	 //predicates.add(root.get("region").in(region));
	        }

//	        if(keyword==null) {
//	            throw new IllegalArgumentException("Keyword is required."); // Corrected message
//	        }
	        if(keyword!=null && keyword.getId()!=null) {
	            predicates.add(criteriaBuilder.equal(root.get("keywordEntity"), keyword));
	        	//predicates.add(root.get("keywordEntity").in(keyword));	        	
	        }
	        	//check for subKeyword
			if (subKeyword!=null && subKeyword.getId()!=null) {
	        	    predicates.add(criteriaBuilder.equal(root.get("subKeyWordEntity"), subKeyword));
	            	//predicates.add(root.get("subKeyWordEntity").in(subKeyword));
	            }
			
			//check for available 
			if (itemAvailEntity!=null && itemAvailEntity.getId()!=null) {
        	    predicates.add(criteriaBuilder.equal(root.get("availableStatusId"), itemAvailEntity));
            	//predicates.add(root.get("subKeyWordEntity").in(subKeyword));
            }
			
			//check for item status
			if (itemStatusOptionEntity!=null && itemStatusOptionEntity.getId()!=null) {
        	    predicates.add(criteriaBuilder.equal(root.get("itemStatusId"), itemStatusOptionEntity));
            	//predicates.add(root.get("subKeyWordEntity").in(subKeyword));
            }
			
			//check partNo
			if(partNo!=null && partNo.trim().length()>0) {
				predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("partNo")), partNo.toLowerCase()));
			}
			
			//check for system Name
			if(systemName!=null && systemName.trim().length()>0) {
				predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("system")), systemName.toLowerCase()));
			}
			
	        if (predicates.isEmpty()) {
	            throw new IllegalArgumentException("At least one search parameter is required.");
	        }

	        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
