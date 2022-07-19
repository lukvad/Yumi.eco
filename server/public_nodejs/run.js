const express = require('express');
const run = express();
var serviceAccount = require("./scooters-9ba1f-firebase-adminsdk-az4fw-a426b32e42.json");
var admin = require ("firebase-admin");
// var cronJob = require("cron").CronJob;
admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
	databaseURL: "https://scooters-9ba1f.firebaseio.com",
	databaseAuthVariableOverride: {
		uid: "my-service"
	}
});

const tokenRoute = require('./main');

var Users = [];
var i=0;
var lat=0;
var lon=0;
var bat=0;
var state;
var clock;
var no = 30;
var histContr;
var userKey;
var Users=[];
var i = 0;


run.use('/945229',tokenRoute);

admin.database().ref('Users').on('child_changed', function(snap){
	var user = snap.val();
	var key = snap.key;
	var name = user.firstname;
	if(user.firstname==="delete"){
		admin.auth().deleteUser(key);
		admin.database().ref('Users').child(key).remove();
	}
})

	//DODANIE SKUTERÓW DO PAMIĘCI RAM 
	admin.database().ref('service').child('scooters/'+no).on('value', function(snap){
		lat = snap.child('latitude').val();
		lon = snap.child('longitude').val();
		state = snap.child('state').val();
		bat = snap.child('battery').val();
		userKey = snap.child('userKey').val();
		// exports.lat=lat;
		// exports.lon=lon;
		// exports.bat=bat;
	});


// admin.database().ref('service').child('users/').on('child_added', function(snap){
// 	Users[i] = snap.key;
// 	i++
// });
admin.database().ref().child('time/'+no).on('value', function(snap){
	clock = snap.val();
});

admin.database().ref().child('control/'+no).on('value', function(snap){

	histContr = snap.child("history").val();
});



setInterval(function (){

	admin.database().ref().child('admin/'+no).orderByKey().limitToLast(1).once('child_added', function(snap){
		if((Math.abs(parseFloat(lat)-parseFloat(snap.child('latitude').val()))>0.0005)
			||(Math.abs(parseFloat(lon)-parseFloat(snap.child('longitude').val()))>0.0005)){
				if(((snap.child('state').val()==='*oF&')||(snap.child('state').val()==='*rE&'))&&(state==='*oF&')){
					admin.database().ref().child('alarms').child(no).update({
						gpsDrift : true,
						time : new Date(),
						state : "active"
					})
				}
		}

		if(new Date() - new Date(parseInt(snap.key))>240000){
			admin.database().ref().child('alarms').child(no).update({
				gpsOff : true,
				time : new Date(),
				state : "active"
			})
		}
	});
},10000);

setInterval(function (){
	if(histContr==='run'){
		admin.database().ref().child('admin/'+no).orderByKey().limitToFirst(1).once('child_added', function(snap){
			admin.database().ref().child('admin/'+no).child(snap.key).remove();
		});
	}
	if(histContr==='delete'){
		admin.database().ref().child('admin/'+no).remove();
	}
	admin.database().ref().child('control/'+no).once('value', function(snap){
		var hack = snap.child('engine').val();
		if (hack==='E'){
			admin.database().ref().child('alarms').child(no).update({
				hack : true,
				time : new Date(),
				state : "active"
			})
		}
		admin.database().ref().child('control/'+no).update({
			engine : 'E'
		})
	});



		admin.database().ref().child('admin/'+no).child(clock).update({
			latitude : lat,
			longitude : lon,
			battery : bat,
			state : state,
			userKey : userKey
		})

	
},60000);

// new cronJob("59 59 23 * * *", function() {
// 	for (i in Users){
// 		admin.database().ref('service').child('users').child(Users[i]).update({charge : 0});
// 	}
// }, null, true);



module.exports = run;
