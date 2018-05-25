

--- staatuse_ajalugu ---
create table staatuse_ajalugu_backup as (select * from staatuse_ajalugu);

delete from staatuse_ajalugu;

alter table staatuse_ajalugu_backup add column meta_xml clob;
update table staatuse_ajalugu_backup set meta_xml = metaxml;
alter table staatuse_ajalugu_backup drop column metaxml;

alter table staatuse_ajalugu add column meta_xml clob;
alter table staatuse_ajalugu drop column metaxml;

alter table staatuse_ajalugu modify(
    fault_actor clob,
    fault_code clob,
    fault_detail clob,
    fault_string clob,
    meta_xml clob
);

insert into staatuse_ajalugu select * from staatuse_ajalugu_backup;

drop table staatuse_ajalugu_backup;



--- vastuvotja ---
create table vastuvotja_backup as (select * from vastuvotja);

delete from vastuvotja;

alter table vastuvotja_backup add column meta_xml clob;
update vastuvotja_backup set meta_xml = metaxml;
alter table vastuvotja_backup drop column metaxml;

alter table vastuvotja add column meta_xml clob;
alter table vastuvotja drop column metaxml;

alter table vastuvotja modify(
    fault_actor clob,
    fault_code clob,
    fault_detail clob,
    fault_string clob,
	meta_xml clob
);

insert into vastuvotja select * from vastuvotja_backup;

drop table vastuvotja_backup;



--- dokument ---
create table dokument_backup as (select * from dokument);

delete from dokument;

alter table dokument modify(
    sisu clob
);

insert into dokument select * from dokument_backup;

drop table dokument_backup;

