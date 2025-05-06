document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("login-form");
    const userAccountInput = document.getElementById("userAccount");
    const userPasswordInput = document.getElementById("userPassword");

    // 监听表单提交
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const userAccount = userAccountInput.value.trim();
        const userPassword = userPasswordInput.value.trim();

        if (!userAccount || !userPassword) {
            alert("账号和密码不能为空");
            return;
        }

        const payload = {
            userAccount: userAccount,
            userPassword: userPassword
        };

        try {
            const response = await fetch("http://TV/UserService/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error("网络响应异常：" + response.status);
            }

            const data = await response.json();

            console.log("登录响应:", data);

            if (data.code === 200 && data.data) {
                const { userId, token, refreshToken } = data.data;

                // ✅ 存入 localStorage
                localStorage.setItem("userId", userId);
                localStorage.setItem("token", token);
                localStorage.setItem("refreshToken", refreshToken);

                // ✅ 跳转至用户主页
                window.location.href = "user-index.html";

            } else {
                alert("登录失败：" + (data.message || "账号或密码错误"));
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("登录失败，请检查网络或重试");
        }
    });
});