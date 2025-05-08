document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const blogTypeSelect = document.getElementById("blogType");
    const shopSelector = document.getElementById("shop-selector");
    const voucherSelector = document.getElementById("voucher-selector");

    const shopSelect = document.getElementById("shop-select");
    const couponSelect = document.getElementById("coupon-select");

    const blogNameInput = document.getElementById("blogName");
    const blogContentInput = document.getElementById("blogContent");
    const mediaFileInput = document.getElementById("mediaFile");
    const uploadStatus = document.getElementById("upload-status");

    const form = document.getElementById("create-blog-form");

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId || !token) {
        alert("请先登录");
        window.location.href = "user-index.html";
        return;
    }

    const numericUserId = parseInt(userId, 10);

    let filePath = "0"; // 默认值为无文件

    /**
     * 动态切换输入框显示
     */
    function handleTypeChange() {
        const selectedType = parseInt(blogTypeSelect.value, 10);

        // 隐藏所有 selector
        [shopSelector, voucherSelector].forEach(el => el.classList.add("hidden"));

        // 显示对应 selector
        if (selectedType === 2 && shopSelector) {
            shopSelector.classList.remove("hidden");
        } else if (selectedType === 3 && voucherSelector) {
            voucherSelector.classList.remove("hidden");
        }
    }

    blogTypeSelect.addEventListener("change", handleTypeChange);
    handleTypeChange(); // 初始化一次

    /**
     * 加载商铺列表
     */
    async function loadShops() {
        try {
            const response = await fetch("http://TV/ShopService/list", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sortType: "time" })
            });

            const data = await response.json();

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                shopSelect.innerHTML = ""; // 清空旧数据

                data.data.data.forEach(shop => {
                    const option = document.createElement("option");
                    option.value = shop.shopId;
                    option.textContent = shop.shopName;
                    shopSelect.appendChild(option);
                });

            } else {
                console.error("获取商铺失败:", data.message);
            }

        } catch (error) {
            console.error("请求出错:", error);
        }
    }

    /**
     * 加载进行中的秒杀活动
     */
    async function loadCoupons() {
        try {
            const response = await fetch("http://TV/VoucherService/getALLSecKillInfo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sortType: "ongoing" }) // 可以是 ongoing/upcoming/expired
            });

            const data = await response.json();

            if (data.code === 200 && Array.isArray(data.data?.data)) {
                couponSelect.innerHTML = ""; // 清空旧数据

                data.data.data.forEach(coupon => {
                    const option = document.createElement("option");
                    option.value = coupon.couponId;
                    option.textContent = coupon.couponName;
                    couponSelect.appendChild(option);
                });

            } else {
                console.error("获取优惠券失败:", data.message);
            }

        } catch (error) {
            console.error("请求出错:", error);
        }
    }

    // 初始加载商铺和优惠券
    if (shopSelect) loadShops();
    if (couponSelect) loadCoupons();

    /**
     * 表单提交逻辑
     */
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const blogType = parseInt(blogTypeSelect.value, 10);

        let targetId = 0;

        // ✅ 根据类型获取 targetId
        if (blogType === 2) {
            targetId = parseInt(shopSelect.value, 10);
            if (isNaN(targetId) || targetId <= 0) {
                alert("请选择有效的商铺 ID");
                return;
            }
        } else if (blogType === 3) {
            targetId = parseInt(couponSelect.value, 10);
            if (isNaN(targetId) || targetId <= 0) {
                alert("请选择有效的秒杀活动");
                return;
            }
        }

        const blogName = blogNameInput.value.trim();
        const blogContent = blogContentInput.value.trim();

        // 验证基本字段
        if (!blogName || !blogContent) {
            alert("标题和内容不能为空");
            return;
        }

        // ✅ 文件上传逻辑
        if (mediaFileInput.files[0]) {
            const formData = new FormData();
            formData.append("file", mediaFileInput.files[0]);

            try {
                const uploadResponse = await fetch("http://TV/BlogService/uploadBlogMedia", {
                    method: "POST",
                    headers: {
                        // 注意：使用 Authorization 时去掉 Bearer（如果你的服务端不需要 token 类型）
                        "Authorization": token
                    },
                    body: formData
                });

                const uploadData = await uploadResponse.json();

                // ✅ 修改关键点：直接从 uploadData.url 取值
                if (uploadData && uploadData.url) {
                    filePath = uploadData.url; // 直接使用 url 字段
                } else {
                    alert("❌ 文件上传失败：" + (uploadData.message || "未返回有效文件路径"));
                    return;
                }

            } catch (err) {
                console.error("文件上传出错:", err);
                alert("无法上传文件，请检查网络连接");
                return;
            }
        }

        // ✅ 构建请求体
        const payload = {

                targetId,
                blogName,
                authorId: numericUserId,
                blogContent,
                filePath,
                blogType

        };

        console.log("发送请求:", payload);

        try {
            const response = await fetch("http://TV/BlogService/createBlog", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (result.code === 200) {
                alert("✅ 动态发布成功！");
                window.location.href = "user-posts.html?tab=my-blogs";
            } else {
                alert("❌ 发布失败：" + (result.message || "未知错误"));
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法提交，请检查网络或重试");
        }
    });

    /**
     * 实时预览上传的文件名
     */
    mediaFileInput.addEventListener("change", () => {
        const file = mediaFileInput.files[0];
        uploadStatus.textContent = file ? `已选择：${file.name}` : "未选择文件";
    });
});