CREATE DATABASE "PhenoManager"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;


--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.19
-- Dumped by pg_dump version 9.5.19

-- Started on 2019-10-03 22:13:50 -03

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12395)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2324 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 181 (class 1259 OID 29256)
-- Name: computational_model; Type: TABLE; Schema: public; Owner: postgres
--

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
    id_experiment bigint NOT NULL
);


ALTER TABLE public.computational_model OWNER TO postgres;

--
-- TOC entry 182 (class 1259 OID 29264)
-- Name: conceptual_param; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conceptual_param (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    key character varying(80),
    id_experiment bigint NOT NULL
);


ALTER TABLE public.conceptual_param OWNER TO postgres;

--
-- TOC entry 183 (class 1259 OID 29272)
-- Name: environment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.environment (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    access_key character varying(150),
    cluster_name character varying(150),
    host_address text,
    image text,
    password character varying(150),
    secret_key character varying(150),
    tag character varying(80) NOT NULL,
    type character varying(255),
    username character varying(150),
    vpn_configuration text,
    vpn_type character varying(255),
    id_computational_model bigint NOT NULL
);


ALTER TABLE public.environment OWNER TO postgres;

--
-- TOC entry 184 (class 1259 OID 29280)
-- Name: experiment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.experiment (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    id_hypothesis bigint NOT NULL
);


ALTER TABLE public.experiment OWNER TO postgres;

--
-- TOC entry 185 (class 1259 OID 29288)
-- Name: extractor_execution; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.extractor_execution (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    execution_metadata_file_id character varying(255),
    status character varying(255),
    id_extractor bigint NOT NULL,
    id_execution bigint NOT NULL
);


ALTER TABLE public.extractor_execution OWNER TO postgres;

--
-- TOC entry 186 (class 1259 OID 29296)
-- Name: hibernate_sequences; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hibernate_sequences (
    sequence_name character varying(255) NOT NULL,
    sequence_next_hi_value bigint
);


ALTER TABLE public.hibernate_sequences OWNER TO postgres;

--
-- TOC entry 187 (class 1259 OID 29301)
-- Name: hypothesis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hypothesis (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    name character varying(80),
    ranking bigint,
    state character varying(255),
    id_parent_hypothesis bigint,
    id_phenomenon bigint NOT NULL
);


ALTER TABLE public.hypothesis OWNER TO postgres;

--
-- TOC entry 188 (class 1259 OID 29309)
-- Name: instance_param; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.instance_param (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    description text,
    key character varying(80),
    value text,
    value_file_content_type character varying(255),
    value_file_id character varying(255),
    value_file_name character varying(255),
    id_computational_model bigint NOT NULL,
    id_conceptual_param bigint NOT NULL
);


ALTER TABLE public.instance_param OWNER TO postgres;

--
-- TOC entry 189 (class 1259 OID 29317)
-- Name: executor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.executor (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    abortion_command text NOT NULL,
    execution_command text NOT NULL,
    execution_url text,
    file_content_type character varying(255),
    file_id character varying(255),
    file_name character varying(255),
    http_body text,
    http_headers text,
    http_verb character varying(255),
    job_name character varying(150),
    tag character varying(80) NOT NULL,
    use_enviroment_variables boolean,
    web_service_type character varying(255),
    id_computational_model bigint NOT NULL
);


ALTER TABLE public.executor OWNER TO postgres;

--
-- TOC entry 190 (class 1259 OID 29325)
-- Name: extractor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.extractor (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    execution_command text NOT NULL,
    abortion_command text NOT NULL,
    file_content_type character varying(255),
    file_id character varying(255),
    file_name character varying(255),
    tag character varying(80) NOT NULL,
    id_computational_model bigint NOT NULL
);


ALTER TABLE public.extractor OWNER TO postgres;

--
-- TOC entry 191 (class 1259 OID 29333)
-- Name: execution; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.execution (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    abortion_metadata_file_id character varying(255),
    finish_date timestamp without time zone,
    execution_metadata_file_id character varying(255),
    output text,
    start_date timestamp without time zone,
    status character varying(255),
    executor_status character varying(255),
    has_abortion_requested boolean,
    upload_metadata boolean NOT NULL,
    id_computational_model bigint NOT NULL,
    id_environment bigint NOT NULL,
    id_executor bigint,
    id_user_account_agent bigint
);


ALTER TABLE public.execution OWNER TO postgres;

--
-- TOC entry 192 (class 1259 OID 29341)
-- Name: permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    role character varying(255) NOT NULL,
    id_computational_model bigint,
    id_experiment bigint,
    id_hypothesis bigint,
    id_phenomenon bigint,
    id_project bigint,
    id_team bigint,
    id_user bigint
);


ALTER TABLE public.permission OWNER TO postgres;

