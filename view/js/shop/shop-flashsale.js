document.addEventListener('DOMContentLoaded', () => {
    const voucherTableBody = document.getElementById("voucher-table-body");

    const tabExpired = document.getElementById("tab-expired");
    const tabOngoing = document.getElementById("tab-ongoing");
    const tabUpcoming = document.getElementById("tab-upcoming");

    let currentTab = "ongoing"; // 默认加载正在进行的活动

    // 获取店铺 ID
    const shopId = localStorage.getItem("shopId");
    if (!shopId) {
        alert("未找到商铺 ID，请重新登录");
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
     * 加载秒杀活动数据
     */
    function loadFlashSales(sortType) {
        fetch("http://TV/VoucherService/getALLSecKillInfo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                sortType: sortType
            })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("收到秒杀活动数据:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    voucherTableBody.innerHTML = ""; // 清空旧数据

                    if (records.length === 0) {
                        const row = document.createElement("tr");
                        row.innerHTML = `<td colspan="8">暂无符合条件的秒杀活动</td>`;
                        voucherTableBody.appendChild(row);
                        return;
                    }

                    records.forEach(voucher => {
                        const row = document.createElement("tr");

                        // 类型映射
                        const typeMap = {
                            1: "立减卷",
                            2: "满减卷",
                            3: "折扣卷"
                        };
                        const typeName = typeMap[voucher.couponType] || "未知类型";

                        // 折扣金额处理（分 → 元 或 折扣）
                        let discountText = "";
                        if (voucher.couponType === 1 || voucher.couponType === 2) {
                            discountText = `${voucher.discountAmount / 100} 元`;
                        } else if (voucher.couponType === 3) {
                            discountText = `${voucher.discountAmount / 10} 折`;
                        } else {
                            discountText = "-";
                        }

                        // 最低使用额度（分 → 元）
                        const minSpendText = voucher.minSpend > 0 ? `${voucher.minSpend / 100} 元` : "无门槛";

                        // 时间格式化
                        const timeRange = `${voucher.startTime} ~ ${voucher.endTime}`;

                        // 构建表格行
                        row.innerHTML = `
                             <td>${voucher.couponName}</td>
                             <td>${typeName}</td>
                             <td>${discountText}</td>
                             <td>${minSpendText}</td>
                             <td>${voucher.totalStock}</td>
                             <td>${voucher.availableStock}</td>
                             <td>${voucher.maxPerUser} 张</td>
                             <td>${timeRange}</td>
                             <td><a href="shop-coupon-users.html?couponId=${voucher.couponId}" class="btn small-btn">查看参与者</a></td>
                        `;

                        voucherTableBody.appendChild(row);
                    });

                } else {
                    voucherTableBody.innerHTML = `<tr><td colspan="8">无法加载数据：${data.message}</td></tr>`;
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                voucherTableBody.innerHTML = `<tr><td colspan="8">请求失败，请检查网络或重试</td></tr>`;
            });
    }

    /**
     * 绑定 tab 切换事件
     */
    function setupTab(tabBtn, sortType) {
        tabBtn.addEventListener("click", () => {
            // 更新激活状态
            [tabExpired, tabOngoing, tabUpcoming].forEach(btn => btn.classList.remove("active"));
            tabBtn.classList.add("active");

            // 切换 tab 并加载数据
            currentTab = sortType;
            loadFlashSales(currentTab);
        });
    }

    setupTab(tabOngoing, "ongoing");
    setupTab(tabExpired, "expired");
    setupTab(tabUpcoming, "upcoming");

    // 初始加载
    loadFlashSales(currentTab);
});