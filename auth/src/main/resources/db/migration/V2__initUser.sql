CREATE SEQUENCE IF NOT EXISTS"public"."users_id_seq"
    INCREMENT 1
    MINVALUE  1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
CREATE TABLE "public"."users" (
                                  "id" int8 NOT NULL DEFAULT nextval('users_id_seq'::regclass),
                                  "email" varchar(255) COLLATE "pg_catalog"."default",
                                  "is_enabled" bool NOT NULL,
                                  "is_lock" bool NOT NULL,
                                  "login" varchar(255) COLLATE "pg_catalog"."default",
                                  "password" varchar(255) COLLATE "pg_catalog"."default",
                                  "role" varchar(255) COLLATE "pg_catalog"."default",
                                  "uid" varchar(255) COLLATE "pg_catalog"."default"
)
;