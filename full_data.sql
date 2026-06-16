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
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.customers (id, is_default, customer_name, telegram_id) FROM stdin;
2	f	kha	\N
1	f	thy	655254730
3	f	Phea	123456789
4	f	hin	1374609917
\.


--
-- Data for Name: customer_addresses; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.customer_addresses (customer_id, address) FROM stdin;
1	43Eo, Ung Pokun (St. 109), Phnom Penh 12251, Cambodia
2	43Eo, Ung Pokun (St. 109), Phnom Penh, Cambodia
4	HW77+VJ3, St 164, Phnom Penh, Cambodia
3	61, Ung Pokun (St. 109), Phnom Penh, Cambodia
3	No 43, 109 Ung Pokun (St. 109), Phnom Penh, កម្ពុជា
\.


--
-- Data for Name: customer_phones; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.customer_phones (customer_id, phone) FROM stdin;
2	+85592444848
4	+85592586688
3	+85511333444
3	+85512444555
2	+85512575152
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: Vuthy
--

COPY public.users (id, password, phone_number, user_name) FROM stdin;
1	$2a$10$jql7nWwFf72dx4vaHdKeseeU93PMJxjMSAm1ATDjL0oA8DC7L.CBa	092444848	sokha
\.


--
-- Name: customers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: Vuthy
--

SELECT pg_catalog.setval('public.customers_id_seq', 4, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: Vuthy
--

SELECT pg_catalog.setval('public.users_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

