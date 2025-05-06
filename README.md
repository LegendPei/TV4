#  运行项目

打开wsl2
输入redis-cli

禁用保护模式
CONFIG SET protected-mode no

nacos:
进入bin目录下打开cmd输入：startup.cmd -m standalone

seata：
进入bin目录下打开cmd输入：seata-server.bat -p 9091 -h 0.0.0.0 -m file

nginx：
进入nginx目录下运行nginx.exe

运行模块顺序:user--->shop--->comment--->voucher--->follow--->blog--->like

## 简化为 redis启动！nacos启动！seata启动！nginx启动！tomcat启动!

# 依赖环境

由TV4的pom.xml统一管理依赖版本，其余模块选择性的依赖上面的东西，其中common模块作为工具类被其他模块依赖

```
<!-- 引入公共模块(如果是其他模块) -->
        <dependency>
            <groupId>com.peitianbao.www</groupId>
            <artifactId>common-module</artifactId>
            <version>${project.version}</version>
        </dependency>

<!-- 全局依赖管理 -->
    <dependencyManagement>
        <dependencies>
            <!-- dom4j 依赖 -->
            <dependency>
                <groupId>org.dom4j</groupId>
                <artifactId>dom4j</artifactId>
                <version>2.1.4</version>
            </dependency>

            <!-- 文件上传 -->
            <dependency>
                <groupId>commons-fileupload</groupId>
                <artifactId>commons-fileupload</artifactId>
                <version>1.4</version>
            </dependency>

            <!-- IO 工具类 -->
            <dependency>
                <groupId>commons-io</groupId>
                <artifactId>commons-io</artifactId>
                <version>2.11.0</version>
            </dependency>

            <!-- Lombok 依赖 -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.28</version>
                <scope>provided</scope>
            </dependency>

            <!-- MySQL JDBC 驱动 -->
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>8.0.33</version>
            </dependency>

            <!-- jbcrypt 依赖 -->
            <dependency>
                <groupId>org.mindrot</groupId>
                <artifactId>jbcrypt</artifactId>
                <version>0.4</version>
            </dependency>

            <!-- Servlet 依赖 -->
            <dependency>
                <groupId>javax.servlet</groupId>
                <artifactId>javax.servlet-api</artifactId>
                <version>4.0.1</version>
                <scope>provided</scope>
            </dependency>

            <!-- Mockito -->
            <dependency>
                <groupId>org.mockito</groupId>
                <artifactId>mockito-core</artifactId>
                <version>5.5.0</version>
                <scope>test</scope>
            </dependency>

            <!-- JWT 依赖 -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>0.12.3</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>0.12.3</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>0.12.3</version>
                <scope>runtime</scope>
            </dependency>

            <!-- Redis 依赖 -->
            <dependency>
                <groupId>redis.clients</groupId>
                <artifactId>jedis</artifactId>
                <version>4.4.3</version>
            </dependency>

            <!-- Gson 依赖 -->
            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>2.10.1</version>
            </dependency>

			<!-- fastjson 依赖 -->
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>1.2.83</version> 
            </dependency>

            <!-- JUnit 5 依赖 -->
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <version>5.10.0</version>
                <scope>test</scope>
            </dependency>
			
			<!-- dubbo 依赖 -->
            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo</artifactId>
                <version>3.2.0</version>
            </dependency>

			<!-- nacos 依赖 -->
            <dependency>
                <groupId>com.alibaba.nacos</groupId>
                <artifactId>nacos-client</artifactId>
                <version>2.5.1</version>
            </dependency>
			
			<!-- seata 依赖 -->
            <dependency>
                <groupId>io.seata</groupId>
                <artifactId>seata-all</artifactId>
                <version>2.0.0</version>
            </dependency>

        </dependencies>
    </dependencyManagement>

```

# 项目结构

项目由八大模块组成，其中common为工具类模块，view为写前端页面的地方，其他模块为对应名字的业务模块