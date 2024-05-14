ALTER TABLE public.invoices
    ADD COLUMN buyer bigint NOT NULL;

ALTER TABLE public.invoices
    ADD COLUMN seller bigint NOT NULL;

ALTER TABLE public.invoices
    ADD CONSTRAINT buyer_fk FOREIGN KEY (buyer)
        REFERENCES public.company (id)
        ON DELETE CASCADE;

ALTER TABLE public.invoices
    ADD CONSTRAINT seller_fk FOREIGN KEY (seller)
        REFERENCES public.company (id)
        ON DELETE CASCADE;