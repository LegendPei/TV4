document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const couponId = urlParams.get("couponId");

    if (!couponId) {
        alert("未找到优惠券 ID");
        window.location.href = "user-flashsale.html";
        return;
    }

    // 页面元素引用
    const couponNameEl = document.getElementById("coupon-name");
    const couponTypeEl = document.getElementById("coupon-type");
    const couponMinSpendEl = document.getElementById("coupon-min-spend");
    const couponTotalStockEl = document.getElementById("coupon-total-stock");
    const couponAvailableStockEl = document.getElementById("coupon-available-stock");
    const couponMaxUserEl = document.getElementById("coupon-max-user");
    const couponStartTimeEl = document.getElementById("coupon-start-time");
    const couponEndTimeEl = document.getElementById("coupon-end-time");
    const couponShopIdEl = document.getElementById("coupon-shop-id");

    const countdownText = document.getElementById("countdown-text");
    const actionsContainer = document.getElementById("coupon-actions");

    // 类型映射表
    const typeMap = {
        1: "立减券",
        2: "满减券",
        3: "折扣券"
    };

    // 请求优惠券详情接口
    fetch("http://TV/VoucherService/getSecKillInfo", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ couponId: parseInt(couponId, 10) })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到优惠券详情:", data);

            if (data.code === 200 && data.data) {
                const coupon = data.data;

                // 更新页面内容
                couponNameEl.textContent = coupon.couponName || "未知名称";
                couponTypeEl.textContent = typeMap[coupon.couponType] || "未知类型";
                couponMinSpendEl.textContent = `${coupon.minSpend / 100} 元`;
                couponTotalStockEl.textContent = coupon.totalStock || "0";
                couponAvailableStockEl.textContent = coupon.availableStock || "0";
                couponMaxUserEl.textContent = `${coupon.maxPerUser} 张`;
                couponStartTimeEl.textContent = coupon.startTime || "-";
                couponEndTimeEl.textContent = coupon.endTime || "-";

                // ✅ 调用接口获取商铺名
                fetch("http://TV/ShopService/info", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ shopId: coupon.shopId })
                })
                    .then(res => res.json())
                    .then(shopData => {
                        if (shopData.code === 200 && shopData.data?.shopName) {
                            couponShopIdEl.innerHTML = `
                                <a href="user-shop.html?shopId=${coupon.shopId}" class="shop-link">
                                    ${shopData.data.shopName}
                                </a>`;
                        } else {
                            couponShopIdEl.textContent = "店铺不存在或拉取失败";
                        }
                    })
                    .catch(err => {
                        console.error("获取商铺信息出错:", err);
                        couponShopIdEl.textContent = "加载失败";
                    });

                // ✅ 修正：将 couponId 明确传入
                handleCountdown(
                    parseInt(couponId, 10),
                    coupon.startTime,
                    coupon.endTime,
                    coupon.availableStock,
                    coupon.shopId
                );

            } else {
                alert("获取优惠券详情失败：" + (data.message || "数据异常"));
                window.location.href = "user-flashsale.html";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取优惠券详情，请检查网络或重试");
            window.location.href = "user-flashsale.html";
        });
});

/**
 * 处理倒计时和按钮显示
 */
function handleCountdown(couponId, startTimeStr, endTimeStr, availableStock, shopId) {
    const now = new Date();
    const startTime = new Date(startTimeStr);
    const endTime = new Date(endTimeStr);

    const countdownText = document.getElementById("countdown-text");
    const actionsContainer = document.getElementById("coupon-actions");

    function updateCountdown() {
        const now = new Date();

        if (now > endTime) {
            // ✅ 活动已结束
            countdownText.textContent = "该活动已结束";
            actionsContainer.innerHTML = `<button class="btn small-btn disabled">该活动已结束</button>`;

        } else if (now < startTime) {
            // 🕐 活动尚未开始
            const diff = startTime - now;
            const { days, hours, minutes, seconds } = getTimeRemaining(diff);
            countdownText.textContent = `距离开始还有：${days}天 ${hours}小时 ${minutes}分钟 ${seconds}秒`;
            actionsContainer.innerHTML = `<button class="btn small-btn disabled">该活动未开始</button>`;

        } else {
            // 🎯 活动进行中
            const diff = endTime - now;

            if (diff <= 0) {
                countdownText.textContent = "该活动已结束";
                actionsContainer.innerHTML = `<button class="btn small-btn disabled">该活动已结束</button>`;
                return;
            }

            const { days, hours, minutes, seconds } = getTimeRemaining(diff);
            countdownText.textContent = `距离结束还有：${days}天 ${hours}小时 ${minutes}分钟 ${seconds}秒`;

            // ✅ 使用 innerHTML + DOM 操作避免重复创建元素
            actionsContainer.innerHTML = `<button id="seckill-button" class="btn seckill-btn">立即抢购</button>`;

            // ✅ 绑定点击事件
            const seckillButton = document.getElementById("seckill-button");
            if (seckillButton && !seckillButton.dataset.hasEvent) {
                seckillButton.dataset.hasEvent = "true"; // 防止重复绑定

                seckillButton.addEventListener("click", async () => {
                    const userId = localStorage.getItem("userId");
                    const token = localStorage.getItem("token");

                    if (!userId || !token) {
                        alert("请先登录");
                        window.location.href = "../../index.html";
                        return;
                    }

                    try {
                        // 🔥 发起秒杀请求，获取 orderId
                        const response = await fetch("http://TV/VoucherService/secKill", {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json",
                                "Authorization": "Bearer " + token
                            },
                            body: JSON.stringify({
                                couponId: parseInt(couponId, 10),
                                userId: parseInt(userId, 10)
                            })
                        });

                        const result = await response.json();

                        if (result.code === 200 && result.data?.orderId) {
                            const orderId = result.data.orderId;
                            const expireTime = Date.now() + 5 * 60 * 1000; // 5 分钟后过期时间戳

                            // ✅ 成功后跳转并携带参数
                            window.location.href = `user-coupon-seckill.html?couponId=${couponId}&orderId=${orderId}&expireTime=${expireTime}`;
                        } else {
                            alert("❌ 抢购失败：" + (result.message || "未知错误"));
                        }

                    } catch (error) {
                        console.error("请求出错:", error);
                        alert("无法完成抢购，请检查网络连接");
                    }
                });
            }
        }
    }

    updateCountdown(); // 初始调用一次
    setInterval(updateCountdown, 1000); // 每秒刷新一次
}

/**
 * 辅助函数：格式化时间差
 */
function getTimeRemaining(diff) {
    const seconds = Math.floor((diff / 1000) % 60);
    const minutes = Math.floor((diff / 1000 / 60) % 60);
    const hours = Math.floor((diff / 1000 / 60 / 60) % 24);
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    return { days, hours, minutes, seconds };
}