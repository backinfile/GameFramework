package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.db.DBDirectProvider;
import com.backinfile.GameFramework.db.DBEntity;
import com.backinfile.GameFramework.db.EntityBase;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class DBTest {

    @DBEntity(tableName = "test2")
    public static class TestDB extends EntityBase {
        public String name;
        public long playerId;
        public int value;
        public int value2;
    }

    @RepeatedTest(3)
    public void testDB() {
        GameStartUp.initAll(DBTest.class);
        GameStartUp.enableDirectDB();

        DBDirectProvider loadProvider = DBDirectProvider.getInstance();
        for (TestDB db : loadProvider.queryAll(TestDB.class)) {
            db.remove();
        }

        TestDB db = new TestDB();
        db.id = 12341L;
        db.playerId = 90001;
        db.name = "bob";
        db.save();

        assert loadProvider.querySingle(TestDB.class, db.id).name.equals(db.name);
        loadProvider.close();
    }


    @Test
    public void test() throws SQLException {
//        GameStartUp.initAll(DBTest.class);
//
////        DBManager.registerAll(DBTest.class.getClassLoader());
//        Connection connection = DriverManager.getConnection("jdbc:sqlite:D:/game.db");
//        DBManager.updateTableStruct(connection);
//
//        TestDB testDB = new TestDB();
//        testDB.id = 4;
//        testDB.name = "2q32";
//        testDB.value = 123;
//        testDB.value2 = 2453;
//        testDB.playerId = 1243;
//
//        DBManager.insert(connection, testDB);
////        DBManager.delete(connection, "test", 1);
//
//        List<Object> test = DBManager.queryAll(connection, "test", 1243);
//
//        connection.close();
    }
}
