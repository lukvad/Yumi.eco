


const express = require('express');
const router = express.Router();
var serviceAccount = require("./scooters-9ba1f-firebase-adminsdk-az4fw-a426b32e42.json");
var admin = require ("firebase-admin");
var timestamp;
var run =  require('./run');
var state;
var engine;
var cost = 0.49;

router.get('/:id/:buf/:bat/:al/:en',(req,res,next) => {
		var math = require('math');
		var date = new Date();
			var al = req.params.al;
			var id = req.params.id;
			var engine = req.params.en;
			var buf = req.params.buf;
			var batt = req.params.bat;
			var bat = range(batt);
			
			var lat = '';
			var lon = '';
			arr = buf.split(',');
			var lastGPS = arr[2];
			var lat = arr[3];
			var lon = arr[4];
			if (!((lat=='')||(lon==''))){
				admin.database().ref().child('time').update({
					[id] : admin.database.ServerValue.TIMESTAMP
				});
				admin.database().ref('service').child('scooters/'+id).update({
					latitude : lat,
					longitude : lon,
					battery : bat,
				});


			}
			else {
					admin.database().ref('service').child('scooters/'+id).update({
						battery : bat,
						engine : engine
					});
			}
			admin.database().ref('control').child(id).update({
				engine : engine
			});

		admin.database().ref('service').child('scooters/'+id).once('value').then(function(snap) {
			state = snap.child('state').val();
			res.send(state);
			var check = snap.child('check').val();
			var userKey = snap.child('userKey').val();
			var scooterDetails = snap.child('name').val();
			var timestamp = snap.child('start').val();
			var start = new Date((snap.child('start').val()));
			var day = start.getDate();
			if(day.toString.length<2){
				day= "0"+day;
			}
			var month = start.getMonth();
			var year = start.getFullYear();
			var hour = start.getHours();
			var minutes = start.getMinutes();
			var timer = math.abs(date-start);
			var months= ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"];
			if(check==true){
					admin.database().ref('service').child('scooters/'+id+'/check').remove();
					admin.database().ref('service').child('scooters/'+id).update({
						feedback : true
					})
			}
			if(state==='*hE&'){
				admin.database().ref('service').child('scooters/'+id).update({state : '*oN&'});
			}
			if(userKey!==""){
			admin.database().ref('service').child('users/'+ userKey).once('value').then(function(snap) {
				var balance = snap.child('balance').val();
				var exCharge = snap.child('charge').val()
				var charge = math.ceil(timer/60000)-1;
				if (charge>138){charge=138;} 
				if (exCharge+charge>138){charge=138-exCharge;} 
				var newBalance = balance - (charge*cost) ;


				if((state==='*oN&')||(state==='*pA&')){
					// if((newBalance <=1)&&(newBalance>-6)){
					// 	admin.database().ref('service').child('users/'+userKey).update({alert : 'low balance'});
					// 	admin.database().ref('service').child('scooters/'+id).update({state : '*lIm&'});
					// }
					if(newBalance < -6){
						if(userKey!==''){
								admin.database().ref().child('alarms').child(id).update({
								stand : true,
								time : new Date(),
								state : "active"
							})
						}
						// admin.database().ref('service').child('users/'+userKey).update({alert : 'no balance'});
						// admin.database().ref('service').child('scooters/'+id).update({state : '*lIm&'});
					}


					if(timer>=86400000){
						admin.database().ref('service').child('users/'+userKey).update({alert : 'full day'});
						admin.database().ref('service').child('scooters/'+id).update({state : '*fIn&'});
					}//TODO ALARM
					// if(bat<2){
					// 	admin.database().ref('service').child('users/'+userKey).update({alert : 'no battery'});
					// 	admin.database().ref('service').child('scooters/'+id).update({state : '*lIm&'});
					// }//TODO ALARM
				}
				else if(state==='*fIn&'){
					admin.database().ref('service').child('scooters/'+id).update({state : '*oF&'});
					admin.database().ref('service').child('users/'+userKey).update({charge : charge+exCharge});
					admin.database().ref('service').child('scooters/'+id).update({userKey: ''});
					admin.database().ref('service').child('users/'+userKey).update({
						balance : newBalance,
						scooterName : ''
					});
					admin.database().ref('service').child('history/'+userKey+'/'+year+'-'+months[month]+'-'+day+'-'+hour+'-'+minutes).update({
						duration : timer,
						cost : ((charge*cost).toFixed(2)).toString(),
						timestamp :  timestamp*-1,
						scooterName : scooterDetails
					});
				}
				else if ((state==='*rE&')&&(timer>900000)){
					admin.database().ref('service').child('users/'+userKey).update({alert : 'overtime'});
					admin.database().ref('service').child('scooters/'+id).update({state : '*oF&'});
					admin.database().ref('service').child('scooters/'+id).update({userKey : ''});
					admin.database().ref('service').child('users/'+userKey).update({scooterName : ''});	
				}
			});	
		} 
		});
});	 

function range(battery){
	var kms = Math.round((parseInt(battery)-800)/4.46);
	if(kms<0){
		kms=0;
	}
	return kms.toString();
}
module.exports = router;