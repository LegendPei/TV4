本项目为工作室考核练手作
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

## common模块

里面包含了手写的一些框架和一些工具类

### api

里面放有需要注册至nacos里面的接口，以供模块间的交互

### exception

里面放有异常类，以供全局异常的处理

### filter

为之前实验全局异常捕获的代码

### model

包含了统一后端响应和排序方法的实体类这些

### mybatis

为手写的简易版的mybatis框架

### springframework

为手写的简易版的sprng框架，能将注解的对象通过扫描器统一注册到bean里进行管理

### util

为手写的工具类，包含以下功能：
1.生成token的工具

2.redis工具

3.连接池

4.反序列化时统一使用的gson工具类

5.加载配置文件和加载xml文件的工具类

6.日志框架及其记录格式的工具类

7.给seata提供代理数据源的工具类及给seata初始化配置的工具类

8.sql语句执行器

9.模仿雪花算法实现的简易版的优惠卷id生成器

## 业务模块

大致的模块结构都是分为这样的：

### controller

为给前端写接口的包，通过注解开发，并且也是消费者使用提供者提供的服务的地方，如果模块为消费者，那么在调用其他模块的时候就会使用seata事务，对于voucher模块还使用了lua脚本来保证抢购的原子性

### dao

为与数据库交互的包，由接口及其实现类构成

### filter

包含跨域和token的过滤器

### listener

为运行模块时自动加载的初始化类和servlet总站

### model

为对应模块的实体类

### service

为对应模块的业务层，并且都有redis

### resources

放置模块的配置文件这些

### webapp

放置了一些web的配置

### test

放置模块的测试类

# 技术栈

## dubbo，版本3.2.0

用web.xml里面存放该模块的dubbo配置信息和调用服务的信息来通过ApplicationInitializer来进行初始化配置信息，并且通过@DubboReference注解来注入提供者提供的服务

## nacos，版本2.5.1

为服务的注册中心，提供者通过dubbo将服务注册到上面，同时也是seata注册服务提供事务管理的地方

## seata，版本2.3.0

由于不能使用spring框架，所以依赖引入的是seata-all 2.0.0 ,通过在resources里面配置相关的file.conf和register.conf文件再使用相关的启动RM和TM的代码来连接到TC上，并且代理数据源和事务开启回滚都需要自己手动完成

## redis

对雪崩有随机化缓存时间的策略：对缓存的刷新时间在原本固定时间的情况下再增加一定范围内的缓存时间

对击穿有判断请求数据是否合法和缓存控制的策略：对前端的请求中如果检测到数据不符合会直接拒绝，合理的数据而在数据库中没有的话会被赋予空值

对穿透有设置合理的ttl的策略：比如说对于秒杀活动，设置ttl的时间为整个活动的存在时间，保证了热点key不会在活动期间过期

并且每次新创建数据时，都会刷新对应redis，部分有需要的模块也会某一段时间内刷新redis来保证数据的准确性

# 数据库建表语句

## 用户表

```
create table users
(
    user_id         int auto_increment        --用户id，插入时自增，范围在100000~199999
        primary key,
    user_name       varchar(50)   not null,   --用户名字
    user_account    varchar(50)   not null,   --用户账号
    followers       int default 0 not null,   --粉丝数
    following_shops int default 0 not null,   --关注商铺数
    following_users int default 0 not null,   --关注用户数
    user_password   varchar(255)  not null,   --用户密码
    constraint account                        
        unique (user_account),                --确保用户账号和名字的唯一性   
    constraint username
        unique (user_name)
);
```

## 商铺表

```
create table shops
(
    shop_id        int auto_increment         --商铺id，插入时自增，范围在200000~299999
        primary key,
    shop_name      varchar(100)  not null,	  --商铺名字
    shop_account   varchar(50)   not null,    --商铺账号 
    shop_followers int default 0 not null,    --商铺粉丝
    shop_likes     int default 0 null,        --商铺点赞数
    shop_info      varchar(255)  not null,    --商铺信息
    shop_password  varchar(255)  not null,    --商铺密码
    shop_address   varchar(255)  not null,    --商铺地址
    constraint shop_account
        unique (shop_account),                --确保商铺账号和名字的唯一性   
    constraint shop_name
        unique (shop_name)
);
```

## 评论表

