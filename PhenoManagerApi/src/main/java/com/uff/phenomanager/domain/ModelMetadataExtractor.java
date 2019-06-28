package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "model_metadata_extractor")
public class ModelMetadataExtractor extends BaseApiEntity {
	
	@Column(name = "execution_command", columnDefinition = "text")
	private String executionCommand;
	
	@Column(name = "tag", length = 80)
	private String tag;
	
	@Column(name = "extractor_file_content_type")
	private String extractorFileContentType;
	
	@Column(name = "extractor_file_id")
	private String extractorFileId;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	@Column(name = "execution_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.IDLE;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_user_account_agent", referencedColumnName = "id")
	private User userAgent;
	
	@Column(name = "extractor_file_name")
	private String extractorFileName;
	
	public ModelMetadataExtractor() {
		super();
	}
	
	public ModelMetadataExtractor(ModelMetadataExtractorBuilder builder) {
		this.extractorFileName = builder.extractorFileName;
		this.tag = builder.tag;
		this.extractorFileId = builder.extractorFileId;
		this.extractorFileContentType = builder.extractorFileContentType;
		this.computationalModel = builder.computationalModel;
		this.executionCommand = builder.executionCommand;
		this.executionStatus = builder.executionStatus;
		this.userAgent = builder.userAgent;
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
	
	public String getExecutionCommand() {
		return executionCommand;
	}

	public void setExecutionCommand(String executionCommand) {
		this.executionCommand = executionCommand;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public User getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(User userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getExtractorFileName() {
		return extractorFileName;
	}

	public void setExtractorFileName(String extractorFileName) {
		this.extractorFileName = extractorFileName;
	}
	
	public String getExtractorFileContentType() {
		return extractorFileContentType;
	}

	public void setExtractorFileContentType(String extractorFileContentType) {
		this.extractorFileContentType = extractorFileContentType;
	}

	public String getExtractorFileId() {
		return extractorFileId;
	}

	public void setExtractorFileId(String extractorFileId) {
		this.extractorFileId = extractorFileId;
	}
	
	public static ModelMetadataExtractorBuilder builder() {
		return new ModelMetadataExtractorBuilder();
	}
	
	public static class ModelMetadataExtractorBuilder extends BaseApiEntityBuilder {
		
		private String extractorFileContentType;
		private String extractorFileId;
		private String tag;
		private ComputationalModel computationalModel;
		private String executionCommand;
		private ExecutionStatus executionStatus = ExecutionStatus.IDLE;
		private User userAgent;
		private String extractorFileName;
		
		public ModelMetadataExtractorBuilder extractorFileContentType(String extractorFileContentType) {
			this.extractorFileContentType = extractorFileContentType;
			return this;
		}
		
		public ModelMetadataExtractorBuilder extractorFileId(String extractorFileId) {
			this.extractorFileId = extractorFileId;
			return this;
		}
		
		public ModelMetadataExtractorBuilder extractorFileName(String extractorFileName) {
			this.extractorFileName = extractorFileName;
			return this;
		}
		
		public ModelMetadataExtractorBuilder tag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public ModelMetadataExtractorBuilder executionStatus(ExecutionStatus executionStatus) {
			this.executionStatus = executionStatus;
			return this;
		}
		
		public ModelMetadataExtractorBuilder userAgent(User userAgent) {
			this.userAgent = userAgent;
			return this;
		}
		
		public ModelMetadataExtractorBuilder executionCommand(String executionCommand) {
			this.executionCommand = executionCommand;
			return this;
		}
		
		public ModelMetadataExtractorBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public ModelMetadataExtractor build() {
			return new ModelMetadataExtractor(this);
		}
	}

}