import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;

public class TestSeataTx {
    public static void main(String[] args) {
        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
        try {
            tx.begin(60000, "my_test_tx_group");
            System.out.println("✅ 成功开启事务：" + RootContext.getXID());
            tx.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}