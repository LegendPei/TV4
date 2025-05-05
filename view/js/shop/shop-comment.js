document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const commentsList = document.getElementById("comments-list");

    const sortTimeBtn = document.getElementById("sort-time");
    const sortLikesBtn = document.getElementById("sort-likes");

    let currentSortType = "time"; // 默认排序方式

    // 获取店铺 ID
    const shopId = localStorage.getItem("shopId");
    if (!shopId) {
        alert("未找到商铺 ID，请重新登录");
        window.location.href = "shop-login.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);
    if (isNaN(numericShopId)) {
        alert("无效的商铺 ID");
        window.location.href = "shop-login.html";
        return;
    }

    // 设置标题
    pageHeader.textContent = "加载中...";

    // 切换排序方式
    sortTimeBtn.addEventListener("click", () => {
        sortTimeBtn.classList.add("active");
        sortLikesBtn.classList.remove("active");
        currentSortType = "time";
        loadComments(currentSortType);
    });

    sortLikesBtn.addEventListener("click", () => {
        sortLikesBtn.classList.add("active");
        sortTimeBtn.classList.remove("active");
        currentSortType = "likes";
        loadComments(currentSortType);
    });

    // 加载评论逻辑
    function loadComments(sortType) {
        fetch("http://TV/CommentService/findAllTargetComments", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                comments: { targetId: numericShopId },
                sortRequest: { sortType: sortType === "likes" ? "likes" : "time" }
            })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("收到评论数据:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    pageHeader.textContent = "商铺评论列表";

                    commentsList.innerHTML = "";

                    if (records.length === 0) {
                        commentsList.innerHTML = "<li class='list-group-item'>暂无评论</li>";
                        return;
                    }

                    records.forEach(comment => {
                        const item = document.createElement("li");
                        item.className = "list-group-item";

                        item.innerHTML = `
                            <div class="comment-header">
                                <span class="comment-user">加载中...</span>
                                <span class="comment-time">${comment.commentTime}</span>
                            </div>
                            <div class="comment-body">${comment.commentContent}</div>
                            <div class="comment-footer">
                                <span>👍 ${comment.commentLikes || 0}</span>
                            </div>
                        `;

                        commentsList.appendChild(item);

                        // 请求用户名
                        fetch("http://TV/UserService/info", {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({ userId: comment.commenterId })
                        })
                            .then(res => res.json())
                            .then(userData => {
                                const userSpan = item.querySelector(".comment-user");
                                if (userData.code === 200 && userData.data) {
                                    userSpan.textContent = userData.data.userName || "匿名用户";
                                } else {
                                    userSpan.textContent = "无法加载用户名";
                                }
                            })
                            .catch(err => {
                                console.error("获取用户名失败:", err);
                                item.querySelector(".comment-user").textContent = "加载失败";
                            });
                    });

                } else {
                    alert("获取评论失败：" + (data.message || "未知错误"));
                    commentsList.innerHTML = "<li class='list-group-item'>无法加载评论</li>";
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法获取评论，请检查网络或重试");
                commentsList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
            });
    }

    // 初始加载
    loadComments(currentSortType);
});