--
-- TOC entry 193 (class 1259 OID 29346)
-- Name: phase; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.phase (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    name character varying(80),
    id_experiment bigint NOT NULL
);


ALTER TABLE public.phase OWNER TO postgres;

--
-- TOC entry 194 (class 1259 OID 29351)
-- Name: phenomenon; Type: TABLE; Schema: public; Owner: postgres
--

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
    id_project bigint NOT NULL
);


ALTER TABLE public.phenomenon OWNER TO postgres;

--
-- TOC entry 195 (class 1259 OID 29359)
-- Name: project; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- TOC entry 196 (class 1259 OID 29367)
-- Name: team; Type: TABLE; Schema: public; Owner: postgres
--

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

--
-- TOC entry 197 (class 1259 OID 29372)
-- Name: team_user_account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team_user_account (
    id_user_account bigint NOT NULL,
    id_team bigint NOT NULL
);


ALTER TABLE public.team_user_account OWNER TO postgres;

--
-- TOC entry 198 (class 1259 OID 29377)
-- Name: user_account; Type: TABLE; Schema: public; Owner: postgres
--

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
    profile_image_file_id character varying(255),
    role character varying(255)
);


ALTER TABLE public.user_account OWNER TO postgres;

--
-- TOC entry 199 (class 1259 OID 29385)
-- Name: validation_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.validation_item (
    id bigint NOT NULL,
    active boolean NOT NULL,
    delete_date timestamp without time zone,
    creation_date timestamp without time zone NOT NULL,
    slug character varying(32) NOT NULL,
    update_date timestamp without time zone NOT NULL,
    expected_value_description text,
    validated boolean,
    validation_evidence_file_content_type character varying(255),
    validation_evidence_file_id character varying(255),
    validation_evidence_file_name character varying(255),
    id_experiment bigint NOT NULL
);


ALTER TABLE public.validation_item OWNER TO postgres;

--
-- TOC entry 200 (class 1259 OID 29393)
-- Name: virtual_machine; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.virtual_machine (
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
    id_environment bigint NOT NULL
);


ALTER TABLE public.virtual_machine OWNER TO postgres;

--
-- TOC entry 2296 (class 0 OID 29256)
-- Dependencies: 181
-- Data for Name: computational_model; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- TOC entry 2108 (class 2606 OID 29263)
-- Name: computational_model_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.computational_model
    ADD CONSTRAINT computational_model_pkey PRIMARY KEY (id);


--
-- TOC entry 2110 (class 2606 OID 29271)
-- Name: conceptual_param_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conceptual_param
    ADD CONSTRAINT conceptual_param_pkey PRIMARY KEY (id);


--
-- TOC entry 2112 (class 2606 OID 29279)
-- Name: environment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.environment
    ADD CONSTRAINT environment_pkey PRIMARY KEY (id);


--
-- TOC entry 2114 (class 2606 OID 29287)
-- Name: experiment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.experiment
    ADD CONSTRAINT experiment_pkey PRIMARY KEY (id);


--
-- TOC entry 2116 (class 2606 OID 29295)
-- Name: extractor_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.extractor_execution
    ADD CONSTRAINT extractor_execution_pkey PRIMARY KEY (id);


--
-- TOC entry 2118 (class 2606 OID 29300)
-- Name: hibernate_sequences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hibernate_sequences
    ADD CONSTRAINT hibernate_sequences_pkey PRIMARY KEY (sequence_name);


--
-- TOC entry 2120 (class 2606 OID 29308)
-- Name: hypothesis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT hypothesis_pkey PRIMARY KEY (id);


--
-- TOC entry 2122 (class 2606 OID 29316)
-- Name: instance_param_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT instance_param_pkey PRIMARY KEY (id);


--
-- TOC entry 2124 (class 2606 OID 29324)
-- Name: executor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.executor
    ADD CONSTRAINT executor_pkey PRIMARY KEY (id);


--
-- TOC entry 2126 (class 2606 OID 29332)
-- Name: extractor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.extractor
    ADD CONSTRAINT extractor_pkey PRIMARY KEY (id);


--
-- TOC entry 2128 (class 2606 OID 29340)
-- Name: execution_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.execution
    ADD CONSTRAINT execution_pkey PRIMARY KEY (id);


--
-- TOC entry 2130 (class 2606 OID 29345)
-- Name: permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


--
-- TOC entry 2132 (class 2606 OID 29350)
-- Name: phase_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phase
    ADD CONSTRAINT phase_pkey PRIMARY KEY (id);


--
-- TOC entry 2134 (class 2606 OID 29358)
-- Name: phenomenon_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT phenomenon_pkey PRIMARY KEY (id);