```
create table comments
(
    comment_id      int auto_increment                             --评论id，在插入时自增，范围1~99999
        primary key,
    commenter_id    int                                not null,   --评论者id
    target_id       int                                not null,   --评论目标id
    comment_time    datetime default CURRENT_TIMESTAMP null,       --评论时间，插入时生成
    comment_content text                               not null,   --评论内容
    comment_likes   int      default 0                 null,       --评论点赞数
    constraint comments_ibfk_1
        foreign key (commenter_id) references users (user_id)      --外键
            on delete cascade
);

create index commenter_id                                          --索引
    on comments (commenter_id);

create index target_id
    on comments (target_id);
```

## 用户关注表

```
create table user_follows
(
    user_id     int not null,           --用户id
    follower_id int not null,			--关注者id
    primary key (user_id, follower_id)	--主键
);
```

## 商铺关注表

```
create table shop_follows
(
    shop_id     int not null,			--商铺id
    follower_id int not null,			--关注者id
    primary key (follower_id, shop_id)	--主键
);

```

## 评论点赞表

```
create table comment_likes
(
    comment_id        int                                not null,		--评论id
    comment_like_id   int                                not null,		--评论点赞者id
    comment_like_time datetime default CURRENT_TIMESTAMP not null,		--评论点赞时间，插入时自动生成
    primary key (comment_id, comment_like_id),							--主键
    constraint comment_likes_ibfk_1
        foreign key (comment_id) references comments (comment_id)		--外键
            on delete cascade
);
```

## 商铺点赞表

```
create table shop_likes
(
    shop_id        int                                not null,			--商铺id
    shop_like_time datetime default CURRENT_TIMESTAMP not null,			--商铺点赞时间，插入时自动生成
    shop_like_id   int                                not null,			--商铺点赞者id
    primary key (shop_id, shop_like_id),								--主键
    constraint shop_likes_ibfk_1
        foreign key (shop_id) references shops (shop_id)				--外键
            on delete cascade
);
```

## 动态点赞表

```
create table blog_likes
(
    blog_id    int                      not null,			--动态id
    liker_id   int                      not null,			--点赞者id
    likes_time datetime default (now()) not null,			--点赞时间，插入时自动生成
    primary key (blog_id, liker_id)							--主键
);
```

## 秒杀活动表

```
create table coupon
(
    coupon_id       int auto_increment 			--秒杀活动id,插入时自动递增，范围300000~399999
        primary key,
    coupon_type     int          not null, 		--秒杀卷类型,1为立减卷2为满减卷3为折扣卷
    discount_amount int          null ,			--折扣金额或折扣比例，单位为分
    min_spend       int          null ,			--使用门槛，，单位为分
    total_stock     int          not null , 	--总库存量
    available_stock int          null ,			--当前可用库存量
    start_time      datetime     not null,		--开始时间
    end_time        datetime     not null,		--结束时间
    max_per_user    int          not null,		--每个用户的限购数量
    coupon_name     varchar(255) not null,		--活动名字
    shop_id         int          not null		--使用商铺id
);
```

## 秒杀活动订单表

```
create table coupon_order
(
    order_id  bigint not null					--订单id
        primary key,
    coupon_id int    not null,					--活动id
    user_id   int    not null					--用户id
);

create index coupon_order_coupon_id_index		--索引
    on coupon_order (coupon_id);

create index coupon_order_user_id_index
    on coupon_order (user_id);
```

## 动态表

```
create table blogs
(
    blog_id          int auto_increment,					--动态id，插入时自增，范围400000~499999
    target_id        int                      null,			--动态目标id，如要推荐的商铺
    blog_name        varchar(255)             not null,		--动态名字
    author_id        int                      not null,		--作者id
    blog_content     varchar(255)             not null,		--动态内容
    blog_time        datetime default (now()) not null,		--动态发布时间，插入时生成
    blog_likes       int      default 0       null,			--动态点赞数
    blog_collections int      default 0       null,			--动态收藏数
    file_path        varchar(255)             null,			--文件路径
    blog_type        int                      not null,		--动态类型1是美食推荐，2是商铺推荐，3是秒杀分享
    primary key (blog_id, author_id)						
);
```

## 动态收藏表

```
create table blog_collection
(
    blog_id         int                                not null,	--动态id
    user_id         int                                not null,  	--用户id
    collection_date datetime default CURRENT_TIMESTAMP not null,	--收藏的时间，插入时生成
    primary key (blog_id, user_id)
);
```

## seata全局锁

在seata配置里面粘贴过来的

```
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `status`         TINYINT      NOT NULL DEFAULT '0' COMMENT '0:locked ,1:rollbacking',
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_status` (`status`),
    KEY `idx_branch_id` (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

