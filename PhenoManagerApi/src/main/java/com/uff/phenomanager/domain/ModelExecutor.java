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
@Table(name = "model_executor")
public class ModelExecutor extends BaseApiEntity {
	
	@Column(name = "execution_command", columnDefinition = "text")
	private String executionCommand;
	
	@Column(name = "abort_command", columnDefinition = "text")
	private String abortCommand;
	
	@Column(name = "job_name", length = 150)
	private String jobName;
	
	@Column(name = "tag", length = 80)
	private String tag;
	
	@Column(name = "executor_file_id")
	private String executorFileId;
	
	@Column(name = "executor_file_content_type")
	private String executorFileContentType;
	
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
	
	@Column(name = "execution_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.IDLE;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_user_account_agent", referencedColumnName = "id")
	private User userAgent;
	
	@Column(name = "executor_file_name")
	private String executorFileName;
	
	public ModelExecutor() {
		super();
	}
	
	public ModelExecutor(ModelExecutorBuilder builder) {
		this.executorFileName = builder.executorFileName;
		this.executorFileId = builder.executorFileId;
		this.executorFileContentType = builder.executorFileContentType;
		this.computationalModel = builder.computationalModel;
		this.executionCommand = builder.executionCommand;
		this.abortCommand = builder.abortCommand;
		this.webServiceType = builder.webServiceType;
		this.httpVerb = builder.httpVerb;
		this.httpHeaders = builder.httpHeaders;
		this.httpBody = builder.httpBody;
		this.executionUrl = builder.executionUrl;
		this.jobName = builder.jobName;
		this.tag = builder.tag;
		this.useEnviromentVariables = builder.useEnviromentVariables;
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
	
	public String getExecutorFileId() {
		return executorFileId;
	}

	public void setExecutorFileId(String executorFileId) {
		this.executorFileId = executorFileId;
	}

	public String getExecutorFileContentType() {
		return executorFileContentType;
	}

	public void setExecutorFileContentType(String executorFileContentType) {
		this.executorFileContentType = executorFileContentType;
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

	public String getAbortCommand() {
		return abortCommand;
	}

	public void setAbortCommand(String abortCommand) {
		this.abortCommand = abortCommand;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Boolean getUseEnviromentVariables() {
		return useEnviromentVariables;
	}

	public void setUseEnviromentVariables(Boolean useEnviromentVariables) {
		this.useEnviromentVariables = useEnviromentVariables;
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
	
	public String getExecutorFileName() {
		return executorFileName;
	}

	public void setExecutorFileName(String executorFileName) {
		this.executorFileName = executorFileName;
	}

	public static ModelExecutorBuilder builder() {
		return new ModelExecutorBuilder();
	}
	
	public static class ModelExecutorBuilder extends BaseApiEntityBuilder {
		
		private String executorFileId;
		private String executorFileContentType;
		private ComputationalModel computationalModel;
		private String executionCommand;
		private String abortCommand;
		private WebServiceType webServiceType;
		private HttpVerb httpVerb;
		private String httpHeaders;
		private String httpBody;
		private String executionUrl;
		private String jobName;
		private Boolean useEnviromentVariables = Boolean.FALSE;
		private ExecutionStatus executionStatus = ExecutionStatus.IDLE;
		private User userAgent;
		private String tag;
		private String executorFileName;
		
		public ModelExecutorBuilder executorFileId(String executorFileId) {
			this.executorFileId = executorFileId;
			return this;
		}
		
		public ModelExecutorBuilder executorFileContentType(String executorFileContentType) {
			this.executorFileContentType = executorFileContentType;
			return this;
		}
		public ModelExecutorBuilder executorFileName(String executorFileName) {
			this.executorFileName = executorFileName;
			return this;
		}
		
		public ModelExecutorBuilder tag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public ModelExecutorBuilder useEnviromentVariables(Boolean useEnviromentVariables) {
			this.useEnviromentVariables = useEnviromentVariables;
			return this;
		}
		
		public ModelExecutorBuilder executionStatus(ExecutionStatus executionStatus) {
			this.executionStatus = executionStatus;
			return this;
		}
		
		public ModelExecutorBuilder userAgent(User userAgent) {
			this.userAgent = userAgent;
			return this;
		}
		public ModelExecutorBuilder jobName(String jobName) {
			this.jobName = jobName;
			return this;
		}
		
		public ModelExecutorBuilder httpProtocolType(String abortCommand) {
			this.abortCommand = abortCommand;
			return this;
		}
		
		public ModelExecutorBuilder webServiceType(WebServiceType webServiceType) {
			this.webServiceType = webServiceType;
			return this;
		}
		
		public ModelExecutorBuilder httpVerb(HttpVerb httpVerb) {
			this.httpVerb = httpVerb;
			return this;
		}
		
		public ModelExecutorBuilder httpHeaders(String httpHeaders) {
			this.httpHeaders = httpHeaders;
			return this;
		}
		
		public ModelExecutorBuilder httpBody(String httpBody) {
			this.httpBody = httpBody;
			return this;
		}
		
		public ModelExecutorBuilder executionUrl(String executionUrl) {
			this.executionUrl = executionUrl;
			return this;
		}
		
		public ModelExecutorBuilder executionCommand(String executionCommand) {
			this.executionCommand = executionCommand;
			return this;
		}
		
		public ModelExecutorBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public ModelExecutor build() {
			return new ModelExecutor(this);
		}
	}

}