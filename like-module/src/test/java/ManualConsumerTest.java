import com.peitianbao.www.api.ShopService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

public class ManualConsumerTest {
    public static void main(String[] args) {
        // 初始化 ApplicationConfig
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("manual-consumer");

        // 初始化 RegistryConfig
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("nacos://127.0.0.1:8848");

        // 创建 ReferenceConfig
        ReferenceConfig<ShopService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setInterface(ShopService.class);
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setTimeout(30000);
        // 获取远程服务代理对象
        ShopService shopService = referenceConfig.get();

        try {
            shopService.incrementShopLikes(200000);
            System.out.println("调用成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}