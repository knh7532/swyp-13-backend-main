<script>
    const Authorization = localStorage.getItem('Authorization');
    const Refresh_Token = localStorage.getItem('Refresh-Token');
    const accessExpire = localStorage.getItem('accessExpire');
    const refreshExpire = localStorage.getItem('refreshExpire');

    // 로그인 되지 않았다면 로그인 창으로 팅기게 한다.
    if (Authorization === null || Refresh_Token === null || accessExpire === null || refreshExpire === null) {
        location.href = "/login";
    }

    $.ajax({
        type: "GET",
        url: "/tokenVerify",
        headers: {'Authorization': Authorization,
            'Refresh-Token': Refresh_Token,
            'accessExpire': accessExpire,
            'refreshExpire': refreshExpire,},
        contentType: "application/json; charset=utf-8"
    }).done(function(resp, status, xhr) {
        if (resp === -1) { // 비정상적인 토큰
            alert("세션이 만료되어 로그인 페이지로 이동합니다.");

            localStorage.removeItem('Authorization');
            localStorage.removeItem('Refresh-Token');
            localStorage.removeItem('accessExpire');
            localStorage.removeItem('refreshExpire');

            location.href = "/login"
        } else if (resp === 2) {
            reissuedtoken = xhr.getResponseHeader('ReissuedToken');
            reissuedExpire = xhr.getResponseHeader('reissuedExpire')

            localStorage.removeItem('Authorization');
            localStorage.removeItem('accessExpire');

            localStorage.setItem('Authorization', reissuedtoken);
            localStorage.setItem('accessExpire', reissuedExpire);
        }
    }).fail(function(error) {
        alert("에러 발생 (콘솔 확인)");
        console.log(error);
    });
</script>