document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const postsList = document.getElementById("posts-list");

    const sortTimeBtn = document.getElementById("sort-time");
    const sortLikesBtn = document.getElementById("sort-likes");
    const sortCollectionsBtn = document.getElementById("sort-collections");

    let currentSortType = "time"; // 默认排序方式为 time

    // 获取 URL 中的 shopId
    const urlParams = new URLSearchParams(window.location.search);
    const shopId = urlParams.get("shopId");

    if (!shopId) {
        alert("未找到商铺 ID，请重新登录");
        window.location.href = "../index.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);
    if (isNaN(numericShopId)) {
        alert("无效的商铺 ID");
        window.location.href = "../index.html";
        return;
    }

    // 设置标题
    pageHeader.textContent = "加载中...";

    // 绑定排序按钮点击事件
    function setupSortButton(button, type) {
        button.addEventListener("click", () => {
            [sortTimeBtn, sortLikesBtn, sortCollectionsBtn].forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
            currentSortType = type;

            // 清空旧数据并重新加载
            postsList.innerHTML = "<li class='list-group-item'>加载中...</li>";
            loadPosts(type);
        });
    }

    setupSortButton(sortTimeBtn, "time");
    setupSortButton(sortLikesBtn, "likes");
    setupSortButton(sortCollectionsBtn, "collections");

    // 加载商铺动态
    function loadPosts(sortType) {
        fetch("http://TV/BlogService/getShopBlogs", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                blog: { authorId: numericShopId },
                sortMode: { sortType: sortType }
            })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("收到商铺动态数据:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    pageHeader.textContent = "商铺动态列表";

                    postsList.innerHTML = ""; // 清空旧数据

                    if (records.length === 0) {
                        const row = document.createElement("li");
                        row.className = "list-group-item";
                        row.innerHTML = `<div class="no-data">暂无动态</div>`;
                        postsList.appendChild(row);
                        return;
                    }

                    records.forEach(blog => {
                        const item = document.createElement("li");
                        item.className = "list-group-item";

                        // 类型映射
                        const blogTypeMap = {
                            1: "美食推荐",
                            2: "商铺推荐",
                            3: "秒杀分享"
                        };

                        const blogTypeText = blogTypeMap[blog.blogType] || "未知类型";

                        // 构建 HTML 模板
                        item.innerHTML = `
                                        <div class="post-info">
                                            <div class="post-title">${blog.blogName}</div>
                                            <div class="post-meta">
                                                ⏱️ ${blog.blogTime} | 👍 ${blog.blogLikes || 0} | 💾 ${blog.blogCollections || 0}
                                            </div>
                                        </div>
                                        <div class="post-actions">
                                            <a href="user-blog-detail.html?blogId=${blog.blogId}" class="btn small-btn">查看详情</a>
                                        </div>
                                    `;


                        postsList.appendChild(item);
                    });

                } else {
                    alert("获取商铺动态失败：" + (data.message || "数据异常"));
                    postsList.innerHTML = `<li class="list-group-item">${data.message}</li>`;
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法获取商铺动态，请检查网络或重试");
                postsList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
            });
    }

    // 初始加载
    loadPosts(currentSortType);
});