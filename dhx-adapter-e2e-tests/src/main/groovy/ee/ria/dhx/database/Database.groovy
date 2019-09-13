package ee.ria.dhx.database

import groovy.sql.Sql
import groovy.xml.MarkupBuilder

class Database {

    String url
    String username
    String password
    String driver
    @Lazy
    Sql db = {
        Sql.newInstance(url, username, password, driver)
    }()

    static injectString(value) {
        if (value instanceof String) {
            return "'${value}'"
        } else {
            return value
        }
    }

    static String injectSql(value) {
        if (value instanceof List) {
            if (value.size() > 0) {
                return " in (${value.collect() { injectString(it) }.join(', ')})"
            } else {
                return " in (null)"
            }

        } else {
            return "=${injectString(value)}"
        }
    }

    static String sqlResultToHtml(List rows) {
        StringWriter st = new StringWriter()
        def mkup = new MarkupBuilder(st)
        if (rows.size() > 0) {
            mkup.html {
                table(style: 'border:1px solid;text-align:center;') {
                    tr {
                        th()
                        rows[0].keySet().each { th(it) }
                    }
                    rows.eachWithIndex { data, index ->
                        tr(style: "outline: thin solid;") {
                            td(index + 1)
                            data.each { k, v -> td(v) }
                        }


                    }
                }

            }
        } else {
            mkup.html {}
        }
        return st.toString()
    }
}
