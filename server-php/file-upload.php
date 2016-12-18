<?php
    
    // 用户的资源目录
    $dir = dirname( __FILE__ )."/note_res"."/".$_POST['dir'];
    if (!is_dir($dir)) {
        mkdir($dir, 0777, true);
    }
    // 用户要操作的文件名
    // $filename = $_POST['file_name'];

    // 以写方式打开 不存在创建文件
    // $file = fopen($dir."/".$filename, "w");

    // $data = base64_decode($_POST['img']);

    // fwrite($file, $data);
    // fclose($file);

    if(move_uploaded_file($_FILES['file']['tmp_name'], $dir."/".($_FILES['file']['name']))) {
       echo "The file ".($_FILES['file']['name'])." has been uploaded.";
    }else{  
       echo "There was an error uploading the file, please try again! Error Code: ".$_FILES['file']['error'];
    }

?>