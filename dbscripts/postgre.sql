

--- staatuse_ajalugu ---
create table staatuse_ajalugu_backup as (select * from staatuse_ajalugu);

delete from staatuse_ajalugu;

alter table staatuse_ajalugu_backup add column meta_xml text;
update staatuse_ajalugu_backup set meta_xml = metaxml;
alter table staatuse_ajalugu_backup drop column metaxml;


alter table staatuse_ajalugu alter column fault_actor type text;
alter table staatuse_ajalugu alter column fault_code type text;
alter table staatuse_ajalugu alter column fault_detail type text;
alter table staatuse_ajalugu alter column fault_string type text;


alter table staatuse_ajalugu add column meta_xml text;
alter table staatuse_ajalugu alter column meta_xml type text;
alter table staatuse_ajalugu drop column metaxml;

insert into staatuse_ajalugu select * from staatuse_ajalugu_backup;

drop table staatuse_ajalugu_backup;



--- vastuvotja ---
create table vastuvotja_backup as (select * from vastuvotja);

delete from vastuvotja;

alter table vastuvotja_backup add column meta_xml text;
update vastuvotja_backup set meta_xml = metaxml;
alter table vastuvotja_backup drop column metaxml;


alter table vastuvotja alter column fault_actor type text;
alter table vastuvotja alter column fault_code type text;
alter table vastuvotja alter column fault_detail type text;
alter table vastuvotja alter column fault_string type text;

alter table vastuvotja add column meta_xml text;
alter table vastuvotja alter column meta_xml type text;
alter table vastuvotja drop column metaxml;


insert into vastuvotja select * from vastuvotja_backup;

drop table vastuvotja_backup;



--- dokument ---
create table dokument_backup as (select * from dokument);

delete from dokument;

alter table dokument alter column sisu type text;

insert into dokument select * from dokument_backup;

drop table dokument_backup;

