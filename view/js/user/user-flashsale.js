document.addEventListener('DOMContentLoaded', () => {
    const voucherList = document.getElementById("voucher-list");

    const tabOngoing = document.getElementById("tab-ongoing");
    const tabExpired = document.getElementById("tab-expired");
    const tabUpcoming = document.getElementById("tab-upcoming");

    let currentTab = "ongoing"; // 默认排序方式为 进行中

    // 切换 tab 并重新加载数据
    function setupTab(tabBtn, type) {
        tabBtn.addEventListener("click", () => {
            [tabOngoing, tabExpired, tabUpcoming].forEach(btn => btn.classList.remove("active"));
            tabBtn.classList.add("active");
            currentTab = type;
            loadFlashSales(currentTab);
        });
    }

    setupTab(tabOngoing, "ongoing");
    setupTab(tabExpired, "expired");
    setupTab(tabUpcoming, "upcoming");

    // 加载秒杀活动列表
    function loadFlashSales(sortType) {
        fetch("http://TV/VoucherService/getALLSecKillInfo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ sortType })
        })
            .then(response => {
                if (!response.ok) throw new Error("网络响应异常：" + response.status);
                return response.json();
            })
            .then(data => {
                console.log("收到秒杀活动:", data);

                if (data.code === 200 && Array.isArray(data.data?.data)) {
                    const records = data.data.data;

                    voucherList.innerHTML = ""; // 清空旧数据

                    if (records.length === 0) {
                        const row = document.createElement("tr");
                        row.innerHTML = `<td colspan="4">暂无符合条件的秒杀活动</td>`;
                        voucherList.appendChild(row);
                        return;
                    }

                    records.forEach(voucher => {
                        const row = document.createElement("tr");

                        // 类型映射
                        const typeMap = {
                            1: "立减券",
                            2: "满减券",
                            3: "折扣券"
                        };
                        const typeName = typeMap[voucher.couponType] || "未知类型";

                        // 构建表格行
                        row.innerHTML = `
                            <td>${voucher.couponName}</td>
                            <td>${typeName}</td>
                            <td>${voucher.maxPerUser} 张</td>
                            <td><a href="user-coupon-detail.html?couponId=${voucher.couponId}" class="btn small-btn">查看详情</a></td>
                        `;

                        voucherList.appendChild(row);
                    });

                } else {
                    alert("获取动态失败：" + (data.message || "数据异常"));
                    voucherList.innerHTML = `<tr><td colspan="4">${data.message}</td></tr>`;
                }
            })
            .catch(error => {
                console.error("请求出错:", error);
                alert("无法获取秒杀活动，请检查网络或重试");
                voucherList.innerHTML = "<tr><td colspan='4'>请求失败，检查网络或重试</td></tr>";
            });
    }

    // 初始加载
    loadFlashSales(currentTab);
});