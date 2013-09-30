package classcard.example;

/**
 * Spring bean
 *
 */
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

/*
 * @author Zensey
 * @since 29.09.2013
 *
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see http://alvinalexander.com/java/jwarehouse/spring-framework-2.5.3/samples/imagedb/src/org/springframework/samples/imagedb/DefaultImageDatabase.java.shtml
 *
 */


public class ClobTest {

    @Autowired
    public JdbcTemplate j;
    @Autowired
    DefaultLobHandler lobHandler;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    class Mapper1 implements ParameterizedRowMapper<Clob> {

        public Clob mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getClob(1);
        }
    }

    public void testSelectClob() {
        int i = j.queryForInt("select 1 from dual");
        System.out.println("Hello ! " + i);

        String s = "select xml_file from TEST_TABLE where id=110";
        Clob o = j.queryForObject(s, new Mapper1());
        try {
            System.out.println("Hello ! " + o + " length=" + o.length());
            System.out.println("Hello ! " + o.getSubString(1, (int) o.length()));

        } catch (SQLException ex) {
            Logger.getLogger(ClobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testInsertClob() {
        String s = "insert into TEST_TABLE (id,xml_file) values (?, ?)";
        j.update(s, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setInt(1, 10000);
                lobHandler.getLobCreator().setClobAsString(ps, 2, "clob test");
                //lobHandler.getLobCreator().setBlobAsBinaryStream(ps, 2, fileAsStream, fileAsStream.toString().getBytes().length);
            }
        });
    }

    public void testCallProcWithClobParameter() {
        String s = "CALL TEST_PROC (?, ?)";
        j.update(s, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, "x.x.x.x");
                lobHandler.getLobCreator().setClobAsString(ps, 2, "clob test");
            }
        });
    }

    // более понятный способ
    public void executeFunctionWithClobParameterAndClobResult() {
        Clob o = (Clob) j.execute( new CallableStatementCreator() {
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement cs = con.prepareCall("{? = call TEST_FUNCT(?, ?)}");
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "x.x.x.x");
                lobHandler.getLobCreator().setClobAsString(cs, 3, "xml test");
                return cs;
            }
        }, new CallableStatementCallback() {
            public Clob doInCallableStatement(CallableStatement cs) throws SQLException {
                cs.execute();
                Clob result = cs.getClob(1);
                return result;
            }
        });

        try {
            System.out.println("Hello ! " + o.getSubString(1, (int) o.length()));
        } catch (Exception ex) {
            Logger.getLogger(ClobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // менее понятный способ
    public void callFunctionWithClobParameterAndClobResult() {
        List<SqlParameter> params = new LinkedList<SqlParameter>();
        params.add(new SqlOutParameter("out", Types.CLOB));
        params.add(new SqlParameter(Types.VARCHAR));
        params.add(new SqlParameter(Types.CLOB));

        Map<String, Object> ret = j.call(
                new CallableStatementCreator() {
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement cs = con.prepareCall("{? = call TEST_FUNCT(?, ?)}");
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "x.x.x.x");
                lobHandler.getLobCreator().setClobAsString(cs, 3, "xml test");
                return cs;
            }
        }, params);

        try {
            Clob c = (Clob)ret.get("out");
            System.out.println("Hello ! " + c.getSubString(1, (int) c.length()));
        } catch (Exception ex) {
            Logger.getLogger(ClobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
