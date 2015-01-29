(function(){
		//Menu Bar
		var mainApp = angular
		.module('tableApp', ['ui.bootstrap'])

		mainApp.controller('bookingTableCtrl',['$scope','$http', function ($scope,$http,$window) {
		
			 $scope.tabs = [
					{ title:'Calendars'},
					{ title:'Timetable'}
				];
	
			//Change details here
			var CLIENT_ID = "cherian@rislin.com";
			var APPLICATION_ID = "NOTIFIER_ACCOUNT_cherian@rislin.com";
			var KEY = "5f5d1b19d86509b5b8646ab7833b856693d1e731";
			
			//Switch to true to display table
			$scope.show = false;
			
			//Stores objects
			$scope.bookings = [];
			$scope.calendars = [];
			
			$scope.bookingHyperlink = '';
			$scope.getHyperlink = false;
			$scope.calendar_name;
			$scope.calendar_id;
			
			//Currently selected calendar in drop down
			$scope.selectedCalendar;
			
			//Url to calendar/event/recurrence API
			var eventRecurrenceUrl = 'http://rislin.info/Notifive_Calendar/v0-1/calendar/event/recurrence';
			
			var calendarUrl = 'http://rislin.info/Notifive_Calendar/v0-1/calendar/url';
			
			var listCalendarUrl = 'http://rislin.info/Notifive_Calendar/v0-1/calendar';
			
			/******Function to call calendar/list API******/
			$scope.getCalendars = function(){
				//recurrence API
				$http.get(listCalendarUrl,{params:{
					"client-id": CLIENT_ID, "application-id": APPLICATION_ID}})
				.success(function(data){
					console.log(JSON.stringify(data));
					
					var count = 0;
					//Store each calendar into bookings[]
					for(var i=0;i<data.calendar.length;i++){
						var calendar_id =  JSON.stringify(data.calendar[i]["calendar-id"]);
						var calendar_name =  JSON.stringify(data.calendar[i]["calendar-name"]);
						//Add new booking to bookings array
						$scope.calendars.push(new Calendar(calendar_id.replace(/"/g,''),calendar_name.replace(/"/g,'')));
						
						count++;
						if(count === 1){
							$scope.calendar_id = calendar_id.replace(/"/g,'');
							$scope.calendar_name=calendar_name.replace(/"/g,'');
							$scope.selectedCalendar = calendar_id.replace(/"/g,'');
						}
					}
				})
				.error(function(data){
					var description = JSON.stringify(data['response-description'])
					console.log(JSON.stringify(data));
					
					var div = document.getElementById('bookingTable');
					div.innerHTML = div.innerHTML + 'Calendars Not Found.';
				})
			}
			
			/******Function to call event/recurrence and calendar/url******/
			$scope.getBookings = function(){
				$scope.bookings = [];
				//recurrence API
				$http.get(eventRecurrenceUrl,{params:{
					"key":KEY, "calendar-name": $scope.calendar_name,"appointment-type":"appointment"}})
				.success(function(data){
					console.log(JSON.stringify(data));
					
					//Set the booking hyperlink using calendar/url API
					$scope.getHyperlink = !$scope.getHyperlink;
					
					//Store each calendar into calendars[]
					for(var i=0;i<data.recurrences.length;i++){
						var title =  JSON.stringify(data.recurrences[i]["title"]);
						var time =  JSON.stringify(data.recurrences[i]["recurrence-time"]);
						var location =  JSON.stringify(data.recurrences[i]["location"]);
						var description =  JSON.stringify(data.recurrences[i]["description"]);
						
						//Add new calendar to calendars array
						$scope.bookings.push(new Booking(title.replace(/"/g,''),time.replace(/"/g,''),location.replace(/"/g,''),description.replace(/"/g,'')));
					}
					//show table
					$scope.show = true;
				})
				.error(function(data){
					var description = JSON.stringify(data['response-description'])
					console.log(JSON.stringify(data));
				})	
			}
			
			/******Call calendar/url API******/
			$scope.$watch('getHyperlink', function(newValue, oldValue) {
				if(newValue === oldValue){
					return;
				}
				else{	
					console.log($scope.calendar_name);
					//url API
					$http.get(calendarUrl,{params:{
						"client-id":CLIENT_ID,"application-id":APPLICATION_ID,"calendar-name":$scope.calendar_name}})
					.success(function(data){
						console.log(JSON.stringify(data));
						$scope.bookingHyperlink = data['calendar-url'];
					})
					.error(function(data){
						return;
					})
				}
			});
			
			/******Call event/recurrence API If Selected Calendar switches******/
			$scope.$watch('selectedCalendar', function(newValue, oldValue) {
				if(newValue === oldValue){
					return;
				}
				else{	
					$scope.getBookings();
				}
			});
			
			//Function to switch the selected calendar
			$scope.switchCalendar = function(selected_id){
				$scope.calendar_id = selected_id;
				$scope.calendar_name = $scope.getNameById(selected_id);
				$scope.selectedCalendar = selected_id;
				$scope.tabs[1].active = true;
			}
			
			$scope.getNameById = function(id){
				var i;
				for(i=0;i<$scope.calendars.length;i++){
					if($scope.calendars[i].calendar_id===id){
						return $scope.calendars[i].calendar_name;
					}
				}
			}
			
			//Execute getCalendars function when page load
			$scope.getCalendars();	
		}]);
			
		/****************Functions******************/
		
		//Calendar Object
		function Calendar(id,name){
			this.calendar_id = id;
			this.calendar_name = name;

		}
		
		//Add an element to $scope.calendars
		function addBooking(Booking){
			$scope.bookings.push(Booking);
		}
		
		//Booking Object
		function Booking(title,time,location,description){
			this.title = title;
			this.time = time;
			this.location = location;
			this.description = description;

		}
		
	
		
	})();








