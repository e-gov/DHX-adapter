--https://github.com/e-gov/DHX-adapter/blob/master/docs/adapter-server-kasutusjuhend.md#51-vahendatavate-lisamine
--https://github.com/e-gov/DHX-adapter/blob/master/docs/adapter-server-testikontseptsioon.png
INSERT INTO asutus(
  asutus_id, date_created, date_modified, version, kapsel_versioon, dhx_asutus,
  dhl_saatmine, member_class, nimetus, own_representee, registrikood,
  representee_end, representee_start, subsystem, xroad_instance, vahendaja_asutus_id
) select nextval('hibernate_sequence'), now(), null, 0, 2.1, false,
  true, null, 'vallavalitsus A', true, '70000001',
  null, now(), null, null, (select asutus_id from asutus where registrikood = '30000001' and subsystem='DHX')
  WHERE
      NOT EXISTS (
          SELECT asutus_id FROM asutus WHERE registrikood = '70000001'
);
INSERT INTO asutus(
  asutus_id, date_created, date_modified, version, kapsel_versioon, dhx_asutus,
  dhl_saatmine, member_class, nimetus, own_representee, registrikood,
  representee_end, representee_start, subsystem, xroad_instance, vahendaja_asutus_id
) select nextval('hibernate_sequence'), now(), null, 0, 2.1, false,
  true, null, 'Muuseum B', true, '70000002',
  null, now(), null, null, (select asutus_id from asutus where registrikood = '30000001' and subsystem='DHX')
  WHERE
      NOT EXISTS (
          SELECT asutus_id FROM asutus WHERE registrikood = '70000002'
);
INSERT INTO asutus(
  asutus_id, date_created, date_modified, version, kapsel_versioon, dhx_asutus,
  dhl_saatmine, member_class, nimetus, own_representee, registrikood,
  representee_end, representee_start, subsystem, xroad_instance, vahendaja_asutus_id
) select nextval('hibernate_sequence'), now(), null, 0, 2.1, false,
  true, null, 'Põhikool C', true, '70000003',
  null, now(), null, null, (select asutus_id from asutus where registrikood = '30000001' and subsystem='DHX')
  WHERE
      NOT EXISTS (
          SELECT asutus_id FROM asutus WHERE registrikood = '70000003'
);
