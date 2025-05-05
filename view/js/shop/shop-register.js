document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('register-form');

    form.addEventListener('submit', function (e) {
        e.preventDefault(); // 阻止默认提交行为

        // 获取表单数据
        const shopAccount = document.getElementById('shopAccount').value.trim();
        const shopPassword = document.getElementById('shopPassword').value.trim();
        const shopName = document.getElementById('shopName').value.trim();
        const shopAddress = document.getElementById('shopAddress').value.trim();
        const shopInfo = document.getElementById('shopInfo').value.trim();

        // 校验必填项
        if (!shopAccount || !shopPassword || !shopName || !shopAddress || !shopInfo) {
            alert("请填写所有字段");
            return;
        }

        // 构造请求体
        const body = JSON.stringify({
            shopAccount: shopAccount,
            shopPassword: shopPassword,
            shopName: shopName,
            shopAddress: shopAddress,
            shopInfo: shopInfo
        });

        // 发起 fetch 请求
        fetch("http://TV/ShopService/register", {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: body
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("注册失败，HTTP 状态码：" + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log("注册响应:", data);
                alert("注册成功！");
                window.location.href = "../index.html"; // 跳转到登录页
            })
            .catch(error => {
                console.error("注册出错:", error);
                alert("注册失败，请稍后再试。");
            });
    });
});