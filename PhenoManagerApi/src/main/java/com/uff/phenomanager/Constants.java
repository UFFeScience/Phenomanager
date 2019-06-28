package com.uff.phenomanager;

public interface Constants {
	
	final String LOCALE_PT = "pt";
	final String LOCALE_BR = "BR";
	final String DATE_TIMEZONE = "UTC";
	final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";
	final String NULL_VALUE = "null";
	final String TMP_DIR = "java.io.tmpdir";
	final String BASE_DOMAIN_URL = "${base.domain.url}";
	final String DEFAULT_ENCODING = "UTF-8";
	final String IMAGE_FORMAT = "png";
	final String TEMP_FILE_SUFFIX = ".png";
	final String TEMP_FILE_PREFIX = "image_profile";
	final String IMAGE_BASE_64_PREFIX = "data:image/png;base64,";
	final int IMG_WIDTH = 100;
	final int IMG_HEIGHT = 100;
	
	public interface API_CLIENT {
		public interface SCI_MANAGER {
			final String BASE_DOMAIN_URL = "${scimanager.base.domain.url}";
			final String SCIENTIFIC_PROJECT_SYNC_PATH = "/api/phenomanager/sync/scientific-project";
			final String USER_SYNC_PATH = "/api/phenomanager/sync/user";
		}
	}
	
	public interface GOOGLE_API {
		final String CLIENT_SECRET = "/client_secret.json";
		final String APPLICATION_NAME = "PhenoManagerApi";
		final String REFRESH_TOKEN = "${google.oauth2.refresh.token}";
		final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
		final String ID_FIELD_BASE = "id";
		final String FILE_FIELDS = "id, name, parents, createdTime";
		final String FOLDER_QUERY_NAME_NO_PARENT = "mimeType='application/vnd.google-apps.folder' and name='%s'";
		final String FOLDER_QUERY_NAME_PARENT = "mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents";
		final String FILE_QUERY_NAME_PARENT = "name='%s' and '%s' in parents";
		final String FOLDER_QUERY_PARENT = "mimeType!='application/vnd.google-apps.folder' and '%s' in parents";
		final String DRIVE_SPACES = "drive";
		final String FOLDER_QUERY_FIELDS = "nextPageToken, files(" + FILE_FIELDS + ")";
		final String ANYONE_PERMISSION_TYPE = "anyone";
		final String READER_PERMISSION_TOLE = "reader";
		final Integer CONNECT_TIMEOUT = 3 * 600000;
		final Integer READ_TIMEOUT = 3 * 600000;
	}
	
	public interface UPLOAD {
		final String ROOT_FOLDER = "model_data";
		final String MODEL_EXECUTOR_FOLDER = "model_executor";
		final String MODEL_EXTRACTOR_FOLDER = "model_extractor";
		final String INSTANCE_PARAM_FOLDER = "instance_param";
		final String VALIDATION_ITEM_FOLDER = "validation_item";
		final String METADATA_FOLDER = "metadata";
		final String PROFILE_IMAGES_FOLDER = "profile_image";
		final String FOLDER_SEPARATOR = " > ";
		final String WORD_SEPARATOR = "_";
	}

	public interface CONTROLLER {
		final String VERSION = "v1";
		final String PATH_SEPARATOR = "/";
		final String SLUG = "slug";
		final String SLUG_PARAM = "{" + SLUG + "}";
		final String SLUG_PATH = PATH_SEPARATOR + SLUG_PARAM;
		final String HEALTH_PATH = "/health";
		final String BASE_PATH = PATH_SEPARATOR + VERSION + PATH_SEPARATOR;
		
		public interface LOGIN {
			final String EMAIL_FIELD = "email";
			final String PASSWORD_FIELD = "password";
			final String NAME = "login";
			final String PATH = PATH_SEPARATOR + NAME;
		}
		
		public interface PERMISSION {
			final String NAME = "permissions";
			final String PATH = BASE_PATH + NAME;
		}
		
