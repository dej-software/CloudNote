<?php 
    // 用户登录相关返回值：
    // "@login_success"      登录：登录成功
    // "@pass_error"         登录：密码错误
    // "@user_error"         登录：用户名错误
    // "@user_exist"         注册：用户名已存在
    // "@register_success"   注册：注册成功
    // "@register_failed"    注册：注册失败（服务器插入数据时出错）
    
    // 导入wpdb使用
    require_once(dirname( __FILE__ ) . '/wp-blog-header.php');
    global $wpdb;
    // 密码相关
    global $wp_hasher;  
    if (empty($wp_hasher)) {  
        require_once( './wp-includes/class-phpass.php');  
        $wp_hasher = new PasswordHash(8, TRUE);  
    }

    $action = $_GET['action'];
    $user = $_GET['user'];
    $pass = $_GET['pass'];

    // 用户表操作
    $user_table = "note_users";
    // 表不存在创建表
    if ($wpdb->get_var("show tables like '$user_table'") != $user_table) {
        $sql = "CREATE TABLE " . $user_table . "(
            id bigint(20) unsigned primary key NOT NULL AUTO_INCREMENT,
            user_login varchar(60) NOT NULL DEFAULT '',
            user_pass varchar(255) NOT NULL DEFAULT '',
            user_email varchar(100) NOT NULL DEFAULT '',
            user_registered datetime NOT NULL DEFAULT '0000-00-00 00:00:00'
            )";

        require_once("./wp-admin/includes/upgrade.php");
        dbDelta($sql);
    }

    $sigPassword = $wp_hasher->HashPassword($pass);  

    // 要插入数据表的用户数组
    $arr = array(
        'user_login' => $user,
        'user_pass' =>  $sigPassword,
        'user_registered' => date('Y-m-d H:i:s', time())
        );


    // 查询用户是否已存在，获取到的是数据表中对应的ID值
    $result = $wpdb->get_var("select * from $user_table where user_login='$user'");
    if ($result != NULL) {

        switch ($action) {
            case 'login':
                $querystr = "SELECT * FROM $user_table WHERE id=$result";
                $results = $wpdb->get_results($querystr);
                if ((count($results) == 1) && $wp_hasher->CheckPassword($pass, $results[0]->user_pass)) {
                    echo "@login_success"; // 登录：登录成功
                    // 如果资源目录不存在，创建资源目录
                    $dir = dirname( __FILE__ )."/note_res"."/$user"."_res";
                    if (!is_dir($dir)) {
                        mkdir($dir, 0777, true);
                    }
                } else {
                    echo "@pass_error"; // 登录：密码错误
                }
                break;

            case 'register':
                echo "@user_exist"; // 注册：用户名已存在
                break;

            default:
                break;
        }
    } else {
        
        switch ($action) {
            case 'login':
                echo "@user_error"; // 登录：用户名错误
                break;

            case 'register':
                if ($wpdb->insert($user_table, $arr)) {
                    echo "@register_success"; // 注册：注册成功
                } else {
                    echo "@register_failed"; // 注册：注册失败（服务器插入数据时出错）
                }
                break;

            default:
                break;
        }
    }

?>