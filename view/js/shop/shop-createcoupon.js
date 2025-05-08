document.addEventListener('DOMContentLoaded', () => {
    const couponNameInput = document.getElementById("couponName");
    const couponTypeSelect = document.getElementById("couponType");
    const minSpendInput = document.getElementById("minSpend");
    const totalStockInput = document.getElementById("totalStock");
    const startTimeInput = document.getElementById("startTime");
    const endTimeInput = document.getElementById("endTime");
    const maxPerUserInput = document.getElementById("maxPerUser");

    const form = document.getElementById("create-coupon-form");

    // 获取店铺 ID
    const shopId = localStorage.getItem("shopId");
    if (!shopId) {
        alert("请先登录");
        window.location.href = "shop-login.html";
        return;
    }

    const numericShopId = parseInt(shopId, 10);
    if (isNaN(numericShopId)) {
        alert("无效的商铺 ID");
        window.location.href = "shop-login.html";
        return;
    }

    /**
     * 时间格式化函数（将 datetime-local 转成后端接受的格式）
     */
    function formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return "";

        // 将 '2025-05-07T18:48' → '2025-05-07 18:48:00'
        const date = new Date(dateTimeStr);

        if (isNaN(date.getTime())) {
            throw new Error("无效的时间格式：" + dateTimeStr);
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    /**
     * 根据优惠券类型动态更新折扣金额标签
     */
    couponTypeSelect.addEventListener("change", () => {
        const selectedType = parseInt(couponTypeSelect.value, 10);
        const labelContainer = document.getElementById("discountAmountLabel");

        if (!labelContainer) {
            console.error("未找到 discountAmountLabel 容器");
            return;
        }

        if (selectedType === 3) {
            labelContainer.innerHTML = `
                折扣力度（如输入 90 表示 9 折）：<br>
                <input type="number" id="discountAmount" min="1" max="1000" required>
            `;
        } else {
            labelContainer.innerHTML = `
                折扣金额（单位：分）：<br>
                <input type="number" id="discountAmount" min="1" required>
            `;
        }
    });

    /**
     * 表单提交逻辑
     */
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const couponName = couponNameInput.value.trim();
        const couponType = parseInt(couponTypeSelect.value, 10);

        // ✅ 使用 querySelector 获取当前可见的 #discountAmount 输入框
        const discountAmountInput = document.querySelector("#discountAmount");
        if (!discountAmountInput) {
            alert("请输入折扣金额/力度");
            return;
        }

        const discountAmountStr = discountAmountInput.value.trim();
        const discountAmount = parseInt(discountAmountStr, 10);

        const minSpend = parseInt(minSpendInput.value, 10);
        const totalStock = parseInt(totalStockInput.value, 10);
        const startTime = startTimeInput.value;
        const endTime = endTimeInput.value;
        const maxPerUser = parseInt(maxPerUserInput.value, 10);

        // 基本验证
        if (!couponName) {
            alert("优惠券名称不能为空");
            return;
        }

        if (isNaN(couponType) || ![1, 2, 3].includes(couponType)) {
            alert("请选择有效的优惠券类型");
            return;
        }

        if (isNaN(discountAmount) || discountAmount <= 0) {
            alert("折扣金额或力度必须大于 0");
            return;
        }

        if ((couponType === 1 || couponType === 2) && (isNaN(minSpend) || minSpend < 0)) {
            alert("最低消费金额必须为有效数字");
            return;
        }

        if (isNaN(totalStock) || totalStock <= 0) {
            alert("总库存必须大于 0");
            return;
        }

        if (!startTime || !endTime) {
            alert("必须设置开始时间和结束时间");
            return;
        }

        let formattedStartTime, formattedEndTime;

        try {
            formattedStartTime = formatDateTime(startTime);
            formattedEndTime = formatDateTime(endTime);
        } catch (e) {
            alert(e.message);
            return;
        }

        if (new Date(formattedStartTime) >= new Date(formattedEndTime)) {
            alert("结束时间必须晚于开始时间");
            return;
        }

        if (isNaN(maxPerUser) || maxPerUser <= 0) {
            alert("每人限领张数必须大于 0");
            return;
        }

        if (couponType === 3 && discountAmount > 1000) {
            alert("折扣力度不能超过 1000");
            return;
        }

        // ✅ 构建请求体
        const payload = {
            couponName,
            couponType,
            discountAmount,
            minSpend,
            totalStock,
            startTime: formattedStartTime,
            endTime: formattedEndTime,
            maxPerUser,
            shopId: numericShopId
        };

        console.log("发送请求:", payload);

        try {
            const response = await fetch("http://TV/VoucherService/createSecKill", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (result.code === 200) {
                alert("✅ 秒杀活动创建成功！");
                window.location.href = "shop-flashsale.html";
            } else {
                alert("❌ 创建失败：" + (result.message || "未知错误"));
            }

        } catch (error) {
            console.error("请求出错:", error);
            alert("无法提交，请检查网络连接");
        }
    });
});