		public interface USER {
			final String NAME = "users";
			final String PATH = BASE_PATH + NAME;
			
			final String SYNC_NAME = "sync";
			final String SYNC_PATH = SLUG_PATH + PATH_SEPARATOR + SYNC_NAME;
		}
		
		public interface TEAM {
			final String NAME = "teams";
			final String PATH = BASE_PATH + NAME;
		}
		
		public interface PROJECT {
			final String NAME = "projects";
			final String PATH = BASE_PATH + NAME;
			
			final String SYNC_NAME = "sync";
			final String SYNC_PATH = SLUG_PATH + PATH_SEPARATOR + SYNC_NAME;
		}
		
		public interface DASHBOARD {
			final String NAME = "dashboard";
			final String PATH = BASE_PATH + NAME;
			
			final String VALIDATION_STATISTICS_NAME = "validation_item_statistics";
			final String VALIDATION_STATISTICS_NAME_PATH = PATH_SEPARATOR + VALIDATION_STATISTICS_NAME;
			
			final String RUNING_MODELS_NAME = "running_models";
			final String RUNING_MODELS_PATH = PATH_SEPARATOR + RUNING_MODELS_NAME;
			
			final String ERROR_MODELS_NAME = "error_models";
			final String ERROR_MODELS_PATH = PATH_SEPARATOR + ERROR_MODELS_NAME;
		}
		
		public interface PHENOMENON {
			final String NAME = "phenomenons";
			final String PATH = BASE_PATH + NAME;
		}
		
		public interface HYPOTHESIS {
			final String NAME = "hypotheses";
			final String PATH = BASE_PATH + NAME;
		}
		
		public interface EXPERIMENT {
			final String NAME = "experiments";
			final String PATH = BASE_PATH + NAME;
		}
		
		public interface COMPUTATIONAL_MODEL {
			final String NAME = "computational_models";
			final String PATH = BASE_PATH + NAME;
			
			final String RUN_NAME = "run";
			final String RUN_PATH = SLUG_PATH + PATH_SEPARATOR + RUN_NAME;
		}
		
		public interface CONCEPTUAL_PARAM {
			final String NAME = "conceptual_params";
			final String EXPERIMENT_SLUG = "experimentSlug";
			final String PATH_EXPERIMENT_SLUG = "{" + EXPERIMENT_SLUG + "}";
			final String PATH = EXPERIMENT.PATH + PATH_SEPARATOR + PATH_EXPERIMENT_SLUG + PATH_SEPARATOR + NAME;
		}
		
		public interface PHASE {
			final String NAME = "phases";
			final String EXPERIMENT_SLUG = "experimentSlug";
			final String PATH_EXPERIMENT_SLUG = "{" + EXPERIMENT_SLUG + "}";
			final String PATH = EXPERIMENT.PATH + PATH_SEPARATOR + PATH_EXPERIMENT_SLUG + PATH_SEPARATOR + NAME;
		}
		
		public interface VALIDATION_ITEM {
			final String NAME = "validation_items";
			final String EXPERIMENT_SLUG = "experimentSlug";
			final String PATH_EXPERIMENT_SLUG = "{" + EXPERIMENT_SLUG + "}";
			final String PATH = EXPERIMENT.PATH + PATH_SEPARATOR + PATH_EXPERIMENT_SLUG + PATH_SEPARATOR + NAME;
			
			final String VALIDATION_EVIDENCE_NAME = "validation_evidence";
			final String VALIDATION_EVIDENCE_PATH = SLUG_PATH + PATH_SEPARATOR + VALIDATION_EVIDENCE_NAME;
		}
		
		public interface INSTANCE_PARAM {
			final String NAME = "instance_params";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;
			
			final String VALUE_FILE_NAME = "value_file";
			final String VALUE_FILE_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + VALUE_FILE_NAME;
		}
		
