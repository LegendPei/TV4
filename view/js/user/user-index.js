document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const shopListBody = document.getElementById("shop-list");

    const sortLikesBtn = document.getElementById("sort-likes");
    const sortFollowersBtn = document.getElementById("sort-followers");

    let currentSortType = "likes"; // 默认排序方式为点赞数

    // 获取用户 ID（验证是否登录）
    const userId = localStorage.getItem("userId");
    document.getElementById("view-profile-btn").href = `user-profile.html?userId=${userId}`;
    if (!userId) {
        alert("未登录，请重新登录");
        window.location.href = "../index.html";
        return;
    }

    // 设置标题
    pageHeader.textContent = "加载中...";

    // 绑定排序按钮点击事件
    function setupSortButton(button, type) {
        button.addEventListener("click", () => {
            [sortLikesBtn, sortFollowersBtn].forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
            currentSortType = type;
            loadShops(currentSortType);
        });
    }

    setupSortButton(sortLikesBtn, "likes");
    setupSortButton(sortFollowersBtn, "followers");

    // 加载商铺列表
    function loadShops(sortType) {
        fetch("http://TV/ShopService/list", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ sortType: sortType })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("收到商铺数据:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    pageHeader.textContent = "商铺列表";

                    shopListBody.innerHTML = ""; // 清空旧数据

                    if (records.length === 0) {
                        const row = document.createElement("tr");
                        row.innerHTML = `<td colspan="4">暂无商铺</td>`;
                        shopListBody.appendChild(row);
                        return;
                    }

                    records.forEach(shop => {
                        const row = document.createElement("tr");
                        row.innerHTML = `
                            <td>${shop.shopName}</td>
                            <td>${shop.shopLikes || 0}</td>
                            <td>${shop.shopFollowers || 0}</td>
                            <td><a href="user-shop.html?shopId=${shop.shopId}" class="btn small-btn">查看详细</a></td>
                        `;
                        shopListBody.appendChild(row);
                    });

                } else {
                    alert("获取商铺失败：" + (data.message || "数据异常"));
                    shopListBody.innerHTML = `<tr><td colspan="4">${data.message}</td></tr>`;
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法获取商铺列表，请检查网络或重试");
                shopListBody.innerHTML = "<tr><td colspan='4'>请求失败，检查网络或重试</td></tr>";
            });
    }

    // 点击退出登录
    document.getElementById("logout-btn").addEventListener("click", () => {
        if (confirm("确定要退出登录吗？")) {
            localStorage.removeItem("userId");
            localStorage.removeItem("token");
            localStorage.removeItem("refreshToken");
            window.location.href = "../index.html";
        }
    });

    // 点击注销账号
    document.getElementById("delete-user-btn").addEventListener("click", async (e) => {
        e.preventDefault();

        if (!confirm("⚠️ 确定要注销账号吗？此操作不可恢复")) {
            return;
        }

        const token = localStorage.getItem("token");
        if (!token) {
            alert("请先登录");
            window.location.href = "../index.html";
            return;
        }

        try {
            const response = await fetch("http://TV/UserService/delete", {
                method: "DELETE",
                headers: {
                    "Authorization": "Bearer " + token
                }
            });

            const result = await response.json();

            if (result.code === 200 && result.data?.message === "用户成功注销") {
                alert("✅ 账号已注销，即将返回首页");
                localStorage.clear(); // 清除所有 Token 和 Id
                setTimeout(() => {
                    window.location.href = "../../index.html";
                }, 1500);

            } else {
                alert("❌ 注销失败：" + (result.message || "未知错误"));
            }

        } catch (error) {
            console.error("注销请求出错:", error);
            alert("注销失败，请检查网络连接");
        }
    });

    // 初始加载
    loadShops(currentSortType);
});