document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get("userId");

    // 页面元素引用
    const usernameEl = document.getElementById("profile-username");
    const followingShopsEl = document.getElementById("profile-following-shops");
    const followersEl = document.getElementById("profile-followers");
    const followingUsersEl = document.getElementById("profile-following-users");

    const ordersSection = document.getElementById("profile-orders-section"); // 新增元素
    const ordersList = document.getElementById("orders-list"); // 新增元素

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    if (!userId || !token) {
        alert("请先登录");
        window.location.href = "user-index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);
    const numericCurrentUserId = parseInt(currentUserId, 10);

    // 设置跳转按钮链接
    const viewFollowingShopsBtn = document.querySelector("#profile-following-shops + a");
    const viewFollowersBtn = document.querySelector("#profile-followers + a");
    const viewFollowingUsersBtn = document.querySelector("#profile-following-users + a");

    if (viewFollowingShopsBtn) {
        viewFollowingShopsBtn.href = `user-follow-shops.html?userId=${numericUserId}`;
    }
    if (viewFollowersBtn) {
        viewFollowersBtn.href = `user-followers.html?userId=${numericUserId}`;
    }
    if (viewFollowingUsersBtn) {
        viewFollowingUsersBtn.href = `user-follow-users.html?userId=${numericUserId}`;
    }

    // ✅ 判断是否是当前用户，决定是否显示“我的订单”部分
    if (numericUserId === numericCurrentUserId && ordersSection) {
        ordersSection.style.display = "block"; // 显示订单区
        loadUserOrders(numericUserId); // 加载订单数据
    } else {
        if (ordersSection) {
            ordersSection.style.display = "none"; // 隐藏订单区
        }
    }

    // 获取用户信息
    fetch("http://TV/UserService/info", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ userId: numericUserId })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到用户信息:", data);

            if (data.code === 200 && data.data) {
                const user = data.data;

                usernameEl.textContent = user.userName || "匿名用户";
                followingShopsEl.textContent = user.FollowingShops || 0;
                followersEl.textContent = user.followers || 0;
                followingUsersEl.textContent = user.FollowingUsers || 0;

            } else {
                alert("获取用户信息失败：" + (data.message || "数据异常"));
                window.location.href = "user-index.html";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取用户信息，请检查网络连接");
            window.location.href = "user-index.html";
        });
});

/**
 * 加载用户的优惠券订单
 */
async function loadUserOrders(userId) {
    const ordersList = document.getElementById("orders-list");
    if (!ordersList) return;

    try {
        const orderResponse = await fetch("http://TV/VoucherService/getUserCouponOrders", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId: userId })
        });

        const orderData = await orderResponse.json();

        if (orderData.code === 200 && Array.isArray(orderData.data?.data)) {
            const orders = orderData.data.data;

            if (orders.length === 0) {
                const item = document.createElement("li");
                item.className = "list-group-item";
                item.innerHTML = `<div>暂无购买记录</div>`;
                ordersList.appendChild(item);
                return;
            }

            for (const order of orders) {
                const couponId = order.couponId;

                try {
                    // 🚀 查询优惠券详情
                    const couponResponse = await fetch("http://TV/VoucherService/getSecKillInfo", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({ couponId: couponId })
                    });

                    const couponData = await couponResponse.json();

                    if (couponData.code === 200 && couponData.data) {
                        const coupon = couponData.data;

                        // 🧾 构建订单项
                        const item = document.createElement("li");
                        item.className = "list-group-item";

                        item.innerHTML = `
                            <div class="order-info">
                                <strong>${coupon.couponName}</strong><br/>
                                <span>店铺 ID：${coupon.shopId}</span><br/>
                                <span>结束时间：${coupon.endTime}</span><br/>
                                <span>订单编号：${order.orderId}</span>
                            </div>
                            <a href="user-coupon-detail.html?couponId=${couponId}&orderId=${order.orderId}" 
                               class="btn small-btn">查看详情</a>
                        `;

                        ordersList.appendChild(item);
                    }

                } catch (err) {
                    console.error(`获取 couponId=${couponId} 失败`, err);
                }
            }

        } else {
            const item = document.createElement("li");
            item.className = "list-group-item";
            item.innerHTML = `<div>没有购买任何优惠券</div>`;
            ordersList.appendChild(item);
        }

    } catch (error) {
        console.error("获取订单列表失败:", error);
        const item = document.createElement("li");
        item.className = "list-group-item";
        item.innerHTML = `<div>无法加载订单记录</div>`;
        ordersList.appendChild(item);
    }
}