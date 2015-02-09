<?php

$key = array_key_exists('key', $_GET) ? $_GET['key'] : null;
$user = array_key_exists('user', $_GET) ? $_GET['user'] : null;

/*Change redirect URL here*/
$REDIRECT_URL = "Location: http://www.rislin.info/book/".$user."/timetable.html";

if($key!=null && $user!=null){
	
	//make directory
	
	if(!mkdir($user, 0777, true)){
		header('HTTP/1.1 500 Internal Server Error');
		echo "Folder $user already exists...\n";
		return;
	}
	
	//copy tableApp_tab.js
	
	$file = 'js/tableApp_tab.js';
	$newfile = $user.'/tableApp_tab.js';
	if (!copy($file, $newfile)) {
		header('HTTP/1.1 500 Internal Server Error');
		echo "failed to copy $file...\n";
		return;
	}
	
	//copy timetable.html
	
	$file = 'timetable.html';
	$newfile = $user.'/timetable.html';
	if (!copy($file, $newfile)) {
		header('HTTP/1.1 500 Internal Server Error');
		echo "failed to copy $file...\n";
		return;
	}
	
	//insert key to tableApp_tabs.js
	
	$fp=fopen($user.'/tableApp_tab.js','c');
	fseek($fp,420);
	if(!fwrite($fp, "var KEY ='".$key."';")){
		fclose($fp);
		header('HTTP/1.1 500 Internal Server Error');
		echo "failed to write to tableApp_tab.js...\n";
		return;
	}
	
	fclose($fp);
	
	header("HTTP/1.1 200 OK");
	
	//redirect
	header($REDIRECT_URL); 
	
	
}
else{
	header('HTTP/1.1 500 Internal Server Error');
	echo 'Must provide key and user';
} 


?>