		public interface EXECUTION_ENVIRONMENT {
			final String NAME = "execution_environments";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;
		}
		
		public interface MODEL_EXECUTOR {
			final String NAME = "model_executors";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;
			
			final String EXECUTOR_NAME = "executor";
			final String EXECUTOR_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + EXECUTOR_NAME;
		}
		
		public interface MODEL_METADATA_EXTRACTOR {
			final String NAME = "model_metadata_extractors";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;
			
			final String EXTRACTOR_NAME = "extractor";
			final String EXTRACTOR_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + EXTRACTOR_NAME;
		}
		
		public interface EXTRACTOR_METADATA {
			final String NAME = "extractor_metadatas";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;

			final String EXECUTION_METADATA_NAME = "execution_metadata";
			final String EXECUTION_METADATA_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + EXECUTION_METADATA_NAME;
		}
		
		public interface MODEL_RESULT_METADATA {
			final String NAME = "model_result_metadatas";
			final String COMPUTATIONAL_MODEL_SLUG = "computationalModelSlug";
			final String PATH_COMPUTATIONAL_MODEL_SLUG = "{" + COMPUTATIONAL_MODEL_SLUG + "}";
			final String PATH = COMPUTATIONAL_MODEL.PATH + PATH_SEPARATOR + PATH_COMPUTATIONAL_MODEL_SLUG + PATH_SEPARATOR + NAME;
			
			final String RESEARCH_OBJECT_NAME = "research_object";
			final String RESEARCH_OBJECT_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + RESEARCH_OBJECT_NAME;
			
			final String EXECUTION_METADATA_NAME = "execution_metadata";
			final String EXECUTION_METADATA_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + EXECUTION_METADATA_NAME;
			
			final String ABORT_METADATA_NAME = "abort_metadata";
			final String ABORT_METADATA_NAME_PATH = SLUG_PATH + PATH_SEPARATOR + ABORT_METADATA_NAME;
		}
	}
	
	public interface MULTITHREAD {
		final String CORE_POOL_SIZE = "${multithread.core-pool-size}";
		final String MAX_POOL_SIZE = "${multithread.max-pool-size}";
		final String QUEUE_CAPACITY = "${multithread.queue-capacity}";
	}
	
	public interface DOWNLOAD {
		final String CONTENT_HEADER = "Content-Disposition";
		final String ATTACHMENT_HEADER = "attachment; filename=%s";
	}
	
	public interface JWT_AUTH {
		final String EXPIRATION_TIME = "${jwt.expiration}";
		final String SECRET = "${jwt.secret}";
		final String TOKEN_PREFIX = "${jwt.prefix}";
		final String HEADER_STRINGS = "${jwt.header.strings}";
		final String CLAIM_EMAIL = "email";
		final String CLAIM_NAME = "name";
		final String CLAIM_ROLE = "role";
		final String CLAIM_INSTITUITION_NAME = "institutionName";
		final String CLAIM_USER_SLUG = "userSlug";
		final String TOKEN = "token";
		final String BEARER = "Bearer";
		final String X_ACCESS_TOKEN = "x-access-token";
		final String CONTENT_TYPE = "Content-Type";
		final String CACHE_CONTROL = "Cache-Control";
		final String AUTHORIZATION = "Authorization";
		final String CONTENT_DISPOSITION = "Content-Disposition";
		final String ALL_PATH_CORS_REGEX = "/**";
		final String ALL_PATH_ORIGIN_REGEX = "*";
	}
	
	public interface RABBIT_MQ {
		final String MODEL_EXECUTION_QUEUE = "modelExecution";
		final String NOTIFICATION_QUEUE = "notification";
	}
	
