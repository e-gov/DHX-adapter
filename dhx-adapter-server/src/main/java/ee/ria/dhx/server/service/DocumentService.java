package ee.ria.dhx.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class DocumentService implements IDocumentService {

    @Autowired
    JdbcTemplate template;

    public List<DocumentStatus> findAll() {
        String sql = "select DISTINCT vastuvotja.outgoing,(select registrikood from public.asutus where asutus_id = dokument.asutus_id) AS saatja_registrikood,(select nimetus from public.asutus where asutus_id = dokument.asutus_id) AS saatja,\n" +
                "                public.dokument.dokument_id, public.transport.saatmise_algus,public.transport.saatmise_lopp, public.asutus.nimetus AS Vastuvõtja,public.asutus.registrikood AS Vastuvõtja_registrikood, public.vastuvotja.staatus_id,vastuvotja.fault_string AS veateade\n" +
                "from public.dokument\n" +
                "         join public.transport on dokument.dokument_id = transport.dokument_id\n" +
                "         join public.vastuvotja on transport.transport_id = vastuvotja.transport_id\n" +
                "         join public.asutus on asutus.asutus_id = vastuvotja.asutus_id\n" +
                "         join public.kaust on public.kaust.kaust_id = public.dokument.kaust_id\n" +
                "where public.transport.saatmise_algus > '2019-06-27'" +
                "ORDER BY saatmise_algus DESC";
        RowMapper<DocumentStatus> rm = new RowMapper<DocumentStatus>() {
            @Override
            public DocumentStatus mapRow(ResultSet resultSet, int i) throws SQLException {
                DocumentStatus documentStatus = new DocumentStatus(resultSet.getString("saatja_registrikood"),
                        resultSet.getString("saatja"),
                        resultSet.getInt("dokument_id"),
                        resultSet.getDate("saatmise_algus"),
                        resultSet.getDate("saatmise_lopp"),
                        resultSet.getString("vastuvõtja"),
                        resultSet.getString("vastuvõtja_registrikood"),
                        resultSet.getInt("staatus_id"),
                        resultSet.getString("veateade"));

                return documentStatus;
            }
        };
        return template.query(sql, rm);
    }
}
