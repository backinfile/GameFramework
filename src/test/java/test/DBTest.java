package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.db.DBDirectProvider;
import com.backinfile.GameFramework.db.DBEntity;
import com.backinfile.GameFramework.db.EntityBase;
import com.backinfile.GameFramework.db.ILoadProvider;
import org.junit.jupiter.api.RepeatedTest;

public class DBTest {

    @DBEntity(tableName = "test2")
    public static class TestDB extends EntityBase {
        public String name;
        public long playerId;
        public int value;
        public int value2;
        public int value3;
    }

    @RepeatedTest(3)
    public void testDB() {
        GameStartUp.initAll(DBTest.class);
        GameStartUp.enableDirectDB();

        ILoadProvider loadProvider = DBDirectProvider.getInstance();
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

    @RepeatedTest(2)
    public void updateTest() {
        GameStartUp.initAll(DBTest.class);
        GameStartUp.enableDirectDB();

        ILoadProvider loadProvider = DBDirectProvider.getInstance();
        for (TestDB db : loadProvider.queryAll(TestDB.class)) {
            db.remove();
        }

        long id = 124233L;
        {
            TestDB db = new TestDB();
            db.id = id;
            db.playerId = 90001;
            db.name = "bob";
            db.save();
        }

        TestDB db = loadProvider.querySingle(TestDB.class, id);
        db.name = "will";
        db.save();

        assert loadProvider.querySingle(TestDB.class, db.id).name.equals(db.name);
        loadProvider.close();
    }

    public static void main(String[] args) {
        GameStartUp.initAll(DBTest.class);
        GameStartUp.enableDirectDB();
        DBDirectProvider.getInstance().backup("D:");
        DBDirectProvider.getInstance().close();
    }
}
