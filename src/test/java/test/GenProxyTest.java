package test;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.RPCMethod;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.Task;
import com.backinfile.GameFramework.gen.GenTools;
import com.backinfile.GameFramework.serialize.Serializable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class GenProxyTest {


    public static class Service1 extends Service {
        @Override
        public void init() {

        }

        @Override
        public void pulse() {

        }

        @Override
        public void pulsePerSec() {

        }

        @RPCMethod
        public Task<String> getTestString(int value) {
            LogCore.core.info("req value:{}", value);
            return Task.completedTask(this.getClass().getName());
        }

        @RPCMethod
        public Task<TestInfo> calcInfo(TestInfo info) {
            info.a = info.args.size();
            return Task.completedTask(info);
        }
    }

    @Serializable
    public static class TestInfo {
        public int a;
        public List<String> args = new ArrayList<>();
    }


    @Test
    public void test() {
        String genPath = "/src/test/java/gen";
        GenTools.genServiceProxy(Service1.class, "gen", GenTools.getAbsolutePath(genPath));
    }

    @Test
    public void testGenAll() {
        String genPath = GenTools.getAbsolutePath("/src/test/java/gen");
        GenTools.genAllServiceProxy(GenProxyTest.class, "gen", genPath, true);
    }
}
