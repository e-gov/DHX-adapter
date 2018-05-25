USE [????? db name]
GO

/****** Object:  Table [dbo].[Demo]    Script Date: 23.05.2018 9:55.34 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


--- staatuse_ajalugu ---
select * into [dbo].[staatuse_ajalugu_backup] from [dbo].[staatuse_ajalugu]

delete from [dbo].[staatuse_ajalugu]


alter table [dbo].[staatuse_ajalugu_backup] add [meta_xml] varchar(max)
update [dbo].[staatuse_ajalugu_backup] set [meta_xml] = [metaxml]
alter table [dbo].[staatuse_ajalugu_backup] drop column [metaxml]


alter table [dbo].[staatuse_ajalugu] alter column [fault_actor] varchar(max)
alter table [dbo].[staatuse_ajalugu] alter column [fault_code] varchar(max)
alter table [dbo].[staatuse_ajalugu] alter column [fault_detail] varchar(max)
alter table [dbo].[staatuse_ajalugu] alter column [fault_string] varchar(max)

alter table [dbo].[staatuse_ajalugu] add [meta_xml] varchar(max);
alter table [dbo].[staatuse_ajalugu] alter column [meta_xml] varchar(max)
alter table [dbo].[staatuse_ajalugu] drop column [metaxml]

SET IDENTITY_INSERT [dbo].[staatuse_ajalugu] ON
GO

insert into [dbo].[staatuse_ajalugu] (
	[staatuse_ajalugu_id],
    [fault_actor],
    [fault_code],
    [fault_detail],
    [fault_string],
    [vastuvotja_staatus_id],
    [staatuse_muutmise_aeg],
    [staatus_id],
    [vastuvotja_id],
    [meta_xml]) select * from [dbo].[staatuse_ajalugu_backup]

drop table [dbo].[staatuse_ajalugu_backup]




--- vastuvotja ---
select * into [dbo].[vastuvotja_backup] from [dbo].[vastuvotja]

delete from [dbo].[vastuvotja];

alter table [dbo].[vastuvotja_backup] add [meta_xml] varchar(max)
update [dbo].[vastuvotja_backup] set [meta_xml] = [metaxml]
alter table [dbo].[vastuvotja_backup] drop column [metaxml]


alter table [dbo].[vastuvotja] alter column [fault_actor] varchar(max);
alter table [dbo].[vastuvotja] alter column [fault_code] varchar(max);
alter table [dbo].[vastuvotja] alter column [fault_detail] varchar(max);
alter table [dbo].[vastuvotja] alter column [fault_string] varchar(max);

alter table [dbo].[vastuvotja] add [meta_xml] varchar(max);
alter table [dbo].[vastuvotja] alter column [meta_xml] varchar(max)
alter table [dbo].[vastuvotja] drop column [metaxml]

SET IDENTITY_INSERT [dbo].[vastuvotja] ON
GO

insert into [dbo].[vastuvotja] (
    [vastuvotja_id],
    [datecreated],
    [datemodified],
    [version],
    [dhx_external_consignment_id],
    [dhx_external_receipt_id],
    [dhx_internal_consignment_id],
    [dok_id_teises_serveris],
    [fault_actor],
    [fault_code],
    [fault_detail],
    [fault_string],
    [last_send_date],
    [outgoing],
    [isikukood],
    [vastuvotja_staatus_id],
    [saatmise_lopp],
    [saatmise_algus],
    [saatmisviis_id],
    [staatuse_muutmise_aeg],
    [staatus_id],
    [allyksus],
    [asutus_id],
    [transport_id],
    [meta_xml]) select * from [dbo].[vastuvotja_backup]

drop table [dbo].[vastuvotja_backup];



--- dokument ---
select * into [dbo].[dokument_backup] from [dbo].[dokument]

delete from [dbo].[dokument];

alter table [dbo].[dokument] alter column [sisu] varchar(max);


SET IDENTITY_INSERT [dbo].[dokument] ON
GO

insert into [dbo].[dokument] (
    [dokument_id],
    [datecreated],
    [datemodified],
    [version],
    [kapsli_versioon],
    [versioon],
    [sisu],
    [guid],
    [outgoingdocument],
    [suurus],
    [sailitustahtaeg],
    [kaust_id],
    [asutus_id]) select * from [dbo].[dokument_backup]
	
drop table [dbo].[dokument_backup];


