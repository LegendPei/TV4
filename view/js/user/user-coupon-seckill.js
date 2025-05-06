document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const couponId = urlParams.get("couponId");
    const orderId = urlParams.get("orderId");
    const expireTimeString = urlParams.get("expireTime");

    const countdownEl = document.getElementById("payment-countdown");
    const btnPaid = document.getElementById("btn-paid");
    const btnCancel = document.getElementById("btn-cancel");

    if (!couponId || !orderId || !expireTimeString) {
        alert("无效请求，请重新进入");
        window.location.href = "user-flashsale.html";
        return;
    }

    const numericCouponId = parseInt(couponId, 10);
    const numericOrderId = parseInt(orderId, 10);
    const expireTime = parseInt(expireTimeString, 10); // 时间戳（毫秒）

    // ✅ 开始倒计时
    startPaymentCountdown(expireTime, countdownEl);

    // ✅ 点击“已支付”
    btnPaid.addEventListener("click", async () => {
        try {
            const response = await fetch("http://TV/VoucherService/confirmPayment", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    orderId: numericOrderId,
                    couponId: numericCouponId,
                    userId: localStorage.getItem("userId")
                })
            });

            const result = await response.json();

            if (result.code === 200) {
                alert("✅ 支付成功！");
                window.location.href = "user-index.html";
            } else {
                alert("❌ 支付确认失败：" + (result.message || "未知错误"));
            }

        } catch (error) {
            console.error("支付确认请求出错:", error);
            alert("支付确认失败，请检查网络连接");
        }
    });

    // ✅ 点击“取消支付”
    btnCancel.addEventListener("click", async () => {
        if (!confirm("确定要取消支付吗？")) return;

        try {
            const response = await fetch("http://TV/VoucherService/cancelPayment", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    orderId: numericOrderId,
                    couponId: numericCouponId,
                    userId: localStorage.getItem("userId")
                })
            });

            const result = await response.json();

            if (result.code === 200) {
                alert("✅ 取消支付成功");
                window.location.href = "user-flashsale.html";
            } else {
                alert("❌ 取消支付失败：" + (result.message || "未知错误"));
            }

        } catch (error) {
            console.error("取消支付请求出错:", error);
            alert("取消支付失败，请检查网络连接");
        }
    });
});

/**
 * 倒计时函数：从当前时间到 expireTime 的剩余时间
 */
function startPaymentCountdown(expireTime, countdownEl) {
    const timer = setInterval(() => {
        const now = Date.now();
        const remaining = expireTime - now;

        if (remaining <= 0) {
            clearInterval(timer);
            countdownEl.textContent = "支付超时，订单已失效";
            document.getElementById("btn-paid").disabled = true;
            document.getElementById("btn-cancel").disabled = true;
            return;
        }

        const minutes = Math.floor((remaining / 1000 / 60) % 60);
        const seconds = Math.floor((remaining / 1000) % 60);

        countdownEl.textContent = `剩余支付时间：${minutes} 分 ${seconds} 秒`;
    }, 1000);
}