package com.uff.model.invoker.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.uff.model.invoker.domain.core.BaseApiEntity;

@Entity
@Table(name = "executor")
public class Executor extends BaseApiEntity {
	
	@Column(name = "execution_command", columnDefinition = "text")
	private String executionCommand;
	
	@Column(name = "abortion_command", columnDefinition = "text")
	private String abortionCommand;
	
	@Column(name = "job_name", length = 150)
	private String jobName;
	
	@Column(name = "tag", length = 80)
	private String tag;
	
	@Column(name = "file_id")
	private String fileId;
	
	@Column(name = "file_content_type")
	private String fileContentType;
	
	@Column(name = "web_service_type")
	@Enumerated(EnumType.STRING)
	private WebServiceType webServiceType;
	
	@Column(name = "execution_url", columnDefinition = "text")
	private String executionUrl;
	
	@Column(name = "http_verb")
	@Enumerated(EnumType.STRING)
	private HttpVerb httpVerb;
	
	@Column(name = "http_headers", columnDefinition = "text")
	private String httpHeaders;
	
	@Column(name = "http_body", columnDefinition = "text")
	private String httpBody;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	@Column(name = "use_enviroment_variables")
	private Boolean useEnviromentVariables;
	
	@Column(name = "file_name")
	private String fileName;
	
	public Executor() {
		super();
	}
	
	public Executor(ExecutorBuilder builder) {
		this.fileName = builder.fileName;
		this.tag = builder.tag;
		this.fileId = builder.fileId;
		this.fileContentType = builder.fileContentType;
		this.computationalModel = builder.computationalModel;
		this.executionCommand = builder.executionCommand;
		this.abortionCommand = builder.abortionCommand;
		this.webServiceType = builder.webServiceType;
		this.httpVerb = builder.httpVerb;
		this.httpHeaders = builder.httpHeaders;
		this.httpBody = builder.httpBody;
		this.executionUrl = builder.executionUrl;
		this.jobName = builder.jobName;
		this.useEnviromentVariables = builder.useEnviromentVariables;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
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

	public WebServiceType getWebServiceType() {
		return webServiceType;
	}

	public void setWebServiceType(WebServiceType webServiceType) {
		this.webServiceType = webServiceType;
	}

	public String getExecutionUrl() {
		return executionUrl;
	}

	public void setExecutionUrl(String executionUrl) {
		this.executionUrl = executionUrl;
	}

	public HttpVerb getHttpVerb() {
		return httpVerb;
	}

	public void setHttpVerb(HttpVerb httpVerb) {
		this.httpVerb = httpVerb;
	}

	public String getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(String httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public String getHttpBody() {
		return httpBody;
	}

	public void setHttpBody(String httpBody) {
		this.httpBody = httpBody;
	}

	public String getAbortionCommand() {
		return abortionCommand;
	}

	public void setAbortionCommand(String abortionCommand) {
		this.abortionCommand = abortionCommand;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public Boolean getUseEnviromentVariables() {
		return useEnviromentVariables;
	}

	public void setUseEnviromentVariables(Boolean useEnviromentVariables) {
		this.useEnviromentVariables = useEnviromentVariables;
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
	
	
	public static ExecutorBuilder builder() {
		return new ExecutorBuilder();
	}
	
	public static class ExecutorBuilder extends BaseApiEntityBuilder {
		
		private String fileId;
		private String fileContentType;
		private String tag;
		private ComputationalModel computationalModel;
		private String executionCommand;
		private String abortionCommand;
		private WebServiceType webServiceType;
		private HttpVerb httpVerb;
		private String httpHeaders;
		private String httpBody;
		private String executionUrl;
		private String jobName;
		private Boolean useEnviromentVariables = Boolean.FALSE;
		private String fileName;
		
		public ExecutorBuilder fileId(String fileId) {
			this.fileId = fileId;
			return this;
		}
		
		public ExecutorBuilder fileContentType(String fileContentType) {
			this.fileContentType = fileContentType;
			return this;
		}
		public ExecutorBuilder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		public ExecutorBuilder tag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public ExecutorBuilder useEnviromentVariables(Boolean useEnviromentVariables) {
			this.useEnviromentVariables = useEnviromentVariables;
			return this;
		}
		
		public ExecutorBuilder jobName(String jobName) {
			this.jobName = jobName;
			return this;
		}
		
		public ExecutorBuilder httpProtocolType(String abortCommand) {
			this.abortionCommand = abortCommand;
			return this;
		}
		
		public ExecutorBuilder webServiceType(WebServiceType webServiceType) {
			this.webServiceType = webServiceType;
			return this;
		}
		
		public ExecutorBuilder httpVerb(HttpVerb httpVerb) {
			this.httpVerb = httpVerb;
			return this;
		}
		
		public ExecutorBuilder httpHeaders(String httpHeaders) {
			this.httpHeaders = httpHeaders;
			return this;
		}
		
		public ExecutorBuilder httpBody(String httpBody) {
			this.httpBody = httpBody;
			return this;
		}
		
		public ExecutorBuilder executionUrl(String executionUrl) {
			this.executionUrl = executionUrl;
			return this;
		}
		
		public ExecutorBuilder executionCommand(String executionCommand) {
			this.executionCommand = executionCommand;
			return this;
		}
		
		public ExecutorBuilder abortionCommand(String abortionCommand) {
			this.abortionCommand = abortionCommand;
			return this;
		}
		
		public ExecutorBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public Executor build() {
			return new Executor(this);
		}
	}

}