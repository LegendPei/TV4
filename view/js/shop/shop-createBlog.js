document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("create-blog-form");
    const blogNameInput = document.getElementById("blogName");
    const blogContentInput = document.getElementById("blogContent");
    const blogTypeInputs = document.querySelectorAll("input[name='blogType']");
    const blogMediaInput = document.getElementById("blogMedia");
    const mediaPreview = document.getElementById("media-preview");

    const shopId = localStorage.getItem("shopId");
    if (!shopId) {
        alert("请先登录");
        window.location.href = "shop-login.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);
    if (isNaN(numericShopId)) {
        alert("无效的店铺 ID");
        window.location.href = "shop-login.html";
        return;
    }

    // 文件预览逻辑
    blogMediaInput.addEventListener("change", () => {
        const file = blogMediaInput.files[0];
        mediaPreview.innerHTML = ""; // 清空旧预览

        if (file) {
            const reader = new FileReader();

            reader.onload = function (e) {
                let previewHTML = "";

                if (file.type.startsWith("image/")) {
                    previewHTML = `<img src="${e.target.result}" alt="预览" style="max-width: 100%; border-radius: 5px;">`;
                } else if (file.type.startsWith("video/")) {
                    previewHTML = `
                        <video controls style="max-width: 100%; border-radius: 5px;">
                            <source src="${e.target.result}" type="${file.type}">
                            您的浏览器不支持视频播放
                        </video>`;
                } else {
                    previewHTML = `<p>已选择文件：${file.name}</p>`;
                }

                mediaPreview.innerHTML = previewHTML;
            };

            reader.readAsDataURL(file);
        }
    });

    // 监听表单提交
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const blogName = blogNameInput.value.trim();
        const blogContent = blogContentInput.value.trim();

        if (!blogName || !blogContent) {
            alert("标题和内容不能为空");
            return;
        }

        let selectedType = 1;
        blogTypeInputs.forEach(input => {
            if (input.checked) {
                selectedType = parseInt(input.value, 10);
            }
        });

        let filePath = "0";

        // 文件上传逻辑
        const file = blogMediaInput.files[0];

        if (file) {
            const formData = new FormData();
            formData.append("file", file);

            try {
                const uploadResponse = await fetch("http://TV/BlogService/uploadBlogMedia", {
                    method: "POST",
                    body: formData
                });

                const uploadData = await uploadResponse.json();
                filePath = uploadData.url || "0";
            } catch (err) {
                alert("文件上传失败：" + err.message);
                console.error("上传错误:", err);
                return;
            }
        }

        // 构建请求体
        const payload = {
            targetId: 0,
            blogName: blogName,
            authorId: numericShopId,
            blogContent: blogContent,
            filePath: filePath,
            blogType: selectedType
        };

        // 提交至接口
        fetch("http://TV/BlogService/createBlog", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("创建动态响应:", data);

                if (data.code === 200 && data.data?.message === "创建动态成功") {
                    alert("✅ 动态发布成功！");
                    window.location.href = "shop-index.html";
                } else {
                    alert("❌ 发布失败：" + (data.message || "未知错误"));
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法发布动态，请检查网络或重试");
            });
    });
});