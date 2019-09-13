package ee.ria.dhx.database

class Postgres extends Database {

    List getDocument(String dokment_id) {
        String query = "SELECT * FROM public.SESSION WHERE dokument_id = '$dokment_id'"
        println "Executing SQL query:\n" + query
        return db.rows(query)
    }
}
