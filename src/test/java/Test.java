import com.backinfile.GameFramework.core.Call;
import com.backinfile.GameFramework.core.serialize.InputStream;
import com.backinfile.GameFramework.core.serialize.SerializableManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Test {

    @org.junit.jupiter.api.Test
    public void test() {
        SerializableManager.registerAll();
        Call call = new Call();
        call.portId = 1;
        call.objId = 123;
        call.value = Arrays.asList(1,2,3);
        call.value2 = 99;
        call.intArr = new int[]{1,2};
        call.setPrivateValue(122);
        call.aBoolean = true;
        call.aBoolean2 = true;
        call.aDouble = 12.234d;
        call.aFloat = 324f;
        call.aByte = 123;


        Call clone = SerializableManager.clone(call);
        System.out.println();
    }
}
