# CloudNote

----------

一个简易的云笔记项目。具有用户注册与登录，笔记的相关操作，同时可同步到云端存储。

## 环境
Android Studio 2.2.X

服务器在wordpress(4.6.1 版本)的基础上使用PHP操作，使用XAMPP(Windows 3.2.2版本)软件，由于涉及视频文件的上传，需要设置Apache服务器的php.ini配置文件：  
post_max_size=512M  
upload_max_filesize=512M

PHP代码（放入wordpress目录下）：  
note-login.php 用户注册、登录的操作  
note-db.php 数据表的操作  
file-upload.php 文件上传的操作

注：使用第三方后端云进行开发效果更佳！

## 软件截图
![](http://i.imgur.com/bZpbsHU.png)
