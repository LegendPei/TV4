document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("update-form");
    const userAccountInput = document.getElementById("userAccount");
    const userPasswordInput = document.getElementById("userPassword");
    const userNameInput = document.getElementById("userName");

    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");

    if (!userId || !token) {
        alert("请先登录");
        window.location.href = "user-login.html";
        return;
    }

    // 初始化字段（如果需要回显用户当前信息，可从 /UserService/info 接口获取）
    // 这里暂时手动填写，后续可以封装成通用函数

    // 监听表单提交
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const userAccount = userAccountInput.value.trim();
        const userPassword = userPasswordInput.value.trim();
        const userName = userNameInput.value.trim();

        if (!userAccount || !userName) {
            alert("账号和昵称不能为空");
            return;
        }

        const payload = {
            userAccount,
            userName
        };

        // 如果有输入密码，则包含进去
        if (userPassword) {
            payload.userPassword = userPassword;
        }

        // 发起更新请求
        fetch("http://TV/UserService/update", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(payload)
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("更新响应:", data);

                if (data.code === 200 && data.data?.message === "用户信息更新成功") {
                    alert("✅ 信息更新成功！");
                    window.location.href = "user-index.html";
                } else {
                    alert("❌ 更新失败：" + (data.message || "数据异常"));
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("更新失败，请检查网络或重试");
            });
    });
});