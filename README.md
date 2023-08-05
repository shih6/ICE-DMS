# ICE DMS
使用nginx部署时注意  
需要修改以下设置
重点在`client_max_body_size`和`proxy_read_timeout`  
否则会出现文件无法上传或在大文件上传到100% 60s后出现`504`超时
```nginx
http{
    client_max_body_size 60000m;
    #自定义变量 $connection_upgrade
    map $http_upgrade $connection_upgrade { 
        default          keep-alive;  #默认为keep-alive 可以支持 一般http请求
        'websocket'      upgrade;     #如果为websocket 则为 upgrade 可升级的。
    }
    server {
        #修改这里
        listen       80;
        server_name  localhost;

        location /
        {
            expires 12h;
            if ($request_uri ~* "(php|jsp|cgi|asp|aspx)")
            {
                 expires 0;
            }
        #修改这里
            proxy_pass http://127.0.0.1:5173/;
            #proxy_connect_timeout    30;  #nginx跟后端服务器连接超时时间(代理连接超时)默认60s
            proxy_read_timeout       1800; #后端服务器数据回传时间(代理发送超时)默认值60s   
            # proxy_send_timeout       1800;  #连接成功后，后端服务器响应时间(代理接收超时)默认值60s
            proxy_set_header Host $host;
            proxy_set_header Upgrade $http_upgrade; #此处配置 上面定义的变量
            
            proxy_set_header Connection $connection_upgrade;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header REMOTE-HOST $remote_addr;
            add_header Cache-Control no-cache;
            add_header X-Cache $upstream_cache_status;
            proxy_set_header Accept-Encoding "";
            sub_filter_once off;
        }
    }
}
```