package com.serverManagement.server.management.entity.options;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name="item-available-status")
@Entity
public class ItemAvailableStatusOptionEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String itemAvailableOption;
	public ItemAvailableStatusOptionEntity(Long id, String itemAvailableOption) {
		super();
		this.id = id;
		this.itemAvailableOption = itemAvailableOption;
	}
	public ItemAvailableStatusOptionEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getItemAvailableOption() {
		return itemAvailableOption.toUpperCase();
	}
	public void setItemAvailableOption(String itemAvailableOption) {
		this.itemAvailableOption = itemAvailableOption;
	}
	@Override
	public String toString() {
		return "ItemAvailableStatusOptionEntity [id=" + id + ", itemAvailableOption=" + itemAvailableOption + "]";
	}
	
}
