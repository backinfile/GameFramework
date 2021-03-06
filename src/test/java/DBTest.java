import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.db.DBAsyncObject;
import com.backinfile.GameFramework.db.DBEntity;
import com.backinfile.GameFramework.db.DBManager;
import com.backinfile.GameFramework.db.EntityBase;
import com.backinfile.GameFramework.proxy.Proxy;
import com.backinfile.GameFramework.proxy.ProxyManager;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.backinfile.support.Time;
import com.backinfile.support.Utils;
import com.ea.async.Async;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DBTest {

    public static class Port1 extends Port {
        public Port1() {
            super("Port1");
        }

        @Override
        public void startup() {
            timerQueue.applyTimer(Time.SEC, () -> {
                DBAsyncObject tmpAsyncObj = Proxy.getProxy(DBAsyncObject.class);
                tmpAsyncObj.backupDatabase();
            });
            super.startup();
        }
    }

    @Test
    public void testDB() {
        Async.init();
        ProxyManager.registerAll(DBTest.class.getClassLoader());
        DBManager.registerAll(DBTest.class.getClassLoader());
        SerializableManager.registerAll(DBTest.class.getClassLoader());

        Node node = new Node();
        node.addPort(new Port1(), Port.of(new DBAsyncObject("game.db")));
        node.startUp();
        node.waitAllPortStartupFinish();
        Utils.sleep(Time.SEC * 2);
        node.abort();
        node.join();

    }


    @DBEntity(table = "test")
    public static class TestDB extends EntityBase {
        public String name;
        public int playerId;
        public int value;
        public int value2;
    }

    @Test
    public void test() throws SQLException {
        DBManager.registerAll(DBAsyncObject.class.getClassLoader());
        Connection connection = DriverManager.getConnection("jdbc:sqlite:game.db");
        DBManager.updateTableStruct(connection);

        TestDB testDB = new TestDB();
        testDB.id = 4;
        testDB.name = "2q32";
        testDB.value = 123;
        testDB.value2 = 2453;
        testDB.playerId = 1243;

        DBManager.insert(connection, testDB);
//        DBManager.delete(connection, "test", 1);

        List<Object> test = DBManager.queryAll(connection, "test", 1243);

        connection.close();
    }
}
