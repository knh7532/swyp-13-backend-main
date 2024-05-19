<div id="">
    잠시만 기다려 주세요...
    <script>
        localStorage.setItem('Authorization', '${accessToken}')
        localStorage.setItem('Refresh-Token', '${refreshToken}')
        localStorage.setItem('accessExpire', '${accessExpire}')
        localStorage.setItem('refreshExpire', '${refreshExpire}')

        setTimeout(function() {
            window.location.href = "/index";
        }, 1500);
    </script>
</div>