<?php

// 比起去追寻已经失去的东西，自己所剩下的东西更加宝贵

    // 导入wpdb使用
    require_once(dirname( __FILE__ ) . '/wp-blog-header.php');
    global $wpdb;

    $action = $_POST['action'];

    // 用户笔记表操作
    $note_table = $_POST['table'];
    // 表不存在创建表
    if ($wpdb->get_var("show tables like '$note_table'") != $note_table) {
        $sql = "CREATE TABLE " . $note_table . "(
            _id bigint(14) primary key,
            title text,
            date varchar(10),
            time varchar(10),
            content text,
            synced boolean default 0,
            deleted boolean default 0
            )";

        require_once(ABSPATH . "wp-admin/includes/upgrade.php");
        dbDelta($sql);
    }

    if (strcmp($action, "get_data") == 0) {
        // 获取用户笔记数据
        $querystr = "SELECT * FROM $note_table";
        $results = $wpdb->get_results($querystr);
        $n = 0;
        while ($n < count($results)) {
            $arr[$n] = array(
                "_id" => $results[$n]->_id,
                "title" => $results[$n]->title,
                "date" => $results[$n]->date,
                "time" => $results[$n]->time,
                "content" => $results[$n]->content,
                "synced" => $results[$n]->synced,
                "deleted" => $results[$n]->deleted
                );
            $n++;
        }

        echo json_encode($arr, JSON_UNESCAPED_UNICODE);
        // 在这里结束程序
        exit();
    }

    // $note_data = $_POST['note_data'];
    $note_data = base64_decode($_POST['note_data']);
    $noteArr = json_decode($note_data, true);
    if (count($noteArr) <= 0) {
        echo "note_data is null";
        exit();
    }

    switch ($action) {
        // 删除或同步操作 这里作简单处理 把数据更新即可（资源暂且不管）
        case 'delete_note':
        case 'sync_note':
            // 先判断是否存在
            for ($i=0; $i < count($noteArr); $i++) { 
                $arr = $noteArr[$i]; 
                $id = $arr['_id'];
                $querystr = "SELECT * FROM $note_table WHERE _id=$id";
                $note = $wpdb->get_results($querystr);
                if (count($note) == 1) {
                    // 更新数据
                    echo "\n更新: ".$arr['_id'];
                    $wpdb->update($note_table, $arr, array('_id' => $arr['_id']));                
                } else {
                    // 插入数据
                    echo "\n插入: ".$arr['_id'];
                    $wpdb->insert($note_table, $arr);
                }
            }

            break;

        default:
            break;
    }

?>