	public interface MSG_ERROR {
		final String GET_PARAMETRIZED_CLASS_ERROR = "Could not get parametrized class";
		final String AUTHENTICATION_ERROR = "Wrong username or password";
		final String USER_NOT_FOUND_ERROR = "User not found with email [{}] and password [{}]";
		final String USER_NOT_FOUND_SLUG_ERROR = "User not found with slug [%s]";
		final String USER_PASSWORD_NULL_ERROR = "User password can not be empty or null";
		final String ENTITIES_NOT_FOUND_ERROR = "No entities found for requestFilter [%s]";
		final String BAD_REQUEST_ERROR = "Malformed request for requestFilter [%s]";
		final String ENTITY_NOT_FOUND_ERROR = "No entity found for slug [%s]";
		final String PARSE_PROJECTIONS_ERROR = "Error parsing projections of filter [%s]";
		final String PARSE_FILTER_FIELDS_ERROR = "Error parsing filter fields of filter [%s]";
		final String PARSE_SORT_ORDER_ERROR = "Error parsing sort order of filter [%s]";
		final String UNEXPECTED_FETCHING_ERROR = "Unexpected error processing query data [%s]";
		final String INVALID_AGGREGATION_ERROR = "Invalid aggregation fields";
		final String INVALID_FILE_CONTENT_ERROR = "Invalid file content error";
		final String PARENT_ENTITY_NULL_ERROR = "Parent Entity [%s] can't be null or empty";
		final String PARENT_ENTITY_NOT_FOUND_ERROR = "Parent Entity [%s] not found for slug [%s]";
		final String PARENT_ENTITY_HYPOTHESIS_NOT_FOUND_ERROR = "Parent Hypothesis of slug [%s] not found for Entity of slug [%s]";
		final String PERMISSION_USER_NOT_FOUND_ERROR = "User of slug [%s] not found for for permission creation";
		final String PERMISSION_TEAM_NOT_FOUND_ERROR = "Team of slug [%s] not found for for permission creation";
		final String ENTITY_PERMISSION_NOT_FOUND_ERROR = "Entity [%s] of slug [%s] not found for permission";
		final String DUPLICATE_USER_PERMISSION_ERROR = "User of slug [%s] already have a permission for this entity";
		final String INVALID_ENTITY_PERMISSION_ERROR = "Invalid entity type [%s] for permission";
		final String ERROR_PARSE_DATE = "Error while parsing date [{}] to Calendar";
		final String ERROR_STRING_DATE_NULL = "String date is null";
		final String ERROR_SENDING_MAIL = "Error while sending email";
		final String AUTH_ERROR_INVALID_TOKEN = "Invalid token [{}].";
		final String AUTHORIZATION_TOKEN_NOT_VALID = "Authorization token not valid";
		final String KEY = "message";
		final String ERROR_PROCESSING_DATA = "Error while processing data";
		final String INTERNAL_ERROR = "Unexpected error";
		final String COMPUTATIONAL_MODEL_NOT_RUNNING_ERROR = "Computational Model can't be stopped because it's not currently running";
		final String COMPUTATIONAL_MODEL_ALREADY_RUNNING_ERROR = "Computational Model can't start process because it's already running a process for this module";
		final String ANY_COMPUTATIONAL_MODEL_ALREADY_RUNNING_ERROR = "Computational Model can't start process because there is already a running process";
		final String EXECUTOR_NOT_FOUND_ERROR = "Executor not found";
		final String EXTRACTOR_NOT_FOUND_ERROR = "Extractor not found";
		final String GET_DRIVE_UPLOAD_FILE_ERROR = "Error while uploading file for drive folder";
		final String GET_DRIVE_DOWNLOAD_FILE_ERROR = "Error while downloading file from google drive";
		final String GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND = "Error while downloading file from google drive, file not found";
		final String GET_DRIVE_FOLDER_FILES_ERROR = "Error while getting drive folder files data";
		final String GOOGLE_OAUTH2_ERROR = "Error while getting access token for google api connection";
	}
	
	public interface MSG_WARN {
		final String GOOGLE_FILE_NOT_FOUND = "File with fileId [{}], not found";
	}

}