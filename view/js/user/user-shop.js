document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const shopId = urlParams.get("shopId");

    // 页面元素引用
    const shopNameEl = document.getElementById("shop-name");
    const shopAddressEl = document.getElementById("shopAddress");
    const shopInfoEl = document.getElementById("shopInfo");
    const shopFollowersEl = document.getElementById("shopFollowers");
    const shopLikesEl = document.getElementById("shopLikes");

    const commentsListEl = document.getElementById("comments-list");

    const btnLikeShop = document.getElementById("btn-like-shop");
    const btnFollowShop = document.getElementById("btn-follow-shop");
    const btnUnfollowShop = document.getElementById("btn-unfollow-shop");

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!shopId || !token || !userId) {
        alert("请先登录或访问有效商铺");
        window.location.href = "user-index.html";
        return;
    }
    // 获取按钮并设置跳转路径
    const viewPostsBtn = document.getElementById("view-posts-btn");
    if (viewPostsBtn) {
        viewPostsBtn.href = `user-shop-posts.html?shopId=${shopId}`;
    }
    // 设置标题
    shopNameEl.textContent = "加载中...";

    // 获取商铺详情
    fetch(`http://TV/ShopService/info`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ shopId: parseInt(shopId, 10) })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到商铺详情:", data);

            if (data.code === 200 && data.data) {
                const shop = data.data;

                // 更新页面内容
                shopNameEl.textContent = shop.shopName || "未知商铺";
                shopAddressEl.textContent = shop.shopAddress || "-";
                shopInfoEl.textContent = shop.shopInfo || "暂无简介";
                shopFollowersEl.textContent = shop.shopFollowers || 0;
                shopLikesEl.textContent = shop.shopLikes || 0;

                // 加载评论
                loadComments("time");

            } else {
                alert("获取商铺详情失败：" + (data.message || "数据异常"));
                window.location.href = "user-index.html";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取商铺详情，请检查网络或重试");
            window.location.href = "user-index.html";
        });

    /**
     * 加载评论列表
     */
    function loadComments(sortType) {
        fetch("http://TV/CommentService/findAllTargetComments", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                comments: { targetId: parseInt(shopId, 10) },
                sortRequest: { sortType: sortType }
            })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(commentData => {
                console.log("收到评论数据:", commentData);

                if (commentData.code === 200 && Array.isArray(commentData.data?.data)) {
                    const records = commentData.data.data;

                    commentsListEl.innerHTML = ""; // 清空旧数据

                    if (records.length === 0) {
                        const row = document.createElement("li");
                        row.className = "list-group-item";
                        row.innerHTML = `<div>暂无评论</div>`;
                        commentsListEl.appendChild(row);
                        return;
                    }

                    records.forEach(comment => {
                        const item = document.createElement("li");
                        item.className = "list-group-item";

                        item.innerHTML = `
                            <div class="comment-header">
                                <span class="user-name">加载中...</span>
                                <span class="comment-time">${comment.commentTime}</span>
                            </div>
                            <div class="comment-body">${comment.commentContent}</div>
                            <div class="comment-footer">
                                👍 <span class="comment-likes">${comment.commentLikes || 0}</span>
                                <div class="comment-actions">
                                    <button class="btn small-btn" data-comment-id="${comment.commentId}">点赞</button>
                                </div>
                            </div>
                        `;

                        commentsListEl.appendChild(item);

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

                        // 绑定评论点赞按钮点击事件
                        item.querySelector("button").addEventListener("click", async () => {
                            const commentId = item.querySelector("button").dataset.commentId;

                            try {
                                const likeResponse = await fetch("http://TV/LikeService/likeComment", {
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "application/json",
                                        "Authorization": "Bearer " + token
                                    },
                                    body: JSON.stringify({
                                        targetId: parseInt(commentId, 10),
                                        likerId: parseInt(userId, 10)
                                    })
                                });

                                const likeData = await likeResponse.json();

                                if (likeData.code === 200) {
                                    const likesSpan = item.querySelector(".comment-likes");
                                    let currentLikes = parseInt(likesSpan.textContent, 10);
                                    likesSpan.textContent = currentLikes + 1;
                                    alert("✅ 评论已点赞");
                                } else {
                                    alert("❌ 点赞失败：" + (likeData.message || "未知错误"));
                                }

                            } catch (error) {
                                console.error("点赞请求出错:", error);
                                alert("点赞失败，请检查网络");
                            }
                        });
                    });

                } else {
                    const errorRow = document.createElement("li");
                    errorRow.className = "list-group-item";
                    errorRow.innerHTML = `<div>无法加载评论：${commentData.message}</div>`;
                    commentsListEl.innerHTML = "";
                    commentsListEl.appendChild(errorRow);
                }
            })
            .catch(error => {
                console.error("评论请求出错:", error);
                const errorRow = document.createElement("li");
                errorRow.className = "list-group-item";
                errorRow.innerHTML = "<div>请求失败，检查网络或重试</div>";
                commentsListEl.innerHTML = "";
                commentsListEl.appendChild(errorRow);
            });
    }

    /**
     * 点赞商铺
     */
    btnLikeShop.addEventListener("click", async () => {
        try {
            const response = await fetch("http://TV/LikeService/likeShop", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    targetId: parseInt(shopId, 10),
                    likerId: parseInt(userId, 10)
                })
            });

            const data = await response.json();

            if (data.code === 200 && data.data?.message === "商铺点赞成功") {
                let currentLikes = parseInt(shopLikesEl.textContent, 10);
                shopLikesEl.textContent = currentLikes + 1;
                alert("✅ 商铺点赞成功");
            } else {
                alert("❌ 点赞失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("请求出错:", err);
            alert("点赞失败，请检查网络连接");
        }
    });

    /**
     * 关注商铺
     */
    btnFollowShop.addEventListener("click", async () => {
        try {
            const response = await fetch("http://TV/FollowService/followShop", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    targetId: parseInt(shopId, 10),
                    followerId: parseInt(userId, 10)
                })
            });

            const data = await response.json();

            if (data.code === 200 && data.data?.message === "商铺关注成功") {
                let currentFollowers = parseInt(shopFollowersEl.textContent, 10);
                shopFollowersEl.textContent = currentFollowers + 1;
                alert("✅ 关注成功");
            } else {
                alert("❌ 关注失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("请求出错:", err);
            alert("关注失败，请检查网络连接");
        }
    });

    /**
     * 取消关注商铺
     */
    btnUnfollowShop.addEventListener("click", async () => {
        if (!confirm("确定要取消关注？")) return;

        try {
            const response = await fetch("http://TV/FollowService/unfollowShop", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    targetId: parseInt(shopId, 10),
                    followerId: parseInt(userId, 10)
                })
            });

            const data = await response.json();

            if (data.code === 200 && data.data?.message === "商铺取消关注成功") {
                let currentFollowers = parseInt(shopFollowersEl.textContent, 10);
                shopFollowersEl.textContent = Math.max(currentFollowers - 1, 0);
                alert("✅ 已取消关注");
            } else {
                alert("❌ 取消关注失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("请求出错:", err);
            alert("取消关注失败，请检查网络连接");
        }
    });
    // 获取评论相关元素
    const btnCommentShop = document.getElementById("btn-comment-shop");
    const commentFormContainer = document.getElementById("comment-form-container");
    const commentForm = document.getElementById("comment-form");
    const commentContentInput = document.getElementById("commentContent");

// 显示评论输入框
    btnCommentShop.addEventListener("click", (e) => {
        e.preventDefault();
        commentFormContainer.style.display = "block";
    });

// 监听评论表单提交
    commentForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const commentContent = commentContentInput.value.trim();

        if (!commentContent) {
            alert("评论内容不能为空");
            return;
        }

        try {
            const response = await fetch("http://TV/CommentService/addComment", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    commentContent: commentContent,
                    commenterId: parseInt(userId, 10),
                    targetId: parseInt(shopId, 10)
                })
            });

            const data = await response.json();

            if (data.code === 200 && data.data?.message === "评论插入成功") {
                alert("✅ 评论发布成功！");
                commentForm.reset(); // 清空表单
                commentFormContainer.style.display = "none"; // 隐藏输入框

                // ✅ 可选：重新加载评论
                loadComments(currentSortType);

            } else {
                alert("❌ 发布失败：" + (data.message || "数据异常"));
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法提交评论，请检查网络或重试");
        }
    });
});