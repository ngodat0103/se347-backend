insert into modules (name) values  ('account-svc');
insert into modules (name) values  ('auth-svc');

insert into operations (id,name) values (1,'CREATE'),
(2,'READ'),
(3,'UPDATE'),
(4,'LIST'),
(5,'DELETE');


insert into permissions (id,name) values (1 ,'SELF_MANAGED_USER_PROFILE');

INSERT INTO public.permissions_operations (operations_id, permission_id) VALUES (1, 1);
INSERT INTO public.permissions_operations (operations_id, permission_id) VALUES (2, 1);
INSERT INTO public.permissions_operations (operations_id, permission_id) VALUES (3, 1);
INSERT INTO public.permissions_operations (operations_id, permission_id) VALUES (4, 1);
INSERT INTO public.permissions_operations (operations_id, permission_id) VALUES (5, 1);


insert into roles(id,name) values (1,'CUSTOMER')
