document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get("userId");

    if (!userId) {
        alert("未找到用户 ID");
        window.location.href = "user-index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);
    const followList = document.getElementById("follow-list");

    // 获取关注的用户 ID 列表
    fetch("http://TV/FollowService/followingUsers", {
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
            console.log("收到关注的用户列表:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const followingUserIds = data.data.data;

                if (followingUserIds.length === 0) {
                    followList.innerHTML = "<li class='list-group-item'>您还未关注任何用户</li>";
                    return;
                }

                // 加载用户详情
                loadUserDetails(followingUserIds);

            } else {
                alert("获取关注用户失败：" + (data.message || "数据异常"));
                followList.innerHTML = "<li class='list-group-item'>获取失败</li>";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取关注的用户，请检查网络连接");
            followList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        });

    /**
     * 异步加载用户信息
     */
    async function loadUserDetails(userIds) {
        followList.innerHTML = "";
        for (const id of userIds) {
            try {
                const res = await fetch("http://TV/UserService/info", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ userId: id })
                });

                const userData = await res.json();

                if (userData.code === 200 && userData.data?.userName) {
                    const item = document.createElement("li");
                    item.className = "list-group-item";

                    item.innerHTML = `
                        <a href="user-profile.html?userId=${id}" class="btn small-btn">${userData.data.userName}</a>
                        <p>用户 ID：${id}</p>
                    `;

                    followList.appendChild(item);
                }

            } catch (err) {
                console.error(`获取用户 ${id} 失败`, err);
            }
        }

        if (followList.children.length === 0) {
            followList.innerHTML = "<li class='list-group-item'>加载失败</li>";
        }
    }
});