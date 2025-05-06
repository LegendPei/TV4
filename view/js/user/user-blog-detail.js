document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const blogId = urlParams.get("blogId");
    if (!blogId) {
        alert("未找到动态 ID，请重新查看");
        window.location.href = "user-posts.html"; // 不要跳到 user-index.html
        return;
    }
    // 页面元素引用
    const blogTitleEl = document.getElementById("blog-title");
    const blogContentEl = document.getElementById("blog-content");
    const blogTimeEl = document.getElementById("blog-time");
    const blogLikesEl = document.getElementById("blog-likes");
    const blogCollectionsEl = document.getElementById("blog-collections");
    const blogTypeEl = document.getElementById("blog-type");
    const mediaPreview = document.getElementById("media-preview");

    const commentsListEl = document.getElementById("comments-list");
    const commentSortTimeBtn = document.getElementById("comment-sort-time");
    const commentSortLikesBtn = document.getElementById("comment-sort-likes");

    const btnLikeBlog = document.getElementById("btn-like-blog");
    const btnCollectBlog = document.getElementById("btn-collect-blog");
    const btnUnCollectBlog = document.getElementById("btn-uncollect-blog");

    const btnCommentBlog = document.getElementById("btn-comment-blog");
    const commentFormContainer = document.getElementById("comment-form-container");
    const commentForm = document.getElementById("comment-form");
    const commentContentInput = document.getElementById("commentContent");

    let currentCommentSortType = "time"; // 默认排序方式为时间

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!blogId || !token || !userId) {
        alert("未找到动态 ID 或未登录");
        window.location.href = "user-index.html";
        return;
    }

    const numericBlogId = parseInt(blogId, 10);
    const numericUserId = parseInt(userId, 10);

    // 设置标题
    blogTitleEl.textContent = "加载中...";

    // 获取动态详情
    fetch("http://TV/BlogService/getBlogInfo", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ blogId: numericBlogId })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到动态详情:", data);

            if (data.code === 200 && data.data) {
                const blog = data.data;

                // 类型映射表（新增）
                const typeMap = {
                    1: "美食推荐",
                    2: "商铺推荐",
                    3: "秒杀分享"
                };

                // 更新页面内容
                blogTitleEl.textContent = blog.blogName || "无标题动态";
                blogContentEl.textContent = blog.blogContent || "暂无内容";
                blogTimeEl.textContent = blog.blogTime || "-";
                blogLikesEl.textContent = blog.blogLikes || "0";
                blogCollectionsEl.textContent = blog.blogCollections || "0";
                blogTypeEl.textContent = typeMap[blog.blogType] || "未知类型";

                // 如果有文件路径，展示媒体文件
                if (mediaPreview) {
                    if (blog.filePath && blog.filePath !== "0") {
                        const filePath = blog.filePath.trim().toLowerCase();

                        if (filePath.endsWith(".mp4") ||
                            filePath.endsWith(".webm") ||
                            filePath.endsWith(".ogg")) {
                            mediaPreview.innerHTML = `
                            <video controls style="max-width: 100%; border-radius: 5px;">
                                <source src="${blog.filePath}" type="video/mp4">
                                您的浏览器不支持视频播放。
                            </video>`;
                        } else {
                            mediaPreview.innerHTML = `<img src="${blog.filePath}" alt="动态媒体" style="max-width: 100%; border-radius: 5px;" />`;
                        }
                    } else {
                        mediaPreview.innerHTML = "<p>该动态没有附加媒体文件</p>";
                    }
                }

                // 加载评论
                loadComments(currentCommentSortType);

            } else {
                alert("获取动态失败：" + (data.message || "数据异常"));
                window.location.href = "user-shop-posts.html";
            }
        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取动态详情，请检查网络或重试");
            window.location.href = "user-posts.html";
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
                comments: { targetId: numericBlogId },
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
                                        likerId: numericUserId
                                    })
                                });

                                const likeData = await likeResponse.json();

                                if (likeData.code === 200) {
                                    let currentLikes = parseInt(item.querySelector(".comment-likes").textContent, 10);
                                    item.querySelector(".comment-likes").textContent = currentLikes + 1;
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
     * 切换评论排序方式
     */
    function setupCommentSortButton(button, type) {
        button.addEventListener("click", () => {
            [commentSortTimeBtn, commentSortLikesBtn].forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
            currentCommentSortType = type;
            commentsListEl.innerHTML = "<li class='list-group-item'>加载中...</li>";
            loadComments(type);
        });
    }

    setupCommentSortButton(commentSortTimeBtn, "time");
    setupCommentSortButton(commentSortLikesBtn, "likes");

    /**
     * 点赞该动态
     */
    btnLikeBlog.addEventListener("click", async () => {
        try {
            const response = await fetch("http://TV/LikeService/likeBlog", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    targetId: numericBlogId,
                    likerId: numericUserId
                })
            });

            const data = await response.json();

            if (data.code === 200) {
                let currentLikes = parseInt(blogLikesEl.textContent, 10);
                blogLikesEl.textContent = currentLikes + 1;
                alert("✅ 动态点赞成功");
            } else {
                alert("❌ 点赞失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("点赞请求出错:", err);
            alert("点赞失败，请检查网络连接");
        }
    });

    /**
     * 收藏该动态
     */
    btnCollectBlog.addEventListener("click", async () => {
        try {
            const response = await fetch("http://TV/BlogService/collectBlog", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    blogId: numericBlogId,
                    userId: numericUserId
                })
            });

            const data = await response.json();

            if (data.code === 200) {
                let currentCollections = parseInt(blogCollectionsEl.textContent, 10);
                blogCollectionsEl.textContent = currentCollections + 1;
                alert("✅ 动态收藏成功");
            } else {
                alert("❌ 收藏失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("收藏请求出错:", err);
            alert("收藏失败，请检查网络连接");
        }
    });

    /**
     * 取消收藏该动态
     */
    btnUnCollectBlog.addEventListener("click", async () => {
        if (!confirm("确定要取消收藏吗？")) return;

        try {
            const response = await fetch("http://TV/BlogService/unCollectBlog", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    blogId: numericBlogId,
                    userId: numericUserId
                })
            });

            const data = await response.json();

            if (data.code === 200) {
                let currentCollections = parseInt(blogCollectionsEl.textContent, 10);
                blogCollectionsEl.textContent = Math.max(currentCollections - 1, 0);
                alert("✅ 已取消收藏");
            } else {
                alert("❌ 取消收藏失败：" + (data.message || "未知错误"));
            }

        } catch (err) {
            console.error("请求出错:", err);
            alert("取消收藏失败，请检查网络连接");
        }
    });

    /**
     * 显示评论输入框（可复用 user-shop.js 的逻辑）
     */
    btnCommentBlog.addEventListener("click", (e) => {
        e.preventDefault();
        commentFormContainer.style.display = "block";
    });

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
                    commenterId: numericUserId,
                    targetId: numericBlogId
                })
            });

            const data = await response.json();

            if (data.code === 200 && data.data?.message === "评论插入成功") {
                alert("✅ 评论发布成功！");
                commentForm.reset(); // 清空表单
                commentFormContainer.style.display = "none"; // 隐藏输入框

                // ✅ 可选：重新加载评论
                loadComments(currentCommentSortType);

            } else {
                alert("❌ 发布失败：" + (data.message || "未知错误"));
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法提交评论，请检查网络或重试");
        }
    });
});