--
-- TOC entry 2136 (class 2606 OID 29366)
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- TOC entry 2138 (class 2606 OID 29371)
-- Name: team_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT team_pkey PRIMARY KEY (id);


--
-- TOC entry 2142 (class 2606 OID 29376)
-- Name: team_user_account_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT team_user_account_pkey PRIMARY KEY (id_user_account, id_team);


--
-- TOC entry 2140 (class 2606 OID 29399)
-- Name: uk_g2l9qqsoeuynt4r5ofdt1x2td; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT uk_g2l9qqsoeuynt4r5ofdt1x2td UNIQUE (name);


--
-- TOC entry 2144 (class 2606 OID 29401)
-- Name: uk_hl02wv5hym99ys465woijmfib; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_account
    ADD CONSTRAINT uk_hl02wv5hym99ys465woijmfib UNIQUE (email);


--
-- TOC entry 2146 (class 2606 OID 29384)
-- Name: user_account_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_account
    ADD CONSTRAINT user_account_pkey PRIMARY KEY (id);


--
-- TOC entry 2148 (class 2606 OID 29392)
-- Name: validation_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.validation_item
    ADD CONSTRAINT validation_item_pkey PRIMARY KEY (id);


--
-- TOC entry 2150 (class 2606 OID 29397)
-- Name: virtual_machine_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.virtual_machine
    ADD CONSTRAINT virtual_machine_pkey PRIMARY KEY (id);


--
-- TOC entry 2175 (class 2606 OID 29522)
-- Name: fk2a87ui67ew4ctlsa5hiqlmlyv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk2a87ui67ew4ctlsa5hiqlmlyv FOREIGN KEY (id_user) REFERENCES public.user_account(id);


--
-- TOC entry 2168 (class 2606 OID 29487)
-- Name: fk35gpd1axm31x1vwcetjedyau6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.execution
    ADD CONSTRAINT fk35gpd1axm31x1vwcetjedyau6 FOREIGN KEY (id_user_account_agent) REFERENCES public.user_account(id);


--
-- TOC entry 2169 (class 2606 OID 29492)
-- Name: fk36f720hu4sonftlm4v4uw1grr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk36f720hu4sonftlm4v4uw1grr FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2160 (class 2606 OID 29447)
-- Name: fk3s1c9y84138i1h71rayor6fs6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT fk3s1c9y84138i1h71rayor6fs6 FOREIGN KEY (id_conceptual_param) REFERENCES public.conceptual_param(id);


--
-- TOC entry 2170 (class 2606 OID 29497)
-- Name: fk3yf32qom9dk7qu9msavr7irch; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk3yf32qom9dk7qu9msavr7irch FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


--
-- TOC entry 2173 (class 2606 OID 29512)
-- Name: fk521s8t0qgt9r3b6pd0wuedpc6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk521s8t0qgt9r3b6pd0wuedpc6 FOREIGN KEY (id_project) REFERENCES public.project(id);


--
-- TOC entry 2152 (class 2606 OID 29407)
-- Name: fk78clum9f8qv29tof0onsrt84i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conceptual_param
    ADD CONSTRAINT fk78clum9f8qv29tof0onsrt84i FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


--
-- TOC entry 2166 (class 2606 OID 29477)
-- Name: fk8fhm6fuajjy2heor63oyaqvmp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.execution
    ADD CONSTRAINT fk8fhm6fuajjy2heor63oyaqvmp FOREIGN KEY (id_environment) REFERENCES public.environment(id);


--
-- TOC entry 2157 (class 2606 OID 29432)
-- Name: fk8g3a4oae5nc8rwvbe88huokir; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT fk8g3a4oae5nc8rwvbe88huokir FOREIGN KEY (id_parent_hypothesis) REFERENCES public.hypothesis(id);


--
-- TOC entry 2176 (class 2606 OID 29527)
-- Name: fk9dtniby9d8u75grr4pek5tdr7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phase
    ADD CONSTRAINT fk9dtniby9d8u75grr4pek5tdr7 FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


--
-- TOC entry 2174 (class 2606 OID 29517)
-- Name: fk9jpwwlvqp8hht3lqfb59n3fd6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fk9jpwwlvqp8hht3lqfb59n3fd6 FOREIGN KEY (id_team) REFERENCES public.team(id);


--
-- TOC entry 2155 (class 2606 OID 29422)
-- Name: fk9v2ieq21ceq62efmodsxko6h6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.extractor_execution
    ADD CONSTRAINT fk9v2ieq21ceq62efmodsxko6h6 FOREIGN KEY (id_extractor) REFERENCES public.extractor(id);


--
-- TOC entry 2158 (class 2606 OID 29437)
-- Name: fkb0ovmcaya5a6p6ri6w41utm55; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hypothesis
    ADD CONSTRAINT fkb0ovmcaya5a6p6ri6w41utm55 FOREIGN KEY (id_phenomenon) REFERENCES public.phenomenon(id);


