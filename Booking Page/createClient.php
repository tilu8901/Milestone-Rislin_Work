<?php

$key = array_key_exists('key', $_GET) ? $_GET['key'] : null;
$user = array_key_exists('user', $_GET) ? $_GET['user'] : null;

if($key!=null && $user!=null){
	
	//make directory
	
	mkdir($user, 0777, true);
	
	$file = 'book/js/tableApp_tab.js';
	$newfile = $user.'/tableApp_tab.js';
	if (!copy($file, $newfile)) {
		header('HTTP/1.1 500 Internal Server Error');
		echo "failed to copy $file...\n";
		return;
	}
	$file = 'book/timetable_tab.html';
	$newfile = $user.'/timetable_tab.html';
	if (!copy($file, $newfile)) {
		header('HTTP/1.1 500 Internal Server Error');
		echo "failed to copy $file...\n";
		return;
	}
	
	//insert key to tableApp_tabs.js
	
	$fp=fopen($user.'/tableApp_tab.js','c');
	fseek($fp,420);
	fwrite($fp, "var KEY ='".$key."';");
	fclose($fp);
	header("HTTP/1.1 200 OK");
	echo 'User Folder '.$user.' Created.';
}
else{
	header('HTTP/1.1 500 Internal Server Error');
	echo 'Must provide key and user';
} 


?>