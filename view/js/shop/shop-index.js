document.addEventListener('DOMContentLoaded', () => {
    // 页面元素引用
    const shopNameEl = document.getElementById("shop-name");
    const shopAccountEl = document.getElementById("shopAccount");
    const shopAddressEl = document.getElementById("shopAddress");
    const shopInfoEl = document.getElementById("shopInfo");
    const shopFollowersEl = document.getElementById("shopFollowers");
    const shopLikesEl = document.getElementById("shopLikes");

    // 获取 token 和 shopId
    const token = localStorage.getItem("token");
    const shopId = localStorage.getItem("shopId");

    if (!token || !shopId) {
        alert("请先登录");
        window.location.href = "shop-login.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);

    fetch("http://TV/ShopService/info", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ shopId: numericShopId })
    })
        .then(response => response.text())
        .then(text => {
            console.log("原始响应文本:", text);

            let shopData;
            try {
                shopData = JSON.parse(text).data;
            } catch (e) {
                throw new Error("JSON 解析失败：" + e.message);
            }

            console.log("实际店铺数据:", shopData);

            // ✅ 这里不再使用 data.data，而是 shopData 就是你要的数据
            shopNameEl.textContent = shopData.shopName || "未知名称";
            shopAccountEl.textContent = shopData.shopAccount || "-";
            shopAddressEl.textContent = shopData.shopAddress || "-";
            shopInfoEl.textContent = shopData.shopInfo || "暂无简介";
            shopFollowersEl.textContent = shopData.shopFollowers || "0";
            shopLikesEl.textContent = shopData.shopLikes || "0";

        })
        .catch(error => {
            console.error("请求失败:", error);
            alert("无法获取店铺信息，请检查网络或重新登录");
            window.location.href = "shop-login.html";
        });
    // 绑定注销商铺按钮点击事件
    const deleteBtn = document.querySelector('[data-action="delete-shop"]');
    if (deleteBtn) {
        deleteBtn.addEventListener("click", function (e) {
            e.preventDefault();

            const confirmed = confirm("⚠️ 您确定要注销店铺吗？此操作将永久删除您的店铺信息！");
            if (confirmed) {
                handleDeleteShop(token);
            }
        });
    }

    // 绑定退出登录按钮点击事件
    const logoutBtn = document.querySelector('[data-action="logout"]');
    if (logoutBtn) {
        logoutBtn.addEventListener("click", function (e) {
            e.preventDefault();

            const confirmed = confirm("您确定要退出登录吗？");
            if (confirmed) {
                logout();
            }
        });
    }
});

// 注销商铺逻辑
function handleDeleteShop(token) {
    fetch("http://TV/ShopService/delete", {
        method: "DELETE",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("注销失败，HTTP 状态码：" + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.message === "success") {
                alert("店铺已成功注销");
                localStorage.clear();
                window.location.href = "../index.html";
            } else {
                alert("注销失败：" + (data.message || "未知错误"));
            }
        })
        .catch(error => {
            console.error("注销出错:", error);
            alert("注销失败，请稍后再试。");
        });
}

// 退出登录逻辑（纯前端）
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("shopId");
    localStorage.removeItem("shopAccount");

    alert("已退出登录");
    window.location.href = "shop-login.html";
}