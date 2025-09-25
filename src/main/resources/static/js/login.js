const href = location.href;
const queryString = href.substring(href.indexOf("?") + 1)
if (queryString === 'error') {
    const errorDiv = document.getElementById('login-failed');
    errorDiv.style.display = 'block';
}

const host = 'http://' + window.location.host;

function onLogin() {
    let username = $('#username').val();
    let password = $('#password').val();

    $.ajax({
        type: "POST",
        url: `/api/user/login`,
        contentType: "application/json",
        data: JSON.stringify({username: username, password: password}),
    })
        .done(function (res, status, xhr) {
            const authHeader = xhr.getResponseHeader('Authorization');
            if (!authHeader) {
                alert('로그인 토큰이 응답 헤더에 없습니다.');
                return;
            }

            localStorage.setItem('Authorization', authHeader);
            window.location.replace('/');
        })
        .fail(function (jqXHR, textStatus) {
            alert("Login Fail");
            window.location.href = host + '/api/user/login-page?error'
        });
}