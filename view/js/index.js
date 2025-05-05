// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    // 获取按钮元素
    const userLoginBtn = document.getElementById('user-login');
    const userRegisterBtn = document.getElementById('user-register-btn');
    const shopLoginBtn = document.getElementById('shop-login');
    const shopRegisterBtn = document.getElementById('shop-register');

    // 用户登录按钮点击事件
    if (userLoginBtn) {
        userLoginBtn.addEventListener('click', () => {
            window.location.href = '../html/user/user-login.html'; // 跳转到用户登录页面
        });
    }

    // 用户注册按钮点击事件
    if (userRegisterBtn) {
        userRegisterBtn.addEventListener('click', () => {
            window.location.href = '../html/user/user-register.html'; // 跳转到用户注册页面
        });
    }

    // 商铺登录按钮点击事件
    if (shopLoginBtn) {
        shopLoginBtn.addEventListener('click', () => {
            window.location.href = '../html/shop/shop-login.html'; // 跳转到商铺登录页面
        });
    }

    // 商铺注册按钮点击事件
    if (shopRegisterBtn) {
        shopRegisterBtn.addEventListener('click', () => {
            window.location.href = '../html/shop/shop-register.html'; // 跳转到商铺注册页面
        });
    }
});