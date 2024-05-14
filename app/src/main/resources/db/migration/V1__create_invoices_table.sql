CREATE TABLE IF NOT EXISTS public.invoices (
id              bigserial               NOT NULL,
date            date                    NOT NULL,
number          character varying(50)   NOT NULL,
PRIMARY KEY (id)
);