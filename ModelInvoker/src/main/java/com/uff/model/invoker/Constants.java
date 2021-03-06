package com.uff.model.invoker;

public interface Constants {
	
	final Integer DEFAULT_LIMIT = 20;
	final String TMP_DIR = "java.io.tmpdir";
	final String USER_HOME_DIR = System.getProperty("user.dir");
	final String REMOTE_MOUNT_POINT = ".";
	final String PATH_SEPARATOR = "/";
	final Integer MAX_LOG_LINES = 10000;
	final String PROFILE_PROPERTY = "${spring.profiles.active:default}";
	final String DEFAULT_PROFILE = "default";
	final String CHECK_IP_FIRST_OPTION = "http://checkip.amazonaws.com/";
	final String CHECK_IP_SECOND_OPTION = "https://ipv4.icanhazip.com/";
	final String LOCALHOST = "localhost";
	final String SYSTEM_HOST_ADDRESS_NAME = "localhost:9501";
	final String INVOKER_STRATEGY_SUFFIX = "%sInvokerStrategy";

	public interface CRON {
		final String ENABLE = "${cron.enable}";
		final String PERIODICITY = "${cron.periodicity}";
		final String ZONE = "${cron.zone}";
	}
	
	public interface PROVIDER {
		public interface CLOUD {
			final Integer START_VM_WAIT_TIME = 30000;
			final String IP_PROTOCOL = "tcp";
			final String KEY_PAIR_EXTENSION = ".pem";
			final String KEY_PREFIX = "Key-";
			final String SECURITY_GROUP_PREFIX = "SG-";
			final String IP_RANGE_PERMISSION = "0.0.0.0/0";
			final String GROUP_NAME_LABEL = "group-name";
			final String KEY_NAME_LABEL = "key-name";
			final String TAG_FILTER_LABEL = "tag:";
			final String CLUSTER_HEADER_PREFIX = "PM-";
			final String RUNNING_STATE = "running";
			final String NODE_TYPE_LABEL = "NodeType";
			final String INSTANCE_STATE_NAME_LABEL = "instance-state-name";
			final String CLUSTER_LABEL_NAME = "Name";
		}
		
		public interface CLUSTER {
			final String STATUS_REASON_COLUMN = "ST";
			final String SCRATCH_DIRECTORY = "/scratch";
			final String SCRATCH_SCRIPT_COMMAND = "sbatch ";
			final String SCRATCH_CHECK_STATUS_COMMAND = "squeue -n ";
			final String SCRATCH_STOP_COMMAND = "scancel -n ";
			final String SCRATCH_SCRIPT_SUFFIX = ".srm";
			final String SCRATCH_PREFIX = "scratch-";
		}

		public interface SSH {
			final Long WAIT_TIME = 1000l;
			final Integer DEFAULT_PORT = 22;
		}
		
		public interface VPN {
			final String CONFIG_FILE_PREFIX = "vpn-config-";
			final String CONFIG_FILE_SUFFIX = ".conf";
			final String VPN_COMMAND = "sudo openvpn ";
			final String VPNC_COMMAND = "sudo vpnc ";
		}
	}
	
	public interface RABBIT_MQ {
		final String MODEL_EXECUTOR_QUEUE = "modelExecutor";
		final String MODEL_KILLER_QUEUE = "modelKiller";
		final String CONCURRENT_CONSUMERS = "${rabbitmq.queue.concurrent.default}";
		final String MAX_CONCURRENT_CONSUMERS = "${rabbitmq.queue.concurrent.max}";
	}
	
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
		final String ERROR_GET_LOCAL_IP = "Error while obtaining local IP addresses";
		final String ERROR_GET_EXTERNAL_IP = "Error trying to get external IP from [{}]";
		final String INVALID_ENVIRONMENT_WORKSPACE_ADDRESS = "Invalid Host Address for local workspace [{}]";
	}
	
	public interface MSG_WARN {
		final String GOOGLE_FILE_NOT_FOUND = "File with fileId [{}], not found";
		final String INVALID_INCOMING_IP = "Null or empty incoming IP";
	}
	
}