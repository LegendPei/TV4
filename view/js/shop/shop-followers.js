document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const followersTable = document.getElementById("followers-table");

    // 从 localStorage 获取店铺 ID
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

    // 请求粉丝列表接口
    fetch("http://TV/FollowService/shopFollowed", {
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
            console.log("粉丝列表响应:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const records = data.data.data;

                // 设置标题
                pageHeader.textContent = "关注该商铺的粉丝";

                // 清空旧数据
                followersTable.innerHTML = "";

                if (records.length === 0) {
                    followersTable.innerHTML = "<div>暂无粉丝</div>";
                    return;
                }

                // 遍历 userId 列表（注意这里不再是 follow.likerId）
                records.forEach(userId => {
                    const item = document.createElement("div");
                    item.className = "like-item";

                    item.innerHTML = `
                        <div class="user-info">
                            <span class="user-name">加载中...</span>
                        </div>
                    `;

                    followersTable.appendChild(item);

                    // 请求用户信息
                    fetch("http://TV/UserService/info", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({ userId: userId })
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
                alert("获取粉丝列表失败：" + (data.message || "数据异常"));
                followersTable.innerHTML = "<div>无法加载粉丝列表</div>";
            }

        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取粉丝列表，请稍后再试。");
            followersTable.innerHTML = "<div>请求失败，检查网络或重试</div>";
        });
});