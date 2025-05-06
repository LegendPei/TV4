document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get("userId");

    // 页面元素引用
    const usernameEl = document.getElementById("profile-username");
    const followingShopsEl = document.getElementById("profile-following-shops");
    const followersEl = document.getElementById("profile-followers");
    const followingUsersEl = document.getElementById("profile-following-users");

    const token = localStorage.getItem("token");

    if (!userId || !token) {
        alert("请先登录");
        window.location.href = "user-index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);

    // 设置跳转按钮的链接
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