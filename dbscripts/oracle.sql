

--- staatuse_ajalugu ---
alter table STAATUSE_AJALUGU add ( FAULT_ACTOR_2 clob, FAULT_CODE_2 clob, FAULT_DETAIL_2 clob, FAULT_STRING_2 clob);

update STAATUSE_AJALUGU set FAULT_ACTOR_2 = FAULT_ACTOR;  -- convert varchar2 to CLOB
update STAATUSE_AJALUGU set FAULT_CODE_2 = FAULT_CODE;  -- convert varchar2 to CLOB
update STAATUSE_AJALUGU set FAULT_DETAIL_2 = FAULT_DETAIL;  -- convert varchar2 to CLOB
update STAATUSE_AJALUGU set FAULT_STRING_2 = FAULT_STRING;  -- convert varchar2 to CLOB

alter table STAATUSE_AJALUGU drop column FAULT_ACTOR;
alter table STAATUSE_AJALUGU drop column FAULT_CODE;
alter table STAATUSE_AJALUGU drop column FAULT_DETAIL;
alter table STAATUSE_AJALUGU drop column FAULT_STRING;

alter table STAATUSE_AJALUGU rename column FAULT_ACTOR_2 to FAULT_ACTOR;
alter table STAATUSE_AJALUGU rename column FAULT_CODE_2 to FAULT_CODE;
alter table STAATUSE_AJALUGU rename column FAULT_DETAIL_2 to FAULT_DETAIL;
alter table STAATUSE_AJALUGU rename column FAULT_STRING_2 to FAULT_STRING;


--- vastuvotja ---
alter table VASTUVOTJA add ( FAULT_ACTOR_2 clob, FAULT_CODE_2 clob, FAULT_DETAIL_2 clob, FAULT_STRING_2 clob);

update VASTUVOTJA set FAULT_ACTOR_2 = FAULT_ACTOR;  -- convert varchar2 to CLOB
update VASTUVOTJA set FAULT_CODE_2 = FAULT_CODE;  -- convert varchar2 to CLOB
update VASTUVOTJA set FAULT_DETAIL_2 = FAULT_DETAIL;  -- convert varchar2 to CLOB
update VASTUVOTJA set FAULT_STRING_2 = FAULT_STRING;  -- convert varchar2 to CLOB

alter table VASTUVOTJA drop column FAULT_ACTOR;
alter table VASTUVOTJA drop column FAULT_CODE;
alter table VASTUVOTJA drop column FAULT_DETAIL;
alter table VASTUVOTJA drop column FAULT_STRING;

alter table VASTUVOTJA rename column FAULT_ACTOR_2 to FAULT_ACTOR;
alter table VASTUVOTJA rename column FAULT_CODE_2 to FAULT_CODE;
alter table VASTUVOTJA rename column FAULT_DETAIL_2 to FAULT_DETAIL;
alter table VASTUVOTJA rename column FAULT_STRING_2 to FAULT_STRING;



--- dokument ---
alter table dokument add ( sisu_2 clob);

update dokument set sisu_2 = sisu;  -- convert varchar2 to CLOB


alter table dokument drop column sisu;


alter table dokument rename column sisu_2 to sisu;


