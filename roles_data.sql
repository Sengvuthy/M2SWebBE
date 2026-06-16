--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: permission; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.permission (id, description, permission_name) FROM stdin;
1	Can create new users	USER_CREATE
2	Can view user data	USER_READ
3	Can update user information	USER_UPDATE
4	Can delete users	USER_DELETE
5	Can create new roles	ROLE_CREATE
6	Can view role data	ROLE_READ
7	Can update role information	ROLE_UPDATE
8	Can delete roles	ROLE_DELETE
9	Can create new permissions	PERMISSION_CREATE
10	Can view permission data	PERMISSION_READ
11	Can update permission information	PERMISSION_UPDATE
12	Can delete permissions	PERMISSION_DELETE
13	Can assign new user role	USER_ROLE:CREATE
14	Can delete user role	USER_ROLE:DELETE
15	Can read user role	USER_ROLE:READ
16	Can assign new role permission	ROLE_PERMISSION:CREATE
17	Can delete role permission	ROLE_PERMISSION:DELETE
18	Can read role permission	ROLE_PERMISSION:READ
19	Can read product sale summary	PRODUCT_SALE_SUMMARY:READ
20	Can create new customers	CUSTOMER_CREATE
21	Can view customer data	CUSTOMER_READ
22	Can update customer information	CUSTOMER_UPDATE
23	Can delete customers	CUSTOMER_DELETE
24	Can create new sellers	SELLER_CREATE
25	Can view seller data	SELLER_READ
26	Can update seller information	SELLER_UPDATE
27	Can delete sellers	SELLER_DELETE
28	Can create new suppliers	SUPPLIER_CREATE
29	Can view suppliers data	SUPPLIER_READ
30	Can update supplier information	SUPPLIER_UPDATE
31	Can delete suppliers	SUPPLIER_DELETE
32	Can create new categories	CATEGORY_CREATE
33	Can view category data	CATEGORY_READ
34	Can update category information	CATEGORY_UPDATE
35	Can delete categories	CATEGORY_DELETE
36	Can create new products	PRODUCT:CREATE
37	Can update product information	PRODUCT:UPDATE
38	Can delete products	PRODUCT:DELETE
39	Can view products data	PRODUCT:READ
40	Can create new product imports	PRODUCT_IMPORT:CREATE
41	Can update product import information	PRODUCT_IMPORT:UPDATE
42	Can delete product imports	PRODUCT_IMPORT:DELETE
43	Can view products import data	PRODUCT_IMPORT:READ
44	Can create new sale reports	SALE_REPORT:CREATE
45	Can update sale report information	SALE_REPORT:UPDATE
46	Can delete sale reports	SALE_REPORT:DELETE
47	Can view sale reports data	SALE_REPORT:READ
48	Can create new sales	SALE:CREATE
49	Can update sale information	SALE:UPDATE
50	Can delete sales	SALE:DELETE
51	Can view sales data	SALE:READ
52	Can create new expense reports	EXPENSE_REPORT:CREATE
53	Can update expense report information	EXPENSE_REPORT:UPDATE
54	Can delete expense reports	EXPENSE_REPORT:DELETE
55	Can view expense reports data	EXPENSE_REPORT:READ
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.roles (id, description, role_name) FROM stdin;
1	OWNER role with full access	OWNER
2	ADMIN role with full access	ADMIN
3	SELLER role can read Categories and Products	SELLER
4	MANAGER role can control all staffs	MANAGER
5	DELIVER role can read Delivery Order	DELIVER
6	CLEANER can only clean	CLEANER
\.


--
-- Data for Name: role_permission; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.role_permission (permission_id, role_id, permission_name, role_name) FROM stdin;
1	1	USER_CREATE	OWNER
2	1	USER_READ	OWNER
3	1	USER_UPDATE	OWNER
4	1	USER_DELETE	OWNER
5	1	ROLE_CREATE	OWNER
6	1	ROLE_READ	OWNER
7	1	ROLE_UPDATE	OWNER
8	1	ROLE_DELETE	OWNER
9	1	PERMISSION_CREATE	OWNER
10	1	PERMISSION_READ	OWNER
11	1	PERMISSION_UPDATE	OWNER
12	1	PERMISSION_DELETE	OWNER
13	1	USER_ROLE:CREATE	OWNER
14	1	USER_ROLE:DELETE	OWNER
15	1	USER_ROLE:READ	OWNER
16	1	ROLE_PERMISSION:CREATE	OWNER
17	1	ROLE_PERMISSION:DELETE	OWNER
18	1	ROLE_PERMISSION:READ	OWNER
19	1	PRODUCT_SALE_SUMMARY:READ	OWNER
20	1	CUSTOMER_CREATE	OWNER
21	1	CUSTOMER_READ	OWNER
22	1	CUSTOMER_UPDATE	OWNER
23	1	CUSTOMER_DELETE	OWNER
24	1	SELLER_CREATE	OWNER
25	1	SELLER_READ	OWNER
26	1	SELLER_UPDATE	OWNER
27	1	SELLER_DELETE	OWNER
28	1	SUPPLIER_CREATE	OWNER
29	1	SUPPLIER_READ	OWNER
30	1	SUPPLIER_UPDATE	OWNER
31	1	SUPPLIER_DELETE	OWNER
32	1	CATEGORY_CREATE	OWNER
33	1	CATEGORY_READ	OWNER
34	1	CATEGORY_UPDATE	OWNER
35	1	CATEGORY_DELETE	OWNER
36	1	PRODUCT:CREATE	OWNER
37	1	PRODUCT:UPDATE	OWNER
38	1	PRODUCT:DELETE	OWNER
39	1	PRODUCT:READ	OWNER
40	1	PRODUCT_IMPORT:CREATE	OWNER
41	1	PRODUCT_IMPORT:UPDATE	OWNER
42	1	PRODUCT_IMPORT:DELETE	OWNER
43	1	PRODUCT_IMPORT:READ	OWNER
44	1	SALE_REPORT:CREATE	OWNER
45	1	SALE_REPORT:UPDATE	OWNER
46	1	SALE_REPORT:DELETE	OWNER
47	1	SALE_REPORT:READ	OWNER
48	1	SALE:CREATE	OWNER
49	1	SALE:UPDATE	OWNER
50	1	SALE:DELETE	OWNER
51	1	SALE:READ	OWNER
52	1	EXPENSE_REPORT:CREATE	OWNER
53	1	EXPENSE_REPORT:UPDATE	OWNER
54	1	EXPENSE_REPORT:DELETE	OWNER
55	1	EXPENSE_REPORT:READ	OWNER
\.


--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.user_role (role_id, user_id, role_name, user_name) FROM stdin;
1	1	OWNER	sokha
2	1	ADMIN	sokha
3	1	SELLER	sokha
4	1	DELIVER	sokha
5	1	MANAGER	sokha
\.


--
-- Name: permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: Vuthy
--

SELECT pg_catalog.setval('public.permission_id_seq', 1, false);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: Vuthy
--

SELECT pg_catalog.setval('public.roles_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

