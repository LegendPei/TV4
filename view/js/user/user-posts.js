document.addEventListener('DOMContentLoaded', () => {
    const postList = document.getElementById("post-list");
    const pageHeader = document.getElementById("page-title");

    const tabMyBlogsBtn = document.getElementById("tab-my-blogs");
    const tabCollectedBlogsBtn = document.getElementById("tab-collected-blogs");
    const tabFollowingBlogsBtn = document.getElementById("tab-following-blogs");

    let currentTab = "my-blogs"; // 默认展示“我的动态”

    const typeMap = {
        1: "美食推荐",
        2: "商铺推荐",
        3: "秒杀分享"
    };

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId || !token) {
        alert("请先登录");
        window.location.href = "../index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);

    /**
     * 显示动态列表
     */
    function showPosts(posts) {
        postList.innerHTML = ""; // 清空旧数据

        if (posts.length === 0) {
            const row = document.createElement("li");
            row.className = "list-group-item";
            row.innerHTML = "<div>暂无动态</div>";
            postList.appendChild(row);
            return;
        }

        posts.forEach(blog => {
            const item = document.createElement("li");
            item.className = "list-group-item";

            // 类型映射
            const typeName = typeMap[blog.blogType] || "未知类型";

            // 构建动态项 HTML
            item.innerHTML = `
                <div class="post-header">
                    <strong>${blog.blogName}</strong> 
                    <span class="post-time">${blog.blogTime}</span>
                </div>
                <div class="post-body">
                    <p>类型：${typeName}</p>
                    <p>点赞数：${blog.blogLikes || 0} | 收藏数：${blog.blogCollections || 0}</p>
                </div>
                <div class="post-actions">
                    <a href="user-blog-detail.html?blogId=${blog.blogId}" class="btn small-btn">查看详情</a>
                </div>
            `;

            postList.appendChild(item);
        });
    }

    /**
     * 请求我的动态
     */
    async function fetchMyBlogs() {
        try {
            const response = await fetch("http://TV/BlogService/getUserBlogs", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    blog: { authorId: numericUserId },
                    sortMode: { sortType: "time" }
                })
            });

            const data = await response.json();

            console.log("我的动态响应:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                showPosts(data.data.data);
            } else {
                alert("获取我的动态失败：" + (data.message || "数据异常"));
                postList.innerHTML = "<li class='list-group-item'>获取失败</li>";
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法获取我的动态，请检查网络或重试");
            postList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        }
    }

    /**
     * 请求收藏的动态
     */
    async function fetchCollectedBlogs() {
        try {
            const response = await fetch("http://TV/BlogService/getUserCollectBlogs", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({ userId: numericUserId })
            });

            const data = await response.json();

            console.log("收藏的动态响应:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                const collectedBlogIds = data.data.data.map(item => item.blogId);

                if (collectedBlogIds.length === 0) {
                    postList.innerHTML = "<li class='list-group-item'>暂无收藏的动态</li>";
                    return;
                }

                // ✅ 异步加载每条动态的详细信息
                loadBlogDetails(collectedBlogIds);

            } else {
                alert("获取收藏动态失败：" + (data.message || "数据异常"));
                postList.innerHTML = "<li class='list-group-item'>获取失败</li>";
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法获取收藏的动态，请检查网络连接");
            postList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        }
    }

    /**
     * 批量获取每篇收藏动态的详细信息
     */
    async function loadBlogDetails(blogIds) {
        postList.innerHTML = "<li class='list-group-item'>加载中...</li>";

        const typeMap = {
            1: "美食推荐",
            2: "商铺推荐",
            3: "秒杀分享"
        };

        const posts = [];

        for (const blogId of blogIds) {
            try {
                const blogResponse = await fetch("http://TV/BlogService/getBlogInfo", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": "Bearer " + token
                    },
                    body: JSON.stringify({ blogId: parseInt(blogId, 10) })
                });

                const blogData = await blogResponse.json();

                if (blogData.code === 200 && blogData.data) {
                    // ✅ 在 blog 对象中加入原始 blogId
                    const blogWithId = {
                        ...blogData.data,
                        blogId: blogId
                    };
                    posts.push(blogWithId);
                }

            } catch (err) {
                console.error(`获取 blogId=${blogId} 失败`, err);
            }
        }

        // 清空旧数据并重新渲染
        postList.innerHTML = "";

        if (posts.length === 0) {
            const row = document.createElement("li");
            row.className = "list-group-item";
            row.textContent = "暂无动态";
            postList.appendChild(row);
            return;
        }

        posts.forEach(blog => {
            const item = document.createElement("li");
            item.className = "list-group-item";

            const typeName = typeMap[blog.blogType] || "未知类型";

            item.innerHTML = `
            <div class="post-header">
                <strong>${blog.blogName}</strong> 
                <span class="post-time">${blog.blogTime}</span>
            </div>
            <div class="post-body">
                <p>类型：${typeName}</p>
                <p>点赞数：${blog.blogLikes || 0} | 收藏数：${blog.blogCollections || 0}</p>
            </div>
            <div class="post-actions">
                <!-- ✅ 使用原始 blogId -->
                <a href="user-blog-detail.html?blogId=${blog.blogId}" class="btn small-btn">查看详情</a>
            </div>
        `;

            postList.appendChild(item);
        });
    }

    /**
     * 请求关注的人和商铺的动态（时间线）
     */
    async function fetchFollowingBlogs() {
        try {
            const response = await fetch("http://TV/BlogService/getTimeline", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({ userId: numericUserId }) // 只传 userId 即可
            });

            const data = await response.json();

            console.log("关注的动态响应:", data);

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                showPosts(data.data.data); // 显示动态列表
            } else {
                alert("获取关注的动态失败：" + (data.message || "数据异常"));
                postList.innerHTML = "<li class='list-group-item'>获取失败</li>";
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法获取关注的动态，请检查网络连接");
            postList.innerHTML = "<li class='list-group-item'>请求失败，请稍后再试</li>";
        }
    }

    /**
     * 切换 Tab 的函数
     */
    function setupTab(tabButton, tabKey) {
        tabButton.addEventListener("click", () => {
            // 移除其他按钮的 active 状态
            [tabMyBlogsBtn, tabCollectedBlogsBtn, tabFollowingBlogsBtn].forEach(btn => btn.classList.remove("active"));

            // 设置当前按钮 active
            tabButton.classList.add("active");

            // 更新 tab 并加载对应内容
            currentTab = tabKey;
            loadPosts();
        });
    }

    setupTab(tabMyBlogsBtn, "my-blogs");
    setupTab(tabCollectedBlogsBtn, "collected-blogs");
    setupTab(tabFollowingBlogsBtn, "following-blogs");

    /**
     * 加载当前 Tab 的内容
     */
    function loadPosts() {
        postList.innerHTML = "<li class='list-group-item'>加载中...</li>";

        switch (currentTab) {
            case "my-blogs":
                fetchMyBlogs();
                break;
            case "collected-blogs":
                fetchCollectedBlogs();
                break;
            case "following-blogs":
                fetchFollowingBlogs();
                break;
            default:
                postList.innerHTML = "<li class='list-group-item'>无效的 Tab 类型</li>";
        }
    }

    // 初始加载
    loadPosts();
});