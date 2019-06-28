package com.uff.phenomanager.domain.core;

public class ApiMetadata {
	
	private Long totalCount;
	private Integer pageOffset;
	private Integer pageSize;
	
	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(Integer pageOffset) {
		this.pageOffset = pageOffset;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public String toString() {
		return "ApiMetadata [totalCount=" + totalCount + ", pageOffset=" + pageOffset + ", pageSize=" + pageSize + "]";
	}
	
}