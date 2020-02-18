package com.uff.model.invoker.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.uff.model.invoker.domain.core.BaseApiEntity;

@Entity
@Table(name = "extractor")
public class Extractor extends BaseApiEntity {
	
	@Column(name = "execution_command", columnDefinition = "text")
	private String executionCommand;
	
	@Column(name = "abortion_command", columnDefinition = "text")
	private String abortionCommand;
	
	@Column(name = "tag", length = 80)
	private String tag;
	
	@Column(name = "file_content_type")
	private String fileContentType;
	
	@Column(name = "file_id")
	private String fileId;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	@Column(name = "file_name")
	private String fileName;
	
	public Extractor() {
		super();
	}
	
	public Extractor(ExtractorBuilder builder) {
		this.fileName = builder.fileName;
		this.tag = builder.tag;
		this.fileId = builder.fileId;
		this.fileContentType = builder.fileContentType;
		this.computationalModel = builder.computationalModel;
		this.executionCommand = builder.executionCommand;
		this.abortionCommand = builder.abortionCommand;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public String getExecutionCommand() {
		return executionCommand;
	}

	public void setExecutionCommand(String executionCommand) {
		this.executionCommand = executionCommand;
	}
	
	public String getAbortionCommand() {
		return abortionCommand;
	}

	public void setAbortionCommand(String abortionCommand) {
		this.abortionCommand = abortionCommand;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static ExtractorBuilder builder() {
		return new ExtractorBuilder();
	}
	
	public static class ExtractorBuilder extends BaseApiEntityBuilder {
		
		private String fileContentType;
		private String fileId;
		private String tag;
		private ComputationalModel computationalModel;
		private String executionCommand;
		private String abortionCommand;
		private String fileName;
		
		public ExtractorBuilder fileContentType(String fileContentType) {
			this.fileContentType = fileContentType;
			return this;
		}
		
		public ExtractorBuilder fileId(String fileId) {
			this.fileId = fileId;
			return this;
		}
		
		public ExtractorBuilder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		public ExtractorBuilder abortionCommand(String abortionCommand) {
			this.abortionCommand = abortionCommand;
			return this;
		}
		
		public ExtractorBuilder tag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public ExtractorBuilder executionCommand(String executionCommand) {
			this.executionCommand = executionCommand;
			return this;
		}
		
		public ExtractorBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public Extractor build() {
			return new Extractor(this);
		}
	}

}