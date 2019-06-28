package com.uff.phenomanager.domain.dto;

public class ValidationStatisticsDto {
	
	private Long itemsValidated;
	private Long totalItems;
	
	public ValidationStatisticsDto() {}

	public ValidationStatisticsDto(NotificationMessageDtoBuilder builder) {
		this.itemsValidated = builder.itemsValidated;
		this.totalItems = builder.totalItems;
	}
	
	public Long getItemsValidated() {
		return itemsValidated;
	}

	public void setItemsValidated(Long itemsValidated) {
		this.itemsValidated = itemsValidated;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public static NotificationMessageDtoBuilder builder() {
		return new NotificationMessageDtoBuilder();
	}
	
	public static class NotificationMessageDtoBuilder {
		
		private Long itemsValidated;
		private Long totalItems;
		
		public NotificationMessageDtoBuilder itemsValidated(Long itemsValidated) {
			this.itemsValidated = itemsValidated;
			return this;
		}
		
		public NotificationMessageDtoBuilder totalItems(Long totalItems) {
			this.totalItems = totalItems;
			return this;
		}
		
		public ValidationStatisticsDto build() {
			return new ValidationStatisticsDto(this);
		}
	}
	
}