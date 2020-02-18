package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.uff.phenomanager.domain.core.BaseApiEntity;

@MappedSuperclass
public class ResearchObject extends BaseApiEntity {
	
	@Column(name = "name", length = 80)
	private String name;
	
	@Column(name = "description", columnDefinition = "text")
	private String description;
	
	public ResearchObject() {}

	public ResearchObject(ResearchObjectBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static class ResearchObjectBuilder extends BaseApiEntityBuilder {
		
		protected String name;
		protected String description;
		
		public ResearchObjectBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public ResearchObjectBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		public ResearchObject build() {
			return new ResearchObject(this);
		}
	}

}