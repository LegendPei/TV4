document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('login-form');

    form.addEventListener('submit', function (e) {
        e.preventDefault(); // 阻止默认提交行为

        // 获取输入值
        const shopAccount = document.getElementById('shopAccount').value.trim();
        const shopPassword = document.getElementById('shopPassword').value.trim();

        // 校验必填项
        if (!shopAccount || !shopPassword) {
            alert("请填写账号和密码");
            return;
        }

        // 构造请求体
        const body = JSON.stringify({
            shopAccount: shopAccount,
            shopPassword: shopPassword
        });

        // 发起 fetch 请求
        fetch("http://TV/ShopService/login", {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: body
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("登录失败，HTTP 状态码：" + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log("登录响应:", data);

                // ✅ 改为使用 data.data.token
                if (data.data && data.data.token && data.data.refreshToken) {
                    const { token, refreshToken, shopId } = data.data;

                    // ✅ 存入 localStorage
                    localStorage.setItem('token', token);
                    localStorage.setItem('refreshToken', refreshToken);
                    localStorage.setItem('shopId', shopId);

                    alert("登录成功！");
                    window.location.href = "../../html/shop/shop-index.html"; // 跳转到商铺首页
                } else {
                    alert("登录失败：" + (data.message || "缺少 token 或 refreshToken"));
                }
            })
            .catch(error => {
                console.error("登录出错:", error);
                alert("登录失败，请稍后再试。");
            });
    });
});