--
-- TOC entry 2178 (class 2606 OID 29537)
-- Name: fkc5h06e3gafn5npnq7099gf9ah; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT fkc5h06e3gafn5npnq7099gf9ah FOREIGN KEY (id_team) REFERENCES public.user_account(id);


--
-- TOC entry 2153 (class 2606 OID 29412)
-- Name: fkcxq6pup0gy5ebfmnxpwn9htnb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.environment
    ADD CONSTRAINT fkcxq6pup0gy5ebfmnxpwn9htnb FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2172 (class 2606 OID 29507)
-- Name: fkfr97yokitq69qpe3fhb7fw36y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fkfr97yokitq69qpe3fhb7fw36y FOREIGN KEY (id_phenomenon) REFERENCES public.phenomenon(id);


--
-- TOC entry 2161 (class 2606 OID 29452)
-- Name: fkga74kl9oppdfjpcdfoi0h58e4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.executor
    ADD CONSTRAINT fkga74kl9oppdfjpcdfoi0h58e4 FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2171 (class 2606 OID 29502)
-- Name: fkhb024i5liig6jha8r5ukntk2i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT fkhb024i5liig6jha8r5ukntk2i FOREIGN KEY (id_hypothesis) REFERENCES public.hypothesis(id);


--
-- TOC entry 2181 (class 2606 OID 29552)
-- Name: fkhbltw3pfcc6w13nxkywvv2fc1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.virtual_machine
    ADD CONSTRAINT fkhbltw3pfcc6w13nxkywvv2fc1 FOREIGN KEY (id_environment) REFERENCES public.environment(id);


--
-- TOC entry 2179 (class 2606 OID 29542)
-- Name: fkj01xdjnjl57qwde8of968ctpq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_user_account
    ADD CONSTRAINT fkj01xdjnjl57qwde8of968ctpq FOREIGN KEY (id_user_account) REFERENCES public.team(id);


--
-- TOC entry 2159 (class 2606 OID 29442)
-- Name: fkjqovrhyutpy44hx61cmmaklnb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.instance_param
    ADD CONSTRAINT fkjqovrhyutpy44hx61cmmaklnb FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2156 (class 2606 OID 29427)
-- Name: fkljpkyrs9a1mgk5ixaoa8a2o6e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.extractor_execution
    ADD CONSTRAINT fkljpkyrs9a1mgk5ixaoa8a2o6e FOREIGN KEY (id_execution) REFERENCES public.execution(id);


--
-- TOC entry 2180 (class 2606 OID 29547)
-- Name: fkm7q4a9qio3cdcoq7b55luxku3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.validation_item
    ADD CONSTRAINT fkm7q4a9qio3cdcoq7b55luxku3 FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


--
-- TOC entry 2154 (class 2606 OID 29417)
-- Name: fknfnu1itvypq0t9xi68t3rp89f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.experiment
    ADD CONSTRAINT fknfnu1itvypq0t9xi68t3rp89f FOREIGN KEY (id_hypothesis) REFERENCES public.hypothesis(id);


--
-- TOC entry 2151 (class 2606 OID 29402)
-- Name: fknrw278qd801v8fsyvgeswseu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.computational_model
    ADD CONSTRAINT fknrw278qd801v8fsyvgeswseu FOREIGN KEY (id_experiment) REFERENCES public.experiment(id);


--
-- TOC entry 2177 (class 2606 OID 29532)
-- Name: fkpc4mplt6pqbmywpflxxhm51bo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fkpc4mplt6pqbmywpflxxhm51bo FOREIGN KEY (id_project) REFERENCES public.project(id);


--
-- TOC entry 2163 (class 2606 OID 29462)
-- Name: fkr03fue7yx31jefch6n0m0x2gd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.extractor
    ADD CONSTRAINT fkr03fue7yx31jefch6n0m0x2gd FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2165 (class 2606 OID 29472)
-- Name: fkru709mtlcceeikbjry3y9iqjs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.execution
    ADD CONSTRAINT fkru709mtlcceeikbjry3y9iqjs FOREIGN KEY (id_computational_model) REFERENCES public.computational_model(id);


--
-- TOC entry 2167 (class 2606 OID 29482)
-- Name: fkt9rupe8wcmv89bffqh1mfp0op; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.execution
    ADD CONSTRAINT fkt9rupe8wcmv89bffqh1mfp0op FOREIGN KEY (id_executor) REFERENCES public.executor(id);


--
-- TOC entry 2323 (class 0 OID 0)
-- Dependencies: 6
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2019-10-03 22:13:50 -03

--
-- PostgreSQL database dump complete
--

