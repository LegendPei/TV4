document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get("userId");

    if (!userId) {
        alert("未找到用户 ID");
        window.location.href = "user-index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);

    // 页面元素引用
    const followList = document.getElementById("follow-list");

    // 获取关注的商铺 ID 列表
    fetch("http://TV/FollowService/followingShops", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ followerId: numericUserId })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到关注的商铺列表:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const shopIds = data.data.data;

                if (shopIds.length === 0) {
                    followList.innerHTML = "<li class='list-group-item'>暂无关注的商铺</li>";
                    return;
                }

                // ✅ 异步加载每个商铺的详细信息
                loadShopDetails(shopIds);

            } else {
                alert("获取商铺列表失败：" + (data.message || "数据异常"));
                followList.innerHTML = "<li class='list-group-item'>获取失败</li>";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取关注的商铺，请检查网络连接");
            followList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        });

    /**
     * 异步加载每个商铺的信息
     */
    async function loadShopDetails(shopIds) {
        followList.innerHTML = ""; // 清空旧数据

        for (const shopId of shopIds) {
            try {
                const response = await fetch("http://TV/ShopService/info", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ shopId: shopId })
                });

                const shopData = await response.json();

                if (shopData.code === 200 && shopData.data?.shopName) {
                    const shop = shopData.data;

                    const item = document.createElement("li");
                    item.className = "list-group-item";

                    item.innerHTML = `
                        <a href="user-shop.html?shopId=${shop.shopId}" class="btn small-btn">${shop.shopName}</a>
                        <p>${shop.shopInfo || "暂无简介"}</p>
                    `;

                    followList.appendChild(item);

                } else {
                    console.error(`商铺 ${shopId} 加载失败`, shopData.message);
                }

            } catch (err) {
                console.error(`获取商铺 ${shopId} 失败`, err);
            }
        }

        if (followList.children.length === 0) {
            followList.innerHTML = "<li class='list-group-item'>加载失败</li>";
        }
    }
});