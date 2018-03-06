var http = require('http');
var fs = require('fs');//引入文件读取模块

var documentRoot = '/data/user/0/com.learn.wlnutter.nodejstest/files';//需要访问的本地文件的存放目录

var server = http.createServer(function (req, res) {

    var url = req.url;
    //客户端输入的url，例如如果输入
    // http://localhost:8889/index.html
    //那么这里的url == /index.html

    //以下三行代码可已配置默认访问页面为index.html
    // if (url == '/') {
    //     url = '/index.html';
    // }

    var file = documentRoot + url;
    console.log(url);

    //参数1:要读取的文件路径;
    //参数2:回调函数,读取失败的信息在err中,err为空表示没有错误.data为读取到的文件数据
    fs.readFile(file, function (err, data) {
         if (err) {
            res.writeHeader(404, {'content-type': 'text/html;charset="utf-8"'});
            res.write('<h1>404错误</h1><p>你要找的页面不存在</p>');
            res.end();

        } else {
            res.writeHeader(200, {'content-type': 'text/html;charset="utf-8"'});
            res.write(data);
            res.end();}
    });
}).listen(3000);// 此处的8889是监听的端口号,可以根据自己的需要配置,注意不要和本地的一些应用程序使用的端口号冲突

 console.log('服务器开启成功');