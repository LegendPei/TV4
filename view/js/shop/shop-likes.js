document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const likesTable = document.getElementById("likes-table");

    const shopId = localStorage.getItem("shopId");
    if (!shopId) {
        alert("未找到店铺 ID，请重新登录");
        window.location.href = "shop-login.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);
    if (isNaN(numericShopId)) {
        alert("无效的店铺 ID");
        window.location.href = "shop-login.html";
        return;
    }

    // 设置标题
    pageHeader.textContent = "加载中...";

    // 请求点赞列表
    fetch("http://TV/LikeService/likeShopList", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ targetId: numericShopId })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("点赞列表响应:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const records = data.data.data;

                // ✅ 不再从 message 提取 shopName
                pageHeader.textContent = "商铺点赞记录";

                likesTable.innerHTML = ""; // 清空旧数据

                if (records.length === 0) {
                    likesTable.innerHTML = "<div>暂无点赞记录</div>";
                    return;
                }

                // 对每条记录发起请求获取用户名
                records.forEach(like => {
                    const item = document.createElement("div");
                    item.className = "like-item";

                    item.innerHTML = `
                        <div class="user-info">
                            <span class="user-name">加载中...</span>
                        </div>
                        <div class="like-time">${like.likeTime}</div>
                    `;

                    likesTable.appendChild(item);

                    // 请求用户信息
                    fetch("http://TV/UserService/info", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({ userId: like.likerId })
                    })
                        .then(res => res.json())
                        .then(userData => {
                            if (userData.code === 200 && userData.data) {
                                const userNameEl = item.querySelector(".user-name");
                                userNameEl.textContent = userData.data.userName || "匿名用户";
                            } else {
                                item.querySelector(".user-name").textContent = "加载失败";
                                console.warn("用户信息查询失败:", userData);
                            }
                        })
                        .catch(err => {
                            item.querySelector(".user-name").textContent = "加载失败";
                            console.error("获取用户信息失败:", err);
                        });
                });

            } else {
                alert("获取点赞记录失败：" + (data.message || "数据异常"));
                likesTable.innerHTML = "<div>无法加载点赞记录</div>";
            }

        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取点赞记录，请稍后再试。");
            likesTable.innerHTML = "<div>请求失败，检查网络或重试</div>";
        });
});