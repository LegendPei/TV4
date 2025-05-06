document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("register-form");
    const userAccountInput = document.getElementById("userAccount");
    const userPasswordInput = document.getElementById("userPassword");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const userNameInput = document.getElementById("userName");

    // 监听表单提交
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const userAccount = userAccountInput.value.trim();
        const userPassword = userPasswordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();
        const userName = userNameInput.value.trim();

        // 表单校验
        if (!userAccount || !userPassword || !userName) {
            alert("请填写所有必填字段！");
            return;
        }

        if (userPassword !== confirmPassword) {
            alert("两次输入的密码不一致，请重新输入");
            return;
        }

        // 构建请求体
        const payload = {
            userAccount,
            userPassword,
            userName
        };

        // 提交至接口
        fetch("http://TV/UserService/register", {
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
                console.log("注册响应:", data);

                if (data.code === 200 && data.data?.message === "用户成功注册") {
                    alert("✅ 注册成功，请登录！");
                    window.location.href = "user-login.html";
                } else {
                    alert("❌ 注册失败：" + (data.message || "未知错误"));
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法注册，请检查网络或重试");
            });
    });
});