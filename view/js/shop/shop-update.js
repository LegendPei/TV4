document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('update-shop-form');

    // 获取 token 和 shopId
    const token = localStorage.getItem('token');
    const shopId = localStorage.getItem('shopId');

    if (!token || !shopId) {
        alert("请先登录");
        window.location.href = "shop-login.html";
        return;
    }

    // 填充当前信息（通过 info 接口获取原始数据）
    fetch("http://TV/ShopService/info", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ shopId })
    })
        .then(response => {
            if (!response.ok) throw new Error("无法获取店铺信息：" + response.status);
            return response.json();
        })
        .then(data => {
            // 填充表单字段
            document.getElementById("shopAccount").value = data.shopAccount || "";
            document.getElementById("shopName").value = data.shopName || "";
            document.getElementById("shopAddress").value = data.shopAddress || "";
            document.getElementById("shopInfo").value = data.shopInfo || "";
        })
        .catch(err => {
            console.error("加载店铺信息失败:", err);
            alert("加载店铺信息失败，请重新登录");
            window.location.href = "shop-login.html";
        });

    // 监听表单提交
    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const shopAccount = document.getElementById('shopAccount').value.trim();
        const shopPassword = document.getElementById('shopPassword').value.trim();
        const shopName = document.getElementById('shopName').value.trim();
        const shopAddress = document.getElementById('shopAddress').value.trim();
        const shopInfo = document.getElementById('shopInfo').value.trim();

        // 构造请求体
        const body = JSON.stringify({
            shopAccount,
            shopPassword,
            shopName,
            shopAddress,
            shopInfo
        });

        // 发起更新请求
        fetch("http://TV/ShopService/update", {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: body
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("更新失败，HTTP 状态码：" + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log("更新响应:", data);
                if (data.message === "success") {
                    alert("更新成功！");
                    window.location.href = "shop-index.html";
                } else {
                    alert("更新失败：" + data.message);
                }
            })
            .catch(error => {
                console.error("更新出错:", error);
                alert("更新失败，请稍后再试。");
            });
    });
});