-------------------------------------------------------------------------------------
-- Database


CREATE DATABASE "PhenoManager"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;


-------------------------------------------------------------------------------------
-- Tables


CREATE TABLE public.computational_model (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    current_version character varying(32),
    is_public_data boolean,
    type character varying(255) NOT NULL,
    id_experiment bigint
);


ALTER TABLE public.computational_model OWNER TO postgres;


CREATE TABLE public.conceptual_param (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    key character varying(80),
    id_experiment bigint
);


ALTER TABLE public.conceptual_param OWNER TO postgres;


CREATE TABLE public.execution_environment (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    access_key character varying(150),
    cluster_name character varying(150),
    type character varying(255),
    host_address text,
    image text,
    password character varying(150),
    secret_key character varying(150),
    username character varying(150),
    vpn_type character varying(255),
    id_computational_model bigint,
    tag character varying(255) NOT NULL,
    vpn_configuration_file_name character varying(255),
    vpn_configuration text
);


ALTER TABLE public.execution_environment OWNER TO postgres;


CREATE TABLE public.experiment (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    id_hypothesis bigint
);


ALTER TABLE public.experiment OWNER TO postgres;


CREATE TABLE public.extractor_metadata (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    id_model_metadata_extractor bigint,
    id_model_result_metadata bigint,
    execution_status character varying(255),
    execution_metadata_file_id character varying(255)
);


ALTER TABLE public.extractor_metadata OWNER TO postgres;


CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO postgres;


CREATE TABLE public.hypothesis (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    state character varying(255),
    ranking bigint,
    id_parent_hypothesis bigint,
    id_phenomenon bigint
);


ALTER TABLE public.hypothesis OWNER TO postgres;


CREATE TABLE public.instance_param (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    key character varying(80),
    id_computational_model bigint,
    id_conceptual_param bigint,
    value_file_name character varying(255),
    value text,
    value_file_id character varying(255),
    value_file_content_type character varying(255)
);


ALTER TABLE public.instance_param OWNER TO postgres;


CREATE TABLE public.model_executor (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    abort_command text,
    execution_command text,
    execution_status character varying(255),
    execution_url text,
    http_body text,
    http_headers text,
    http_protocol_type character varying(255),
    http_verb character varying(255),
    job_name character varying(150),
    tag character varying(80),
    use_enviroment_variables boolean,
    id_computational_model bigint,
    id_user_account_agent bigint,
    executor_file_name character varying(255),
    executor_file_id character varying(255),
    executor_file_content_type character varying(255)
);


ALTER TABLE public.model_executor OWNER TO postgres;


CREATE TABLE public.model_metadata_extractor (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    execution_command text,
    execution_status character varying(255),
    tag character varying(80),
    id_computational_model bigint,
    id_user_account_agent bigint,
    extractor_file_name character varying(255),
    extractor_file_id character varying,
    extractor_file_content_type character varying(255)
);


ALTER TABLE public.model_metadata_extractor OWNER TO postgres;


CREATE TABLE public.model_result_metadata (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    execution_finish_date timestamp without time zone,
    execution_start_date timestamp without time zone,
    id_computational_model bigint,
    id_model_executor bigint,
    id_user_account_agent bigint,
    execution_status character varying(255),
    id_execution_environment bigint,
    execution_metadata_file_id character varying(255),
    abort_metadata_file_id character varying(255),
    execution_output text,
);


ALTER TABLE public.model_result_metadata OWNER TO postgres;


CREATE TABLE public.permission (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    role character varying(255),
    id_computational_model bigint,
    id_experiment bigint,
    id_hypothesis bigint,
    id_phenomenon bigint,
    id_project bigint,
    id_user bigint,
    id_team bigint
);


ALTER TABLE public.permission OWNER TO postgres;


CREATE TABLE public.phase (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    name character varying(80),
    id_experiment bigint
);


ALTER TABLE public.phase OWNER TO postgres;


CREATE TABLE public.phenomenon (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    research_domain character varying(255) NOT NULL,
    id_project bigint
);


ALTER TABLE public.phenomenon OWNER TO postgres;


CREATE TABLE public.project (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80)
);


