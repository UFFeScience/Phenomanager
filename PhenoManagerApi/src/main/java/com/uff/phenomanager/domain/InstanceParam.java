package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "instance_param")
public class InstanceParam extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	@JsonBackReference(value = "computationalInstanceParams")
	private ComputationalModel computationalModel;
	
	@OneToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_conceptual_param")
	private ConceptualParam conceptualParam;
	
	@Column(name = "key", length = 80)
	private String key;
	
	@Column(name = "description", columnDefinition = "text")
	private String description;
	
	@Column(name = "value", columnDefinition = "text")
	private String value;
	
	@Column(name = "value_file_id")
	private String valueFileId;
	
	@Column(name = "value_file_content_type")
	private String valueFileContentType;
	
	@Column(name = "value_file_name")
	private String valueFileName;
	
	public InstanceParam() {}
	
	public InstanceParam(InstanceParamBuilder builder) {
		this.value = builder.value;
		this.valueFileName = builder.valueFileName;
		this.computationalModel = builder.computationalModel;
		this.key = builder.key;
		this.valueFileId = builder.valueFileId;
		this.valueFileContentType = builder.valueFileContentType;
		this.description = builder.description;
		this.conceptualParam = builder.conceptualParam;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
		this.setCountDistinct(builder.getCountDistinct());
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
		
	public String getValueFileId() {
		return valueFileId;
	}

	public void setValueFileId(String valueFileId) {
		this.valueFileId = valueFileId;
	}

	public String getValueFileContentType() {
		return valueFileContentType;
	}

	public void setValueFileContentType(String valueFileContentType) {
		this.valueFileContentType = valueFileContentType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public ConceptualParam getConceptualParam() {
		return conceptualParam;
	}

	public void setConceptualParam(ConceptualParam conceptualParam) {
		this.conceptualParam = conceptualParam;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getValueFileName() {
		return valueFileName;
	}

	public void setValueFileName(String valueFileName) {
		this.valueFileName = valueFileName;
	}

	public static InstanceParamBuilder builder() {
		return new InstanceParamBuilder();
	}
	
	public static class InstanceParamBuilder extends BaseApiEntityBuilder {
		
		private ComputationalModel computationalModel;
		private ConceptualParam conceptualParam;
		private String key;
		private String value;
		private String valueFileId;
		private String valueFileContentType;
		private String description;
		private String valueFileName;
		
		public InstanceParamBuilder valueFileId(String valueFileId) {
			this.valueFileId = valueFileId;
			return this;
		}
		
		public InstanceParamBuilder valueFileContentType(String valueFileContentType) {
			this.valueFileContentType = valueFileContentType;
			return this;
		}
		
		public InstanceParamBuilder value(String value) {
			this.value = value;
			return this;
		}
		
		public InstanceParamBuilder valueFileName(String valueFileName) {
			this.valueFileName = valueFileName;
			return this;
		}
		
		public InstanceParamBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public InstanceParamBuilder conceptualParam(ConceptualParam conceptualParam) {
			this.conceptualParam = conceptualParam;
			return this;
		}
		
		public InstanceParamBuilder key(String key) {
			this.key = key;
			return this;
		}
		
		public InstanceParamBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		public InstanceParam build() {
			return new InstanceParam(this);
		}
	}

}