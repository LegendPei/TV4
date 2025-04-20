package com.peitianbao.www.util.token;

import com.peitianbao.www.util.LoadProperties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Properties;

/**
 * @author leg
 */
public class JwtKeyGenerator {
    private static final SecretKey SECRET_KEY;

    //加载配置
    static String configFileName = "application.properties";
    static Properties properties = LoadProperties.load(configFileName);
    static String algorithm = properties.getProperty("jwt.algorithm");
    static int keySize = Integer.parseInt(properties.getProperty("jwt.keySize"));

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
            keyGen.init(keySize);

            //生成密钥
            SECRET_KEY = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("无法生成密钥", e);
        }
    }

    //获取原始密钥
    public static SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}