ALTER TABLE public.project OWNER TO postgres;


CREATE TABLE public.team (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    name character varying(80)
);


ALTER TABLE public.team OWNER TO postgres;


CREATE TABLE public.team_user_account (
    id_user_account bigint NOT NULL,
    id_team bigint NOT NULL
);


ALTER TABLE public.team_user_account OWNER TO postgres;


CREATE TABLE public.user_account (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    email character varying(150),
    institution_name character varying(100),
    name character varying(80),
    password text,
    role character varying(255),
    profile_image_file_id character varying(255)
);


ALTER TABLE public.user_account OWNER TO postgres;


CREATE TABLE public.validation_item (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    expected_value_description text,
    validated boolean,
    id_experiment bigint,
    validation_evidence_file_name character varying(255),
    validation_evidence_file_id character varying(255),
    validation_evidence_file_content_type character varying(255)
);


ALTER TABLE public.validation_item OWNER TO postgres;


CREATE TABLE public.virtual_machine_config (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    disk_space double precision,
    financial_cost double precision,
    gflops double precision,
    number_of_cores integer,
    platform character varying(150),
    ram integer,
    type character varying(150),
    id_execution_environment bigint
);


ALTER TABLE public.virtual_machine_config OWNER TO postgres;


ALTER TABLE ONLY public.computational_model
    ADD CONSTRAINT computational_model_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.conceptual_param
    ADD CONSTRAINT conceptual_param_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.execution_environment
    ADD CONSTRAINT execution_environment_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.experiment
    ADD CONSTRAINT experiment_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.extractor_metadata
    ADD CONSTRAINT extractor_metadata_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT hypothesis_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT instance_param_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.model_executor
    ADD CONSTRAINT model_executor_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.model_metadata_extractor
    ADD CONSTRAINT model_metadata_extractor_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.model_result_metadata
    ADD CONSTRAINT model_result_metadata_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.phase
    ADD CONSTRAINT phase_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT phenomenon_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.team
    ADD CONSTRAINT team_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT team_user_account_pkey PRIMARY KEY (id_user_account, id_team);


ALTER TABLE ONLY public.conceptual_param
    ADD CONSTRAINT uk_64pqq6quv1u0txft2nbm4sc0k UNIQUE (key);


ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT uk_eu6vl3vgrehldaccuabv0bccn UNIQUE (key);


ALTER TABLE ONLY public.user_account
    ADD CONSTRAINT uk_hl02wv5hym99ys465woijmfib UNIQUE (email);


ALTER TABLE ONLY public.team
    ADD CONSTRAINT uk_kas9w8ead0ska5n3csefp2bpp UNIQUE (name);


ALTER TABLE ONLY public.user_account
    ADD CONSTRAINT user_account_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.validation_item
    ADD CONSTRAINT validation_item_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.virtual_machine_config
    ADD CONSTRAINT virtual_machine_config_pkey PRIMARY KEY (id);


CREATE INDEX fki_ar45gpd2rxh31t1vpqetuipyzx8 ON public.model_result_metadata USING btree (id_execution_environment);


CREATE INDEX pkslug_model_executor ON public.model_executor USING hash (slug);


CREATE INDEX pkslug_model_metadata_extractor ON public.model_metadata_extractor USING btree (slug);


CREATE INDEX pkslug_model_result_metadata ON public.model_result_metadata USING hash (slug);


