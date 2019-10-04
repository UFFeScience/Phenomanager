package com.uff.model.invoker;

public interface Constants {
	
	final Integer DEFAULT_LIMIT = 20;
	final String TMP_DIR = "java.io.tmpdir";
	final String PATH_SEPARATOR = "/";
	final Integer MAX_LOG_LINES = 10000;
	
	public interface BASH {
		final String DETECT_OS_BASH = "echo \"$OSTYPE\"";
	}
	
	public interface MULTITHREAD {
		final String CORE_POOL_SIZE = "${multithread.core-pool-size}";
		final String MAX_POOL_SIZE = "${multithread.max-pool-size}";
		final String QUEUE_CAPACITY = "${multithread.queue-capacity}";
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
	
	public interface MSG_ERROR {
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