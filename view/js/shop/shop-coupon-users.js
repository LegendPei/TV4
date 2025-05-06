document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const couponId = urlParams.get("couponId");

    const pageHeader = document.getElementById("page-title");
    const usersListEl = document.getElementById("users-list");

    if (!couponId) {
        alert("未找到优惠券 ID");
        window.location.href = "shop-flashsale.html";
        return;
    }

    // 设置标题
    pageHeader.textContent = "加载中...";

    // 请求参与用户 ID 列表
    fetch("http://TV/VoucherService/getCouponUsersId", {
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
            console.log("收到参与用户数据:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const userIds = data.data.data;

                pageHeader.textContent = "参与用户列表";

                if (userIds.length === 0) {
                    usersListEl.innerHTML = "<li class='list-group-item'>暂无用户参与</li>";
                    return;
                }

                // 异步获取每个用户的信息
                userIds.forEach(userId => {
                    const item = document.createElement("li");
                    item.className = "list-group-item";
                    item.innerHTML = `
                        <div class="user-info">
                            <span class="user-name">加载中...</span>
                        </div>
                    `;
                    usersListEl.appendChild(item);

                    // 请求用户名
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
                                item.querySelector(".user-name").textContent = userData.data.userName || "匿名用户";
                            } else {
                                item.querySelector(".user-name").textContent = "加载失败";
                            }
                        })
                        .catch(err => {
                            item.querySelector(".user-name").textContent = "加载失败";
                            console.error("获取用户名失败:", err);
                        });
                });

            } else {
                alert("获取用户列表失败：" + (data.message || "数据异常"));
                usersListEl.innerHTML = `<li class="list-group-item">${data.message}</li>`;
            }

        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取参与用户，请检查网络或重试");
            usersListEl.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        });
});