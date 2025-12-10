package com.serverManagement.server.management.entity.options;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name="item-status-option")
@Entity
public class ItemStatusOptionEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String itemStatus;
	public ItemStatusOptionEntity(Long id, String itemStatus) {
		super();
		this.id = id;
		this.itemStatus = itemStatus;
	}
	public ItemStatusOptionEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	@Override
	public String toString() {
		return "ItemStatusOptionEntity [id=" + id + ", itemStatus=" + itemStatus + "]";
	}
	
}