ALTER TABLE ONLY public.model_result_metadata
    ADD CONSTRAINT ar45gpd2rxh31t1vpqetuipyzx8 FOREIGN KEY (id_execution_environment) REFERENCES public.execution_environment(id);


ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT fk1qbqilclgjbqiuws1tnxfoliv FOREIGN KEY (id_team) REFERENCES public.user_account(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk2a87ui67ew4ctlsa5hiqlmlyv FOREIGN KEY (id_user) REFERENCES public.user_account(id);


ALTER TABLE ONLY public.model_result_metadata
    ADD CONSTRAINT fk35gpd1axm31x1vwcetjedyau6 FOREIGN KEY (id_user_account_agent) REFERENCES public.user_account(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk36f720hu4sonftlm4v4uw1grr FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT fk3s1c9y84138i1h71rayor6fs6 FOREIGN KEY (id_conceptual_param) REFERENCES public.conceptual_param(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk3yf32qom9dk7qu9msavr7irch FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk521s8t0qgt9r3b6pd0wuedpc6 FOREIGN KEY (id_project) REFERENCES public.project(id);


ALTER TABLE ONLY public.conceptual_param
    ADD CONSTRAINT fk78clum9f8qv29tof0onsrt84i FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT fk8g3a4oae5nc8rwvbe88huokir FOREIGN KEY (id_parent_hypothesis) REFERENCES public.hypothesis(id);


ALTER TABLE ONLY public.phase
    ADD CONSTRAINT fk9dtniby9d8u75grr4pek5tdr7 FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


ALTER TABLE ONLY public.extractor_metadata
    ADD CONSTRAINT fk9v2ieq21ceq62efmodsxko6h6 FOREIGN KEY (id_model_metadata_extractor) REFERENCES public.model_metadata_extractor(id);


ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT fkb0ovmcaya5a6p6ri6w41utm55 FOREIGN KEY (id_phenomenon) REFERENCES public.phenomenon(id);


ALTER TABLE ONLY public.execution_environment
    ADD CONSTRAINT fkcxq6pup0gy5ebfmnxpwn9htnb FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT fkf0jp1ap5mov0vryu564krmx5r FOREIGN KEY (id_user_account) REFERENCES public.team(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fkfr97yokitq69qpe3fhb7fw36y FOREIGN KEY (id_phenomenon) REFERENCES public.phenomenon(id);


ALTER TABLE ONLY public.model_executor
    ADD CONSTRAINT fkga74kl9oppdfjpcdfoi0h58e4 FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fkhb024i5liig6jha8r5ukntk2i FOREIGN KEY (id_hypothesis) REFERENCES public.hypothesis(id);


ALTER TABLE ONLY public.virtual_machine_config
    ADD CONSTRAINT fkhbltw3pfcc6w13nxkywvv2fc1 FOREIGN KEY (id_execution_environment) REFERENCES public.execution_environment(id);


ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT fkjqovrhyutpy44hx61cmmaklnb FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fkklrh7fd3vaf0cwobwd8hw94qt FOREIGN KEY (id_team) REFERENCES public.team(id);


ALTER TABLE ONLY public.extractor_metadata
    ADD CONSTRAINT fkljpkyrs9a1mgk5ixaoa8a2o6e FOREIGN KEY (id_model_result_metadata) REFERENCES public.model_result_metadata(id);


ALTER TABLE ONLY public.validation_item
    ADD CONSTRAINT fkm7q4a9qio3cdcoq7b55luxku3 FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


ALTER TABLE ONLY public.experiment
    ADD CONSTRAINT fknfnu1itvypq0t9xi68t3rp89f FOREIGN KEY (id_hypothesis) REFERENCES public.hypothesis(id);


ALTER TABLE ONLY public.computational_model
    ADD CONSTRAINT fknrw278qd801v8fsyvgeswseu FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fkpc4mplt6pqbmywpflxxhm51bo FOREIGN KEY (id_project) REFERENCES public.project(id);


ALTER TABLE ONLY public.model_metadata_extractor
    ADD CONSTRAINT fkr03fue7yx31jefch6n0m0x2gd FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


ALTER TABLE ONLY public.model_executor
    ADD CONSTRAINT fkrjm4vtmkxjnq469tdqkv6rt2a FOREIGN KEY (id_user_account_agent) REFERENCES public.user_account(id);


ALTER TABLE ONLY public.model_result_metadata
    ADD CONSTRAINT fkru709mtlcceeikbjry3y9iqjs FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);



ALTER TABLE ONLY public.model_metadata_extractor
    ADD CONSTRAINT fks16r5rpcrovmxmdiv52fnibeb FOREIGN KEY (id_user_account_agent) REFERENCES public.user_account(id);


ALTER TABLE ONLY public.model_result_metadata
    ADD CONSTRAINT fkt9rupe8wcmv89bffqh1mfp0op FOREIGN KEY (id_model_executor) REFERENCES public.model_executor(id);
