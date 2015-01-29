<?php

$key = array_key_exists('key', $_GET) ? $_GET['key'] : null;
$user = array_key_exists('user', $_GET) ? $_GET['user'] : null;

if($key!=null && $user!=null){
	$file = 'js/tableApp_tab.js';
	$newfile = $user.'/tableApp_tab.js';
	//make directory
	mkdir($user, 0777, true);

	
	function recurse_copy($src,$dst) { 
		$dir = opendir($src); 
		@mkdir($dst); 
		while(false !== ( $file = readdir($dir)) ) { 
			if (( $file != '.' ) && ( $file != '..' )) { 
				if ( is_dir($src . '/' . $file) ) { 
					recurse_copy($src . '/' . $file,$dst . '/' . $file); 
				} 
				else { 
					copy($src . '/' . $file,$dst . '/' . $file); 
				} 
			} 
		} 
		closedir($dir); 
	} 
	recurse_copy('book',$user);
	
	$fp=fopen($user.'/js/tableApp_tab.js','c');
	fseek($fp,0,SEEK_END);
	fwrite($fp, "var userKey ='".$key."';");
	fclose($fp);
	
}
else echo 'Must provide key and user';


?>