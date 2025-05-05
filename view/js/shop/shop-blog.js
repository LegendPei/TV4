document.addEventListener('DOMContentLoaded', () => {
    // 获取 URL 中的 blogId
    const urlParams = new URLSearchParams(window.location.search);
    const blogId = urlParams.get('blogId');

    if (!blogId) {
        alert("未找到动态 ID");
        window.location.href = "shop-posts.html";
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
    const sortTimeBtn = document.getElementById("sort-time");
    const sortLikesBtn = document.getElementById("sort-likes");

    let currentSortType = "time"; // 默认排序方式

    // 类型映射表
    const typeMap = {
        1: "美食推荐",
        2: "商铺推荐",
        3: "秒杀分享"
    };

    // 请求动态详情接口
    fetch(`http://TV/BlogService/getBlogInfo`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ blogId: parseInt(blogId, 10) })
    })
        .then(response => {
            if (!response.ok) throw new Error("网络响应异常：" + response.status);
            return response.json();
        })
        .then(data => {
            console.log("收到动态详情:", data);

            if (data.code === 200 && data.data) {
                const blog = data.data;

                // 设置页面内容
                blogTitleEl.textContent = blog.blogName || "无标题动态";
                blogContentEl.textContent = blog.blogContent || "暂无内容";
                blogTimeEl.textContent = blog.blogTime || "-";
                blogLikesEl.textContent = blog.blogLikes || "0";
                blogCollectionsEl.textContent = blog.blogCollections || "0";
                blogTypeEl.textContent = typeMap[blog.blogType] || "未知类型";

                // 展示媒体文件（图片或视频）
                if (blog.filePath && blog.filePath !== "0") {
                    const filePath = blog.filePath;

                    if (
                        filePath.toLowerCase().endsWith(".mp4") ||
                        filePath.toLowerCase().endsWith(".webm") ||
                        filePath.toLowerCase().endsWith(".ogg")
                    ) {
                        mediaPreview.innerHTML = `
                            <video controls style="max-width: 100%; border-radius: 8px;">
                                <source src="${filePath}" type="video/mp4">
                                您的浏览器不支持视频播放。
                            </video>`;
                    } else {
                        mediaPreview.innerHTML = `
                            <img src="${filePath}" alt="动态媒体" style="max-width: 100%; border-radius: 8px;" />`;
                    }
                } else {
                    mediaPreview.innerHTML = "<p>该动态没有附加媒体文件</p>";
                }

                // 加载评论
                loadComments(currentSortType);

            } else {
                alert("获取动态详情失败：" + (data.message || "数据异常"));
                window.location.href = "shop-posts.html";
            }

        })
        .catch(error => {
            console.error("请求出错:", error);
            alert("无法获取动态详情，请检查网络或重试");
            window.location.href = "shop-posts.html";
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
                comments: { targetId: parseInt(blogId, 10) },
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
                        const noItem = document.createElement("div");
                        noItem.className = "table-row";
                        noItem.innerHTML = `
                        <div class="table-cell" style="text-align: center;">暂无评论</div>
                    `;
                        commentsListEl.appendChild(noItem);
                        return;
                    }

                    records.forEach(comment => {
                        const row = document.createElement("div");
                        row.className = "table-row";

                        row.innerHTML = `
                        <div class="table-cell">${comment.commenterName || "加载中..."}</div>
                        <div class="table-cell">${comment.commentContent}</div>
                        <div class="table-cell time">${comment.commentTime}</div>
                        <div class="table-cell likes">👍 ${comment.commentLikes || 0}</div>
                    `;

                        commentsListEl.appendChild(row);

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
                                    row.querySelector(".table-cell").textContent = userData.data.userName || "匿名用户";
                                } else {
                                    row.querySelector(".table-cell").textContent = "加载失败";
                                }
                            })
                            .catch(err => {
                                row.querySelector(".table-cell").textContent = "加载失败";
                                console.error("获取用户名失败:", err);
                            });
                    });

                } else {
                    const errorRow = document.createElement("div");
                    errorRow.className = "table-row";
                    errorRow.innerHTML = `<div class="table-cell" style="text-align: center;">无法加载评论</div>`;
                    commentsListEl.innerHTML = "";
                    commentsListEl.appendChild(errorRow);
                }
            })
            .catch(error => {
                console.error("评论请求出错:", error);
                const errorRow = document.createElement("div");
                errorRow.className = "table-row";
                errorRow.innerHTML = `<div class="table-cell" style="text-align: center;">请求失败，请检查网络或重试</div>`;
                commentsListEl.innerHTML = "";
                commentsListEl.appendChild(errorRow);
            });
    }

    /**
     * 绑定排序按钮点击事件
     */
    function setupSortButton(button, type) {
        button.addEventListener("click", () => {
            [sortTimeBtn, sortLikesBtn].forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
            currentSortType = type;

            // 重新加载评论
            loadComments(type);
        });
    }

    // 初始化排序按钮
    setupSortButton(sortTimeBtn, "time");
    setupSortButton(sortLikesBtn, "likes");
});