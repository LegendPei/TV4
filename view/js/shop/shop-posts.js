document.addEventListener('DOMContentLoaded', () => {
    const pageHeader = document.getElementById("page-title");
    const postsList = document.getElementById("posts-list");

    const sortTimeBtn = document.getElementById("sort-time");
    const sortLikesBtn = document.getElementById("sort-likes");
    const sortCollectionsBtn = document.getElementById("sort-collections");

    let currentSortType = "time"; // 默认排序方式为时间

    // 获取店铺 ID
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

    // 切换排序逻辑
    function setupSortButton(button, type) {
        button.addEventListener("click", () => {
            // 更新激活状态
            [sortTimeBtn, sortLikesBtn, sortCollectionsBtn].forEach(btn => {
                btn.classList.remove("active");
            });
            button.classList.add("active");

            // 修改当前排序方式
            currentSortType = type;

            // 重新加载数据
            loadPosts(currentSortType);
        });
    }

    setupSortButton(sortTimeBtn, "time");
    setupSortButton(sortLikesBtn, "likes");
    setupSortButton(sortCollectionsBtn, "collections");

    // 加载动态逻辑
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
                console.log("收到动态数据:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    pageHeader.textContent = "商铺动态列表";

                    postsList.innerHTML = "";

                    if (records.length === 0) {
                        postsList.innerHTML = "<li class='list-group-item'>暂无动态</li>";
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

                        // 构建动态项 HTML
                        item.innerHTML = `
                            <div class="post-info">
                                <div class="post-title">${blog.blogName}</div>
                                <div class="post-meta">
                                    ⏱️ ${blog.blogTime} | 👍 ${blog.blogLikes || 0} | 💾 ${blog.blogCollections || 0} | 🧾 ${blogTypeText}
                                </div>
                            </div>
                            <div class="post-actions">
                                <a href="shop-post-detail.html?blogId=${blog.blogId}" class="btn small-btn">查看详情</a>
                            </div>
                        `;

                        postsList.appendChild(item);
                    });

                } else {
                    alert("获取动态失败：" + (data.message || "未知错误"));
                    postsList.innerHTML = "<li class='list-group-item'>无法加载动态</li>";
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法获取商铺动态，请稍后再试。");
                postsList.innerHTML = "<li class='list-group-item'>请求失败，请检查网络或重试</li>";
            });
    }

    // 初始加载
    loadPosts(currentSortType);
});